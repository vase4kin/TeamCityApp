package teamcityapp.features.settings.view

import android.content.Intent
import android.content.res.Configuration
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.vase4kin.teamcityapp.TeamCityApplicationBase
import com.github.vase4kin.teamcityapp.helper.HiltApiTestRule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import teamcityapp.libraries.settings.ThemeMode

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class SettingsActivityTest {
    @JvmField @Rule(order = 0) val hiltRule = HiltAndroidRule(this)
    @JvmField @Rule(order = 1) val apiRule = HiltApiTestRule(hiltRule)
    @JvmField @Rule(order = 2) val compose = createEmptyComposeRule()
    private val app get() = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TeamCityApplicationBase
    private val repository get() = app.appInjector.settingsRepository()

    @Before fun setUp() = runBlocking { repository.setTheme(ThemeMode.LIGHT) }
    @After fun tearDown() = runBlocking { repository.setTheme(ThemeMode.SYSTEM) }
    private fun launch() = ActivityScenario.launch<SettingsActivity>(Intent(app, SettingsActivity::class.java))
    private fun awaitSelected(text: String) {
        compose.waitUntil(5_000) {
            compose.onAllNodes(hasText(text) and isSelected()).fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNode(hasText(text) and isSelected()).assertIsDisplayed()
    }

    @Test fun changesThemePersistsAndRecreatesWithoutLegacyFragments() {
        launch().use { scenario ->
            awaitSelected("Light theme")
            compose.onNodeWithText("Dark theme").performClick()
            awaitSelected("Dark theme")
            assertEquals(ThemeMode.DARK, runBlocking { repository.theme.first() })
            scenario.recreate()
            awaitSelected("Dark theme")
            scenario.onActivity { activity ->
                assertTrue(activity.supportFragmentManager.fragments.isEmpty())
                assertEquals(Configuration.UI_MODE_NIGHT_YES,
                    activity.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK)
            }
        }
        launch().use { awaitSelected("Dark theme") }
    }

    @Test fun followsSystemAndCloseReturnsToCaller() {
        launch().use { scenario ->
            awaitSelected("Light theme")
            compose.onNodeWithText("Follow system").performClick()
            awaitSelected("Follow system")
            assertEquals(ThemeMode.SYSTEM, runBlocking { repository.theme.first() })
            compose.onNodeWithContentDescription("Close").performClick()
            compose.waitUntil(5_000) { scenario.state == androidx.lifecycle.Lifecycle.State.DESTROYED }
        }
    }
}
