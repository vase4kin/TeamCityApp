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

package teamcityapp.features.settings.impl

import android.app.Application
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import teamcityapp.libraries.app_theme.*
import teamcityapp.libraries.theme.TeamCityTheme

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "en-rUS-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class SettingsScreenTest {
    @get:Rule val compose = createComposeRule()
    private val content = SettingsUiState.Content(ThemeMode.System,ThemeOptions.forSdk(35).modes)
    @Test fun themeRowOpensDialogAndBackClosesScreen() {
        var opened=false;var closed=false
        compose.setContent { TeamCityTheme { SettingsScreen(content, {}, {}, {}, {closed=true}, onOpenDialog={opened=true}) } }
        compose.onNodeWithText("General").assertIsDisplayed();compose.onNodeWithText("Follow system").assertIsDisplayed()
        compose.onNodeWithTag("settings:theme").performClick();assertTrue(opened)
        compose.onNodeWithContentDescription("Back").performClick();assertTrue(closed)
    }
    @Test fun dialogSelectsImmediatelyAndDismisses() {
        val selected=mutableListOf<ThemeMode>();var dismissals=0
        compose.setContent { TeamCityTheme { SettingsScreen(content, {selected+=it}, {}, {}, {}, dialogOpen=true, onDismissDialog={dismissals++}) } }
        compose.onNode(isDialog()).assertExists()
        compose.onNode(hasText("Follow system") and isSelected()).assertExists()
        compose.onNodeWithText("Dark theme").performClick()
        assertEquals(listOf(ThemeMode.Dark),selected);assertEquals(1,dismissals)
    }
    @Test fun cancellingDialogDoesNotChangePreference() {
        val selected=mutableListOf<ThemeMode>();var dismissed=false
        compose.setContent { TeamCityTheme { SettingsScreen(content, {selected+=it}, {}, {}, {}, dialogOpen=true, onDismissDialog={dismissed=true}) } }
        compose.onNodeWithText("CANCEL").performClick();assertTrue(dismissed);assertTrue(selected.isEmpty())
    }
    @Test fun savingPreventsAnotherDialogAndShowsProgress() {
        var opened=false
        compose.setContent { TeamCityTheme { SettingsScreen(content.copy(saving=true), {}, {}, {}, {}, onOpenDialog={opened=true}) } }
        compose.onNodeWithTag("settings:theme").assertIsNotEnabled().performClick();assertFalse(opened)
        compose.onNodeWithTag("settings:saving").assertIsDisplayed()
    }
    @Test fun readFailureAndSaveFailureExposeSeparateRetries() {
        var retries=0
        compose.setContent { TeamCityTheme { SettingsScreen(SettingsUiState.Error, {}, {retries++}, {}, {}) } }
        compose.onNodeWithText("Unable to load theme settings").assertIsDisplayed();compose.onNodeWithText("Retry").performClick();assertEquals(1,retries)
    }
    @Test fun saveFailureKeepsCurrentSummaryAndOffersRetry() {
        var retries=0
        compose.setContent { TeamCityTheme { SettingsScreen(content.copy(saveFailed=true), {}, {}, {retries++}, {}) } }
        compose.onNodeWithText("Follow system").assertIsDisplayed();compose.onNodeWithText("Retry").performClick();assertEquals(1,retries)
    }
    @Test fun savedChoiceNoLongerInSdkOptionsUsesLegacyNotSetSummary() {
        compose.setContent { TeamCityTheme { SettingsScreen(content.copy(selected=ThemeMode.AutoBattery), {}, {}, {}, {}) } }
        compose.onNodeWithText("Not set").assertIsDisplayed()
    }
    @Test fun olderAndroidShowsOnlySupportedDialogChoices() {
        compose.setContent { TeamCityTheme { SettingsScreen(content.copy(selected=ThemeMode.Light,options=ThemeOptions.forSdk(24).modes), {}, {}, {}, {}, dialogOpen=true) } }
        compose.onNode(hasText("Light theme") and isSelected()).assertExists();compose.onNodeWithText("Dark theme").assertExists()
        compose.onNodeWithText("Auto battery").assertDoesNotExist()
        compose.onAllNodesWithText("Follow system").assertCountEquals(0)
    }
    @Test fun preferenceUsesProvidedColorsAndTypography() {
        compose.setContent {
            MaterialTheme(colorScheme = lightColorScheme(onSurface = Color.Red, onSurfaceVariant = Color.Blue), typography = Typography(bodyLarge = TextStyle(fontSize = 22.sp), bodyMedium = TextStyle(fontSize = 18.sp), headlineSmall = TextStyle(fontSize = 28.sp))) {
                SettingsScreen(content, {}, {}, {}, {})
            }
        }
        assertThemeText("Theme", 22, Color.Red)
        assertThemeText("Follow system", 18, Color.Blue)
    }

    @Test fun themeDialogUsesProvidedTypography() {
        compose.setContent {
            MaterialTheme(colorScheme = lightColorScheme(onSurface = Color.Red, onSurfaceVariant = Color.Blue), typography = Typography(bodyLarge = TextStyle(fontSize = 22.sp), bodyMedium = TextStyle(fontSize = 18.sp), headlineSmall = TextStyle(fontSize = 28.sp))) {
                SettingsScreen(content, {}, {}, {}, {}, dialogOpen = true)
            }
        }
        assertThemeText("Dark theme", 22, Color.Red)
    }

    private fun assertThemeText(text: String, fontSize: Int, color: Color) {
        val results = mutableListOf<TextLayoutResult>()
        compose.onAllNodesWithText(text, useUnmergedTree = true)[0]
            .performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(results) }
        assertEquals(fontSize.sp, results.single().layoutInput.style.fontSize)
        assertEquals(color, results.single().layoutInput.style.color)
    }

}
