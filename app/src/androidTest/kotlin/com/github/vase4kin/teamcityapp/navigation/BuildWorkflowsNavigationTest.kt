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

package com.github.vase4kin.teamcityapp.navigation

import android.content.Intent
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.pressBack
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.vase4kin.teamcityapp.TeamCityApplicationBase
import com.github.vase4kin.teamcityapp.api.TeamCityService
import com.github.vase4kin.teamcityapp.buildlist.api.Build
import com.github.vase4kin.teamcityapp.buildlist.filter.BuildListFilterImpl
import com.github.vase4kin.teamcityapp.di.modules.FakeTeamCityServiceImpl
import com.github.vase4kin.teamcityapp.di.modules.Mocks
import com.github.vase4kin.teamcityapp.helper.HiltApiTestRule
import com.github.vase4kin.teamcityapp.helper.RootDestinationLauncher
import com.github.vase4kin.teamcityapp.helper.TestUtils
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.reactivex.subjects.SingleSubject
import javax.inject.Inject
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.mockito.Spy
import teamcityapp.features.change_details.api.ChangeDetails
import teamcityapp.features.change_details.api.ChangedFile
import teamcityapp.features.change_details.api.navigation.ChangeDetailsDestination
import teamcityapp.features.test_details.api.navigation.TestDetailsDestination

