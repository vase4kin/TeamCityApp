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

package teamcityapp.features.running_builds.impl

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
import teamcityapp.features.running_builds.api.*
import teamcityapp.libraries.builds.BuildLaunchData
import teamcityapp.libraries.list_state.ListUiState

@OptIn(ExperimentalCoroutinesApi::class)
class RunningBuildsViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val store = ViewModelStore()
    private val rows = listOf(buildRow(), buildRow("2"))
    private val repository = FakeRepository()

    @Before fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After fun tearDown() {
        store.clear()
        Dispatchers.resetMain()
    }
    private fun vm() = RunningBuildsViewModel(repository).also { store.put("builds", it) }
    private fun TestScope.observe(vm: RunningBuildsViewModel) = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.state.collect {} }

    @Test fun firstLoadIsCollectionDrivenAndInitialResumeDoesNotDuplicateIt() = runTest(dispatcher) {
        val vm = vm()
        vm.onResumed()
        runCurrent()
        assertTrue(repository.calls.isEmpty())
        observe(vm)
        runCurrent()
        assertEquals(listOf(repository.query.value to false), repository.calls)
        assertEquals(ListUiState.Content(rows), vm.state.value.list)
    }

    @Test fun allFilterIgnoresFavoriteChangesAndUsesEmptyIds() = runTest(dispatcher) {
        repository.query.value = RunningBuildsQuery("account", RunningBuildsFilter.All, listOf("irrelevant"))
        val vm = vm()
        observe(vm)
        runCurrent()
        assertTrue(repository.calls.single().first.favoriteConfigurationIds.isEmpty())
        repository.query.value = repository.query.value.copy(favoriteConfigurationIds = listOf("changed"))
        runCurrent()
        assertEquals(1, repository.calls.size)
    }

    @Test fun emptyFavoritesMakesNoRepositoryRequest() = runTest(dispatcher) {
        repository.query.value = RunningBuildsQuery("account")
        val vm = vm()
        observe(vm)
        runCurrent()
        assertEquals(ListUiState.Empty(), vm.state.value.list)
        assertEquals(R.string.running_builds_empty_favorites, vm.state.value.emptyMessageRes)
        vm.refresh()
        runCurrent()
        assertTrue(repository.calls.isEmpty())
    }

    @Test fun favoriteIdChangesReloadTheirOwnQuery() = runTest(dispatcher) {
        val vm = vm()
        observe(vm)
        runCurrent()
        repository.query.value = repository.query.value.copy(favoriteConfigurationIds = listOf("other"))
        runCurrent()
        assertEquals(listOf("other"), repository.calls.last().first.favoriteConfigurationIds)
        assertEquals(2, repository.calls.size)
    }

    @Test fun accountChangeNeverEmitsOldRowsUnderNewAccount() = runTest(dispatcher) {
        val vm = vm()
        val emissions = mutableListOf<RunningBuildsUiState>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.state.collect { emissions += it } }
        runCurrent()
        val deferred = CompletableDeferred<List<BuildLaunchData>>()
        repository.load = { _, _ -> deferred.await() }
        repository.query.value = repository.query.value.copy(accountKey = "other-account")
        runCurrent()
        assertEquals(ListUiState.Loading, vm.state.value.list)
        val changed = listOf(buildRow("new"))
        deferred.complete(changed)
        runCurrent()
        assertFalse(emissions.any { it.query.accountKey == "other-account" && (it.list as? ListUiState.Content)?.items == rows })
        assertEquals(ListUiState.Content(changed), vm.state.value.list)
    }

    @Test fun filterChangeCancelsOldRequestAndEmitsMatchingEmptyState() = runTest(dispatcher) {
        var cancelled = false
        repository.load = { query, _ ->
            if (query.filter == RunningBuildsFilter.Favorites) {
                try {
                    awaitCancellation()
                } finally {
                    cancelled = true
                }
            } else {
                emptyList()
            }
        }
        val vm = vm()
        observe(vm)
        runCurrent()
        repository.query.value = repository.query.value.copy(filter = RunningBuildsFilter.All)
        runCurrent()
        assertTrue(cancelled)
        assertEquals(RunningBuildsFilter.All, vm.state.value.query.filter)
        assertEquals(ListUiState.Empty(), vm.state.value.list)
        assertEquals(R.string.running_builds_empty_all, vm.state.value.emptyMessageRes)
    }

    @Test fun sortsConfigurationIdsIgnoringCaseAndKeepsEqualIdQueueOrder() = runTest(dispatcher) {
        val ordered = listOf(buildRow("z").copy(buildTypeId = "z"), buildRow("first").copy(buildTypeId = "a"), buildRow("second").copy(buildTypeId = "A"), buildRow("third").copy(buildTypeId = "b"))
        repository.load = { _, _ -> ordered }
        val vm = vm()
        observe(vm)
        runCurrent()
        assertEquals(listOf("first", "second", "third", "z"), (vm.state.value.list as ListUiState.Content).items.map { it.id })
    }

    @Test fun refreshForcesListAndRetainsCompletedRows() = runTest(dispatcher) {
        val vm = vm()
        observe(vm)
        runCurrent()
        val deferred = CompletableDeferred<List<BuildLaunchData>>()
        repository.load = { _, _ -> deferred.await() }
        vm.refresh()
        runCurrent()
        assertEquals(ListUiState.Content(rows, isRefreshing = true), vm.state.value.list)
        assertTrue(repository.calls.last().second)
        deferred.complete(emptyList())
        runCurrent()
        assertEquals(ListUiState.Empty(), vm.state.value.list)
    }

    @Test fun failedRefreshRetainsRowsAndRetryForcesData() = runTest(dispatcher) {
        val vm = vm()
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
        assertTrue(repository.calls.last().second)
    }

    @Test fun lastCollectorCancelsPendingRequestWithoutError() = runTest(dispatcher) {
        var cancelled = false
        repository.load = { _, _ ->
            try {
                awaitCancellation()
            } finally {
                cancelled = true
            }
        }
        val vm = vm()
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
    }

    @Test fun configurationRecreationKeepsCompletedRowsWithoutReload() = runTest(dispatcher) {
        val vm = vm()
        vm.onResumed()
        val collector = observe(vm)
        runCurrent()
        vm.onPaused()
        collector.cancel()
        runCurrent()
        vm.onConfigurationRecreation(true)
        vm.onResumed()
        observe(vm)
        runCurrent()
        assertEquals(1, repository.calls.size)
    }

    @Test fun realReturnUsesNormalListCachePolicy() = runTest(dispatcher) {
        val vm = vm()
        vm.onResumed()
        val collector = observe(vm)
        runCurrent()
        vm.onPaused()
        collector.cancel()
        runCurrent()
        vm.onResumed()
        observe(vm)
        runCurrent()
        assertEquals(listOf(false, false), repository.calls.map { it.second })
    }

    @Test fun hiddenConfigurationRecreationStillReloadsOnReturn() = runTest(dispatcher) {
        val vm = vm()
        vm.onResumed()
        val collector = observe(vm)
        runCurrent()
        vm.onPaused()
        collector.cancel()
        runCurrent()
        vm.onConfigurationRecreation(false)
        vm.onResumed()
        observe(vm)
        runCurrent()
        assertEquals(2, repository.calls.size)
    }

    private inner class FakeRepository : RunningBuildsRepository {
        override val query = MutableStateFlow(RunningBuildsQuery("account", favoriteConfigurationIds = listOf("Android_Debug")))
        val calls = mutableListOf<Pair<RunningBuildsQuery, Boolean>>()
        var load: suspend (RunningBuildsQuery, Boolean) -> List<BuildLaunchData> = { _, _ -> rows }
        override suspend fun builds(query: RunningBuildsQuery, forceRefresh: Boolean): List<BuildLaunchData> {
            calls += query to forceRefresh
            return load(query, forceRefresh)
        }
    }
}
