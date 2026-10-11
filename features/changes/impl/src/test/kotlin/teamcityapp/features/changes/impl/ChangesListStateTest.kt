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
import org.junit.Assert.assertEquals
import org.junit.Test
import teamcityapp.libraries.list_state.ListUiState

class ChangesListStateTest {
    @Test fun initialFailureHasFullScreenRetryWhileCompletedEmptyListHasRefreshRetry() {
        val error = LoadState.Error(IllegalStateException("offline"))
        assertEquals(ListUiState.Error, changesListState(emptyList(), error, completed = false))
        assertEquals(ListUiState.Empty(refreshFailed = true), changesListState(emptyList(), error, completed = true))
    }

    @Test fun reloadingEmptyContentKeepsEmptyPresentation() {
        assertEquals(ListUiState.Loading, changesListState(emptyList(), LoadState.Loading, completed = false))
        assertEquals(ListUiState.Empty(isRefreshing = true), changesListState(emptyList(), LoadState.Loading, completed = true))
    }

    @Test fun refreshingAndFailedContentKeepRowsAvailable() {
        val rows = listOf(change())
        assertEquals(ListUiState.Content(rows, isRefreshing = true), changesListState(rows, LoadState.Loading, completed = true))
        assertEquals(ListUiState.Content(rows, refreshFailed = true), changesListState(rows, LoadState.Error(IllegalStateException()), completed = true))
    }
}
