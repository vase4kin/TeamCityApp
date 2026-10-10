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

package com.github.vase4kin.teamcityapp.home.view

import android.app.Activity
import android.app.Instrumentation.ActivityResult
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.intent.Intents.getIntents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.vase4kin.teamcityapp.TeamCityApplicationBase
import com.github.vase4kin.teamcityapp.api.TeamCityService
import com.github.vase4kin.teamcityapp.buildlist.view.BuildListActivity
import com.github.vase4kin.teamcityapp.dagger.modules.AppModule
import com.github.vase4kin.teamcityapp.dagger.modules.FakeTeamCityServiceImpl
import com.github.vase4kin.teamcityapp.dagger.modules.Mocks
import com.github.vase4kin.teamcityapp.helper.CustomIntentsTestRule
import com.github.vase4kin.teamcityapp.helper.HiltApiTestRule
import com.github.vase4kin.teamcityapp.helper.TestUtils
import com.github.vase4kin.teamcityapp.home.dagger.RateTheAppTestAppModule
import com.github.vase4kin.teamcityapp.navigation.api.*
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.testing.*
import dagger.hilt.components.SingletonComponent
import io.reactivex.Single
import org.hamcrest.core.AllOf.allOf
import org.junit.*
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.Spy
import teamcityapp.features.navigation.api.NavigationNavigation
import teamcityapp.features.navigation.impl.R as NavigationR
import teamcityapp.libraries.remote.RemoteService

