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

import androidx.paging.LoadState
import teamcityapp.features.change_details.api.ChangeDetails
import teamcityapp.libraries.list_state.ListUiState

/** Initial failures replace loading; refresh failures retain the completed list, even if empty. */
internal fun changesListState(items: List<ChangeDetails>, refresh: LoadState, completed: Boolean): ListUiState<ChangeDetails> = if (items.isNotEmpty()) {
    ListUiState.Content(items, isRefreshing = refresh is LoadState.Loading, refreshFailed = refresh is LoadState.Error)
} else {
    when (refresh) {
        is LoadState.Loading -> if (completed) ListUiState.Empty(isRefreshing = true) else ListUiState.Loading
        is LoadState.Error -> if (completed) ListUiState.Empty(refreshFailed = true) else ListUiState.Error
        is LoadState.NotLoading -> ListUiState.Empty()
    }
}
