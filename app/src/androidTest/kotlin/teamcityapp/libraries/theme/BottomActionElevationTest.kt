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

package teamcityapp.libraries.theme

import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.os.SystemClock
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.closeSoftKeyboard
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
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import teamcityapp.features.create_account.impl.CreateAccountActivity
import teamcityapp.features.filter_builds.impl.FilterBuildsActivity
import teamcityapp.features.run_build.impl.RunBuildActivity
import teamcityapp.libraries.app_theme.ThemeMode

/** Actual activity viewport, scroll-end and keyboard evidence for fixed action surfaces. */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class BottomActionElevationTest {
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
        app.appInjector.sharedUserStorage().saveGuestUserAccountAndSetItAsActive(Mocks.URL, false)
    }

    @After fun restore() {
        runBlocking { app.appInjector.themePreferences().setTheme(originalTheme) }
        instrumentation.runOnMainSync { AppCompatDelegate.setDefaultNightMode(originalNight) }
    }

    @Test fun runLight() = run(false)

    @Test fun runDark() = run(true)

    @Test fun createLight() = create(false)

    @Test fun createDark() = create(true)

    @Test fun filterLight() = filter(false)

    @Test fun filterDark() = filter(true)

    private fun mode(value: Boolean) {
        dark = value
        runBlocking { app.appInjector.themePreferences().setTheme(if (dark) ThemeMode.Dark else ThemeMode.Light) }
        instrumentation.runOnMainSync { AppCompatDelegate.setDefaultNightMode(if (dark) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO) }
    }
    private fun run(dark: Boolean) {
        mode(dark)
        val intent = Intent(app, RunBuildActivity::class.java).putExtra(com.github.vase4kin.teamcityapp.runbuild.interactor.EXTRA_BUILD_TYPE_ID, "href")
        ActivityScenario.launch<RunBuildActivity>(intent).use { scenario ->
            await("run-build")
            inspect(scenario, "run-build", "fit", false)
            compose.onNodeWithTag("run-build:options").performClick()
            repeat(4) { index ->
                compose.onNodeWithTag("run-build:add").performScrollTo().performClick()
                compose.onNodeWithTag("parameter:name").performTextInput("env.option$index")
                compose.onNodeWithTag("parameter:value").performTextInput("value$index")
                compose.onNodeWithTag("parameter:confirm").performClick()
            }
            closeSoftKeyboard()
            scroll("run-build", -10_000f)
            inspect(scenario, "run-build", "overflow", true)
            val bounds = compose.onNodeWithTag("run-build:bottom-action").fetchSemanticsNode().boundsInWindow
            scroll("run-build", 10_000f)
            inspect(scenario, "run-build", "bottom", false)
            assertEquals(bounds, compose.onNodeWithTag("run-build:bottom-action").fetchSemanticsNode().boundsInWindow)
            scroll("run-build", -100f)
            inspect(scenario, "run-build", "back", true)
        }
    }
    private fun create(dark: Boolean) {
        mode(dark)
        ActivityScenario.launch<CreateAccountActivity>(Intent(app, CreateAccountActivity::class.java)).use { scenario ->
            await("create-account")
            inspect(scenario, "create-account", "fit", false)
            landscape(scenario)
            val beforeHeight = compose.onNodeWithTag("create-account:scroll").fetchSemanticsNode().boundsInWindow.height
            compose.onNodeWithTag("auth:url").performTouchInput { click(center) }
            compose.waitUntil(5_000) {
                var visible = false
                scenario.onActivity { visible = ViewCompat.getRootWindowInsets(it.window.decorView)?.isVisible(WindowInsetsCompat.Type.ime()) == true }
                visible
            }
            settle()
            val keyboardHeight = compose.onNodeWithTag("create-account:scroll").fetchSemanticsNode().boundsInWindow.height
            var imeBottom = 0
            scenario.onActivity { imeBottom = ViewCompat.getRootWindowInsets(it.window.decorView)?.getInsets(WindowInsetsCompat.Type.ime())?.bottom ?: 0 }
            if (imeBottom > 0) {
                assertTrue("Docked IME must actually reduce form viewport", keyboardHeight < beforeHeight)
            } else {
                assertEquals("Floating input toolbar does not consume bottom space", beforeHeight, keyboardHeight, 1f)
            }
            scroll("create-account", -10_000f)
            val range = compose.onNodeWithTag("create-account:scroll").fetchSemanticsNode().config[SemanticsProperties.VerticalScrollAxisRange]
            inspect(scenario, "create-account", if (imeBottom > 0) "keyboard" else "input-toolbar-overflow", range.value() < range.maxValue())
            scroll("create-account", 10_000f)
            inspect(scenario, "create-account", if (imeBottom > 0) "keyboard-bottom" else "input-toolbar-bottom", false)
            closeSoftKeyboard()
            portrait(scenario)
            inspect(scenario, "create-account", "input-hidden", false)
        }
    }
    private fun filter(dark: Boolean) {
        mode(dark)
        ActivityScenario.launch<FilterBuildsActivity>(Intent(app, FilterBuildsActivity::class.java)).use { scenario ->
            await("filter-builds")
            inspect(scenario, "filter-builds", "fit", false)
            landscape(scenario)
            inspect(scenario, "filter-builds", "landscape", true)
            scroll("filter-builds", 10_000f)
            inspect(scenario, "filter-builds", "bottom", false)
            scroll("filter-builds", -40f)
            inspect(scenario, "filter-builds", "back", true)
            portrait(scenario)
        }
    }
    private fun <A : android.app.Activity> landscape(scenario: ActivityScenario<A>) = orient(scenario, true)
    private fun <A : android.app.Activity> portrait(scenario: ActivityScenario<A>) = orient(scenario, false)
    private fun <A : android.app.Activity> orient(scenario: ActivityScenario<A>, landscape: Boolean) {
        scenario.onActivity { it.requestedOrientation = if (landscape) ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE else ActivityInfo.SCREEN_ORIENTATION_PORTRAIT }
        compose.waitUntil(5_000) {
            var ready = false
            scenario.onActivity { ready = (it.window.decorView.width > it.window.decorView.height) == landscape }
            ready
        }
        settle()
    }
    private fun await(prefix: String) {
        compose.waitUntil(10_000) { compose.onAllNodesWithTag("$prefix:bottom-action").fetchSemanticsNodes(atLeastOneRootRequired = false).isNotEmpty() }
        settle()
    }
    private fun scroll(prefix: String, distance: Float) {
        compose.onNodeWithTag("$prefix:scroll").performSemanticsAction(SemanticsActions.ScrollBy) { it(0f, distance) }
        settle()
    }
    private fun settle() {
        compose.mainClock.advanceTimeBy(1_000)
        compose.waitForIdle()
        SystemClock.sleep(300)
        instrumentation.waitForIdleSync()
    }
    private fun <A : android.app.Activity> inspect(scenario: ActivityScenario<A>, prefix: String, state: String, raised: Boolean) {
        settle()
        val bar = compose.onNodeWithTag("$prefix:bottom-action").fetchSemanticsNode().boundsInWindow
        val viewport = compose.onNodeWithTag("$prefix:scroll").fetchSemanticsNode().boundsInWindow
        val delta = IntArray(2)
        scenario.onActivity {
            val screen = IntArray(2)
            val window = IntArray(2)
            it.window.decorView.getLocationOnScreen(screen)
            it.window.decorView.getLocationInWindow(window)
            delta[0] = screen[0] - window[0]
            delta[1] = screen[1] - window[1]
        }
        var imeBottom = 0
        var navBottom = 0
        var decorHeight = 0
        scenario.onActivity {
            val insets = ViewCompat.getRootWindowInsets(it.window.decorView)
            imeBottom = insets?.getInsets(WindowInsetsCompat.Type.ime())?.bottom ?: 0
            navBottom = insets?.getInsets(WindowInsetsCompat.Type.navigationBars())?.bottom ?: 0
            decorHeight = it.window.decorView.height
        }
        android.util.Log.i("BottomActionEvidence", "$prefix/$state: viewport=$viewport bar=$bar imeBottom=$imeBottom raised=$raised")
        val bitmap = requireNotNull(instrumentation.uiAutomation.takeScreenshot())
        val x = bar.left.toInt() + delta[0] + 8
        val flat = bitmap.getPixel(x, viewport.top.toInt() + delta[1] + 8)
        val actual = bitmap.getPixel(x, bar.center.y.toInt() + delta[1])
        if (raised) {
            assertNotEquals("Remaining content needs a distinct action surface", flat, actual)
        } else {
            assertEquals("Fit/end surface must blend with screen", flat, actual)
        }
        if (imeBottom == 0 && navBottom > 0) {
            assertEquals("Footer must paint through the navigation inset", decorHeight.toFloat(), bar.bottom, 1f)
            // Three-button navigation may add an OS contrast scrim over the same surface.
            if (InstrumentationRegistry.getArguments().getString("verifyGestureNavigationTone") == "true") {
                val navigation = bitmap.getPixel(x, decorHeight + delta[1] - navBottom / 2)
                assertEquals("Gesture area and actions must share the same tone", actual, navigation)
            }
        }
        compose.onNodeWithTag(if (prefix == "filter-builds") "$prefix:apply" else "$prefix:submit").assertIsDisplayed()
        if (android.os.Build.VERSION.SDK_INT >= 29 && InstrumentationRegistry.getArguments().getString("persistBottomActionEvidence") == "true") {
            val values = android.content.ContentValues().apply {
                put(android.provider.MediaStore.Images.Media.DISPLAY_NAME, "${prefix}_${state}_${if (dark) "dark" else "light"}.png")
                put(android.provider.MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(android.provider.MediaStore.Images.Media.RELATIVE_PATH, "Pictures/TeamCityBottomActionTests")
            }
            val uri = requireNotNull(app.contentResolver.insert(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values))
            requireNotNull(app.contentResolver.openOutputStream(uri)).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        }
    }
}
