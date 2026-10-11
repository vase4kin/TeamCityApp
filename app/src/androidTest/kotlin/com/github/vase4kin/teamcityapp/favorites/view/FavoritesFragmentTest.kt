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

package com.github.vase4kin.teamcityapp.favorites.view

import android.content.Intent
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.TeamCityApplicationBase
import com.github.vase4kin.teamcityapp.api.TeamCityService
import com.github.vase4kin.teamcityapp.base.extractor.BundleExtractorValues
import com.github.vase4kin.teamcityapp.dagger.modules.FakeTeamCityServiceImpl
import com.github.vase4kin.teamcityapp.dagger.modules.Mocks
import com.github.vase4kin.teamcityapp.helper.CustomIntentsTestRule
import com.github.vase4kin.teamcityapp.helper.HiltApiTestRule
import com.github.vase4kin.teamcityapp.helper.TestUtils
import com.github.vase4kin.teamcityapp.home.view.HomeActivity
import com.github.vase4kin.teamcityapp.navigation.api.BuildType
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.reactivex.Single
import io.reactivex.subjects.SingleSubject
import org.hamcrest.core.AllOf.allOf
import org.junit.*
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.*
import org.mockito.Spy
import teamcityapp.features.build_history.api.BuildHistoryNavigation
import teamcityapp.features.build_history.impl.R as HistoryR
import teamcityapp.features.favorites.impl.R as FavoritesR
import teamcityapp.features.navigation.api.NavigationNavigation
import teamcityapp.features.navigation.impl.R as NavigationR

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class FavoritesFragmentTest {
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
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val app get() = context.applicationContext as TeamCityApplicationBase
    private val storage: SharedUserStorage get() = app.appInjector.sharedUserStorage()

    companion object {
        @JvmStatic @BeforeClass
        fun disableOnboarding() = TestUtils.disableOnboarding()
    }

    @Before fun setUp() {
        storage.clearAll()
        storage.saveGuestUserAccountAndSetItAsActive(Mocks.URL, false)
        app.getSharedPreferences("rateTheAppPref", android.content.Context.MODE_PRIVATE).edit().putBoolean("rated", true).commit()
    }

    @Test fun emptyFavoritesShowTheComposeToolbarAndEmptyMessage() {
        openFavorites()
        assertTextVisible(text(FavoritesR.string.favorites_title))
        assertTextVisible(text(FavoritesR.string.favorites_empty))
        Assert.assertTrue(storage.favoriteBuildTypeIds.isEmpty())
    }

    @Test fun favoritesToolbarOpensTheDrawer() {
        openFavorites()
        assertTextVisible(text(FavoritesR.string.favorites_empty))
        compose.onNodeWithTag("favorites:up").performClick()
        compose.waitUntil(10_000) { compose.onAllNodesWithTag("drawer:list").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithTag("drawer:settings").assertIsDisplayed()
    }

    @Test fun addFavoritesFabOffersTheProjectsTab() {
        openFavorites()
        assertTextVisible(text(FavoritesR.string.favorites_empty))
        onView(withId(R.id.home_floating_action_button)).perform(click())
        onView(withText(R.string.text_info_add)).check(matches(isDisplayed()))
        onView(withText(R.string.text_info_add_action)).perform(click())
        awaitProjectRow()
        compose.onNodeWithText(text(NavigationR.string.navigation_projects_title), useUnmergedTree = true).assertIsDisplayed()
    }

    @Test fun addFromBuildListShowsTheConfigurationGroupedUnderItsProject() {
        activityRule.launchActivity(null)
        awaitProjectRow()
        compose.onNodeWithTag("navigation:row:configuration:build_type_id").performClick()
        awaitBuildList()
        compose.onNodeWithTag("history:favorite").performClick()
        compose.waitUntil(10_000) { "build_type_id" in storage.favoriteBuildTypeIds }
        assertTextVisible(text(HistoryR.string.history_favorite_added))
        compose.onNodeWithText(text(HistoryR.string.history_view)).performClick()
        awaitConfiguration("build_type_id")
        project("projectId123").assertTextEquals("Secret project")
        configuration("build_type_id").assertTextContains("build type")
        Assert.assertEquals(listOf("build_type_id"), storage.favoriteBuildTypeIds)
    }

    @Test fun removingAFavoriteInBuildListRefreshesFavoritesOnReturn() {
        storage.addBuildTypeToFavorites("build_type_id")
        openFavorites()
        awaitConfiguration("build_type_id")
        configuration("build_type_id").performClick()
        awaitBuildList()
        compose.onNodeWithTag("history:favorite").performClick()
        compose.waitUntil(10_000) { storage.favoriteBuildTypeIds.isEmpty() }
        assertTextVisible(text(HistoryR.string.history_favorite_removed))
        pressBack()
        assertTextVisible(text(FavoritesR.string.favorites_empty))
        Assert.assertTrue(storage.favoriteBuildTypeIds.isEmpty())
    }

    @Test fun configurationNavigationKeepsTheExistingBuildListArguments() {
        storage.addBuildTypeToFavorites("build_type_id")
        openFavorites()
        awaitConfiguration("build_type_id")
        configuration("build_type_id").performClick()
        intended(allOf(hasComponent(BuildHistoryNavigation.LEGACY_ACTIVITY), hasExtra(BundleExtractorValues.ID, "build_type_id"), hasExtra(BundleExtractorValues.NAME, "build type")))
        assertTextVisible("build type")
    }

    @Test fun projectHeaderOpensTheInstalledRecursiveNavigationAlias() {
        storage.addBuildTypeToFavorites("build_type_id")
        openFavorites()
        awaitConfiguration("build_type_id")
        project("projectId123").performClick()
        intended(allOf(hasComponent(NavigationNavigation.LEGACY_ACTIVITY), hasExtra(NavigationNavigation.PROJECT_ID, "projectId123"), hasExtra(NavigationNavigation.PROJECT_NAME, "Secret project")))
        assertTextVisible("Secret project")
    }

    @Test fun partialFailureKeepsSavedIdsAndSuccessfulRowsAndCanBeRetried() {
        storage.addBuildTypeToFavorites("good")
        storage.addBuildTypeToFavorites("missing")
        `when`(teamCityService.buildType("good")).thenReturn(Single.just(build("good", "Available configuration")))
        `when`(teamCityService.buildType("missing")).thenReturn(Single.error(RuntimeException("offline")))
        openFavorites()
        awaitConfiguration("good")
        compose.onNodeWithTag("favorites:partial").assertIsDisplayed()
        configuration("good").assertTextContains("Available configuration")
        configuration("missing").assertDoesNotExist()
        Assert.assertEquals(listOf("good", "missing"), storage.favoriteBuildTypeIds)
        `when`(teamCityService.buildType("missing")).thenReturn(Single.just(build("missing", "Recovered configuration")))
        compose.onNodeWithText(text(FavoritesR.string.favorites_retry)).performClick()
        awaitConfiguration("missing")
        compose.onNodeWithTag("favorites:partial").assertDoesNotExist()
        configuration("good").assertIsDisplayed()
        Assert.assertEquals(listOf("good", "missing"), storage.favoriteBuildTypeIds)
    }

    @Test fun allFailedFavoritesShowAnErrorKeepSavedIdsAndCanBeRetried() {
        storage.addBuildTypeToFavorites("good")
        storage.addBuildTypeToFavorites("missing")
        `when`(teamCityService.buildType(anyString())).thenReturn(Single.error(RuntimeException("offline")))
        openFavorites()
        assertTextVisible(text(FavoritesR.string.favorites_all_failed))
        assertTextVisible(text(teamcityapp.libraries.theme.R.string.error_load_title))
        compose.onNodeWithText(text(FavoritesR.string.favorites_empty)).assertDoesNotExist()
        Assert.assertEquals(listOf("good", "missing"), storage.favoriteBuildTypeIds)
        `when`(teamCityService.buildType("good")).thenReturn(Single.just(build("good", "Recovered first")))
        `when`(teamCityService.buildType("missing")).thenReturn(Single.just(build("missing", "Recovered second")))
        compose.onNodeWithText(text(teamcityapp.libraries.theme.R.string.action_retry)).performClick()
        awaitConfiguration("missing")
        configuration("good").assertIsDisplayed()
        compose.onNodeWithText(text(FavoritesR.string.favorites_all_failed)).assertDoesNotExist()
    }

    @Test fun allFailedRefreshRetainsTheLastCompletedRowsUntilRetrySucceeds() {
        storage.addBuildTypeToFavorites("build_type_id")
        openFavorites()
        awaitConfiguration("build_type_id")
        `when`(teamCityService.buildType(anyString())).thenReturn(Single.error(RuntimeException("offline refresh")))
        compose.onNodeWithTag("favorites:list").performTouchInput { swipeDown() }
        assertTextVisible(text(FavoritesR.string.favorites_all_failed))
        configuration("build_type_id").assertIsDisplayed()
        Assert.assertEquals(listOf("build_type_id"), storage.favoriteBuildTypeIds)
        `when`(teamCityService.buildType("build_type_id")).thenReturn(Single.just(build("build_type_id", "Updated favorite")))
        compose.onNodeWithText(text(teamcityapp.libraries.list_ui.R.string.list_action_retry)).performClick()
        awaitConfiguration("build_type_id")
        assertTextVisible("Updated favorite")
        compose.onNodeWithText(text(FavoritesR.string.favorites_all_failed)).assertDoesNotExist()
    }

    @Test fun recreatingFavoritesRetainsRowsWithoutAnotherRequest() {
        storage.addBuildTypeToFavorites("build_type_id")
        ActivityScenario.launch<HomeActivity>(Intent(context, HomeActivity::class.java)).use { scenario ->
            awaitProjectRow()
            clickFavorites()
            awaitConfiguration("build_type_id")
            scenario.recreate()
            awaitConfiguration("build_type_id")
            verify(teamCityService, times(1)).buildType("build_type_id")
            verify(teamCityService, times(1)).listBuildTypes(NavigationNavigation.ROOT_PROJECT_ID)
        }
    }

    @Test fun homeRecreationRetainsBothTabsAndTheirSelectedState() {
        storage.addBuildTypeToFavorites("build_type_id")
        ActivityScenario.launch<HomeActivity>(Intent(context, HomeActivity::class.java)).use { scenario ->
            awaitProjectRow()
            clickFavorites()
            awaitConfiguration("build_type_id")
            scenario.recreate()
            awaitConfiguration("build_type_id")
            compose.onNodeWithText(text(FavoritesR.string.favorites_title), useUnmergedTree = true).assertIsDisplayed()
            verify(teamCityService, times(1)).buildType("build_type_id")
            onView(withId(R.id.projects)).perform(click())
            awaitProjectRow()
            scenario.recreate()
            awaitProjectRow()
            compose.onNodeWithText(text(NavigationR.string.navigation_projects_title), useUnmergedTree = true).assertIsDisplayed()
            verify(teamCityService, times(1)).listBuildTypes(NavigationNavigation.ROOT_PROJECT_ID)
            clickFavorites()
            awaitConfiguration("build_type_id")
            compose.waitUntil(10_000) { mockingDetails(teamCityService).invocations.count { it.method.name == "buildType" && it.arguments.contentEquals(arrayOf("build_type_id")) } == 2 }
            scenario.recreate()
            awaitConfiguration("build_type_id")
            verify(teamCityService, times(2)).buildType("build_type_id")
        }
    }

    @Test fun sameServerDifferentUserCancelsTheOldFavoritesBatchOnAccountSwitch() {
        storage.clearAll()
        saveUser("alice")
        storage.addBuildTypeToFavorites("alice-build")
        saveUser("bob")
        storage.addBuildTypeToFavorites("bob-build")
        storage.setUserActive(Mocks.URL, "alice")
        val alice = SingleSubject.create<BuildType>()
        val bob = SingleSubject.create<BuildType>()
        `when`(teamCityService.buildType("alice-build")).thenReturn(alice)
        `when`(teamCityService.buildType("bob-build")).thenReturn(bob)
        ActivityScenario.launch<HomeActivity>(Intent(context, HomeActivity::class.java)).use { scenario ->
            awaitProjectRow()
            clickFavorites()
            compose.waitUntil(10_000) { alice.hasObservers() }
            scenario.onActivity { activity ->
                storage.setUserActive(Mocks.URL, "bob")
                activity.startActivity(
                    Intent(activity, HomeActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        .putExtra(BundleExtractorValues.IS_REQUIRED_TO_RELOAD, true)
                )
            }
            awaitProjectRow()
            compose.waitUntil(10_000) { !alice.hasObservers() }
            clickFavorites()
            compose.waitUntil(10_000) { bob.hasObservers() }
            // A late result from Alice must never appear in Bob's restored tab.
            alice.onSuccess(build("alice-build", "Alice private configuration"))
            bob.onSuccess(build("bob-build", "Bob configuration"))
            awaitConfiguration("bob-build")
            configuration("bob-build").assertTextContains("Bob configuration")
            configuration("alice-build").assertDoesNotExist()
            Assert.assertEquals("bob", storage.activeUser.userName)
            Assert.assertEquals(listOf("bob-build"), storage.favoriteBuildTypeIds)
            Assert.assertEquals(listOf("alice-build"), storage.userAccounts.single { it.userName == "alice" }.buildTypeIds)
            verify(teamCityService, times(1)).buildType("alice-build")
            verify(teamCityService, times(1)).buildType("bob-build")
        }
    }

    private fun awaitBuildList() {
        compose.waitUntil(10_000) {
            compose.onAllNodesWithTag("history:list").fetchSemanticsNodes().isNotEmpty() &&
                compose.onAllNodesWithTag("history:favorite").fetchSemanticsNodes().singleOrNull()?.config?.contains(androidx.compose.ui.semantics.SemanticsProperties.Disabled) == false
        }
        compose.onNodeWithTag("history:favorite").assertIsEnabled()
    }

    private fun saveUser(name: String) {
        var success = false
        storage.saveUserAccountAndSetItAsActive(
            Mocks.URL,
            name,
            "password",
            false,
            object : SharedUserStorage.OnStorageListener {
                override fun onSuccess() {
                    success = true
                }
                override fun onFail() {
                    Assert.fail("Could not save fixture account")
                }
            }
        )
        Assert.assertTrue(success)
    }
    private fun build(id: String, name: String) = Mocks.buildTypeMock().apply {
        setId(id)
        this.name = name
    }
    private fun project(id: String) = compose.onNodeWithTag("favorites:project:$id")
    private fun configuration(id: String) = compose.onNodeWithTag("favorites:configuration:$id")
    private fun openFavorites() {
        activityRule.launchActivity(null)
        awaitProjectRow()
        clickFavorites()
    }
    private fun clickFavorites() {
        onView(withId(R.id.favorites)).perform(click())
    }
    private fun awaitProjectRow() {
        compose.waitUntil(10_000) { compose.onAllNodesWithTag("navigation:row:project:id").fetchSemanticsNodes().isNotEmpty() }
    }
    private fun awaitConfiguration(id: String) {
        compose.waitUntil(10_000) { compose.onAllNodesWithTag("favorites:configuration:$id").fetchSemanticsNodes().isNotEmpty() }
    }
    private fun assertTextVisible(value: String) {
        compose.waitUntil(10_000) { compose.onAllNodesWithText(value, useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText(value, useUnmergedTree = true).assertIsDisplayed()
    }
    private fun text(id: Int) = context.getString(id)
}
