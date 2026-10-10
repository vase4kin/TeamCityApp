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

package teamcityapp.features.agents.impl

import android.app.Application
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import teamcityapp.features.agents.api.Agent
import teamcityapp.features.agents.api.AgentsFilter
import teamcityapp.libraries.list_state.ListUiState
import teamcityapp.libraries.theme.TeamCityTheme

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "en-rUS-w360dp-h800dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class AgentsScreenTest {
    @get:Rule val compose = createComposeRule()

    @Test fun drawerButtonDelegatesNavigation() {
        var clicks = 0
        compose.setContent { TeamCityTheme { AgentsScreen(AgentsUiState(list = ListUiState.Empty()), {}, {}, { clicks++ }) } }
        compose.onNodeWithContentDescription(RuntimeEnvironment.getApplication().getString(R.string.agents_open_drawer)).performClick()
        assertEquals(1, clicks)
    }

    @Test fun disconnectedFilterHasItsOwnEmptyMessage() {
        compose.setContent { TeamCityTheme { AgentsScreen(AgentsUiState(AgentsFilter.Disconnected, ListUiState.Empty()), {}, {}, {}) } }
        compose.onNodeWithText(RuntimeEnvironment.getApplication().getString(R.string.agents_empty_disconnected)).assertIsDisplayed()
    }

    @Test fun initialErrorRetryDelegatesLoading() {
        var retries = 0
        compose.setContent { TeamCityTheme { AgentsScreen(AgentsUiState(list = ListUiState.Error), {}, { retries++ }, {}) } }
        compose.onNodeWithText(RuntimeEnvironment.getApplication().getString(teamcityapp.libraries.theme.R.string.action_retry)).performClick()
        assertEquals(1, retries)
    }

    @Test fun contentSupportsDuplicateNamesAndPullToRefresh() {
        var refreshes = 0
        compose.setContent {
            TeamCityTheme {
                AgentsScreen(AgentsUiState(list = ListUiState.Content(listOf(Agent("1", "Same name"), Agent("2", "Same name")))), { refreshes++ }, {}, {})
            }
        }
        compose.onNodeWithTag("agents:row:1").assertIsDisplayed()
        compose.onNodeWithTag("agents:row:2").assertIsDisplayed()
        compose.onNodeWithTag("agents:list").performTouchInput { swipeDown() }
        compose.waitForIdle()
        assertEquals(1, refreshes)
    }
}
