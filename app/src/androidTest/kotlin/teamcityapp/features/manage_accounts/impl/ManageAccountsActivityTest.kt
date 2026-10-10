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

package teamcityapp.features.manage_accounts.impl

import android.app.Activity
import android.app.Instrumentation.ActivityResult
import android.content.Intent
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.vase4kin.teamcityapp.TeamCityApplicationBase
import com.github.vase4kin.teamcityapp.dagger.modules.Mocks
import com.github.vase4kin.teamcityapp.helper.HiltApiTestRule
import com.github.vase4kin.teamcityapp.home.view.HomeActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import teamcityapp.features.create_account.impl.CreateAccountActivity
import teamcityapp.features.login.impl.LoginActivity
import teamcityapp.libraries.app_theme.ThemeMode

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ManageAccountsActivityTest {
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
    private val storage get() = app.appInjector.sharedUserStorage()
    private val first = Mocks.URL
    private val second = "https://teamcity.example/secondary"
    private val warning = "Ignore SSL certificate validity is enabled for this account"

    @Before fun before() {
        storage.clearAll()
        storage.saveGuestUserAccountAndSetItAsActive(first, false)
        storage.saveGuestUserAccountAndSetItAsActive(second, true)
    }
    private fun launch() = ActivityScenario.launch<ManageAccountsActivity>(Intent(app, ManageAccountsActivity::class.java))
    private fun row(url: String) = compose.onNodeWithTag("accounts:row:$url:Guest user:remove")
    private fun awaitRows() {
        compose.waitUntil(5_000) { compose.onAllNodesWithText(second).fetchSemanticsNodes().isNotEmpty() }
    }
    private fun awaitDialog() {
        compose.waitUntil(5_000) { compose.onAllNodesWithTag("accounts:dialog").fetchSemanticsNodes().isNotEmpty() }
    }

    @Test fun cornerIconNativeLight() = verifyNativeCorner(false)

    @Test fun cornerIconNativeDark() = verifyNativeCorner(true)

    private fun verifyNativeCorner(dark: Boolean) {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val originalTheme = runBlocking { app.appInjector.themePreferences().theme.first() }
        val originalNight = AppCompatDelegate.getDefaultNightMode()
        try {
            runBlocking { app.appInjector.themePreferences().setTheme(if (dark) ThemeMode.Dark else ThemeMode.Light) }
            instrumentation.runOnMainSync { AppCompatDelegate.setDefaultNightMode(if (dark) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO) }
            listOf(false, true).forEach { multiple ->
                storage.clearAll()
                val name = if (multiple) "alexander.morgan.platform" else "Guest user"
                if (multiple) {
                    storage.saveUserAccountAndSetItAsActive(
                        first,
                        name,
                        "test-password",
                        false,
                        object : com.github.vase4kin.teamcityapp.storage.SharedUserStorage.OnStorageListener {
                            override fun onSuccess() = Unit
                            override fun onFail() = throw AssertionError("Named account fixture could not be saved")
                        }
                    )
                    storage.saveGuestUserAccountAndSetItAsActive(second, true)
                } else {
                    storage.saveGuestUserAccountAndSetItAsActive(first, false)
                }
                val prefix = "manage_${if (multiple) "multiple" else "single"}_${if (dark) "dark" else "light"}"
                launch().use { scenario ->
                    compose.waitUntil(5_000) { compose.onAllNodesWithText(first).fetchSemanticsNodes().isNotEmpty() }
                    compose.mainClock.advanceTimeBy(500)
                    compose.waitForIdle()
                    android.os.SystemClock.sleep(300)
                    val target = compose.onNodeWithTag("accounts:row:$first:$name:remove")
                    target.assertWidthIsAtLeast(teamcityapp.libraries.theme.TeamCityDimensions.minimumTouchTarget)
                        .assertHeightIsAtLeast(teamcityapp.libraries.theme.TeamCityDimensions.minimumTouchTarget)
                        .assertContentDescriptionEquals("Remove $name account at $first")
                    compose.onNodeWithText(name, useUnmergedTree = true).performTouchInput { click(center) }
                    compose.onNodeWithText(first, useUnmergedTree = true).performTouchInput { click(center) }
                    compose.onNodeWithTag("accounts:dialog").assertDoesNotExist()
                    assertEquals(if (multiple) 2 else 1, storage.userAccounts.size)
                    val before = captureNative("${prefix}_idle")
                    val bounds = target.fetchSemanticsNode().boundsInWindow
                    val delta = IntArray(2)
                    scenario.onActivity {
                        val screen = IntArray(2)
                        val window = IntArray(2)
                        it.window.decorView.getLocationOnScreen(screen)
                        it.window.decorView.getLocationInWindow(window)
                        delta[0] = screen[0] - window[0]
                        delta[1] = screen[1] - window[1]
                    }
                    target.performTouchInput { down(center) }
                    try {
                        compose.mainClock.advanceTimeBy(200)
                        compose.waitForIdle()
                        android.os.SystemClock.sleep(100)
                        val held = captureNative("${prefix}_pressed")
                        val y = bounds.center.y.toInt() + delta[1]
                        listOf(bounds.left.toInt() + 4, bounds.right.toInt() - 5).forEach { x ->
                            assertNotEquals("Icon feedback must fill its bounded target", before.getPixel(x + delta[0], y), held.getPixel(x + delta[0], y))
                        }
                    } finally {
                        target.performTouchInput { cancel() }
                    }
                    compose.mainClock.advanceTimeBy(500)
                    compose.waitForIdle()
                    target.performClick()
                    awaitDialog()
                    captureNative("${prefix}_confirmation")
                    compose.onNodeWithText("Cancel").performClick()
                    compose.onNodeWithTag("accounts:dialog").assertDoesNotExist()
                    assertEquals(if (multiple) 2 else 1, storage.userAccounts.size)
                    captureNative("${prefix}_cancelled")
                    if (multiple) {
                        target.performClick()
                        awaitDialog()
                        compose.onNodeWithText("Remove").performClick()
                        compose.waitUntil(5_000) { storage.userAccounts.size == 1 && compose.onAllNodesWithText(first).fetchSemanticsNodes().isEmpty() }
                        assertEquals(second, storage.activeUser.teamcityUrl)
                        captureNative("${prefix}_confirmed")
                    }
                }
            }
        } finally {
            runBlocking { app.appInjector.themePreferences().setTheme(originalTheme) }
            instrumentation.runOnMainSync { AppCompatDelegate.setDefaultNightMode(originalNight) }
        }
    }

    private fun captureNative(name: String): android.graphics.Bitmap {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        if (!name.endsWith("_pressed")) {
            compose.mainClock.advanceTimeBy(300)
            compose.waitForIdle()
            android.os.SystemClock.sleep(200)
        }
        instrumentation.waitForIdleSync()
        val bitmap = requireNotNull(instrumentation.uiAutomation.takeScreenshot())
        if (android.os.Build.VERSION.SDK_INT >= 29 && InstrumentationRegistry.getArguments().getString("persistManageAccountEvidence") == "true") {
            val values = android.content.ContentValues().apply {
                put(android.provider.MediaStore.Images.Media.DISPLAY_NAME, "$name.png")
                put(android.provider.MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(android.provider.MediaStore.Images.Media.RELATIVE_PATH, "Pictures/TeamCityAccountTests")
            }
            val uri = requireNotNull(app.contentResolver.insert(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values))
            requireNotNull(app.contentResolver.openOutputStream(uri)).use { bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it) }
        }
        return bitmap
    }

    @Test fun contentSurvivesRecreationAndReadsNewAccountsAfterResume() {
        launch().use { scenario ->
            awaitRows()
            compose.onAllNodesWithText("Guest user").assertCountEquals(2)
            scenario.recreate()
            awaitRows()
            scenario.moveToState(Lifecycle.State.CREATED)
            storage.saveGuestUserAccountAndSetItAsActive("https://teamcity.example/new", false)
            scenario.moveToState(Lifecycle.State.RESUMED)
            compose.waitUntil(5_000) { compose.onAllNodesWithText("https://teamcity.example/new").fetchSemanticsNodes().isNotEmpty() }
            compose.onNodeWithText("https://teamcity.example/new").assertIsDisplayed()
        }
    }

    @Test fun warningHasItsOwnDialogAndDoesNotRequestRemoval() {
        launch().use {
            awaitRows()
            compose.onNodeWithText(warning, useUnmergedTree = true).performClick()
            awaitDialog()
            compose.onNodeWithText("Warning").assertIsDisplayed()
            compose.onNodeWithText("Remove").assertDoesNotExist()
            compose.onNodeWithText("OK").performClick()
            compose.onNodeWithTag("accounts:dialog").assertDoesNotExist()
            assertEquals(2, storage.userAccounts.size)
        }
    }

    @Test fun cancelKeepsAccountsAndBackClosesActivity() {
        launch().use { scenario ->
            awaitRows()
            row(first).performClick()
            awaitDialog()
            compose.onNodeWithText("Cancel").performClick()
            assertEquals(2, storage.userAccounts.size)
            compose.onNodeWithContentDescription("Back").performClick()
            compose.waitUntil(5_000) { scenario.state == Lifecycle.State.DESTROYED }
        }
    }

    @Test fun inactiveRemovalStaysHereAndPreservesTheActiveAccount() {
        launch().use { scenario ->
            awaitRows()
            row(first).performClick()
            awaitDialog()
            compose.onNodeWithText("Remove").performClick()
            compose.waitUntil(5_000) { compose.onAllNodesWithText(first).fetchSemanticsNodes().isEmpty() }
            row(second).assertIsDisplayed()
            assertEquals(second, storage.activeUser.teamcityUrl)
            assertEquals(1, storage.userAccounts.size)
            assertEquals(Lifecycle.State.RESUMED, scenario.state)
        }
    }

    @Test fun activeRemovalSwitchesAccountAndOpensHome() {
        Intents.init()
        try {
            intending(hasComponent(HomeActivity::class.java.name)).respondWith(ActivityResult(Activity.RESULT_OK, null))
            launch().use {
                awaitRows()
                row(second).performClick()
                awaitDialog()
                compose.onNodeWithText("Remove").performClick()
                compose.waitUntil(5_000) { Intents.getIntents().any { it.component?.className == HomeActivity::class.java.name } }
                intended(hasComponent(HomeActivity::class.java.name))
                assertEquals(first, storage.activeUser.teamcityUrl)
                assertEquals(1, storage.userAccounts.size)
            }
        } finally {
            Intents.release()
        }
    }

    @Test fun lastRemovalClearsStorageAndOpensLogin() {
        storage.clearAll()
        storage.saveGuestUserAccountAndSetItAsActive(first, false)
        Intents.init()
        try {
            intending(hasComponent(LoginActivity::class.java.name)).respondWith(ActivityResult(Activity.RESULT_OK, null))
            launch().use {
                compose.waitUntil(5_000) { compose.onAllNodesWithText(first).fetchSemanticsNodes().isNotEmpty() }
                row(first).performClick()
                awaitDialog()
                compose.onNodeWithText("Remove").performClick()
                compose.waitUntil(10_000) { storage.userAccounts.isEmpty() }
                compose.waitUntil(10_000) {
                    val failure = compose.onAllNodesWithTag("accounts:remove_error").fetchSemanticsNodes().isNotEmpty()
                    check(!failure) { "Last-account cleanup failed" }
                    Intents.getIntents().any { it.component?.className == LoginActivity::class.java.name }
                }
                intended(hasComponent(LoginActivity::class.java.name))
                assertFalse(storage.hasUserAccounts())
            }
        } finally {
            Intents.release()
        }
    }

    @Test fun addAccountOpensTheExistingCreateAccountScreen() {
        Intents.init()
        try {
            intending(hasComponent(CreateAccountActivity::class.java.name)).respondWith(ActivityResult(Activity.RESULT_OK, null))
            launch().use {
                awaitRows()
                compose.onNodeWithTag("accounts:add").performClick()
                intended(hasComponent(CreateAccountActivity::class.java.name))
            }
        } finally {
            Intents.release()
        }
    }

    @Test fun removalDialogAndSelectedIdentitySurviveRecreation() {
        launch().use { scenario ->
            awaitRows()
            row(first).performClick()
            awaitDialog()
            scenario.recreate()
            awaitDialog()
            compose.onNodeWithText("Remove").performClick()
            compose.waitUntil(5_000) { compose.onAllNodesWithText(first).fetchSemanticsNodes().isEmpty() }
            assertEquals(second, storage.activeUser.teamcityUrl)
        }
    }

    @Test fun emptyStorageRendersAnEmptyListAndKeepsAddAction() {
        storage.clearAll()
        launch().use {
            compose.waitUntil(5_000) { compose.onAllNodesWithTag("accounts:empty").fetchSemanticsNodes().isNotEmpty() }
            compose.onNodeWithTag("accounts:add").assertIsDisplayed()
            compose.onNodeWithText("Guest user").assertDoesNotExist()
        }
    }
}
