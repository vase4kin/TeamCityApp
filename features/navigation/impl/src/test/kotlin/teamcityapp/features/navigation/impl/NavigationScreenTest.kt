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

package teamcityapp.features.navigation.impl

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
import teamcityapp.features.navigation.api.NavigationEntry
import teamcityapp.libraries.build_configurations.BuildConfigurationSummary
import teamcityapp.libraries.build_configurations.ProjectReference
import teamcityapp.libraries.list_state.ListUiState
import teamcityapp.libraries.theme.TeamCityTheme

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "en-rUS-w360dp-h800dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class NavigationScreenTest {
    @get:Rule val compose = createComposeRule()
    private val project = ProjectReference("project", "TeamCity")
    private val entries = listOf(NavigationEntry.Project(project, "Project description"), NavigationEntry.Configuration(BuildConfigurationSummary("build", "Android", null, project)))

    @Test fun projectAndConfigurationRowsHaveIndependentActionsAndOptionalDescriptions() {
        val clicked = mutableListOf<NavigationEntry>()
        compose.setContent { TeamCityTheme { NavigationScreen(NavigationUiState(project, ListUiState.Content(entries)), {}, {}, {}, { clicked += it }, {}, {}) } }
        compose.onNodeWithText("Project description").assertIsDisplayed()
        compose.onNodeWithTag("navigation:row:project:project").performClick()
        compose.onNodeWithTag("navigation:row:configuration:build").performClick()
        assertEquals(entries, clicked)
    }

    @Test fun rootDrawerAndNestedBackExposeAccessibleNavigation() {
        var opened = 0
        compose.setContent { TeamCityTheme { NavigationScreen(NavigationUiState(), {}, {}, { opened++ }, {}, {}, {}) } }
        compose.onNodeWithContentDescription("Open navigation drawer").performClick()
        assertEquals(1, opened)
    }

    @Test fun nestedActivityToolbarUsesArgumentTitleAndBack() {
        var backs = 0
        compose.setContent { TeamCityTheme { NavigationScreen(NavigationUiState(project, root = false), {}, {}, { backs++ }, {}, {}, {}) } }
        compose.onNodeWithText("TeamCity").assertIsDisplayed()
        compose.onNodeWithContentDescription("Back").performClick()
        assertEquals(1, backs)
    }

    @Test fun ratingActionsAreIndependentOfRowNavigation() {
        var cancelled = 0
        var rated = 0
        var rowsClicked = 0
        compose.setContent { TeamCityTheme { NavigationScreen(NavigationUiState(project, ListUiState.Content(entries), RatingPromptState.Available()), {}, {}, {}, { rowsClicked++ }, { cancelled++ }, { rated++ }) } }
        compose.onNodeWithText("Cancel").performClick()
        compose.onNodeWithText("Rate", useUnmergedTree = true).performClick()
        assertEquals(1, cancelled)
        assertEquals(1, rated)
        assertEquals(0, rowsClicked)
    }

    @Test fun savingDisablesBothRatingActions() {
        compose.setContent { TeamCityTheme { NavigationScreen(NavigationUiState(project, ListUiState.Content(entries), RatingPromptState.Available(isSaving = true)), {}, {}, {}, {}, {}, {}) } }
        compose.onNodeWithTag("navigation:rate-cancel").assertIsNotEnabled()
        compose.onNodeWithTag("navigation:rate-now").assertIsNotEnabled()
    }

    @Test fun emptyListDoesNotRenderAnEligibleRatingCard() {
        compose.setContent { TeamCityTheme { NavigationScreen(NavigationUiState(project, ListUiState.Empty(), RatingPromptState.Available()), {}, {}, {}, {}, {}, {}) } }
        compose.onNodeWithText("No projects or build configurations").assertIsDisplayed()
        compose.onNodeWithTag("navigation:rating").assertDoesNotExist()
    }

    @Test fun optionalRatingFailureRetriesOnlyPreferenceAndLeavesRowsClickable() {
        var ratingRetries = 0
        var nodeRetries = 0
        compose.setContent {
            TeamCityTheme {
                NavigationScreen(NavigationUiState(project, ListUiState.Content(entries), RatingPromptState.Unavailable), {}, { nodeRetries++ }, {}, {}, {}, {}, onRatingRetry = { ratingRetries++ })
            }
        }
        compose.onNodeWithTag("navigation:rating-unavailable").assertIsDisplayed()
        compose.onNodeWithText("Retry rating preference").performClick()
        assertEquals(1, ratingRetries)
        assertEquals(0, nodeRetries)
        compose.onNodeWithTag("navigation:row:project:project").assertIsDisplayed()
    }
}
