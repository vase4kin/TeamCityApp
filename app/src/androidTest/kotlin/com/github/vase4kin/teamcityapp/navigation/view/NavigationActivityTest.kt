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

package com.github.vase4kin.teamcityapp.navigation.view

import android.content.Intent
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.TeamCityApplicationBase
import com.github.vase4kin.teamcityapp.api.TeamCityService
import com.github.vase4kin.teamcityapp.dagger.modules.FakeTeamCityServiceImpl
import com.github.vase4kin.teamcityapp.dagger.modules.Mocks
import com.github.vase4kin.teamcityapp.helper.HiltApiTestRule
import com.github.vase4kin.teamcityapp.helper.TestUtils
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.reactivex.Single
import org.junit.*
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.*
import org.mockito.Spy
import teamcityapp.features.navigation.api.NavigationNavigation
import teamcityapp.features.navigation.impl.NavigationActivity
import teamcityapp.features.navigation.impl.R as NavigationR

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class NavigationActivityTest {
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

    @Test fun legacyActivityAliasAcceptsTheExistingProjectArguments() {
        launch().use { scenario ->
            awaitRows()
            compose.onNodeWithText("Selected project", useUnmergedTree = true).assertIsDisplayed()
            verify(teamCityService).listBuildTypes("selected-project")
            scenario.onActivity {
                Assert.assertEquals(NavigationNavigation.LEGACY_ACTIVITY, it.intent.component?.className)
                Assert.assertEquals("selected-project", it.intent.getStringExtra("id"))
                Assert.assertEquals("Selected project", it.intent.getStringExtra("name"))
            }
        }
    }

    @Test fun aliasArgumentsAndCompletedRowsSurviveRecreationWithoutReload() {
        launch().use { scenario ->
            awaitRows()
            scenario.recreate()
            awaitRows()
            compose.onNodeWithText("Selected project", useUnmergedTree = true).assertIsDisplayed()
            verify(teamCityService, times(1)).listBuildTypes("selected-project")
        }
    }

    @Test fun navigateUpFinishesTheRecursiveActivity() {
        launch().use { scenario ->
            awaitRows()
            compose.onNodeWithContentDescription(text(NavigationR.string.navigation_back)).performClick()
            compose.waitUntil(10_000) { scenario.state == Lifecycle.State.DESTROYED }
        }
    }

    @Test fun initialErrorRetainsProjectTitleAndCanBeRetried() {
        `when`(teamCityService.listBuildTypes(anyString())).thenReturn(Single.error(RuntimeException("offline")))
        launch().use {
            assertTextVisible(text(R.string.error_view_error_text))
            compose.onNodeWithText("Selected project", useUnmergedTree = true).assertIsDisplayed()
            `when`(teamCityService.listBuildTypes(anyString())).thenReturn(Single.just(Mocks.navigationNode()))
            compose.onNodeWithText(text(teamcityapp.libraries.theme.R.string.action_retry)).performClick()
            awaitRows()
        }
    }

    @Test fun refreshFailureKeepsRowsAndAllowsAnotherRefresh() {
        launch().use {
            awaitRows()
            `when`(teamCityService.listBuildTypes(anyString())).thenReturn(Single.error(RuntimeException("refresh offline")))
            compose.onNodeWithTag("navigation:list").performTouchInput { swipeDown() }
            assertTextVisible(text(teamcityapp.libraries.list_ui.R.string.list_refresh_failed))
            compose.onNodeWithTag("navigation:row:project:id").assertIsDisplayed()
            `when`(teamCityService.listBuildTypes(anyString())).thenReturn(Single.just(Mocks.navigationNode()))
            compose.onNodeWithText(text(teamcityapp.libraries.list_ui.R.string.list_action_retry)).performClick()
            compose.waitUntil(10_000) {
                compose.onAllNodesWithText(text(teamcityapp.libraries.list_ui.R.string.list_refresh_failed)).fetchSemanticsNodes().isEmpty()
            }
            compose.onNodeWithTag("navigation:row:configuration:build_type_id").assertIsDisplayed()
        }
    }

    private fun launch(): ActivityScenario<NavigationActivity> = ActivityScenario.launch(
        Intent().setClassName(context.packageName, NavigationNavigation.LEGACY_ACTIVITY)
            .putExtra(NavigationNavigation.PROJECT_ID, "selected-project")
            .putExtra(NavigationNavigation.PROJECT_NAME, "Selected project")
    )
    private fun awaitRows() {
        compose.waitUntil(10_000) { compose.onAllNodesWithTag("navigation:row:project:id").fetchSemanticsNodes().isNotEmpty() }
    }
    private fun assertTextVisible(value: String) {
        compose.waitUntil(10_000) { compose.onAllNodesWithText(value, useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText(value, useUnmergedTree = true).assertIsDisplayed()
    }
    private fun text(id: Int) = context.getString(id)
}
