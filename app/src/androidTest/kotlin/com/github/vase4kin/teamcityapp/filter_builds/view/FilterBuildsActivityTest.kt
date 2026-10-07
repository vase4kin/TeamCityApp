/*
 * Copyright 2019 Andrey Tolpeev
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

package com.github.vase4kin.teamcityapp.filter_builds.view

import android.view.View
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.TeamCityApplicationBase
import com.github.vase4kin.teamcityapp.api.TeamCityService
import com.github.vase4kin.teamcityapp.buildlist.api.Builds
import com.github.vase4kin.teamcityapp.dagger.modules.FakeTeamCityServiceImpl
import com.github.vase4kin.teamcityapp.dagger.modules.Mocks
import com.github.vase4kin.teamcityapp.helper.CustomIntentsTestRule
import com.github.vase4kin.teamcityapp.helper.HiltApiTestRule
import com.github.vase4kin.teamcityapp.helper.TestUtils
import com.github.vase4kin.teamcityapp.helper.eq
import com.github.vase4kin.teamcityapp.home.view.HomeActivity
import com.github.vase4kin.teamcityapp.runbuild.api.Branch
import com.github.vase4kin.teamcityapp.runbuild.api.Branches
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.reactivex.Single
import java.util.ArrayList
import org.hamcrest.Matchers.instanceOf
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.not
import org.hamcrest.core.AllOf.allOf
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.timeout
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.Spy
import teamcityapp.features.filter_builds.impl.FilterBuildsActivity

/**
 * Tests for [FilterBuildsActivity]
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class FilterBuildsActivityTest {
    @JvmField
    @Rule(order = 4)
    val compose = createEmptyComposeRule()
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

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
    val activityRule: CustomIntentsTestRule<HomeActivity> =
        CustomIntentsTestRule(HomeActivity::class.java)

    @Spy
    private val teamCityService: TeamCityService = FakeTeamCityServiceImpl()

    companion object {
        @JvmStatic
        @BeforeClass
        fun disableOnboarding() {
            TestUtils.disableOnboarding()
        }
    }

    @Before
    fun setUp() {
        val app =
            InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TeamCityApplicationBase
        app.appInjector.sharedUserStorage().clearAll()
        app.appInjector.sharedUserStorage()
            .saveGuestUserAccountAndSetItAsActive(Mocks.URL, false)
        `when`(
            teamCityService.listBuilds(
                anyString(),
                anyString()
            )
        ).thenReturn(Single.just(Builds(0, emptyList())))
    }

    @Test
    fun testUserCanFilterBuildsWithDefaultFilter() {
        activityRule.launchActivity(null)

        // Open build type
        onView(withText("build type")).perform(click())

        // Pressing filter builds toolbar item
        onView(withId(R.id.filter_builds)).perform(click())

        // Click on filter fab
        compose.onNodeWithTag("filter-builds:apply").performClick()

        // Check data was loaded with new filter
        verify(teamCityService, timeout(5_000)).listBuilds(
            anyString(),
            eq("state:any,canceled:any,failedToStart:any,branch:default:any,personal:false,pinned:false,count:10")
        )
    }

    @Test
    fun testUserCanFilterBuildsByPinned() {
        activityRule.launchActivity(null)

        // Open build type
        onView(withText("build type")).perform(click())

        // Pressing filter builds toolbar item
        onView(withId(R.id.filter_builds)).perform(click())

        // Click on pin switcher
        compose.onNodeWithTag("filter-builds:pinned").performClick()

        // Click on filter fab
        compose.onNodeWithTag("filter-builds:apply").performClick()

        // Check data was loaded with new filter
        verify(teamCityService, timeout(5_000)).listBuilds(
            anyString(),
            eq("state:any,canceled:any,failedToStart:any,branch:default:any,personal:false,pinned:true,count:10")
        )
    }

    @Test
    fun testUserCanFilterBuildsByPersonal() {
        activityRule.launchActivity(null)

        // Open build type
        onView(withText("build type")).perform(click())

        // Pressing filter builds toolbar item
        onView(withId(R.id.filter_builds)).perform(click())

        // Click on pin switcher
        compose.onNodeWithTag("filter-builds:personal").performClick()

        // Click on filter fab
        compose.onNodeWithTag("filter-builds:apply").performClick()

        // Check data was loaded with new filter
        verify<TeamCityService>(teamCityService, timeout(5_000)).listBuilds(
            anyString(),
            eq("state:any,canceled:any,failedToStart:any,branch:default:any,personal:true,pinned:false,count:10")
        )
    }

    @Test
    fun testUserCanFilterBuildsByBranch() {
        // Prepare mocks
        val branches = ArrayList<Branch>()
        branches.add(Branch("dev1"))
        branches.add(Branch("dev2"))
        `when`(teamCityService.listBranches(anyString())).thenReturn(Single.just(Branches(branches)))

        // Starting the activity
        activityRule.launchActivity(null)

        // Open build type
        onView(withText("build type")).perform(click())

        // Pressing filter builds toolbar item
        onView(withId(R.id.filter_builds)).perform(click())

        // Choose branch from autocomplete and verify it is appeared
        compose.onNodeWithTag("branches:input")
            .performTextInput("dev")
        compose.onNodeWithText("dev1").performClick()

        // Click on filter fab
        compose.onNodeWithTag("filter-builds:apply").performClick()

        // Check data was loaded with new filter
        verify(teamCityService, timeout(5_000)).listBuilds(
            anyString(),
            eq("state:any,canceled:any,failedToStart:any,branch:name:dev1,personal:false,pinned:false,count:10")
        )
    }

    @Test
    fun testPinnedSwitchIsGoneWhenQueuedFilterIsChosen() {
        activityRule.launchActivity(null)

        // Open build type
        onView(withText("build type")).perform(click())

        // Pressing filter builds toolbar item
        onView(withId(R.id.filter_builds)).perform(click())

        // Check switchers are shown
        compose.onNodeWithTag("filter-builds:pinned").assertIsDisplayed()
        compose.onNodeWithTag("filter-builds:personal").assertIsDisplayed()

        // Click on filter chooser
        compose.onNodeWithTag("filter-builds:chooser").performClick()

        // Filter by queued
        compose.onNodeWithText("Queued").performClick()

        // Check switchers
        compose.onNodeWithTag("filter-builds:pinned").assertDoesNotExist()
        compose.onNodeWithTag("filter-builds:pinned").assertDoesNotExist()
        compose.onNodeWithTag("filter-builds:personal").assertIsDisplayed()
    }

    @Test
    fun testUserCanFilterBuildsWithSuccessFilter() {
        activityRule.launchActivity(null)

        // Open build type
        onView(withText("build type")).perform(click())

        // Pressing filter builds toolbar item
        onView(withId(R.id.filter_builds)).perform(click())

        // Click on filter chooser
        compose.onNodeWithTag("filter-builds:chooser").performClick()

        // Filter by success
        compose.onNodeWithText("Success").performClick()

        // Click on filter fab
        compose.onNodeWithTag("filter-builds:apply").performClick()

        // Check data was loaded with new filter
        verify(teamCityService, timeout(5_000)).listBuilds(
            anyString(),
            eq("status:SUCCESS,branch:default:any,personal:false,pinned:false,count:10")
        )
    }

    @Test
    fun testUserCanFilterBuildsWithFailedFilter() {
        activityRule.launchActivity(null)

        // Open build type
        onView(withText("build type")).perform(click())

        // Pressing filter builds toolbar item
        onView(withId(R.id.filter_builds)).perform(click())

        // Click on filter chooser
        compose.onNodeWithTag("filter-builds:chooser").performClick()

        // Filter by success
        compose.onNodeWithText("Failed").performClick()

        // Click on filter fab
        compose.onNodeWithTag("filter-builds:apply").performClick()

        // Check data was loaded with new filter
        verify(teamCityService, timeout(5_000)).listBuilds(
            anyString(),
            eq("status:FAILURE,branch:default:any,personal:false,pinned:false,count:10")
        )
    }

    @Test
    fun testUserCanFilterBuildsWithFailedServerErrorFilter() {
        activityRule.launchActivity(null)

        // Open build type
        onView(withText("build type")).perform(click())

        // Pressing filter builds toolbar item
        onView(withId(R.id.filter_builds)).perform(click())

        // Click on filter chooser
        compose.onNodeWithTag("filter-builds:chooser").performClick()

        // Filter by success
        compose.onNodeWithText("Failed due server error").performClick()

        // Click on filter fab
        compose.onNodeWithTag("filter-builds:apply").performClick()

        // Check data was loaded with new filter
        verify(teamCityService, timeout(5_000)).listBuilds(
            anyString(),
            eq("status:ERROR,branch:default:any,personal:false,pinned:false,count:10")
        )
    }

    @Test
    fun testUserCanFilterBuildsWithCancelledFilter() {
        activityRule.launchActivity(null)

        // Open build type
        onView(withText("build type")).perform(click())

        // Pressing filter builds toolbar item
        onView(withId(R.id.filter_builds)).perform(click())

        // Click on filter chooser
        compose.onNodeWithTag("filter-builds:chooser").performClick()

        // Filter by success
        compose.onNodeWithText("Cancelled").performClick()

        // Click on filter fab
        compose.onNodeWithTag("filter-builds:apply").performClick()

        // Check data was loaded with new filter
        verify(teamCityService, timeout(5_000)).listBuilds(
            anyString(),
            eq("canceled:true,branch:default:any,personal:false,pinned:false,count:10")
        )
    }

    @Test
    fun testUserCanFilterBuildsWithFailedToStartFilter() {
        activityRule.launchActivity(null)

        // Open build type
        onView(withText("build type")).perform(click())

        // Pressing filter builds toolbar item
        onView(withId(R.id.filter_builds)).perform(click())

        // Click on filter chooser
        compose.onNodeWithTag("filter-builds:chooser").performClick()

        // Filter by success
        compose.onNodeWithText("Failed to start").performClick()

        // Click on filter fab
        compose.onNodeWithTag("filter-builds:apply").performClick()

        // Check data was loaded with new filter
        verify(teamCityService, timeout(5_000)).listBuilds(
            anyString(),
            eq("failedToStart:true,branch:default:any,personal:false,pinned:false,count:10")
        )
    }

    @Test
    fun testUserCanFilterBuildsWithRunningFilter() {
        activityRule.launchActivity(null)

        // Open build type
        onView(withText("build type")).perform(click())

        // Pressing filter builds toolbar item
        onView(withId(R.id.filter_builds)).perform(click())

        // Click on filter chooser
        compose.onNodeWithTag("filter-builds:chooser").performClick()

        // Filter by success
        compose.onNodeWithText("Running").performClick()

        // Click on filter fab
        compose.onNodeWithTag("filter-builds:apply").performClick()

        // Check data was loaded with new filter
        verify(teamCityService, timeout(5_000)).listBuilds(
            anyString(),
            eq("running:true,branch:default:any,personal:false,pinned:false")
        )
    }

    @Test
    fun testUserCanFilterBuildsWithQueuedFilter() {
        activityRule.launchActivity(null)

        // Open build type
        onView(withText("build type")).perform(click())

        // Pressing filter builds toolbar item
        onView(withId(R.id.filter_builds)).perform(click())

        // Click on filter chooser
        compose.onNodeWithTag("filter-builds:chooser").performClick()

        // Filter by success
        compose.onNodeWithText("Queued").performClick()

        // Click on filter fab
        compose.onNodeWithTag("filter-builds:apply").performClick()

        // Check data was loaded with new filter
        verify(teamCityService, timeout(5_000)).listBuilds(
            anyString(),
            eq("state:queued,branch:default:any,personal:false,pinned:any")
        )
    }
}
