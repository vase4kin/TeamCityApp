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
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.TeamCityApplicationBase
import com.github.vase4kin.teamcityapp.app_navigation.AppNavigationItem
import com.github.vase4kin.teamcityapp.dagger.modules.FakeTeamCityServiceImpl
import com.github.vase4kin.teamcityapp.dagger.modules.Mocks
import com.github.vase4kin.teamcityapp.helper.HiltApiTestRule
import com.github.vase4kin.teamcityapp.helper.TestUtils
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

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
        launch(AppNavigationItem.FAVORITES.ordinal).use { scenario ->
            awaitDisplayed("favorites:up")
            assertSelected(scenario, AppNavigationItem.FAVORITES)
        }
    }

    @Test fun recreationKeepsTheChosenTabInsteadOfReapplyingTheLaunchTab() {
        launch(AppNavigationItem.FAVORITES.ordinal).use { scenario ->
            awaitDisplayed("favorites:up")
            onView(withId(R.id.build_queue)).perform(click())
            awaitDisplayed("build_queue:drawer")
            assertSelected(scenario, AppNavigationItem.BUILD_QUEUE)
            // The unchanged Intent still requests Favorites; restoration must take precedence.
            scenario.onActivity { assertEquals(AppNavigationItem.FAVORITES.ordinal, it.intent.getIntExtra(HomeActivity.ARG_TAB, -1)) }
            scenario.recreate()
            awaitDisplayed("build_queue:drawer")
            assertSelected(scenario, AppNavigationItem.BUILD_QUEUE)
        }
    }

    @Test fun negativeLaunchTabLeavesTheDefaultProjectsTabSelected() {
        launch(-1).use { scenario ->
            awaitDisplayed("navigation:up")
            assertSelected(scenario, AppNavigationItem.PROJECTS)
        }
    }

    @Test fun launchTabPastTheLastItemLeavesTheDefaultProjectsTabSelected() {
        launch(AppNavigationItem.values().size).use { scenario ->
            awaitDisplayed("navigation:up")
            assertSelected(scenario, AppNavigationItem.PROJECTS)
        }
    }

    private fun launch(index: Int): ActivityScenario<HomeActivity> = ActivityScenario.launch(
        Intent(context, HomeActivity::class.java).putExtra(HomeActivity.ARG_TAB, index)
    )

    private fun awaitDisplayed(tag: String) {
        // Hidden Home roots remain composed, so existence alone cannot prove tab selection.
        compose.waitUntil(10_000) { runCatching { compose.onNodeWithTag(tag, useUnmergedTree = true).assertIsDisplayed() }.isSuccess }
        compose.onNodeWithTag(tag, useUnmergedTree = true).assertIsDisplayed()
    }

    private fun assertSelected(scenario: ActivityScenario<HomeActivity>, tab: AppNavigationItem) {
        scenario.onActivity { activity ->
            assertEquals(tab.id, activity.findViewById<BottomNavigationView>(R.id.navigation).selectedItemId)
        }
    }
}
