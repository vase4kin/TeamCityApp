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

package teamcityapp.features.build_history.impl

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelStore
import androidx.paging.testing.asSnapshot
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import teamcityapp.features.build_history.api.*

@OptIn(ExperimentalCoroutinesApi::class)
class BuildHistoryViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val store = ViewModelStore()
    private val repository = FakeHistoryRepository()
    private val onboarding = FakeHistoryOnboarding()
    private val tracker = FakeHistoryTracker()

    @Before fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @After fun cleanup() {
        store.clear()
        Dispatchers.resetMain()
    }
    private fun viewModel(handle: SavedStateHandle = SavedStateHandle(mapOf("id" to "configuration", "name" to "Build Android"))) = BuildHistoryViewModel(handle, repository, onboarding, tracker).also { store.put("history", it) }
    private fun TestScope.observe(vm: BuildHistoryViewModel) = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.controls.collect {} }

    @Test fun pagingProducesLocalizedRowAndSectionDescriptorsWithoutChangingLaunchPayloads() = runTest(dispatcher) {
        val queued = historyBuild("queued").copy(state = "queued", number = null, waitReason = null)
        repository.loadPage = { BuildHistoryPage(listOf(queued)) }
        val row = viewModel().selection.value.rows.asSnapshot().single()
        assertEquals(queued, row.build)
        assertEquals(teamcityapp.libraries.build_ui.R.string.build_queued, row.row.statusLabelRes)
        assertEquals(teamcityapp.libraries.theme.UiText.Resource(teamcityapp.libraries.build_ui.R.string.build_queued_fallback), row.row.statusText)
        assertEquals(teamcityapp.libraries.theme.UiText.Resource(R.string.history_queued_section), row.sectionTitle)
    }

    @Test fun controlsLoadOnlyOnCollectionAndPagesStayLazy() = runTest(dispatcher) {
        val vm = viewModel()
        runCurrent()
        assertEquals(0, repository.favoriteCalls)
        assertEquals(0, onboarding.loads)
        observe(vm)
        runCurrent()
        assertEquals(FavoriteState.Available(false), vm.controls.value.favorite)
        assertEquals(OnboardingState.Available(), vm.controls.value.onboarding)
        assertTrue(repository.requests.isEmpty())
    }

    @Test fun completedControlsAndCachedPagesSurviveConfigurationWithoutReload() = runTest(dispatcher) {
        val vm = viewModel()
        val collector = observe(vm)
        runCurrent()
        assertEquals(listOf(historyBuild()), vm.selection.value.pages.asSnapshot())
        collector.cancel()
        runCurrent()
        advanceTimeBy(10_000)
        observe(vm)
        runCurrent()
        assertEquals(listOf(historyBuild()), vm.selection.value.pages.asSnapshot())
        assertEquals(1, repository.favoriteCalls)
        assertEquals(1, onboarding.loads)
        assertEquals(1, repository.requests.size)
    }

    @Test fun losingLastCollectorCancelsPendingOptionalLoadsAndResubscriptionRetries() = runTest(dispatcher) {
        var favoriteCancelled = false
        var promptCancelled = false
        repository.loadFavorite = {
            try {
                awaitCancellation()
            } finally {
                favoriteCancelled = true
            }
        }
        onboarding.pending = {
            try {
                awaitCancellation()
            } finally {
                promptCancelled = true
            }
        }
        val vm = viewModel()
        val collector = observe(vm)
        runCurrent()
        collector.cancel()
        runCurrent()
        assertTrue(favoriteCancelled)
        assertTrue(promptCancelled)
        assertEquals(FavoriteState.Loading, vm.controls.value.favorite)
        repository.loadFavorite = { true }
        onboarding.pending = { emptyList() }
        observe(vm)
        runCurrent()
        assertEquals(FavoriteState.Available(true), vm.controls.value.favorite)
        assertEquals(2, repository.favoriteCalls)
        assertEquals(2, onboarding.loads)
    }

    @Test fun optionalFailuresRetryIndependentlyWithoutReloadingPages() = runTest(dispatcher) {
        repository.loadFavorite = { error("storage unavailable") }
        onboarding.pending = { error("tips unavailable") }
        val vm = viewModel()
        observe(vm)
        runCurrent()
        assertEquals(FavoriteState.Unavailable, vm.controls.value.favorite)
        assertEquals(R.string.history_favorite_unavailable, vm.controls.value.favoriteFailureMessageRes)
        assertEquals(OnboardingState.Unavailable, vm.controls.value.onboarding)
        repository.loadFavorite = { true }
        vm.retryFavorite()
        runCurrent()
        assertEquals(FavoriteState.Available(true), vm.controls.value.favorite)
        assertEquals(1, onboarding.loads)
        onboarding.pending = { listOf(BuildHistoryPrompt.Favorite) }
        vm.retryOnboarding()
        runCurrent()
        assertEquals(OnboardingState.Available(BuildHistoryPrompt.Favorite), vm.controls.value.onboarding)
        assertEquals(R.string.history_prompt_favorite_title, (vm.controls.value.onboarding as OnboardingState.Available).titleRes)
        assertEquals(R.string.history_prompt_favorite_description, (vm.controls.value.onboarding as OnboardingState.Available).descriptionRes)
        assertTrue(repository.requests.isEmpty())
    }

    @Test fun lateCanceledOptionalResponseCannotBecomeRetainedMembership() = runTest(dispatcher) {
        val response = CompletableDeferred<Boolean>()
        repository.loadFavorite = { withContext(NonCancellable) { response.await() } }
        val vm = viewModel()
        val collector = observe(vm)
        runCurrent()
        collector.cancel()
        runCurrent()
        response.complete(true)
        runCurrent()
        repository.loadFavorite = { false }
        observe(vm)
        runCurrent()
        assertEquals(FavoriteState.Available(false), vm.controls.value.favorite)
        assertEquals(2, repository.favoriteCalls)
    }

    @Test fun favoriteCanFinishWhileOptionalTipsAreStillLoading() = runTest(dispatcher) {
        onboarding.pending = { awaitCancellation() }
        val vm = viewModel()
        observe(vm)
        runCurrent()
        assertEquals(FavoriteState.Available(false), vm.controls.value.favorite)
        assertEquals(OnboardingState.Loading, vm.controls.value.onboarding)
    }

    @Test fun favoriteChangesPersistOnceAndPendingWriteSurvivesCollectionGap() = runTest(dispatcher) {
        val write = CompletableDeferred<Unit>()
        repository.writeFavorite = { _, _ -> write.await() }
        val vm = viewModel()
        val collector = observe(vm)
        runCurrent()
        vm.toggleFavorite()
        vm.toggleFavorite()
        runCurrent()
        assertEquals(FavoriteState.Available(false, updating = true), vm.controls.value.favorite)
        collector.cancel()
        runCurrent()
        write.complete(Unit)
        runCurrent()
        observe(vm)
        runCurrent()
        assertEquals(FavoriteState.Available(true), vm.controls.value.favorite)
        assertEquals(listOf("configuration" to true), repository.writes)
        assertEquals(BuildHistoryNoticeKind.FavoriteAdded, vm.controls.value.notice?.kind)
        assertEquals(R.string.history_favorite_added, vm.controls.value.notice?.messageRes)
        assertEquals(R.string.history_view, vm.controls.value.notice?.actionLabelRes)
        assertEquals(R.string.history_remove_favorite, vm.controls.value.favoriteActionLabelRes)
        vm.toggleFavorite()
        runCurrent()
        assertEquals(FavoriteState.Available(false), vm.controls.value.favorite)
        assertEquals(BuildHistoryNoticeKind.FavoriteRemoved, vm.controls.value.notice?.kind)
        assertEquals(R.string.history_favorite_removed, vm.controls.value.notice?.messageRes)
        assertNull(vm.controls.value.notice?.actionLabelRes)
        assertEquals(R.string.history_add_favorite, vm.controls.value.favoriteActionLabelRes)
    }

    @Test fun favoriteWriteFailureRetainsMembershipAndRetriesSameIntent() = runTest(dispatcher) {
        repository.writeFavorite = { _, _ -> error("disk") }
        val vm = viewModel()
        observe(vm)
        runCurrent()
        vm.toggleFavorite()
        runCurrent()
        assertEquals(FavoriteState.Available(false, updateFailed = true), vm.controls.value.favorite)
        assertEquals(R.string.history_favorite_update_failed, vm.controls.value.favoriteFailureMessageRes)
        assertNull(vm.controls.value.notice)
        repository.writeFavorite = { _, _ -> }
        vm.toggleFavorite()
        runCurrent()
        assertEquals(FavoriteState.Available(true), vm.controls.value.favorite)
        assertEquals(listOf("configuration" to true, "configuration" to true), repository.writes)
    }

    @Test fun onboardingPreservesRunFilterFavoriteOrderAndPersistsEachOnce() = runTest(dispatcher) {
        onboarding.pending = { listOf(BuildHistoryPrompt.Favorite, BuildHistoryPrompt.Run, BuildHistoryPrompt.Filter, BuildHistoryPrompt.Run) }
        val vm = viewModel()
        observe(vm)
        runCurrent()
        assertEquals(OnboardingState.Available(BuildHistoryPrompt.Run), vm.controls.value.onboarding)
        assertEquals(R.string.history_prompt_run_title, (vm.controls.value.onboarding as OnboardingState.Available).titleRes)
        assertEquals(R.string.history_prompt_run_description, (vm.controls.value.onboarding as OnboardingState.Available).descriptionRes)
        vm.dismissPrompt()
        vm.dismissPrompt()
        runCurrent()
        assertEquals(OnboardingState.Available(BuildHistoryPrompt.Filter), vm.controls.value.onboarding)
        assertEquals(R.string.history_prompt_filter_title, (vm.controls.value.onboarding as OnboardingState.Available).titleRes)
        assertEquals(R.string.history_prompt_filter_description, (vm.controls.value.onboarding as OnboardingState.Available).descriptionRes)
        vm.dismissPrompt()
        runCurrent()
        assertEquals(OnboardingState.Available(BuildHistoryPrompt.Favorite), vm.controls.value.onboarding)
        assertEquals(R.string.history_prompt_favorite_title, (vm.controls.value.onboarding as OnboardingState.Available).titleRes)
        assertEquals(R.string.history_prompt_favorite_description, (vm.controls.value.onboarding as OnboardingState.Available).descriptionRes)
        vm.dismissPrompt()
        runCurrent()
        assertEquals(OnboardingState.Available(), vm.controls.value.onboarding)
        assertEquals(BuildHistoryPrompt.entries, onboarding.shown)
    }

    @Test fun onboardingSaveFailureKeepsActualPromptUntilRetrySucceeds() = runTest(dispatcher) {
        onboarding.pending = { listOf(BuildHistoryPrompt.Filter, BuildHistoryPrompt.Favorite) }
        onboarding.write = { error("disk") }
        val vm = viewModel()
        observe(vm)
        runCurrent()
        vm.dismissPrompt()
        runCurrent()
        assertEquals(OnboardingState.Available(BuildHistoryPrompt.Filter, saveFailed = true), vm.controls.value.onboarding)
        assertEquals(R.string.history_retry, (vm.controls.value.onboarding as OnboardingState.Available).dismissLabelRes)
        onboarding.write = {}
        vm.dismissPrompt()
        runCurrent()
        assertEquals(OnboardingState.Available(BuildHistoryPrompt.Favorite), vm.controls.value.onboarding)
        assertEquals(R.string.history_prompt_favorite_title, (vm.controls.value.onboarding as OnboardingState.Available).titleRes)
        assertEquals(R.string.history_prompt_favorite_description, (vm.controls.value.onboarding as OnboardingState.Available).descriptionRes)
    }

    @Test fun pendingOnboardingSaveSurvivesConfigurationWithoutDuplicatingWrite() = runTest(dispatcher) {
        val saved = CompletableDeferred<Unit>()
        onboarding.pending = { listOf(BuildHistoryPrompt.Run) }
        onboarding.write = { saved.await() }
        val vm = viewModel()
        val collector = observe(vm)
        runCurrent()
        vm.dismissPrompt()
        runCurrent()
        collector.cancel()
        runCurrent()
        observe(vm)
        runCurrent()
        assertEquals(OnboardingState.Available(BuildHistoryPrompt.Run, saving = true), vm.controls.value.onboarding)
        vm.dismissPrompt()
        saved.complete(Unit)
        runCurrent()
        assertEquals(listOf(BuildHistoryPrompt.Run), onboarding.shown)
    }

    @Test fun clearingViewModelCancelsPendingFavoriteAndOnboardingWrites() = runTest(dispatcher) {
        var favoriteCancelled = false
        var promptCancelled = false
        repository.writeFavorite = { _, _ ->
            try {
                awaitCancellation()
            } finally {
                favoriteCancelled = true
            }
        }
        onboarding.pending = { listOf(BuildHistoryPrompt.Run) }
        onboarding.write = {
            try {
                awaitCancellation()
            } finally {
                promptCancelled = true
            }
        }
        val vm = viewModel()
        observe(vm)
        runCurrent()
        vm.toggleFavorite()
        vm.dismissPrompt()
        runCurrent()
        store.clear()
        runCurrent()
        assertTrue(favoriteCancelled)
        assertTrue(promptCancelled)
        assertNull(vm.controls.value.notice)
    }

    @Test fun effectiveFilterSurvivesSavedStateAndResetRestoresAnyDefault() = runTest(dispatcher) {
        val handle = SavedStateHandle(mapOf("id" to "configuration", BuildHistoryNavigation.EXTRA_LOCATOR to "running:true,personal:false"))
        val vm = viewModel(handle)
        assertEquals("running:true,personal:false", vm.selection.value.query.locator)
        vm.applyFilter("state:queued,pinned:any,personal:false")
        assertEquals("state:queued,pinned:any,personal:false", handle.get<String>(BuildHistoryViewModel.EFFECTIVE_LOCATOR))
        store.clear()
        val restored = viewModel(handle)
        assertEquals("state:queued,pinned:any,personal:false", restored.selection.value.query.locator)
        restored.resetFilters()
        assertEquals(BuildHistoryQuery.DEFAULT_LOCATOR, restored.selection.value.query.locator)
        assertEquals(BuildHistoryQuery.DEFAULT_LOCATOR, handle.get<String>(BuildHistoryViewModel.EFFECTIVE_LOCATOR))
    }

    @Test fun filterAndResetCreateFreshForcedGenerationsAndEachAppendUsesLatestLocator() = runTest(dispatcher) {
        repository.loadPage = { request ->
            if (request.next == null) {
                BuildHistoryPage((0..9).map { historyBuild(it.toString()) }, "next")
            } else {
                BuildHistoryPage((10..19).map { historyBuild(it.toString()) })
            }
        }
        val vm = viewModel()
        vm.selection.value.pages.asSnapshot { scrollTo(18) }
        assertEquals(listOf(false, true), repository.requests.map { it.force })
        vm.applyFilter("branch:name:feature/x,personal:false,pinned:false,count:10")
        vm.selection.value.pages.asSnapshot { scrollTo(18) }
        assertEquals(listOf(true, true), repository.requests.takeLast(2).map { it.force })
        assertTrue(repository.requests.takeLast(2).all { it.query.locator == "branch:name:feature/x,personal:false,pinned:false,count:10" })
        vm.resetFilters()
        vm.selection.value.pages.asSnapshot()
        assertEquals(BuildHistoryQuery.DEFAULT_LOCATOR, repository.requests.last().query.locator)
        assertTrue(repository.requests.last().force)
    }

    @Test fun replacingFilterCancelsObsoleteLoadAndPublishesMatchingQueryAndPagesAtomically() = runTest(dispatcher) {
        var oldCancelled = false
        repository.loadPage = { request ->
            if (request.query.locator == BuildHistoryQuery.DEFAULT_LOCATOR) {
                try {
                    awaitCancellation()
                } finally {
                    oldCancelled = true
                }
            } else {
                BuildHistoryPage(listOf(historyBuild("filtered")))
            }
        }
        val vm = viewModel()
        backgroundScope.launch { vm.selection.value.pages.asSnapshot() }
        runCurrent()
        vm.applyFilter("state:queued")
        runCurrent()
        assertTrue(oldCancelled)
        assertEquals("state:queued", vm.selection.value.query.locator)
        assertEquals(listOf(historyBuild("filtered")), vm.selection.value.pages.asSnapshot())
    }

    @Test fun pullRefreshAndInitialRetryForceCurrentEffectiveFilterWithoutReset() = runTest(dispatcher) {
        val vm = viewModel(SavedStateHandle(mapOf("id" to "configuration", BuildHistoryNavigation.EXTRA_LOCATOR to "pinned:true")))
        vm.selection.value.pages.asSnapshot {
            vm.prepareRefresh()
            refresh()
        }
        assertEquals(listOf(false, true), repository.requests.map { it.force })
        assertTrue(repository.requests.all { it.query.locator == "pinned:true" })
    }

    @Test fun queuedResultRefreshIsConsumedOnceAndPreservesShowNotice() = runTest(dispatcher) {
        val vm = viewModel()
        observe(vm)
        runCurrent()
        vm.onQueuedBuildResult("/queued")
        runCurrent()
        assertTrue(vm.controls.value.refreshPending)
        assertTrue(vm.claimRefresh())
        assertFalse(vm.claimRefresh())
        vm.prepareRefresh()
        runCurrent()
        assertEquals(BuildHistoryNoticeKind.Queued, vm.controls.value.notice?.kind)
        assertEquals(R.string.history_queued, vm.controls.value.notice?.messageRes)
        assertEquals(R.string.history_show, vm.controls.value.notice?.actionLabelRes)
        vm.applyFilter("running:true")
        runCurrent()
        assertEquals(BuildHistoryNoticeKind.FiltersApplied, vm.controls.value.notice?.kind)
        assertEquals(R.string.history_filters_applied, vm.controls.value.notice?.messageRes)
        assertEquals(R.string.history_reset, vm.controls.value.notice?.actionLabelRes)
        vm.prepareRefresh()
        runCurrent()
        assertNull(vm.controls.value.notice)
    }

    @Test fun queuedShowReturnsFullLaunchOnceAndRetriesFailureWithoutDuplicates() = runTest(dispatcher) {
        val result = historyBuild().copy(personal = true, pinned = true)
        repository.loadQueued = { error("offline") }
        val vm = viewModel()
        observe(vm)
        runCurrent()
        vm.onQueuedBuildResult("/queued")
        vm.openQueuedBuild()
        vm.openQueuedBuild()
        runCurrent()
        assertEquals(QueuedBuildState.Failed, vm.controls.value.queuedBuild)
        assertEquals(listOf("/queued"), repository.queuedRequests)
        repository.loadQueued = { result }
        vm.openQueuedBuild()
        runCurrent()
        assertEquals(result, vm.claimQueuedBuild())
        assertNull(vm.claimQueuedBuild())
        assertEquals(2, tracker.queued)
    }

    @Test fun newQueuedResultCancelsOlderPendingShowAndNeverLaunchesOldBuild() = runTest(dispatcher) {
        val oldResponse = CompletableDeferred<teamcityapp.libraries.builds.BuildLaunchData>()
        repository.loadQueued = { if (it == "/old") withContext(NonCancellable) { oldResponse.await() } else historyBuild("new") }
        val vm = viewModel()
        observe(vm)
        runCurrent()
        vm.onQueuedBuildResult("/old")
        vm.openQueuedBuild()
        runCurrent()
        vm.onQueuedBuildResult("/new")
        runCurrent()
        oldResponse.complete(historyBuild("old"))
        runCurrent()
        assertNull(vm.claimQueuedBuild())
        vm.openQueuedBuild()
        runCurrent()
        assertEquals(historyBuild("new"), vm.claimQueuedBuild())
    }

    @Test fun blankQueuedResultShowsFailureAndDismissDoesNotLaunch() = runTest(dispatcher) {
        val vm = viewModel()
        observe(vm)
        runCurrent()
        vm.onQueuedBuildResult("")
        vm.openQueuedBuild()
        runCurrent()
        assertEquals(QueuedBuildState.Failed, vm.controls.value.queuedBuild)
        assertTrue(repository.queuedRequests.isEmpty())
        vm.dismissQueuedFailure()
        runCurrent()
        assertEquals(QueuedBuildState.Idle, vm.controls.value.queuedBuild)
        assertNull(vm.claimQueuedBuild())
    }

    @Test fun oldSnackbarAcknowledgementCannotDismissNewNotice() = runTest(dispatcher) {
        val vm = viewModel()
        observe(vm)
        runCurrent()
        vm.applyFilter("running:true")
        runCurrent()
        val old = vm.controls.value.notice!!
        vm.onQueuedBuildResult("/queued")
        runCurrent()
        vm.dismissNotice(old.id)
        runCurrent()
        assertEquals(BuildHistoryNoticeKind.Queued, vm.controls.value.notice?.kind)
        assertEquals(R.string.history_queued, vm.controls.value.notice?.messageRes)
        assertEquals(R.string.history_show, vm.controls.value.notice?.actionLabelRes)
    }

    @Test fun screenAndRunTelemetryAreExplicitUiEvents() {
        val vm = viewModel()
        vm.onResumed()
        vm.onRunBuildClicked()
        assertEquals(1, tracker.views)
        assertEquals(1, tracker.runs)
    }

    @Test fun realReturnRechecksExternallyChangedFavoriteAndGlobalPromptsWithoutReloadingPages() = runTest(dispatcher) {
        onboarding.pending = { listOf(BuildHistoryPrompt.Run) }
        val vm = viewModel()
        vm.onResumed()
        observe(vm)
        runCurrent()
        val pages = vm.selection.value.pages
        assertEquals(listOf(historyBuild()), pages.asSnapshot())
        assertEquals(OnboardingState.Available(BuildHistoryPrompt.Run), vm.controls.value.onboarding)
        assertEquals(R.string.history_prompt_run_title, (vm.controls.value.onboarding as OnboardingState.Available).titleRes)
        assertEquals(R.string.history_prompt_run_description, (vm.controls.value.onboarding as OnboardingState.Available).descriptionRes)
        vm.onPaused()
        repository.loadFavorite = { true }
        onboarding.pending = { emptyList() }
        vm.onResumed()
        runCurrent()
        assertEquals(FavoriteState.Available(true), vm.controls.value.favorite)
        assertEquals(OnboardingState.Available(), vm.controls.value.onboarding)
        assertEquals(2, repository.favoriteCalls)
        assertEquals(2, onboarding.loads)
        assertSame(pages, vm.selection.value.pages)
        assertEquals(listOf(historyBuild()), pages.asSnapshot())
        assertEquals(1, repository.requests.size)
    }

    @Test fun configurationReturnKeepsCompletedControlsAndDoesNotRecheckPreferences() = runTest(dispatcher) {
        val vm = viewModel()
        vm.onResumed()
        val collector = observe(vm)
        runCurrent()
        vm.onPaused()
        collector.cancel()
        runCurrent()
        vm.onConfigurationRecreation()
        vm.onResumed()
        observe(vm)
        runCurrent()
        assertEquals(1, repository.favoriteCalls)
        assertEquals(1, onboarding.loads)
    }

    @Test fun realReturnDoesNotRacePendingFavoriteOrPromptPersistence() = runTest(dispatcher) {
        onboarding.pending = { listOf(BuildHistoryPrompt.Run, BuildHistoryPrompt.Filter) }
        val favoriteSaved = CompletableDeferred<Unit>()
        val promptSaved = CompletableDeferred<Unit>()
        repository.writeFavorite = { _, _ -> favoriteSaved.await() }
        onboarding.write = { promptSaved.await() }
        val vm = viewModel()
        vm.onResumed()
        observe(vm)
        runCurrent()
        vm.toggleFavorite()
        vm.dismissPrompt()
        runCurrent()
        vm.onPaused()
        vm.onResumed()
        runCurrent()
        assertEquals(1, repository.favoriteCalls)
        assertEquals(1, onboarding.loads)
        assertEquals(FavoriteState.Available(false, updating = true), vm.controls.value.favorite)
        assertEquals(OnboardingState.Available(BuildHistoryPrompt.Run, saving = true), vm.controls.value.onboarding)
        favoriteSaved.complete(Unit)
        promptSaved.complete(Unit)
        runCurrent()
        assertEquals(FavoriteState.Available(true), vm.controls.value.favorite)
        assertEquals(OnboardingState.Available(BuildHistoryPrompt.Filter), vm.controls.value.onboarding)
        assertEquals(R.string.history_prompt_filter_title, (vm.controls.value.onboarding as OnboardingState.Available).titleRes)
        assertEquals(R.string.history_prompt_filter_description, (vm.controls.value.onboarding as OnboardingState.Available).descriptionRes)
        assertEquals(1, repository.writes.size)
        assertEquals(listOf(BuildHistoryPrompt.Run), onboarding.shown)
    }
}
