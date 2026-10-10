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

package com.github.vase4kin.teamcityapp.snapshot_dependencies.view

import android.app.Activity
import android.app.Instrumentation.ActivityResult
import android.content.Intent
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.TeamCityApplicationBase
import com.github.vase4kin.teamcityapp.api.TeamCityService
import com.github.vase4kin.teamcityapp.base.extractor.BundleExtractorValues
import com.github.vase4kin.teamcityapp.build_details.view.BuildDetailsActivity
import com.github.vase4kin.teamcityapp.buildlist.api.Build
import com.github.vase4kin.teamcityapp.buildlist.api.Builds
import com.github.vase4kin.teamcityapp.dagger.modules.FakeTeamCityServiceImpl
import com.github.vase4kin.teamcityapp.dagger.modules.Mocks
import com.github.vase4kin.teamcityapp.helper.BuildComposeFixtures
import com.github.vase4kin.teamcityapp.helper.CustomActivityTestRule
import com.github.vase4kin.teamcityapp.helper.HiltApiTestRule
import com.github.vase4kin.teamcityapp.helper.TestUtils
import com.github.vase4kin.teamcityapp.helper.tapNativePagerTab
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.reactivex.Single
import io.reactivex.subjects.SingleSubject
import java.util.concurrent.atomic.AtomicInteger
import org.hamcrest.core.AllOf.allOf
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.*
import org.mockito.Spy
import teamcityapp.features.build_history.api.BuildHistoryNavigation
import teamcityapp.features.snapshot_dependencies.impl.R as SnapshotR
import teamcityapp.libraries.builds.*

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class SnapshotDependenciesFragmentTest {
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
    val activityRule = CustomActivityTestRule(BuildDetailsActivity::class.java)

    @JvmField
    @Rule(order = 4)
    val compose = createEmptyComposeRule()

    @Spy private val teamCityService: TeamCityService = FakeTeamCityServiceImpl()
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Before fun setUp() {
        androidx.test.espresso.intent.Intents.init()
        TestUtils.disableOnboarding()
        val app = context.applicationContext as TeamCityApplicationBase
        app.appInjector.cacheManager().evictAllCache()
        app.appInjector.sharedUserStorage().clearAll()
        app.appInjector.sharedUserStorage().saveGuestUserAccountAndSetItAsActive(Mocks.URL, false)
    }
    private fun snapshotText(value: String) = compose.onAllNodes(hasText(value) and hasAnyAncestor(hasTestTag("snapshot:screen")))
    private fun text(id: Int) = context.getString(id)
    private fun awaitText(value: String) {
        compose.waitUntil(10_000) {
            compose.onAllNodesWithText(value).fetchSemanticsNodes(atLeastOneRootRequired = false).indices.any { index ->
                runCatching { compose.onAllNodesWithText(value)[index].assertIsDisplayed() }.isSuccess
            }
        }
    }
    private fun awaitTag(value: String) {
        compose.waitUntil(10_000) { compose.onAllNodesWithTag(value).fetchSemanticsNodes(atLeastOneRootRequired = false).isNotEmpty() }
    }
    private fun tab(title: String) = compose.tapNativePagerTab(title)
    private fun sheet(label: String) {
        awaitText(label)
        compose.onNodeWithText(label).performClick()
        compose.waitUntil(10_000) { compose.onAllNodesWithTag("sheet:content").fetchSemanticsNodes(atLeastOneRootRequired = false).isEmpty() }
    }

    @org.junit.After fun releaseIntents() {
        androidx.test.espresso.intent.Intents.release()
    }

    @Test fun absentSnapshotCollectionDoesNotCreateATab() {
        `when`(teamCityService.build(anyString())).thenReturn(Single.just(BuildComposeFixtures.legacy(BuildComposeFixtures.finished)))
        activityRule.launchActivity(BuildComposeFixtures.intent(BuildComposeFixtures.incoming.copy(snapshotDependencies = null)))
        onView(withText(R.string.tab_parameters)).perform(scrollTo())
        onView(withText(R.string.tab_snapshot_dependencies)).check(androidx.test.espresso.assertion.ViewAssertions.doesNotExist())
    }

    @Test fun presentEmptySnapshotCollectionStillCreatesTabAndEmptyState() {
        `when`(teamCityService.listBuilds(anyString())).thenReturn(Single.just(Builds(0, emptyList())))
        start()
        awaitText(text(SnapshotR.string.snapshot_dependencies_empty))
    }

    @Test fun hydratedQueuedRunningAndFinishedRowsPreserveServerOrderAndGrouping() {
        dependencies()
        start()
        awaitTag("snapshot:build:dependency-1")
        compose.onNodeWithTag("snapshot:configuration:0").assertTextContains("Current project - Current configuration")
        compose.onNodeWithTag("snapshot:build:dependency-1").assertTextContains("Waiting for dependency agent")
        compose.onNodeWithTag("snapshot:build:dependency-2").assertTextContains("Running dependency tests")
        compose.onNodeWithTag("snapshot:list").performScrollToNode(hasTestTag("snapshot:configuration:2"))
        compose.onNodeWithTag("snapshot:configuration:2").assertTextContains("Other project - Other configuration")
        compose.onNodeWithTag("snapshot:build:dependency-3").assertTextContains("Finished dependency")
        verify(teamCityService).listBuilds("42")
    }

    @Test fun clickingBuildPassesEveryFieldToBuildDetailsBeforeItsTabsAreCreated() {
        dependencies()
        start()
        awaitTag("snapshot:build:dependency-2")
        compose.onNodeWithTag("snapshot:build:dependency-2").performClick()
        intended(allOf(hasComponent(BuildDetailsActivity::class.java.name), hasExtra(BundleExtractorValues.NAME, ""), BuildComposeFixtures.fullPayload(BundleExtractorValues.BUILD, running)))
        awaitTag("overview:list")
        onView(withText("Artifacts")).check(androidx.test.espresso.assertion.ViewAssertions.doesNotExist())
        onView(withText(R.string.tab_snapshot_dependencies)).perform(scrollTo()).check(matches(isDisplayed()))
    }

    @Test fun configurationHeaderLaunchesTheCorrectHistory() {
        dependencies()
        start()
        awaitTag("snapshot:configuration:0")
        compose.onNodeWithTag("snapshot:configuration:0").performClick()
        intended(allOf(hasComponent(BuildHistoryNavigation.LEGACY_ACTIVITY), hasExtra(BundleExtractorValues.ID, "current-config"), hasExtra(BundleExtractorValues.NAME, "Current configuration")))
    }

    @Test fun initialFailureCanRetryTheDependencyQuery() {
        `when`(teamCityService.listBuilds(anyString())).thenReturn(Single.error(RuntimeException("snapshot offline")))
        start()
        compose.waitUntil(10_000) { snapshotText(text(R.string.error_view_error_text)).fetchSemanticsNodes().isNotEmpty() }
        dependencies()
        snapshotText(text(teamcityapp.libraries.theme.R.string.action_retry)).onFirst().performClick()
        awaitTag("snapshot:build:dependency-1")
    }

    @Test fun failedHydrationIsReportedAndRetriedInsteadOfPassingPartialBuilds() {
        dependencies()
        `when`(teamCityService.build(running.href)).thenReturn(Single.error(RuntimeException("detail offline")))
        start()
        compose.waitUntil(10_000) { snapshotText(text(R.string.error_view_error_text)).fetchSemanticsNodes().isNotEmpty() }
        `when`(teamCityService.build(running.href)).thenReturn(Single.just(BuildComposeFixtures.legacy(running)))
        snapshotText(text(teamcityapp.libraries.theme.R.string.action_retry)).onFirst().performClick()
        awaitTag("snapshot:build:dependency-2")
    }

    @Test fun restoredSnapshotTabRetainsCompletedRowsWithoutDuplicateNetworkWork() {
        val lists = AtomicInteger()
        dependencies()
        `when`(teamCityService.listBuilds(anyString())).thenAnswer {
            lists.incrementAndGet()
            Single.just(Builds(3, snapshots.map(BuildComposeFixtures::legacy)))
        }
        ActivityScenario.launch<BuildDetailsActivity>(BuildComposeFixtures.intent()).use { scenario ->
            tab(text(R.string.tab_snapshot_dependencies))
            awaitTag("snapshot:build:dependency-1")
            val completedLists = lists.get()
            scenario.recreate()
            awaitTag("snapshot:build:dependency-1")
            assertEquals(completedLists, lists.get())
            compose.onNodeWithTag("snapshot:build:dependency-2").performClick()
            intended(allOf(hasComponent(BuildDetailsActivity::class.java.name), BuildComposeFixtures.fullPayload(BundleExtractorValues.BUILD, running)))
        }
    }

    @Test fun switchingAwayCancelsPendingDependenciesAndReturningReloads() {
        val pending = SingleSubject.create<Builds>()
        val cancellations = AtomicInteger()
        `when`(teamCityService.listBuilds(anyString())).thenReturn(pending.doOnDispose { cancellations.incrementAndGet() })
        start()
        compose.waitUntil(10_000) { pending.hasObservers() }
        tab("Overview")
        compose.waitUntil(10_000) { cancellations.get() == 1 }
        assertFalse(pending.hasObservers())
        dependencies()
        tab(text(R.string.tab_snapshot_dependencies))
        awaitTag("snapshot:build:dependency-1")
        snapshotText(text(R.string.error_view_error_text)).assertCountEquals(0)
    }
    private val queued = BuildComposeFixtures.loaded.copy(id = "dependency-1", href = "/buildQueue/dependency-1", state = "queued", waitReason = "Waiting for dependency agent")
    private val running = BuildComposeFixtures.loaded.copy(id = "dependency-2", href = "/builds/dependency-2", statusText = "Running dependency tests")
    private val finished = BuildComposeFixtures.finished.copy(id = "dependency-3", href = "/builds/dependency-3", statusText = "Finished dependency", configuration = BuildConfigurationData("other-config", "Other configuration", "other-project", "Other project"), buildTypeId = "other-config")
    private val snapshots get() = listOf(queued, running, finished)
    private fun dependencies() {
        `when`(teamCityService.listBuilds(anyString())).thenReturn(Single.just(Builds(3, snapshots.map(BuildComposeFixtures::legacy))))
        snapshots.forEach { build -> `when`(teamCityService.build(build.href)).thenReturn(Single.just(BuildComposeFixtures.legacy(build))) }
        `when`(teamCityService.build(BuildComposeFixtures.incoming.href)).thenReturn(Single.just(BuildComposeFixtures.legacy(BuildComposeFixtures.finished)))
    }
    private fun start() {
        `when`(teamCityService.build(BuildComposeFixtures.incoming.href)).thenReturn(Single.just(BuildComposeFixtures.legacy(BuildComposeFixtures.finished)))
        activityRule.launchActivity(BuildComposeFixtures.intent())
        tab(text(R.string.tab_snapshot_dependencies))
    }
}
