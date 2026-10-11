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

package teamcityapp.features.navigation.impl

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelStore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import teamcityapp.features.navigation.api.*
import teamcityapp.features.navigation.impl.tracker.NavigationTracker
import teamcityapp.libraries.build_configurations.ProjectReference
import teamcityapp.libraries.list_state.ListUiState

@OptIn(ExperimentalCoroutinesApi::class)
class NavigationViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val store = ViewModelStore()
    private val rows = listOf(NavigationEntry.Project(ProjectReference("child", "Child")))
    private val calls = mutableListOf<Pair<String, Boolean>>()
    private var load: suspend () -> List<NavigationEntry> = { rows }
    private var eligibility: suspend () -> Boolean = { true }
    private var save: suspend () -> Unit = {}
    private var saves = 0
    private var eligibilityCalls = 0
    private val events = mutableListOf<String>()
    private val repository = object : NavigationRepository {
        override suspend fun entries(projectId: String, forceRefresh: Boolean): List<NavigationEntry> {
            calls += projectId to forceRefresh
            return load()
        }
    }
    private val rating = object : NavigationRatingRepository {
        override suspend fun isEligible(): Boolean {
            eligibilityCalls++
            return eligibility()
        }
        override suspend fun markHandled() {
            saves++
            save()
        }
    }
    private val tracker = object : NavigationTracker {
        override fun viewShown() {
            events += "view"
        }
        override fun ratingShown() {
            events += "show"
        }
        override fun ratingCancelled() {
            events += "cancel"
        }
        override fun ratingSelected() {
            events += "rate"
        }
    }

    @Before fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After fun tearDown() {
        store.clear()
        Dispatchers.resetMain()
    }
    private fun viewModel(args: Map<String, Any?> = emptyMap()) = NavigationViewModel(SavedStateHandle(args), repository, rating, tracker).also { store.put("navigation", it) }
    private fun TestScope.observe(vm: NavigationViewModel) = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.state.collect {} }

    @Test fun collectionLoadsRootOnceAndResumeOnlyTracksView() = runTest(dispatcher) {
        val vm = viewModel()
        vm.onResumed()
        runCurrent()
        assertTrue(calls.isEmpty())
        observe(vm)
        runCurrent()
        assertEquals(listOf("_Root" to false), calls)
        assertEquals(ListUiState.Content(rows), vm.state.value.list)
        assertEquals(RatingPromptState.Available(), vm.state.value.rating)
        assertEquals(listOf("view", "show"), events)
        vm.onResumed()
        runCurrent()
        assertEquals(1, calls.size)
    }

    @Test fun legacyArgumentsSelectProjectAndTitle() = runTest(dispatcher) {
        val vm = viewModel(mapOf("id" to "nested", "name" to "Nested project"))
        observe(vm)
        runCurrent()
        assertEquals(listOf("nested" to false), calls)
        assertEquals(ProjectReference("nested", "Nested project"), vm.state.value.project)
    }

    @Test fun emptyContentNeverEvaluatesOrShowsRating() = runTest(dispatcher) {
        load = { emptyList() }
        eligibility = { error("Should not be called") }
        val vm = viewModel()
        observe(vm)
        runCurrent()
        assertEquals(ListUiState.Empty(), vm.state.value.list)
        assertEquals(RatingPromptState.Hidden, vm.state.value.rating)
        assertTrue(events.isEmpty())
    }

    @Test fun ineligibleContentDoesNotShowRating() = runTest(dispatcher) {
        eligibility = { false }
        val vm = viewModel()
        observe(vm)
        runCurrent()
        assertEquals(RatingPromptState.Hidden, vm.state.value.rating)
        assertTrue(events.isEmpty())
    }

    @Test fun optionalRatingFailureKeepsContentAndHasExplicitUnavailableState() = runTest(dispatcher) {
        eligibility = { error("preferences unavailable") }
        val vm = viewModel()
        observe(vm)
        runCurrent()
        assertEquals(ListUiState.Content(rows), vm.state.value.list)
        assertEquals(RatingPromptState.Unavailable, vm.state.value.rating)
    }

    @Test fun failedOptionalRatingCanRecoverOnRefresh() = runTest(dispatcher) {
        eligibility = { error("preferences unavailable") }
        val vm = viewModel()
        observe(vm)
        runCurrent()
        eligibility = { true }
        vm.refresh()
        runCurrent()
        assertEquals(ListUiState.Content(rows), vm.state.value.list)
        assertEquals(RatingPromptState.Available(), vm.state.value.rating)
        assertEquals(listOf("show"), events)
    }

    @Test fun cancelledEligibilityDoesNotBecomeOptionalFailureAndResumesOnReturn() = runTest(dispatcher) {
        var cancelled = false
        eligibility = {
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
        assertEquals(ListUiState.Content(rows), vm.state.value.list)
        assertEquals(RatingPromptState.Hidden, vm.state.value.rating)
        eligibility = { true }
        observe(vm)
        runCurrent()
        assertEquals(ListUiState.Content(rows), vm.state.value.list)
        assertEquals(RatingPromptState.Available(), vm.state.value.rating)
        assertEquals(listOf("show"), events)
        assertEquals(1, calls.size)
    }

    @Test fun initialFailureCanRetryWithForceRefresh() = runTest(dispatcher) {
        load = { error("offline") }
        val vm = viewModel()
        observe(vm)
        runCurrent()
        assertEquals(ListUiState.Error, vm.state.value.list)
        load = { rows }
        vm.retry()
        runCurrent()
        assertEquals(ListUiState.Content(rows), vm.state.value.list)
        assertEquals("_Root" to true, calls.last())
    }

    @Test fun refreshFailureKeepsRowsAndRatingChoice() = runTest(dispatcher) {
        val vm = viewModel()
        observe(vm)
        runCurrent()
        load = { error("offline") }
        vm.refresh()
        runCurrent()
        assertEquals(ListUiState.Content(rows, refreshFailed = true), vm.state.value.list)
        assertEquals(RatingPromptState.Available(), vm.state.value.rating)
    }

    @Test fun lastCollectorLeavingCancelsInitialLoadWithoutAnError() = runTest(dispatcher) {
        var cancelled = false
        load = {
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
        load = { rows }
        observe(vm)
        runCurrent()
        assertEquals(2, calls.size)
        assertEquals(ListUiState.Content(rows), vm.state.value.list)
    }

    @Test fun completedContentAndRatingSurviveConfigurationOrHiddenCollectionGap() = runTest(dispatcher) {
        val vm = viewModel()
        val collector = observe(vm)
        runCurrent()
        collector.cancel()
        runCurrent()
        observe(vm)
        vm.onResumed()
        runCurrent()
        assertEquals(1, calls.size)
        assertEquals(1, events.count { it == "show" })
        assertEquals(RatingPromptState.Available(), vm.state.value.rating)
    }

    @Test fun cancelledRefreshReturningEmptyCannotClearRetainedRating() = runTest(dispatcher) {
        val vm = viewModel()
        val collector = observe(vm)
        runCurrent()
        load = {
            try {
                awaitCancellation()
            } catch (_: CancellationException) {
                emptyList()
            }
        }
        vm.refresh()
        runCurrent()
        collector.cancel()
        runCurrent()
        load = { awaitCancellation() }
        observe(vm)
        runCurrent()
        assertEquals(RatingPromptState.Available(), vm.state.value.rating)
        assertEquals(ListUiState.Content(rows, isRefreshing = true), vm.state.value.list)
        assertEquals(1, events.count { it == "show" })
    }

    @Test fun cancellingRatingPersistsOnceAndStaysHiddenAfterRefresh() = runTest(dispatcher) {
        val vm = viewModel()
        observe(vm)
        runCurrent()
        vm.onRateCancel()
        vm.onRateCancel()
        runCurrent()
        assertEquals(1, saves)
        assertEquals(RatingPromptState.Hidden, vm.state.value.rating)
        assertFalse(vm.state.value.openRating)
        vm.refresh()
        runCurrent()
        assertEquals(RatingPromptState.Hidden, vm.state.value.rating)
        assertEquals(listOf("show", "cancel"), events)
    }

    @Test fun rateNowOpensStoreOnlyAfterSuccessfulPersistenceAndAcknowledgesLaunch() = runTest(dispatcher) {
        val saved = CompletableDeferred<Unit>()
        save = { saved.await() }
        val vm = viewModel()
        observe(vm)
        runCurrent()
        vm.onRateNow()
        runCurrent()
        assertEquals(RatingPromptState.Available(isSaving = true), vm.state.value.rating)
        assertFalse(vm.state.value.openRating)
        saved.complete(Unit)
        runCurrent()
        assertTrue(vm.state.value.openRating)
        assertEquals(RatingPromptState.Hidden, vm.state.value.rating)
        assertTrue(vm.consumeRatingRequest())
        runCurrent()
        assertFalse(vm.state.value.openRating)
        assertEquals(listOf("show", "rate"), events)
    }

    @Test fun failedPersistenceRetainsCardAndAllowsRetryWithoutOpeningStore() = runTest(dispatcher) {
        save = { error("storage unavailable") }
        val vm = viewModel()
        observe(vm)
        runCurrent()
        vm.onRateNow()
        runCurrent()
        assertEquals(RatingPromptState.Available(saveFailed = true), vm.state.value.rating)
        assertFalse(vm.state.value.openRating)
        save = {}
        vm.onRateNow()
        runCurrent()
        assertTrue(vm.state.value.openRating)
        assertEquals(2, saves)
    }

    @Test fun consumedStoreRequestCannotLaunchAgainWhenOldStateIsReplayed() = runTest(dispatcher) {
        val vm = viewModel()
        val collector = observe(vm)
        runCurrent()
        vm.onRateNow()
        runCurrent()
        assertTrue(vm.state.value.openRating)
        assertTrue(vm.consumeRatingRequest())
        collector.cancel()
        runCurrent()
        assertFalse(vm.consumeRatingRequest())
        observe(vm)
        runCurrent()
        assertFalse(vm.state.value.openRating)
    }

    @Test fun pendingRatingPersistenceSurvivesCollectionGapWithoutDuplicateSubmission() = runTest(dispatcher) {
        val saved = CompletableDeferred<Unit>()
        save = { saved.await() }
        val vm = viewModel()
        val collector = observe(vm)
        runCurrent()
        vm.onRateNow()
        runCurrent()
        collector.cancel()
        runCurrent()
        saved.complete(Unit)
        runCurrent()
        observe(vm)
        runCurrent()
        assertEquals(1, saves)
        assertTrue(vm.state.value.openRating)
        assertEquals(1, calls.size)
    }

    @Test fun clearedViewModelCannotPublishLaunchFromNoncooperativePersistence() = runTest(dispatcher) {
        val saved = CompletableDeferred<Unit>()
        save = { withContext(NonCancellable) { saved.await() } }
        val vm = viewModel()
        observe(vm)
        runCurrent()
        vm.onRateNow()
        runCurrent()
        store.clear()
        saved.complete(Unit)
        runCurrent()
        assertFalse(vm.consumeRatingRequest())
        assertFalse(vm.state.value.openRating)
    }

    @Test fun externallyHandledGlobalRatingIsHiddenOnRealReturnWithoutReloadingNodes() = runTest(dispatcher) {
        val vm = viewModel()
        vm.onResumed()
        val collector = observe(vm)
        runCurrent()
        assertEquals(RatingPromptState.Available(), vm.state.value.rating)
        vm.onPaused()
        collector.cancel()
        runCurrent()
        eligibility = { false }
        vm.onResumed()
        observe(vm)
        runCurrent()
        assertEquals(RatingPromptState.Hidden, vm.state.value.rating)
        assertEquals(ListUiState.Content(rows), vm.state.value.list)
        assertEquals(1, calls.size)
        assertEquals(2, eligibilityCalls)
    }

    @Test fun optionalRatingRetryDoesNotReloadNodes() = runTest(dispatcher) {
        eligibility = { error("preferences unavailable") }
        val vm = viewModel()
        observe(vm)
        runCurrent()
        eligibility = { true }
        vm.retryRating()
        runCurrent()
        assertEquals(RatingPromptState.Available(), vm.state.value.rating)
        assertEquals(1, calls.size)
        assertEquals(2, eligibilityCalls)
    }

    @Test fun visibleConfigurationRetainsCompletedEligibilityWhileHiddenReturnRechecksIt() = runTest(dispatcher) {
        val vm = viewModel()
        vm.onResumed()
        val collector = observe(vm)
        runCurrent()
        vm.onPaused()
        collector.cancel()
        runCurrent()
        vm.onConfigurationRecreation(wasVisible = true)
        vm.onResumed()
        val next = observe(vm)
        runCurrent()
        assertEquals(1, eligibilityCalls)
        vm.onPaused()
        next.cancel()
        runCurrent()
        vm.onConfigurationRecreation(wasVisible = false)
        vm.onResumed()
        observe(vm)
        runCurrent()
        assertEquals(2, eligibilityCalls)
        assertEquals(1, calls.size)
    }
}