@UninstallModules(AppModule::class)
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class RateTheAppTest {
    @JvmField
    @Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @JvmField
    @Rule(order = 1)
    val mockitoRule = org.mockito.junit.MockitoJUnit.rule().strictness(org.mockito.quality.Strictness.LENIENT)

    @JvmField
    @Rule(order = 2)
    val apiRule = HiltApiTestRule(hiltRule) { teamCityService }

    @JvmField
    @Rule(order = 3)
    val activityRule = CustomIntentsTestRule(HomeActivity::class.java)

    @JvmField
    @Rule(order = 4)
    val compose = createEmptyComposeRule()

    @Spy private val teamCityService: TeamCityService = FakeTeamCityServiceImpl()

    @BindValue @Mock
    lateinit var remoteService: RemoteService
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val sharedPreferences: SharedPreferences get() = context.getSharedPreferences("rateTheAppPref", Context.MODE_PRIVATE)

    companion object {
        @JvmStatic @BeforeClass
        fun disableOnboarding() = TestUtils.disableOnboarding()
    }

    @Before fun setUp() {
        val app = context.applicationContext as TeamCityApplicationBase
        app.appInjector.sharedUserStorage().apply {
            clearAll()
            saveGuestUserAccountAndSetItAsActive(Mocks.URL, false)
        }
        sharedPreferences.edit().clear().commit()
        `when`(remoteService.isNotChurn()).thenReturn(true)
    }

    @Test fun eligibleRatingAppearsBetweenFirstAndSecondEntries() {
        activityRule.launchActivity(null)
        awaitRating()
        compose.onNodeWithText(text(NavigationR.string.navigation_rate_title)).assertIsDisplayed()
        compose.onNodeWithText(text(NavigationR.string.navigation_rate_description)).assertIsDisplayed()
        val first = row("project:id").fetchSemanticsNode().boundsInRoot
        val card = compose.onNodeWithTag("navigation:rating").fetchSemanticsNode().boundsInRoot
        val second = row("configuration:build_type_id").fetchSemanticsNode().boundsInRoot
        Assert.assertTrue(first.bottom <= card.top)
        Assert.assertTrue(card.bottom <= second.top)
    }

    @Test fun cancelPersistsChoiceAndLeavesEntriesUsable() {
        activityRule.launchActivity(null)
        awaitRating()
        compose.onNodeWithText(text(NavigationR.string.navigation_rate_cancel)).performClick()
        awaitHandled()
        row("project:id").assertTextContains("Description")
        row("configuration:build_type_id").assertIsDisplayed()
        row("project:id").performClick()
        awaitRows()
        compose.onNodeWithTag("navigation:rating").assertDoesNotExist()
        intended(allOf(hasComponent(NavigationNavigation.LEGACY_ACTIVITY), hasExtra(NavigationNavigation.PROJECT_NAME, "Project")))
    }

    @Test fun ratePersistsChoiceAndLaunchesTheStore() {
        activityRule.launchActivity(null)
        awaitRating()
        intending(hasAction(Intent.ACTION_VIEW)).respondWith(ActivityResult(Activity.RESULT_OK, null))
        compose.onNodeWithText(text(NavigationR.string.navigation_rate_now)).performClick()
        awaitHandled()
        compose.waitUntil(10_000) { getIntents().any { it.action == Intent.ACTION_VIEW && it.dataString == "market://details?id=${context.packageName}" } }
        intended(allOf(hasAction(Intent.ACTION_VIEW), hasData("market://details?id=${context.packageName}")))
        row("project:id").assertIsDisplayed()
    }

    @Test fun cancelledRatingDoesNotBreakConfigurationNavigation() {
        activityRule.launchActivity(null)
        awaitRating()
        compose.onNodeWithText(text(NavigationR.string.navigation_rate_cancel)).performClick()
        awaitHandled()
        row("configuration:build_type_id").performClick()
        intended(hasComponent(BuildListActivity::class.java.name))
        TestUtils.matchToolbarTitle("build type")
    }

    @Test fun emptyProjectDoesNotShowRating() {
        `when`(teamCityService.listBuildTypes(anyString())).thenReturn(Single.just(NavigationNode(Projects(emptyList()), BuildTypes(emptyList()))))
        activityRule.launchActivity(null)
        compose.waitUntil(10_000) { compose.onAllNodesWithText(text(NavigationR.string.navigation_empty)).fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithTag("navigation:rating").assertDoesNotExist()
    }

    @Test fun ineligibleUserDoesNotSeeRating() {
        `when`(remoteService.isNotChurn()).thenReturn(false)
        activityRule.launchActivity(null)
        awaitRows()
        compose.onNodeWithTag("navigation:rating").assertDoesNotExist()
        Assert.assertFalse(sharedPreferences.getBoolean("rated", false))
    }

    @Test fun cancelledChoiceSurvivesRecreationWithoutReloadingEntries() {
        ActivityScenario.launch<HomeActivity>(Intent(context, HomeActivity::class.java)).use { scenario ->
            awaitRating()
            compose.onNodeWithText(text(NavigationR.string.navigation_rate_cancel)).performClick()
            awaitHandled()
            scenario.recreate()
            awaitRows()
            compose.onNodeWithTag("navigation:rating").assertDoesNotExist()
            Assert.assertTrue(sharedPreferences.getBoolean("rated", false))
            verify(teamCityService, times(1)).listBuildTypes(NavigationNavigation.ROOT_PROJECT_ID)
        }
    }

    private fun row(identity: String) = compose.onNodeWithTag("navigation:row:$identity")
    private fun awaitRows() {
        compose.waitUntil(10_000) { compose.onAllNodesWithTag("navigation:row:project:id").fetchSemanticsNodes().isNotEmpty() }
    }
    private fun awaitRating() {
        compose.waitUntil(10_000) { compose.onAllNodesWithTag("navigation:rating").fetchSemanticsNodes().isNotEmpty() }
    }
    private fun awaitHandled() {
        compose.waitUntil(10_000) {
            sharedPreferences.getBoolean("rated", false) && compose.onAllNodesWithTag("navigation:rating").fetchSemanticsNodes().isEmpty()
        }
    }
    private fun text(id: Int) = context.getString(id)

    @Module(includes = [RateTheAppTestAppModule::class])
    @InstallIn(SingletonComponent::class)
    object TestBindings
}