@OptIn(ExperimentalTestApi::class)
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class BuildWorkflowsNavigationTest {
    @JvmField
    @Rule(order = 0)
    val hilt = HiltAndroidRule(this)

    @JvmField
    @Rule(order = 1)
    val mockito = org.mockito.junit.MockitoJUnit.rule().strictness(org.mockito.quality.Strictness.LENIENT)

    @JvmField
    @Rule(order = 2)
    val api = HiltApiTestRule(hilt) { service }

    @JvmField
    @Rule(order = 3)
    val compose = createEmptyComposeRule()

    @Spy private val service: TeamCityService = FakeTeamCityServiceImpl()

    @Inject lateinit var launcher: RootDestinationLauncher

    private val app get() = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TeamCityApplicationBase
    private val storage get() = app.appInjector.sharedUserStorage()

    @Before fun before() {
        TestUtils.disableOnboarding()
        storage.clearAll()
        storage.saveGuestUserAccountAndSetItAsActive(Mocks.URL, false)
    }
    private fun launch() = ActivityScenario.launch<MainActivity>(Intent(app, MainActivity::class.java))
    private fun awaitTag(tag: String) {
        compose.waitUntil(15_000) { compose.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithTag(tag).assertIsDisplayed()
    }
    private fun awaitRoot() = awaitTag("navigation:row:project:id")
    private fun openHistory() {
        awaitRoot()
        compose.onNodeWithTag("navigation:row:configuration:build_type_id").performClick()
        awaitTag("history:list")
    }

    @Test fun filterResultSurvivesFormRecreationAndReplacesTheHistoryQueryInTheSameActivity() {
        launch().use { scenario ->
            openHistory()
            compose.onNodeWithTag("history:filter").performClick()
            awaitTag("filter-builds:chooser")
            compose.onNodeWithTag("filter-builds:status:Success").performClick()
            scenario.recreate()
            awaitTag("filter-builds:chooser")
            compose.onNodeWithTag("filter-builds:status:Success").assertIsSelected()
            compose.onNodeWithTag("filter-builds:apply").performClick()
            awaitTag("history:list")
            val locator = BuildListFilterImpl().apply { setFilter(0) }.toLocator()
            compose.waitUntil(10_000) { runCatching { verify(service).listBuilds("build_type_id", locator) }.isSuccess }
            scenario.recreate()
            awaitTag("history:list")
            pressBack()
            awaitRoot()
            scenario.onActivity { assertTrue(it.supportFragmentManager.fragments.isEmpty()) }
        }
    }

    @Test fun cancellingRunBuildReturnsToHistoryWithoutQueueing() {
        launch().use {
            openHistory()
            compose.onNodeWithTag("history:run").performClick()
            awaitTag("run-build:submit")
            pressBack()
            awaitTag("history:list")
            verify(service, never()).queueBuild(org.mockito.kotlin.any())
        }
    }

    @Test fun queuedResultReturnsToItsHistoryVisitAndIsNotResubmittedAfterRecreation() {
        launch().use { scenario ->
            openHistory()
            compose.onNodeWithTag("history:run").performClick()
            awaitTag("run-build:submit")
            scenario.recreate()
            awaitTag("run-build:submit")
            compose.onNodeWithTag("run-build:submit").performClick()
            awaitTag("history:list")
            compose.waitUntil(10_000) { compose.onAllNodesWithText("Build is added to build queue").fetchSemanticsNodes().isNotEmpty() }
            scenario.recreate()
            awaitTag("history:list")
            verify(service, times(1)).queueBuild(org.mockito.kotlin.any())
            scenario.onActivity { assertTrue(it.supportFragmentManager.fragments.isEmpty()) }
        }
    }

    @Test fun pendingRunRetainsItsRequestAcrossRecreationAndDeliversOnlyOneTypedResult() {
        val pending = SingleSubject.create<Build>()
        doReturn(pending).`when`(service).queueBuild(org.mockito.kotlin.any())
        launch().use { scenario ->
            openHistory()
            var historyVisit: String? = null
            scenario.onActivity { historyVisit = it.navigationSnapshot!!.entries.last().id }
            compose.onNodeWithTag("history:run").performClick()
            awaitTag("run-build:submit")
            compose.onNodeWithTag("run-build:submit").performClick()
            compose.waitUntil(10_000) { pending.hasObservers() }
            scenario.recreate()
            awaitTag("run-build:submit")
            compose.onNodeWithTag("run-build:submit").assertIsNotEnabled()
            assertTrue(pending.hasObservers())
            verify(service, times(1)).queueBuild(org.mockito.kotlin.any())
            pending.onSuccess(Mocks.queuedBuild1())
            awaitTag("history:list")
            compose.waitUntil(10_000) { compose.onAllNodesWithText("Build is added to build queue").fetchSemanticsNodes().isNotEmpty() }
            scenario.recreate()
            awaitTag("history:list")
            scenario.onActivity {
                assertEquals(historyVisit, it.navigationSnapshot!!.entries.last().id)
                assertTrue(it.navigationSnapshot!!.results.isEmpty())
            }
            verify(service, times(1)).queueBuild(org.mockito.kotlin.any())
        }
    }

    @Test fun replacingTheAccountStackCancelsAPendingRunAndIgnoresItsLateResult() {
        val pending = SingleSubject.create<Build>()
        doReturn(pending).`when`(service).queueBuild(org.mockito.kotlin.any())
        launch().use { scenario ->
            openHistory()
            compose.onNodeWithTag("history:run").performClick()
            awaitTag("run-build:submit")
            compose.onNodeWithTag("run-build:submit").performClick()
            compose.waitUntil(10_000) { pending.hasObservers() }
            scenario.onActivity { activity ->
                activity.startActivity(android.content.Intent().setClassName(activity, IncomingDestinationResolver.HOME_COMPONENT).putExtra(com.github.vase4kin.teamcityapp.base.extractor.BundleExtractorValues.IS_REQUIRED_TO_RELOAD, true).addFlags(android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP))
            }
            awaitRoot()
            compose.waitUntil(10_000) { !pending.hasObservers() }
            pending.onSuccess(Mocks.queuedBuild1())
            compose.waitForIdle()
            scenario.onActivity {
                assertEquals(1, it.navigationSnapshot!!.entries.size)
                assertTrue(it.navigationSnapshot!!.entries.single().destination is teamcityapp.features.home.api.HomeDestination)
                assertTrue(it.navigationSnapshot!!.results.isEmpty())
            }
            compose.onNodeWithText("Build is added to build queue").assertDoesNotExist()
            verify(service, times(1)).queueBuild(org.mockito.kotlin.any())
        }
    }

    @Test fun testAndChangeDetailsUseDestinationArgumentsAndBackRestoresHome() {
        launch().use { scenario ->
            awaitRoot()
            scenario.onActivity { launcher.open(it, TestDetailsDestination("/guestAuth/app/rest/testOccurrences/id:4482,build:(id:835695)")) }
            compose.waitUntil(10_000) { compose.onAllNodesWithText("exception error").fetchSemanticsNodes().isNotEmpty() }
            pressBack()
            awaitRoot()
            val details = ChangeDetails("change-1", "  Commit message  ", "Author", "2026-10-11", listOf(ChangedFile("Main.kt", "edited")), "revision", "https://example.org/change")
            scenario.onActivity { launcher.open(it, ChangeDetailsDestination(details)) }
            awaitTag("change_details:list")
            compose.onNodeWithText("Commit message").assertIsDisplayed()
            scenario.recreate()
            awaitTag("change_details:list")
            compose.onNodeWithText("Commit message").assertIsDisplayed()
            pressBack()
            awaitRoot()
            scenario.onActivity { assertTrue(it.supportFragmentManager.fragments.isEmpty()) }
        }
    }

    @Test fun favoritesActionSelectsTheExistingHomeAndPreservesTheProjectsHistory() {
        launch().use { scenario ->
            openHistory()
            var historyVisit: String? = null
            scenario.onActivity { historyVisit = it.navigationSnapshot!!.entries.last().id }
            compose.waitUntil(10_000) { runCatching { compose.onNodeWithTag("history:favorite").assertIsEnabled() }.isSuccess }
            compose.onNodeWithTag("history:favorite").performClick()
            compose.waitUntil(10_000) { compose.onAllNodesWithText("Open favorites").fetchSemanticsNodes().isNotEmpty() }
            compose.onNodeWithText("Open favorites").performClick()
            awaitTag("home:tab:Favorites")
            compose.onNodeWithTag("home:tab:Favorites").assertIsSelected()
            compose.onNodeWithTag("history:list").assertDoesNotExist()
            compose.onNodeWithTag("home:tab:Projects").performClick()
            awaitTag("history:list")
            scenario.onActivity { assertEquals(historyVisit, it.navigationSnapshot!!.entries.last().id) }
            compose.onNodeWithTag("home:tab:Favorites").performClick()
            awaitTag("favorites:configuration:build_type_id")
            androidx.test.espresso.Espresso.pressBackUnconditionally()
            compose.waitUntil(10_000) { scenario.state == androidx.lifecycle.Lifecycle.State.DESTROYED }
        }
    }
}
