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
import teamcityapp.libraries.app_theme.*
import teamcityapp.libraries.theme.TeamCityTheme

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "en-rUS-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class SettingsScreenTest {
    @get:Rule val compose = createComposeRule()
    private val content = SettingsUiState.Content(ThemeMode.System, ThemeOptions.forSdk(35).modes)

    @Test fun visibleThemeChoiceSelectsImmediatelyAndBackClosesScreen() {
        val selected = mutableListOf<ThemeMode>()
        var closed = false
        compose.setContent { TeamCityTheme { SettingsScreen(content, { selected += it }, {}, {}, { closed = true }) } }
        compose.onNodeWithTag("settings:option:System").assertIsSelected()
        compose.onNodeWithTag("settings:option:Dark").performScrollTo().performClick()
        assertEquals(listOf(ThemeMode.Dark), selected)
        compose.onNodeWithContentDescription("Back").performClick()
        assertTrue(closed)
    }

    @Test fun savingDisablesEveryChoiceAndShowsProgress() {
        compose.setContent { TeamCityTheme { SettingsScreen(content.copy(saving = true), { fail("No selection while saving") }, {}, {}, {}) } }
        content.options.forEach { compose.onNodeWithTag("settings:option:$it").assertIsNotEnabled().performClick() }
        compose.onNodeWithTag("settings:saving").assertIsDisplayed()
    }

    @Test fun readFailureAndSaveFailureExposeSeparateRetries() {
        var retries = 0
        compose.setContent { TeamCityTheme { SettingsScreen(SettingsUiState.Error, {}, { retries++ }, {}, {}) } }
        compose.onNodeWithText("Unable to load theme settings").assertIsDisplayed()
        compose.onNodeWithText("Try again").performClick()
        assertEquals(1, retries)
    }

    @Test fun saveFailureKeepsCurrentSummaryAndOffersRetry() {
        var retries = 0
        compose.setContent { TeamCityTheme { SettingsScreen(content.copy(saveFailed = true), {}, {}, { retries++ }, {}) } }
        compose.onNodeWithTag("settings:current").assertTextEquals("Current theme: Follow system")
        compose.onNodeWithText("Try again").performClick()
        assertEquals(1, retries)
    }

    @Test fun savedChoiceNoLongerInSdkOptionsUsesLegacyNotSetSummary() {
        compose.setContent { TeamCityTheme { SettingsScreen(content.copy(selected = ThemeMode.AutoBattery), {}, {}, {}, {}) } }
        compose.onNodeWithText("Not set").assertIsDisplayed()
    }

    @Test fun olderAndroidShowsOnlySupportedChoices() {
        val options = ThemeOptions.forSdk(24).modes
        compose.setContent { TeamCityTheme { SettingsScreen(content.copy(selected = ThemeMode.Light, options = options), {}, {}, {}, {}) } }
        options.forEach { compose.onNodeWithTag("settings:option:$it").assertExists() }
        compose.onNodeWithTag("settings:option:Light").assertIsSelected()
        compose.onNodeWithTag("settings:option:System").assertDoesNotExist()
    }
}
