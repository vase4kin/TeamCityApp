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
import com.github.vase4kin.teamcityapp.dagger.modules.FakeTeamCityServiceImpl
import com.github.vase4kin.teamcityapp.dagger.modules.Mocks
import com.github.vase4kin.teamcityapp.helper.CustomIntentsTestRule
import com.github.vase4kin.teamcityapp.helper.HiltApiTestRule
import com.github.vase4kin.teamcityapp.helper.TestUtils
import com.github.vase4kin.teamcityapp.navigation.api.*
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.reactivex.Single
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.greaterThanOrEqualTo
import org.hamcrest.core.AllOf.allOf
import org.junit.*
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.*
import org.mockito.Spy
import teamcityapp.features.build_history.api.BuildHistoryNavigation
import teamcityapp.features.navigation.api.NavigationNavigation
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
    val activityRule = CustomIntentsTestRule(HomeActivity::class.java)

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
        intended(allOf(hasComponent(NavigationNavigation.LEGACY_ACTIVITY), hasExtra(NavigationNavigation.PROJECT_ID, "id"), hasExtra(NavigationNavigation.PROJECT_NAME, "Project")))
        verify(teamCityService).listBuildTypes(NavigationNavigation.ROOT_PROJECT_ID)
        verify(teamCityService).listBuildTypes("id")
        row("project:child").performClick()
        assertTextVisible("New project")
        assertTextVisible("Description")
        intended(allOf(hasComponent(NavigationNavigation.LEGACY_ACTIVITY), hasExtra(NavigationNavigation.PROJECT_ID, "child"), hasExtra(NavigationNavigation.PROJECT_NAME, "New project")))
        verify(teamCityService).listBuildTypes("child")
    }

    @Test fun configurationRetainsBuildListArguments() {
        activityRule.launchActivity(null)
        awaitRow("configuration:build_type_id")
        row("configuration:build_type_id").performClick()
        intended(allOf(hasComponent(BuildHistoryNavigation.LEGACY_ACTIVITY), hasExtra(BundleExtractorValues.ID, "build_type_id"), hasExtra(BundleExtractorValues.NAME, "build type")))
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
        assertTextVisible(text(R.string.error_view_error_text))
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
        ActivityScenario.launch<HomeActivity>(Intent(context, HomeActivity::class.java)).use { scenario ->
            awaitRow("project:id")
            scenario.recreate()
            awaitRow("project:id")
            row("configuration:build_type_id").assertIsDisplayed()
            verify(teamCityService, times(1)).listBuildTypes(NavigationNavigation.ROOT_PROJECT_ID)
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
        val title = compose.onNodeWithText(text(NavigationR.string.navigation_projects_title), useUnmergedTree = true)
            .fetchSemanticsNode().boundsInWindow
        assertThat(title.top.toInt(), greaterThanOrEqualTo(inset))
    }

    private fun row(identity: String) = compose.onNodeWithTag("navigation:row:$identity")
    private fun awaitRow(identity: String) {
        compose.waitUntil(10_000) { compose.onAllNodesWithTag("navigation:row:$identity").fetchSemanticsNodes().isNotEmpty() }
    }
    private fun assertTextVisible(value: String) {
        compose.waitUntil(10_000) { compose.onAllNodesWithText(value, useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText(value, useUnmergedTree = true).assertIsDisplayed()
    }
    private fun text(id: Int) = context.getString(id)
}
