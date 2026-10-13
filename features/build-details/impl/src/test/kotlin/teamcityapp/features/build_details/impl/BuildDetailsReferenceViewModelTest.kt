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

package teamcityapp.features.build_details.impl

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelStore
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import teamcityapp.features.build_details.api.*
import teamcityapp.features.build_details.impl.tracker.BuildDetailsTracker
import teamcityapp.libraries.builds.BuildLaunchData

@OptIn(ExperimentalCoroutinesApi::class)
class BuildDetailsReferenceViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val store = ViewModelStore()
    private val tracker = Mockito.mock(BuildDetailsTracker::class.java)
    private val requests = mutableListOf<String>()
    private var load: suspend () -> BuildLaunchData = { build }
    private val build = BuildLaunchData("42", "opaque/build/42", state = "queued")
    private val repository = object : BuildDetailsRepository {
        override suspend fun queuedBuild(href: String): BuildLaunchData {
            requests += href
            return load()
        }
        override suspend fun triggeredByCurrentUser(build: BuildLaunchData) = false
        override suspend fun cancel(build: BuildLaunchData, reAddToQueue: Boolean): BuildDetailsMutation<BuildLaunchData> = error("unused")
        override suspend fun restart(build: BuildLaunchData): BuildDetailsMutation<String> = error("unused")
    }

    @Before fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @After fun cleanup() {
        store.clear()
        Dispatchers.resetMain()
    }
    private fun model(href: String = "opaque/build/42") = BuildDetailsReferenceViewModel(SavedStateHandle(mapOf(BuildDetailsReferenceViewModel.HREF to href)), repository, tracker).also { store.put("reference", it) }
    private fun TestScope.observe(vm: BuildDetailsReferenceViewModel) = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.state.collect {} }

    @Test fun loadStartsOnCollectionAndRetainsCompleteSnapshotAcrossRecreationGap() = runTest(dispatcher) {
        val vm = model()
        runCurrent()
        assertTrue(requests.isEmpty())
        val subscription = observe(vm)
        runCurrent()
        assertEquals(BuildDetailsReferenceState.Content(build), vm.state.value)
        subscription.cancel()
        runCurrent()
        observe(vm)
        runCurrent()
        assertEquals(listOf("opaque/build/42"), requests)
        assertEquals(BuildDetailsReferenceState.Content(build), vm.state.value)
    }

    @Test fun failureCanRetrySameOpaqueReferenceAndResolveARunningBuild() = runTest(dispatcher) {
        load = { error("offline") }
        val vm = model()
        observe(vm)
        runCurrent()
        assertEquals(BuildDetailsReferenceState.Error, vm.state.value)
        load = { build.copy(state = "running") }
        vm.retry()
        runCurrent()
        assertEquals(BuildDetailsReferenceState.Content(build.copy(state = "running")), vm.state.value)
        assertEquals(listOf("opaque/build/42", "opaque/build/42"), requests)
    }

    @Test fun leavingCancelsPendingLoadWithoutTurningCancellationIntoError() = runTest(dispatcher) {
        var cancelled = false
        load = {
            try {
                awaitCancellation()
            } finally {
                cancelled = true
            }
        }
        val vm = model()
        val subscription = observe(vm)
        runCurrent()
        subscription.cancel()
        advanceTimeBy(5_000)
        runCurrent()
        assertTrue(cancelled)
        assertEquals(BuildDetailsReferenceState.Loading, vm.state.value)
        load = { build }
        observe(vm)
        runCurrent()
        assertEquals(BuildDetailsReferenceState.Content(build), vm.state.value)
    }

    @Test fun pendingLoadSurvivesAConfigurationSubscriptionGapWithoutAnotherRequest() = runTest(dispatcher) {
        val response = CompletableDeferred<BuildLaunchData>()
        load = { response.await() }
        val vm = model()
        val subscription = observe(vm)
        runCurrent()
        subscription.cancel()
        advanceTimeBy(100)
        runCurrent()
        observe(vm)
        runCurrent()
        assertEquals(listOf("opaque/build/42"), requests)
        response.complete(build)
        runCurrent()
        assertEquals(BuildDetailsReferenceState.Content(build), vm.state.value)
    }

    @Test fun missingReferenceProducesRecoverableErrorWithoutNetworkCall() = runTest(dispatcher) {
        val vm = model("")
        observe(vm)
        runCurrent()
        assertEquals(BuildDetailsReferenceState.Error, vm.state.value)
        assertTrue(requests.isEmpty())
    }
}
