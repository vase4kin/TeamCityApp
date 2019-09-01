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
import com.github.vase4kin.teamcityapp.TeamCityApplication
import com.github.vase4kin.teamcityapp.api.TeamCityService
import com.github.vase4kin.teamcityapp.buildlist.api.Builds
import com.github.vase4kin.teamcityapp.dagger.components.AppComponent
import com.github.vase4kin.teamcityapp.dagger.components.RestApiComponent
import com.github.vase4kin.teamcityapp.dagger.modules.AppModule
import com.github.vase4kin.teamcityapp.dagger.modules.FakeTeamCityServiceImpl
import com.github.vase4kin.teamcityapp.dagger.modules.Mocks
import com.github.vase4kin.teamcityapp.dagger.modules.RestApiModule
import com.github.vase4kin.teamcityapp.helper.CustomIntentsTestRule
import com.github.vase4kin.teamcityapp.helper.TestUtils
import com.github.vase4kin.teamcityapp.helper.eq
import com.github.vase4kin.teamcityapp.home.view.HomeActivity
import com.github.vase4kin.teamcityapp.runbuild.api.Branch
import com.github.vase4kin.teamcityapp.runbuild.api.Branches
import io.reactivex.Single
import it.cosenonjaviste.daggermock.DaggerMockRule
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.instanceOf
import org.hamcrest.Matchers.not
import org.hamcrest.core.AllOf.allOf
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Matchers.anyString
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.Spy
import java.util.ArrayList

/**
 * Tests for [FilterBuildsActivity]
 */
@RunWith(AndroidJUnit4::class)
class FilterBuildsActivityTest {

    @JvmField
    @Rule
    val daggerRule: DaggerMockRule<RestApiComponent> =
        DaggerMockRule(RestApiComponent::class.java, RestApiModule(Mocks.URL))
            .addComponentDependency(
                AppComponent::class.java,
                AppModule(InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TeamCityApplication)
            )
            .set { restApiComponent ->
                val app =
                    InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TeamCityApplication
                app.setRestApiInjector(restApiComponent)
            }

    @JvmField
    @Rule
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
            InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TeamCityApplication
        app.restApiInjector.sharedUserStorage().clearAll()
        app.restApiInjector.sharedUserStorage()
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
        onView(withText("build type"))
            .perform(click())

        // Pressing filter builds toolbar item
        onView(withId(R.id.filter_builds))
            .perform(click())

        // Click on filter fab
        onView(withId(R.id.fab_filter)).perform(click())

