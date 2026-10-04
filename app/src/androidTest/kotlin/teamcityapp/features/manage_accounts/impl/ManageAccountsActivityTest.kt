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
import com.github.vase4kin.teamcityapp.account.create.view.CreateAccountActivity
import com.github.vase4kin.teamcityapp.dagger.modules.Mocks
import com.github.vase4kin.teamcityapp.helper.HiltApiTestRule
import com.github.vase4kin.teamcityapp.home.view.HomeActivity
import com.github.vase4kin.teamcityapp.login.view.LoginActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

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
    private fun row(url: String) = compose.onNodeWithTag("accounts:row:$url:Guest user")
    private fun awaitRows() {
        compose.waitUntil(5_000) { compose.onAllNodesWithText(second).fetchSemanticsNodes().isNotEmpty() }
    }
    private fun awaitDialog() {
        compose.waitUntil(5_000) { compose.onAllNodesWithTag("accounts:dialog").fetchSemanticsNodes().isNotEmpty() }
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
            compose.onNodeWithText("SURE").assertDoesNotExist()
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
            compose.onNodeWithText("NOPE").performClick()
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
            compose.onNodeWithText("SURE").performClick()
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
                compose.onNodeWithText(second, useUnmergedTree = true).performClick()
                awaitDialog()
                compose.onNodeWithText("SURE").performClick()
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
                compose.onNodeWithText("SURE").performClick()
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
                compose.onNodeWithContentDescription("Add account").performClick()
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
            compose.onNodeWithText("SURE").performClick()
            compose.waitUntil(5_000) { compose.onAllNodesWithText(first).fetchSemanticsNodes().isEmpty() }
            assertEquals(second, storage.activeUser.teamcityUrl)
        }
    }

    @Test fun emptyStorageRendersAnEmptyListAndKeepsAddAction() {
        storage.clearAll()
        launch().use {
            compose.waitUntil(5_000) { compose.onAllNodesWithTag("accounts:empty").fetchSemanticsNodes().isNotEmpty() }
            compose.onNodeWithContentDescription("Add account").assertIsDisplayed()
            compose.onNodeWithText("Guest user").assertDoesNotExist()
        }
    }
}
