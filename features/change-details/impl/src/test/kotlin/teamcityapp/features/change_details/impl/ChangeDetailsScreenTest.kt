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
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.StateRestorationTester
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

    @Test fun appBarStaysPinnedAndChangesColorOnlyWhileScrolled() {
        var flat = androidx.compose.ui.graphics.Color.Unspecified
        var raised = androidx.compose.ui.graphics.Color.Unspecified
        compose.setContent {
            TeamCityTheme {
                flat = androidx.compose.material3.MaterialTheme.colorScheme.surface
                raised = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainer
                ChangeDetailsScreen(ChangeDetailsUiState.Content(fixture.copy(files = (1..60).map { ChangedFile("File $it.kt", "changed") })), {}, { _, _ -> }, {})
            }
        }
        val bar = compose.onNodeWithTag("screen:app-bar")
        val bounds = bar.fetchSemanticsNode().boundsInRoot
        fun assertBar(expected: androidx.compose.ui.graphics.Color) {
            compose.mainClock.advanceTimeBy(1_000)
            compose.waitForIdle()
            bar.assertIsDisplayed()
            assertEquals(bounds, bar.fetchSemanticsNode().boundsInRoot)
            val pixels = bar.captureToImage().toPixelMap()
            assertEquals(expected, pixels[pixels.width - 4, pixels.height / 2])
        }
        assertBar(flat)
        val list = compose.onNodeWithTag("change_details:list")
        list.performTouchInput { swipeUp(startY = height * .65f, endY = height * .35f, durationMillis = 1_000) }
        assertBar(raised)
        repeat(3) { list.performTouchInput { swipeDown(startY = height * .35f, endY = height * .65f, durationMillis = 1_000) } }
        assertBar(flat)
    }

    @Test fun cardAndFilesRenderAndPlatformActionsReceiveExactValues() {
        var url: String? = null
        var diff: Pair<String, String>? = null
        var closed = false
        compose.setContent { TeamCityTheme { ChangeDetailsScreen(ChangeDetailsUiState.Content(fixture), { url = it }, { id, name -> diff = id to name }, { closed = true }) } }
        compose.onNodeWithText(fixture.comment).assertIsDisplayed()
        compose.onNodeWithText("Show more").assertDoesNotExist()
        compose.onNodeWithText("Show less").assertDoesNotExist()
        compose.onNodeWithText(fixture.revision).assertIsDisplayed()
        compose.onNodeWithText("By Developer on 01 Oct 2026").assertIsDisplayed()
        compose.onNodeWithText("Changed files (3)").assertIsDisplayed()
        compose.onNodeWithText("More details").performClick()
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

    @Test fun longCommentExpandsAndCollapsesWithoutTriggeringBrowserAction() {
        val comment = (1..6).joinToString("\n") { "Commit comment line $it" }
        var browserOpens = 0
        compose.setContent { TeamCityTheme { ChangeDetailsScreen(ChangeDetailsUiState.Content(fixture.copy(comment = comment)), { browserOpens++ }, { _, _ -> }, {}) } }
        val collapsed = commentLayout(comment)
        assertEquals(3, collapsed.lineCount)
        assertTrue(collapsed.isLineEllipsized(2))
        compose.onNodeWithText("More details").assertIsDisplayed()
        compose.onNodeWithText("Show more").performClick()
        val expanded = commentLayout(comment)
        assertEquals(6, expanded.lineCount)
        assertFalse(expanded.hasVisualOverflow)
        compose.onNodeWithText("Show more").assertDoesNotExist()
        compose.onNodeWithText("Show less").performScrollTo().performClick()
        assertEquals(3, commentLayout(comment).lineCount)
        compose.onNodeWithText("Show more").assertIsDisplayed()
        assertEquals(0, browserOpens)
    }

    @Test fun expandedCommentSurvivesRestorationAndResetsForAnotherComment() {
        val comment = (1..6).joinToString("\n") { "Commit comment line $it" }
        val state = mutableStateOf(fixture.copy(comment = comment))
        val restoration = StateRestorationTester(compose)
        restoration.setContent { TeamCityTheme { ChangeDetailsScreen(ChangeDetailsUiState.Content(state.value), {}, { _, _ -> }, {}) } }
        compose.onNodeWithText("Show more").performClick()
        restoration.emulateSavedInstanceStateRestore()
        assertEquals(6, commentLayout(comment).lineCount)
        compose.onNodeWithText("Show less").performScrollTo().performClick()
        assertEquals(3, commentLayout(comment).lineCount)
        compose.onNodeWithText("Show more").performClick()
        compose.runOnIdle { state.value = state.value.copy(id = "another-change") }
        assertEquals(3, commentLayout(comment).lineCount)
        compose.onNodeWithText("Show more").performClick()
        val replacement = "$comment\nAnother comment line"
        compose.runOnIdle { state.value = state.value.copy(comment = replacement) }
        assertEquals(3, commentLayout(replacement).lineCount)
        compose.onNodeWithText("Show more").assertIsDisplayed()
        compose.onNodeWithText("Show less").assertDoesNotExist()
    }

    @Test fun expansionDependsOnRenderedOverflowIncludingWrappedComments() {
        val state = mutableStateOf(fixture.copy(comment = "First line\nSecond line\nThird line"))
        compose.setContent { TeamCityTheme { ChangeDetailsScreen(ChangeDetailsUiState.Content(state.value), {}, { _, _ -> }, {}) } }
        assertEquals(3, commentLayout(state.value.comment).lineCount)
        compose.onNodeWithText("Show more").assertDoesNotExist()
        val wrapped = List(30) { "A long comment without explicit line breaks" }.joinToString(" ")
        compose.runOnIdle { state.value = state.value.copy(comment = wrapped) }
        assertEquals(3, commentLayout(wrapped).lineCount)
        compose.onNodeWithText("Show more").performClick()
        assertTrue(commentLayout(wrapped).lineCount > 3)
        assertFalse(commentLayout(wrapped).hasVisualOverflow)
        compose.onNodeWithText("Show less").performScrollTo().performClick()
        compose.runOnIdle { state.value = state.value.copy(comment = "Short comment") }
        compose.onNodeWithText("Show more").assertDoesNotExist()
        compose.onNodeWithText("Show less").assertDoesNotExist()
    }

    private fun commentLayout(comment: String): TextLayoutResult {
        val results = mutableListOf<TextLayoutResult>()
        compose.onNodeWithText(comment, useUnmergedTree = true).performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(results) }
        return results.single()
    }

    @Test fun tappingLongCommentTogglesExpansionWhileShortCommentIsNotClickable() {
        val comment = (1..6).joinToString("\n") { "Commit comment line $it" }
        val state = mutableStateOf(fixture.copy(comment = comment))
        var browserOpens = 0
        compose.setContent { TeamCityTheme { ChangeDetailsScreen(ChangeDetailsUiState.Content(state.value), { browserOpens++ }, { _, _ -> }, {}) } }
        val title = compose.onNodeWithText(comment)
        title.assertHasClickAction().performTouchInput { click() }
        assertEquals(6, commentLayout(comment).lineCount)
        compose.onNodeWithText("Show less").assertIsDisplayed()
        title.performTouchInput { click() }
        assertEquals(3, commentLayout(comment).lineCount)
        compose.onNodeWithText("Show more").assertIsDisplayed()
        compose.runOnIdle { state.value = state.value.copy(comment = "Short comment") }
        compose.onNodeWithText("Short comment").assertHasNoClickAction()
        assertEquals(0, browserOpens)
    }

    @Test fun duplicateNamesRetainSeparateActionsAndTypes() {
        val data = fixture.copy(files = listOf(ChangedFile("same.kt", "added"), ChangedFile("same.kt", "removed")))
        val selected = mutableListOf<String>()
        compose.setContent { TeamCityTheme { ChangeDetailsScreen(ChangeDetailsUiState.Content(data), {}, { _, name -> selected += name }, {}) } }
        compose.onNodeWithTag("change_details:file:0").performClick()
        compose.onNodeWithTag("change_details:file:1").performClick()
        assertEquals(listOf("same.kt", "same.kt"), selected)
        compose.onNodeWithText("added").assertIsDisplayed()
        compose.onNodeWithText("removed").assertIsDisplayed()
    }

    @Test fun contentUsesProvidedColorsAndTypography() {
        compose.setContent {
            MaterialTheme(colorScheme = lightColorScheme(onSurface = Color.Red, onSurfaceVariant = Color.Blue), typography = Typography(bodyLarge = TextStyle(fontSize = 22.sp), bodyMedium = TextStyle(fontSize = 18.sp), headlineSmall = TextStyle(fontSize = 28.sp))) {
                ChangeDetailsScreen(ChangeDetailsUiState.Content(fixture), {}, { _, _ -> }, {})
            }
        }
        assertThemeText(fixture.comment, 28, Color.Red)
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
