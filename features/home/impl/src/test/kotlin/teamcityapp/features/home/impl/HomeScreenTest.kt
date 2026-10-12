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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import teamcityapp.features.home.api.*
import teamcityapp.libraries.theme.*

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class HomeScreenTest {
    @get:Rule val compose = createComposeRule()

    @Test fun compactCountFailureStaysAboveTabsWithoutMovingTheToolbar() = assertCountFailureStaysInFooter(expanded = false)

    @Test fun expandedCountFailureStaysBelowContentWithoutMovingTheToolbar() = assertCountFailureStaysInFooter(expanded = true)

    @OptIn(ExperimentalMaterial3Api::class)
    private fun assertCountFailureStaysInFooter(expanded: Boolean) {
        val healthy = HomeUiState(selected = HomeTab.Favorites, badges = mapOf(HomeTab.Favorites to HomeBadge.Count(1), HomeTab.Running to HomeBadge.Count(2), HomeTab.Agents to HomeBadge.Count(9)))
        val state = mutableStateOf(healthy)
        var retries = 0
        compose.setContent {
            TeamCityTheme {
                HomeScreen(state.value, expanded, {}, {}, {
                    retries++
                    state.value = healthy
                }, {}, remember { SnackbarHostState() }) {
                    Scaffold(topBar = {
                        CenterAlignedTopAppBar(title = { Text("Favorites toolbar", Modifier.testTag("test:toolbar-title")) }, windowInsets = WindowInsets(top = 24.dp))
                    }) { padding -> Text("Favorite configuration", Modifier.padding(padding)) }
                }
            }
        }
        val title = compose.onNodeWithTag("test:toolbar-title").fetchSemanticsNode().boundsInRoot
        compose.runOnIdle { state.value = healthy.copy(badges = healthy.badges + (HomeTab.Running to HomeBadge.Failed())) }
        assertEquals(title, compose.onNodeWithTag("test:toolbar-title").fetchSemanticsNode().boundsInRoot)
        assertTrue(title.top >= 24f)
        val notice = compose.onNodeWithTag("home:retry-notice").assertIsDisplayed().fetchSemanticsNode().boundsInRoot
        assertTrue(notice.top > title.bottom)
        assertTrue(compose.onNodeWithTag("home:fab").fetchSemanticsNode().boundsInRoot.bottom <= notice.top)
        if (!expanded) assertTrue(notice.bottom <= compose.onNodeWithTag("home:tab:Favorites").fetchSemanticsNode().boundsInRoot.top)
        compose.onNodeWithTag("home:count:Favorites", useUnmergedTree = true).assertTextEquals("1")
        compose.onNodeWithTag("home:count:Agents", useUnmergedTree = true).assertTextEquals("9")
        compose.onNodeWithText("Retry counts").performClick()
        assertEquals(1, retries)
        compose.onNodeWithTag("home:retry-notice").assertDoesNotExist()
        assertEquals(title, compose.onNodeWithTag("test:toolbar-title").fetchSemanticsNode().boundsInRoot)
    }

    @Test fun onboardingFailureUsesTheSameFooterAndRetryCallback() {
        var retries = 0
        compose.setContent { TeamCityTheme { HomeScreen(HomeUiState(onboardingFailed = true), false, {}, {}, { retries++ }, {}, remember { SnackbarHostState() }) { Text("Content") } } }
        compose.onNodeWithText("Home tips couldn’t be loaded.").assertIsDisplayed()
        compose.onNodeWithText("Retry").performClick()
        assertEquals(1, retries)
    }

    @Test fun drawerTipHighlightsItsChildControlAndOnlyDismissesTheTip() {
        var dismissals = 0
        var drawerClicks = 0
        val state = mutableStateOf(HomeUiState(prompt = HomePrompt.Drawer.ui()))
        compose.setContent {
            TeamCityTheme {
                HomeScreen(state.value, true, {}, {}, {}, {
                    dismissals++
                    state.value = state.value.copy(prompt = null)
                }, remember { SnackbarHostState() }) {
                    IconButton({ drawerClicks++ }, Modifier.coachmarkAnchor(DRAWER_COACHMARK_ANCHOR)) { Text("Drawer") }
                }
            }
        }
        compose.onNodeWithTag("home:coachmark:Drawer").assertIsDisplayed()
        compose.onNodeWithText("Drawer").assertDoesNotExist()
        compose.onNodeWithTag("home:dismiss-prompt").performClick()
        assertEquals(1, dismissals)
        assertEquals(0, drawerClicks)
        compose.onNodeWithTag("home:coachmark:Drawer").assertDoesNotExist()
        compose.onNodeWithText("Drawer").performClick()
        assertEquals(1, drawerClicks)
    }

    @Test fun everyFabTipUsesItsSelectedTabsActionAndCannotActivateItThroughTheOverlay() {
        var fabClicks = 0
        val state = mutableStateOf(HomeUiState(selected = HomeTab.Favorites))
        compose.setContent { TeamCityTheme { HomeScreen(state.value, false, {}, { fabClicks++ }, {}, { state.value = state.value.copy(prompt = null) }, remember { SnackbarHostState() }) { Text("Content") } } }
        listOf(HomePrompt.Favorites, HomePrompt.Running, HomePrompt.Queue, HomePrompt.Agents).forEach { prompt ->
            compose.runOnIdle { state.value = HomeUiState(selected = HomeTab.valueOf(prompt.name)) }
            val target = compose.onNodeWithTag("home:fab").fetchSemanticsNode().boundsInRoot.center
            compose.runOnIdle { state.value = state.value.copy(prompt = prompt.ui()) }
            compose.onNodeWithTag("home:coachmark:$prompt").assertIsDisplayed()
            compose.onNodeWithTag("home:fab").assertDoesNotExist()
            compose.onRoot().performTouchInput { click(target) }
            compose.onNodeWithTag("home:coachmark:$prompt").assertDoesNotExist()
            assertEquals(0, fabClicks)
        }
    }

    @Test fun noTipIsShownUntilItsActualControlHasBeenPlaced() {
        compose.setContent { TeamCityTheme { HomeScreen(HomeUiState(prompt = HomePrompt.Drawer.ui()), false, {}, {}, {}, {}, remember { SnackbarHostState() }) { Text("No drawer control") } } }
        compose.onNodeWithTag("home:coachmark:Drawer").assertDoesNotExist()
        compose.onNodeWithText("No drawer control").assertIsDisplayed()
    }
}
