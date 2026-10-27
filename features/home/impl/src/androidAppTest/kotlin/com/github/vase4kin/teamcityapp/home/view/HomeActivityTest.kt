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

import android.content.Intent
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.TeamCityApplicationBase
import com.github.vase4kin.teamcityapp.api.TeamCityService
import com.github.vase4kin.teamcityapp.base.extractor.BundleExtractorValues
import com.github.vase4kin.teamcityapp.buildlist.api.Builds
import com.github.vase4kin.teamcityapp.di.modules.FakeTeamCityServiceImpl
import com.github.vase4kin.teamcityapp.di.modules.Mocks
import com.github.vase4kin.teamcityapp.di.modules.PendingFixture
import com.github.vase4kin.teamcityapp.helper.CustomIntentsTestRule
import com.github.vase4kin.teamcityapp.helper.HiltApiTestRule
import com.github.vase4kin.teamcityapp.helper.TestUtils
import com.github.vase4kin.teamcityapp.helper.currentRootSnapshot
import com.github.vase4kin.teamcityapp.helper.hasDestination
import com.github.vase4kin.teamcityapp.navigation.IncomingDestinationResolver
import com.github.vase4kin.teamcityapp.navigation.LegacyProjectEntry
import com.github.vase4kin.teamcityapp.navigation.MainActivity
import com.github.vase4kin.teamcityapp.navigation.api.*
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.greaterThanOrEqualTo
import org.hamcrest.core.AllOf.allOf
import org.junit.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.ArgumentMatchers.eq
import org.mockito.ArgumentMatchers.nullable
import org.mockito.Mockito.*
import org.mockito.Spy
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doSuspendableAnswer
import org.mockito.kotlin.verifyBlocking
import org.mockito.kotlin.whenever
import teamcityapp.features.build_history.api.navigation.BuildHistoryDestination
import teamcityapp.features.favorites.impl.R as FavoritesR
import teamcityapp.features.home.impl.R as HomeR
import teamcityapp.features.navigation.api.navigation.ProjectDestination
import teamcityapp.features.navigation.impl.R as NavigationR

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class HomeActivityTest {
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
    val activityRule = CustomIntentsTestRule(MainActivity::class.java, IncomingDestinationResolver.HOME_COMPONENT)

    @JvmField
    @Rule(order = 4)
    val compose = createEmptyComposeRule()

    @Spy private val teamCityService: TeamCityService = FakeTeamCityServiceImpl()
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

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
        app.getSharedPreferences("rateTheAppPref", android.content.Context.MODE_PRIVATE).edit().putBoolean("rated", true).commit()
    }

    @Test fun projectsAndConfigurationsRenderAndNavigateRecursively() {
        val child = NavigationNode(
            Projects(
                listOf(
                    Project().apply {
                        id = "child"
                        name = "New project"
                        description = "Contains a lot of projects"
                    }
                )
            ),
            BuildTypes(
                listOf(
                    BuildType().apply {
                        setId("child-build")
                        name = "Build and run tests"
                    }
                )
            )
        )
        doReturn(child).whenever(teamCityService) { listBuildTypes("id") }
        activityRule.launchActivity(null)
        assertTextVisible(text(NavigationR.string.navigation_projects_title))
        awaitRow("project:id")
        row("project:id").assertTextContains("Project").assertTextContains("Description")
        row("configuration:build_type_id").assertTextContains("build type")
        row("project:id").performClick()
        assertTextVisible("New project")
        row("project:child").assertTextContains("Contains a lot of projects")
        row("configuration:child-build").assertTextContains("Build and run tests")
        org.junit.Assert.assertEquals(ProjectDestination("id", "Project", false), currentRootSnapshot().entries.last().destination)
        verifyBlocking(teamCityService) { listBuildTypes(LegacyProjectEntry.ROOT_PROJECT_ID) }
        verifyBlocking(teamCityService) { listBuildTypes("id") }
        row("project:child").performClick()
        assertTextVisible("New project")
        assertTextVisible("Description")
        assertEquals(ProjectDestination("child", "New project", false), currentRootSnapshot().entries.last().destination)
        verifyBlocking(teamCityService) { listBuildTypes("child") }
    }

    @Test fun configurationRetainsBuildListArguments() {
        activityRule.launchActivity(null)
        awaitRow("configuration:build_type_id")
        row("configuration:build_type_id").performClick()
        org.junit.Assert.assertEquals(BuildHistoryDestination("build_type_id", "build type"), currentRootSnapshot().entries.last().destination)
        compose.waitUntil(10_000) { compose.onAllNodesWithTag("history:back").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("build type", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test fun avatarOpensAccount() {
        activityRule.launchActivity(null)
        awaitRow("project:id")
        compose.onNodeWithTag("home:account").performClick()
        compose.waitUntil(10_000) { compose.onAllNodesWithTag("drawer:list").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithTag("drawer:settings").assertIsDisplayed()
    }

    @Test fun initialErrorCanBeRetried() {
        doSuspendableAnswer { throw RuntimeException("offline") }.whenever(teamCityService) { listBuildTypes(anyString()) }
        activityRule.launchActivity(null)
        assertTextVisible(text(teamcityapp.libraries.theme.R.string.error_load_message))
        doReturn(Mocks.navigationNode()).whenever(teamCityService) { listBuildTypes(anyString()) }
        compose.onNodeWithText(text(teamcityapp.libraries.theme.R.string.action_retry)).performClick()
        awaitRow("project:id")
        row("configuration:build_type_id").assertIsDisplayed()
    }

    @Test fun emptyProjectsShowTheComposeEmptyState() {
        doReturn(NavigationNode(Projects(emptyList()), BuildTypes(emptyList()))).whenever(teamCityService) { listBuildTypes(anyString()) }
        activityRule.launchActivity(null)
        assertTextVisible(text(NavigationR.string.navigation_empty))
        compose.onNodeWithTag("navigation:rating").assertDoesNotExist()
    }

    @Test fun completedRootDataSurvivesRecreationWithoutAnotherRequest() {
        ActivityScenario.launch<MainActivity>(Intent(context, MainActivity::class.java).setClassName(context, IncomingDestinationResolver.HOME_COMPONENT)).use { scenario ->
            awaitRow("project:id")
            scenario.recreate()
            awaitRow("project:id")
            row("configuration:build_type_id").assertIsDisplayed()
            verifyBlocking(teamCityService, times(1)) { listBuildTypes(LegacyProjectEntry.ROOT_PROJECT_ID) }
        }
    }

    @Test fun composeToolbarDoesNotOverlapStatusBar() {
        activityRule.launchActivity(null)
        awaitRow("project:id")
        var inset = 0
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            inset = ViewCompat.getRootWindowInsets(activityRule.activity.window.decorView)
                ?.getInsets(WindowInsetsCompat.Type.statusBars())?.top ?: 0
        }
        val title = compose.onAllNodesWithText(text(NavigationR.string.navigation_projects_title), useUnmergedTree = true)
            .fetchSemanticsNodes().minBy { it.boundsInWindow.top }.boundsInWindow
        assertThat(title.top.toInt(), greaterThanOrEqualTo(inset))
    }

    @Test fun failedRunningCountStaysQuietOnFavoritesAndRetriesOnlyOnActivity() {
        (context.applicationContext as TeamCityApplicationBase).appInjector.sharedUserStorage().addBuildTypeToFavorites("build_type_id")
        val runningCount = PendingFixture<Builds>()
        doSuspendableAnswer { runningCount.await() }.whenever(teamCityService) { listRunningBuilds(anyString(), eq("count")) }
        activityRule.launchActivity(null)
        awaitRow("project:id")
        compose.onNodeWithTag("home:tab:Favorites").performClick()
        compose.waitUntil(10_000) { compose.onAllNodesWithTag("favorites:configuration:build_type_id").fetchSemanticsNodes().isNotEmpty() }
        fun titleBounds() = compose.onAllNodesWithText(text(FavoritesR.string.favorites_title), useUnmergedTree = true).fetchSemanticsNodes().minBy { it.boundsInWindow.top }.boundsInWindow
        val beforeFailure = titleBounds()
        var statusInset = 0
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            statusInset = ViewCompat.getRootWindowInsets(activityRule.activity.window.decorView)?.getInsets(WindowInsetsCompat.Type.statusBars())?.top ?: 0
        }
        assertThat(beforeFailure.top.toInt(), greaterThanOrEqualTo(statusInset))
        runningCount.fail(RuntimeException("count unavailable"))
        compose.waitForIdle()
        compose.onNodeWithTag("home:snackbar").assertDoesNotExist()
        assertEquals(beforeFailure, titleBounds())
        compose.onNodeWithTag("favorites:configuration:build_type_id").assertIsDisplayed()
        compose.onNodeWithTag("home:tab:Activity").performClick()
        compose.onNodeWithTag("home:tab:Running").performClick()
        compose.waitUntil(10_000) { compose.onAllNodesWithText(text(HomeR.string.home_running_count_failed)).fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithTag("home:snackbar").assertIsDisplayed()
        doReturn(Builds(3, emptyList())).whenever(teamCityService) { listRunningBuilds(anyString(), eq("count")) }
        compose.onNode(hasText(text(HomeR.string.home_retry)) and hasAnyAncestor(hasTestTag("retry:snackbar"))).performClick()
        compose.waitUntil(10_000) { compose.onAllNodesWithTag("home:snackbar").fetchSemanticsNodes().isEmpty() }
        compose.onNodeWithTag("home:count:Running", useUnmergedTree = true).assertTextEquals("3")
        compose.onNodeWithTag("home:count:Activity", useUnmergedTree = true).assertDoesNotExist()
        compose.onNodeWithTag("home:count-failed:Activity", useUnmergedTree = true).assertDoesNotExist()
    }

    @Test fun primaryTabsRetainTheirNestedVisitsAcrossRecreationAndBack() {
        val storage = (context.applicationContext as TeamCityApplicationBase).appInjector.sharedUserStorage()
        storage.addBuildTypeToFavorites("build_type_id")
        val successful = Mocks.successBuild().apply { id = "history-build" }
        val running = Mocks.runningBuild().apply { id = "running-build" }
        doReturn(Builds(1, listOf(successful))).whenever(teamCityService) { listBuilds(anyString(), anyString()) }
        doReturn(Builds(1, listOf(running))).whenever(teamCityService) { listRunningBuilds(anyString(), nullable(String::class.java)) }
        doReturn(successful).whenever(teamCityService) { build(successful.href) }
        doReturn(running).whenever(teamCityService) { build(running.href) }
        ActivityScenario.launch<MainActivity>(Intent(context, MainActivity::class.java).setClassName(context, IncomingDestinationResolver.HOME_COMPONENT)).use { scenario ->
            fun await(tag: String) {
                compose.waitUntil(10_000) { compose.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty() }
                compose.onNodeWithTag(tag).assertIsDisplayed()
            }
            fun assertNavigation() {
                listOf("Projects", "Favorites", "Activity").forEach { compose.onNodeWithTag("home:tab:$it").assertIsDisplayed() }
                compose.onNodeWithTag("home:title").assertDoesNotExist()
                compose.onAllNodesWithTag("screen:app-bar").assertCountEquals(1)
            }
            awaitRow("project:id")
            row("project:id").performClick()
            awaitRow("configuration:build_type_id")
            assertNavigation()
            val project = currentRootSnapshot().entries.last().id
            row("configuration:build_type_id").performClick()
            await("history:build:history-build")
            assertNavigation()
            val history = currentRootSnapshot().entries.last().id
            compose.onNodeWithTag("history:build:history-build").performClick()
            await("details:actions")
            assertNavigation()
            val build = currentRootSnapshot().entries.last().id

            compose.onNodeWithTag("home:tab:Favorites").performClick()
            await("favorites:configuration:build_type_id")
            compose.onNodeWithTag("favorites:configuration:build_type_id").performClick()
            await("history:list")
            val favoriteHistory = currentRootSnapshot().entries.last().id
            val historyRequests = mockingDetails(teamCityService).invocations.count { it.method.name == "listBuilds" }
            assertNavigation()

            compose.onNodeWithTag("home:tab:Activity").performClick()
            await("running_builds:build:running-build")
            compose.onNodeWithTag("running_builds:build:running-build").performClick()
            await("details:actions")
            val activityBuild = currentRootSnapshot().entries.last().id
            assertNavigation()
            scenario.recreate()
            await("details:actions")
            assertEquals(activityBuild, currentRootSnapshot().entries.last().id)

            compose.onNodeWithTag("home:tab:Favorites").performClick()
            await("history:list")
            assertEquals(favoriteHistory, currentRootSnapshot().entries.last().id)
            assertEquals(historyRequests, mockingDetails(teamCityService).invocations.count { it.method.name == "listBuilds" })
            compose.onNodeWithTag("home:tab:Favorites").performClick()
            await("favorites:configuration:build_type_id")
            compose.onNodeWithTag("home:title").assertIsDisplayed()
            assertTrue(currentRootSnapshot().entries.last().destination is teamcityapp.features.home.api.HomeDestination)
            compose.onNodeWithTag("home:tab:Projects").performClick()
            await("details:actions")
            assertEquals(build, currentRootSnapshot().entries.last().id)
            assertNavigation()
            pressBack()
            await("history:list")
            assertEquals(history, currentRootSnapshot().entries.last().id)
            pressBack()
            awaitRow("configuration:build_type_id")
            assertEquals(project, currentRootSnapshot().entries.last().id)
            assertNavigation()
        }
    }

    @Test fun runningBuildTransitionKeepsBothHeadersStableAndNavigationVisible() {
        val storage = (context.applicationContext as TeamCityApplicationBase).appInjector.sharedUserStorage()
        storage.addBuildTypeToFavorites("build_type_id")
        val running = Mocks.runningBuild().apply { id = "running-transition" }
        doReturn(Builds(1, listOf(running))).whenever(teamCityService) { listRunningBuilds(anyString(), nullable(String::class.java)) }
        doReturn(running).whenever(teamCityService) { build(running.href) }
        ActivityScenario.launch<MainActivity>(Intent(context, MainActivity::class.java).setClassName(context, IncomingDestinationResolver.HOME_COMPONENT)).use {
            awaitRow("project:id")
            compose.onNodeWithTag("home:tab:Activity").performClick()
            compose.waitUntil(10_000) { compose.onAllNodesWithTag("running_builds:build:running-transition").fetchSemanticsNodes().isNotEmpty() }
            val navigationBounds = compose.onNodeWithTag("home:tab:Activity").fetchSemanticsNode().boundsInRoot
            val headerBounds = compose.onNodeWithTag("home:title").fetchSemanticsNode().boundsInRoot
            val listBounds = compose.onNodeWithTag("running_builds:list").fetchSemanticsNode().boundsInRoot
            compose.mainClock.autoAdvance = false
            try {
                compose.onNodeWithTag("running_builds:build:running-transition").performClick()
                compose.mainClock.advanceTimeByFrame()
                compose.waitForIdle()
                compose.mainClock.advanceTimeBy(320)
                compose.onNodeWithTag("running_builds:drawer").assertDoesNotExist()
                compose.onNodeWithTag("details:actions").assertExists()
                assertEquals(headerBounds, compose.onNodeWithTag("home:title").fetchSemanticsNode().boundsInRoot)
                assertEquals(listBounds, compose.onNodeWithTag("running_builds:list").fetchSemanticsNode().boundsInRoot)
                assertEquals(navigationBounds, compose.onNodeWithTag("home:tab:Activity").fetchSemanticsNode().boundsInRoot)
                compose.mainClock.advanceTimeBy(1_000)
                compose.waitForIdle()
                compose.onNodeWithTag("home:title").assertDoesNotExist()
                val actionsBounds = compose.onNodeWithTag("details:actions").fetchSemanticsNode().boundsInRoot
                pressBack()
                compose.mainClock.advanceTimeByFrame()
                compose.waitForIdle()
                compose.mainClock.advanceTimeBy(320)
                compose.onNodeWithTag("running_builds:drawer").assertDoesNotExist()
                assertEquals(headerBounds, compose.onNodeWithTag("home:title").fetchSemanticsNode().boundsInRoot)
                assertEquals(listBounds, compose.onNodeWithTag("running_builds:list").fetchSemanticsNode().boundsInRoot)
                assertEquals(actionsBounds, compose.onNodeWithTag("details:actions").fetchSemanticsNode().boundsInRoot)
                assertEquals(navigationBounds, compose.onNodeWithTag("home:tab:Activity").fetchSemanticsNode().boundsInRoot)
                compose.mainClock.advanceTimeBy(1_000)
                compose.waitForIdle()
                compose.onNodeWithTag("home:title").assertIsDisplayed()
                compose.onNodeWithTag("details:actions").assertDoesNotExist()
                compose.onNodeWithTag("running_builds:build:running-transition").assertIsDisplayed()
            } finally {
                compose.mainClock.autoAdvance = true
            }
        }
    }

    private fun row(identity: String) = compose.onNodeWithTag("navigation:row:$identity")
    private fun awaitRow(identity: String) {
        compose.waitUntil(10_000) { compose.onAllNodesWithTag("navigation:row:$identity").fetchSemanticsNodes().isNotEmpty() }
    }
    private fun assertTextVisible(value: String) {
        compose.waitUntil(10_000) { compose.onAllNodesWithText(value, useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty() }
        compose.onAllNodesWithText(value, useUnmergedTree = true).onFirst().assertIsDisplayed()
    }
    private fun text(id: Int) = context.getString(id)
}
