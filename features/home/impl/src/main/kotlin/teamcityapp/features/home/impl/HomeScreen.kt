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

package teamcityapp.features.home.impl

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import teamcityapp.features.home.api.HomeTab
import teamcityapp.libraries.resources.R as SharedR
import teamcityapp.libraries.theme.*
import teamcityapp.libraries.theme.TeamCityTheme

@Composable
fun HomeScreen(
    state: HomeUiState,
    expanded: Boolean,
    onTabSelected: (HomeTab) -> Unit,
    onFab: () -> Unit,
    onRetry: () -> Unit,
    onDismissPrompt: () -> Unit,
    snackbar: SnackbarHostState,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val labels = mapOf(
        HomeTab.Projects to R.string.home_projects,
        HomeTab.Favorites to R.string.home_favorites,
        HomeTab.Running to R.string.home_running,
        HomeTab.Queue to R.string.home_queue,
        HomeTab.Agents to R.string.home_agents
    )
    val icons = mapOf(
        HomeTab.Projects to R.drawable.ic_home_white_24dp,
        HomeTab.Favorites to R.drawable.ic_favorite_white_24dp,
        HomeTab.Running to R.drawable.ic_directions_run_white_24px,
        HomeTab.Queue to R.drawable.ic_layers_white_24dp,
        HomeTab.Agents to SharedR.drawable.ic_directions_railway_black_24dp
    )

    @Composable fun TabIcon(tab: HomeTab) {
        BadgedBox(badge = {
            when (val badge = state.badges[tab]) {
                is HomeBadge.Count -> if (badge.value > 0) Badge { Text(badge.value.toString(), Modifier.testTag("home:count:${tab.name}")) }

                is HomeBadge.Failed -> {
                    val description = stringResource(badge.message)
                    Badge(Modifier.semantics { contentDescription = description }) { Text("!", modifier = Modifier.testTag("home:count-failed:${tab.name}")) }
                }

                else -> Unit
            }
        }) { Icon(painterResource(icons.getValue(tab)), contentDescription = null) }
    }
    val anchors = remember { CoachmarkAnchors() }
    val prompt = state.prompt
    val promptBounds = prompt?.let { anchors.bounds(if (it.prompt == teamcityapp.features.home.api.HomePrompt.Drawer) DRAWER_COACHMARK_ANCHOR else "home:fab") }
    Box(modifier.fillMaxSize().coachmarkHost(anchors)) {
        CompositionLocalProvider(LocalCoachmarkAnchors provides anchors) {
            Row(Modifier.fillMaxSize().then(if (promptBounds != null) Modifier.clearAndSetSemantics { } else Modifier)) {
                if (expanded) {
                    NavigationRail {
                        HomeTab.entries.forEach { tab ->
                            NavigationRailItem(
                                selected = state.selected == tab,
                                onClick = { onTabSelected(tab) },
                                icon = { TabIcon(tab) },
                                label = { Text(stringResource(labels.getValue(tab)), maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                modifier = Modifier.testTag("home:tab:${tab.name}")
                            )
                        }
                    }
                }
                Scaffold(
                    modifier = Modifier.weight(1f),
                    contentWindowInsets = WindowInsets(0, 0, 0, 0),
                    bottomBar = {
                        Surface {
                            Column(
                                Modifier.fillMaxWidth().windowInsetsPadding(
                                    WindowInsets.safeDrawing.only(if (expanded) WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom else WindowInsetsSides.Horizontal)
                                )
                            ) {
                                state.retryMessageRes?.let { message ->
                                    ErrorNotice(
                                        message = stringResource(message),
                                        onRetry = onRetry,
                                        actionLabel = stringResource(state.retryLabelRes),
                                        modifier = Modifier.padding(horizontal = TeamCityDimensions.contentPadding, vertical = TeamCityDimensions.smallSpacing).testTag("home:retry-notice")
                                    )
                                }
                                if (!expanded) {
                                    NavigationBar {
                                        HomeTab.entries.forEach { tab ->
                                            NavigationBarItem(
                                                selected = state.selected == tab,
                                                onClick = { onTabSelected(tab) },
                                                icon = { TabIcon(tab) },
                                                label = { Text(stringResource(labels.getValue(tab)), maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                                modifier = Modifier.testTag("home:tab:${tab.name}")
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    },
                    floatingActionButton = {
                        if (state.selected != HomeTab.Projects) {
                            FloatingActionButton(onClick = onFab, modifier = Modifier.testTag("home:fab").coachmarkAnchor("home:fab", anchors)) {
                                Icon(
                                    painterResource(if (state.selected == HomeTab.Favorites) R.drawable.ic_favorite_white_24dp else R.drawable.ic_filter_list_white_24px),
                                    contentDescription = stringResource(state.fabLabelRes)
                                )
                            }
                        }
                    },
                    snackbarHost = { SnackbarHost(snackbar) }
                ) { padding ->
                    Box(Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding)) { content() }
                }
            }
        }
        if (prompt != null && promptBounds != null) {
            Coachmark(
                title = stringResource(prompt.title),
                description = stringResource(prompt.description),
                dismissLabel = stringResource(R.string.home_continue),
                anchor = promptBounds,
                onDismiss = onDismissPrompt,
                modifier = Modifier.testTag("home:coachmark:${prompt.prompt}"),
                dismissModifier = Modifier.testTag("home:dismiss-prompt"),
                placement = if (prompt.prompt == teamcityapp.features.home.api.HomePrompt.Drawer) CoachmarkPlacement.Below else CoachmarkPlacement.Above
            )
        }
    }
}

@Preview @Composable
private fun HomePreview() {
    TeamCityTheme { HomeScreen(HomeUiState(), false, {}, {}, {}, {}, remember { SnackbarHostState() }) { Text("Projects", Modifier.padding(TeamCityDimensions.contentPadding)) } }
}
