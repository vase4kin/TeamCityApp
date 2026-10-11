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

package teamcityapp.features.changes.impl

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
import teamcityapp.features.change_details.api.ChangeDetails
import teamcityapp.libraries.list_state.ListUiState
import teamcityapp.libraries.theme.TeamCityTheme

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "en-rUS-w360dp-h800dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ChangesScreenTest {
    @get:Rule val compose = createComposeRule()

    @Test fun rowShowsChangeFieldsAndDispatchesCompleteDetails() {
        val detail = change().copy(comment = "  Keep the build queue responsive  ")
        var selected: ChangeDetails? = null
        compose.setContent {
            TeamCityTheme {
                ChangesScreen(ListUiState.Content(listOf(detail)), ChangesCountState.Available(1), 1, { ChangeRowUiState(detail) }, { detail.id }, ChangesAppendState.Idle, {}, {}, {}, { selected = it })
            }
        }
        compose.onNodeWithText("Keep the build queue responsive").assertIsDisplayed()
        compose.onNodeWithText("By john-117 on 30 Jul 16 00:36").assertIsDisplayed()
        compose.onNodeWithText("21312fsd1321").assertIsDisplayed()
        compose.onNodeWithText("1 changed file").assertIsDisplayed()
        compose.onNodeWithTag("changes:change:42").performClick()
        assertEquals(detail, selected)
    }

    @Test fun appendFailureKeepsRowsAndRetriesOnlyPagingWhileCountRetriesIndependently() {
        val detail = change()
        var pageRetries = 0
        var countRetries = 0
        compose.setContent {
            TeamCityTheme {
                ChangesScreen(ListUiState.Content(listOf(detail)), ChangesCountState.Unavailable, 1, { ChangeRowUiState(detail) }, { detail.id }, ChangesAppendState.Error, {}, { pageRetries++ }, { countRetries++ }, {})
            }
        }
        compose.onNodeWithText("Keep the build queue responsive").assertIsDisplayed()
        compose.onNodeWithText("Retry").performClick()
        assertEquals(1, pageRetries)
        assertEquals(0, countRetries)
        compose.onNodeWithText("Retry count").performClick()
        assertEquals(1, countRetries)
        assertEquals(1, pageRetries)
    }

    @Test fun emptyStateKeepsOptionalCountFailureVisible() {
        compose.setContent {
            TeamCityTheme {
                ChangesScreen(ListUiState.Empty(), ChangesCountState.Unavailable, 0, { null }, { it }, ChangesAppendState.Idle, {}, {}, {}, {})
            }
        }
        compose.onNodeWithText("No changes").assertIsDisplayed()
        compose.onNodeWithText("Couldn’t load the change count.").assertIsDisplayed()
        compose.onNodeWithTag("changes:list").assertDoesNotExist()
    }
}
