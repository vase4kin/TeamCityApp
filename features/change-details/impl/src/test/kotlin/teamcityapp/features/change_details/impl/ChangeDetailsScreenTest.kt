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

package teamcityapp.features.change_details.impl

import android.app.Application
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import teamcityapp.features.change_details.api.ChangedFile
import teamcityapp.libraries.theme.TeamCityTheme

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "w448dp-h997dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ChangeDetailsScreenTest {
    @get:Rule val compose = createComposeRule()

    @Test fun cardAndFilesRenderAndPlatformActionsReceiveExactValues() {
        var url: String? = null
        var diff: Pair<String, String>? = null
        var closed = false
        compose.setContent { TeamCityTheme { ChangeDetailsScreen(ChangeDetailsUiState.Content(fixture), { url = it }, { id, name -> diff = id to name }, { closed = true }) } }
        compose.onNodeWithText(fixture.comment).assertIsDisplayed()
        compose.onNodeWithText(fixture.revision).assertIsDisplayed()
        compose.onNodeWithText("By Developer on 01 Oct 2026").assertIsDisplayed()
        compose.onNodeWithText("Changed files (3)").assertIsDisplayed()
        compose.onNodeWithText("MORE DETAILS").performClick()
        assertEquals(fixture.webUrl, url)
        compose.onNodeWithTag("change_details:file:1").performClick()
        assertEquals(fixture.id to fixture.files[1].name, diff)
        compose.onNodeWithContentDescription("Close").performClick()
        assertTrue(closed)
    }

    @Test fun noFilesHidesDiffHintAndStateChangesRemovePreviousFiles() {
        val state = mutableStateOf<ChangeDetailsUiState>(ChangeDetailsUiState.Content(fixture))
        compose.setContent { TeamCityTheme { ChangeDetailsScreen(state.value, {}, { _, _ -> }, {}) } }
        compose.onNodeWithTag("change_details:file:0").assertExists()
        compose.runOnIdle { state.value = ChangeDetailsUiState.Content(fixture.copy(files = emptyList())) }
        compose.onNodeWithText("Changed files (0)").assertIsDisplayed()
        compose.onNodeWithText("Click on a file to view a diff").assertDoesNotExist()
        compose.onNodeWithTag("change_details:file:0").assertDoesNotExist()
        compose.runOnIdle { state.value = ChangeDetailsUiState.InvalidInput }
        compose.onNodeWithTag("change_details:list").assertDoesNotExist()
    }

    @Test fun duplicateNamesRetainSeparateActionsAndTypes() {
        val data = fixture.copy(files = listOf(ChangedFile("same.kt", "added"), ChangedFile("same.kt", "removed")))
        val selected = mutableListOf<String>()
        compose.setContent { TeamCityTheme { ChangeDetailsScreen(ChangeDetailsUiState.Content(data), {}, { _, name -> selected += name }, {}) } }
        compose.onNodeWithTag("change_details:file:0").performClick()
        compose.onNodeWithTag("change_details:file:1").performClick()
        assertEquals(listOf("same.kt", "same.kt"), selected)
        compose.onNodeWithText("ADDED").assertIsDisplayed()
        compose.onNodeWithText("REMOVED").assertIsDisplayed()
    }

    @Test fun scrollingHidesToolbarAndScrollingBackRestoresIt() {
        val data = fixture.copy(files = (0..60).map { ChangedFile("File $it.kt", "changed") })
        compose.setContent { TeamCityTheme { ChangeDetailsScreen(ChangeDetailsUiState.Content(data), {}, { _, _ -> }, {}) } }
        compose.onNodeWithText("Change details").assertIsDisplayed()
        compose.onNodeWithTag("change_details:list").performTouchInput { swipeUp() }
        compose.waitForIdle()
        compose.onNodeWithText("Change details").assertIsNotDisplayed()
        compose.onNodeWithTag("change_details:list").performTouchInput { swipeDown() }
        compose.waitForIdle()
        compose.onNodeWithText("Change details").assertIsDisplayed()
    }

    @Test fun contentUsesProvidedColorsAndTypography() {
        compose.setContent {
            MaterialTheme(colorScheme = lightColorScheme(onSurface = Color.Red, onSurfaceVariant = Color.Blue), typography = Typography(bodyLarge = TextStyle(fontSize = 22.sp), bodyMedium = TextStyle(fontSize = 18.sp), headlineSmall = TextStyle(fontSize = 28.sp))) {
                ChangeDetailsScreen(ChangeDetailsUiState.Content(fixture), {}, { _, _ -> }, {})
            }
        }
        assertThemeText(fixture.comment, 22, Color.Red)
        assertThemeText(fixture.revision, 18, Color.Red)
    }

    private fun assertThemeText(text: String, fontSize: Int, color: Color) {
        val results = mutableListOf<TextLayoutResult>()
        compose.onAllNodesWithText(text, useUnmergedTree = true)[0]
            .performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(results) }
        assertEquals(fontSize.sp, results.single().layoutInput.style.fontSize)
        assertEquals(color, results.single().layoutInput.style.color)
    }
}
