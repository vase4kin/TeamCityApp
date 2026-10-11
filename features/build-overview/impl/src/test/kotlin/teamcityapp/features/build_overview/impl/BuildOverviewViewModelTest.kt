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

package teamcityapp.features.build_overview.impl

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelStore
import java.io.Serializable
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*
import teamcityapp.features.build_overview.api.*
import teamcityapp.libraries.builds.BuildLaunchData
import teamcityapp.libraries.builds.compatibility.BuildLaunchCodec
import teamcityapp.libraries.list_state.ListUiState

@OptIn(ExperimentalCoroutinesApi::class)
class BuildOverviewViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val store = ViewModelStore()
    private val repository = FakeRepository()
    private var incoming = BuildOverviewFixtures.finished
    private val codec = object : BuildLaunchCodec {
        override fun encode(build: BuildLaunchData): Serializable = "serialized-legacy-build"
        override fun decode(payload: Serializable): BuildLaunchData {
            assertEquals("serialized-legacy-build", payload)
            return incoming
        }
    }

    @Before fun before() {
        Dispatchers.setMain(dispatcher)
    }

    @After fun after() {
        store.clear()
        Dispatchers.resetMain()
    }
    private fun vm() = BuildOverviewViewModel(SavedStateHandle(mapOf(BuildOverviewNavigation.BUILD to codec.encode(incoming))), repository, codec).also { store.put("overview", it) }
    private fun TestScope.observe(vm: BuildOverviewViewModel) = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.state.collect {} }

    @Test fun finishedInitialLoadUsesCacheAndIsCollectionDriven() = runTest(dispatcher) {
        val vm = vm()
        runCurrent()
        assertTrue(repository.calls.isEmpty())
        observe(vm)
        runCurrent()
        assertEquals(listOf(incoming.href to false), repository.calls)
        assertEquals(BuildOverviewFixtures.finished, vm.state.value.build)
    }

    @Test fun runningInitialLoadForcesFreshDetails() = runTest(dispatcher) {
        incoming = BuildOverviewFixtures.build("running")
        val vm = vm()
        observe(vm)
        runCurrent()
        assertEquals(listOf(incoming.href to true), repository.calls)
    }

    @Test fun queuedInitialLoadPreservesTheLegacyCachePolicy() = runTest(dispatcher) {
        incoming = BuildOverviewFixtures.build("queued")
        val vm = vm()
        observe(vm)
        runCurrent()
        assertEquals(listOf(incoming.href to false), repository.calls)
    }

    @Test fun loadedDetailOverridesIncomingStatusAndCompleteSnapshotForActions() = runTest(dispatcher) {
        incoming = BuildOverviewFixtures.build("running")
        val result = BuildOverviewFixtures.finished.copy(branchName = "new-branch", number = "999")
        repository.load = { _, _ -> result }
        val vm = vm()
        observe(vm)
        runCurrent()
        assertEquals(result, vm.state.value.build)
        assertEquals(result.tests, vm.state.value.build?.tests)
        assertTrue(BuildOverviewAction.Restart in vm.state.value.actions)
        assertFalse(BuildOverviewAction.Stop in vm.state.value.actions)
    }

    @Test fun refreshStatusChangeUpdatesMenuAndDisablesActionsWhilePending() = runTest(dispatcher) {
        repository.load = { _, _ -> BuildOverviewFixtures.build("running") }
        val vm = vm()
        observe(vm)
        runCurrent()
        assertTrue(BuildOverviewAction.Stop in vm.state.value.actions)
        val result = CompletableDeferred<BuildLaunchData>()
        repository.load = { _, _ -> result.await() }
        vm.refresh()
        runCurrent()
        assertTrue(vm.state.value.actions.isEmpty())
        assertTrue((vm.state.value.list as ListUiState.Content).isRefreshing)
        result.complete(BuildOverviewFixtures.finished)
        runCurrent()
        assertTrue(BuildOverviewAction.Restart in vm.state.value.actions)
    }

    @Test fun laterRefreshUsesTheLatestServerBuildHref() = runTest(dispatcher) {
        val current = BuildOverviewFixtures.build("running").copy(href = "/build/started")
        repository.load = { _, _ -> current }
        val vm = vm()
        observe(vm)
        runCurrent()
        vm.refresh()
        runCurrent()
        assertEquals(listOf(incoming.href to false, current.href to true), repository.calls)
    }

    @Test fun refreshFailurePreservesLoadedDetailsAndActions() = runTest(dispatcher) {
        val vm = vm()
        observe(vm)
        runCurrent()
        repository.load = { _, _ -> error("offline") }
        vm.refresh()
        runCurrent()
        assertEquals(BuildOverviewFixtures.finished, vm.state.value.build)
        assertTrue((vm.state.value.list as ListUiState.Content).refreshFailed)
        assertTrue(BuildOverviewAction.Restart in vm.state.value.actions)
    }

    @Test fun initialFailureRetryBypassesCache() = runTest(dispatcher) {
        repository.load = { _, _ -> error("offline") }
        val vm = vm()
        observe(vm)
        runCurrent()
        assertEquals(ListUiState.Error, vm.state.value.list)
        assertNull(vm.state.value.build)
        assertTrue(vm.state.value.actions.isEmpty())
        repository.load = { _, _ -> BuildOverviewFixtures.finished }
        vm.retry()
        runCurrent()
        assertEquals(listOf(incoming.href to false, incoming.href to true), repository.calls)
    }

    @Test fun pendingLoadCancelsImmediatelyAndRunningResubscriptionStillForces() = runTest(dispatcher) {
        incoming = BuildOverviewFixtures.build("running")
        var cancelled = 0
        repository.load = { _, _ ->
            try {
                awaitCancellation()
            } finally {
                cancelled++
            }
        }
        val vm = vm()
        val first = observe(vm)
        runCurrent()
        first.cancel()
        runCurrent()
        assertEquals(1, cancelled)
        observe(vm)
        runCurrent()
        assertEquals(listOf(incoming.href to true, incoming.href to true), repository.calls)
    }

    @Test fun completedDetailAndActionsSurviveConfigurationGapWithoutDuplicateWork() = runTest(dispatcher) {
        val vm = vm()
        val first = observe(vm)
        runCurrent()
        val completed = vm.state.value
        first.cancel()
        runCurrent()
        observe(vm)
        runCurrent()
        assertEquals(completed, vm.state.value)
        assertEquals(1, repository.calls.size)
    }

    @Test fun hiddenTabRefreshIsRememberedAndLoadsOnReturn() = runTest(dispatcher) {
        val vm = vm()
        val first = observe(vm)
        runCurrent()
        first.cancel()
        runCurrent()
        vm.refresh()
        runCurrent()
        assertEquals(1, repository.calls.size)
        observe(vm)
        runCurrent()
        assertEquals(listOf(incoming.href to false, incoming.href to true), repository.calls)
    }

    @Test fun cancelledNoncooperativeSuccessCannotReplaceCurrentState() = runTest(dispatcher) {
        val result = CompletableDeferred<BuildLaunchData>()
        repository.load = { _, _ -> withContext(NonCancellable) { result.await() } }
        val vm = vm()
        val first = observe(vm)
        runCurrent()
        first.cancel()
        runCurrent()
        result.complete(BuildOverviewFixtures.finished)
        runCurrent()
        assertNull(vm.state.value.build)
        repository.load = { _, _ -> BuildOverviewFixtures.build("queued") }
        observe(vm)
        runCurrent()
        assertTrue(BuildOverviewAction.RemoveFromQueue in vm.state.value.actions)
    }

    @Test fun cancellationNeverBecomesAnErrorState() = runTest(dispatcher) {
        repository.load = { _, _ -> throw CancellationException("cancelled") }
        val vm = vm()
        observe(vm)
        runCurrent()
        assertEquals(ListUiState.Loading, vm.state.value.list)
        assertTrue(vm.state.value.actions.isEmpty())
    }
    private class FakeRepository : BuildOverviewRepository {
        val calls = mutableListOf<Pair<String, Boolean>>()
        var load: suspend (String, Boolean) -> BuildLaunchData = { _, _ -> BuildOverviewFixtures.finished }
        override suspend fun build(href: String, forceRefresh: Boolean): BuildLaunchData {
            calls += href to forceRefresh
            return load(href, forceRefresh)
        }
    }
}
