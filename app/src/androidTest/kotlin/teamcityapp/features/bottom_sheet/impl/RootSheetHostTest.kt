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

package teamcityapp.features.bottom_sheet.impl

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.SystemClock
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.navigation3.runtime.NavKey
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.pressBack
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.vase4kin.teamcityapp.TeamCityApplicationBase
import com.github.vase4kin.teamcityapp.di.modules.Mocks
import com.github.vase4kin.teamcityapp.helper.HiltApiTestRule
import com.github.vase4kin.teamcityapp.helper.RootDestinationLauncher
import com.github.vase4kin.teamcityapp.helper.TestUtils
import com.github.vase4kin.teamcityapp.navigation.IncomingDestinationResolver
import com.github.vase4kin.teamcityapp.navigation.MainActivity
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
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
import teamcityapp.features.bottom_sheet.api.SheetMenuType
import teamcityapp.features.bottom_sheet.api.navigation.ActionSheetDestination
import teamcityapp.features.drawer.api.navigation.DrawerDestination
import teamcityapp.features.filter_bottom_sheet.api.navigation.QuickFilterDestination
import teamcityapp.libraries.app_theme.ThemeMode

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class RootSheetHostTest {
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
    private var originalAccessibilityFlags = 0
    private var dark = false

    @Before fun before() {
        originalTheme = runBlocking { app.appInjector.themePreferences().theme.first() }
        originalNight = AppCompatDelegate.getDefaultNightMode()
        val service = instrumentation.uiAutomation.serviceInfo
        originalAccessibilityFlags = service.flags
        service.flags = service.flags or android.accessibilityservice.AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
        instrumentation.uiAutomation.serviceInfo = service
        TestUtils.disableOnboarding()
        app.appInjector.sharedUserStorage().apply {
            clearAll()
            saveGuestUserAccountAndSetItAsActive(Mocks.URL, false)
        }
    }

    @After fun restoreTheme() {
        val service = instrumentation.uiAutomation.serviceInfo
        service.flags = originalAccessibilityFlags
        instrumentation.uiAutomation.serviceInfo = service
        runBlocking { app.appInjector.themePreferences().setTheme(originalTheme) }
        instrumentation.runOnMainSync { AppCompatDelegate.setDefaultNightMode(originalNight) }
    }

    @Test fun actionLight() = action(false)

    @Test fun actionDark() = action(true)

    @Test fun quickLight() = quick(false)

    @Test fun quickDark() = quick(true)

    @Test fun drawerLight() = drawer(false)

    @Test fun drawerDark() = drawer(true)

    @Test fun drawerMultipleLight() = drawer(false, multiple = true)

    @Test fun drawerMultipleDark() = drawer(true, multiple = true)

    private fun action(dark: Boolean) = exercise("action", "sheet:Copy", dark) {
        ActionSheetDestination("Configuration", listOf("configuration-id"), SheetMenuType.BuildType, it.navigationSnapshot!!.entries.first().id, "host-test")
    }

    private fun quick(dark: Boolean) = exercise("quick", "quick-filter:apply", dark) {
        QuickFilterDestination(0)
    }

    private val storageListener = object : com.github.vase4kin.teamcityapp.storage.SharedUserStorage.OnStorageListener {
        override fun onSuccess() = Unit
        override fun onFail() = throw AssertionError("Named native account fixture could not be saved")
    }

    private fun drawer(dark: Boolean, multiple: Boolean = false) {
        app.appInjector.sharedUserStorage().apply {
            clearAll()
            if (multiple) saveUserAccountAndSetItAsActive("https://teamcity.example/secondary", "build.engineer", "test-password", true, storageListener)
            saveUserAccountAndSetItAsActive(Mocks.URL, "alex.morgan", "test-password", false, storageListener)
        }
        exercise(if (multiple) "drawer_multiple" else "drawer", "drawer:add", dark) { DrawerDestination }
    }

    @Inject lateinit var launcher: RootDestinationLauncher

    private fun exercise(name: String, rowTag: String, dark: Boolean, factory: (MainActivity) -> NavKey) {
        this.dark = dark
        runBlocking { app.appInjector.themePreferences().setTheme(if (dark) ThemeMode.Dark else ThemeMode.Light) }
        instrumentation.runOnMainSync { AppCompatDelegate.setDefaultNightMode(if (dark) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO) }
        ActivityScenario.launch<MainActivity>(Intent(app, MainActivity::class.java).setClassName(app, IncomingDestinationResolver.HOME_COMPONENT)).use { scenario ->
            await("home:tab:Projects")
            scenario.onActivity { launcher.open(it, factory(it)) }
            await(rowTag)
            assertHost(scenario)
            capture("${name}_initial")
            await("navigation:sheet")
            assertHost(scenario)
            if (name.startsWith("drawer")) {
                assertDrawerSpacingAndMenuWidths(scenario)
                listOf("settings", "about", "rate", "privacy").forEach { action ->
                    assertRowEdgesAndRipple("${name}_$action", "drawer:$action", scenario)
                    compose.mainClock.advanceTimeBy(500)
                    compose.waitForIdle()
                    SystemClock.sleep(300) // Let cancelled row feedback disappear before the next idle capture.
                }
            } else {
                assertRowEdgesAndRipple(name, rowTag, scenario)
            }
            scenario.recreate()
            await(rowTag)
            assertHost(scenario)
            capture("${name}_recreated")
            pressBack()
            compose.waitUntil(10_000) { compose.onAllNodesWithTag("navigation:sheet").fetchSemanticsNodes().isEmpty() }
            scenario.onActivity { launcher.open(it, factory(it)) }
            await(rowTag)
            assertHost(scenario)
            capture("${name}_reopened")
            if (name == "action") {
                listOf(false, true).forEach { right ->
                    compose.onNodeWithTag(rowTag).performTouchInput { click(Offset(if (right) width - 2f else 2f, height / 2f)) }
                    instrumentation.waitForIdleSync()
                    scenario.onActivity {
                        val clipboard = it.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                        assertEquals("configuration-id", clipboard.primaryClip?.getItemAt(0)?.text?.toString())
                        assertTrue(it.supportFragmentManager.fragments.isEmpty())
                        assertEquals(1, it.navigationSnapshot!!.entries.size)
                        if (!right) launcher.open(it, factory(it))
                    }
                    if (!right) await(rowTag)
                }
                // Android's clipboard preview is a separate system window; avoid
                // carrying it into the next sheet's evidence without changing copy behavior.
                compose.waitUntil(10_000) {
                    instrumentation.uiAutomation.windows.none { window ->
                        val root = window.root
                        root?.packageName == "com.android.systemui" && root.findAccessibilityNodeInfosByText("configuration-id").isNotEmpty()
                    }
                }
            }
        }
    }

    private fun await(tag: String) {
        compose.waitUntil(10_000) { compose.onAllNodesWithTag(tag).fetchSemanticsNodes(atLeastOneRootRequired = false).isNotEmpty() }
        compose.onNodeWithTag(tag).assertIsDisplayed()
        SystemClock.sleep(500) // Native window animation is not driven by the Compose test clock.
        instrumentation.waitForIdleSync()
    }
    private fun assertHost(scenario: ActivityScenario<MainActivity>) {
        compose.onNodeWithTag("navigation:sheet").assertIsDisplayed()
        scenario.onActivity { assertTrue(it.supportFragmentManager.fragments.isEmpty()) }
    }

    private fun assertDrawerSpacingAndMenuWidths(scenario: ActivityScenario<MainActivity>) {
        compose.waitUntil(10_000) { compose.onAllNodesWithTag("drawer:active-card").fetchSemanticsNodes().isNotEmpty() }
        compose.onNode(SemanticsMatcher.expectValue(androidx.compose.ui.semantics.SemanticsProperties.StateDescription, "Current account")).assertIsDisplayed()
        val active = compose.onNodeWithTag("drawer:active-card")
        active.assertHasNoClickAction()
        val density = app.resources.displayMetrics.density
        compose.onNodeWithTag("drawer:manage").assertIsDisplayed().assertHeightIsAtLeast(teamcityapp.libraries.theme.TeamCityDimensions.minimumTouchTarget)
        val sheet = compose.onNodeWithTag("navigation:sheet").fetchSemanticsNode().boundsInWindow
        val left = sheet.left
        val right = sheet.right
        listOf("add", "settings", "about", "rate", "privacy").forEach { action ->
            val row = compose.onNodeWithTag("drawer:$action").performScrollTo()
            row.assertIsDisplayed().assert(SemanticsMatcher.expectValue(androidx.compose.ui.semantics.SemanticsProperties.Role, androidx.compose.ui.semantics.Role.Button))
            val bounds = row.fetchSemanticsNode().boundsInWindow
            assertEquals(left + 16f * density, bounds.left, 1f)
            assertEquals(right - 16f * density, bounds.right, 1f)
        }
    }

    private fun assertRowEdgesAndRipple(name: String, tag: String, scenario: ActivityScenario<MainActivity>) {
        val row = compose.onNodeWithTag(tag)
        if (name.startsWith("drawer")) {
            row.performScrollTo()
            compose.mainClock.advanceTimeBy(500)
            compose.waitForIdle()
            SystemClock.sleep(300)
        }
        row.assert(SemanticsMatcher.expectValue(androidx.compose.ui.semantics.SemanticsProperties.Role, if (name == "quick") androidx.compose.ui.semantics.Role.RadioButton else androidx.compose.ui.semantics.Role.Button))
        val bounds = row.fetchSemanticsNode().boundsInWindow
        var screenDeltaX = 0
        var screenDeltaY = 0
        val sheet = compose.onNodeWithTag("navigation:sheet").fetchSemanticsNode().boundsInWindow
        scenario.onActivity {
            val location = IntArray(2)
            val screen = IntArray(2)
            it.window.decorView.getLocationInWindow(location)
            it.window.decorView.getLocationOnScreen(screen)
            screenDeltaX = screen[0] - location[0]
            screenDeltaY = screen[1] - location[1]
        }
        val inset = if (name.startsWith("drawer")) 16f * app.resources.displayMetrics.density else 0f
        assertEquals(sheet.left + inset, bounds.left, 1f)
        assertEquals(sheet.right - inset, bounds.right, 1f)
        val before = capture("${name}_idle")
        row.performTouchInput { down(center) }
        try {
            compose.mainClock.advanceTimeBy(500)
            compose.waitForIdle()
            SystemClock.sleep(100)
            instrumentation.waitForIdleSync()
            val pressed = capture("${name}_pressed")
            val y = bounds.center.y.toInt() + screenDeltaY
            listOf(bounds.left.toInt() + 3, bounds.right.toInt() - 4).forEach { x ->
                assertNotEquals("Held ripple must reach row edge", before.getPixel(x + screenDeltaX, y), pressed.getPixel(x + screenDeltaX, y))
            }
        } finally {
            row.performTouchInput { cancel() }
        }
    }
    private fun capture(name: String): Bitmap {
        val bitmap = requireNotNull(instrumentation.uiAutomation.takeScreenshot())
        // Screenshot persistence is opt-in local evidence. Pixel assertions always run,
        // including on API24–28 where MediaStore.RELATIVE_PATH is unavailable.
        if (android.os.Build.VERSION.SDK_INT < 29 || InstrumentationRegistry.getArguments().getString("persistNativeSheetEvidence") != "true") return bitmap
        val mode = if (dark) "dark" else "light"
        val values = android.content.ContentValues().apply {
            put(android.provider.MediaStore.Images.Media.DISPLAY_NAME, "${name}_$mode.png")
            put(android.provider.MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(android.provider.MediaStore.Images.Media.RELATIVE_PATH, "Pictures/TeamCitySheetTests")
        }
        val uri = requireNotNull(app.contentResolver.insert(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values))
        requireNotNull(app.contentResolver.openOutputStream(uri)).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        return bitmap
    }
    companion object {
        private const val TAG = "native-sheet-test"
    }
}
