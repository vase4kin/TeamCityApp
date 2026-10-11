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

package teamcityapp.features.test_details.impl

import android.app.Application
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
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
import teamcityapp.libraries.theme.TeamCityTheme

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "en-rUS-w360dp-h800dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TestDetailsScreenTest {
    @get:Rule val compose = createComposeRule()

    @Test fun rendersLiteralHtmlWithoutInterpretingIt() {
        val details = "<script>alert('test')</script> & expected <true>"
        compose.setContent { TeamCityTheme { TestDetailsScreen(TestDetailsUiState.Content(details), {}, {}) } }
        compose.onNodeWithText(details).assertIsDisplayed()
        compose.onNodeWithText("Details").assertIsDisplayed()
    }

    @Test fun retryAndCloseInvokeTheirCallbacks() {
        var retried = 0
        var closed = 0
        compose.setContent { TeamCityTheme { TestDetailsScreen(TestDetailsUiState.Error, { retried++ }, { closed++ }) } }
        compose.onNodeWithText("Try again").performClick()
        compose.onNodeWithContentDescription("Close").performClick()
        assertEquals(1, retried)
        assertEquals(1, closed)
    }

    @Test fun stateChangeRemovesStaleContentAndRetry() {
        val state = androidx.compose.runtime.mutableStateOf<TestDetailsUiState>(TestDetailsUiState.Error)
        compose.setContent { TeamCityTheme { TestDetailsScreen(state.value, {}, {}) } }
        compose.onNodeWithText("Try again").assertIsDisplayed()
        compose.runOnIdle { state.value = TestDetailsUiState.Empty }
        compose.onNodeWithText("No test details").assertIsDisplayed()
        compose.onNodeWithText("Try again").assertDoesNotExist()
    }

    @Test fun contentUsesProvidedColorsAndTypography() {
        compose.setContent {
            MaterialTheme(colorScheme = lightColorScheme(onSurface = Color.Red, onSurfaceVariant = Color.Blue), typography = Typography(bodyLarge = TextStyle(fontSize = 22.sp), bodyMedium = TextStyle(fontSize = 18.sp), headlineSmall = TextStyle(fontSize = 28.sp))) {
                TestDetailsScreen(TestDetailsUiState.Content("Theme-aware output"), {}, {})
            }
        }
        assertThemeText("Theme-aware output", 18, Color.Red)
    }

    @Test fun errorUsesProvidedColorsAndTypography() {
        compose.setContent {
            MaterialTheme(colorScheme = lightColorScheme(onSurface = Color.Red, onSurfaceVariant = Color.Blue), typography = Typography(bodyMedium = TextStyle(fontSize = 18.sp))) {
                TestDetailsScreen(TestDetailsUiState.Error, {}, {})
            }
        }
        val message = org.robolectric.RuntimeEnvironment.getApplication()
            .getString(teamcityapp.libraries.theme.R.string.error_load_message)
        assertThemeText(message, 18, Color.Blue)
    }

    private fun assertThemeText(text: String, fontSize: Int, color: Color) {
        val results = mutableListOf<TextLayoutResult>()
        compose.onAllNodesWithText(text, useUnmergedTree = true)[0]
            .performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(results) }
        assertEquals(fontSize.sp, results.single().layoutInput.style.fontSize)
        assertEquals(color, results.single().layoutInput.style.color)
    }
}
