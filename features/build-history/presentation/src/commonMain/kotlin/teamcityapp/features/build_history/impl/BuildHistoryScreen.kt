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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import teamcityapp.features.build_history.api.BuildHistoryPrompt
import teamcityapp.features.build_history.presentation.resources.*
import teamcityapp.libraries.build_ui.TeamCityBuildRow
import teamcityapp.libraries.build_ui.buildRowKeys
import teamcityapp.libraries.builds.BuildLaunchData
import teamcityapp.libraries.design_system.resolve
import teamcityapp.libraries.list_state.ListUiState
import teamcityapp.libraries.list_ui.*
import teamcityapp.libraries.theme.BindTeamCityAppBarToList
import teamcityapp.libraries.theme.TeamCityAppBarSurface
import teamcityapp.libraries.theme.TeamCityDimensions
import teamcityapp.libraries.theme.TeamCityRetrySnackbar
import teamcityapp.libraries.theme.TeamCitySnackbarHost
import teamcityapp.libraries.theme.TeamCityTheme
import teamcityapp.libraries.theme.TeamCityTooltip
import teamcityapp.libraries.theme.teamCityTopAppBarColors

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SharedBuildHistoryScreen(
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
    listState: LazyListState = rememberLazyListState(),
    showActions: Boolean = true,
    coachmark: @Composable (OnboardingState.Available, Rect, () -> Unit) -> Unit = { _, _, _ -> },
    showRunBuild: Boolean = true,
    showFilters: Boolean = true
) {
    val retryLabel = stringResource(Res.string.history_retry)
    TeamCityRetrySnackbar(controls.favoriteFailureMessageRes?.let { stringResource(it) }, retryLabel, snackbar, onRetry = if (controls.favorite == FavoriteState.Unavailable) onFavoriteRetry else onFavorite)
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
                                TeamCityTooltip(stringResource(Res.string.history_back)) {
                                    IconButton(onClick = onBack, modifier = Modifier.testTag("history:back")) {
                                        Icon(painterResource(Res.drawable.ic_history_back), stringResource(Res.string.history_back))
                                    }
                                }
                            }
                        )
                        Text(title, Modifier.fillMaxWidth().padding(start = TeamCityDimensions.contentPadding, end = TeamCityDimensions.contentPadding, bottom = TeamCityDimensions.contentPadding), style = MaterialTheme.typography.headlineSmall)
                    }
                }
            },
            floatingActionButtonPosition = FabPosition.End,
            floatingActionButton = {
                if (showActions) {
                    BuildHistoryActions(
                        controls = controls,
                        showRunBuild = showRunBuild && state != ListUiState.Error,
                        showFilters = showFilters,
                        onRunBuild = onRunBuild,
                        onFilters = onFilters,
                        onFavorite = onFavorite,
                        onFavoriteRetry = onFavoriteRetry,
                        onAnchor = { target, bounds -> anchors[target] = bounds },
                        modifier = Modifier.widthIn(max = (maxWidth - TeamCityDimensions.extraLargeSpacing).coerceAtLeast(TeamCityDimensions.minimumTouchTarget))
                            .onSizeChanged { actionHeight = with(density) { it.height.toDp() } }
                    )
                }
            },
            snackbarHost = { TeamCitySnackbarHost(snackbar) }
        ) { padding ->
            val actionClearance = if (showActions) actionHeight + TeamCityDimensions.sectionSpacing else TeamCityDimensions.noSpacing
            // A retry footer needs an unobstructed hit target even when only partly scrolled
            // into view. Ordinary history content can scroll behind the floating controls.
            val retryClearance = if (appendState == HistoryAppendState.Error) actionClearance else TeamCityDimensions.noSpacing
            BoxWithConstraints(Modifier.fillMaxSize().padding(padding).padding(bottom = retryClearance).testTag("history:body")) {
                Column(Modifier.fillMaxSize()) {
                    TeamCityListContainer(
                        state,
                        onRefresh,
                        onRetry,
                        Modifier.weight(1f).fillMaxWidth().testTag("history:main-content"),
                        empty = { TeamCityListEmpty(stringResource(Res.string.history_empty)) }
                    ) {
                        LazyColumn(
                            Modifier.fillMaxSize().testTag("history:list"),
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
        if (prompt != null && promptBounds != null) coachmark(prompt, promptBounds, onPromptDismiss)
    }
}

@Composable
private fun HistorySection(build: HistoryBuildRow) {
    val title = build.sectionTitle.resolve()
    TeamCityListSectionHeader(title, Modifier.testTag("history:section:${build.sectionKey}"))
}

@Preview
@Composable
private fun BuildHistoryPreview() {
    val rows = listOf(BuildLaunchData("42", "/app/rest/builds/id:42", number = "42", state = "finished", status = "SUCCESS", startDate = "20261010T102030+0700", branchName = "main"))
    TeamCityTheme {
        SharedBuildHistoryScreen(
            "Build TeamCityApp", ListUiState.Content(rows), BuildHistoryControls(FavoriteState.Available(false), OnboardingState.Available()), rows.size, { HistoryBuildRow(rows[it]) }, { HistoryBuildRow(rows[it]) }, HistoryAppendState.Idle,
            {}, {}, {}, {}, {}, {}, {}, {}, {}, {}
        )
    }
}
