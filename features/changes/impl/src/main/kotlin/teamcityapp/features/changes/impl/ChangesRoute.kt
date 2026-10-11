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

package teamcityapp.features.changes.impl

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import teamcityapp.features.changes.impl.router.ChangesRouter

@Composable
internal fun ChangesRoute(router: ChangesRouter, viewModel: ChangesViewModel = hiltViewModel()) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val visiblePages = remember(viewModel, lifecycle) { viewModel.rows.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED) }
    val changes = visiblePages.collectAsLazyPagingItems()
    val count by viewModel.count.collectAsStateWithLifecycle()
    LaunchedEffect(count, router) {
        when (val current = count) {
            is ChangesCountState.Available -> router.updateTabCount(current.count)
            ChangesCountState.Unavailable -> router.updateTabCount(0)
            ChangesCountState.Loading -> Unit
        }
    }
    var completed by rememberSaveable { mutableStateOf(false) }
    val refresh = changes.loadState.refresh
    if (refresh is LoadState.NotLoading) SideEffect { completed = true }
    // Snapshot items are used only for shell presentation; row access still uses Paging[index].
    val state = changesListState(changes.itemSnapshotList.items.map { it.change }, refresh, completed)
    ChangesScreen(
        state, count, changes.itemCount,
        itemAt = { changes[it] },
        itemKey = { changes.peek(it)?.change?.id ?: "changes:placeholder:$it" },
        appendState = when (changes.loadState.append) {
            is LoadState.Loading -> ChangesAppendState.Loading
            is LoadState.Error -> ChangesAppendState.Error
            is LoadState.NotLoading -> ChangesAppendState.Idle
        },
        onRefresh = {
            viewModel.prepareRefresh()
            changes.refresh()
        },
        onRetry = {
            viewModel.prepareRefresh()
            changes.refresh()
        },
        onCountRetry = viewModel::retryCount,
        onChange = router::openChange,
        onAppendRetry = changes::retry
    )
}