        // Check data was loaded with new filter
        verify(teamCityService).listBuilds(
            anyString(),
            eq("state:any,canceled:any,failedToStart:any,branch:default:any,personal:false,pinned:false,count:10")
        )
    }

    @Test
    fun testUserCanFilterBuildsByPinned() {
        activityRule.launchActivity(null)

        // Open build type
        onView(withText("build type"))
            .perform(click())

        // Pressing filter builds toolbar item
        onView(withId(R.id.filter_builds))
            .perform(click())

        // Click on pin switcher
        onView(withId(R.id.switcher_is_pinned)).perform(click())

        // Click on filter fab
        onView(withId(R.id.fab_filter)).perform(click())

        // Check data was loaded with new filter
        verify(teamCityService).listBuilds(
            anyString(),
            eq("state:any,canceled:any,failedToStart:any,branch:default:any,personal:false,pinned:true,count:10")
        )
    }

    @Test
    fun testUserCanFilterBuildsByPersonal() {
        activityRule.launchActivity(null)

        // Open build type
        onView(withText("build type"))
            .perform(click())

        // Pressing filter builds toolbar item
        onView(withId(R.id.filter_builds))
            .perform(click())

        // Click on pin switcher
        onView(withId(R.id.switcher_is_personal)).perform(click())

        // Click on filter fab
        onView(withId(R.id.fab_filter)).perform(click())

        // Check data was loaded with new filter
        verify<TeamCityService>(teamCityService).listBuilds(
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
        onView(withText("build type"))
            .perform(click())

        // Pressing filter builds toolbar item
        onView(withId(R.id.filter_builds))
            .perform(click())

        // Choose branch from autocomplete and verify it is appeared
        onView(withId(R.id.autocomplete_branches))
            .perform(typeText("dev"))
        onData(allOf(`is`(instanceOf<Any>(String::class.java)), `is`<String>("dev1")))
            .inRoot(RootMatchers.withDecorView(not(`is`<View>(activityRule.activity.window.decorView))))
            .perform(click())
        onView(withText("dev1")).perform(click(), closeSoftKeyboard())

        // Click on filter fab
        onView(withId(R.id.fab_filter)).perform(click())

        // Check data was loaded with new filter
        verify(teamCityService).listBuilds(
            anyString(),
            eq("state:any,canceled:any,failedToStart:any,branch:name:dev1,personal:false,pinned:false,count:10")
        )
    }

    @Test
    fun testPinnedSwitchIsGoneWhenQueuedFilterIsChosen() {
        activityRule.launchActivity(null)

        // Open build type
        onView(withText("build type"))
            .perform(click())

        // Pressing filter builds toolbar item
        onView(withId(R.id.filter_builds))
            .perform(click())

        // Check switchers are shown
        onView(withId(R.id.switcher_is_pinned)).check(matches(isDisplayed()))
        onView(withId(R.id.switcher_is_personal)).check(matches(isDisplayed()))

        // Click on filter chooser
        onView(withId(R.id.chooser_filter)).perform(click())

        // Filter by queued
        onView(withText("Queued")).perform(click())

        // Check switchers
        onView(withId(R.id.switcher_is_pinned)).check(matches(not(isDisplayed())))
        onView(withId(R.id.divider_switcher_is_pinned)).check(matches(not(isDisplayed())))
        onView(withId(R.id.switcher_is_personal)).check(matches(isDisplayed()))
    }

    @Test
    fun testUserCanFilterBuildsWithSuccessFilter() {
        activityRule.launchActivity(null)

        // Open build type
        onView(withText("build type"))
            .perform(click())

        // Pressing filter builds toolbar item
        onView(withId(R.id.filter_builds))
            .perform(click())

        // Click on filter chooser
        onView(withId(R.id.chooser_filter)).perform(click())

        // Filter by success
        onView(withText("Success")).perform(click())

        // Click on filter fab
        onView(withId(R.id.fab_filter)).perform(click())

        // Check data was loaded with new filter
        verify(teamCityService).listBuilds(
            anyString(),
            eq("status:SUCCESS,branch:default:any,personal:false,pinned:false,count:10")
        )
    }

    @Test
    fun testUserCanFilterBuildsWithFailedFilter() {
        activityRule.launchActivity(null)

        // Open build type
        onView(withText("build type"))
            .perform(click())

        // Pressing filter builds toolbar item
        onView(withId(R.id.filter_builds))
            .perform(click())

        // Click on filter chooser
        onView(withId(R.id.chooser_filter)).perform(click())

        // Filter by success
        onView(withText("Failed")).perform(click())

        // Click on filter fab
        onView(withId(R.id.fab_filter)).perform(click())

        // Check data was loaded with new filter
        verify(teamCityService).listBuilds(
            anyString(),
            eq("status:FAILURE,branch:default:any,personal:false,pinned:false,count:10")
        )
    }

    @Test
    fun testUserCanFilterBuildsWithFailedServerErrorFilter() {
        activityRule.launchActivity(null)

        // Open build type
        onView(withText("build type"))
            .perform(click())

        // Pressing filter builds toolbar item
        onView(withId(R.id.filter_builds))
            .perform(click())

        // Click on filter chooser
        onView(withId(R.id.chooser_filter)).perform(click())

        // Filter by success
        onView(withText("Failed due server error")).perform(click())

        // Click on filter fab
        onView(withId(R.id.fab_filter)).perform(click())

        // Check data was loaded with new filter
        verify(teamCityService).listBuilds(
            anyString(),
            eq("status:ERROR,branch:default:any,personal:false,pinned:false,count:10")
        )
    }

    @Test
    fun testUserCanFilterBuildsWithCancelledFilter() {
        activityRule.launchActivity(null)

        // Open build type
        onView(withText("build type"))
            .perform(click())

        // Pressing filter builds toolbar item
        onView(withId(R.id.filter_builds))
            .perform(click())

        // Click on filter chooser
        onView(withId(R.id.chooser_filter)).perform(click())

        // Filter by success
        onView(withText("Cancelled")).perform(click())

        // Click on filter fab
        onView(withId(R.id.fab_filter)).perform(click())

        // Check data was loaded with new filter
        verify(teamCityService).listBuilds(
            anyString(),
            eq("canceled:true,branch:default:any,personal:false,pinned:false,count:10")
        )
    }

    @Test
    fun testUserCanFilterBuildsWithFailedToStartFilter() {
        activityRule.launchActivity(null)

        // Open build type
        onView(withText("build type"))
            .perform(click())

        // Pressing filter builds toolbar item
        onView(withId(R.id.filter_builds))
            .perform(click())

        // Click on filter chooser
        onView(withId(R.id.chooser_filter)).perform(click())

        // Filter by success
        onView(withText("Failed to start")).perform(click())

        // Click on filter fab
        onView(withId(R.id.fab_filter)).perform(click())

        // Check data was loaded with new filter
        verify(teamCityService).listBuilds(
            anyString(),
            eq("failedToStart:true,branch:default:any,personal:false,pinned:false,count:10")
        )
    }

    @Test
    fun testUserCanFilterBuildsWithRunningFilter() {
        activityRule.launchActivity(null)

        // Open build type
        onView(withText("build type"))
            .perform(click())

        // Pressing filter builds toolbar item
        onView(withId(R.id.filter_builds))
            .perform(click())

        // Click on filter chooser
        onView(withId(R.id.chooser_filter)).perform(click())

        // Filter by success
        onView(withText("Running")).perform(click())

        // Click on filter fab
        onView(withId(R.id.fab_filter)).perform(click())

        // Check data was loaded with new filter
        verify(teamCityService).listBuilds(
            anyString(),
            eq("running:true,branch:default:any,personal:false,pinned:false")
        )
    }

    @Test
    fun testUserCanFilterBuildsWithQueuedFilter() {
        activityRule.launchActivity(null)

        // Open build type
        onView(withText("build type"))
            .perform(click())

        // Pressing filter builds toolbar item
        onView(withId(R.id.filter_builds))
            .perform(click())

        // Click on filter chooser
        onView(withId(R.id.chooser_filter)).perform(click())

        // Filter by success
        onView(withText("Queued")).perform(click())

        // Click on filter fab
        onView(withId(R.id.fab_filter)).perform(click())

        // Check data was loaded with new filter
        verify(teamCityService).listBuilds(
            anyString(),
            eq("state:queued,branch:default:any,personal:false,pinned:any")
        )
    }
}
