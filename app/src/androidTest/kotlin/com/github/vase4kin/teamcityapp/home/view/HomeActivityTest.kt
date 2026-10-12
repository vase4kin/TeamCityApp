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
import io.reactivex.Single
import io.reactivex.subjects.SingleSubject
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.greaterThanOrEqualTo
import org.hamcrest.core.AllOf.allOf
import org.junit.*
import org.junit.Assert.assertEquals
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.*
import org.mockito.Spy
import teamcityapp.features.build_history.api.navigation.BuildHistoryDestination
import teamcityapp.features.favorites.impl.R as FavoritesR
import teamcityapp.features.home.impl.R as HomeR
import teamcityapp.features.navigation.api.navigation.ProjectDestination
import teamcityapp.features.navigation.impl.R as NavigationR

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class HomeActivityTest {
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
        `when`(teamCityService.listBuildTypes("id")).thenReturn(Single.just(child))
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
        verify(teamCityService).listBuildTypes(LegacyProjectEntry.ROOT_PROJECT_ID)
        verify(teamCityService).listBuildTypes("id")
        row("project:child").performClick()
        assertTextVisible("New project")
        assertTextVisible("Description")
        assertEquals(ProjectDestination("child", "New project", false), currentRootSnapshot().entries.last().destination)
        verify(teamCityService).listBuildTypes("child")
    }

    @Test fun configurationRetainsBuildListArguments() {
        activityRule.launchActivity(null)
        awaitRow("configuration:build_type_id")
        row("configuration:build_type_id").performClick()
        org.junit.Assert.assertEquals(BuildHistoryDestination("build_type_id", "build type"), currentRootSnapshot().entries.last().destination)
        compose.waitUntil(10_000) { compose.onAllNodesWithTag("history:back").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("build type", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test fun rootToolbarOpensTheDrawer() {
        activityRule.launchActivity(null)
        awaitRow("project:id")
        compose.onNodeWithContentDescription(text(NavigationR.string.navigation_open_drawer)).performClick()
        compose.waitUntil(10_000) { compose.onAllNodesWithTag("drawer:list").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithTag("drawer:settings").assertIsDisplayed()
    }

    @Test fun initialErrorCanBeRetried() {
        `when`(teamCityService.listBuildTypes(anyString())).thenReturn(Single.error(RuntimeException("offline")))
        activityRule.launchActivity(null)
        assertTextVisible(text(teamcityapp.libraries.theme.R.string.error_load_message))
        `when`(teamCityService.listBuildTypes(anyString())).thenReturn(Single.just(Mocks.navigationNode()))
        compose.onNodeWithText(text(teamcityapp.libraries.theme.R.string.action_retry)).performClick()
        awaitRow("project:id")
        row("configuration:build_type_id").assertIsDisplayed()
    }

    @Test fun emptyProjectsShowTheComposeEmptyState() {
        `when`(teamCityService.listBuildTypes(anyString())).thenReturn(Single.just(NavigationNode(Projects(emptyList()), BuildTypes(emptyList()))))
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
            verify(teamCityService, times(1)).listBuildTypes(LegacyProjectEntry.ROOT_PROJECT_ID)
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

    @Test fun failedRunningCountStaysBelowFavoritesAndRetriesWithoutMovingItsToolbar() {
        (context.applicationContext as TeamCityApplicationBase).appInjector.sharedUserStorage().addBuildTypeToFavorites("build_type_id")
        val runningCount = SingleSubject.create<Builds>()
        doReturn(runningCount).`when`(teamCityService).listRunningBuilds(anyString(), eq("count"))
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
        runningCount.onError(RuntimeException("count unavailable"))
        compose.waitUntil(10_000) { compose.onAllNodesWithTag("home:retry-notice").fetchSemanticsNodes().isNotEmpty() }
        assertEquals(beforeFailure, titleBounds())
        val notice = compose.onNodeWithTag("home:retry-notice").assertIsDisplayed().fetchSemanticsNode().boundsInWindow
        Assert.assertTrue(notice.top >= beforeFailure.bottom)
        Assert.assertTrue(notice.bottom <= compose.onNodeWithTag("home:tab:Favorites").fetchSemanticsNode().boundsInWindow.top)
        compose.onNodeWithTag("home:count-failed:Running", useUnmergedTree = true).assertIsDisplayed()
        compose.onNodeWithTag("home:count:Favorites", useUnmergedTree = true).assertTextEquals("1")
        compose.onNodeWithTag("favorites:configuration:build_type_id").assertIsDisplayed()
        doReturn(Single.just(Builds(3, emptyList()))).`when`(teamCityService).listRunningBuilds(anyString(), eq("count"))
        compose.onNodeWithText(text(HomeR.string.home_retry_counts)).performClick()
        compose.waitUntil(10_000) { compose.onAllNodesWithTag("home:retry-notice").fetchSemanticsNodes().isEmpty() }
        compose.onNodeWithTag("home:count:Running", useUnmergedTree = true).assertTextEquals("3")
        assertEquals(beforeFailure, titleBounds())
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
