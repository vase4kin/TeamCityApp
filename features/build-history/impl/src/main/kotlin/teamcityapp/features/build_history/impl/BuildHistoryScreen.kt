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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.FloatingToolbarDefaults.floatingToolbarVerticalNestedScroll
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.tooling.preview.Preview
import teamcityapp.features.build_history.api.BuildHistoryPrompt
import teamcityapp.libraries.build_ui.TeamCityBuildRow
import teamcityapp.libraries.build_ui.buildRowKeys
import teamcityapp.libraries.builds.BuildLaunchData
import teamcityapp.libraries.list_state.ListUiState
import teamcityapp.libraries.list_ui.*
import teamcityapp.libraries.theme.BindTeamCityAppBarToList
import teamcityapp.libraries.theme.ErrorNotice
import teamcityapp.libraries.theme.TeamCityAppBarSurface
import teamcityapp.libraries.theme.TeamCityDimensions
import teamcityapp.libraries.theme.TeamCityLoadingIndicator
import teamcityapp.libraries.theme.TeamCityTheme
import teamcityapp.libraries.theme.resolve
import teamcityapp.libraries.theme.teamCityTopAppBarColors

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
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
    onOnboardingRetry: () -> Unit,
    onPromptDismiss: () -> Unit,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    onAppendRetry: () -> Unit,
    onBuild: (BuildLaunchData) -> Unit,
    modifier: Modifier = Modifier,
    snackbar: SnackbarHostState = remember { SnackbarHostState() },
    listState: LazyListState = rememberLazyListState()
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    BindTeamCityAppBarToList(listState, scrollBehavior.state)
    val itemKeys = remember(state, itemCount, itemPeek) {
        val snapshots = (0 until itemCount).map(itemPeek)
        val buildKeys = buildRowKeys(snapshots.filterNotNull().map { it.build }).iterator()
        snapshots.mapIndexed { index, build -> if (build == null) "history:placeholder:$index" else buildKeys.next() }
    }
    val anchors = remember { mutableStateMapOf<BuildHistoryPrompt, Rect>() }
    val prompt = (controls.onboarding as? OnboardingState.Available)?.takeIf {
        it.prompt != null && (it.prompt != BuildHistoryPrompt.Run || state != ListUiState.Error)
    }
    val promptBounds = prompt?.prompt?.let(anchors::get)
    var toolbarExpanded by rememberSaveable { mutableStateOf(true) }
    val touchExplorationEnabled = rememberHistoryTouchExplorationEnabled()
    val atTop by remember(listState) { derivedStateOf { !listState.canScrollBackward } }
    val actionsExpanded = toolbarExpanded || atTop || prompt != null || touchExplorationEnabled || state !is ListUiState.Content
    var actionHeight by remember { mutableStateOf(FloatingToolbarDefaults.ContainerSize) }
    val density = LocalDensity.current
    BoxWithConstraints(modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection).then(if (promptBounds != null) Modifier.clearAndSetSemantics { } else Modifier),
            topBar = {
                TeamCityAppBarSurface(scrollBehavior, Modifier.testTag("screen:app-bar")) {
                    Column(Modifier.windowInsetsPadding(TopAppBarDefaults.windowInsets)) {
                        TopAppBar(
                            colors = teamCityTopAppBarColors().copy(containerColor = androidx.compose.ui.graphics.Color.Transparent, scrolledContainerColor = androidx.compose.ui.graphics.Color.Transparent),
                            scrollBehavior = scrollBehavior,
                            title = {},
                            windowInsets = WindowInsets(0, 0, 0, 0),
                            navigationIcon = {
                                IconButton(onClick = onBack, modifier = Modifier.testTag("history:back")) {
                                    Icon(painterResource(R.drawable.ic_history_back), stringResource(R.string.history_back))
                                }
                            }
                        )
                        Text(title, Modifier.fillMaxWidth().padding(start = TeamCityDimensions.contentPadding, end = TeamCityDimensions.contentPadding, bottom = TeamCityDimensions.contentPadding), style = MaterialTheme.typography.headlineSmall)
                    }
                }
            },
            floatingActionButton = {
                BuildHistoryActions(
                    controls = controls,
                    expanded = actionsExpanded,
                    showRunBuild = state != ListUiState.Error,
                    onRunBuild = onRunBuild,
                    onFilters = onFilters,
                    onFavorite = onFavorite,
                    onFavoriteRetry = onFavoriteRetry,
                    onAnchor = { target, bounds -> anchors[target] = bounds },
                    modifier = Modifier.widthIn(max = (maxWidth - TeamCityDimensions.extraLargeSpacing).coerceAtLeast(TeamCityDimensions.minimumTouchTarget))
                        .onSizeChanged { actionHeight = with(density) { it.height.toDp() } }
                )
            },
            snackbarHost = {
                BoxWithConstraints(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    val actionOnNewLine = maxWidth < TeamCityDimensions.historyInlineActionMinWidth && LocalDensity.current.fontScale > 1f
                    SnackbarHost(snackbar) { data -> Snackbar(data, actionOnNewLine = actionOnNewLine) }
                }
            }
        ) { padding ->
            val actionClearance = actionHeight + TeamCityDimensions.sectionSpacing
            // A retry footer needs an unobstructed hit target even when only partly scrolled
            // into view. Ordinary history content can scroll behind the floating controls.
            val retryClearance = if (appendState == HistoryAppendState.Error) actionClearance else TeamCityDimensions.noSpacing
            BoxWithConstraints(Modifier.fillMaxSize().padding(padding).padding(bottom = retryClearance).testTag("history:body")) {
                val noticeMaxHeight = maxHeight / 3
                Column(Modifier.fillMaxSize()) {
                    Column(
                        Modifier.fillMaxWidth().heightIn(max = noticeMaxHeight).verticalScroll(rememberScrollState()).testTag("history:optional-errors")
                    ) {
                        when (val favorite = controls.favorite) {
                            FavoriteState.Unavailable -> HistoryOptionalFailure(stringResource(requireNotNull(controls.favoriteFailureMessageRes)), onFavoriteRetry)
                            is FavoriteState.Available -> if (favorite.updateFailed) HistoryOptionalFailure(stringResource(requireNotNull(controls.favoriteFailureMessageRes)), onFavorite)
                            FavoriteState.Loading -> Unit
                        }
                        if (controls.onboarding == OnboardingState.Unavailable) HistoryOptionalFailure(stringResource(R.string.history_onboarding_unavailable), onOnboardingRetry)
                    }
                    TeamCityListContainer(
                        state,
                        onRefresh,
                        onRetry,
                        Modifier.weight(1f).fillMaxWidth().testTag("history:main-content"),
                        empty = { TeamCityListEmpty(stringResource(R.string.history_empty)) }
                    ) {
                        LazyColumn(
                            Modifier.fillMaxSize().testTag("history:list").then(
                                if (prompt == null && !touchExplorationEnabled) {
                                    Modifier.floatingToolbarVerticalNestedScroll(
                                        expanded = toolbarExpanded,
                                        onExpand = { toolbarExpanded = true },
                                        onCollapse = { toolbarExpanded = false },
                                        expandScrollDistanceThreshold = TeamCityDimensions.sectionSpacing,
                                        collapseScrollDistanceThreshold = TeamCityDimensions.sectionSpacing
                                    )
                                } else {
                                    Modifier
                                }
                            ),
                            state = listState,
                            contentPadding = PaddingValues(top = TeamCityDimensions.smallSpacing, bottom = actionClearance - retryClearance + TeamCityDimensions.contentPadding)
                        ) {
                            items(itemCount, key = { itemKeys[it] }, contentType = { "build" }) { index ->
                                val build = itemAt(index)
                                if (build == null) {
                                    TeamCityListLoadingRow()
                                } else {
                                    val previous = if (index > 0) itemPeek(index - 1) else null
                                    if (previous == null || previous.sectionKey != build.sectionKey) HistorySection(build)
                                    val next = if (index + 1 < itemCount) itemPeek(index + 1) else null
                                    TeamCityBuildRow(
                                        build.row,
                                        { onBuild(build.build) },
                                        Modifier.testTag("history:build:${build.build.id}"),
                                        position = listRowPosition(
                                            hasPrevious = previous != null && previous.sectionKey == build.sectionKey,
                                            hasNext = next != null && next.sectionKey == build.sectionKey
                                        )
                                    )
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
        }
        if (prompt != null && promptBounds != null) HistoryCoachmark(prompt, promptBounds, onPromptDismiss)
    }
}

@Composable
private fun HistorySection(build: HistoryBuildRow) {
    val title = build.sectionTitle.resolve()
    TeamCityListSectionHeader(title, Modifier.testTag("history:section:${build.sectionKey}"))
}

@Composable
private fun HistoryOptionalFailure(message: String, onRetry: () -> Unit) {
    ErrorNotice(message, onRetry, Modifier.fillMaxWidth().padding(horizontal = TeamCityDimensions.contentPadding, vertical = TeamCityDimensions.smallSpacing), actionLabel = stringResource(R.string.history_retry))
}

@Preview
@Composable
private fun BuildHistoryPreview() {
    val rows = listOf(BuildLaunchData("42", "/app/rest/builds/id:42", number = "42", state = "finished", status = "SUCCESS", startDate = "20261010T102030+0700", branchName = "main"))
    TeamCityTheme {
        BuildHistoryScreen(
            "Build TeamCityApp", ListUiState.Content(rows), BuildHistoryControls(FavoriteState.Available(false), OnboardingState.Available()), rows.size, { HistoryBuildRow(rows[it]) }, { HistoryBuildRow(rows[it]) }, HistoryAppendState.Idle,
            {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}
        )
    }
}
