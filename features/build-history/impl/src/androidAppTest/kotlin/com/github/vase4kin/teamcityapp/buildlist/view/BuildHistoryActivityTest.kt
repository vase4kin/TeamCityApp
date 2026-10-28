/*
 * Copyright 2019 Andrey Tolpeev
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

package com.github.vase4kin.teamcityapp.buildlist.view

import android.app.Activity
import android.content.Intent
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.pressBack
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.vase4kin.teamcityapp.TeamCityApplicationBase
import com.github.vase4kin.teamcityapp.api.TeamCityService
import com.github.vase4kin.teamcityapp.buildlist.api.Build
import com.github.vase4kin.teamcityapp.buildlist.api.Builds
import com.github.vase4kin.teamcityapp.buildlist.filter.BuildListFilterImpl
import com.github.vase4kin.teamcityapp.di.modules.FakeTeamCityServiceImpl
import com.github.vase4kin.teamcityapp.di.modules.Mocks
import com.github.vase4kin.teamcityapp.di.modules.PendingFixture
import com.github.vase4kin.teamcityapp.helper.HiltApiTestRule
import com.github.vase4kin.teamcityapp.helper.TestUtils
import com.github.vase4kin.teamcityapp.navigation.LegacyBuildHistoryEntry
import com.github.vase4kin.teamcityapp.navigation.MainActivity
import com.github.vase4kin.teamcityapp.runbuild.api.Branch
import com.github.vase4kin.teamcityapp.runbuild.api.Branches
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import java.util.concurrent.atomic.AtomicBoolean
import org.junit.*
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.*
import org.mockito.Spy
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doSuspendableAnswer
import org.mockito.kotlin.verifyBlocking
import org.mockito.kotlin.whenever
import teamcityapp.features.build_details.api.BuildDetailsDestination
import teamcityapp.features.build_history.api.BuildHistoryQuery
import teamcityapp.features.build_history.impl.R as HistoryR
import teamcityapp.features.filter_builds.api.BuildStatusFilter
import teamcityapp.libraries.navigation.NavigationSnapshot
import teamcityapp.libraries.onboarding.OnboardingManagerImpl

/** Installed host and app adapters are exercised through the existing account API graph. */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class BuildHistoryActivityTest {
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
    val compose = createEmptyComposeRule()

    @Spy private val teamCityService: TeamCityService = FakeTeamCityServiceImpl()
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val app get() = context.applicationContext as TeamCityApplicationBase
    private val storage get() = app.appInjector.sharedUserStorage()
    private val details = mutableMapOf<String, Build>()
    private var scenario: ActivityScenario<MainActivity>? = null
    private var runResult = Activity.RESULT_OK
    private var filterResult = Activity.RESULT_OK

    @Before fun setup() {
        TestUtils.disableOnboarding()
        storage.clearAll()
        storage.saveGuestUserAccountAndSetItAsActive(Mocks.URL, false)
        app.getSharedPreferences("rateTheAppPref", android.content.Context.MODE_PRIVATE).edit().putBoolean("rated", true).commit()
        val fallback = FakeTeamCityServiceImpl()
        doSuspendableAnswer { invocation ->
            val href: String = invocation.getArgument(0)
            (details[href] ?: details["/$href"])?.let { it } ?: fallback.build(if (href.startsWith("/")) href else "/$href")
        }.whenever(teamCityService) { build(anyString()) }
        doReturn(Branches(listOf(Branch("main"), Branch("branch/raw")))).whenever(teamCityService) { listBranches(anyString()) }
    }

    @After fun cleanup() {
        TestUtils.disableOnboarding()
    }

    @Test fun implementationComponentAliasPreservesLegacyFilterThroughRootRecreation() {
        installPage(listOf(build("1")))
        val filter = selectedFilter()
        launch(filter, "teamcityapp.features.build_history.impl.BuildHistoryActivity").use { scenario ->
            awaitRow("1")
            scenario.recreate()
            awaitRow("1")
            scenario.onActivity { Assert.assertEquals(MainActivity::class.java, it.javaClass) }
            verifyBlocking(teamCityService, times(1)) { listBuilds("build_type_id", filter.toLocator()) }
        }
    }

    @Test fun installedAliasAcceptsIdNameAndDefaultAnyLocator() {
        installPage(listOf(build("1")))
        launch().use { scenario ->
            awaitRow("1")
            compose.onNodeWithText("build type", useUnmergedTree = true).assertIsDisplayed()
            verifyBlocking(teamCityService) { listBuilds("build_type_id", BuildHistoryQuery.DEFAULT_LOCATOR) }
            scenario.onActivity {
                Assert.assertEquals(LegacyBuildHistoryEntry.LEGACY_ACTIVITY, it.intent.component?.className)
                Assert.assertEquals("build_type_id", it.intent.getStringExtra("id"))
                Assert.assertEquals("build type", it.intent.getStringExtra("name"))
            }
        }
    }

    @Test fun legacySerializableFilterUsesSelectedLocatorAndSurvivesRecreation() {
        val filter = selectedFilter()
        installPage(listOf(build("1")))
        launch(filter).use { scenario ->
            awaitRow("1")
            verifyBlocking(teamCityService) { listBuilds("build_type_id", filter.toLocator()) }
            scenario.onActivity { Assert.assertFalse(it.intent.hasExtra("filter")) }
            scenario.recreate()
            awaitRow("1")
            verifyBlocking(teamCityService, times(1)) { listBuilds("build_type_id", filter.toLocator()) }
            verifyBlocking(teamCityService, never()) { listBuilds("build_type_id", BuildHistoryQuery.DEFAULT_LOCATOR) }
        }
    }

    @Test fun legacyMockDuplicateIdsKeepNumbersStatusesBranchesAndDateSections() {
        launch().use {
            awaitText("Running tests")
            listOf("22 June 2016", "21 June 2016", "#2458", "#2459", "#2460", "Success", "Error with smth").forEach(::awaitText)
            compose.onAllNodesWithText("refs/heads/master").assertCountEquals(2)
            compose.onAllNodesWithTag("history:build:randomId").assertCountEquals(3)
            compose.onNodeWithTag("history:section:22 June 2016").assertHasNoClickAction()
            compose.onNodeWithTag("history:section:21 June 2016").assertHasNoClickAction()
        }
    }

    @Test fun queuedRowsAppearBeforeDatedRowsAndHeadersCannotNavigate() {
        installPage(listOf(build("finished"), build("queued", "queued")))
        launch().use {
            awaitRow("queued")
            val queued = compose.onNodeWithTag("history:build:queued").fetchSemanticsNode().boundsInRoot
            val finished = compose.onNodeWithTag("history:build:finished").fetchSemanticsNode().boundsInRoot
            Assert.assertTrue(queued.top < finished.top)
            compose.onNodeWithTag("history:section:queued").assertHasNoClickAction()
            compose.onNodeWithTag("history:section:22 June 2016").assertHasNoClickAction()
            Assert.assertTrue(snapshot().entries.none { it.destination is BuildDetailsDestination })
        }
    }

    @Test fun initialFailureCanBeRetriedAndEmptySuccessStillExposesRun() {
        doSuspendableAnswer { throw IllegalStateException("offline") }.whenever(teamCityService) { listBuilds(anyString(), anyString()) }
        launch().use {
            awaitText(text(teamcityapp.libraries.theme.R.string.action_retry))
            installPage(emptyList())
            compose.onNodeWithText(text(teamcityapp.libraries.theme.R.string.action_retry)).performClick()
            awaitText(text(HistoryR.string.history_empty))
            compose.onNodeWithTag("history:run").assertIsDisplayed()
            Assert.assertEquals(2, pageCalls(BuildHistoryQuery.DEFAULT_LOCATOR))
        }
    }

    @Test fun refreshFailureRetainsRowsAndRetryReplacesThem() {
        installPage(listOf(build("1")))
        launch().use {
            awaitRow("1")
            doSuspendableAnswer { throw IllegalStateException("refresh offline") }.whenever(teamCityService) { listBuilds(anyString(), anyString()) }
            pullRefresh()
            awaitText("Couldn’t refresh. Try again.")
            compose.onNodeWithTag("history:build:1").assertIsDisplayed()
            installPage(listOf(build("2")))
            compose.onNodeWithText("Retry").performClick()
            awaitRow("2")
            compose.onNodeWithTag("history:build:1").assertDoesNotExist()
        }
    }

    @Test fun completedEmptySurvivesRecreationAndFailedPullRefresh() {
        installPage(emptyList())
        launch().use { scenario ->
            awaitText(text(HistoryR.string.history_empty))
            scenario.recreate()
            awaitText(text(HistoryR.string.history_empty))
            Assert.assertEquals(1, pageCalls(BuildHistoryQuery.DEFAULT_LOCATOR))
            doSuspendableAnswer { throw IllegalStateException("offline") }.whenever(teamCityService) { listBuilds(anyString(), anyString()) }
            compose.onRoot().performTouchInput { swipe(start = center.copy(y = height * .2f), end = center.copy(y = height * .85f), durationMillis = 600) }
            awaitText("Couldn’t refresh. Try again.")
            compose.onNodeWithText(text(HistoryR.string.history_empty)).assertIsDisplayed()
        }
    }

    @Test fun opaquePaginationAndRecreationKeepBothPagesWithoutDuplicateCalls() {
        val more = (13..20).map { build("$it") }
        val url = "/app/rest/builds?locator=opaque:12&fields=build(id,href)"
        installPage((1..12).map { build("$it") }, url)
        registerDetails(more)
        doReturn(page(more)).whenever(teamCityService) { listMoreBuilds(url) }
        launch().use { scenario ->
            awaitRow("1")
            scrollTo("12")
            scrollTo("20")
            scenario.recreate()
            scrollTo("20")
            verifyBlocking(teamCityService, times(1)) { listBuilds("build_type_id", BuildHistoryQuery.DEFAULT_LOCATOR) }
            verifyBlocking(teamCityService, times(1)) { listMoreBuilds(url) }
        }
    }

    @Test fun appendFailureKeepsRowsAndRetriesSameOpaqueUrl() {
        installPage((1..12).map { build("$it") }, "/next/12")
        registerDetails(listOf(build("13")))
        val failed = AtomicBoolean(true)
        doSuspendableAnswer { if (failed.get()) throw IllegalStateException("append offline") else page(listOf(build("13"))) }.whenever(teamCityService) { listMoreBuilds("/next/12") }
        launch().use {
            awaitRow("1")
            scrollTo("12")
            compose.waitUntil(10_000) { moreCalls("/next/12") == 1 }
            compose.onNodeWithTag("history:list").performScrollToNode(hasText("Retry"))
            awaitText("Retry")
            compose.onNodeWithTag("history:build:12").assertIsDisplayed()
            failed.set(false)
            compose.onNodeWithText("Retry").performClick()
            scrollTo("13")
            verifyBlocking(teamCityService, times(2)) { listMoreBuilds("/next/12") }
            Assert.assertEquals(1, pageCalls(BuildHistoryQuery.DEFAULT_LOCATOR))
        }
    }

    @Test fun emptyContinuationEndsPagingWithoutStaleCursor() {
        installPage((1..12).map { build("$it") }, "/next/12")
        doReturn(page(emptyList(), "/stale/next")).whenever(teamCityService) { listMoreBuilds("/next/12") }
        launch().use {
            awaitRow("1")
            scrollTo("12")
            compose.waitUntil(10_000) { moreCalls("/next/12") == 1 }
            compose.waitForIdle()
            verifyBlocking(teamCityService, never()) { listMoreBuilds("/stale/next") }
            compose.onNodeWithTag("history:build:12").assertIsDisplayed()
        }
    }

    @Test fun rowPushesCompleteBuildSnapshotToDetails() {
        val detail = fullBuild("1")
        installPage(listOf(detail))
        launch().use {
            awaitRow("1")
            compose.onNodeWithTag("history:build:1").performClick()
            assertCompleteBuildDestination("1")
        }
    }

    @Test fun typedRunResultRefreshesHistoryWithShowAction() {
        installPage(emptyList())
        prepareRun(Activity.RESULT_OK)
        launch().use {
            awaitText(text(HistoryR.string.history_empty))
            installPage(listOf(build("after-run")))
            compose.onNodeWithTag("history:run").performClick()
            completeRun()
            awaitRow("after-run")
            awaitText(text(HistoryR.string.history_queued))
            compose.onNodeWithText(text(HistoryR.string.history_show)).assertIsDisplayed()
        }
    }

    @Test fun cancelledRunDoesNotRefreshOrOfferShow() {
        installPage(emptyList())
        prepareRun(Activity.RESULT_CANCELED)
        launch().use {
            awaitText(text(HistoryR.string.history_empty))
            compose.onNodeWithTag("history:run").performClick()
            completeRun()
            compose.waitForIdle()
            compose.onNodeWithText(text(HistoryR.string.history_queued)).assertDoesNotExist()
            Assert.assertEquals(1, pageCalls(BuildHistoryQuery.DEFAULT_LOCATOR))
        }
    }

    @Test fun queuedShowNavigatesImmediatelyAndResolvesCompleteTypedSnapshot() {
        installPage(emptyList())
        prepareRun(Activity.RESULT_OK)
        val pending = PendingFixture<Build>()
        doSuspendableAnswer { pending.await() }.whenever(teamCityService) { build("/queued/result") }
        launch().use {
            awaitText(text(HistoryR.string.history_empty))
            compose.onNodeWithTag("history:run").performClick()
            completeRun()
            awaitText(text(HistoryR.string.history_show))
            compose.onNodeWithText(text(HistoryR.string.history_show)).performClick()
            compose.waitUntil(10_000) { pending.isAwaiting }
            compose.onNodeWithTag("details:reference-loading").assertIsDisplayed()
            pending.complete(fullBuild("queued"))
            assertCompleteBuildDestination("queued")
            // Reference resolution and the newly visible Overview each own one request.
            verifyBlocking(teamCityService, times(2)) { build("/queued/result") }
        }
    }

    @Test fun queuedShowFailureRetriesDirectServerRequest() {
        installPage(emptyList())
        prepareRun(Activity.RESULT_OK)
        doSuspendableAnswer { throw IllegalStateException("offline") }.whenever(teamCityService) { build("/queued/result") }
        launch().use {
            awaitText(text(HistoryR.string.history_empty))
            compose.onNodeWithTag("history:run").performClick()
            completeRun()
            awaitText(text(HistoryR.string.history_show))
            compose.onNodeWithText(text(HistoryR.string.history_show)).performClick()
            awaitText(text(teamcityapp.features.build_details.impl.R.string.error_opening_build))
            doReturn(fullBuild("queued")).whenever(teamCityService) { build("/queued/result") }
            compose.onNodeWithText(text(HistoryR.string.history_retry)).performClick()
            assertCompleteBuildDestination("queued")
            // Failed reference loading, explicit retry, and the new Overview load.
            verifyBlocking(teamCityService, times(3)) { build("/queued/result") }
        }
    }

    @Test fun selectedFilterSurvivesRefreshRecreationAndReset() {
        installPage(listOf(build("1")))
        val filter = selectedFilter()
        filterResult = Activity.RESULT_OK
        launch().use { scenario ->
            awaitRow("1")
            compose.onNodeWithTag("history:filter").performClick()
            completeFilter()
            awaitText(text(HistoryR.string.history_filters_applied))
            compose.waitUntil(10_000) { pageCalls(filter.toLocator()) == 1 }
            scenario.recreate()
            awaitRow("1")
            Assert.assertEquals(1, pageCalls(filter.toLocator()))
            pullRefresh()
            compose.waitUntil(10_000) { pageCalls(filter.toLocator()) == 2 }
            compose.onNodeWithTag("history:filter").performClick()
            completeFilter()
            awaitText(text(HistoryR.string.history_reset))
            compose.onNodeWithText(text(HistoryR.string.history_reset)).performClick()
            compose.waitUntil(10_000) { pageCalls(BuildHistoryQuery.DEFAULT_LOCATOR) == 2 }
        }
    }

    @Test fun newFilterHidesOldRowsUntilNewPageCompletes() {
        installPage(listOf(build("1")))
        val filter = selectedFilter()
        val pending = PendingFixture<Builds>()
        doSuspendableAnswer { pending.await() }.whenever(teamCityService) { listBuilds("build_type_id", filter.toLocator()) }
        filterResult = Activity.RESULT_OK
        launch().use {
            awaitRow("1")
            compose.onNodeWithTag("history:filter").performClick()
            completeFilter()
            compose.waitUntil(10_000) { pending.isAwaiting }
            compose.onNodeWithTag("history:build:1").assertDoesNotExist()
            val rows = listOf(build("2"))
            registerDetails(rows)
            pending.complete(page(rows))
            awaitRow("2")
        }
    }

    @Test fun cancelledFilterKeepsCurrentRowsAndLocator() {
        installPage(listOf(build("1")))
        filterResult = Activity.RESULT_CANCELED
        launch().use {
            awaitRow("1")
            compose.onNodeWithTag("history:filter").performClick()
            completeFilter()
            compose.waitForIdle()
            compose.onNodeWithTag("history:build:1").assertIsDisplayed()
            Assert.assertEquals(1, pageCalls(BuildHistoryQuery.DEFAULT_LOCATOR))
        }
    }

    @Test fun favoriteTogglePersistsAndRecreationKeepsMembership() {
        installPage(listOf(build("1")))
        launch().use { scenario ->
            awaitRow("1")
            awaitFavoriteEnabled()
            compose.onNodeWithTag("history:favorite").performClick()
            awaitText(text(HistoryR.string.history_favorite_added))
            Assert.assertEquals(listOf("build_type_id"), storage.favoriteBuildTypeIds)
            scenario.recreate()
            awaitRow("1")
            compose.onNodeWithContentDescription(text(HistoryR.string.history_remove_favorite)).assertIsDisplayed()
            Assert.assertEquals(listOf("build_type_id"), storage.favoriteBuildTypeIds)
            awaitFavoriteEnabled()
            compose.onNodeWithTag("history:favorite").performClick()
            awaitText(text(HistoryR.string.history_favorite_removed))
            Assert.assertTrue(storage.favoriteBuildTypeIds.isEmpty())
        }
    }

    @Test fun favoriteSnackbarOpensHomeFavoritesAndReplacesStandaloneHistory() {
        installPage(listOf(build("1")))
        launch().use { scenario ->
            awaitRow("1")
            awaitFavoriteEnabled()
            compose.onNodeWithTag("history:favorite").performClick()
            awaitText(text(HistoryR.string.history_view))
            compose.onNodeWithText(text(HistoryR.string.history_view)).performClick()
            compose.waitUntil(10_000) { compose.onAllNodesWithTag("favorites:configuration:build_type_id").fetchSemanticsNodes().isNotEmpty() }
            Assert.assertEquals(listOf("build_type_id"), storage.favoriteBuildTypeIds)
            scenario.onActivity { Assert.assertEquals(1, it.navigationSnapshot!!.entries.size) }
            androidx.test.espresso.Espresso.pressBackUnconditionally()
            compose.waitUntil(10_000) { scenario.state == Lifecycle.State.DESTROYED }
            Assert.assertEquals(1, pageCalls(BuildHistoryQuery.DEFAULT_LOCATOR))
        }
    }

    @Test fun actualOnboardingAnchorsSequenceAndInstalledFlagsSurviveRecreation() {
        val prefs = app.getSharedPreferences(OnboardingManagerImpl.PREF_NAME, android.content.Context.MODE_PRIVATE)
        prefs.edit().putBoolean("RunBuild", false).putBoolean("FilterBuilds", false).putBoolean("AddToFavoritesFromBuildType", false).commit()
        installPage(listOf(build("1")))
        launch().use { scenario ->
            assertCoachmark("Run", "history:run", HistoryR.string.history_prompt_run_description, above = true)
            scenario.recreate()
            assertCoachmark("Run", "history:run", HistoryR.string.history_prompt_run_description, above = true)
            compose.onNodeWithTag("history:prompt-dismiss").performClick()
            assertCoachmark("Filter", "history:filter", HistoryR.string.history_prompt_filter_description, above = true)
            Assert.assertTrue(prefs.getBoolean("RunBuild", false))
            compose.onNodeWithTag("history:prompt-dismiss").performClick()
            assertCoachmark("Favorite", "history:favorite", HistoryR.string.history_prompt_favorite_description, above = true)
            Assert.assertTrue(prefs.getBoolean("FilterBuilds", false))
            compose.onNodeWithTag("history:prompt-dismiss").performClick()
            compose.waitUntil(10_000) { compose.onAllNodesWithTag("history:coachmark:Favorite").fetchSemanticsNodes().isEmpty() }
            Assert.assertTrue(prefs.getBoolean("AddToFavoritesFromBuildType", false))
            compose.onNodeWithTag("history:build:1").assertIsDisplayed()
        }
    }

    @Test fun completedRowsSurviveBackgroundReturnAndRecreationWithoutAnotherPageRequest() {
        installPage(listOf(build("1")))
        launch().use { scenario ->
            awaitRow("1")
            scenario.moveToState(Lifecycle.State.CREATED)
            scenario.moveToState(Lifecycle.State.RESUMED)
            awaitRow("1")
            scenario.recreate()
            awaitRow("1")
            verifyBlocking(teamCityService, times(1)) { listBuilds("build_type_id", BuildHistoryQuery.DEFAULT_LOCATOR) }
        }
    }

    @Test fun backFinishesTheInstalledAliasHost() {
        installPage(emptyList())
        launch().use { scenario ->
            awaitText(text(HistoryR.string.history_empty))
            compose.onNodeWithTag("history:back").performClick()
            compose.waitUntil(10_000) { scenario.state == Lifecycle.State.DESTROYED }
        }
    }

    @Test fun actualRunBuildScreenReturnsItsExistingResultToHistory() {
        installPage(emptyList())
        launch().use {
            awaitText(text(HistoryR.string.history_empty))
            compose.onNodeWithTag("history:run").performClick()
            compose.waitUntil(10_000) { compose.onAllNodes(hasTestTag("run-build:submit") and isEnabled()).fetchSemanticsNodes().isNotEmpty() }
            installPage(listOf(build("real-run")))
            compose.onNodeWithTag("run-build:submit").performClick()
            awaitRow("real-run")
            awaitText(text(HistoryR.string.history_queued))
            Assert.assertEquals(2, pageCalls(BuildHistoryQuery.DEFAULT_LOCATOR))
            verifyBlocking(teamCityService) { queueBuild(org.mockito.kotlin.any<Build>()) }
        }
    }

    @Test fun actualFilterBuildsScreenSerializesQueuedSelectionBackToHistory() {
        installPage(listOf(build("1")))
        launch().use {
            awaitRow("1")
            compose.onNodeWithTag("history:filter").performClick()
            compose.waitUntil(10_000) { compose.onAllNodesWithTag("filter-builds:status:Queued").fetchSemanticsNodes().isNotEmpty() }
            compose.onNodeWithTag("filter-builds:status:Queued").performClick()
            compose.onNodeWithTag("filter-builds:personal").performClick()
            compose.onNodeWithTag("filter-builds:apply").performClick()
            val locator = "state:queued,branch:default:any,personal:true,pinned:any"
            compose.waitUntil(10_000) { pageCalls(locator) == 1 }
            awaitText(text(HistoryR.string.history_filters_applied))
            verifyBlocking(teamCityService) { listBuilds("build_type_id", locator) }
        }
    }

    @Test fun pendingQueuedShowSurvivesRecreationWithoutDuplicateRequestOrLaunch() {
        installPage(emptyList())
        prepareRun(Activity.RESULT_OK)
        val pending = PendingFixture<Build>()
        doSuspendableAnswer { pending.await() }.whenever(teamCityService) { build("/queued/result") }
        launch().use { scenario ->
            awaitText(text(HistoryR.string.history_empty))
            compose.onNodeWithTag("history:run").performClick()
            completeRun()
            awaitText(text(HistoryR.string.history_show))
            compose.onNodeWithText(text(HistoryR.string.history_show)).performClick()
            compose.waitUntil(10_000) { pending.isAwaiting }
            scenario.recreate()
            compose.onNodeWithTag("details:reference-loading").assertIsDisplayed()
            verifyBlocking(teamCityService, times(1)) { build("/queued/result") }
            pending.complete(fullBuild("queued"))
            assertCompleteBuildDestination("queued")
            scenario.recreate()
            compose.waitForIdle()
            verifyBlocking(teamCityService, times(2)) { build("/queued/result") }
            Assert.assertEquals(1, snapshot().entries.count { it.destination is BuildDetailsDestination })
        }
    }

    @Test fun destroyingHistoryCancelsPendingQueuedShowAndIgnoresLateResponse() {
        installPage(emptyList())
        prepareRun(Activity.RESULT_OK)
        val pending = PendingFixture<Build>()
        doSuspendableAnswer { pending.await() }.whenever(teamCityService) { build("/queued/result") }
        val scenario = launch()
        try {
            awaitText(text(HistoryR.string.history_empty))
            compose.onNodeWithTag("history:run").performClick()
            completeRun()
            awaitText(text(HistoryR.string.history_show))
            compose.onNodeWithText(text(HistoryR.string.history_show)).performClick()
            compose.waitUntil(10_000) { pending.isAwaiting }
        } finally {
            scenario.close()
        }
        compose.waitUntil(10_000) { !pending.isAwaiting }
        pending.complete(fullBuild("late"))
        Assert.assertEquals(Lifecycle.State.DESTROYED, scenario.state)
    }

    private fun launch(filter: BuildListFilterImpl? = null, component: String = LegacyBuildHistoryEntry.LEGACY_ACTIVITY): ActivityScenario<MainActivity> = ActivityScenario.launch<MainActivity>(
        Intent().setClassName(context.packageName, component)
            .putExtra("id", "build_type_id").putExtra("name", "build type").apply { filter?.let { putExtra("filter", it) } }
    ).also { scenario = it }
    private fun installPage(rows: List<Build>, next: String? = null) {
        registerDetails(rows)
        doReturn(page(rows, next)).whenever(teamCityService) { listBuilds(anyString(), anyString()) }
    }
    private fun registerDetails(rows: List<Build>) {
        rows.forEach { details[it.href] = it }
    }
    private fun build(id: String, state: String = "finished"): Build = Gson().fromJson(
        """{"id":"$id","href":"/builds/$id","number":"$id","state":"$state","status":"SUCCESS","statusText":"Build $id passed","branchName":"main","buildTypeId":"build_type_id","startDate":"20160622T230008+0700","waitReason":"Waiting for agent"}""",
        Build::class.java
    )
    private fun fullBuild(id: String): Build = Gson().fromJson(
        """{"id":"$id","href":"/queued/result","number":"098","state":"queued","status":"SUCCESS","statusText":"Waiting","branchName":"raw/branch","buildTypeId":"build_type_id","buildType":{"id":"Android","name":"Android build"},"testOccurrences":{"href":"/tests","failed":2},"snapshot-dependencies":{"href":"/snapshots","count":3},"properties":{"property":[{"name":"env","value":"test","own":true}]}}""",
        Build::class.java
    )
    private fun page(rows: List<Build>, next: String? = null): Builds {
        val gson = Gson()
        return gson.fromJson(
            JsonObject().apply {
                addProperty("count", rows.size)
                add("build", JsonArray().apply { rows.forEach { add(gson.toJsonTree(it)) } })
                next?.let { addProperty("nextHref", it) }
            },
            Builds::class.java
        )
    }
    private fun selectedFilter() = BuildListFilterImpl().apply {
        setFilter(BuildStatusFilter.Cancelled.ordinal)
        setBranch("branch/raw")
        setPersonal(true)
        setPinned(true)
    }
    private fun prepareRun(resultCode: Int) {
        runResult = resultCode
        doReturn(fullBuild("queued")).whenever(teamCityService) { queueBuild(org.mockito.kotlin.any<Build>()) }
    }
    private fun completeRun() {
        compose.waitUntil(10_000) { compose.onAllNodes(hasTestTag("run-build:submit") and isEnabled()).fetchSemanticsNodes().isNotEmpty() }
        if (runResult == Activity.RESULT_OK) compose.onNodeWithTag("run-build:submit").performClick() else pressBack()
        compose.waitUntil(10_000) { compose.onAllNodesWithTag("run-build:submit").fetchSemanticsNodes().isEmpty() }
    }
    private fun completeFilter() {
        compose.waitUntil(10_000) { compose.onAllNodesWithTag("filter-builds:chooser").fetchSemanticsNodes().isNotEmpty() }
        if (filterResult == Activity.RESULT_CANCELED) {
            pressBack()
        } else {
            compose.onNodeWithTag("filter-builds:status:Cancelled").performClick()
            compose.onNodeWithTag("branches:input").performScrollTo().performTextClearance()
            compose.onNodeWithTag("branches:input").performTextInput("branch/raw")
            compose.onNodeWithTag("filter-builds:personal").performScrollTo().performClick()
            compose.onNodeWithTag("filter-builds:pinned").performScrollTo().performClick()
            compose.onNodeWithTag("filter-builds:apply").performClick()
        }
        compose.waitUntil(10_000) { compose.onAllNodesWithTag("filter-builds:chooser").fetchSemanticsNodes().isEmpty() }
    }
    private fun snapshot(): NavigationSnapshot {
        var result: NavigationSnapshot? = null
        scenario!!.onActivity { result = it.navigationSnapshot }
        return requireNotNull(result)
    }
    private fun assertCompleteBuildDestination(id: String) {
        compose.waitUntil(10_000) { snapshot().entries.last().destination is BuildDetailsDestination }
        compose.waitUntil(10_000) { compose.onAllNodesWithTag("overview:list").fetchSemanticsNodes().isNotEmpty() }
        val destination = snapshot().entries.last().destination as BuildDetailsDestination
        val payload = destination.build
        Assert.assertEquals(id, payload.id)
        Assert.assertEquals("098", payload.number)
        Assert.assertEquals("raw/branch", payload.branchName)
        Assert.assertEquals("Android", payload.configuration?.id)
        Assert.assertEquals(2, payload.tests?.failed)
        Assert.assertEquals("/snapshots", payload.snapshotDependencies?.href)
        Assert.assertEquals("env", payload.properties?.items?.single()?.name)
        Assert.assertEquals("build type", destination.configurationName)
    }
    private fun awaitFavoriteEnabled() = compose.waitUntil(10_000) {
        compose.onAllNodes(hasTestTag("history:favorite") and isEnabled()).fetchSemanticsNodes().isNotEmpty()
    }
    private fun awaitRow(id: String) = compose.waitUntil(10_000) { compose.onAllNodesWithTag("history:build:$id").fetchSemanticsNodes().isNotEmpty() }
    private fun awaitText(value: String) {
        compose.waitUntil(10_000) { compose.onAllNodesWithText(value, useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText(value, useUnmergedTree = true).assertIsDisplayed()
    }
    private fun pullRefresh() = compose.onNodeWithTag("history:list").performTouchInput { swipeDown() }
    private fun scrollTo(id: String) = compose.onNodeWithTag("history:list").performScrollToNode(hasTestTag("history:build:$id"))
    private fun pageCalls(locator: String) = mockingDetails(teamCityService).invocations.count { it.method.name == "listBuilds" && it.arguments.contentEquals(arrayOf("build_type_id", locator)) }
    private fun moreCalls(url: String) = mockingDetails(teamCityService).invocations.count { it.method.name == "listMoreBuilds" && it.arguments.contentEquals(arrayOf(url)) }
    private fun text(id: Int) = context.getString(id)
    private fun assertCoachmark(prompt: String, target: String, description: Int, above: Boolean = false) {
        awaitText(text(description))
        compose.onNodeWithTag("history:coachmark:$prompt").assertIsDisplayed()
        val targetBounds = compose.onNodeWithTag(target, useUnmergedTree = true).fetchSemanticsNode().boundsInRoot
        val explanationBounds = compose.onNodeWithText(text(description)).fetchSemanticsNode().boundsInRoot
        if (above) Assert.assertTrue(explanationBounds.bottom < targetBounds.top) else Assert.assertTrue(explanationBounds.top > targetBounds.bottom)
    }
}
