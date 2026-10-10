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

package teamcityapp.libraries.list_ui

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import teamcityapp.libraries.list_state.ListUiState
import teamcityapp.libraries.theme.TeamCityTheme

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "en-rUS-w360dp-h800dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TeamCityListContainerTest {
    @get:Rule val compose = createComposeRule()

    @Test fun initialErrorRetryAndRefreshFailureKeepSeparatePresentation() {
        val state = mutableStateOf<ListUiState<String>>(ListUiState.Error)
        var retries = 0
        compose.setContent {
            TeamCityTheme {
                TeamCityListContainer(state.value, {}, { retries++ }, Modifier.fillMaxSize(), empty = { TeamCityListEmpty("No agents") }) {
                    LazyColumn { items(2) { Text("Agent $it") } }
                }
            }
        }
        compose.onNodeWithText("Try again").performClick()
        assertEquals(1, retries)
        compose.runOnIdle { state.value = ListUiState.Content(listOf("0", "1"), refreshFailed = true) }
        compose.onNodeWithText("Agent 0").assertIsDisplayed()
        compose.onNodeWithText("Couldn’t refresh. Try again.").assertIsDisplayed()
        compose.onNodeWithText("Retry").performClick()
        assertEquals(2, retries)
        compose.runOnIdle { state.value = ListUiState.Content(listOf("0", "1"), isRefreshing = true, refreshFailed = true) }
        compose.onNodeWithText("Retry").assertIsNotEnabled()
        compose.onNodeWithText("Agent 0").assertIsDisplayed()
    }

    @Test fun emptyListSupportsPullToRefresh() = pullToRefresh(ListUiState.Empty())

    @Test fun populatedListSupportsPullToRefresh() = pullToRefresh(ListUiState.Content(listOf("agent")))

    private fun pullToRefresh(state: ListUiState<String>) {
        var refreshes = 0
        compose.setContent {
            TeamCityTheme {
                TeamCityListContainer(state, { refreshes++ }, {}, Modifier.fillMaxSize().testTag("list"), empty = { TeamCityListEmpty("No agents") }) {
                    LazyColumn(Modifier.fillMaxSize()) { item { Text("Agent") } }
                }
            }
        }
        compose.onNodeWithTag("list").performTouchInput {
            swipe(start = center.copy(y = height * .15f), end = center.copy(y = height * .85f), durationMillis = 600)
        }
        compose.waitForIdle()
        assertEquals(1, refreshes)
    }

    @Test fun sectionHeaderAndAppendRetryDispatchFeatureActions() {
        var sectionClicks = 0
        var appendRetries = 0
        compose.setContent {
            TeamCityTheme {
                Column {
                    TeamCityListSectionHeader("Project", onClick = { sectionClicks++ })
                    TeamCityListSectionHeader("Date")
                    TeamCityListAppendRetry({ appendRetries++ })
                }
            }
        }
        compose.onNodeWithText("Project").performClick()
        compose.onNodeWithText("Date").assertHasNoClickAction()
        compose.onNodeWithText("Retry").performClick()
        assertEquals(1, sectionClicks)
        assertEquals(1, appendRetries)
    }
}
