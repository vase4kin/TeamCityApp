/*
 * Copyright 2019 Andrey Tolpeev
 * Copyright 2020 Andrey Tolpeev
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

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import teamcityapp.features.build_history.api.BuildHistoryPrompt
import teamcityapp.libraries.build_ui.TeamCityBuildRow
import teamcityapp.libraries.build_ui.buildRowKeys
import teamcityapp.libraries.builds.BuildLaunchData
import teamcityapp.libraries.list_state.ListUiState
import teamcityapp.libraries.list_ui.*
import teamcityapp.libraries.theme.TeamCityFloatingActionButton
import teamcityapp.libraries.theme.TeamCityTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BuildHistoryScreen(
    title: String,
    state: ListUiState<BuildLaunchData>,
    controls: BuildHistoryControls,
    itemCount: Int,
    itemAt: (Int) -> BuildLaunchData?,
    itemPeek: (Int) -> BuildLaunchData?,
    appendState: HistoryAppendState,
    onBack: () -> Unit,
    onRunBuild: () -> Unit,
    onFilters: () -> Unit,
    onFavorite: () -> Unit,
    onFavoriteRetry: () -> Unit,
    onOnboardingRetry: () -> Unit,
    onPromptDismiss: () -> Unit,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    onAppendRetry: () -> Unit,
    onBuild: (BuildLaunchData) -> Unit,
    onQueuedRetry: () -> Unit,
    onQueuedDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    snackbar: SnackbarHostState = remember { SnackbarHostState() },
    listState: LazyListState = rememberLazyListState()
) {
    val itemKeys = remember(state, itemCount, itemPeek) {
        val snapshots = (0 until itemCount).map(itemPeek)
        val buildKeys = buildRowKeys(snapshots.filterNotNull()).iterator()
        snapshots.mapIndexed { index, build -> if (build == null) "history:placeholder:$index" else buildKeys.next() }
    }
    val anchors = remember { mutableStateMapOf<BuildHistoryPrompt, Rect>() }
    val prompt = (controls.onboarding as? OnboardingState.Available)?.takeIf {
        it.prompt != BuildHistoryPrompt.Run || state != ListUiState.Error
    }
    val promptBounds = prompt?.prompt?.let(anchors::get)
    Box(modifier.fillMaxSize()) {
        Scaffold(
            modifier = if (promptBounds != null) Modifier.clearAndSetSemantics { } else Modifier,
            topBar = {
                TopAppBar(
                    title = { Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    navigationIcon = {
                        IconButton(onClick = onBack, modifier = Modifier.testTag("history:back")) {
                            Icon(painterResource(R.drawable.ic_history_back), stringResource(R.string.history_back))
                        }
                    },
                    actions = {
                        IconButton(onClick = onFilters, modifier = Modifier.testTag("history:filter").onGloballyPositioned { anchors[BuildHistoryPrompt.Filter] = it.boundsInRoot() }) {
                            Icon(painterResource(R.drawable.ic_history_filter), stringResource(R.string.history_filter))
                        }
                        val favorite = controls.favorite
                        IconButton(
                            onClick = if (favorite == FavoriteState.Unavailable) onFavoriteRetry else onFavorite,
                            enabled = favorite != FavoriteState.Loading && (favorite !is FavoriteState.Available || !favorite.updating),
                            modifier = Modifier.testTag("history:favorite").onGloballyPositioned { anchors[BuildHistoryPrompt.Favorite] = it.boundsInRoot() }
                        ) {
                            if (favorite == FavoriteState.Loading || (favorite is FavoriteState.Available && favorite.updating)) {
                                CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(
                                    painterResource(if (favorite is FavoriteState.Available && favorite.favorite) R.drawable.ic_history_favorite else R.drawable.ic_history_favorite_border),
                                    stringResource(if (favorite is FavoriteState.Available && favorite.favorite) R.string.history_remove_favorite else R.string.history_add_favorite)
                                )
                            }
                        }
                    }
                )
            },
            floatingActionButton = {
                if (state != ListUiState.Error) {
                    TeamCityFloatingActionButton(
                        onRunBuild,
                        Modifier.testTag("history:run").onGloballyPositioned { anchors[BuildHistoryPrompt.Run] = it.boundsInRoot() }
                    ) { Icon(painterResource(R.drawable.ic_history_run), stringResource(R.string.history_run)) }
                }
            },
            snackbarHost = {
                BoxWithConstraints(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    val actionOnNewLine = maxWidth < 480.dp && LocalDensity.current.fontScale > 1f
                    if (controls.queuedBuild == QueuedBuildState.Failed) {
                        Snackbar(
                            Modifier.padding(12.dp).testTag("history:queued-error"),
                            actionOnNewLine = actionOnNewLine,
                            action = { TextButton(onClick = onQueuedRetry, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.inversePrimary)) { Text(stringResource(R.string.history_retry)) } },
                            dismissAction = { TextButton(onClick = onQueuedDismiss, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.inversePrimary)) { Text(stringResource(R.string.history_cancel)) } }
                        ) { Text(stringResource(R.string.history_open_failed)) }
                    } else {
                        SnackbarHost(snackbar) { data -> Snackbar(data, actionOnNewLine = actionOnNewLine) }
                    }
                }
            }
        ) { padding ->
            Column(Modifier.fillMaxSize().padding(padding)) {
                when (val favorite = controls.favorite) {
                    FavoriteState.Unavailable -> HistoryOptionalFailure(stringResource(R.string.history_favorite_unavailable), onFavoriteRetry)
                    is FavoriteState.Available -> if (favorite.updateFailed) HistoryOptionalFailure(stringResource(R.string.history_favorite_update_failed), onFavorite)
                    FavoriteState.Loading -> Unit
                }
                if (controls.onboarding == OnboardingState.Unavailable) HistoryOptionalFailure(stringResource(R.string.history_onboarding_unavailable), onOnboardingRetry)
                TeamCityListContainer(
                    state,
                    onRefresh,
                    onRetry,
                    Modifier.weight(1f).fillMaxWidth(),
                    empty = { TeamCityListEmpty(stringResource(R.string.history_empty)) }
                ) {
                    LazyColumn(Modifier.fillMaxSize().testTag("history:list"), state = listState, contentPadding = PaddingValues(top = 8.dp, bottom = 88.dp)) {
                        items(itemCount, key = { itemKeys[it] }, contentType = { "build" }) { index ->
                            val build = itemAt(index)
                            if (build == null) {
                                TeamCityListLoadingRow()
                            } else {
                                val previous = if (index > 0) itemPeek(index - 1) else null
                                if (previous == null || historySectionKey(previous) != historySectionKey(build)) HistorySection(build)
                                TeamCityBuildRow(build, { onBuild(build) }, Modifier.testTag("history:build:${build.id}"))
                            }
                        }
                        when (appendState) {
                            HistoryAppendState.Loading -> item(key = "history:append-loading") { TeamCityListAppendLoading() }
                            HistoryAppendState.Error -> item(key = "history:append-error") { TeamCityListAppendRetry(onAppendRetry) }
                            HistoryAppendState.Idle -> Unit
                        }
                    }
                }
            }
        }
        if (promptBounds != null && prompt != null) HistoryCoachmark(prompt, promptBounds, onPromptDismiss)
    }
    when (controls.queuedBuild) {
        QueuedBuildState.Loading -> AlertDialog(
            onDismissRequest = {},
            title = { Text(stringResource(R.string.history_opening)) },
            text = { CircularProgressIndicator(Modifier.size(32.dp).testTag("history:opening-progress")) },
            confirmButton = {}
        )

        else -> Unit
    }
}

@Composable
private fun HistorySection(build: BuildLaunchData) {
    val title = if (build.isQueued) stringResource(R.string.history_queued_section) else historyDate(build) ?: stringResource(R.string.history_unknown_date)
    Text(
        title,
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp).testTag("history:section:${historySectionKey(build)}"),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun HistoryOptionalFailure(message: String, onRetry: () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.errorContainer) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
            Text(message, style = MaterialTheme.typography.bodyMedium)
            TextButton(onClick = onRetry) { Text(stringResource(R.string.history_retry)) }
        }
    }
}

@Preview
@Composable
private fun BuildHistoryPreview() {
    val rows = listOf(BuildLaunchData("42", "/app/rest/builds/id:42", number = "42", state = "finished", status = "SUCCESS", startDate = "20261010T102030+0700", branchName = "main"))
    TeamCityTheme {
        BuildHistoryScreen(
            "Build TeamCityApp", ListUiState.Content(rows), BuildHistoryControls(FavoriteState.Available(false), OnboardingState.Available()), rows.size, { rows[it] }, { rows[it] }, HistoryAppendState.Idle,
            {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}
        )
    }
}
