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

package teamcityapp.libraries.authentication

import android.content.Intent
import android.graphics.Bitmap
import android.os.SystemClock
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.vase4kin.teamcityapp.TeamCityApplicationBase
import com.github.vase4kin.teamcityapp.dagger.modules.Mocks
import com.github.vase4kin.teamcityapp.helper.HiltApiTestRule
import com.github.vase4kin.teamcityapp.helper.TestUtils
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
import teamcityapp.features.create_account.impl.CreateAccountActivity
import teamcityapp.features.login.impl.LoginActivity
import teamcityapp.features.run_build.impl.RunBuildActivity
import teamcityapp.libraries.app_theme.ThemeMode

/** Actual activity/window evidence: local golden rendering does not animate held ripples. */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class AuthenticationSwitchRippleTest {
    @JvmField
    @Rule(order = 0)
    val hilt = HiltAndroidRule(this)

    @JvmField
    @Rule(order = 1)
    val api = HiltApiTestRule(hilt)

    @JvmField
    @Rule(order = 2)
    val compose = createEmptyComposeRule()
    private val instrumentation get() = InstrumentationRegistry.getInstrumentation()
    private val app get() = instrumentation.targetContext.applicationContext as TeamCityApplicationBase
    private var originalTheme = ThemeMode.System
    private var originalNight = AppCompatDelegate.MODE_NIGHT_UNSPECIFIED
    private var dark = false

    @Before fun prepare() {
        originalTheme = runBlocking { app.appInjector.themePreferences().theme.first() }
        originalNight = AppCompatDelegate.getDefaultNightMode()
        TestUtils.disableOnboarding()
        app.appInjector.sharedUserStorage().clearAll()
    }

    @After fun restore() {
        runBlocking { app.appInjector.themePreferences().setTheme(originalTheme) }
        instrumentation.runOnMainSync { AppCompatDelegate.setDefaultNightMode(originalNight) }
    }

    @Test fun loginLight() = login(false)

    @Test fun loginDark() = login(true)

    @Test fun createLight() = create(false)

    @Test fun createDark() = create(true)

    @Test fun runLight() = run(false)

    @Test fun runDark() = run(true)

    private fun login(dark: Boolean) {
        mode(dark)
        ActivityScenario.launch<LoginActivity>(Intent(app, LoginActivity::class.java)).use { scenario ->
            inspect("login", "login:form") { block -> scenario.onActivity { block(it.window.decorView) } }
        }
    }
    private fun create(dark: Boolean) {
        mode(dark)
        ActivityScenario.launch<CreateAccountActivity>(Intent(app, CreateAccountActivity::class.java)).use { scenario ->
            inspect("create", "create-account:form") { block -> scenario.onActivity { block(it.window.decorView) } }
        }
    }
    private fun run(dark: Boolean) {
        mode(dark)
        app.appInjector.sharedUserStorage().saveGuestUserAccountAndSetItAsActive(Mocks.URL, false)
        val intent = Intent(app, RunBuildActivity::class.java).putExtra(com.github.vase4kin.teamcityapp.runbuild.interactor.EXTRA_BUILD_TYPE_ID, "href")
        ActivityScenario.launch<RunBuildActivity>(intent).use { scenario ->
            compose.waitUntil(10_000) { compose.onAllNodesWithTag("run-build:options").fetchSemanticsNodes(atLeastOneRootRequired = false).isNotEmpty() }
            compose.onNodeWithTag("run-build:options").performClick()
            inspect("run", "run-build:options-card", listOf("personal" to false, "top" to false, "clean" to true)) { block -> scenario.onActivity { block(it.window.decorView) } }
        }
    }
    private fun mode(value: Boolean) {
        dark = value
        runBlocking { app.appInjector.themePreferences().setTheme(if (dark) ThemeMode.Dark else ThemeMode.Light) }
        instrumentation.runOnMainSync { AppCompatDelegate.setDefaultNightMode(if (dark) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO) }
    }
    private fun inspect(name: String, paneTag: String, controls: List<Pair<String, Boolean>> = listOf("guest" to false, "ssl" to false), decor: ((android.view.View) -> Unit) -> Unit) {
        compose.waitUntil(10_000) { compose.onAllNodesWithTag(if (name == "run") "run-build:personal" else "auth:guest").fetchSemanticsNodes(atLeastOneRootRequired = false).isNotEmpty() }
        SystemClock.sleep(500)
        controls.forEach { (control, checked) ->
            val row = compose.onNodeWithTag("${if (name == "run") "run-build" else "auth"}:$control")
            row.performScrollTo().assertIsDisplayed().assert(SemanticsMatcher.expectValue(SemanticsProperties.ToggleableState, if (checked) androidx.compose.ui.state.ToggleableState.On else androidx.compose.ui.state.ToggleableState.Off)).assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Switch))
            compose.mainClock.advanceTimeBy(500)
            compose.waitForIdle()
            SystemClock.sleep(300)
            val bounds = row.fetchSemanticsNode().boundsInWindow
            val pane = compose.onNodeWithTag(paneTag).fetchSemanticsNode().boundsInWindow
            assertEquals(pane.left, bounds.left, 1f)
            assertEquals(pane.right, bounds.right, 1f)
            var dx = 0
            var dy = 0
            decor { view ->
                val window = IntArray(2)
                val screen = IntArray(2)
                view.getLocationInWindow(window)
                view.getLocationOnScreen(screen)
                dx = screen[0] - window[0]
                dy = screen[1] - window[1]
            }
            val before = capture("${name}_${control}_idle")
            row.performTouchInput { down(center) }
            try {
                compose.mainClock.advanceTimeBy(500)
                compose.waitForIdle()
                SystemClock.sleep(100)
                instrumentation.waitForIdleSync()
                val pressed = capture("${name}_${control}_pressed")
                val y = bounds.center.y.toInt() + dy
                listOf(bounds.left.toInt() + 3, bounds.right.toInt() - 4).forEach { x ->
                    assertNotEquals("Held switch ripple must reach form edge", before.getPixel(x + dx, y), pressed.getPixel(x + dx, y))
                }
            } finally {
                row.performTouchInput { cancel() }
            }
            row.assert(SemanticsMatcher.expectValue(SemanticsProperties.ToggleableState, if (checked) androidx.compose.ui.state.ToggleableState.On else androidx.compose.ui.state.ToggleableState.Off))
            compose.onNodeWithTag("auth:warning").assertDoesNotExist()
        }
        if (name == "run") return
        // A released edge tap still requests explicit consent; cancelling cannot
        // enable SSL bypass, and the passive thumb dispatches the same request once.
        val ssl = compose.onNodeWithTag("auth:ssl")
        ssl.performTouchInput { click(androidx.compose.ui.geometry.Offset(2f, height / 2f)) }
        compose.onNodeWithTag("auth:warning").assertIsDisplayed()
        compose.onNodeWithText(app.getString(R.string.warning_ssl_dialog_negative)).performClick()
        ssl.assertIsOff()
        val density = app.resources.displayMetrics.density
        ssl.performTouchInput { click(androidx.compose.ui.geometry.Offset(width - 50f * density, height - 28f * density)) }
        compose.onNodeWithTag("auth:warning").assertIsDisplayed()
        compose.onNodeWithText(app.getString(R.string.dialog_ok_title)).performClick()
        ssl.assertIsOn()
    }
    private fun capture(name: String): Bitmap {
        val bitmap = requireNotNull(instrumentation.uiAutomation.takeScreenshot())
        if (android.os.Build.VERSION.SDK_INT < 29 || InstrumentationRegistry.getArguments().getString("persistAuthSwitchEvidence") != "true") return bitmap
        val values = android.content.ContentValues().apply {
            put(android.provider.MediaStore.Images.Media.DISPLAY_NAME, "${name}_${if (dark) "dark" else "light"}.png")
            put(android.provider.MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(android.provider.MediaStore.Images.Media.RELATIVE_PATH, "Pictures/TeamCityAuthSwitchTests")
        }
        val uri = requireNotNull(app.contentResolver.insert(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values))
        requireNotNull(app.contentResolver.openOutputStream(uri)).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        return bitmap
    }
}
