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

import androidx.lifecycle.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.test.*
import kotlinx.serialization.json.Json
import org.junit.*
import org.junit.Assert.*
import teamcityapp.features.build_details.api.*
import teamcityapp.libraries.builds.*

@OptIn(ExperimentalCoroutinesApi::class)
class BuildDetailsMetadataViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val store = ViewModelStore()
    private val build = BuildLaunchData("42", "/b", state = "running", changes = BuildCollectionLink("/changes"), tests = BuildTests("/tests", 10, 2, 3, 15))
    private val calls = mutableListOf<BuildDetailsTab>()
    private val acknowledged = mutableListOf<BuildDetailsPrompt>()
    private var promptReads = 0
    private var count: suspend (BuildDetailsTab) -> Int = { if (it == BuildDetailsTab.Changes) 12 else 15 }
    private var prompts: suspend () -> Set<BuildDetailsPrompt> = { BuildDetailsPrompt.entries.toSet() }
    private var acknowledge: suspend (BuildDetailsPrompt) -> Unit = {}
    private val repository = object : BuildDetailsMetadataRepository {
        override suspend fun count(build: BuildLaunchData, tab: BuildDetailsTab): Int {
            calls += tab
            return count(tab)
        }
        override suspend fun pendingPrompts(): Set<BuildDetailsPrompt> {
            promptReads++
            return prompts()
        }
        override suspend fun acknowledgePrompt(prompt: BuildDetailsPrompt) {
            acknowledged += prompt
            acknowledge(prompt)
        }
    }

    @Before fun before() {
        Dispatchers.setMain(dispatcher)
    }

    @After fun after() {
        store.clear()
        Dispatchers.resetMain()
    }
    private fun handle(build: BuildLaunchData = this.build) = SavedStateHandle(mapOf(BuildDetailsViewModel.BUILD to Json.encodeToString(build)))
    private fun model(saved: SavedStateHandle = handle(), name: String = "metadata") = BuildDetailsMetadataViewModel(saved, repository).also { store.put(name, it) }
    private fun TestScope.observe(model: BuildDetailsMetadataViewModel) = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { model.state.collect() }

    @Test fun collectionLoadsBothCountsBeforeAnyChildTabExistsAndRetainsCompletedResults() = runTest(dispatcher) {
        val vm = model()
        runCurrent()
        assertTrue(calls.isEmpty())
        assertEquals(0, promptReads)
        val collector = observe(vm)
        runCurrent()
        assertEquals(BuildDetailsTab.Changes to BuildDetailsCount.Available(12), vm.state.value.counts.entries.first().toPair())
        assertEquals(BuildDetailsCount.Available(15), vm.state.value.counts[BuildDetailsTab.Tests])
        assertEquals(2, calls.size)
        collector.cancel()
        runCurrent()
        observe(vm)
        runCurrent()
        assertEquals(2, calls.size)
        assertEquals(1, promptReads)
    }

    @Test fun optionalFailureNeverBecomesZeroAndRetryOnlyReadsTheFailedSection() = runTest(dispatcher) {
        count = { if (it == BuildDetailsTab.Tests) error("offline") else 12 }
        val vm = model()
        observe(vm)
        runCurrent()
        assertEquals(BuildDetailsCount.Unavailable, vm.state.value.counts[BuildDetailsTab.Tests])
        assertEquals(BuildDetailsCount.Available(12), vm.state.value.counts[BuildDetailsTab.Changes])
        count = { 0 }
        vm.retryFailed()
        runCurrent()
        assertEquals(BuildDetailsCount.Available(0), vm.state.value.counts[BuildDetailsTab.Tests])
        assertEquals(1, calls.count { it == BuildDetailsTab.Changes })
        assertEquals(2, calls.count { it == BuildDetailsTab.Tests })
    }

    @Test fun collectorLossCancelsPendingReadsWithoutDiscardingAnIndependentCompletedSection() = runTest(dispatcher) {
        var cancelled = false
        count = {
            if (it == BuildDetailsTab.Changes) {
                12
            } else {
                try {
                    awaitCancellation()
                } finally {
                    cancelled = true
                }
            }
        }
        val vm = model()
        val collector = observe(vm)
        runCurrent()
        assertEquals(BuildDetailsCount.Available(12), vm.state.value.counts[BuildDetailsTab.Changes])
        assertEquals(BuildDetailsCount.Loading, vm.state.value.counts[BuildDetailsTab.Tests])
        collector.cancel()
        runCurrent()
        assertTrue(cancelled)
        assertEquals(BuildDetailsCount.Loading, vm.state.value.counts[BuildDetailsTab.Tests])
        count = { 15 }
        observe(vm)
        runCurrent()
        assertEquals(1, calls.count { it == BuildDetailsTab.Changes })
        assertEquals(2, calls.count { it == BuildDetailsTab.Tests })
    }

    @Test fun savedResultsAndDismissedEducationRestoreWithoutDuplicateCountReadsOrWrites() = runTest(dispatcher) {
        val saved = handle()
        val vm = model(saved)
        observe(vm)
        runCurrent()
        vm.dismissPrompt()
        vm.dismissPrompt()
        runCurrent()
        val copy = SavedStateHandle(saved.keys().associateWith { saved.get<Any?>(it) })
        val restored = model(copy, "restored")
        observe(restored)
        runCurrent()
        assertEquals(vm.state.value.counts, restored.state.value.counts)
        assertNull(restored.state.value.prompt)
        assertEquals(2, calls.size)
        assertEquals(listOf(BuildDetailsPrompt.Stop), acknowledged)
    }

    @Test fun promptsFollowLoadedActionStateAndIgnoreAnotherBuildSnapshot() = runTest(dispatcher) {
        val vm = model()
        observe(vm)
        runCurrent()
        assertEquals(BuildDetailsPrompt.Stop, vm.state.value.prompt?.prompt)
        vm.loaded(build.copy(state = "queued"))
        runCurrent()
        assertEquals(BuildDetailsPrompt.Remove, vm.state.value.prompt?.prompt)
        vm.dismissPrompt()
        runCurrent()
        assertNull(vm.state.value.prompt)
        vm.loaded(build.copy(state = "finished"))
        runCurrent()
        assertEquals(BuildDetailsPrompt.Restart, vm.state.value.prompt?.prompt)
        vm.loaded(build.copy(id = "other", state = "running"))
        runCurrent()
        assertEquals(BuildDetailsPrompt.Restart, vm.state.value.prompt?.prompt)
        assertEquals(R.string.text_onboarding_restart_build, vm.state.value.prompt?.description)
        vm.loaded(build.copy(state = "unknown server state"))
        runCurrent()
        assertNull(vm.state.value.prompt)
    }

    @Test fun absentCountsDoNotCreateRequestsAndCannotBeRetried() = runTest(dispatcher) {
        val vm = model(handle(build.copy(changes = null, tests = null)))
        observe(vm)
        runCurrent()
        vm.retryCount(BuildDetailsTab.Changes)
        vm.retryCount(BuildDetailsTab.Overview)
        runCurrent()
        assertTrue(calls.isEmpty())
        assertTrue(vm.state.value.counts.isEmpty())
    }

    @Test fun countsPublishWhileEducationIsStillPendingAndOwnerRemovalCancelsIt() = runTest(dispatcher) {
        var cancelled = false
        prompts = {
            try {
                awaitCancellation()
            } finally {
                cancelled = true
            }
        }
        val vm = model()
        observe(vm)
        runCurrent()
        assertEquals(BuildDetailsCount.Available(12), vm.state.value.counts[BuildDetailsTab.Changes])
        store.clear()
        runCurrent()
        assertTrue(cancelled)
    }

    @Test fun preferenceReadFailureIsRetryableWithoutRefetchingSuccessfulCounts() = runTest(dispatcher) {
        prompts = { error("storage unavailable") }
        val vm = model()
        observe(vm)
        runCurrent()
        assertTrue(vm.state.value.onboardingFailed)
        assertNull(vm.state.value.prompt)
        prompts = { setOf(BuildDetailsPrompt.Stop) }
        vm.retryFailed()
        runCurrent()
        assertFalse(vm.state.value.onboardingFailed)
        assertEquals(BuildDetailsPrompt.Stop, vm.state.value.prompt?.prompt)
        assertEquals(2, calls.size)
    }

    @Test fun failedAcknowledgementRequiresExplicitRetryAndDoesNotRepeatDuringRecreation() = runTest(dispatcher) {
        acknowledge = { error("write failed") }
        val vm = model()
        val collector = observe(vm)
        runCurrent()
        vm.dismissPrompt()
        runCurrent()
        assertTrue(vm.state.value.onboardingFailed)
        collector.cancel()
        runCurrent()
        observe(vm)
        runCurrent()
        assertNull(vm.state.value.prompt)
        assertEquals(1, acknowledged.size)
        acknowledge = {}
        vm.retryFailed()
        runCurrent()
        vm.dismissPrompt()
        runCurrent()
        assertFalse(vm.state.value.onboardingFailed)
        assertEquals(2, acknowledged.size)
    }

    @Test fun equalBuildVisitsHaveIndependentMetadataOwnersAndInvalidCountsAreFailures() = runTest(dispatcher) {
        val first = model(name = "first")
        val second = model(name = "second")
        observe(first)
        runCurrent()
        first.dismissPrompt()
        runCurrent()
        count = { -1 }
        observe(second)
        runCurrent()
        assertEquals(BuildDetailsPrompt.Stop, second.state.value.prompt?.prompt)
        assertNull(first.state.value.prompt)
        assertTrue(second.state.value.counts.values.all { it == BuildDetailsCount.Unavailable })
        assertTrue(first.state.value.counts.values.all { it is BuildDetailsCount.Available })
    }
}
