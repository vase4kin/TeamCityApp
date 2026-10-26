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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import teamcityapp.libraries.list_ui.PagingListSnapshot

@Composable
fun SharedChangesContent(viewModel: ChangesViewModel, onChange: (teamcityapp.features.change_details.api.ChangeDetails) -> Unit, onCount: (Int) -> Unit = {}, parentCount: ChangesCountState? = null, onParentCountRetry: (() -> Unit)? = null, onUnavailableCount: () -> Unit = {}) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val visiblePages = remember(viewModel, lifecycle) { viewModel.rows.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED) }
    val changes = visiblePages.collectAsLazyPagingItems()
    val count = parentCount ?: run {
        val ownCount by viewModel.count.collectAsStateWithLifecycle()
        ownCount
    }
    if (parentCount == null) {
        LaunchedEffect(count, onCount, onUnavailableCount) {
            when (val current = count) {
                is ChangesCountState.Available -> onCount(current.count)
                ChangesCountState.Unavailable -> onUnavailableCount()
                ChangesCountState.Loading -> Unit
            }
        }
    }
    var completed by rememberSaveable { mutableStateOf(false) }
    val refresh = changes.loadState.refresh
    if (refresh is LoadState.NotLoading) SideEffect { completed = true }
    val rows = PagingListSnapshot(changes)
    val state = changesListState(rows.items.map { it.change }, refresh, completed)
    ChangesScreen(
        state, count, rows.itemCount,
        itemAt = rows::get,
        itemKey = { rows.peek(it)?.change?.id ?: "changes:placeholder:$it" },
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
        onCountRetry = onParentCountRetry ?: viewModel::retryCount,
        onChange = onChange,
        onAppendRetry = changes::retry
    )
}
