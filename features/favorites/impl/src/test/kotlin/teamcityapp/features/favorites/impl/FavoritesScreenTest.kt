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

package teamcityapp.features.favorites.impl

import android.app.Application
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import teamcityapp.libraries.build_configurations.BuildConfigurationSummary
import teamcityapp.libraries.build_configurations.ProjectReference
import teamcityapp.libraries.list_state.ListUiState
import teamcityapp.libraries.theme.TeamCityTheme

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "en-rUS-w360dp-h800dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class FavoritesScreenTest {
    @get:Rule val compose = createComposeRule()
    private val first = ProjectReference("first", "Same title")
    private val second = ProjectReference("second", "Same title")
    private val rows = listOf(BuildConfigurationSummary("1", "Android", "Description", first), BuildConfigurationSummary("2", "Release", null, second))

    @Test fun sameNamedProjectsRemainDistinctAndHeadersNavigateToTheirOwnId() {
        val projects = mutableListOf<ProjectReference>()
        val configurations = mutableListOf<BuildConfigurationSummary>()
        compose.setContent { TeamCityTheme { FavoritesScreen(FavoritesUiState(ListUiState.Content(rows)), {}, {}, {}, projects::add, configurations::add) } }
        compose.onNodeWithTag("favorites:project:first").performClick()
        compose.onNodeWithTag("favorites:project:second").performClick()
        compose.onNodeWithTag("favorites:configuration:1").performClick()
        compose.onNodeWithTag("favorites:configuration:2").performClick()
        assertEquals(listOf(first, second), projects)
        assertEquals(rows, configurations)
        compose.onNodeWithText("Description").assertIsDisplayed()
    }

    @Test fun partialFailureKeepsSuccessfulRowsAndRetryAction() {
        var retries = 0
        val state = FavoritesUiState(ListUiState.Content(rows), FavoritesFailure.Partial(listOf("missing")))
        compose.setContent { TeamCityTheme { FavoritesScreen(state, {}, { retries++ }, {}, {}, {}) } }
        compose.onNodeWithTag("favorites:partial").assertIsDisplayed()
        compose.onNodeWithText("Retry").performClick()
        compose.onNodeWithTag("favorites:configuration:1").assertIsDisplayed()
        assertEquals(1, retries)
    }

    @Test fun partialRetryIsDisabledWhileRefreshing() {
        var retries = 0
        val state = FavoritesUiState(ListUiState.Content(rows, isRefreshing = true), FavoritesFailure.Partial(listOf("missing")))
        compose.setContent { TeamCityTheme { FavoritesScreen(state, {}, { retries++ }, {}, {}, {}) } }
        compose.onNodeWithText("Retry").assertIsNotEnabled()
        assertEquals(0, retries)
    }

    @Test fun allFailedShowsSavedFavoritesMessageInsteadOfEmpty() {
        compose.setContent { TeamCityTheme { FavoritesScreen(FavoritesUiState(ListUiState.Error, FavoritesFailure.AllFailed), {}, {}, {}, {}, {}) } }
        compose.onNodeWithText("Couldn't load favorites. Your saved favorites are kept.").assertIsDisplayed()
        compose.onNodeWithText("No favorite configurations added").assertDoesNotExist()
    }

    @Test fun drawerActionHasAccessibleLabel() {
        var opens = 0
        compose.setContent { TeamCityTheme { FavoritesScreen(FavoritesUiState(ListUiState.Empty()), {}, {}, { opens++ }, {}, {}) } }
        compose.onNodeWithContentDescription("Open navigation drawer").performClick()
        assertEquals(1, opens)
        compose.onNodeWithText("No favorite configurations added").assertIsDisplayed()
    }
}
