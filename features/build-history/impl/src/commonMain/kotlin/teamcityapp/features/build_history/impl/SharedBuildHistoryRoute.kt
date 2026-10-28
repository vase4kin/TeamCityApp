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

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.*
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import org.jetbrains.compose.resources.stringResource
import teamcityapp.libraries.list_ui.PagingListSnapshot

@Composable
fun SharedBuildHistoryRoute(
    vm: BuildHistoryViewModel,
    title: String,
    onLocatorChanged: (String) -> Unit,
    onBack: () -> Unit,
    onRunBuild: () -> Unit,
    onFilters: () -> Unit,
    onBuild: (teamcityapp.libraries.builds.BuildLaunchData) -> Unit,
    onQueuedBuild: (String) -> Unit,
    onFavorites: () -> Unit
) {
    LifecycleResumeEffect(vm) {
        vm.onScreenViewed()
        onPauseOrDispose {}
    }
    val controls by vm.controls.collectAsStateWithLifecycle()
    val selection by vm.selection.collectAsStateWithLifecycle()
    LaunchedEffect(selection.query.locator) {
        onLocatorChanged(selection.query.locator)
    }
    key(selection.pages) {
        val builds = selection.rows.collectAsLazyPagingItems()
        val rows = PagingListSnapshot(builds)
        var completed by remember { mutableStateOf(false) }
        if (builds.loadState.refresh is LoadState.NotLoading) SideEffect { completed = true }
        val refresh = {
            vm.prepareRefresh()
            builds.refresh()
        }
        androidx.lifecycle.compose.LifecycleResumeEffect(controls.refreshPending, selection.pages) {
            if (vm.claimRefresh()) refresh()
            onPauseOrDispose {}
        }
        val snackbar = remember { SnackbarHostState() }
        val notice = controls.notice
        val message = notice?.let { stringResource(it.messageRes) }.orEmpty()
        val action = notice?.actionLabelRes?.let { stringResource(it) }
        val lifecycle = androidx.lifecycle.compose.LocalLifecycleOwner.current.lifecycle
        LaunchedEffect(notice?.id, lifecycle) {
            if (notice != null) {
                lifecycle.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.RESUMED) {
                    val result = snackbar.showSnackbar(message, action, duration = if (notice.kind == BuildHistoryNoticeKind.FiltersApplied) SnackbarDuration.Indefinite else SnackbarDuration.Long)
                    if (result == SnackbarResult.ActionPerformed) {
                        when (notice.kind) {
                            BuildHistoryNoticeKind.Queued -> vm.queuedBuildReference()?.let { onQueuedBuild(it) }
                            BuildHistoryNoticeKind.FiltersApplied -> vm.resetFilters()
                            BuildHistoryNoticeKind.FavoriteAdded -> onFavorites()
                            else -> Unit
                        }
                    }
                    vm.dismissNotice(notice.id)
                }
            }
        }
        SharedBuildHistoryScreen(
            title, historyListState(rows.items.map { it.build }, builds.loadState.refresh, completed),
            controls, rows.itemCount, rows::get, rows::peek,
            when (builds.loadState.append) {
                is LoadState.Loading -> HistoryAppendState.Loading
                is LoadState.Error -> HistoryAppendState.Error
                else -> HistoryAppendState.Idle
            },
            onBack,
            {
                vm.onRunBuildClicked()
                onRunBuild()
            },
            onFilters,
            vm::toggleFavorite, vm::retryFavorite, vm::dismissPrompt, refresh, refresh, builds::retry,
            onBuild,
            snackbar = snackbar
        )
    }
}
