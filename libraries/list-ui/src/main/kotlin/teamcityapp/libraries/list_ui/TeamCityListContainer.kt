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

package teamcityapp.libraries.list_ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import teamcityapp.libraries.list_state.ListUiState
import teamcityapp.libraries.theme.ErrorContent
import teamcityapp.libraries.theme.ErrorNotice
import teamcityapp.libraries.theme.TeamCityTheme

/**
 * Presentation shell for a list. The feature owns its lazy list, keys, rows and scroll state.
 * Refresh failures leave the last completed content visible and expose an accessible retry.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamCityListContainer(
    state: ListUiState<*>,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    loading: @Composable () -> Unit = { TeamCityListLoading() },
    empty: @Composable () -> Unit,
    refreshFailureMessage: String? = null,
    content: @Composable () -> Unit
) {
    Surface(modifier) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val failureMaxHeight = (maxHeight / 3).coerceAtLeast(48.dp)
            when (state) {
                ListUiState.Loading -> loading()

                ListUiState.Error -> ErrorContent(Modifier.fillMaxSize(), onRetry)

                is ListUiState.Empty, is ListUiState.Content -> {
                    val refreshing = when (state) {
                        is ListUiState.Empty -> state.isRefreshing
                        is ListUiState.Content -> state.isRefreshing
                    }
                    val refreshFailed = when (state) {
                        is ListUiState.Empty -> state.refreshFailed
                        is ListUiState.Content -> state.refreshFailed
                    }
                    Column(Modifier.fillMaxSize()) {
                        if (refreshFailed) {
                            RefreshFailure(onRetry, enabled = !refreshing, message = refreshFailureMessage, maxHeight = failureMaxHeight)
                        }
                        PullToRefreshBox(
                            isRefreshing = refreshing,
                            onRefresh = onRefresh,
                            modifier = Modifier.weight(1f).fillMaxWidth()
                        ) {
                            if (state is ListUiState.Empty) {
                                // A scrollable empty surface lets the same pull gesture reload an empty list.
                                BoxWithConstraints(Modifier.fillMaxSize()) {
                                    Box(
                                        Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).heightIn(min = maxHeight),
                                        contentAlignment = Alignment.Center
                                    ) { empty() }
                                }
                            } else {
                                content()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RefreshFailure(onRetry: () -> Unit, enabled: Boolean, message: String?, maxHeight: androidx.compose.ui.unit.Dp) {
    ErrorNotice(
        message = message ?: stringResource(R.string.list_refresh_failed),
        onRetry = onRetry,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp).heightIn(max = maxHeight),
        actionLabel = stringResource(R.string.list_action_retry),
        enabled = enabled
    )
}

@Preview
@Composable
private fun ListContainerPreview() {
    TeamCityTheme {
        TeamCityListContainer(ListUiState.Empty(), {}, {}, Modifier.fillMaxSize(), empty = { TeamCityListEmpty("No items") }) { }
    }
}
