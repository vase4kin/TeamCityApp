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

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.Density
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import teamcityapp.features.filter_bottom_sheet.api.QuickFilter
import teamcityapp.features.home.api.*
import teamcityapp.features.home.impl.resources.*
import teamcityapp.libraries.theme.*

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class HomeScreenTest {
    @get:Rule val compose = createComposeRule()
    private val badges = mapOf(HomeTab.Running to HomeBadge.Count(2), HomeTab.Queue to HomeBadge.Count(3), HomeTab.Agents to HomeBadge.Count(9), HomeTab.Favorites to HomeBadge.Count(1))
    private val query = HomeQuery("account", listOf("configuration"), QuickFilter.RunningFavorites, QuickFilter.QueuedAll, QuickFilter.AgentsConnected, HomeAccount("Andrey Tolpeev", "https://teamcity.example"))

    @Test fun compactNavigationHintsKeepEqualTargetsAndDoNotSelectDestinations() {
        val selected = mutableListOf<HomeTab>()
        compose.setContent { TeamCityTheme { HomeScreen(HomeUiState(), false, selected::add, {}, {}, {}, remember { SnackbarHostState() }) { Text("Content") } } }
        val tabs = listOf("Projects", "Favorites", "Activity")
        val bounds = tabs.map { compose.onNodeWithTag("home:tab:$it").fetchSemanticsNode().boundsInRoot }
        assertEquals(bounds[0].width, bounds[1].width, 1f)
        assertEquals(bounds[1].width, bounds[2].width, 1f)
        tabs.forEach { name ->
            val tab = compose.onNodeWithTag("home:tab:$name")
            tab.assertContentDescriptionEquals(name).assert(SemanticsMatcher.expectValue(androidx.compose.ui.semantics.SemanticsProperties.Role, androidx.compose.ui.semantics.Role.Tab))
            tab.performTouchInput { longClick() }
            compose.onNode(hasText(name) and hasAnyAncestor(isPopup())).assertIsDisplayed()
            assertTrue(selected.isEmpty())
            compose.mainClock.advanceTimeBy(2500)
        }
        compose.onNodeWithTag("home:tab:Projects").assertIsSelected()
        compose.onNodeWithTag("home:tab:Favorites").performClick()
        assertEquals(listOf(HomeTab.Favorites), selected)
    }

    @Test fun expandedNavigationHasOneTabTargetPerDestinationAndShowsHints() {
        val selected = mutableListOf<HomeTab>()
        compose.setContent { TeamCityTheme { HomeScreen(HomeUiState(), true, selected::add, {}, {}, {}, remember { SnackbarHostState() }) { Text("Content") } } }
        compose.onAllNodes(hasTestTag("home:tab:Favorites")).assertCountEquals(1)
        compose.onNodeWithTag("home:tab:Favorites").performTouchInput { longClick() }
        compose.onNode(hasText("Favorites") and hasAnyAncestor(isPopup())).assertIsDisplayed()
        assertTrue(selected.isEmpty())
        compose.mainClock.advanceTimeBy(2500)
        compose.onNodeWithTag("home:tab:Favorites").performClick()
        assertEquals(listOf(HomeTab.Favorites), selected)
    }

    @Test fun projectsExposeThreeDestinationsAndAnAccessibleAccountAvatar() {
        var accounts = 0
        compose.setContent { TeamCityTheme { HomeScreen(HomeUiState(query = query, badges = badges), false, {}, {}, {}, {}, remember { SnackbarHostState() }, onAccount = { accounts++ }) { Text("Project content") } } }
        listOf("Projects", "Favorites", "Activity").forEach { compose.onNodeWithTag("home:tab:$it").assertIsDisplayed() }
        listOf("Running", "Queue", "Agents").forEach { compose.onNodeWithTag("home:tab:$it").assertDoesNotExist() }
        compose.onNodeWithTag("home:count:Activity", useUnmergedTree = true).assertDoesNotExist()
        compose.onNodeWithTag("home:count:Favorites", useUnmergedTree = true).assertDoesNotExist()
        compose.onNodeWithContentDescription("Open account for Andrey Tolpeev").performClick()
        assertEquals(1, accounts)
        compose.onNodeWithText("https://teamcity.example").assertDoesNotExist()
        listOf("Projects", "Favorites", "Activity").forEach { compose.onNodeWithTag("home:tab:$it").assertContentDescriptionEquals(it) }
        compose.onAllNodesWithText("Favorites").assertCountEquals(0)
        compose.onNodeWithTag("home:fab").assertDoesNotExist()
        compose.onNodeWithTag("home:action").assertDoesNotExist()
    }

    @Test fun activitySelectionGroupsAllThreeViewsAndUsesTheirExistingCallbacks() {
        val state = mutableStateOf(HomeUiState(query = query, badges = badges))
        var actions = 0
        compose.setContent { TeamCityTheme { HomeScreen(state.value, false, { state.value = state.value.copy(selected = it) }, { actions++ }, {}, {}, remember { SnackbarHostState() }) { Text("Content") } } }
        compose.onNodeWithTag("home:tab:Activity").performClick()
        compose.onNodeWithTag("home:tab:Activity").assertIsSelected()
        compose.onNodeWithTag("home:tab:Running").assertIsSelected()
        compose.onNodeWithTag("home:filter:RunningFavorites").assertIsDisplayed().assertIsSelected()
        compose.onNodeWithTag("home:filter:RunningAll").assertIsDisplayed()
        compose.onNodeWithTag("home:tab:Queue").performClick()
        compose.onNodeWithTag("home:tab:Queue").assertIsSelected()
        compose.onNodeWithTag("home:filter:QueuedAll").assertIsDisplayed().assertIsSelected()
        compose.onNodeWithTag("home:tab:Agents").performClick()
        compose.onNodeWithTag("home:filter:AgentsConnected").assertIsDisplayed().assertIsSelected()
        compose.onNodeWithTag("home:count:Agents", useUnmergedTree = true).assertTextEquals("9")
        compose.onNodeWithText("Filters").assertDoesNotExist()
        compose.onNodeWithTag("home:filter-summary").assertDoesNotExist()
        compose.onNodeWithTag("home:filter:AgentsDisconnected").performClick()
        assertEquals(1, actions)
    }

    @Test fun chipsApplyOnlyNewSelectionsAndEachTabRetainsItsOwnFilter() {
        val state = mutableStateOf(HomeUiState(selected = HomeTab.Running, query = query, badges = badges))
        val applied = mutableListOf<QuickFilter>()
        compose.setContent {
            TeamCityTheme {
                HomeScreen(state.value, false, { state.value = state.value.copy(selected = it) }, { filter ->
                    applied += filter
                    val current = checkNotNull(state.value.query)
                    state.value = state.value.copy(
                        query = when (filter) {
                            QuickFilter.RunningAll, QuickFilter.RunningFavorites -> current.copy(runningFilter = filter)
                            QuickFilter.QueuedAll, QuickFilter.QueuedFavorites -> current.copy(queueFilter = filter)
                            else -> current.copy(agentsFilter = filter)
                        }
                    )
                }, {}, {}, remember { SnackbarHostState() }) { Text("Content") }
            }
        }
        compose.onNodeWithTag("home:filter:RunningFavorites").performClick()
        assertTrue(applied.isEmpty())
        val runningBounds = listOf("RunningAll", "RunningFavorites").map { compose.onNodeWithTag("home:filter:$it").fetchSemanticsNode().boundsInRoot }
        compose.onNodeWithTag("home:filter:RunningAll").performClick().assertIsSelected()
        assertEquals(runningBounds, listOf("RunningAll", "RunningFavorites").map { compose.onNodeWithTag("home:filter:$it").fetchSemanticsNode().boundsInRoot })
        compose.onNodeWithTag("home:tab:Queue").performClick()
        compose.onNodeWithTag("home:filter:QueuedAll").assertIsSelected()
        val queueBounds = listOf("QueuedAll", "QueuedFavorites").map { compose.onNodeWithTag("home:filter:$it").fetchSemanticsNode().boundsInRoot }
        compose.onNodeWithTag("home:filter:QueuedFavorites").performClick().assertIsSelected()
        assertEquals(queueBounds, listOf("QueuedAll", "QueuedFavorites").map { compose.onNodeWithTag("home:filter:$it").fetchSemanticsNode().boundsInRoot })
        compose.onNodeWithTag("home:tab:Agents").performClick()
        val agentBounds = listOf("AgentsConnected", "AgentsDisconnected").map { compose.onNodeWithTag("home:filter:$it").fetchSemanticsNode().boundsInRoot }
        compose.onNodeWithTag("home:filter:AgentsDisconnected").performClick().assertIsSelected()
        assertEquals(agentBounds, listOf("AgentsConnected", "AgentsDisconnected").map { compose.onNodeWithTag("home:filter:$it").fetchSemanticsNode().boundsInRoot })
        compose.onNodeWithTag("home:tab:Running").performClick()
        compose.onNodeWithTag("home:filter:RunningAll").assertIsSelected()
        compose.onNodeWithTag("home:tab:Queue").performClick()
        compose.onNodeWithTag("home:filter:QueuedFavorites").assertIsSelected()
        assertEquals(listOf(QuickFilter.RunningAll, QuickFilter.QueuedFavorites, QuickFilter.AgentsDisconnected), applied)
    }

    @Test fun chipsAreDisabledUntilTheAccountQueryIsAvailableAndWhileApplying() {
        val state = mutableStateOf(HomeUiState(selected = HomeTab.Running))
        compose.setContent { TeamCityTheme { HomeScreen(state.value, false, {}, {}, {}, {}, remember { SnackbarHostState() }) { Text("Content") } } }
        compose.onNodeWithTag("home:filter:RunningAll").assertIsNotEnabled()
        compose.onNodeWithTag("home:filter:RunningFavorites").assertIsNotEnabled()
        compose.runOnIdle { state.value = state.value.copy(query = query, applyingFilter = QuickFilter.RunningAll) }
        compose.onNodeWithTag("home:filter:RunningAll").assertIsNotEnabled()
        compose.onNodeWithTag("home:filter:RunningFavorites").assertIsNotEnabled().assertIsSelected()
        compose.runOnIdle { state.value = state.value.copy(applyingFilter = null, filterFailed = true) }
        compose.onNodeWithTag("home:filter:RunningAll").assertIsEnabled()
        compose.onNodeWithText("Couldn’t change the filter. Tap a chip to try again.").assertIsDisplayed()
    }

    @Test fun compactCountFailureShowsSnackbarWithoutMovingHeader() = assertFooter(false)

    @Test fun compactAgentsBadgeMatchesTheOtherActivityCounts() = assertAgentsBadges(false, 1f)

    @Test fun expandedAgentsBadgeMatchesTheOtherActivityCounts() = assertAgentsBadges(true, 1f)

    @Test fun enlargedTextShowsAgentsBadgeBelowItsLabel() = assertAgentsBadges(false, 1.5f)

    @Test fun runningCountRefreshKeepsOtherSegmentLabelsAndBoundsStable() = assertStableSegments(1f)

    @Test fun enlargedTextRunningCountRefreshKeepsOtherSegmentsStable() = assertStableSegments(1.5f)

    private fun assertStableSegments(fontScale: Float) {
        val state = mutableStateOf(HomeUiState(selected = HomeTab.Running, query = query, badges = badges))
        compose.setContent {
            val density = LocalDensity.current.density
            CompositionLocalProvider(LocalDensity provides Density(density, fontScale)) {
                TeamCityTheme { HomeScreen(state.value, false, {}, {}, {}, {}, remember { SnackbarHostState() }) { Text("Build content") } }
            }
        }
        val nodes = listOf(compose.onNodeWithTag("home:tab:Queue"), compose.onNodeWithTag("home:tab:Agents"), compose.onNodeWithText("Queue", useUnmergedTree = true), compose.onNodeWithText("Agents", useUnmergedTree = true))
        val bounds = nodes.map { it.fetchSemanticsNode().boundsInRoot }
        compose.runOnIdle { state.value = state.value.copy(query = query.copy(runningFilter = QuickFilter.RunningAll), badges = badges + (HomeTab.Running to HomeBadge.Loading)) }
        assertEquals(bounds, nodes.map { it.fetchSemanticsNode().boundsInRoot })
        compose.onNodeWithTag("home:count:Queue", useUnmergedTree = true).assertTextEquals("3")
        compose.onNodeWithTag("home:count:Agents", useUnmergedTree = true).assertTextEquals("9")
        compose.runOnIdle { state.value = state.value.copy(badges = badges + (HomeTab.Running to HomeBadge.Count(12))) }
        assertEquals(bounds, nodes.map { it.fetchSemanticsNode().boundsInRoot })
    }

    private fun assertAgentsBadges(expanded: Boolean, fontScale: Float) {
        val state = mutableStateOf(HomeUiState(selected = HomeTab.Agents, query = query, badges = badges))
        compose.setContent {
            val density = LocalDensity.current.density
            CompositionLocalProvider(LocalDensity provides Density(density, fontScale)) {
                TeamCityTheme { HomeScreen(state.value, expanded, {}, {}, {}, {}, remember { SnackbarHostState() }) { Text("Agent content") } }
            }
        }
        compose.onNodeWithTag("home:count:Agents", useUnmergedTree = true).assertIsDisplayed().assertTextEquals("9")
        compose.onNodeWithTag("home:count:Activity", useUnmergedTree = true).assertDoesNotExist()
        compose.runOnIdle { state.value = state.value.copy(badges = badges + (HomeTab.Agents to HomeBadge.Count(0))) }
        compose.onNodeWithTag("home:count:Agents", useUnmergedTree = true).assertDoesNotExist()
        compose.onNodeWithTag("home:count:Activity", useUnmergedTree = true).assertDoesNotExist()
        compose.runOnIdle { state.value = state.value.copy(badges = badges + (HomeTab.Agents to HomeBadge.Loading)) }
        compose.onNodeWithTag("home:count:Agents", useUnmergedTree = true).assertDoesNotExist()
        compose.onNodeWithTag("home:count:Activity", useUnmergedTree = true).assertDoesNotExist()
        compose.runOnIdle { state.value = state.value.copy(badges = badges + (HomeTab.Agents to HomeBadge.Failed())) }
        compose.onNodeWithTag("home:count-failed:Agents", useUnmergedTree = true).assertIsDisplayed().assertTextEquals("!")
        compose.onNodeWithTag("home:count-failed:Activity", useUnmergedTree = true).assertDoesNotExist()
        compose.onNodeWithTag("home:count:Running", useUnmergedTree = true).assertTextEquals("2")
        compose.onNodeWithTag("home:count:Queue", useUnmergedTree = true).assertTextEquals("3")
        compose.runOnIdle { state.value = state.value.copy(badges = badges) }
        compose.onNodeWithTag("home:count:Agents", useUnmergedTree = true).assertTextEquals("9")
        compose.onNodeWithTag("home:count:Activity", useUnmergedTree = true).assertDoesNotExist()
    }

    @Test fun expandedCountFailureShowsSnackbarWithoutMovingHeader() = assertFooter(true)
    private fun assertFooter(expanded: Boolean) {
        val healthy = HomeUiState(selected = HomeTab.Running, query = query, badges = badges)
        val state = mutableStateOf(healthy)
        var retries = 0
        compose.setContent {
            TeamCityTheme {
                HomeScreen(state.value, expanded, {}, {}, {
                    retries++
                    state.value = healthy
                }, {}, remember { SnackbarHostState() }) { Text("Activity content") }
            }
        }
        val title = compose.onNodeWithTag("home:title").fetchSemanticsNode().boundsInRoot
        compose.runOnIdle { state.value = healthy.copy(badges = healthy.badges + (HomeTab.Running to HomeBadge.Failed())) }
        assertEquals(title, compose.onNodeWithTag("home:title").fetchSemanticsNode().boundsInRoot)
        val notice = compose.onNodeWithText("Couldn’t load the running build count.").assertIsDisplayed().fetchSemanticsNode().boundsInRoot
        assertTrue(notice.top > title.bottom)
        compose.onNodeWithTag("home:action").assertIsDisplayed()
        compose.onNodeWithTag("home:count-failed:Activity", useUnmergedTree = true).assertDoesNotExist()
        compose.onNodeWithText("Retry").performClick()
        assertEquals(1, retries)
        compose.onNodeWithTag("home:retry-notice").assertDoesNotExist()
        assertEquals(title, compose.onNodeWithTag("home:title").fetchSemanticsNode().boundsInRoot)
    }

    @Test fun accountTipHighlightsTheAvatarAndDismissesWithoutOpeningAccount() {
        var dismissals = 0
        var accounts = 0
        val state = mutableStateOf(HomeUiState(prompt = HomePrompt.Drawer.ui()))
        compose.setContent {
            TeamCityTheme {
                HomeScreen(state.value, false, {}, {}, {}, {
                    dismissals++
                    state.value = state.value.copy(prompt = null)
                }, remember { SnackbarHostState() }, onAccount = { accounts++ }) { Text("Content") }
            }
        }
        compose.onNodeWithTag("home:coachmark:Drawer").assertIsDisplayed()
        compose.onNodeWithTag("home:account").assertDoesNotExist()
        compose.onNodeWithTag("home:dismiss-prompt").performClick()
        assertEquals(1, dismissals)
        assertEquals(0, accounts)
        compose.onNodeWithTag("home:account").performClick()
        assertEquals(1, accounts)
    }

    @Test fun actionTipsProtectTheInlineControlsFromActivationThroughTheOverlay() {
        var actions = 0
        val state = mutableStateOf(HomeUiState(selected = HomeTab.Favorites, query = query))
        compose.setContent { TeamCityTheme { HomeScreen(state.value, false, {}, { actions++ }, {}, { state.value = state.value.copy(prompt = null) }, remember { SnackbarHostState() }) { Text("Content") } } }
        listOf(HomePrompt.Running, HomePrompt.Queue, HomePrompt.Agents).forEach { prompt ->
            compose.runOnIdle { state.value = HomeUiState(selected = HomeTab.valueOf(prompt.name), query = query) }
            val target = compose.onNodeWithTag("home:filter:${state.value.filterOptions.first { !it.selected }.filter.name}").fetchSemanticsNode().boundsInRoot.center
            compose.runOnIdle { state.value = state.value.copy(prompt = prompt.ui()) }
            compose.onNodeWithTag("home:coachmark:$prompt").assertIsDisplayed()
            compose.onNodeWithTag("home:action").assertDoesNotExist()
            compose.onRoot().performTouchInput { click(target) }
            assertEquals(0, actions)
            compose.onNodeWithTag("home:coachmark:$prompt").assertDoesNotExist()
        }
    }

    @Test fun repeatedCountFailureOffersRetryAgainAfterAnExplicitAttempt() {
        val state = mutableStateOf(HomeUiState(selected = HomeTab.Running, badges = badges + (HomeTab.Running to HomeBadge.Failed())))
        var retries = 0
        compose.setContent {
            TeamCityTheme {
                HomeScreen(state.value, false, {}, {}, {
                    retries++
                    state.value = state.value.copy(retryAttempt = retries.toLong())
                }, {}, remember { SnackbarHostState() }) { Text("Content") }
            }
        }
        repeat(2) { compose.onNodeWithText("Retry").assertIsDisplayed().performClick() }
        assertEquals(2, retries)
        compose.onNodeWithText("Couldn’t load the running build count.").assertIsDisplayed()
    }

    @Test fun optionalGuidanceFailureDoesNotInterruptHome() {
        compose.setContent { TeamCityTheme { HomeScreen(HomeUiState(onboardingFailed = true), false, {}, {}, {}, {}, remember { SnackbarHostState() }) { Text("Content") } } }
        compose.onNodeWithTag("retry:snackbar").assertDoesNotExist()
        compose.onNodeWithText("Home tips couldn’t be loaded.").assertDoesNotExist()
        compose.onNodeWithText("Content").assertIsDisplayed()
    }

    @Test fun activityCountFailureStaysQuietOnOtherTabsAndDismissesWhenLeavingActivity() {
        val state = mutableStateOf(HomeUiState(selected = HomeTab.Projects, badges = badges + (HomeTab.Running to HomeBadge.Failed())))
        compose.setContent { TeamCityTheme { HomeScreen(state.value, false, {}, {}, {}, {}, remember { SnackbarHostState() }) { Text("Content") } } }
        compose.onNodeWithTag("retry:snackbar").assertDoesNotExist()
        compose.runOnIdle { state.value = state.value.copy(selected = HomeTab.Favorites) }
        compose.onNodeWithTag("retry:snackbar").assertDoesNotExist()
        compose.runOnIdle { state.value = state.value.copy(selected = HomeTab.Running) }
        compose.onNodeWithText("Couldn’t load the running build count.").assertIsDisplayed()
        compose.onNodeWithText("Retry").assertIsDisplayed()
        compose.runOnIdle { state.value = state.value.copy(selected = HomeTab.Favorites) }
        compose.onNodeWithTag("retry:snackbar").assertDoesNotExist()
        compose.runOnIdle { state.value = state.value.copy(selected = HomeTab.Agents) }
        compose.onNodeWithText("Couldn’t load the running build count.").assertIsDisplayed()
    }

    @Test fun activityFilterFailureStaysQuietAwayFromActivityAndClearsOnDeparture() {
        val state = mutableStateOf(HomeUiState(selected = HomeTab.Projects, query = query, badges = badges, filterFailed = true))
        compose.setContent { TeamCityTheme { HomeScreen(state.value, false, {}, {}, {}, {}, remember { SnackbarHostState() }) { Text("Content") } } }
        compose.onNodeWithTag("retry:snackbar").assertDoesNotExist()
        compose.runOnIdle { state.value = state.value.copy(selected = HomeTab.Favorites) }
        compose.onNodeWithTag("retry:snackbar").assertDoesNotExist()
        compose.runOnIdle { state.value = state.value.copy(selected = HomeTab.Running) }
        compose.onNodeWithText("Couldn’t change the filter. Tap a chip to try again.").assertIsDisplayed()
        compose.runOnIdle { state.value = state.value.copy(selected = HomeTab.Favorites) }
        compose.onNodeWithTag("retry:snackbar").assertDoesNotExist()
    }

    @Test fun nestedContentDoesNotShowActivityCountFailures() {
        compose.setContent { TeamCityTheme { HomeScreen(HomeUiState(selected = HomeTab.Running, badges = badges + (HomeTab.Running to HomeBadge.Failed())), false, {}, {}, {}, {}, remember { SnackbarHostState() }, showHomeHeader = false) { Text("Build details") } } }
        compose.onNodeWithTag("retry:snackbar").assertDoesNotExist()
        compose.onNodeWithText("Build details").assertIsDisplayed()
    }

    @Test fun countMessagesNameTheFailedCountsAndIgnoreOptionalGuidance() {
        val failures = listOf(
            setOf(HomeTab.Running) to Res.string.home_running_count_failed,
            setOf(HomeTab.Queue) to Res.string.home_queue_count_failed,
            setOf(HomeTab.Agents) to Res.string.home_agents_count_failed,
            setOf(HomeTab.Running, HomeTab.Queue) to Res.string.home_build_counts_failed,
            setOf(HomeTab.Running, HomeTab.Agents) to Res.string.home_build_and_agent_counts_failed,
            setOf(HomeTab.Queue, HomeTab.Agents) to Res.string.home_build_and_agent_counts_failed,
            setOf(HomeTab.Running, HomeTab.Queue, HomeTab.Agents) to Res.string.home_build_and_agent_counts_failed
        )
        failures.forEach { (failed, message) ->
            val state = HomeUiState(selected = HomeTab.Running, badges = badges + failed.associateWith { HomeBadge.Failed() }, onboardingFailed = true)
            assertEquals(message, state.retryMessageRes)
            assertNull(state.copy(selected = HomeTab.Projects).retryMessageRes)
            assertNull(state.copy(selected = HomeTab.Favorites).retryMessageRes)
        }
        assertNull(HomeUiState(selected = HomeTab.Running, badges = badges, onboardingFailed = true).retryMessageRes)
    }

    @Test fun nestedScreensKeepNavigationVisibleAndOwnTheirHeader() {
        var selected = HomeTab.Projects
        compose.setContent {
            TeamCityTheme {
                HomeScreen(HomeUiState(selected = HomeTab.Running, query = query), false, { selected = it }, {}, {}, {}, remember { SnackbarHostState() }, showHomeHeader = false) {
                    Column {
                        Text("Build details header")
                        Text("Build content")
                    }
                }
            }
        }
        compose.onNodeWithText("Build details header").assertIsDisplayed()
        compose.onNodeWithText("Build content").assertIsDisplayed()
        compose.onNodeWithTag("home:title").assertDoesNotExist()
        compose.onNodeWithTag("home:account").assertDoesNotExist()
        compose.onNodeWithTag("home:activity-views").assertDoesNotExist()
        listOf("Projects", "Favorites", "Activity").forEach { compose.onNodeWithTag("home:tab:$it").assertIsDisplayed() }
        compose.onNodeWithTag("home:tab:Activity").assertIsSelected()
        compose.onNodeWithTag("home:tab:Favorites").performClick()
        assertEquals(HomeTab.Favorites, selected)
    }
}
