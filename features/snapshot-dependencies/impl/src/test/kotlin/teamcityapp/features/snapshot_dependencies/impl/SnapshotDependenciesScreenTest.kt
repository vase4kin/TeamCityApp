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

package teamcityapp.features.snapshot_dependencies.impl

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
import teamcityapp.libraries.build_ui.buildConfigurationTitle
import teamcityapp.libraries.builds.*
import teamcityapp.libraries.list_state.ListUiState
import teamcityapp.libraries.theme.TeamCityTheme

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "en-rUS-w360dp-h800dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class SnapshotDependenciesScreenTest {
    @get:Rule val compose = createComposeRule()

    @Test fun rowPassesFullLaunchSnapshot() {
        val build = snapshotBuild().copy(personal = true, pinned = true)
        var opened: BuildLaunchData? = null
        compose.setContent { TeamCityTheme { SnapshotDependenciesScreen(ListUiState.Content(listOf(build)), {}, {}, { opened = it }, { _, _ -> }) } }
        compose.onNodeWithTag("snapshot:build:1").performClick()
        assertSame(build, opened)
        compose.onNodeWithContentDescription("Personal build").assertExists()
        compose.onNodeWithContentDescription("Pinned build").assertExists()
    }

    @Test fun sectionOpensHistoryWithConfigurationIdAndName() {
        var history: Pair<String, String>? = null
        compose.setContent { TeamCityTheme { SnapshotDependenciesScreen(ListUiState.Content(listOf(snapshotBuild())), {}, {}, {}, { id, name -> history = id to name }) } }
        compose.onNodeWithTag("snapshot:configuration:0").performClick()
        assertEquals("Android_Debug" to "Android Debug", history)
    }

    @Test fun adjacentEqualTitlesShareAHeaderButNonAdjacentTitlesDoNot() {
        val a = snapshotBuild()
        val b = snapshotBuild("2").copy(configuration = BuildConfigurationData("Android_Debug", "Android Debug", "Mobile", "Mobile"))
        val c = snapshotBuild("3").copy(buildTypeId = "other", configuration = null)
        val d = snapshotBuild("4")
        compose.setContent { TeamCityTheme { SnapshotDependenciesScreen(ListUiState.Content(listOf(a, b, c, d)), {}, {}, {}, { _, _ -> }) } }
        compose.onNodeWithTag("snapshot:configuration:0").assertExists()
        compose.onNodeWithTag("snapshot:configuration:1").assertDoesNotExist()
        compose.onNodeWithTag("snapshot:configuration:2").assertExists()
        compose.onNodeWithTag("snapshot:configuration:3").assertExists()
    }

    @Test fun partialConfigurationUsesLegacyIdFallbackAndEmptyName() {
        val build = snapshotBuild().copy(configuration = BuildConfigurationData("different-id", "Debug", projectName = null))
        assertEquals("Android_Debug", buildConfigurationTitle(build))
        var history: Pair<String, String>? = null
        compose.setContent { TeamCityTheme { SnapshotDependenciesScreen(ListUiState.Content(listOf(build.copy(configuration = null))), {}, {}, {}, { id, name -> history = id to name }) } }
        compose.onNodeWithTag("snapshot:configuration:0").performClick()
        assertEquals("Android_Debug" to "", history)
    }

    @Test fun queuedWaitReasonAndMissingNumberAreRendered() {
        val build = snapshotBuild().copy(state = "queued", number = null, waitReason = "Waiting for a compatible agent", statusText = "ignored server status")
        compose.setContent { TeamCityTheme { SnapshotDependenciesScreen(ListUiState.Content(listOf(build)), {}, {}, {}, { _, _ -> }) } }
        compose.onNodeWithText("Waiting for a compatible agent", useUnmergedTree = true).assertIsDisplayed()
        compose.onNodeWithText("#No number", useUnmergedTree = true).assertIsDisplayed()
        compose.onNodeWithText("ignored server status", useUnmergedTree = true).assertDoesNotExist()
    }

    @Test fun initialFailureRetriesThroughSharedContainer() {
        var retries = 0
        compose.setContent { TeamCityTheme { SnapshotDependenciesScreen(ListUiState.Error, {}, { retries++ }, {}, { _, _ -> }) } }
        compose.onNodeWithText("Try again").performClick()
        assertEquals(1, retries)
    }

    @Test fun refreshFailureKeepsLaunchableRows() {
        var opened = false
        compose.setContent { TeamCityTheme { SnapshotDependenciesScreen(ListUiState.Content(listOf(snapshotBuild()), refreshFailed = true), {}, {}, { opened = true }, { _, _ -> }) } }
        compose.onNodeWithTag("snapshot:build:1").performClick()
        assertTrue(opened)
        compose.onNodeWithText("Couldn’t refresh. Try again.").assertExists()
    }

    @Test fun snapshotEmptyMessageIsSpecificToThisTab() {
        compose.setContent { TeamCityTheme { SnapshotDependenciesScreen(ListUiState.Empty(), {}, {}, {}, { _, _ -> }) } }
        compose.onNodeWithText("No snapshot dependencies").assertIsDisplayed()
    }
}
