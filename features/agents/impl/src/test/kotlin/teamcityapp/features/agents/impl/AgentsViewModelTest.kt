/*
 * Copyright 2026 Andrey Tolpeev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package teamcityapp.features.agents.impl

import androidx.lifecycle.ViewModelStore
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import teamcityapp.features.agents.api.Agent
import teamcityapp.features.agents.api.AgentsFilter
import teamcityapp.features.agents.api.AgentsRepository
import teamcityapp.libraries.list_state.ListUiState

@OptIn(ExperimentalCoroutinesApi::class)
class AgentsViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val store = ViewModelStore()
    private val rows = listOf(Agent("1", "Linux agent"))
    private val repository = FakeRepository()

    @Before fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After fun tearDown() {
        store.clear()
        Dispatchers.resetMain()
    }

    private fun viewModel() = AgentsViewModel(repository).also { store.put("agents", it) }
    private fun TestScope.observe(vm: AgentsViewModel) = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.state.collect {} }

    @Test fun initialLoadingIsCollectionDrivenAndFirstResumeDoesNotDuplicateIt() = runTest(dispatcher) {
        val vm = viewModel()
        vm.onResumed()
        runCurrent()
        assertTrue(repository.calls.isEmpty())
        observe(vm)
        runCurrent()
        assertEquals(listOf(AgentsFilter.Connected to false), repository.calls)
        assertEquals(ListUiState.Content(rows), vm.state.value.list)
        assertEquals(R.string.agents_empty_connected, vm.state.value.emptyMessageRes)
    }

    @Test fun changedFilterCancelsOldRequestAndLoadsDisconnectedAgents() = runTest(dispatcher) {
        var cancelled = false
        repository.load = { filter, _ ->
            if (filter == AgentsFilter.Connected) {
                try {
                    awaitCancellation()
                } finally {
                    cancelled = true
                }
            }
            emptyList()
        }
        val vm = viewModel()
        observe(vm)
        runCurrent()
        repository.filter.value = AgentsFilter.Disconnected
        runCurrent()
        assertTrue(cancelled)
        assertEquals(AgentsUiState(AgentsFilter.Disconnected, ListUiState.Empty()), vm.state.value)
        assertEquals(R.string.agents_empty_disconnected, vm.state.value.emptyMessageRes)
        assertEquals(listOf(AgentsFilter.Connected to false, AgentsFilter.Disconnected to false), repository.calls)
    }

    @Test fun everyFilterSwitchEmissionKeepsRowsAssociatedWithTheirOwnFilter() = runTest(dispatcher) {
        val vm = viewModel()
        val emissions = mutableListOf<AgentsUiState>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.state.collect { emissions += it } }
        runCurrent()
        val result = CompletableDeferred<List<Agent>>()
        repository.load = { _, _ -> result.await() }
        repository.filter.value = AgentsFilter.Disconnected
        runCurrent()
        assertEquals(AgentsUiState(AgentsFilter.Disconnected, ListUiState.Loading), vm.state.value)
        val disconnected = listOf(Agent("2", "Offline agent"))
        result.complete(disconnected)
        runCurrent()
        assertEquals(AgentsUiState(AgentsFilter.Disconnected, ListUiState.Content(disconnected)), vm.state.value)
        assertFalse(emissions.any { it.filter == AgentsFilter.Disconnected && (it.list as? ListUiState.Content<Agent>)?.items == rows })
    }

    @Test fun pullRefreshBypassesCacheAndKeepsCompletedRows() = runTest(dispatcher) {
        val vm = viewModel()
        observe(vm)
        runCurrent()
        val result = CompletableDeferred<List<Agent>>()
        repository.load = { _, _ -> result.await() }
        vm.refresh()
        runCurrent()
        assertEquals(ListUiState.Content(rows, isRefreshing = true), vm.state.value.list)
        assertEquals(AgentsFilter.Connected to true, repository.calls.last())
        result.complete(emptyList())
        runCurrent()
        assertEquals(ListUiState.Empty(), vm.state.value.list)
    }

    @Test fun failedRefreshKeepsRowsAndRetryLoadsFreshData() = runTest(dispatcher) {
        val vm = viewModel()
        observe(vm)
        runCurrent()
        repository.load = { _, _ -> error("offline") }
        vm.refresh()
        runCurrent()
        assertEquals(ListUiState.Content(rows, refreshFailed = true), vm.state.value.list)
        repository.load = { _, _ -> rows }
        vm.retry()
        runCurrent()
        assertEquals(ListUiState.Content(rows), vm.state.value.list)
        assertEquals(R.string.agents_empty_connected, vm.state.value.emptyMessageRes)
        assertEquals(AgentsFilter.Connected to true, repository.calls.last())
    }

    @Test fun lastCollectorLeavingCancelsWithoutAnErrorAndReturnRetries() = runTest(dispatcher) {
        var cancelled = false
        repository.load = { _, _ ->
            try {
                awaitCancellation()
            } finally {
                cancelled = true
            }
        }
        val vm = viewModel()
        val collector = observe(vm)
        runCurrent()
        collector.cancel()
        runCurrent()
        assertTrue(cancelled)
        assertEquals(ListUiState.Loading, vm.state.value.list)
        repository.load = { _, _ -> rows }
        observe(vm)
        runCurrent()
        assertEquals(ListUiState.Content(rows), vm.state.value.list)
        assertEquals(R.string.agents_empty_connected, vm.state.value.emptyMessageRes)
        assertEquals(2, repository.calls.size)
    }

    @Test fun configurationRecreationRetainsContentWithoutReloading() = runTest(dispatcher) {
        val vm = viewModel()
        vm.onResumed()
        val collector = observe(vm)
        runCurrent()
        vm.onPaused()
        collector.cancel()
        runCurrent()
        vm.onConfigurationRecreation(wasVisible = true)
        vm.onResumed()
        observe(vm)
        runCurrent()
        assertEquals(1, repository.calls.size)
        assertEquals(ListUiState.Content(rows), vm.state.value.list)
        assertEquals(R.string.agents_empty_connected, vm.state.value.emptyMessageRes)
    }

    @Test fun hiddenTabRecreatedForConfigurationStillReloadsWhenSelected() = runTest(dispatcher) {
        val vm = viewModel()
        vm.onResumed()
        val collector = observe(vm)
        runCurrent()
        vm.onPaused()
        collector.cancel()
        runCurrent()
        vm.onConfigurationRecreation(wasVisible = false)
        vm.onResumed()
        observe(vm)
        runCurrent()
        assertEquals(listOf(AgentsFilter.Connected to false, AgentsFilter.Connected to false), repository.calls)
    }

    @Test fun realPauseAndResumeReloadsUsingNormalCachePolicy() = runTest(dispatcher) {
        val vm = viewModel()
        vm.onResumed()
        val collector = observe(vm)
        runCurrent()
        vm.onPaused()
        collector.cancel()
        runCurrent()
        vm.onResumed()
        observe(vm)
        runCurrent()
        assertEquals(listOf(AgentsFilter.Connected to false, AgentsFilter.Connected to false), repository.calls)
    }

    @Test fun selectingDisconnectedWhileHiddenChangesEmptyMessageOnReturn() = runTest(dispatcher) {
        repository.load = { _, _ -> emptyList() }
        val vm = viewModel()
        val collector = observe(vm)
        runCurrent()
        collector.cancel()
        runCurrent()
        repository.filter.value = AgentsFilter.Disconnected
        observe(vm)
        runCurrent()
        assertEquals(AgentsUiState(AgentsFilter.Disconnected, ListUiState.Empty()), vm.state.value)
        assertEquals(R.string.agents_empty_disconnected, vm.state.value.emptyMessageRes)
    }

    private inner class FakeRepository : AgentsRepository {
        override val filter = MutableStateFlow(AgentsFilter.Connected)
        val calls = mutableListOf<Pair<AgentsFilter, Boolean>>()
        var load: suspend (AgentsFilter, Boolean) -> List<Agent> = { _, _ -> rows }
        override suspend fun agents(filter: AgentsFilter, forceRefresh: Boolean): List<Agent> {
            calls += filter to forceRefresh
            return load(filter, forceRefresh)
        }
    }
}
