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

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalAccessibilityManager
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.currentStateAsState
import androidx.lifecycle.flowWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import kotlinx.coroutines.delay
import teamcityapp.features.build_history.impl.router.BuildHistoryRouter

@Composable
internal fun BuildHistoryRoute(router: BuildHistoryRouter, viewModel: BuildHistoryViewModel = hiltViewModel()) {
    LifecycleResumeEffect(viewModel) {
        viewModel.onResumed()
        onPauseOrDispose { viewModel.onPaused() }
    }
    val selection by viewModel.selection.collectAsStateWithLifecycle(minActiveState = Lifecycle.State.RESUMED)
    val controls by viewModel.controls.collectAsStateWithLifecycle(minActiveState = Lifecycle.State.RESUMED)
    val lifecycleState by LocalLifecycleOwner.current.lifecycle.currentStateAsState()
    var completed by rememberSaveable(selection.query) { mutableStateOf(false) }
    val listState = rememberSaveable(selection.query, saver = LazyListState.Saver) { LazyListState() }
    // A query and its Paging generation travel together. No new filter can display old rows.
    // PagingData has its own cached page-event stream. Dispose its presenter while hidden,
    // retaining completed-empty and scroll state here alongside the ViewModel's page cache.
    if (lifecycleState.isAtLeast(Lifecycle.State.RESUMED)) {
        key(selection.pages) {
            BuildHistoryPagingContent(selection, controls, router, viewModel, completed, { completed = true }, listState)
        }
    }
}

@Composable
private fun BuildHistoryPagingContent(
    selection: HistorySelection,
    controls: BuildHistoryControls,
    router: BuildHistoryRouter,
    viewModel: BuildHistoryViewModel,
    completed: Boolean,
    onCompleted: () -> Unit,
    listState: LazyListState
) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val visiblePages = remember(selection.rows, lifecycle) { selection.rows.flowWithLifecycle(lifecycle, Lifecycle.State.RESUMED) }
    val builds = visiblePages.collectAsLazyPagingItems()
    if (builds.loadState.refresh is LoadState.NotLoading) SideEffect { onCompleted() }
    val state = historyListState(builds.itemSnapshotList.items.map { it.build }, builds.loadState.refresh, completed)
    val refresh = {
        viewModel.prepareRefresh()
        builds.refresh()
    }
    LifecycleResumeEffect(controls.refreshPending, selection.pages) {
        if (viewModel.claimRefresh()) refresh()
        onPauseOrDispose { }
    }
    LifecycleResumeEffect(controls.queuedBuild, router) {
        viewModel.claimQueuedBuild()?.let { router.openBuild(it, viewModel.configurationName) }
        onPauseOrDispose { }
    }
    val accessibility = LocalAccessibilityManager.current
    LaunchedEffect(controls.queuedBuild, lifecycle) {
        if (controls.queuedBuild == QueuedBuildState.Failed) {
            delay(accessibility?.calculateRecommendedTimeoutMillis(4_000L, containsIcons = false, containsText = true, containsControls = true) ?: 4_000L)
            viewModel.dismissQueuedFailure()
        }
    }

    val snackbar = remember { SnackbarHostState() }
    val notice = controls.notice
    val message = notice?.let { stringResource(it.messageRes) }.orEmpty()
    val action = notice?.actionLabelRes?.let { stringResource(it) }
    LaunchedEffect(notice?.id, snackbar) {
        if (notice == null) return@LaunchedEffect
        val result = snackbar.showSnackbar(
            message,
            action,
            withDismissAction = true,
            duration = if (notice.kind == BuildHistoryNoticeKind.FiltersApplied) SnackbarDuration.Indefinite else SnackbarDuration.Long
        )
        if (result == SnackbarResult.ActionPerformed) {
            when (notice.kind) {
                BuildHistoryNoticeKind.Queued -> viewModel.openQueuedBuild()
                BuildHistoryNoticeKind.FiltersApplied -> viewModel.resetFilters()
                BuildHistoryNoticeKind.FavoriteAdded -> router.openFavorites()
                BuildHistoryNoticeKind.FavoriteRemoved -> Unit
            }
        }
        viewModel.dismissNotice(notice.id)
    }

    BuildHistoryScreen(
        title = viewModel.configurationName,
        state = state,
        controls = controls,
        itemCount = builds.itemCount,
        itemAt = { builds[it] },
        itemPeek = { builds.peek(it) },
        appendState = when (builds.loadState.append) {
            is LoadState.Loading -> HistoryAppendState.Loading
            is LoadState.Error -> HistoryAppendState.Error
            is LoadState.NotLoading -> HistoryAppendState.Idle
        },
        onBack = router::navigateUp,
        onRunBuild = {
            viewModel.onRunBuildClicked()
            router.openRunBuild(viewModel.configurationId)
        },
        onFilters = { router.openFilterBuilds(viewModel.configurationId) },
        onFavorite = viewModel::toggleFavorite,
        onFavoriteRetry = viewModel::retryFavorite,
        onOnboardingRetry = viewModel::retryOnboarding,
        onPromptDismiss = viewModel::dismissPrompt,
        onRefresh = refresh,
        onRetry = refresh,
        onAppendRetry = builds::retry,
        onBuild = { router.openBuild(it, viewModel.configurationName) },
        onQueuedRetry = viewModel::openQueuedBuild,
        onQueuedDismiss = viewModel::dismissQueuedFailure,
        snackbar = snackbar,
        listState = listState
    )
}
