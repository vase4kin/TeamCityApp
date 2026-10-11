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

package teamcityapp.libraries.build_ui

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.text.TextLayoutResult
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import teamcityapp.libraries.builds.*
import teamcityapp.libraries.theme.TeamCityTheme

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "en-rUS-w360dp-h800dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class BuildUiTest {
    @get:Rule val compose = createComposeRule()
    private val build = BuildLaunchData("1", "/builds/1", number = "42", status = "SUCCESS", state = "finished", statusText = "Success")

    @Test fun rowIsAnAccessibleActionWithPersonalAndPinnedLabels() {
        var opened = false
        compose.setContent { TeamCityTheme { TeamCityBuildRow(BuildRowUiState(build.copy(personal = true, pinned = true)), { opened = true }, androidx.compose.ui.Modifier.testTag("row")) } }
        compose.onNodeWithTag("row").performClick()
        assertTrue(opened)
        compose.onNodeWithContentDescription("Personal build").assertExists()
        compose.onNodeWithContentDescription("Pinned build").assertExists()
    }

    @Test fun queuedFallbackAndNullNumberMatchLegacyFormatting() {
        compose.setContent { TeamCityTheme { TeamCityBuildRow(BuildRowUiState(build.copy(state = "queued", number = null)), {}) } }
        compose.onNodeWithText("Queued build", useUnmergedTree = true).assertExists()
        compose.onNodeWithText("#No number", useUnmergedTree = true).assertExists()
    }

    @Test fun queuedWaitReasonReplacesStatusText() {
        compose.setContent { TeamCityTheme { TeamCityBuildRow(BuildRowUiState(build.copy(state = "queued", waitReason = "Waiting for an agent")), {}) } }
        compose.onNodeWithText("Waiting for an agent", useUnmergedTree = true).assertExists()
        compose.onNodeWithText("Success", useUnmergedTree = true).assertDoesNotExist()
    }

    @Test fun runningIndicatorHasStatusDescription() {
        compose.setContent { TeamCityTheme { TeamCityBuildRow(BuildRowUiState(build.copy(state = "running")), {}) } }
        compose.onNodeWithContentDescription("Running").assertExists()
    }

    @Test fun statusIsVisibleTextEvenWhenServerDetailIsMissingOrUsesDifferentWording() {
        val current = mutableStateOf(build.copy(statusText = null))
        compose.setContent { TeamCityTheme { TeamCityBuildRow(BuildRowUiState(current.value), {}) } }
        val cases = listOf(
            build.copy(statusText = null) to "Success",
            build.copy(status = "FAILURE", statusText = "2 tests failed") to "Failed",
            build.copy(status = "ERROR", statusText = null) to "Error",
            build.copy(status = "UNKNOWN", statusText = null) to "Unknown",
            build.copy(state = "running", statusText = null) to "Running",
            build.copy(state = "queued", waitReason = "Waiting for an agent") to "Queued"
        )
        cases.forEach { (snapshot, label) ->
            compose.runOnIdle { current.value = snapshot }
            compose.onNodeWithText(label, useUnmergedTree = true).assertIsDisplayed()
        }
    }

    @Test fun matchingServerStatusDoesNotRepeatTheVisibleLabel() {
        compose.setContent { TeamCityTheme { TeamCityBuildRow(BuildRowUiState(build), {}) } }
        compose.onAllNodesWithText("Success", useUnmergedTree = true).assertCountEquals(1)
    }

    @Test fun longBuildDetailsWrapWithoutClippingAndKeepFlagsAndRowAction() {
        val snapshot = build.copy(
            number = "123456789012345678901234567890",
            statusText = "Building the production mobile application with every deployment environment",
            branchName = "feature/preserve-the-complete-build-launch-payload-and-configuration-navigation",
            personal = true,
            pinned = true
        )
        var opened = false
        compose.setContent { TeamCityTheme { TeamCityBuildRow(BuildRowUiState(snapshot), { opened = true }, androidx.compose.ui.Modifier.testTag("row")) } }
        compose.onNodeWithText("#${snapshot.number}", useUnmergedTree = true).assertIsDisplayed()
        compose.onNodeWithText(snapshot.statusText!!, useUnmergedTree = true).assertIsDisplayed()
        compose.onNodeWithText(snapshot.branchName!!, useUnmergedTree = true).assertIsDisplayed()
        listOf("#${snapshot.number}", snapshot.statusText, snapshot.branchName).forEach { text ->
            val layouts = mutableListOf<TextLayoutResult>()
            compose.onNodeWithText(text!!, useUnmergedTree = true).performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(layouts) }
            assertFalse("Long build text must wrap instead of clipping: $text", layouts.single().hasVisualOverflow)
        }
        compose.onNodeWithContentDescription("Personal build").assertIsDisplayed()
        compose.onNodeWithContentDescription("Pinned build").assertIsDisplayed()
        compose.onNodeWithTag("row").performClick()
        assertTrue(opened)
    }

    @Test fun absentAndPartialConfigurationsUseTopLevelId() {
        assertEquals("top-level", buildConfigurationTitle(build.copy(buildTypeId = "top-level")))
        assertEquals("top-level", buildConfigurationTitle(build.copy(buildTypeId = "top-level", configuration = BuildConfigurationData("nested", "Debug"))))
    }

    @Test fun projectNamePresencePreservesLegacyTitleIncludingNullName() {
        assertEquals("Mobile - null", buildConfigurationTitle(build.copy(configuration = BuildConfigurationData("nested", projectName = "Mobile"))))
    }

    @Test fun headerActionUsesTopLevelIdAndConfigurationName() {
        var history: Pair<String, String>? = null
        compose.setContent { TeamCityTheme { TeamCityBuildConfigurationHeader(build.copy(buildTypeId = "top-level", configuration = BuildConfigurationData("nested", "Debug", "Mobile", "Mobile")), { id, name -> history = id to name }) } }
        compose.onNodeWithText("Mobile - Debug").performClick()
        assertEquals("top-level" to "Debug", history)
    }
}
