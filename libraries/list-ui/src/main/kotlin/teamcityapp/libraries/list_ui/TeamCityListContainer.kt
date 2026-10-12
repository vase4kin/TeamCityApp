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
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import teamcityapp.libraries.list_state.ListUiState
import teamcityapp.libraries.theme.ErrorContent
import teamcityapp.libraries.theme.ErrorNotice
import teamcityapp.libraries.theme.R as ThemeR
import teamcityapp.libraries.theme.TeamCityDimensions
import teamcityapp.libraries.theme.TeamCityTheme

/**
 * Presentation shell for a list. The feature owns its lazy list, keys, rows and scroll state.
 * Refresh failures leave the last completed content visible and expose an accessible retry.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TeamCityListContainer(
    state: ListUiState<*>,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    loading: @Composable () -> Unit = { TeamCityListLoading() },
    empty: @Composable () -> Unit,
    refreshFailureMessage: String? = null,
    updatesAvailable: Boolean = false,
    onApplyUpdates: () -> Unit = {},
    content: @Composable () -> Unit
) {
    Surface(modifier) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val failureMaxHeight = (maxHeight / 3).coerceAtLeast(TeamCityDimensions.minimumTouchTarget)
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
                    var failureDismissed by rememberSaveable(refreshFailed) { mutableStateOf(false) }
                    Box(Modifier.fillMaxSize()) {
                        val refreshState = rememberPullToRefreshState()
                        PullToRefreshBox(
                            isRefreshing = refreshing,
                            onRefresh = onRefresh,
                            state = refreshState,
                            indicator = {
                                PullToRefreshDefaults.LoadingIndicator(
                                    state = refreshState,
                                    isRefreshing = refreshing,
                                    modifier = Modifier.align(Alignment.TopCenter)
                                )
                            },
                            modifier = Modifier.fillMaxSize()
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
                        if (refreshFailed && !failureDismissed) {
                            RefreshFailure(onRetry, enabled = !refreshing, message = refreshFailureMessage, maxHeight = failureMaxHeight, onDismiss = { failureDismissed = true }, modifier = Modifier.align(Alignment.TopCenter))
                        } else if (!refreshFailed && updatesAvailable) {
                            ErrorNotice(
                                message = stringResource(R.string.list_updates_available),
                                onRetry = onApplyUpdates,
                                actionLabel = stringResource(R.string.list_action_update),
                                modifier = Modifier.align(Alignment.TopCenter).floatingNotice(failureMaxHeight).testTag("list:updates"),
                                tonal = true,
                                leadingIcon = ThemeR.drawable.ic_refresh_24dp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RefreshFailure(onRetry: () -> Unit, enabled: Boolean, message: String?, maxHeight: androidx.compose.ui.unit.Dp, onDismiss: () -> Unit, modifier: Modifier) {
    ErrorNotice(
        message = message ?: stringResource(R.string.list_refresh_failed),
        onRetry = onRetry,
        modifier = modifier.floatingNotice(maxHeight).testTag("list:refresh-failure"),
        actionLabel = stringResource(R.string.list_action_retry),
        enabled = enabled,
        onDismiss = onDismiss,
        tonal = true
    )
}

@Composable
private fun Modifier.floatingNotice(maxHeight: androidx.compose.ui.unit.Dp): Modifier = widthIn(max = TeamCityDimensions.screenContentMaxWidth).fillMaxWidth()
    .padding(horizontal = TeamCityDimensions.contentPadding, vertical = TeamCityDimensions.smallSpacing).heightIn(max = maxHeight).shadow(6.dp, MaterialTheme.shapes.medium)

@Preview
@Composable
private fun ListContainerPreview() {
    TeamCityTheme {
        TeamCityListContainer(ListUiState.Empty(), {}, {}, Modifier.fillMaxSize(), empty = { TeamCityListEmpty("No items") }) { }
    }
}
