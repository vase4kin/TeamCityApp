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

import androidx.paging.LoadState
import org.junit.Assert.*
import org.junit.Test
import teamcityapp.libraries.list_state.ListUiState

class BuildHistoryPresentationTest {
    @Test fun initialLoadAndErrorReplaceShellWhileCompletedEmptyRefreshRetainsEmpty() {
        assertEquals(ListUiState.Loading, historyListState(emptyList(), LoadState.Loading, false))
        assertEquals(ListUiState.Error, historyListState(emptyList(), LoadState.Error(Exception()), false))
        assertEquals(ListUiState.Empty(isRefreshing = true), historyListState(emptyList(), LoadState.Loading, true))
        assertEquals(ListUiState.Empty(refreshFailed = true), historyListState(emptyList(), LoadState.Error(Exception()), true))
    }

    @Test fun contentSurvivesRefreshFailure() {
        val rows = listOf(historyBuild())
        assertEquals(ListUiState.Content(rows, isRefreshing = true), historyListState(rows, LoadState.Loading, true))
        assertEquals(ListUiState.Content(rows, refreshFailed = true), historyListState(rows, LoadState.Error(Exception()), true))
    }

    @Test fun validLegacyDateKeepsEnglishMonthAndIgnoresServerOffset() {
        assertEquals("10 October", historyDate(historyBuild()))
        assertEquals("10 October", historySectionKey(historyBuild().copy(startDate = "20261010T102030-1200")))
        assertEquals("queued", historySectionKey(historyBuild(state = "queued")))
    }

    @Test fun missingOrMalformedDateGetsStableUnknownSection() {
        assertNull(historyDate(historyBuild().copy(startDate = null)))
        assertEquals("unknown", historySectionKey(historyBuild().copy(startDate = "bad")))
        assertNull(historyDate(historyBuild().copy(startDate = "20260231T102030")))
    }
}
