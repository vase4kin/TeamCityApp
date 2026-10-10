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

package teamcityapp.features.about.impl

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import teamcityapp.libraries.theme.TeamCityTheme

@OptIn(ExperimentalTestApi::class)
@RunWith(AndroidJUnit4::class)
class AboutScreenTest {
    @get:Rule val compose = createComposeRule()
    private val offline = AboutUiState.Content(ServerDetailsUiState.Unavailable)
    private fun text(id: Int) = InstrumentationRegistry.getInstrumentation().targetContext.getString(id)

    private fun content(
        width: Int,
        state: AboutUiState = offline,
        onAction: (AboutAction) -> Unit = {},
        onClose: () -> Unit = {},
        height: Int = 700
    ) {
        compose.setContent {
            DeviceConfigurationOverride(DeviceConfigurationOverride.WindowSize(DpSize(width.dp, height.dp))) {
                TeamCityTheme { AboutScreen(state, {}, onAction, onClose) }
            }
        }
    }

    @Test fun compactWindowStacksSectionsAndShowsUnavailableServer() {
        content(360)
        val cards = compose.onAllNodesWithTag("about:section").fetchSemanticsNodes()
        assertTrue(cards.size >= 2)
        assertEquals(cards[0].boundsInRoot.left, cards[1].boundsInRoot.left, 1f)
        assertTrue(cards[1].boundsInRoot.top > cards[0].boundsInRoot.bottom)
        compose.onNodeWithTag("about:server-unavailable").assertIsDisplayed()
    }

    @Test fun wideWindowUsesCenteredColumns() {
        content(1000)
        val cards = compose.onAllNodesWithTag("about:section").fetchSemanticsNodes()
        assertEquals(cards[0].boundsInRoot.top, cards[1].boundsInRoot.top, 1f)
        assertTrue(cards[1].boundsInRoot.left > cards[0].boundsInRoot.right)
        assertTrue(cards[0].boundsInRoot.left > 0)
    }

    @Test fun ratingEventIsDelegatedToCaller() {
        val actions = mutableListOf<AboutAction>()
        content(360, onAction = actions::add)
        compose.onNodeWithText(text(R.string.about_app_text_rate_app)).performScrollTo().performClick()
        assertEquals(listOf(AboutAction.Rate), actions)
    }

    @Test fun backEventIsDelegatedToCaller() {
        var backCount = 0
        content(360, onClose = { backCount++ })
        compose.onNodeWithContentDescription(text(teamcityapp.libraries.theme.R.string.action_back)).performClick()
        assertEquals(1, backCount)
    }

    @Test fun backTooltipIsShownOnLongPress() {
        content(360)
        val back = text(teamcityapp.libraries.theme.R.string.action_back)
        compose.onNodeWithContentDescription(back).performTouchInput { longClick() }
        compose.onNodeWithText(back).assertIsDisplayed()
    }

    @Test fun contentScrollsWhileAppBarStaysPinned() {
        content(360, height = 500)
        val back = compose.onNodeWithContentDescription(text(teamcityapp.libraries.theme.R.string.action_back))
        val initialBounds = back.fetchSemanticsNode().boundsInRoot
        val grid = compose.onNode(hasScrollToIndexAction())
        grid.performTouchInput { swipeUp() }
        val privacy = text(teamcityapp.libraries.resources.R.string.about_app_text_privacy)
        grid.performScrollToNode(hasText(privacy))
        compose.onNodeWithText(privacy).assertIsDisplayed()
        back.assertIsDisplayed()
        assertEquals(initialBounds, back.fetchSemanticsNode().boundsInRoot)
    }
}
