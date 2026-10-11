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

package teamcityapp.features.build_queue.impl

import android.app.Application
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import teamcityapp.features.build_queue.api.*
import teamcityapp.libraries.builds.BuildLaunchData
import teamcityapp.libraries.list_state.ListUiState
import teamcityapp.libraries.theme.TeamCityTheme

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "en-rUS-w360dp-h800dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class BuildQueueScreenTest {
    @get:Rule val compose = createComposeRule()
    private val query = BuildQueueQuery("account")

    @Test fun drawerActionDelegatesToHost() {
        var opened = false
        compose.setContent { TeamCityTheme { BuildQueueScreen(BuildQueueUiState(query, ListUiState.Empty()), {}, {}, { opened = true }, {}, { _, _ -> }) } }
        compose.onNodeWithContentDescription("Open navigation drawer").performClick()
        assertTrue(opened)
    }

    @Test fun rowPassesCompleteSnapshot() {
        val build = buildRow()
        var opened: BuildLaunchData? = null
        compose.setContent { TeamCityTheme { BuildQueueScreen(BuildQueueUiState(query, ListUiState.Content(listOf(build))), {}, {}, {}, { opened = it }, { _, _ -> }) } }
        compose.onNodeWithTag("build_queue:build:1").performClick()
        assertSame(build, opened)
    }

    @Test fun headerOpensHistoryWithConfigurationIdentity() {
        var selected: Pair<String, String>? = null
        compose.setContent { TeamCityTheme { BuildQueueScreen(BuildQueueUiState(query, ListUiState.Content(listOf(buildRow()))), {}, {}, {}, {}, { id, name -> selected = id to name }) } }
        compose.onNodeWithTag("build_queue:configuration:0").performClick()
        assertEquals("Android_Debug" to "Android Debug", selected)
    }

    @Test fun adjacentEqualTitlesShareHeader() {
        compose.setContent { TeamCityTheme { BuildQueueScreen(BuildQueueUiState(query, ListUiState.Content(listOf(buildRow(), buildRow("2")))), {}, {}, {}, {}, { _, _ -> }) } }
        compose.onNodeWithTag("build_queue:configuration:0").assertExists()
        compose.onNodeWithTag("build_queue:configuration:1").assertDoesNotExist()
    }

    @Test fun favoritesEmptyMessageMatchesHomeFilter() {
        compose.setContent { TeamCityTheme { BuildQueueScreen(BuildQueueUiState(query, ListUiState.Empty()), {}, {}, {}, {}, { _, _ -> }) } }
        compose.onNodeWithText("Favorite builds queue is empty").assertIsDisplayed()
    }

    @Test fun allEmptyMessageMatchesHomeFilter() {
        compose.setContent { TeamCityTheme { BuildQueueScreen(BuildQueueUiState(query.copy(filter = BuildQueueFilter.All), ListUiState.Empty()), {}, {}, {}, {}, { _, _ -> }) } }
        compose.onNodeWithText("Builds queue is empty").assertIsDisplayed()
    }

    @Test fun initialErrorRetriesThroughSharedContainer() {
        var retried = false
        compose.setContent { TeamCityTheme { BuildQueueScreen(BuildQueueUiState(query, ListUiState.Error), {}, { retried = true }, {}, {}, { _, _ -> }) } }
        compose.onNodeWithText("Try again").performClick()
        assertTrue(retried)
    }
}
