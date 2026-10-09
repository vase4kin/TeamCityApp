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

package teamcityapp.features.properties.impl

import android.app.Application
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
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
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import teamcityapp.features.properties.api.Property
import teamcityapp.libraries.theme.TeamCityTheme

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "en-rUS-w360dp-h800dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class PropertiesScreenTest {
    @get:Rule val compose = createComposeRule()

    @Test fun nonEmptyValueCopiesOriginalNameAndValue() {
        val property = Property("sdk", "24")
        var copied: Property? = null
        compose.setContent { TeamCityTheme { PropertiesScreen(PropertiesUiState.Content(listOf(property)), { copied = it }) } }
        compose.onNodeWithTag("properties:row:0").performClick()
        assertEquals(property, copied)
    }

    @Test fun emptyValueIsLabeledAndCannotCopy() {
        compose.setContent { TeamCityTheme { PropertiesScreen(PropertiesUiState.Content(listOf(Property("sdk", ""))), { fail("Empty value must not open copy") }) } }
        compose.onNodeWithText("Empty").assertIsDisplayed()
        compose.onNodeWithTag("properties:row:0").assertHasNoClickAction()
    }

    @Test fun rowsInheritColorsAndTypographyFromTheProvidedTheme() {
        compose.setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(onSurface = Color.Red, onSurfaceVariant = Color.Blue),
                typography = Typography(
                    bodyMedium = TextStyle(fontSize = 22.sp),
                    bodyLarge = TextStyle(fontSize = 30.sp)
                )
            ) {
                PropertiesScreen(
                    PropertiesUiState.Content(listOf(Property("sdk", "24"), Property("env.CI", ""))),
                    {}
                )
            }
        }
        assertTextStyle("sdk", 22, Color.Blue)
        assertTextStyle("24", 30, Color.Red)
        assertTextStyle("Empty", 30, Color.Blue)
    }

    @Test fun emptyMessageInheritsColorsAndTypographyFromTheProvidedTheme() {
        compose.setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(onSurfaceVariant = Color.Blue),
                typography = Typography(bodyLarge = TextStyle(fontSize = 30.sp))
            ) {
                PropertiesScreen(PropertiesUiState.Empty, {})
            }
        }
        val message = RuntimeEnvironment.getApplication().getString(R.string.empty_list_message_parameters)
        assertTextStyle(message, 30, Color.Blue)
    }

    @Test fun longPreviewCopiesOriginalValueAndExpansionDoesNotCopy() {
        val property = Property("env.description", "  https://server/path?x=1\n" + List(20) { "Technical value $it" }.joinToString("\n") + "  ")
        val copied = mutableListOf<Property>()
        compose.setContent { TeamCityTheme { PropertiesScreen(PropertiesUiState.Content(listOf(property)), { copied += it }) } }
        val preview = mutableListOf<TextLayoutResult>()
        compose.onNodeWithText(property.value, useUnmergedTree = true).performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(preview) }
        assertEquals(2, preview.single().lineCount)
        assertTrue(preview.single().hasVisualOverflow)
        compose.onNodeWithTag("properties:row:0").performClick()
        assertEquals(listOf(property), copied)
        compose.onNodeWithTag("properties:expand:0").performClick()
        assertEquals(listOf(property), copied)
        val full = mutableListOf<TextLayoutResult>()
        compose.onNodeWithTag("properties:full:0").performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(full) }
        assertTrue(full.single().lineCount >= 20)
        assertFalse(full.single().didOverflowHeight)
        compose.onNodeWithTag("properties:full:0").performTouchInput { click(Offset(10f, 10f)) }
        assertEquals(listOf(property), copied)
        compose.onNodeWithTag("properties:expand:0").performClick()
        compose.onNodeWithTag("properties:full:0").assertDoesNotExist()
        assertEquals(listOf(property), copied)
    }

    @Test fun expandedValueCanStillCollapseAfterStateRestoration() {
        val restoration = StateRestorationTester(compose)
        val property = Property("long", List(10) { "A long original value $it" }.joinToString("\n"))
        restoration.setContent { TeamCityTheme { PropertiesScreen(PropertiesUiState.Content(listOf(property)), {}) } }
        compose.onNodeWithTag("properties:expand:0").performClick()
        restoration.emulateSavedInstanceStateRestore()
        compose.onNodeWithTag("properties:full:0").assertExists()
        compose.onNodeWithTag("properties:expand:0").performClick()
        compose.onNodeWithTag("properties:full:0").assertDoesNotExist()
    }

    @Test fun fullWidthEdgesEachCopyExactlyOnceAndDuplicateNamesKeepOrder() {
        val properties = listOf(Property("same", " first "), Property("same", "https://server/raw?x=1&x=2"))
        val copied = mutableListOf<Property>()
        compose.setContent { TeamCityTheme { PropertiesScreen(PropertiesUiState.Content(properties), { copied += it }) } }
        compose.onNodeWithTag("properties:row:0").performTouchInput { click(Offset(1f, centerY)) }
        compose.onNodeWithTag("properties:row:1").performTouchInput { click(Offset(width - 1f, centerY)) }
        assertEquals(properties, copied)
    }

    // SDK 27 avoids Robolectric's unimplemented native Magnifier surface.
    @Test
    @Config(sdk = [27])
    fun tappingTheValueCopiesOnceWhileLongPressSelectionDoesNotCopy() {
        val property = Property("sdk", "original selectable value")
        val copied = mutableListOf<Property>()
        compose.setContent { TeamCityTheme { PropertiesScreen(PropertiesUiState.Content(listOf(property)), { copied += it }) } }
        compose.onNodeWithText(property.value, useUnmergedTree = true).performTouchInput { click() }
        assertEquals(listOf(property), copied)
        compose.onNodeWithText(property.value, useUnmergedTree = true).performTouchInput { longClick() }
        assertEquals(listOf(property), copied)
    }

    private fun assertTextStyle(text: String, fontSize: Int, color: Color) {
        val results = mutableListOf<TextLayoutResult>()
        compose.onNodeWithText(text, useUnmergedTree = true).performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(results) }
        assertEquals(fontSize.sp, results.single().layoutInput.style.fontSize)
        assertEquals(color, results.single().layoutInput.style.color)
    }

    @Test fun longListScrollsToLastParameterAndKeepsItsAction() {
        val properties = (0..50).map { Property("name$it", "value$it") }
        var copied: Property? = null
        compose.setContent { TeamCityTheme { PropertiesScreen(PropertiesUiState.Content(properties), { copied = it }) } }
        compose.onNodeWithTag("properties:list").performScrollToIndex(50)
        compose.onNodeWithTag("properties:row:50").performClick()
        assertEquals(properties.last(), copied)
    }
}
