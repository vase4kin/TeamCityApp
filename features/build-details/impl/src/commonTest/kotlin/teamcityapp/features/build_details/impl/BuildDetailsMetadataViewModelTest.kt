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
import kotlin.test.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.test.*
import kotlinx.serialization.json.Json
import teamcityapp.features.build_details.api.*
import teamcityapp.features.build_details.impl.resources.*
import teamcityapp.libraries.builds.*

@OptIn(ExperimentalCoroutinesApi::class)
class BuildDetailsMetadataViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val store = ViewModelStore()
    private val build = BuildLaunchData("42", "/b", state = "running", changes = BuildCollectionLink("/changes"), tests = BuildTests("/tests", 10, 2, 3, 15))
    private val calls = mutableListOf<BuildDetailsSection>()
    private val acknowledged = mutableListOf<BuildDetailsPrompt>()
    private var promptReads = 0
    private var count: suspend (BuildDetailsSection) -> Int = { if (it == BuildDetailsSection.Changes) 12 else 15 }
    private var prompts: suspend () -> Set<BuildDetailsPrompt> = { BuildDetailsPrompt.entries.toSet() }
    private var acknowledge: suspend (BuildDetailsPrompt) -> Unit = {}
    private val repository = object : BuildDetailsMetadataRepository {
        override suspend fun count(build: BuildLaunchData, tab: BuildDetailsSection): Int {
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

    @BeforeTest fun before() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest fun after() {
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
        assertEquals(BuildDetailsSection.Changes to BuildDetailsCount.Available(12), vm.state.value.counts.entries.first().toPair())
        assertEquals(BuildDetailsCount.Available(15), vm.state.value.counts[BuildDetailsSection.Tests])
        assertEquals(2, calls.size)
        collector.cancel()
        runCurrent()
        observe(vm)
        runCurrent()
        assertEquals(2, calls.size)
        assertEquals(1, promptReads)
    }

    @Test fun returnedSectionCountReplacesAndPersistsMetadataWithoutAnotherRequest() = runTest(dispatcher) {
        val saved = handle()
        val vm = model(saved)
        observe(vm)
        runCurrent()
        vm.updateCount(BuildDetailsSection.Tests, 21)
        runCurrent()
        assertEquals(BuildDetailsCount.Available(21), vm.state.value.counts[BuildDetailsSection.Tests])
        assertEquals(2, calls.size)
        val recreated = model(saved, "restored")
        observe(recreated)
        runCurrent()
        assertEquals(BuildDetailsCount.Available(21), recreated.state.value.counts[BuildDetailsSection.Tests])
        assertEquals(2, calls.size)
    }

    @Test fun optionalFailureNeverBecomesZeroAndRetryOnlyReadsTheFailedSection() = runTest(dispatcher) {
        count = { if (it == BuildDetailsSection.Tests) error("offline") else 12 }
        val vm = model()
        observe(vm)
        runCurrent()
        assertEquals(BuildDetailsCount.Unavailable, vm.state.value.counts[BuildDetailsSection.Tests])
        assertEquals(BuildDetailsCount.Available(12), vm.state.value.counts[BuildDetailsSection.Changes])
        count = { 0 }
        vm.retryFailed()
        runCurrent()
        assertEquals(BuildDetailsCount.Available(0), vm.state.value.counts[BuildDetailsSection.Tests])
        assertEquals(1, calls.count { it == BuildDetailsSection.Changes })
        assertEquals(2, calls.count { it == BuildDetailsSection.Tests })
    }

    @Test fun collectorLossCancelsPendingReadsWithoutDiscardingAnIndependentCompletedSection() = runTest(dispatcher) {
        var cancelled = false
        count = {
            if (it == BuildDetailsSection.Changes) {
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
        assertEquals(BuildDetailsCount.Available(12), vm.state.value.counts[BuildDetailsSection.Changes])
        assertEquals(BuildDetailsCount.Loading, vm.state.value.counts[BuildDetailsSection.Tests])
        collector.cancel()
        runCurrent()
        assertTrue(cancelled)
        assertEquals(BuildDetailsCount.Loading, vm.state.value.counts[BuildDetailsSection.Tests])
        count = { 15 }
        observe(vm)
        runCurrent()
        assertEquals(1, calls.count { it == BuildDetailsSection.Changes })
        assertEquals(2, calls.count { it == BuildDetailsSection.Tests })
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
        assertEquals(Res.string.text_onboarding_restart_build, vm.state.value.prompt?.description)
        vm.loaded(build.copy(state = "unknown server state"))
        runCurrent()
        assertNull(vm.state.value.prompt)
    }

    @Test fun absentCountsDoNotCreateRequestsAndCannotBeRetried() = runTest(dispatcher) {
        val vm = model(handle(build.copy(changes = null, tests = null)))
        observe(vm)
        runCurrent()
        vm.retryCount(BuildDetailsSection.Changes)
        vm.retryCount(BuildDetailsSection.Parameters)
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
        assertEquals(BuildDetailsCount.Available(12), vm.state.value.counts[BuildDetailsSection.Changes])
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

    @Test fun failedAcknowledgementKeepsTheSameTipForExplicitRetryWithoutRepeatingWrites() = runTest(dispatcher) {
        acknowledge = { error("write failed") }
        val vm = model()
        val collector = observe(vm)
        runCurrent()
        vm.dismissPrompt()
        runCurrent()
        assertTrue(vm.state.value.prompt!!.saveFailed)
        collector.cancel()
        runCurrent()
        observe(vm)
        runCurrent()
        assertEquals(BuildDetailsPrompt.Stop, vm.state.value.prompt?.prompt)
        assertEquals(1, acknowledged.size)
        acknowledge = {}
        vm.dismissPrompt()
        runCurrent()
        assertNull(vm.state.value.prompt)
        assertEquals(2, acknowledged.size)
    }

    @Test fun actionTipsAdvanceIndividuallyAndRestoreAtTheNextAvailableButton() = runTest(dispatcher) {
        val saved = handle(build.copy(webUrl = "https://server/build/42"))
        val vm = model(saved)
        observe(vm)
        runCurrent()
        vm.dismissPrompt()
        vm.dismissPrompt()
        runCurrent()
        assertEquals(listOf(BuildDetailsPrompt.Stop), acknowledged)
        assertEquals(BuildDetailsPrompt.Share, vm.state.value.prompt?.prompt)
        val restored = model(SavedStateHandle(saved.keys().associateWith { saved.get<Any?>(it) }), "restored")
        observe(restored)
        runCurrent()
        assertEquals(BuildDetailsPrompt.Share, restored.state.value.prompt?.prompt)
        restored.dismissPrompt()
        runCurrent()
        assertEquals(BuildDetailsPrompt.Browser, restored.state.value.prompt?.prompt)
        restored.dismissPrompt()
        runCurrent()
        assertNull(restored.state.value.prompt)
        assertEquals(listOf(BuildDetailsPrompt.Stop, BuildDetailsPrompt.Share, BuildDetailsPrompt.Browser), acknowledged)
    }

    @Test fun missingWebUrlDoesNotOfferShareOrBrowserTipsAndDelayedSaveDoesNotAdvanceEarly() = runTest(dispatcher) {
        acknowledge = { delay(1_000) }
        val vm = model()
        observe(vm)
        runCurrent()
        vm.dismissPrompt()
        runCurrent()
        assertTrue(vm.state.value.prompt!!.saving)
        vm.dismissPrompt()
        advanceTimeBy(1_000)
        runCurrent()
        assertNull(vm.state.value.prompt)
        assertEquals(listOf(BuildDetailsPrompt.Stop), acknowledged)
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
