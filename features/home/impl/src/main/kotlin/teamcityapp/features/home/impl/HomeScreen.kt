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
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import teamcityapp.features.home.api.HomePrompt
import teamcityapp.features.home.api.HomeTab
import teamcityapp.libraries.theme.*

private val activityTabs = listOf(HomeTab.Running, HomeTab.Queue, HomeTab.Agents)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
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
    onAccount: () -> Unit = {},
    onActivity: () -> Unit = { onTabSelected(HomeTab.Running) },
    showHomeHeader: Boolean = true,
    showNavigation: Boolean = true,
    content: @Composable () -> Unit
) {
    val retryMessage = state.retryMessageRes?.let { stringResource(it) }
    val retryLabel = stringResource(state.retryLabelRes)
    val retryAction by rememberUpdatedState(onRetry)
    LaunchedEffect(retryMessage, state.query?.accountKey, state.retryAttempt, showNavigation) {
        if (showNavigation && retryMessage != null && snackbar.showSnackbar(retryMessage, retryLabel, withDismissAction = true, duration = SnackbarDuration.Long) == SnackbarResult.ActionPerformed) retryAction()
    }
    val anchors = remember { CoachmarkAnchors() }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val prompt = state.prompt.takeIf { showHomeHeader }
    val promptBounds = prompt?.let { anchors.bounds(if (it.prompt == HomePrompt.Drawer) DRAWER_COACHMARK_ANCHOR else "home:action") }
    val destinations = listOf(
        Triple("Projects", R.string.home_projects, R.drawable.ic_home_white_24dp),
        Triple("Favorites", R.string.home_favorites, R.drawable.ic_favorite_white_24dp),
        Triple("Activity", R.string.home_activity, R.drawable.ic_activity)
    )
    fun selected(name: String) = when (name) {
        "Projects" -> state.selected == HomeTab.Projects
        "Favorites" -> state.selected == HomeTab.Favorites
        else -> state.isActivity
    }
    fun select(name: String) = when (name) {
        "Projects" -> onTabSelected(HomeTab.Projects)
        "Favorites" -> onTabSelected(HomeTab.Favorites)
        else -> onActivity()
    }

    Box(modifier.fillMaxSize().coachmarkHost(anchors)) {
        CompositionLocalProvider(LocalCoachmarkAnchors provides anchors) {
            Row(Modifier.fillMaxSize().then(if (promptBounds != null) Modifier.clearAndSetSemantics { } else Modifier)) {
                if (expanded && showNavigation) {
                    NavigationRail {
                        destinations.forEach { (name, label, icon) ->
                            NavigationRailItem(selected(name), { select(name) }, { Icon(painterResource(icon), contentDescription = null) }, label = { Text(stringResource(label)) }, modifier = Modifier.testTag("home:tab:$name"))
                        }
                    }
                }
                Scaffold(
                    modifier = Modifier.weight(1f).nestedScroll(scrollBehavior.nestedScrollConnection),
                    contentWindowInsets = WindowInsets(0, 0, 0, 0),
                    topBar = {
                        if (showHomeHeader) {
                            TeamCityAppBarSurface(scrollBehavior) {
                                TopAppBar(
                                    modifier = Modifier.testTag("screen:app-bar"),
                                    colors = teamCityTopAppBarColors(),
                                    scrollBehavior = scrollBehavior,
                                    title = {
                                        Column {
                                            Text(stringResource(state.titleRes), style = MaterialTheme.typography.headlineMedium, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.testTag("home:title"))
                                        }
                                    },
                                    actions = {
                                        val description = state.query?.account?.userName?.let { stringResource(R.string.home_open_named_account, it) } ?: stringResource(R.string.home_open_account)
                                        FilledTonalIconButton(onAccount, Modifier.padding(end = TeamCityDimensions.smallSpacing).testTag("home:account").coachmarkAnchor(DRAWER_COACHMARK_ANCHOR, anchors).semantics { contentDescription = description }) {
                                            val initials = state.accountInitials
                                            if (initials == null) {
                                                Icon(painterResource(R.drawable.ic_account), null)
                                            } else {
                                                Text(initials, style = MaterialTheme.typography.labelLarge)
                                            }
                                        }
                                    }
                                )
                                if (state.isActivity) {
                                    val largeText = LocalDensity.current.fontScale > 1.3f
                                    Row(Modifier.fillMaxWidth().padding(horizontal = TeamCityDimensions.contentPadding).selectableGroup().testTag("home:activity-views"), horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)) {
                                        activityTabs.forEachIndexed { index, tab ->
                                            ToggleButton(
                                                checked = state.selected == tab,
                                                onCheckedChange = { onTabSelected(tab) },
                                                shapes = when (index) {
                                                    0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                                                    activityTabs.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                                                    else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                                                },
                                                contentPadding = PaddingValues(horizontal = TeamCityDimensions.smallSpacing, vertical = TeamCityDimensions.smallSpacing),
                                                modifier = Modifier.weight(1f).testTag("home:tab:${tab.name}").semantics { selected = state.selected == tab }
                                            ) {
                                                if (largeText) {
                                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                        Text(stringResource(tab.labelRes()), maxLines = 1)
                                                        HomeCountBadge(state.badges[tab], tab.name)
                                                    }
                                                } else {
                                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                                        Text(stringResource(tab.labelRes()), maxLines = 1)
                                                        HomeCountBadge(state.badges[tab], tab.name)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                if (state.isActivity) {
                                    Row(Modifier.fillMaxWidth().padding(horizontal = TeamCityDimensions.contentPadding, vertical = TeamCityDimensions.smallSpacing), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(TeamCityDimensions.smallSpacing)) {
                                        state.filterSummaryRes?.let { Text(stringResource(it), Modifier.weight(1f).testTag("home:filter-summary"), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                                        FilledTonalButton(onFab, Modifier.testTag("home:action").coachmarkAnchor("home:action", anchors)) {
                                            Icon(painterResource(if (state.selected == HomeTab.Favorites) R.drawable.ic_favorite_white_24dp else R.drawable.ic_filter_list_white_24px), null, Modifier.size(18.dp))
                                            Spacer(Modifier.width(TeamCityDimensions.smallSpacing))
                                            Text(stringResource(state.fabLabelRes))
                                        }
                                    }
                                }
                            }
                        }
                    },
                    bottomBar = {
                        if (showNavigation) {
                            Surface {
                                Column(Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.safeDrawing.only(if (expanded) WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom else WindowInsetsSides.Horizontal))) {
                                    if (!expanded) {
                                        NavigationBar {
                                            destinations.forEach { (name, label, icon) ->
                                                val destinationLabel = stringResource(label)
                                                NavigationBarItem(selected(name), { select(name) }, { Icon(painterResource(icon), contentDescription = null) }, modifier = Modifier.testTag("home:tab:$name").semantics { contentDescription = destinationLabel })
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    },
                    snackbarHost = {
                        if (showNavigation) {
                            val largeText = LocalDensity.current.fontScale >= 1.3f
                            SnackbarHost(snackbar) { Snackbar(it, modifier = Modifier.testTag("home:snackbar"), actionOnNewLine = largeText) }
                        }
                    }
                ) { padding ->
                    CompositionLocalProvider(LocalTeamCityHeaderProvided provides showHomeHeader, LocalTeamCityAppBarState provides scrollBehavior.state.takeIf { showHomeHeader }) {
                        Box(Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding)) { content() }
                    }
                }
            }
        }
        if (prompt != null && promptBounds != null) Coachmark(title = stringResource(prompt.title), description = stringResource(prompt.description), dismissLabel = stringResource(R.string.home_continue), anchor = promptBounds, onDismiss = onDismissPrompt, modifier = Modifier.testTag("home:coachmark:${prompt.prompt}"), dismissModifier = Modifier.testTag("home:dismiss-prompt"), placement = CoachmarkPlacement.Below)
    }
}

@Composable
private fun HomeCountBadge(badge: HomeBadge?, tag: String) {
    when (badge) {
        is HomeBadge.Count -> if (badge.value > 0) Badge(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer) { Text(badge.value.toString(), Modifier.testTag("home:count:$tag")) }

        is HomeBadge.Failed -> {
            val description = stringResource(badge.message)
            Badge(Modifier.semantics { contentDescription = description }, containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer) { Text("!", Modifier.testTag("home:count-failed:$tag")) }
        }

        else -> Unit
    }
}

private fun HomeTab.labelRes() = when (this) {
    HomeTab.Running -> R.string.home_running
    HomeTab.Queue -> R.string.home_queue
    HomeTab.Agents -> R.string.home_agents
    HomeTab.Projects -> R.string.home_projects
    HomeTab.Favorites -> R.string.home_favorites
}

@Preview @Composable
private fun HomePreview() {
    TeamCityTheme { HomeScreen(HomeUiState(), false, {}, {}, {}, {}, remember { SnackbarHostState() }) { Text("Projects", Modifier.padding(TeamCityDimensions.contentPadding)) } }
}
