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

package teamcityapp.features.tests.impl

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import teamcityapp.features.tests.api.TestOccurrence
import teamcityapp.features.tests.impl.router.TestsRouter
import teamcityapp.libraries.list_state.ListUiState

@Composable
internal fun TestsRoute(router: TestsRouter, viewModel: TestsViewModel = hiltViewModel()) {
    val selection by viewModel.selection.collectAsStateWithLifecycle()
    val count by viewModel.count.collectAsStateWithLifecycle()
    LaunchedEffect(count, router) {
        when (val current = count) {
            is TestsCountState.Available -> router.updateTabCount(current.count)
            TestsCountState.Unavailable -> router.updateTabCount(0)
            TestsCountState.Loading -> Unit
        }
    }
    // A new filter gets a fresh Paging presenter and scroll state; its previous rows never
    // render with the newly selected filter. A configuration recreation reuses its generation.
    key(selection.filter.name) {
        val lifecycle = LocalLifecycleOwner.current.lifecycle
        val visiblePages = remember(selection.pages, lifecycle) { selection.pages.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED) }
        val tests = visiblePages.collectAsLazyPagingItems()
        var completed by rememberSaveable { mutableStateOf(false) }
        val refresh = tests.loadState.refresh
        if (refresh is LoadState.NotLoading) SideEffect { completed = true }
        val state: ListUiState<TestOccurrence> = if (tests.itemCount > 0) {
            ListUiState.Content(tests.itemSnapshotList.items, isRefreshing = refresh is LoadState.Loading, refreshFailed = refresh is LoadState.Error)
        } else {
            when (refresh) {
                is LoadState.Loading -> if (completed) ListUiState.Empty(isRefreshing = true) else ListUiState.Loading
                is LoadState.Error -> if (completed) ListUiState.Empty(refreshFailed = true) else ListUiState.Error
                is LoadState.NotLoading -> ListUiState.Empty()
            }
        }
        TestsScreen(
            state = state,
            filter = selection.filter,
            counts = viewModel.counts,
            countState = count,
            itemCount = tests.itemCount,
            itemAt = { tests[it] },
            itemPeek = { tests.peek(it) },
            appendState = when (tests.loadState.append) {
                is LoadState.Loading -> TestsAppendState.Loading
                is LoadState.Error -> TestsAppendState.Error
                is LoadState.NotLoading -> TestsAppendState.Idle
            },
            onFilter = viewModel::selectFilter,
            onRefresh = {
                viewModel.prepareRefresh()
                tests.refresh()
            },
            onRetry = {
                viewModel.prepareRefresh()
                tests.refresh()
            },
            onCountRetry = viewModel::retryCount,
            onFailedTest = router::openFailedTest,
            onAppendRetry = tests::retry
        )
    }
}
