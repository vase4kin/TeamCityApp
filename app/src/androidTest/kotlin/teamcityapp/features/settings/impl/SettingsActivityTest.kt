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

import android.content.Intent
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.vase4kin.teamcityapp.TeamCityApplicationBase
import com.github.vase4kin.teamcityapp.helper.HiltApiTestRule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import teamcityapp.features.about.impl.AboutActivity
import teamcityapp.libraries.app_theme.ThemeMode

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class SettingsActivityTest {
    @JvmField
    @Rule(order = 0)
    val hilt = HiltAndroidRule(this)

    @JvmField
    @Rule(order = 1)
    val api = HiltApiTestRule(hilt)

    @JvmField
    @Rule(order = 2)
    val compose = createEmptyComposeRule()
    private val app get() = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TeamCityApplicationBase
    private val repo get() = app.appInjector.themePreferences()

    @Before fun before() {
        app.appInjector.sharedUserStorage().clearAll()
        app.appInjector.sharedUserStorage().saveGuestUserAccountAndSetItAsActive(com.github.vase4kin.teamcityapp.dagger.modules.Mocks.URL, false)
        runBlocking { repo.setTheme(ThemeMode.Light) }
    }

    @After fun after() {
        runBlocking { repo.setTheme(ThemeMode.System) }
    }
    private fun launch() = ActivityScenario.launch<SettingsActivity>(Intent(app, SettingsActivity::class.java))
    private fun awaitSummary(text: String) {
        compose.waitUntil(5_000) { compose.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText(text).assertIsDisplayed()
    }

    @Test fun selectingThemePersistsAppliesAndSurvivesRecreation() {
        launch().use { scenario ->
            awaitSummary("Light theme")
            compose.onNodeWithTag("settings:theme").performClick()
            compose.onNodeWithText("Dark theme").performClick()
            awaitSummary("Dark theme")
            compose.waitUntil(5_000) { AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES }
            scenario.onActivity { assertEquals(Configuration.UI_MODE_NIGHT_YES, it.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) }
            assertEquals(ThemeMode.Dark, runBlocking { repo.theme.first() })
            scenario.recreate()
            awaitSummary("Dark theme")
            scenario.moveToState(Lifecycle.State.CREATED)
            scenario.moveToState(Lifecycle.State.RESUMED)
            awaitSummary("Dark theme")
        }
    }

    @Test fun openDialogSurvivesConfigurationChange() {
        launch().use { scenario ->
            awaitSummary("Light theme")
            compose.onNodeWithTag("settings:theme").performClick()
            compose.onNodeWithTag("settings:dialog").assertIsDisplayed()
            scenario.recreate()
            compose.waitUntil(5_000) { compose.onAllNodesWithTag("settings:dialog").fetchSemanticsNodes().isNotEmpty() }
            compose.onNodeWithText("Dark theme").performClick()
            awaitSummary("Dark theme")
        }
    }

    @Test fun cancelKeepsSavedThemeAndBackClosesActivity() {
        launch().use { scenario ->
            awaitSummary("Light theme")
            compose.onNodeWithTag("settings:theme").performClick()
            compose.onNodeWithText("CANCEL").performClick()
            compose.onNodeWithTag("settings:dialog").assertDoesNotExist()
            assertEquals(ThemeMode.Light, runBlocking { repo.theme.first() })
            compose.onNodeWithContentDescription("Back").performClick()
            compose.waitUntil(5_000) { scenario.state == Lifecycle.State.DESTROYED }
        }
    }

    @Test fun savedThemeIsAppliedToOtherComposeActivitiesBeforeOpeningSettings() {
        runBlocking { repo.setTheme(ThemeMode.Dark) }
        compose.waitUntil(5_000) { AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES }
        ActivityScenario.launch<AboutActivity>(Intent(app, AboutActivity::class.java)).use { scenario ->
            scenario.onActivity { assertEquals(Configuration.UI_MODE_NIGHT_YES, it.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) }
            scenario.recreate()
            scenario.onActivity { assertEquals(Configuration.UI_MODE_NIGHT_YES, it.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) }
        }
    }

    @Test fun savedThemeIsReadOnANewActivityLaunch() {
        launch().use { awaitSummary("Light theme") }
        runBlocking { repo.setTheme(ThemeMode.Dark) }
        launch().use { awaitSummary("Dark theme") }
    }
}
