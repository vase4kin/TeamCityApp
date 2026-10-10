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

package com.github.vase4kin.teamcityapp.runningbuilds.view

import android.app.Activity
import android.content.Intent
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.TeamCityApplicationBase
import com.github.vase4kin.teamcityapp.api.TeamCityService
import com.github.vase4kin.teamcityapp.base.extractor.BundleExtractorValues
import com.github.vase4kin.teamcityapp.buildlist.api.Build
import com.github.vase4kin.teamcityapp.buildlist.api.Builds
import com.github.vase4kin.teamcityapp.dagger.modules.FakeTeamCityServiceImpl
import com.github.vase4kin.teamcityapp.dagger.modules.Mocks
import com.github.vase4kin.teamcityapp.helper.CustomActivityTestRule
import com.github.vase4kin.teamcityapp.helper.HiltApiTestRule
import com.github.vase4kin.teamcityapp.helper.TestUtils
import com.github.vase4kin.teamcityapp.home.view.HomeActivity
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage
import com.google.gson.Gson
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.reactivex.Single
import io.reactivex.subjects.SingleSubject
import java.util.concurrent.atomic.AtomicInteger
import org.hamcrest.core.AllOf.allOf
import org.junit.*
import org.junit.runner.RunWith
import org.mockito.Mockito.mockingDetails
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Spy
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import teamcityapp.features.filter_bottom_sheet.impl.R as QuickR
import teamcityapp.features.running_builds.impl.R as FeatureR
import teamcityapp.libraries.list_ui.R as ListR

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class RunningBuildsFragmentTest {
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
    val activityRule = CustomActivityTestRule(HomeActivity::class.java)

    @JvmField
    @Rule(order = 4)
    val compose = createEmptyComposeRule()

    @Spy private val teamCityService: TeamCityService = FakeTeamCityServiceImpl()
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val app get() = context.applicationContext as TeamCityApplicationBase
    private val storage get() = app.appInjector.sharedUserStorage()

    companion object {
        private const val DETAILS_COMPONENT = "com.github.vase4kin.teamcityapp.build_details.view.BuildDetailsActivity"
        private const val HISTORY_COMPONENT = teamcityapp.features.build_history.api.BuildHistoryNavigation.LEGACY_ACTIVITY

        @JvmStatic @BeforeClass
        fun disableOnboarding() = TestUtils.disableOnboarding()
    }

    @Before fun setUp() {
        androidx.test.espresso.intent.Intents.init()
        storage.clearAll()
        storage.saveGuestUserAccountAndSetItAsActive(Mocks.URL, false)
        app.appInjector.cacheManager().evictAllCache()
        app.getSharedPreferences("rateTheAppPref", android.content.Context.MODE_PRIVATE).edit().putBoolean("rated", true).commit()
        // Isolate Home's live count requests from the pending row/detail subjects in these tests.
        doReturn(Single.just(Builds(0, emptyList()))).whenever(teamCityService).listRunningBuilds(any(), eq("count"))
    }

    @org.junit.After fun releaseIntents() {
        androidx.test.espresso.intent.Intents.release()
    }

    @Test fun toolbarAndEmptyFavoritesRenderThroughCompose() {
        openTab()
        assertTextVisible(text(FeatureR.string.running_builds_title))
        assertTextVisible(text(FeatureR.string.running_builds_empty_favorites))
        Assert.assertTrue(storage.favoriteBuildTypeIds.isEmpty())
        assertBadgeCount(0)
    }

    @Test fun toolbarOpensTheDrawerFromTheHomeHostedFragment() {
        openTab()
        assertTextVisible(text(FeatureR.string.running_builds_empty_favorites))
        compose.onNodeWithTag("running_builds:drawer").performClick()
        compose.waitUntil(10_000) { compose.onAllNodesWithTag("drawer:list").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithTag("drawer:settings").assertIsDisplayed()
    }

    @Test fun filterFabSwitchesEmptyFavoritesToAllAndKeepsTheEmptyCount() {
        whenever(teamCityService.listRunningBuilds("running:true,branch:default:any,personal:false,pinned:false", null)).thenReturn(Single.just(Builds(0, emptyList())))
        openTab()
        assertTextVisible(text(FeatureR.string.running_builds_empty_favorites))
        showAllFromFilterFab()
        assertTextVisible(text(FeatureR.string.running_builds_empty_all))
        onView(withText(R.string.text_filters_applied)).check(matches(isDisplayed()))
        assertBadgeCount(0)
        verify(teamCityService).listRunningBuilds("running:true,branch:default:any,personal:false,pinned:false", null)
    }

    @Test fun favoritesAndAllPreserveGroupedRowsAndHomeLiveBadgeCounts() {
        val first = build("1", "A", "Alpha", "Project A", "First build")
        val second = build("2", "A", "Alpha", "Project A", "Second build")
        val third = build("3", "B", "Beta", "Project B", "Third build")
        storage.addBuildTypeToFavorites("A")
        storage.addBuildTypeToFavorites("B")
        stubFavorite("A", listOf(first), 1)
        stubFavorite("B", listOf(third), 1)
        whenever(teamCityService.listRunningBuilds("running:true,branch:default:any,personal:false,pinned:false", null)).thenReturn(Single.just(Builds(3, listOf(first, second, third))))
        whenever(teamCityService.listRunningBuilds("running:true,branch:default:any,personal:false,pinned:false", "count")).thenReturn(Single.just(Builds(3, emptyList())))
        stubDetails(first, second, third)
        openTab()
        awaitRow("3")
        row("1").assertTextContains("#1").assertTextContains("main")
        compose.onNodeWithTag("running_builds:configuration:0").assertTextEquals("Project A - Alpha")
        compose.onNodeWithTag("running_builds:configuration:1").assertTextEquals("Project B - Beta")
        row("2").assertDoesNotExist()
        assertBadgeCount(2)
        showAllFromFilterFab()
        awaitRow("2")
        row("1").assertIsDisplayed()
        row("3").assertIsDisplayed()
        compose.onNodeWithTag("running_builds:configuration:2").assertTextEquals("Project B - Beta")
        assertBadgeCount(3)
        verify(teamCityService, times(1)).listRunningBuilds(favoriteLocator("A"), null)
        verify(teamCityService, times(1)).listRunningBuilds(favoriteLocator("B"), null)
        verify(teamCityService, times(1)).listRunningBuilds("running:true,branch:default:any,personal:false,pinned:false", null)
    }

    @Test fun rowNavigationPreservesTheCompleteBuildPayloadAndOriginalHomeFlags() {
        val build = build("1", "top-level", "Configuration", "Project", "Complete payload", nestedId = "nested")
        storage.addBuildTypeToFavorites("top-level")
        stubFavorite("top-level", listOf(build))
        stubDetails(build)
        Intents.intending(hasComponent(DETAILS_COMPONENT)).respondWith(android.app.Instrumentation.ActivityResult(Activity.RESULT_OK, null))
        openTab()
        awaitRow("1")
        row("1").performClick()
        val intent = Intents.getIntents().last { it.component?.className == DETAILS_COMPONENT }
        Assert.assertTrue(intent.flags and Intent.FLAG_ACTIVITY_NEW_TASK != 0)
        Assert.assertTrue(intent.flags and Intent.FLAG_ACTIVITY_SINGLE_TOP != 0)
        Assert.assertNull(intent.getStringExtra(BundleExtractorValues.NAME))
        @Suppress("DEPRECATION")
        val actual = intent.getSerializableExtra(BundleExtractorValues.BUILD) as Build
        Assert.assertEquals(build.id, actual.id)
        Assert.assertEquals(build.href, actual.href)
        Assert.assertEquals("top-level", actual.buildTypeId)
        Assert.assertEquals("nested", actual.buildType!!.id)
        Assert.assertNotNull(actual.changes)
        Assert.assertNotNull(actual.artifacts)
        Assert.assertNotNull(actual.testOccurrences)
        Assert.assertNotNull(actual.snapshotBuilds)
        Assert.assertEquals(2, actual.testOccurrences!!.count)
        Assert.assertEquals("properties", actual.properties!!.id)
        Assert.assertTrue(actual.properties!!.properties.single().isOwn)
        Assert.assertTrue(actual.isPersonal)
        Assert.assertTrue(actual.isPinned)
        Assert.assertTrue(actual.isCleanSources)
        Assert.assertTrue(actual.isQueueAtTop)
    }

    @Test fun headerNavigationKeepsTopLevelConfigurationIdAndName() {
        val build = build("1", "top-level", "Configuration title", "Project", "Ready", nestedId = "nested")
        storage.addBuildTypeToFavorites("top-level")
        stubFavorite("top-level", listOf(build))
        stubDetails(build)
        Intents.intending(hasComponent(HISTORY_COMPONENT)).respondWith(android.app.Instrumentation.ActivityResult(Activity.RESULT_OK, null))
        openTab()
        awaitRow("1")
        compose.onNodeWithTag("running_builds:configuration:0").performClick()
        val intent = Intents.getIntents().last { it.component?.className == HISTORY_COMPONENT }
        Assert.assertEquals("top-level", intent.getStringExtra(BundleExtractorValues.ID))
        Assert.assertEquals("Configuration title", intent.getStringExtra(BundleExtractorValues.NAME))
        Assert.assertNull(intent.extras!!.get(BundleExtractorValues.BUILD_LIST_FILTER))
    }

    @Test fun initialErrorCanRetryWithoutRemovingFavorites() {
        storage.addBuildTypeToFavorites("A")
        whenever(teamCityService.listRunningBuilds(favoriteLocator("A"), null)).thenReturn(Single.error(RuntimeException("offline")))
        openTab()
        assertTextVisible(text(teamcityapp.libraries.theme.R.string.error_load_message))
        assertBadgeCount(0)
        val recovered = build("1", "A", "Alpha", "Project A", "Recovered")
        stubFavorite("A", listOf(recovered))
        stubDetails(recovered)
        compose.onNodeWithText(text(teamcityapp.libraries.theme.R.string.action_retry)).performClick()
        awaitRow("1")
        row("1").assertTextContains("Recovered")
        Assert.assertEquals(listOf("A"), storage.favoriteBuildTypeIds)
    }

    @Test fun failedRefreshRetainsRowsAndRetriesTheList() {
        val build = build("1", "A", "Alpha", "Project A", "Retained row")
        storage.addBuildTypeToFavorites("A")
        stubFavorite("A", listOf(build))
        stubDetails(build)
        openTab()
        awaitRow("1")
        whenever(teamCityService.listRunningBuilds(favoriteLocator("A"), null)).thenReturn(Single.error(RuntimeException("offline refresh")))
        compose.onNodeWithTag("running_builds:list").performTouchInput { swipeDown() }
        assertTextVisible(text(ListR.string.list_refresh_failed))
        row("1").assertIsDisplayed()
        stubFavorite("A", listOf(build))
        compose.onNodeWithText(text(ListR.string.list_action_retry)).performClick()
        compose.waitUntil(10_000) { compose.onAllNodesWithText(text(ListR.string.list_refresh_failed)).fetchSemanticsNodes().isEmpty() }
        row("1").assertIsDisplayed()
        Assert.assertEquals(listOf("A"), storage.favoriteBuildTypeIds)
    }

    @Test fun actualHomeHideShowCancelsPendingDetailsAndReloadsOnReturn() {
        val build = build("1", "A", "Alpha", "Project A", "Returned row")
        storage.addBuildTypeToFavorites("A")
        stubFavorite("A", listOf(build))
        val first = SingleSubject.create<Build>()
        val returned = SingleSubject.create<Build>()
        val subscriptions = AtomicInteger()
        doReturn(Single.defer { if (subscriptions.incrementAndGet() == 1) first else returned }).whenever(teamCityService).build(serviceUrl(build))
        openTab()
        compose.waitUntil(10_000) { first.hasObservers() }
        onView(withId(R.id.projects)).perform(click())
        awaitProjects()
        compose.waitUntil(10_000) { !first.hasObservers() }
        clickTab()
        compose.waitUntil(10_000) { returned.hasObservers() }
        first.onSuccess(this.build("old", "A", "Alpha", "Project A", "Late hidden result"))
        returned.onSuccess(build)
        awaitRow("1")
        row("1").assertTextContains("Returned row")
        row("old").assertDoesNotExist()
        Assert.assertEquals(2, subscriptions.get())
    }

    @Test fun completedRowsKeepScrollPositionAcrossActualHomeHideShow() {
        val rows = (1..12).map { build("$it", "A", "Alpha", "Project A", "Build $it") }
        storage.addBuildTypeToFavorites("A")
        stubFavorite("A", rows)
        stubDetails(*rows.toTypedArray())
        openTab()
        awaitRow("1")
        compose.onNodeWithTag("running_builds:list").performScrollToNode(hasTestTag("running_builds:build:12"))
        row("12").assertIsDisplayed()
        onView(withId(R.id.projects)).perform(click())
        awaitProjects()
        clickTab()
        compose.waitUntil(10_000) { listCalls(favoriteLocator("A")) == 2 }
        row("12").assertIsDisplayed()
    }

    @Test fun visibleHomeRecreationRetainsRowsWithoutDuplicateListLoading() {
        val build = build("1", "A", "Alpha", "Project A", "Retained row")
        storage.addBuildTypeToFavorites("A")
        stubFavorite("A", listOf(build))
        stubDetails(build)
        ActivityScenario.launch<HomeActivity>(Intent(context, HomeActivity::class.java)).use { scenario ->
            awaitProjects()
            clickTab()
            awaitRow("1")
            scenario.recreate()
            awaitRow("1")
            assertTextVisible(text(FeatureR.string.running_builds_title))
            verify(teamCityService, times(1)).listRunningBuilds(favoriteLocator("A"), null)
        }
    }

    @Test fun hiddenHomeRecreationStillReloadsOnRealTabReturn() {
        val build = build("1", "A", "Alpha", "Project A", "Retained row")
        storage.addBuildTypeToFavorites("A")
        stubFavorite("A", listOf(build))
        stubDetails(build)
        ActivityScenario.launch<HomeActivity>(Intent(context, HomeActivity::class.java)).use { scenario ->
            awaitProjects()
            clickTab()
            awaitRow("1")
            onView(withId(R.id.projects)).perform(click())
            awaitProjects()
            scenario.recreate()
            awaitProjects()
            clickTab()
            awaitRow("1")
            compose.waitUntil(10_000) { listCalls(favoriteLocator("A")) == 2 }
            verify(teamCityService, times(2)).listRunningBuilds(favoriteLocator("A"), null)
        }
    }

    @Test fun sameServerDifferentUserCancelsOldPendingListAndUsesOnlyNewFavorites() {
        storage.clearAll()
        saveUser("alice")
        storage.addBuildTypeToFavorites("alice-build")
        saveUser("bob")
        storage.addBuildTypeToFavorites("bob-build")
        storage.setUserActive(Mocks.URL, "alice")
        val alice = SingleSubject.create<Builds>()
        val bob = SingleSubject.create<Builds>()
        whenever(teamCityService.listRunningBuilds(favoriteLocator("alice-build"), null)).thenReturn(alice)
        whenever(teamCityService.listRunningBuilds(favoriteLocator("bob-build"), null)).thenReturn(bob)
        val aliceBuild = build("alice", "alice-build", "Alice private configuration", "Alice", "Alice private row")
        val bobBuild = build("bob", "bob-build", "Bob configuration", "Bob", "Bob row")
        stubDetails(aliceBuild, bobBuild)
        ActivityScenario.launch<HomeActivity>(Intent(context, HomeActivity::class.java)).use { scenario ->
            awaitProjects()
            clickTab()
            compose.waitUntil(10_000) { alice.hasObservers() }
            scenario.onActivity { activity ->
                storage.setUserActive(Mocks.URL, "bob")
                activity.startActivity(
                    Intent(activity, HomeActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        .putExtra(BundleExtractorValues.IS_REQUIRED_TO_RELOAD, true)
                )
            }
            awaitProjects()
            compose.waitUntil(10_000) { !alice.hasObservers() }
            clickTab()
            compose.waitUntil(10_000) { bob.hasObservers() }
            alice.onSuccess(Builds(1, listOf(aliceBuild)))
            bob.onSuccess(Builds(1, listOf(bobBuild)))
            awaitRow("bob")
            row("bob").assertTextContains("Bob row")
            row("alice").assertDoesNotExist()
            Assert.assertEquals("bob", storage.activeUser.userName)
            Assert.assertEquals(listOf("bob-build"), storage.favoriteBuildTypeIds)
            Assert.assertEquals(listOf("alice-build"), storage.userAccounts.single { it.userName == "alice" }.buildTypeIds)
            verify(teamCityService, times(1)).listRunningBuilds(favoriteLocator("alice-build"), null)
            verify(teamCityService, times(1)).listRunningBuilds(favoriteLocator("bob-build"), null)
        }
    }

    private fun openTab() {
        activityRule.launchActivity(null)
        awaitProjects()
        clickTab()
    }
    private fun clickTab() {
        onView(withId(R.id.running_builds)).perform(click())
    }
    private fun awaitProjects() {
        compose.waitUntil(10_000) { compose.onAllNodesWithTag("navigation:row:project:id").fetchSemanticsNodes().isNotEmpty() }
    }
    private fun row(id: String) = compose.onNodeWithTag("running_builds:build:$id")
    private fun awaitRow(id: String) {
        compose.waitUntil(10_000) { compose.onAllNodesWithTag("running_builds:build:$id").fetchSemanticsNodes().isNotEmpty() }
    }
    private fun assertTextVisible(value: String) {
        compose.waitUntil(10_000) { compose.onAllNodesWithText(value, useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText(value, useUnmergedTree = true).assertIsDisplayed()
    }
    private fun text(id: Int) = context.getString(id)
    private fun showAllFromFilterFab() {
        onView(allOf(withId(R.id.home_floating_action_button), isDisplayed())).perform(click())
        compose.onNodeWithText(text(QuickR.string.text_show_running)).performClick()
    }
    private fun assertBadgeCount(expected: Int) {
        compose.waitUntil(10_000) {
            var actual: Int? = null
            InstrumentationRegistry.getInstrumentation().runOnMainSync {
                val navigation = activityRule.activity.findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.navigation)
                actual = navigation.getBadge(R.id.running_builds)?.number
            }
            actual == expected
        }
    }
    private fun favoriteLocator(id: String): String = "running:true,branch:default:any,personal:false,pinned:false,buildType:$id"
    private fun stubFavorite(id: String, rows: List<Build>, count: Int = rows.size) {
        whenever(teamCityService.listRunningBuilds(favoriteLocator(id), null)).thenReturn(Single.just(Builds(rows.size, rows)))
        whenever(teamCityService.listRunningBuilds(favoriteLocator(id), "count")).thenReturn(Single.just(Builds(count, emptyList())))
    }
    private fun stubDetails(vararg rows: Build) {
        rows.forEach { whenever(teamCityService.build(serviceUrl(it))).thenReturn(Single.just(it)) }
    }
    private fun serviceUrl(build: Build) = build.href
    private fun listCalls(locator: String?): Int = mockingDetails(teamCityService).invocations.count { it.method.name == "listRunningBuilds" && it.arguments.contentEquals(arrayOf(locator, null)) }
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
    private fun build(id: String, configId: String, configName: String, projectName: String, message: String, nestedId: String = configId): Build {
        val json = com.google.gson.JsonObject().apply {
            addProperty("id", id)
            addProperty("href", "/app/rest/builds/id:$id")
            addProperty("webUrl", "https://ci.example/build/$id")
            addProperty("number", id)
            addProperty("state", "running")
            addProperty("status", "SUCCESS")
            addProperty("statusText", message)
            addProperty("waitReason", message)
            addProperty("branchName", "main")
            addProperty("buildTypeId", configId)
            addProperty("queuedDate", "20201010T010000+0700")
            addProperty("startDate", "20201010T010100+0700")
            add(
                "buildType",
                com.google.gson.JsonObject().apply {
                    addProperty("id", nestedId)
                    addProperty("name", configName)
                    addProperty("projectName", projectName)
                }
            )
            add(
                "changes",
                com.google.gson.JsonObject().apply {
                    addProperty("href", "/changes")
                    addProperty("count", 1)
                }
            )
            add("artifacts", com.google.gson.JsonObject().apply { addProperty("href", "/artifacts") })
            add(
                "testOccurrences",
                com.google.gson.JsonObject().apply {
                    addProperty("href", "/tests")
                    addProperty("count", 2)
                    addProperty("failed", 2)
                }
            )
            add(
                "snapshot-dependencies",
                com.google.gson.JsonObject().apply {
                    addProperty("href", "/snapshots")
                    addProperty("count", 1)
                }
            )
            add(
                "properties",
                com.google.gson.JsonObject().apply {
                    addProperty("id", "properties")
                    add(
                        "property",
                        com.google.gson.JsonArray().apply {
                            add(
                                com.google.gson.JsonObject().apply {
                                    addProperty("name", "environment")
                                    addProperty("value", "test")
                                    addProperty("own", true)
                                }
                            )
                        }
                    )
                }
            )
            addProperty("personal", true)
            addProperty("pinned", true)
            addProperty("cleanSources", true)
            addProperty("queueAtTop", true)
        }
        return Gson().fromJson(json, Build::class.java)
    }
}
