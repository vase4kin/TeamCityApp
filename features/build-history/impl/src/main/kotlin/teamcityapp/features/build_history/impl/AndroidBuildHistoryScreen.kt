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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import teamcityapp.libraries.builds.BuildLaunchData
import teamcityapp.libraries.list_state.ListUiState
@Composable
internal fun BuildHistoryScreen(
    title: String,
    state: ListUiState<BuildLaunchData>,
    controls: BuildHistoryControls,
    itemCount: Int,
    itemAt: (Int) -> HistoryBuildRow?,
    itemPeek: (Int) -> HistoryBuildRow?,
    appendState: HistoryAppendState,
    onBack: () -> Unit,
    onRunBuild: () -> Unit,
    onFilters: () -> Unit,
    onFavorite: () -> Unit,
    onFavoriteRetry: () -> Unit,
    onPromptDismiss: () -> Unit,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    onAppendRetry: () -> Unit,
    onBuild: (BuildLaunchData) -> Unit,
    modifier: Modifier = Modifier,
    snackbar: SnackbarHostState = remember { SnackbarHostState() },
    listState: LazyListState = rememberLazyListState()
) = SharedBuildHistoryScreen(
    title, state, controls, itemCount, itemAt, itemPeek, appendState,
    onBack, onRunBuild, onFilters, onFavorite, onFavoriteRetry, onPromptDismiss, onRefresh, onRetry,
    onAppendRetry, onBuild, modifier, snackbar, listState,
    coachmark = { prompt, bounds, dismiss -> HistoryCoachmark(prompt, bounds, dismiss) }
)
