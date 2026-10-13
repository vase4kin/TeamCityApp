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
import teamcityapp.libraries.theme.*

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class HomeScreenTest {
    @get:Rule val compose = createComposeRule()
    private val badges = mapOf(HomeTab.Running to HomeBadge.Count(2), HomeTab.Queue to HomeBadge.Count(3), HomeTab.Agents to HomeBadge.Count(9), HomeTab.Favorites to HomeBadge.Count(1))
    private val query = HomeQuery("account", listOf("configuration"), QuickFilter.RunningFavorites, QuickFilter.QueuedAll, QuickFilter.AgentsConnected, HomeAccount("Andrey Tolpeev", "https://teamcity.example"))

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
        compose.onNodeWithText("Favorite configurations").assertIsDisplayed()
        compose.onNodeWithTag("home:tab:Queue").performClick()
        compose.onNodeWithTag("home:tab:Queue").assertIsSelected()
        compose.onNodeWithText("All configurations").assertIsDisplayed()
        compose.onNodeWithTag("home:tab:Agents").performClick()
        compose.onNodeWithText("Connected agents").assertIsDisplayed()
        compose.onNodeWithTag("home:count:Agents", useUnmergedTree = true).assertTextEquals("9")
        compose.onNodeWithText("Filters").performClick()
        assertEquals(1, actions)
    }

    @Test fun compactCountFailureShowsSnackbarWithoutMovingHeader() = assertFooter(false)

    @Test fun compactAgentsBadgeMatchesTheOtherActivityCounts() = assertAgentsBadges(false, 1f)

    @Test fun expandedAgentsBadgeMatchesTheOtherActivityCounts() = assertAgentsBadges(true, 1f)

    @Test fun enlargedTextShowsAgentsBadgeBelowItsLabel() = assertAgentsBadges(false, 1.5f)

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
        val healthy = HomeUiState(selected = HomeTab.Favorites, query = query, badges = badges)
        val state = mutableStateOf(healthy)
        var retries = 0
        compose.setContent {
            TeamCityTheme {
                HomeScreen(state.value, expanded, {}, {}, {
                    retries++
                    state.value = healthy
                }, {}, remember { SnackbarHostState() }) { Text("Favorite configuration") }
            }
        }
        val title = compose.onNodeWithTag("home:title").fetchSemanticsNode().boundsInRoot
        compose.runOnIdle { state.value = healthy.copy(badges = healthy.badges + (HomeTab.Running to HomeBadge.Failed())) }
        assertEquals(title, compose.onNodeWithTag("home:title").fetchSemanticsNode().boundsInRoot)
        val notice = compose.onNodeWithText("Some tab counts couldn’t be loaded.").assertIsDisplayed().fetchSemanticsNode().boundsInRoot
        assertTrue(notice.top > title.bottom)
        compose.onNodeWithTag("home:action").assertDoesNotExist()
        compose.onNodeWithTag("home:count-failed:Activity", useUnmergedTree = true).assertDoesNotExist()
        compose.onNodeWithText("Retry counts").performClick()
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
            val target = compose.onNodeWithTag("home:action").fetchSemanticsNode().boundsInRoot.center
            compose.runOnIdle { state.value = state.value.copy(prompt = prompt.ui()) }
            compose.onNodeWithTag("home:coachmark:$prompt").assertIsDisplayed()
            compose.onNodeWithTag("home:action").assertDoesNotExist()
            compose.onRoot().performTouchInput { click(target) }
            assertEquals(0, actions)
            compose.onNodeWithTag("home:coachmark:$prompt").assertDoesNotExist()
        }
    }

    @Test fun repeatedCountFailureOffersRetryAgainAfterAnExplicitAttempt() {
        val state = mutableStateOf(HomeUiState(badges = badges + (HomeTab.Running to HomeBadge.Failed())))
        var retries = 0
        compose.setContent {
            TeamCityTheme {
                HomeScreen(state.value, false, {}, {}, {
                    retries++
                    state.value = state.value.copy(retryAttempt = retries.toLong())
                }, {}, remember { SnackbarHostState() }) { Text("Content") }
            }
        }
        repeat(2) { compose.onNodeWithText("Retry counts").assertIsDisplayed().performClick() }
        assertEquals(2, retries)
        compose.onNodeWithText("Some tab counts couldn’t be loaded.").assertIsDisplayed()
    }

    @Test fun onboardingFailureShowsSnackbarRetry() {
        var retries = 0
        compose.setContent { TeamCityTheme { HomeScreen(HomeUiState(onboardingFailed = true), false, {}, {}, { retries++ }, {}, remember { SnackbarHostState() }) { Text("Content") } } }
        compose.onNodeWithText("Home tips couldn’t be loaded.").assertIsDisplayed()
        compose.onNodeWithText("Retry").performClick()
        assertEquals(1, retries)
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
