/*
 * Copyright 2020 Andrey Tolpeev
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

package teamcityapp.features.drawer.impl
import android.app.Activity
import android.app.Instrumentation.ActivityResult
import android.content.Intent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.lifecycle.Lifecycle
import androidx.navigation3.runtime.NavKey
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.TeamCityApplicationBase
import com.github.vase4kin.teamcityapp.base.extractor.BundleExtractorValues
import com.github.vase4kin.teamcityapp.di.modules.Mocks
import com.github.vase4kin.teamcityapp.helper.HiltApiTestRule
import com.github.vase4kin.teamcityapp.helper.TestUtils
import com.github.vase4kin.teamcityapp.navigation.IncomingDestinationResolver
import com.github.vase4kin.teamcityapp.navigation.MainActivity
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import javax.inject.Inject
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import teamcityapp.features.about.api.navigation.AboutDestination
import teamcityapp.features.create_account.api.navigation.CreateAccountDestination
import teamcityapp.features.drawer.api.navigation.DrawerDestination
import teamcityapp.features.home.api.HomeDestination
import teamcityapp.features.manage_accounts.api.navigation.ManageAccountsDestination
import teamcityapp.features.settings.api.navigation.SettingsDestination
import teamcityapp.libraries.navigation.DestinationRegistry

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class DrawerTest {
    @JvmField
    @Rule(order = 0)
    val hilt = HiltAndroidRule(this)

    @JvmField
    @Rule(order = 1)
    val api = HiltApiTestRule(hilt)

    @JvmField
    @Rule(order = 2)
    val compose = createEmptyComposeRule()

    @Inject lateinit var destinationRegistry: DestinationRegistry
    private val instrumentation get() = InstrumentationRegistry.getInstrumentation()
    private val app get() = instrumentation.targetContext.applicationContext as TeamCityApplicationBase
    private val storage get() = app.appInjector.sharedUserStorage()
    private val first = Mocks.URL
    private val second = "https://teamcity.example/secondary"
    private fun row(url: String) = compose.onNodeWithTag("drawer:account:${url.length}:$url:Guest user")

    @Before fun before() {
        TestUtils.disableOnboarding()
        storage.clearAll()
        storage.saveGuestUserAccountAndSetItAsActive(first, false)
    }
    private fun launch() = ActivityScenario.launch<MainActivity>(Intent(app, MainActivity::class.java).setClassName(app, IncomingDestinationResolver.HOME_COMPONENT))
    private fun open() {
        compose.waitUntil(5_000) { runCatching { compose.onNodeWithTag("home:account").assertIsDisplayed() }.isSuccess }
        compose.onNodeWithTag("home:account").performClick()
        awaitRows()
    }
    private fun awaitRows(url: String = first) {
        compose.waitUntil(5_000) { compose.onAllNodesWithText(url, useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty() }
    }

    @Test fun activeAccountIsVisibleAndCannotBeSelected() {
        launch().use {
            open()
            row(first).assertHasNoClickAction()
            compose.onNode(SemanticsMatcher.expectValue(androidx.compose.ui.semantics.SemanticsProperties.StateDescription, "Current account")).assertIsDisplayed()
            assertEquals(first, storage.activeUser.teamcityUrl)
        }
    }

    @Test fun inactiveAccountSwitchesStorageAndRequestsOneHomeReload() {
        storage.saveGuestUserAccountAndSetItAsActive(second, true)
        launch().use { scenario ->
            open()
            row(second).assertHasNoClickAction()
            row(first).assertIsDisplayed()
            var previous: String? = null
            scenario.onActivity { previous = it.navigationSnapshot!!.entries.first().id }
            row(first).performClick()
            compose.waitUntil(10_000) { compose.onAllNodesWithTag("drawer:list").fetchSemanticsNodes().isEmpty() }
            compose.onNodeWithTag("home:tab:Projects").assertIsDisplayed()
            assertEquals(first, storage.activeUser.teamcityUrl)
            scenario.onActivity {
                assertEquals(HomeDestination(), it.navigationSnapshot!!.entries.single().destination)
                assertNotEquals(previous, it.navigationSnapshot!!.entries.single().id)
                assertTrue(it.supportFragmentManager.fragments.isEmpty())
            }
        }
    }
    private fun navigation(tag: String, destination: Class<*>, key: NavKey? = null) {
        launch().use { scenario ->
            open()
            compose.onNodeWithTag("drawer:$tag").performScrollTo().performClick()
            compose.waitUntil(10_000) { compose.onAllNodesWithTag("drawer:list").fetchSemanticsNodes().isEmpty() }
            scenario.onActivity {
                assertEquals(key, it.navigationSnapshot!!.entries.last().destination)
                assertEquals(HomeDestination(), it.navigationSnapshot!!.entries.first().destination)
                assertEquals(3, it.navigationSnapshot!!.entries.size)
                assertTrue(it.supportFragmentManager.fragments.isEmpty())
            }
        }
    }

    @Test fun aboutOpensAboutDestination() = navigation("about", MainActivity::class.java, AboutDestination)

    @Test fun settingsOpensSettingsDestination() = navigation("settings", MainActivity::class.java, SettingsDestination)

    @Test fun manageOpensManageAccountsDestination() = navigation("manage", MainActivity::class.java, ManageAccountsDestination)

    @Test fun addOpensCreateAccountDestination() = navigation("add", MainActivity::class.java, CreateAccountDestination)

    @Test fun privacyLaunchesTheExistingUrl() {
        launch().use {
            open()
            Intents.init()
            try {
                Intents.intending(hasAction(Intent.ACTION_VIEW)).respondWith(ActivityResult(Activity.RESULT_OK, null))
                compose.onNodeWithTag("drawer:privacy").performScrollTo().performClick()
                Intents.intended(hasData(app.getString(R.string.about_app_url_privacy)))
            } finally {
                Intents.release()
            }
        }
    }

    @Test fun ratingLaunchesTheStore() {
        launch().use {
            open()
            Intents.init()
            try {
                Intents.intending(hasAction(Intent.ACTION_VIEW)).respondWith(ActivityResult(Activity.RESULT_OK, null))
                compose.onNodeWithTag("drawer:rate").performScrollTo().performClick()
                assertTrue(Intents.getIntents().any { it.action == Intent.ACTION_VIEW && it.dataString?.contains("id=com.github.vase4kin.teamcityapp") == true })
            } finally {
                Intents.release()
            }
        }
    }

    @Test fun accountSurvivesHostRecreationWithAllActions() {
        launch().use { scenario ->
            open()
            scenario.recreate()
            awaitRows()
            row(first).assertHasNoClickAction()
            compose.onNodeWithTag("drawer:settings").assertIsDisplayed()
            compose.onNodeWithTag("drawer:privacy").performScrollTo().assertIsDisplayed()
        }
    }

    @Test fun backDismissesAndReopeningCreatesFreshViewResources() {
        launch().use {
            open()
            pressBack()
            compose.waitUntil(5_000) { compose.onAllNodesWithTag("drawer:list").fetchSemanticsNodes(atLeastOneRootRequired = false).isEmpty() }
            open()
            row(first).assertIsDisplayed()
            compose.onNodeWithTag("drawer:privacy").performScrollTo().assertIsDisplayed()
        }
    }

    @Test fun returningToAccountReadsNewAccounts() {
        launch().use { scenario ->
            open()
            scenario.moveToState(Lifecycle.State.CREATED)
            storage.saveGuestUserAccountAndSetItAsActive(second, true)
            scenario.moveToState(Lifecycle.State.RESUMED)
            awaitRows(second)
            row(second).assertHasNoClickAction()
            row(first).assertHasClickAction()
        }
    }

    @Test fun accountScrollsManyAccountsToFooterAndSupportsBack() {
        (1..20).forEach { storage.saveGuestUserAccountAndSetItAsActive("https://teamcity.example/server-$it", it % 2 == 0) }
        launch().use { scenario ->
            open()
            compose.onNodeWithTag("drawer:list").performScrollToKey("footer")
            compose.onNodeWithTag("drawer:privacy").performScrollTo().assertIsDisplayed()
            compose.onNodeWithTag("drawer:rate").assertIsDisplayed()
            compose.onNodeWithTag("navigation:sheet").assertDoesNotExist()
            compose.onNodeWithTag("account:back").performClick()
            compose.waitUntil(10_000) { compose.onAllNodesWithTag("drawer:list").fetchSemanticsNodes().isEmpty() }
            scenario.onActivity { assertEquals(1, it.navigationSnapshot!!.entries.size) }
        }
    }

    @Test fun swipingContentKeepsFooterActionsReachable() {
        (1..20).forEach { storage.saveGuestUserAccountAndSetItAsActive("https://teamcity.example/server-$it", false) }
        launch().use { scenario ->
            open()
            compose.onNodeWithTag("drawer:list").performTouchInput {
                swipe(start = Offset(center.x, height * 0.4f), end = Offset(center.x, 20f), durationMillis = 500)
            }
            compose.onNodeWithTag("drawer:list").performScrollToKey("footer")
            compose.onNodeWithTag("drawer:privacy").performScrollTo().assertIsDisplayed()
        }
    }
}
