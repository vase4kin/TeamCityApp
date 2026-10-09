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
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.*
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.TeamCityApplicationBase
import com.github.vase4kin.teamcityapp.base.extractor.BundleExtractorValues
import com.github.vase4kin.teamcityapp.dagger.modules.Mocks
import com.github.vase4kin.teamcityapp.helper.HiltApiTestRule
import com.github.vase4kin.teamcityapp.helper.TestUtils
import com.github.vase4kin.teamcityapp.home.view.HomeActivity
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import teamcityapp.features.about.impl.AboutActivity
import teamcityapp.features.create_account.impl.CreateAccountActivity
import teamcityapp.features.drawer.impl.navigation.DrawerNavigationImpl
import teamcityapp.features.manage_accounts.impl.ManageAccountsActivity
import teamcityapp.features.settings.impl.SettingsActivity

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
    private fun launch() = ActivityScenario.launch<HomeActivity>(Intent(app, HomeActivity::class.java))
    private fun open() {
        onView(withContentDescription(R.string.content_navigation_content_description)).perform(click())
        awaitRows()
    }
    private fun awaitRows(url: String = first) {
        compose.waitUntil(5_000) { compose.onAllNodesWithText(url, useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty() }
    }
    private fun fragment(scenario: ActivityScenario<HomeActivity>, action: (DrawerBottomSheetDialogFragment) -> Unit) {
        scenario.onActivity { action(it.supportFragmentManager.findFragmentByTag(DrawerNavigationImpl.TAG) as DrawerBottomSheetDialogFragment) }
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
            Intents.init()
            try {
                Intents.intending(hasComponent(HomeActivity::class.java.name)).respondWith(ActivityResult(Activity.RESULT_OK, null))
                row(first).performClick()
                compose.waitUntil(5_000) { Intents.getIntents().any { it.component?.className == HomeActivity::class.java.name } }
                Intents.intended(hasComponent(HomeActivity::class.java.name))
                assertEquals(first, storage.activeUser.teamcityUrl)
                assertTrue(Intents.getIntents().single { it.component?.className == HomeActivity::class.java.name }.getBooleanExtra(BundleExtractorValues.IS_REQUIRED_TO_RELOAD, false))
                compose.waitUntil(5_000) { compose.onAllNodesWithTag("drawer:list").fetchSemanticsNodes(atLeastOneRootRequired = false).isEmpty() }
                scenario.moveToState(Lifecycle.State.CREATED)
                scenario.moveToState(Lifecycle.State.RESUMED)
                assertEquals(1, Intents.getIntents().count { it.component?.className == HomeActivity::class.java.name })
            } finally {
                Intents.release()
            }
        }
    }
    private fun navigation(tag: String, destination: Class<*>) {
        launch().use {
            open()
            Intents.init()
            try {
                Intents.intending(hasComponent(destination.name)).respondWith(ActivityResult(Activity.RESULT_OK, null))
                compose.onNodeWithTag("drawer:$tag").performScrollTo().performClick()
                Intents.intended(hasComponent(destination.name))
            } finally {
                Intents.release()
            }
        }
    }

    @Test fun aboutOpensAboutActivity() = navigation("about", AboutActivity::class.java)

    @Test fun settingsOpensSettingsActivity() = navigation("settings", SettingsActivity::class.java)

    @Test fun manageOpensManageAccountsActivity() = navigation("manage", ManageAccountsActivity::class.java)

    @Test fun addOpensCreateAccountActivity() = navigation("add", CreateAccountActivity::class.java)

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

    @Test fun sheetSurvivesHostRecreationWithAllActions() {
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

    @Test fun returningToSheetReadsNewAccounts() {
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

    @Test fun expandedSheetScrollsManyAccountsToFooterAndSupportsNativeDrag() {
        (1..20).forEach { storage.saveGuestUserAccountAndSetItAsActive("https://teamcity.example/server-$it", it % 2 == 0) }
        launch().use { scenario ->
            open()
            fragment(scenario) { (it.dialog as BottomSheetDialog).behavior.state = BottomSheetBehavior.STATE_EXPANDED }
            compose.waitUntil(5_000) {
                var expanded = false
                fragment(scenario) { expanded = (it.dialog as BottomSheetDialog).behavior.state == BottomSheetBehavior.STATE_EXPANDED }
                expanded
            }
            compose.onNodeWithTag("drawer:list").performScrollToKey("footer")
            compose.onNodeWithTag("drawer:privacy").performScrollTo().assertIsDisplayed()
            compose.onNodeWithTag("drawer:rate").assertIsDisplayed()
            fragment(scenario) { assertTrue((it.dialog as BottomSheetDialog).behavior.isDraggable) }
        }
    }

    @Test fun swipingContentExpandsTheNativeSheet() {
        (1..20).forEach { storage.saveGuestUserAccountAndSetItAsActive("https://teamcity.example/server-$it", false) }
        launch().use { scenario ->
            open()
            compose.onNodeWithTag("drawer:list").performTouchInput {
                swipe(start = Offset(center.x, height * 0.4f), end = Offset(center.x, 20f), durationMillis = 500)
            }
            compose.waitUntil(5_000) {
                var expanded = false
                fragment(scenario) { expanded = (it.dialog as BottomSheetDialog).behavior.state == BottomSheetBehavior.STATE_EXPANDED }
                expanded
            }
            compose.onNodeWithTag("drawer:list").performScrollToKey("footer")
            compose.onNodeWithTag("drawer:privacy").performScrollTo().assertIsDisplayed()
        }
    }
}
