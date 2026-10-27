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

package com.github.vase4kin.teamcityapp.queue.view

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
import com.github.vase4kin.teamcityapp.di.modules.FakeTeamCityServiceImpl
import com.github.vase4kin.teamcityapp.di.modules.Mocks
import com.github.vase4kin.teamcityapp.di.modules.PendingFixture
import com.github.vase4kin.teamcityapp.helper.CustomActivityTestRule
import com.github.vase4kin.teamcityapp.helper.HiltApiTestRule
import com.github.vase4kin.teamcityapp.helper.TestUtils
import com.github.vase4kin.teamcityapp.helper.currentRootSnapshot
import com.github.vase4kin.teamcityapp.helper.hasDestination
import com.github.vase4kin.teamcityapp.navigation.IncomingDestinationResolver
import com.github.vase4kin.teamcityapp.navigation.MainActivity
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage
import com.google.gson.Gson
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
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
import org.mockito.kotlin.doSuspendableAnswer
import org.mockito.kotlin.eq
import org.mockito.kotlin.verifyBlocking
import org.mockito.kotlin.whenever
import teamcityapp.features.build_details.api.BuildDetailsDestination
import teamcityapp.features.build_history.api.navigation.BuildHistoryDestination
import teamcityapp.features.build_queue.impl.R as FeatureR
import teamcityapp.libraries.data.builds.BUILD_LAUNCH_FIELDS
import teamcityapp.libraries.data.builds.BuildLaunchMapper
import teamcityapp.libraries.list_ui.R as ListR

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class BuildQueueFragmentTest {
    @get:org.junit.Rule(order = 6)
    val phaseThreeEvidence = com.github.vase4kin.teamcityapp.helper.PhaseThreeEvidenceRule()

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
    val activityRule = CustomActivityTestRule(MainActivity::class.java, IncomingDestinationResolver.HOME_COMPONENT)

    @JvmField
    @Rule(order = 4)
    val compose = createEmptyComposeRule()

    @Spy private val teamCityService: TeamCityService = FakeTeamCityServiceImpl()
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val app get() = context.applicationContext as TeamCityApplicationBase
    private val storage get() = app.appInjector.sharedUserStorage()

    companion object {
        private const val DETAILS_COMPONENT = "com.github.vase4kin.teamcityapp.build_details.view.BuildDetailsActivity"

        @JvmStatic @BeforeClass
        fun disableOnboarding() = TestUtils.disableOnboarding()
    }

    @Before fun setUp() {
        androidx.test.espresso.intent.Intents.init()
        storage.clearAll()
        storage.saveGuestUserAccountAndSetItAsActive(Mocks.URL, false)
        app.getSharedPreferences("rateTheAppPref", android.content.Context.MODE_PRIVATE).edit().putBoolean("rated", true).commit()
        // Isolate Home's live count requests from the pending row/detail subjects in these tests.
        doReturn(Builds(0, emptyList())).whenever(teamCityService) { listQueueBuilds(anyOrNull(), eq("count")) }
    }

    @org.junit.After fun releaseIntents() {
        androidx.test.espresso.intent.Intents.release()
    }

    @Test fun toolbarAndEmptyFavoritesRenderThroughCompose() {
        openTab()
        assertTextVisible(text(teamcityapp.features.home.impl.R.string.home_activity))
        assertTextVisible(text(FeatureR.string.build_queue_empty_favorites))
        Assert.assertTrue(storage.favoriteBuildTypeIds.isEmpty())
        assertBadgeCount(0)
    }

    @Test fun toolbarOpensTheDrawerFromTheHomeHostedFragment() {
        openTab()
        assertTextVisible(text(FeatureR.string.build_queue_empty_favorites))
        compose.onNodeWithTag("home:account").performClick()
        compose.waitUntil(10_000) { compose.onAllNodesWithTag("drawer:list").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithTag("drawer:settings").assertIsDisplayed()
    }

    @Test fun filterFabSwitchesEmptyFavoritesToAllAndKeepsTheEmptyCount() {
        doReturn(Builds(0, emptyList())).whenever(teamCityService) { listQueueBuilds(null, BUILD_LAUNCH_FIELDS) }
        openTab()
        assertTextVisible(text(FeatureR.string.build_queue_empty_favorites))
        showAllFromFilterChip()
        assertTextVisible(text(FeatureR.string.build_queue_empty_all))
        compose.onNodeWithTag("home:filter:QueuedAll").assertIsSelected()
        compose.onNodeWithTag("home:snackbar").assertDoesNotExist()
        assertBadgeCount(0)
        verifyBlocking(teamCityService) { listQueueBuilds(null, BUILD_LAUNCH_FIELDS) }
    }

    @Test fun favoritesAndAllPreserveGroupedRowsAndHomeLiveBadgeCounts() {
        val first = build("1", "A", "Alpha", "Project A", "First build")
        val second = build("2", "A", "Alpha", "Project A", "Second build")
        val third = build("3", "B", "Beta", "Project B", "Third build")
        storage.addBuildTypeToFavorites("A")
        storage.addBuildTypeToFavorites("B")
        stubFavorite("A", listOf(first), 1)
        stubFavorite("B", listOf(third), 1)
        doReturn(Builds(3, listOf(first, second, third))).whenever(teamCityService) { listQueueBuilds(null, BUILD_LAUNCH_FIELDS) }
        doReturn(Builds(3, emptyList())).whenever(teamCityService) { listQueueBuilds(null, "count") }
        stubDetails(first, second, third)
        openTab()
        awaitRow("3")
        row("1").assertTextContains("#1").assertTextContains("main")
        compose.onNodeWithTag("build_queue:configuration:0").assertTextEquals("Project A - Alpha")
        compose.onNodeWithTag("build_queue:configuration:1").assertTextEquals("Project B - Beta")
        row("2").assertDoesNotExist()
        assertBadgeCount(2)
        showAllFromFilterChip()
        awaitRow("2")
        row("1").assertIsDisplayed()
        row("3").assertIsDisplayed()
        compose.onNodeWithTag("build_queue:configuration:2").assertTextEquals("Project B - Beta")
        assertBadgeCount(3)
        verifyBlocking(teamCityService, times(1)) { listQueueBuilds(favoriteLocator("A"), BUILD_LAUNCH_FIELDS) }
        verifyBlocking(teamCityService, times(1)) { listQueueBuilds(favoriteLocator("B"), BUILD_LAUNCH_FIELDS) }
        verifyBlocking(teamCityService, times(1)) { listQueueBuilds(null, BUILD_LAUNCH_FIELDS) }
    }

    @Test fun rowNavigationPreservesTheCompleteBuildPayloadInTheSameRoot() {
        val build = build("1", "top-level", "Configuration", "Project", "Complete payload", nestedId = "nested")
        storage.addBuildTypeToFavorites("top-level")
        stubFavorite("top-level", listOf(build))
        stubDetails(build)
        openTab()
        awaitRow("1")
        verifyBlocking(teamCityService, org.mockito.Mockito.never()) { build(any()) }
        row("1").performClick()
        Assert.assertEquals(BuildDetailsDestination(BuildLaunchMapper().toLaunchData(build)), currentRootSnapshot().entries.last().destination)
        Assert.assertTrue(currentRootSnapshot().entries.first().destination is teamcityapp.features.home.api.HomeDestination)
    }

    @Test fun headerNavigationKeepsTopLevelConfigurationIdAndName() {
        val build = build("1", "top-level", "Configuration title", "Project", "Ready", nestedId = "nested")
        storage.addBuildTypeToFavorites("top-level")
        stubFavorite("top-level", listOf(build))
        stubDetails(build)
        openTab()
        awaitRow("1")
        compose.onNodeWithTag("build_queue:configuration:0").performClick()
        Assert.assertEquals(BuildHistoryDestination("top-level", "Configuration title"), currentRootSnapshot().entries.last().destination)
    }

    @Test fun initialErrorCanRetryWithoutRemovingFavorites() {
        storage.addBuildTypeToFavorites("A")
        doSuspendableAnswer { throw RuntimeException("offline") }.whenever(teamCityService) { listQueueBuilds(favoriteLocator("A"), BUILD_LAUNCH_FIELDS) }
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
        doSuspendableAnswer { throw RuntimeException("offline refresh") }.whenever(teamCityService) { listQueueBuilds(favoriteLocator("A"), BUILD_LAUNCH_FIELDS) }
        compose.onNodeWithTag("build_queue:list").performTouchInput { swipeDown() }
        assertTextVisible(text(ListR.string.list_refresh_failed))
        row("1").assertIsDisplayed()
        stubFavorite("A", listOf(build))
        compose.onNodeWithText(text(ListR.string.list_action_retry)).performClick()
        compose.waitUntil(10_000) { compose.onAllNodesWithText(text(ListR.string.list_refresh_failed)).fetchSemanticsNodes().isEmpty() }
        row("1").assertIsDisplayed()
        Assert.assertEquals(listOf("A"), storage.favoriteBuildTypeIds)
    }

    @Test fun actualHomeHideShowCancelsPendingBulkLoadAndReloadsOnReturn() {
        val build = build("1", "A", "Alpha", "Project A", "Returned row")
        storage.addBuildTypeToFavorites("A")
        stubFavorite("A", listOf(build))
        val first = PendingFixture<Builds>()
        val returned = PendingFixture<Builds>()
        val subscriptions = AtomicInteger()
        doSuspendableAnswer { if (subscriptions.incrementAndGet() == 1) first.await() else returned.await() }.whenever(teamCityService) { listQueueBuilds(favoriteLocator("A"), BUILD_LAUNCH_FIELDS) }
        openTab()
        compose.waitUntil(10_000) { first.isAwaiting }
        compose.onNodeWithTag("home:tab:Projects").performClick()
        awaitProjects()
        compose.waitUntil(10_000) { !first.isAwaiting }
        clickTab()
        compose.waitUntil(10_000) { returned.isAwaiting }
        first.complete(Builds(1, listOf(this.build("old", "A", "Alpha", "Project A", "Late hidden result"))))
        returned.complete(Builds(1, listOf(build)))
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
        compose.onNodeWithTag("build_queue:list").performScrollToNode(hasTestTag("build_queue:build:12"))
        row("12").assertIsDisplayed()
        compose.onNodeWithTag("home:tab:Projects").performClick()
        awaitProjects()
        clickTab()
        compose.waitForIdle()
        org.junit.Assert.assertEquals(1, listCalls(favoriteLocator("A")))
        row("12").assertIsDisplayed()
    }

    @Test fun visibleHomeRecreationRetainsRowsWithoutDuplicateListLoading() {
        val build = build("1", "A", "Alpha", "Project A", "Retained row")
        storage.addBuildTypeToFavorites("A")
        stubFavorite("A", listOf(build))
        stubDetails(build)
        ActivityScenario.launch<MainActivity>(Intent(context, MainActivity::class.java).setClassName(context, IncomingDestinationResolver.BUILD_DETAILS_COMPONENT).setClassName(context, IncomingDestinationResolver.HOME_COMPONENT)).use { scenario ->
            awaitProjects()
            clickTab()
            awaitRow("1")
            scenario.recreate()
            awaitRow("1")
            assertTextVisible(text(teamcityapp.features.home.impl.R.string.home_activity))
            verifyBlocking(teamCityService, times(1)) { listQueueBuilds(favoriteLocator("A"), BUILD_LAUNCH_FIELDS) }
        }
    }

    @Test fun hiddenHomeRecreationRetainsContentOnRealTabReturn() {
        val build = build("1", "A", "Alpha", "Project A", "Retained row")
        storage.addBuildTypeToFavorites("A")
        stubFavorite("A", listOf(build))
        stubDetails(build)
        ActivityScenario.launch<MainActivity>(Intent(context, MainActivity::class.java).setClassName(context, IncomingDestinationResolver.BUILD_DETAILS_COMPONENT).setClassName(context, IncomingDestinationResolver.HOME_COMPONENT)).use { scenario ->
            awaitProjects()
            clickTab()
            awaitRow("1")
            compose.onNodeWithTag("home:tab:Projects").performClick()
            awaitProjects()
            scenario.recreate()
            awaitProjects()
            clickTab()
            awaitRow("1")
            compose.waitForIdle()
            verifyBlocking(teamCityService, times(1)) { listQueueBuilds(favoriteLocator("A"), BUILD_LAUNCH_FIELDS) }
        }
    }

    @Test fun sameServerDifferentUserCancelsOldPendingListAndUsesOnlyNewFavorites() {
        storage.clearAll()
        saveUser("alice")
        storage.addBuildTypeToFavorites("alice-build")
        saveUser("bob")
        storage.addBuildTypeToFavorites("bob-build")
        storage.setUserActive(Mocks.URL, "alice")
        val alice = PendingFixture<Builds>()
        val bob = PendingFixture<Builds>()
        doSuspendableAnswer { alice.await() }.whenever(teamCityService) { listQueueBuilds(favoriteLocator("alice-build"), BUILD_LAUNCH_FIELDS) }
        doSuspendableAnswer { bob.await() }.whenever(teamCityService) { listQueueBuilds(favoriteLocator("bob-build"), BUILD_LAUNCH_FIELDS) }
        val aliceBuild = build("alice", "alice-build", "Alice private configuration", "Alice", "Alice private row")
        val bobBuild = build("bob", "bob-build", "Bob configuration", "Bob", "Bob row")
        stubDetails(aliceBuild, bobBuild)
        ActivityScenario.launch<MainActivity>(Intent(context, MainActivity::class.java).setClassName(context, IncomingDestinationResolver.BUILD_DETAILS_COMPONENT).setClassName(context, IncomingDestinationResolver.HOME_COMPONENT)).use { scenario ->
            awaitProjects()
            clickTab()
            compose.waitUntil(10_000) { alice.isAwaiting }
            scenario.onActivity { activity ->
                storage.setUserActive(Mocks.URL, "bob")
                activity.startActivity(
                    Intent(activity, MainActivity::class.java).setClassName(activity, IncomingDestinationResolver.BUILD_DETAILS_COMPONENT).setClassName(activity, IncomingDestinationResolver.HOME_COMPONENT)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        .putExtra(BundleExtractorValues.IS_REQUIRED_TO_RELOAD, true)
                )
            }
            awaitProjects()
            compose.waitUntil(10_000) { !alice.isAwaiting }
            clickTab()
            compose.waitUntil(10_000) { bob.isAwaiting }
            alice.complete(Builds(1, listOf(aliceBuild)))
            bob.complete(Builds(1, listOf(bobBuild)))
            awaitRow("bob")
            row("bob").assertTextContains("Bob row")
            row("alice").assertDoesNotExist()
            Assert.assertEquals("bob", storage.activeUser.userName)
            Assert.assertEquals(listOf("bob-build"), storage.favoriteBuildTypeIds)
            Assert.assertEquals(listOf("alice-build"), storage.userAccounts.single { it.userName == "alice" }.buildTypeIds)
            verifyBlocking(teamCityService, times(1)) { listQueueBuilds(favoriteLocator("alice-build"), BUILD_LAUNCH_FIELDS) }
            verifyBlocking(teamCityService, times(1)) { listQueueBuilds(favoriteLocator("bob-build"), BUILD_LAUNCH_FIELDS) }
        }
    }

    private fun openTab() {
        activityRule.launchActivity(null)
        awaitProjects()
        clickTab()
    }
    private fun clickTab() {
        compose.onNodeWithTag("home:tab:Activity").performClick()
        compose.onNodeWithTag("home:tab:Queue").performClick()
    }
    private fun awaitProjects() {
        compose.waitUntil(10_000) { compose.onAllNodesWithTag("navigation:row:project:id").fetchSemanticsNodes().isNotEmpty() }
    }
    private fun row(id: String) = compose.onNodeWithTag("build_queue:build:$id")
    private fun awaitRow(id: String) {
        compose.waitUntil(10_000) { compose.onAllNodesWithTag("build_queue:build:$id").fetchSemanticsNodes().isNotEmpty() }
    }
    private fun assertTextVisible(value: String) {
        compose.waitUntil(10_000) { compose.onAllNodesWithText(value, useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty() }
        compose.onAllNodesWithText(value, useUnmergedTree = true).onFirst().assertIsDisplayed()
    }
    private fun text(id: Int) = context.getString(id)
    private fun showAllFromFilterChip() {
        compose.onNodeWithTag("home:filter:QueuedAll").performClick()
    }
    private fun assertBadgeCount(expected: Int) {
        compose.waitUntil(10_000) {
            if (expected == 0) {
                compose.onAllNodesWithTag("home:count:Queue", useUnmergedTree = true).fetchSemanticsNodes().isEmpty()
            } else {
                runCatching { compose.onNodeWithTag("home:count:Queue", useUnmergedTree = true).assertTextEquals(expected.toString()) }.isSuccess
            }
        }
    }
    private fun favoriteLocator(id: String): String = "buildType:$id"
    private fun stubFavorite(id: String, rows: List<Build>, count: Int = rows.size) {
        doReturn(Builds(rows.size, rows)).whenever(teamCityService) { listQueueBuilds(favoriteLocator(id), BUILD_LAUNCH_FIELDS) }
        doReturn(Builds(count, emptyList())).whenever(teamCityService) { listQueueBuilds(favoriteLocator(id), "count") }
    }
    private fun stubDetails(vararg rows: Build) {
        rows.forEach { doReturn(it).whenever(teamCityService) { build(serviceUrl(it)) } }
    }
    private fun serviceUrl(build: Build) = build.href
    private fun listCalls(locator: String?): Int = mockingDetails(teamCityService).invocations.count { it.method.name == "listQueueBuilds" && it.arguments.contentEquals(arrayOf(locator, BUILD_LAUNCH_FIELDS)) }
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
            addProperty("state", "queued")
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
