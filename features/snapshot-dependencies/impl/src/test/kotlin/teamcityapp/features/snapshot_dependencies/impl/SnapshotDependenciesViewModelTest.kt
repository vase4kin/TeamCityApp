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

package teamcityapp.features.snapshot_dependencies.impl

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelStore
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import teamcityapp.features.snapshot_dependencies.api.SnapshotDependenciesRepository
import teamcityapp.libraries.builds.BuildLaunchData
import teamcityapp.libraries.list_state.ListUiState

@OptIn(ExperimentalCoroutinesApi::class)
class SnapshotDependenciesViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val store = ViewModelStore()
    private val rows = listOf(snapshotBuild(), snapshotBuild("2"))
    private val repository = FakeRepository()

    @Before fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After fun tearDown() {
        store.clear()
        Dispatchers.resetMain()
    }
    private fun vm() = SnapshotDependenciesViewModel(repository, SavedStateHandle(mapOf("id" to "parent"))).also { store.put("snapshot", it) }
    private fun TestScope.observe(vm: SnapshotDependenciesViewModel) = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.state.collect {} }

    @Test fun firstLoadIsCollectionDrivenAndFirstResumeDoesNotDuplicateIt() = runTest(dispatcher) {
        val vm = vm()
        vm.onResumed()
        runCurrent()
        assertTrue(repository.calls.isEmpty())
        observe(vm)
        runCurrent()
        assertEquals(listOf("parent" to false), repository.calls)
        assertEquals(ListUiState.Content(rows), vm.state.value.list)
    }

    @Test fun returnedOrderAndFullLaunchSnapshotsRemainUnchanged() = runTest(dispatcher) {
        repository.load = { rows.reversed() }
        val vm = vm()
        observe(vm)
        runCurrent()
        assertEquals(ListUiState.Content(rows.reversed()), vm.state.value.list)
        assertNotNull((vm.state.value.list as ListUiState.Content).items.first().tests)
    }

    @Test fun refreshUsesFreshListWhileRetainingRows() = runTest(dispatcher) {
        val vm = vm()
        observe(vm)
        runCurrent()
        val deferred = CompletableDeferred<List<BuildLaunchData>>()
        repository.load = { deferred.await() }
        vm.refresh()
        runCurrent()
        assertEquals(ListUiState.Content(rows, isRefreshing = true), vm.state.value.list)
        assertEquals("parent" to true, repository.calls.last())
        deferred.complete(emptyList())
        runCurrent()
        assertEquals(ListUiState.Empty(), vm.state.value.list)
    }

    @Test fun failedRefreshRetainsRowsAndRetryForcesData() = runTest(dispatcher) {
        val vm = vm()
        observe(vm)
        runCurrent()
        repository.load = { error("offline") }
        vm.refresh()
        runCurrent()
        assertEquals(ListUiState.Content(rows, refreshFailed = true), vm.state.value.list)
        repository.load = { rows }
        vm.retry()
        runCurrent()
        assertEquals(ListUiState.Content(rows), vm.state.value.list)
        assertEquals("parent" to true, repository.calls.last())
    }

    @Test fun failedInitialRequestBecomesErrorAndRetryForcesData() = runTest(dispatcher) {
        repository.load = { error("offline") }
        val vm = vm()
        observe(vm)
        runCurrent()
        assertEquals(ListUiState.Error, vm.state.value.list)
        repository.load = { rows }
        vm.retry()
        runCurrent()
        assertEquals(ListUiState.Content(rows), vm.state.value.list)
        assertEquals("parent" to true, repository.calls.last())
    }

    @Test fun lostCollectorCancelsPendingRequestWithoutError() = runTest(dispatcher) {
        var cancelled = false
        repository.load = {
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
        repository.load = { rows }
        observe(vm)
        runCurrent()
        assertEquals(ListUiState.Content(rows), vm.state.value.list)
    }

    @Test fun visibleConfigurationRecreationKeepsCompletedResults() = runTest(dispatcher) {
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

    @Test fun genuineReturnReloadsUsingCachePolicy() = runTest(dispatcher) {
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
        assertEquals(listOf("parent" to false, "parent" to false), repository.calls)
    }

    @Test fun hiddenConfigurationRecreationPreservesReloadOnReturn() = runTest(dispatcher) {
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

    @Test(expected = IllegalStateException::class)
    fun missingParentIdFailsBeforeLoading() {
        SnapshotDependenciesViewModel(repository, SavedStateHandle())
    }

    @Test(expected = IllegalArgumentException::class)
    fun blankParentIdFailsBeforeLoading() {
        SnapshotDependenciesViewModel(repository, SavedStateHandle(mapOf("id" to " ")))
    }

    private inner class FakeRepository : SnapshotDependenciesRepository {
        val calls = mutableListOf<Pair<String, Boolean>>()
        var load: suspend (Boolean) -> List<BuildLaunchData> = { rows }
        override suspend fun dependencies(buildId: String, forceRefresh: Boolean): List<BuildLaunchData> {
            calls += buildId to forceRefresh
            return load(forceRefresh)
        }
    }
}
