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

package teamcityapp.features.filter_builds.impl

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import teamcityapp.features.filter_builds.api.*
import teamcityapp.libraries.theme.TeamCityTheme

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "en-rUS-w360dp-h800dp-notnight-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class FilterBuildsScreenTest {
    @get:Rule val compose = createComposeRule()

    @Test fun queuedFilterHidesPinnedWithoutChangingTheSavedChoice() {
        val state = mutableStateOf(FilterBuildsUiState(branches = emptyList(), filter = BuildFilter(pinned = true)))
        compose.setContent { TeamCityTheme { FilterBuildsScreen(state.value, { state.value = state.value.copy(filter = it) }, {}, {}, {}, dialog = true) } }
        compose.onNodeWithText("Queued").performClick()
        compose.onNodeWithTag("filter-builds:pinned").assertDoesNotExist()
        assertTrue(state.value.filter.pinned)
        assertEquals(BuildStatusFilter.Queued, state.value.filter.status)
    }

    @Test fun singleBranchOffersTheAnyBranchDefault() {
        compose.setContent { TeamCityTheme { FilterBuildsScreen(FilterBuildsUiState(branches = listOf("main")), {}, {}, {}, {}) } }
        compose.onNodeWithTag("branches:input").assertDoesNotExist()
        compose.onNodeWithText("No branches available to filter").assertIsDisplayed()
    }
}
