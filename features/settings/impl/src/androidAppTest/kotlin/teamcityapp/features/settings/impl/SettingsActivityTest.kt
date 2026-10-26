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
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.vase4kin.teamcityapp.TeamCityApplicationBase
import com.github.vase4kin.teamcityapp.di.modules.Mocks
import com.github.vase4kin.teamcityapp.helper.DeviceEvidence
import com.github.vase4kin.teamcityapp.helper.HiltApiTestRule
import com.github.vase4kin.teamcityapp.helper.TestUtils
import com.github.vase4kin.teamcityapp.navigation.IncomingDestinationResolver
import com.github.vase4kin.teamcityapp.navigation.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import teamcityapp.features.about.api.navigation.AboutDestination
import teamcityapp.features.build_details.api.*
import teamcityapp.features.build_history.api.navigation.BuildHistoryDestination
import teamcityapp.features.create_account.api.navigation.CreateAccountDestination
import teamcityapp.features.home.api.*
import teamcityapp.features.login.api.navigation.LoginDestination
import teamcityapp.features.manage_accounts.api.navigation.ManageAccountsDestination
import teamcityapp.libraries.app_theme.ThemeMode
import teamcityapp.libraries.builds.BuildCollectionLink
import teamcityapp.libraries.data.builds.BuildLaunchMapper
import teamcityapp.libraries.navigation.DestinationRegistry
import teamcityapp.libraries.theme.teamCityColorScheme

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

    @Inject lateinit var registry: DestinationRegistry

    @Before fun before() {
        TestUtils.disableOnboarding()
        app.appInjector.cacheManager().evictAllCache()
        app.appInjector.sharedUserStorage().clearAll()
        app.appInjector.sharedUserStorage().saveGuestUserAccountAndSetItAsActive(com.github.vase4kin.teamcityapp.di.modules.Mocks.URL, false)
        runBlocking { repo.setTheme(ThemeMode.Light) }
    }

    @Test fun themeChangesPreserveOtherScreensAndRetainedPanes() {
        val build = BuildLaunchMapper().toLaunchData(Mocks.failedBuild()).copy(snapshotDependencies = BuildCollectionLink(null))
        val screens = listOf(
            HomeDestination(HomeTab.Projects) to "home:tab:Projects",
            HomeDestination(HomeTab.Favorites) to "home:tab:Favorites",
            HomeDestination(HomeTab.Running) to "home:tab:Running",
            HomeDestination(HomeTab.Queue) to "home:tab:Queue",
            HomeDestination(HomeTab.Agents) to "home:tab:Agents",
            AboutDestination to "about:section",
            ManageAccountsDestination to "accounts:list",
            LoginDestination to "login:form",
            CreateAccountDestination to "create-account:form",
            BuildHistoryDestination("id", "Theme regression") to "history:body",
            BuildDetailsDestination(build) to "overview:list",
            BuildDetailsSectionDestination(build, BuildDetailsSection.Changes) to "changes:list",
            BuildDetailsSectionDestination(build, BuildDetailsSection.Tests) to "tests:list",
            BuildDetailsSectionDestination(build, BuildDetailsSection.Parameters) to "properties:list",
            BuildDetailsSectionDestination(build, BuildDetailsSection.Artifacts) to "artifacts:list",
            BuildDetailsSectionDestination(build, BuildDetailsSection.Snapshots) to "snapshot:list"
        )
        screens.forEach { (destination, tag) ->
            runBlocking { repo.setTheme(ThemeMode.Light) }
            val intent = Intent(app, MainActivity::class.java).putExtra(MainActivity.EXTRA_DESTINATION, registry.encodeDestination(destination))
            ActivityScenario.launch<MainActivity>(intent).use { scenario ->
                try {
                    compose.waitUntil(10_000) { compose.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty() }
                } catch (error: ComposeTimeoutException) {
                    throw AssertionError("Screen did not open: $destination ($tag)", error)
                }
                var original: MainActivity? = null
                scenario.onActivity { original = it }
                val snapshot = original!!.navigationSnapshot
                assertEquals(destination, snapshot!!.entries.last().destination)
                if (destination is HomeDestination) compose.onNodeWithTag(tag).assertIsSelected()
                val bounds = compose.onAllNodesWithTag(tag)[0].fetchSemanticsNode().boundsInRoot
                listOf(ThemeMode.Dark, ThemeMode.Light).forEach { mode ->
                    runBlocking { repo.setTheme(mode) }
                    compose.waitUntil(5_000) { AppCompatDelegate.getDefaultNightMode() == if (mode == ThemeMode.Dark) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO }
                    assertRenderedTheme(mode == ThemeMode.Dark)
                    scenario.onActivity {
                        assertSame("$destination must retain its host", original, it)
                        assertEquals("$destination must retain its visits", snapshot, it.navigationSnapshot)
                    }
                    assertEquals("$destination must keep its layout", bounds, compose.onAllNodesWithTag(tag)[0].fetchSemanticsNode().boundsInRoot)
                    if (destination is HomeDestination) compose.onNodeWithTag(tag).assertIsSelected()
                }
            }
        }
    }

    @After fun after() {
        runBlocking { repo.setTheme(ThemeMode.System) }
    }
    private fun launch() = ActivityScenario.launch<MainActivity>(Intent().setClassName(app, IncomingDestinationResolver.SETTINGS_COMPONENT))
    private fun awaitSummary(text: String) {
        compose.waitUntil(5_000) { compose.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText(text).assertIsDisplayed()
    }

    private fun assertRenderedTheme(dark: Boolean) {
        compose.waitForIdle()
        val expected = teamCityColorScheme(dark).background
        if (Build.VERSION.SDK_INT >= 26) {
            val pixels = compose.onRoot().captureToImage().toPixelMap()
            assertEquals(expected, pixels[1, pixels.height / 2])
            return
        }
        // Window PixelCopy needs API 26. Older display captures may see the
        // preceding frame, so require the exact requested color within a bound.
        compose.waitUntil(5_000) {
            // Material tooltips own a second, small Compose root on older Android.
            val bounds = compose.onAllNodes(isRoot()).fetchSemanticsNodes()
                .maxBy { it.boundsInWindow.width * it.boundsInWindow.height }.boundsInWindow
            val bitmap = checkNotNull(InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot())
            try {
                val x = (bounds.left.toInt() + 1).coerceIn(0, bitmap.width - 1)
                val y = (bounds.top + bounds.height / 2).toInt().coerceIn(0, bitmap.height - 1)
                Color(bitmap.getPixel(x, y)) == expected
            } finally {
                bitmap.recycle()
            }
        }
    }

    @Test fun repeatedThemeSelectionsRecolorImmediatelyWithoutReplacingTheScreen() {
        launch().use { scenario ->
            awaitSummary("Light theme")
            var original: MainActivity? = null
            scenario.onActivity { original = it }
            val snapshot = original!!.navigationSnapshot
            assertRenderedTheme(false)
            repeat(2) {
                listOf(ThemeMode.Dark, ThemeMode.Light).forEach { mode ->
                    val dark = mode == ThemeMode.Dark
                    compose.onNodeWithTag("settings:option:$mode").performClick()
                    compose.waitUntil(5_000) { runBlocking { repo.theme.first() } == mode }
                    compose.waitUntil(5_000) { compose.onAllNodesWithTag("settings:saving").fetchSemanticsNodes().isEmpty() }
                    compose.onNodeWithTag("settings:option:$mode").assertIsSelected()
                    assertRenderedTheme(dark)
                    scenario.onActivity {
                        assertSame("Theme changes must keep the activity alive", original, it)
                        assertEquals(snapshot, it.navigationSnapshot)
                        val bars = WindowCompat.getInsetsController(it.window, it.window.decorView)
                        assertEquals(!dark, bars.isAppearanceLightStatusBars)
                        assertEquals(Build.VERSION.SDK_INT >= 26 && !dark, bars.isAppearanceLightNavigationBars)
                    }
                }
            }
        }
    }

    @Test fun selectingThemePersistsAppliesAndSurvivesRecreation() {
        launch().use { scenario ->
            awaitSummary("Light theme")
            DeviceEvidence.capture("android_settings_light")
            compose.onNodeWithText("Dark theme").performClick()
            awaitSummary("Dark theme")
            compose.waitUntil(5_000) { AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES }
            scenario.onActivity { assertEquals(Configuration.UI_MODE_NIGHT_YES, it.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) }
            assertEquals(ThemeMode.Dark, runBlocking { repo.theme.first() })
            DeviceEvidence.capture("android_settings_dark")
            scenario.recreate()
            awaitSummary("Dark theme")
            scenario.moveToState(Lifecycle.State.CREATED)
            scenario.moveToState(Lifecycle.State.RESUMED)
            awaitSummary("Dark theme")
        }
    }

    @Test fun selectedChoiceSurvivesConfigurationChange() {
        launch().use { scenario ->
            awaitSummary("Light theme")
            compose.onNodeWithTag("settings:option:Light").assertIsSelected()
            scenario.recreate()
            awaitSummary("Light theme")
            compose.onNodeWithTag("settings:option:Light").assertIsSelected()
            compose.onNodeWithText("Dark theme").performClick()
            awaitSummary("Dark theme")
        }
    }

    @Test fun backWithoutSelectingKeepsSavedThemeAndClosesActivity() {
        launch().use { scenario ->
            awaitSummary("Light theme")
            assertEquals(ThemeMode.Light, runBlocking { repo.theme.first() })
            compose.onNodeWithContentDescription("Back").performClick()
            compose.waitUntil(5_000) { scenario.state == Lifecycle.State.DESTROYED }
        }
    }

    @Test fun savedThemeIsAppliedToOtherComposeActivitiesBeforeOpeningSettings() {
        runBlocking { repo.setTheme(ThemeMode.Dark) }
        compose.waitUntil(5_000) { AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES }
        ActivityScenario.launch<MainActivity>(Intent().setClassName(app, IncomingDestinationResolver.ABOUT_COMPONENT)).use { scenario ->
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
