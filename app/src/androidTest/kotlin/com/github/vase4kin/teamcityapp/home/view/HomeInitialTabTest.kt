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

package com.github.vase4kin.teamcityapp.home.view

import android.content.Intent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.TeamCityApplicationBase
import com.github.vase4kin.teamcityapp.base.extractor.BundleExtractorValues
import com.github.vase4kin.teamcityapp.dagger.modules.FakeTeamCityServiceImpl
import com.github.vase4kin.teamcityapp.dagger.modules.Mocks
import com.github.vase4kin.teamcityapp.helper.HiltApiTestRule
import com.github.vase4kin.teamcityapp.helper.TestUtils
import com.github.vase4kin.teamcityapp.navigation.IncomingDestinationResolver
import com.github.vase4kin.teamcityapp.navigation.MainActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import javax.inject.Inject
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import teamcityapp.features.build_log.api.BuildLogConsentStorage
import teamcityapp.features.home.api.HomeTab
import teamcityapp.features.settings.api.navigation.SettingsDestination
import teamcityapp.libraries.navigation.DestinationRegistry

/** Incoming Home tabs apply only to a fresh host; restored navigation owns recreation. */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class HomeInitialTabTest {
    @JvmField
    @Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @JvmField
    @Rule(order = 1)
    val apiRule = HiltApiTestRule(hiltRule) { FakeTeamCityServiceImpl() }

    @JvmField
    @Rule(order = 2)
    val compose = createEmptyComposeRule()

    @Inject lateinit var consent: BuildLogConsentStorage

    @Inject lateinit var registry: DestinationRegistry

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Before fun setUp() {
        TestUtils.disableOnboarding()
        val app = context.applicationContext as TeamCityApplicationBase
        app.appInjector.sharedUserStorage().apply {
            clearAll()
            saveGuestUserAccountAndSetItAsActive(Mocks.URL, false)
        }
        app.appInjector.cacheManager().evictAllCache()
        app.getSharedPreferences("rateTheAppPref", android.content.Context.MODE_PRIVATE)
            .edit().putBoolean("rated", true).commit()
    }

    @Test fun freshHomeConsumesTheRequestedFavoritesTab() {
        launch(HomeTab.Favorites.ordinal).use { scenario ->
            awaitDisplayed("favorites:up")
            assertSelected(scenario, HomeTab.Favorites)
        }
    }

    @Test fun recreationKeepsTheChosenTabInsteadOfReapplyingTheLaunchTab() {
        launch(HomeTab.Favorites.ordinal).use { scenario ->
            awaitDisplayed("favorites:up")
            compose.onNodeWithTag("home:tab:Queue").performClick()
            awaitDisplayed("build_queue:drawer")
            assertSelected(scenario, HomeTab.Queue)
            // The unchanged Intent still requests Favorites; restoration must take precedence.
            scenario.onActivity { assertEquals(HomeTab.Favorites.ordinal, it.intent.getIntExtra(IncomingDestinationResolver.HOME_TAB, -1)) }
            scenario.recreate()
            awaitDisplayed("build_queue:drawer")
            assertSelected(scenario, HomeTab.Queue)
        }
    }

    @Test fun negativeLaunchTabLeavesTheDefaultProjectsTabSelected() {
        launch(-1).use { scenario ->
            awaitDisplayed("navigation:up")
            assertSelected(scenario, HomeTab.Projects)
        }
    }

    @Test fun launchTabPastTheLastItemLeavesTheDefaultProjectsTabSelected() {
        launch(HomeTab.entries.size).use { scenario ->
            awaitDisplayed("navigation:up")
            assertSelected(scenario, HomeTab.Projects)
        }
    }

    @Test fun installedTabRequestReturnsToTheExistingHomeVisitAndRemovesTheSettingsVisit() {
        launch(HomeTab.Queue.ordinal).use { scenario ->
            awaitDisplayed("build_queue:drawer")
            var home: String? = null
            scenario.onActivity {
                home = it.navigationSnapshot!!.entries.single().id
                it.startActivity(Intent(it, MainActivity::class.java).putExtra(MainActivity.EXTRA_DESTINATION, registry.encodeDestination(SettingsDestination)))
            }
            awaitDisplayed("settings:theme")
            scenario.onActivity {
                it.startActivity(Intent().setClassName(it, IncomingDestinationResolver.HOME_COMPONENT).putExtra(IncomingDestinationResolver.HOME_TAB, HomeTab.Favorites.ordinal))
            }
            awaitDisplayed("favorites:up")
            assertSelected(scenario, HomeTab.Favorites)
            scenario.onActivity {
                assertEquals(home, it.navigationSnapshot!!.entries.single().id)
                assertTrue(it.supportFragmentManager.fragments.isEmpty())
            }
            scenario.recreate()
            awaitDisplayed("favorites:up")
            assertSelected(scenario, HomeTab.Favorites)
        }
    }

    @Test fun bothInstalledReloadPoliciesResetConsentOnceAndKeepTheRequestedTabThroughRecreation() {
        listOf(BundleExtractorValues.IS_REQUIRED_TO_RELOAD, BundleExtractorValues.IS_NEW_ACCOUNT_CREATED).forEach { flag ->
            runBlocking { consent.setAcknowledged(true) }
            ActivityScenario.launch<MainActivity>(Intent(context, MainActivity::class.java).setClassName(context, IncomingDestinationResolver.HOME_COMPONENT).putExtra(IncomingDestinationResolver.HOME_TAB, HomeTab.Queue.ordinal).putExtra(flag, true)).use { scenario ->
                awaitDisplayed("build_queue:drawer")
                assertSelected(scenario, HomeTab.Queue)
                assertFalse(runBlocking { consent.acknowledged() })
                scenario.onActivity { assertFalse(it.intent.hasExtra(flag)) }
                runBlocking { consent.setAcknowledged(true) }
                scenario.recreate()
                awaitDisplayed("build_queue:drawer")
                assertSelected(scenario, HomeTab.Queue)
                assertTrue(runBlocking { consent.acknowledged() })
            }
        }
    }

    private fun launch(index: Int): ActivityScenario<MainActivity> = ActivityScenario.launch(
        Intent(context, MainActivity::class.java).setClassName(context, IncomingDestinationResolver.HOME_COMPONENT).putExtra(IncomingDestinationResolver.HOME_TAB, index)
    )

    private fun awaitDisplayed(tag: String) {
        // Hidden Home roots remain composed, so existence alone cannot prove tab selection.
        compose.waitUntil(10_000) { runCatching { compose.onNodeWithTag(tag, useUnmergedTree = true).assertIsDisplayed() }.isSuccess }
        compose.onNodeWithTag(tag, useUnmergedTree = true).assertIsDisplayed()
    }

    private fun assertSelected(scenario: ActivityScenario<MainActivity>, tab: HomeTab) {
        compose.onNodeWithTag("home:tab:${tab.name}").assertIsSelected()
        scenario.onActivity { assertEquals(1, it.navigationSnapshot!!.entries.size) }
    }
}
