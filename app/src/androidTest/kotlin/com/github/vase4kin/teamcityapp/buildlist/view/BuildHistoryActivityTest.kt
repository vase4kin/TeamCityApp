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
import android.app.Instrumentation
import android.content.Intent
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.core.content.IntentCompat
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.vase4kin.teamcityapp.TeamCityApplicationBase
import com.github.vase4kin.teamcityapp.api.TeamCityService
import com.github.vase4kin.teamcityapp.app_navigation.AppNavigationItem
import com.github.vase4kin.teamcityapp.base.extractor.BundleExtractorValues
import com.github.vase4kin.teamcityapp.build_details.view.BuildDetailsActivity
import com.github.vase4kin.teamcityapp.buildlist.api.Build
import com.github.vase4kin.teamcityapp.buildlist.api.Builds
import com.github.vase4kin.teamcityapp.buildlist.filter.BuildListFilterImpl
import com.github.vase4kin.teamcityapp.dagger.modules.FakeTeamCityServiceImpl
import com.github.vase4kin.teamcityapp.dagger.modules.Mocks
import com.github.vase4kin.teamcityapp.helper.HiltApiTestRule
import com.github.vase4kin.teamcityapp.helper.TestUtils
import com.github.vase4kin.teamcityapp.home.view.HomeActivity
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.reactivex.Single
import io.reactivex.subjects.SingleSubject
import java.util.concurrent.atomic.AtomicBoolean
import org.hamcrest.core.AllOf.allOf
import org.junit.*
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.*
import org.mockito.Spy
import org.mockito.kotlin.whenever
import teamcityapp.features.build_history.api.BuildHistoryNavigation
import teamcityapp.features.build_history.api.BuildHistoryQuery
import teamcityapp.features.build_history.impl.BuildHistoryActivity
import teamcityapp.features.build_history.impl.R as HistoryR
import teamcityapp.features.filter_builds.api.BuildStatusFilter
import teamcityapp.features.filter_builds.api.navigation.FilterBuildsNavigation
import teamcityapp.features.filter_builds.impl.FilterBuildsActivity
import teamcityapp.features.run_build.api.BUILD_TYPE_ID
import teamcityapp.features.run_build.api.navigation.RunBuildNavigation
import teamcityapp.features.run_build.impl.RunBuildActivity
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

    @Before fun setup() {
        TestUtils.disableOnboarding()
        app.appInjector.cacheManager().evictAllCache()
        storage.clearAll()
        storage.saveGuestUserAccountAndSetItAsActive(Mocks.URL, false)
        app.getSharedPreferences("rateTheAppPref", android.content.Context.MODE_PRIVATE).edit().putBoolean("rated", true).commit()
        val fallback = FakeTeamCityServiceImpl()
        doAnswer { invocation ->
            val href: String = invocation.getArgument(0)
            (details[href] ?: details["/$href"])?.let { Single.just(it) } ?: fallback.build(if (href.startsWith("/")) href else "/$href")
        }.whenever(teamCityService).build(anyString())
        Intents.init()
    }

    @After fun cleanup() {
        Intents.release()
        TestUtils.disableOnboarding()
    }

    @Test fun installedAliasAcceptsIdNameAndDefaultAnyLocator() {
        installPage(listOf(build("1")))
        launch().use { scenario ->
            awaitRow("1")
            compose.onNodeWithText("build type", useUnmergedTree = true).assertIsDisplayed()
            verify(teamCityService).listBuilds("build_type_id", BuildHistoryQuery.DEFAULT_LOCATOR)
            scenario.onActivity {
                Assert.assertEquals(BuildHistoryNavigation.LEGACY_ACTIVITY, it.intent.component?.className)
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
            verify(teamCityService).listBuilds("build_type_id", filter.toLocator())
            scenario.onActivity { Assert.assertFalse(it.intent.hasExtra("filter")) }
            scenario.recreate()
            awaitRow("1")
            verify(teamCityService, times(1)).listBuilds("build_type_id", filter.toLocator())
            verify(teamCityService, never()).listBuilds("build_type_id", BuildHistoryQuery.DEFAULT_LOCATOR)
        }
    }

    @Test fun legacyMockDuplicateIdsKeepNumbersStatusesBranchesAndDateSections() {
        launch().use {
            awaitText("Running tests")
            listOf("22 June", "21 June", "#2458", "#2459", "#2460", "Success", "Error with smth").forEach(::awaitText)
            compose.onAllNodesWithText("refs/heads/master").assertCountEquals(2)
            compose.onAllNodesWithTag("history:build:randomId").assertCountEquals(3)
            compose.onNodeWithTag("history:section:22 June").assertHasNoClickAction()
            compose.onNodeWithTag("history:section:21 June").assertHasNoClickAction()
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
            compose.onNodeWithTag("history:section:22 June").assertHasNoClickAction()
            Assert.assertTrue(Intents.getIntents().none { it.component?.className == BuildDetailsActivity::class.java.name })
        }
    }

    @Test fun initialFailureCanBeRetriedAndEmptySuccessStillExposesRun() {
        doReturn(Single.error<Builds>(IllegalStateException("offline"))).whenever(teamCityService).listBuilds(anyString(), anyString())
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
            doReturn(Single.error<Builds>(IllegalStateException("refresh offline"))).whenever(teamCityService).listBuilds(anyString(), anyString())
            pullRefresh()
            awaitText(text(teamcityapp.libraries.list_ui.R.string.list_refresh_failed))
            compose.onNodeWithTag("history:build:1").assertIsDisplayed()
            installPage(listOf(build("2")))
            compose.onNodeWithText(text(teamcityapp.libraries.list_ui.R.string.list_action_retry)).performClick()
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
            doReturn(Single.error<Builds>(IllegalStateException("offline"))).whenever(teamCityService).listBuilds(anyString(), anyString())
            compose.onRoot().performTouchInput { swipe(start = center.copy(y = height * .2f), end = center.copy(y = height * .85f), durationMillis = 600) }
            awaitText(text(teamcityapp.libraries.list_ui.R.string.list_refresh_failed))
            compose.onNodeWithText(text(HistoryR.string.history_empty)).assertIsDisplayed()
        }
    }

    @Test fun opaquePaginationAndRecreationKeepBothPagesWithoutDuplicateCalls() {
        val more = (13..20).map { build("$it") }
        val url = "/app/rest/builds?locator=opaque:12&fields=build(id,href)"
        installPage((1..12).map { build("$it") }, url)
        registerDetails(more)
        doReturn(Single.just(page(more))).whenever(teamCityService).listMoreBuilds(url)
        launch().use { scenario ->
            awaitRow("1")
            scrollTo("12")
            scrollTo("20")
            scenario.recreate()
            scrollTo("20")
            verify(teamCityService, times(1)).listBuilds("build_type_id", BuildHistoryQuery.DEFAULT_LOCATOR)
            verify(teamCityService, times(1)).listMoreBuilds(url)
        }
    }

    @Test fun appendFailureKeepsRowsAndRetriesSameOpaqueUrl() {
        installPage((1..12).map { build("$it") }, "/next/12")
        registerDetails(listOf(build("13")))
        val failed = AtomicBoolean(true)
        doAnswer { if (failed.get()) Single.error<Builds>(IllegalStateException("append offline")) else Single.just(page(listOf(build("13")))) }.whenever(teamCityService).listMoreBuilds("/next/12")
        launch().use {
            awaitRow("1")
            scrollTo("12")
            compose.waitUntil(10_000) { moreCalls("/next/12") == 1 }
            compose.onNodeWithTag("history:list").performScrollToNode(hasText(text(teamcityapp.libraries.list_ui.R.string.list_action_retry)))
            awaitText(text(teamcityapp.libraries.list_ui.R.string.list_action_retry))
            compose.onNodeWithTag("history:build:12").assertIsDisplayed()
            failed.set(false)
            compose.onNodeWithText(text(teamcityapp.libraries.list_ui.R.string.list_action_retry)).performClick()
            scrollTo("13")
            verify(teamCityService, times(2)).listMoreBuilds("/next/12")
            Assert.assertEquals(1, pageCalls(BuildHistoryQuery.DEFAULT_LOCATOR))
        }
    }

    @Test fun emptyContinuationEndsPagingWithoutStaleCursor() {
        installPage((1..12).map { build("$it") }, "/next/12")
        doReturn(Single.just(page(emptyList(), "/stale/next"))).whenever(teamCityService).listMoreBuilds("/next/12")
        launch().use {
            awaitRow("1")
            scrollTo("12")
            compose.waitUntil(10_000) { moreCalls("/next/12") == 1 }
            compose.waitForIdle()
            verify(teamCityService, never()).listMoreBuilds("/stale/next")
            compose.onNodeWithTag("history:build:12").assertIsDisplayed()
        }
    }

    @Test fun rowForwardsCompleteLegacyBuildToDetails() {
        val detail = fullBuild("1")
        installPage(listOf(detail))
        blockBuildDetails()
        launch().use {
            awaitRow("1")
            compose.onNodeWithTag("history:build:1").performClick()
            assertCompleteBuildIntent("1")
        }
    }

    @Test fun runResultUsesExistingContractAndRefreshesHistoryWithShowAction() {
        installPage(emptyList())
        stubRunResult(Activity.RESULT_OK)
        launch().use {
            awaitText(text(HistoryR.string.history_empty))
            installPage(listOf(build("after-run")))
            compose.onNodeWithTag("history:run").performClick()
            intended(allOf(hasComponent(RunBuildActivity::class.java.name), hasExtra(BUILD_TYPE_ID, "build_type_id")))
            awaitRow("after-run")
            awaitText(text(HistoryR.string.history_queued))
            compose.onNodeWithText(text(HistoryR.string.history_show)).assertIsDisplayed()
            Assert.assertEquals(489, RunBuildNavigation.REQUEST_CODE)
        }
    }

    @Test fun cancelledRunDoesNotRefreshOrOfferShow() {
        installPage(emptyList())
        stubRunResult(Activity.RESULT_CANCELED)
        launch().use {
            awaitText(text(HistoryR.string.history_empty))
            compose.onNodeWithTag("history:run").performClick()
            compose.waitForIdle()
            compose.onNodeWithText(text(HistoryR.string.history_queued)).assertDoesNotExist()
            Assert.assertEquals(1, pageCalls(BuildHistoryQuery.DEFAULT_LOCATOR))
        }
    }

    @Test fun queuedShowWaitsForFullResponseAndLaunchesFullLegacyPayload() {
        installPage(emptyList())
        stubRunResult(Activity.RESULT_OK)
        blockBuildDetails()
        val pending = SingleSubject.create<Build>()
        doReturn(pending).whenever(teamCityService).build("/queued/result")
        launch().use {
            awaitText(text(HistoryR.string.history_empty))
            compose.onNodeWithTag("history:run").performClick()
            awaitText(text(HistoryR.string.history_show))
            compose.onNodeWithText(text(HistoryR.string.history_show)).performClick()
            compose.waitUntil(10_000) { pending.hasObservers() }
            compose.onNodeWithTag("history:opening-progress").assertIsDisplayed()
            pending.onSuccess(fullBuild("queued"))
            assertCompleteBuildIntent("queued")
            verify(teamCityService, times(1)).build("/queued/result")
        }
    }

    @Test fun queuedShowFailureRetriesDirectServerRequest() {
        installPage(emptyList())
        stubRunResult(Activity.RESULT_OK)
        blockBuildDetails()
        doReturn(Single.error<Build>(IllegalStateException("offline"))).whenever(teamCityService).build("/queued/result")
        launch().use {
            awaitText(text(HistoryR.string.history_empty))
            compose.onNodeWithTag("history:run").performClick()
            awaitText(text(HistoryR.string.history_show))
            compose.onNodeWithText(text(HistoryR.string.history_show)).performClick()
            awaitText(text(HistoryR.string.history_open_failed))
            doReturn(Single.just(fullBuild("queued"))).whenever(teamCityService).build("/queued/result")
            compose.onNodeWithText(text(HistoryR.string.history_retry)).performClick()
            assertCompleteBuildIntent("queued")
            verify(teamCityService, times(2)).build("/queued/result")
        }
    }

    @Test fun selectedFilterSurvivesRefreshRecreationAndReset() {
        installPage(listOf(build("1")))
        val filter = selectedFilter()
        stubFilterResult(Activity.RESULT_OK, filter)
        launch().use { scenario ->
            awaitRow("1")
            compose.onNodeWithTag("history:filter").performClick()
            intended(allOf(hasComponent(FilterBuildsActivity::class.java.name), hasExtra(FilterBuildsNavigation.BUILD_TYPE_ID, "build_type_id")))
            awaitText(text(HistoryR.string.history_filters_applied))
            compose.waitUntil(10_000) { pageCalls(filter.toLocator()) == 1 }
            scenario.recreate()
            awaitRow("1")
            Assert.assertEquals(1, pageCalls(filter.toLocator()))
            pullRefresh()
            compose.waitUntil(10_000) { pageCalls(filter.toLocator()) == 2 }
            compose.onNodeWithTag("history:filter").performClick()
            awaitText(text(HistoryR.string.history_reset))
            compose.onNodeWithText(text(HistoryR.string.history_reset)).performClick()
            compose.waitUntil(10_000) { pageCalls(BuildHistoryQuery.DEFAULT_LOCATOR) == 2 }
            Assert.assertEquals(12921, FilterBuildsNavigation.REQUEST_CODE)
        }
    }

    @Test fun newFilterHidesOldRowsUntilNewPageCompletes() {
        installPage(listOf(build("1")))
        val filter = selectedFilter()
        val pending = SingleSubject.create<Builds>()
        doReturn(pending).whenever(teamCityService).listBuilds("build_type_id", filter.toLocator())
        stubFilterResult(Activity.RESULT_OK, filter)
        launch().use {
            awaitRow("1")
            compose.onNodeWithTag("history:filter").performClick()
            compose.waitUntil(10_000) { pending.hasObservers() }
            compose.onNodeWithTag("history:build:1").assertDoesNotExist()
            val rows = listOf(build("2"))
            registerDetails(rows)
            pending.onSuccess(page(rows))
            awaitRow("2")
        }
    }

    @Test fun cancelledFilterKeepsCurrentRowsAndLocator() {
        installPage(listOf(build("1")))
        stubFilterResult(Activity.RESULT_CANCELED, selectedFilter())
        launch().use {
            awaitRow("1")
            compose.onNodeWithTag("history:filter").performClick()
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

    @Test fun favoriteSnackbarOpensHomeFavoritesAndBackReturnsToRetainedHistory() {
        installPage(listOf(build("1")))
        launch().use {
            awaitRow("1")
            awaitFavoriteEnabled()
            compose.onNodeWithTag("history:favorite").performClick()
            awaitText(text(HistoryR.string.history_view))
            compose.onNodeWithText(text(HistoryR.string.history_view)).performClick()
            intended(allOf(hasComponent(HomeActivity::class.java.name), hasExtra(HomeActivity.ARG_TAB, AppNavigationItem.FAVORITES.ordinal)))
            compose.waitUntil(10_000) { compose.onAllNodesWithTag("favorites:configuration:build_type_id").fetchSemanticsNodes().isNotEmpty() }
            Assert.assertEquals(listOf("build_type_id"), storage.favoriteBuildTypeIds)
            pressBack()
            awaitRow("1")
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
            assertCoachmark("Filter", "history:filter", HistoryR.string.history_prompt_filter_description)
            Assert.assertTrue(prefs.getBoolean("RunBuild", false))
            compose.onNodeWithTag("history:prompt-dismiss").performClick()
            assertCoachmark("Favorite", "history:favorite", HistoryR.string.history_prompt_favorite_description)
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
            verify(teamCityService, times(1)).listBuilds("build_type_id", BuildHistoryQuery.DEFAULT_LOCATOR)
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
            verify(teamCityService).queueBuild(org.mockito.kotlin.any<Build>())
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
            verify(teamCityService).listBuilds("build_type_id", locator)
        }
    }

    @Test fun pendingQueuedShowSurvivesRecreationWithoutDuplicateRequestOrLaunch() {
        installPage(emptyList())
        stubRunResult(Activity.RESULT_OK)
        blockBuildDetails()
        val pending = SingleSubject.create<Build>()
        doReturn(pending).whenever(teamCityService).build("/queued/result")
        launch().use { scenario ->
            awaitText(text(HistoryR.string.history_empty))
            compose.onNodeWithTag("history:run").performClick()
            awaitText(text(HistoryR.string.history_show))
            compose.onNodeWithText(text(HistoryR.string.history_show)).performClick()
            compose.waitUntil(10_000) { pending.hasObservers() }
            scenario.recreate()
            compose.onNodeWithTag("history:opening-progress").assertIsDisplayed()
            pending.onSuccess(fullBuild("queued"))
            assertCompleteBuildIntent("queued")
            scenario.recreate()
            compose.waitForIdle()
            verify(teamCityService, times(1)).build("/queued/result")
            Assert.assertEquals(1, Intents.getIntents().count { it.component?.className == BuildDetailsActivity::class.java.name })
        }
    }

    @Test fun destroyingHistoryCancelsPendingQueuedShowAndIgnoresLateResponse() {
        installPage(emptyList())
        stubRunResult(Activity.RESULT_OK)
        blockBuildDetails()
        val pending = SingleSubject.create<Build>()
        doReturn(pending).whenever(teamCityService).build("/queued/result")
        val scenario = launch()
        try {
            awaitText(text(HistoryR.string.history_empty))
            compose.onNodeWithTag("history:run").performClick()
            awaitText(text(HistoryR.string.history_show))
            compose.onNodeWithText(text(HistoryR.string.history_show)).performClick()
            compose.waitUntil(10_000) { pending.hasObservers() }
        } finally {
            scenario.close()
        }
        compose.waitUntil(10_000) { !pending.hasObservers() }
        pending.onSuccess(fullBuild("late"))
        Assert.assertTrue(Intents.getIntents().none { it.component?.className == BuildDetailsActivity::class.java.name })
    }

    private fun launch(filter: BuildListFilterImpl? = null): ActivityScenario<BuildHistoryActivity> = ActivityScenario.launch(
        Intent().setClassName(context.packageName, BuildHistoryNavigation.LEGACY_ACTIVITY)
            .putExtra("id", "build_type_id").putExtra("name", "build type").apply { filter?.let { putExtra("filter", it) } }
    )
    private fun installPage(rows: List<Build>, next: String? = null) {
        registerDetails(rows)
        doReturn(Single.just(page(rows, next))).whenever(teamCityService).listBuilds(anyString(), anyString())
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
    private fun stubRunResult(resultCode: Int) = intending(hasComponent(RunBuildActivity::class.java.name)).respondWith(
        Instrumentation.ActivityResult(resultCode, Intent().putExtra(RunBuildNavigation.EXTRA_HREF, "/queued/result"))
    )
    private fun stubFilterResult(resultCode: Int, filter: BuildListFilterImpl) = intending(hasComponent(FilterBuildsActivity::class.java.name)).respondWith(
        Instrumentation.ActivityResult(resultCode, Intent().putExtra(FilterBuildsNavigation.EXTRA_FILTER, filter))
    )
    private fun blockBuildDetails() = intending(hasComponent(BuildDetailsActivity::class.java.name)).respondWith(Instrumentation.ActivityResult(Activity.RESULT_CANCELED, null))
    private fun assertCompleteBuildIntent(id: String) {
        compose.waitUntil(10_000) { Intents.getIntents().any { it.component?.className == BuildDetailsActivity::class.java.name } }
        val intent = Intents.getIntents().last { it.component?.className == BuildDetailsActivity::class.java.name }
        val payload = IntentCompat.getSerializableExtra(intent, BundleExtractorValues.BUILD, Build::class.java)!!
        Assert.assertEquals(id, payload.id)
        Assert.assertEquals("098", payload.number)
        Assert.assertEquals("raw/branch", payload.branchName)
        Assert.assertEquals("Android", payload.buildType?.id)
        Assert.assertEquals(2, payload.testOccurrences?.failed)
        Assert.assertEquals("/snapshots", payload.snapshotBuilds?.href)
        Assert.assertEquals("env", payload.properties?.properties?.single()?.name)
        Assert.assertEquals("build type", intent.getStringExtra(BundleExtractorValues.NAME))
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
