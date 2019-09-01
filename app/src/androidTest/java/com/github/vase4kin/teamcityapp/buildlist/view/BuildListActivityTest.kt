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

package com.github.vase4kin.teamcityapp.buildlist.view

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents.assertNoUnverifiedIntents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.BundleMatchers.hasEntry
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtras
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.TeamCityApplication
import com.github.vase4kin.teamcityapp.api.TeamCityService
import com.github.vase4kin.teamcityapp.base.extractor.BundleExtractorValues
import com.github.vase4kin.teamcityapp.build_details.view.BuildDetailsActivity
import com.github.vase4kin.teamcityapp.buildlist.api.Builds
import com.github.vase4kin.teamcityapp.buildlist.filter.BuildListFilterImpl
import com.github.vase4kin.teamcityapp.dagger.components.AppComponent
import com.github.vase4kin.teamcityapp.dagger.components.RestApiComponent
import com.github.vase4kin.teamcityapp.dagger.modules.AppModule
import com.github.vase4kin.teamcityapp.dagger.modules.FakeTeamCityServiceImpl
import com.github.vase4kin.teamcityapp.dagger.modules.Mocks
import com.github.vase4kin.teamcityapp.dagger.modules.RestApiModule
import com.github.vase4kin.teamcityapp.filter_builds.router.FilterBuildsRouter
import com.github.vase4kin.teamcityapp.filter_builds.view.FilterBuildsActivity
import com.github.vase4kin.teamcityapp.filter_builds.view.FilterBuildsView
import com.github.vase4kin.teamcityapp.helper.CustomIntentsTestRule
import com.github.vase4kin.teamcityapp.helper.RecyclerViewMatcher.Companion.withRecyclerView
import com.github.vase4kin.teamcityapp.helper.TestUtils
import com.github.vase4kin.teamcityapp.helper.TestUtils.Companion.hasItemsCount
import com.github.vase4kin.teamcityapp.helper.TestUtils.Companion.matchToolbarTitle
import com.github.vase4kin.teamcityapp.home.view.HomeActivity
import com.github.vase4kin.teamcityapp.overview.view.eq
import com.github.vase4kin.teamcityapp.runbuild.interactor.EXTRA_BUILD_TYPE_ID
import com.github.vase4kin.teamcityapp.runbuild.router.RunBuildRouter
import com.github.vase4kin.teamcityapp.runbuild.view.RunBuildActivity
import io.reactivex.Single
import it.cosenonjaviste.daggermock.DaggerMockRule
import org.hamcrest.CoreMatchers.equalTo
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

/**
 * Tests for [BuildListActivity]
 */
@RunWith(AndroidJUnit4::class)
class BuildListActivityTest {

    @Rule
    @JvmField
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

    @Rule
    @JvmField
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
    }

    @Test
    fun testUserCanSeeToolbarText() {
        activityRule.launchActivity(null)

        // Open build type
        onView(withText("build type"))
            .perform(click())

        // Checking toolbar title
        matchToolbarTitle("build type")
    }

    @Test
    fun testUserCanSeeSuccessFullyLoadedBuilds() {
        activityRule.launchActivity(null)

        // Open build type
        onView(withText("build type"))
            .perform(click())

        // List has item with header
        onView(withId(R.id.build_recycler_view)).check(hasItemsCount(5))
        // Checking header 1
        onView(withRecyclerView(R.id.build_recycler_view).atPositionOnView(0, R.id.section_text))
            .check(matches(withText("22 June")))
        // Checking adapter item 1
        onView(withRecyclerView(R.id.build_recycler_view).atPositionOnView(1, R.id.itemTitle))
            .check(matches(withText("Running tests")))
        onView(withRecyclerView(R.id.build_recycler_view).atPositionOnView(1, R.id.itemSubTitle))
            .check(matches(withText("refs/heads/master")))
        onView(withRecyclerView(R.id.build_recycler_view).atPositionOnView(1, R.id.buildNumber))
            .check(matches(withText("#2458")))
        // Checking header 2
        onView(withRecyclerView(R.id.build_recycler_view).atPositionOnView(2, R.id.section_text))
            .check(matches(withText("21 June")))
        // Checking adapter item 2
        onView(withRecyclerView(R.id.build_recycler_view).atPositionOnView(3, R.id.itemTitle))
            .check(matches(withText("Success")))
        onView(withRecyclerView(R.id.build_recycler_view).atPositionOnView(3, R.id.itemSubTitle))
            .check(matches(withText("refs/heads/master")))
        onView(withRecyclerView(R.id.build_recycler_view).atPositionOnView(3, R.id.buildNumber))
            .check(matches(withText("#2459")))
        // Checking adapter item 3
        onView(withRecyclerView(R.id.build_recycler_view).atPositionOnView(4, R.id.itemTitle))
            .check(matches(withText("Error with smth")))
        onView(withRecyclerView(R.id.build_recycler_view).atPositionOnView(4, R.id.buildNumber))
            .check(matches(withText("#2460")))
    }

    @Test
    fun testUserCanSeeFailureMessageIfSmthBadHappensInBuildListLoading() {
        `when`(teamCityService.listBuilds(anyString(), anyString())).thenReturn(
            Single.error(
                RuntimeException("smth bad happend!")
            )
        )

        activityRule.launchActivity(null)

        // Open build type
        onView(withText("build type"))
            .perform(click())

        // Check error message
        onView(withText(R.string.error_view_error_text)).check(matches(isDisplayed()))
    }

    @Test
    fun testUserCanSeeLoadEmptyListMessageIfBuildListIsEmpty() {
        `when`(
            teamCityService.listBuilds(
                anyString(),
                anyString()
            )
        ).thenReturn(Single.just(Builds(0, emptyList())))

        activityRule.launchActivity(null)

        // Open build type
        onView(withText("build type"))
            .perform(click())

        // Check the list is empty
        onView(withId(R.id.empty_title)).check(matches(isDisplayed()))
            .check(matches(withText(R.string.empty_list_message_builds)))
    }

    @Test
    fun testUserCanOpenRunBuild() {
        `when`(
            teamCityService.listBuilds(
                anyString(),
                anyString()
            )
        ).thenReturn(Single.just(Builds(0, emptyList())))

        activityRule.launchActivity(null)

        // Open build type
        onView(withText("build type"))
            .perform(click())

        // Pressing run build fab
        onView(withId(R.id.floating_action_button))
            .perform(click())

        // Check run build activity is opened
        intended(
            allOf(
                hasComponent(RunBuildActivity::class.java.name),
                hasExtras(hasEntry(equalTo(EXTRA_BUILD_TYPE_ID), equalTo("build_type_id")))
            )
        )
    }

    @Test
    fun testUserCanSeeSnackBarIfBuildIsAddedToQueue() {
        `when`(
            teamCityService.listBuilds(
                anyString(),
                anyString()
            )
        ).thenReturn(Single.just(Builds(0, emptyList())))

        // Preparing stubbing intent
        val resultData = Intent()
        resultData.putExtra(RunBuildRouter.EXTRA_HREF, "href")
        val result = Instrumentation.ActivityResult(Activity.RESULT_OK, resultData)

        activityRule.launchActivity(null)

        // Open build type
        onView(withText("build type"))
            .perform(click())

        // Set up result stubbing
        intending(hasComponent(RunBuildActivity::class.java.name)).respondWith(result)

        // Pressing run build fab
        onView(withId(R.id.floating_action_button))
            .perform(click())

        // Check snack bar text
        onView(withText(R.string.text_build_is_run)).check(matches(isDisplayed()))
    }

    @Test
    fun testUserCanSeeBuildListIsRefreshedIfBuildIsAddedToQueue() {
        `when`(teamCityService.listBuilds(anyString(), anyString()))
            .thenReturn(Single.just(Builds(0, emptyList())))
            .thenCallRealMethod()

        // Preparing stubbing intent
        val resultData = Intent()
        resultData.putExtra(RunBuildRouter.EXTRA_HREF, "href")
        val result = Instrumentation.ActivityResult(Activity.RESULT_OK, resultData)

        activityRule.launchActivity(null)

        // Open build type
        onView(withText("build type"))
            .perform(click())

        // Check the list is empty
        onView(withId(R.id.empty_title)).check(matches(isDisplayed()))
            .check(matches(withText(R.string.empty_list_message_builds)))

        // Set up result stubbing
        intending(hasComponent(RunBuildActivity::class.java.name)).respondWith(result)

        // Pressing run build fab
        onView(withId(R.id.floating_action_button))
            .perform(click())

        // Check new items appeared
        onView(withRecyclerView(R.id.build_recycler_view).atPositionOnView(1, R.id.itemTitle))
            .check(matches(withText("Running tests")))
    }

    @Test
    fun testUserCanOpenRecentlyQueuedBuildFromSnackBarIfNoErrors() {
        `when`(
            teamCityService.listBuilds(
                anyString(),
                anyString()
            )
        ).thenReturn(Single.just(Builds(0, emptyList())))

        // Preparing stubbing intent
        val resultData = Intent()
        resultData.putExtra(RunBuildRouter.EXTRA_HREF, "href")
        val result = Instrumentation.ActivityResult(Activity.RESULT_OK, resultData)

        activityRule.launchActivity(null)

        // Open build type
        onView(withText("build type"))
            .perform(click())

        // Set up result stubbing
        intending(hasComponent(RunBuildActivity::class.java.name)).respondWith(result)

        // Pressing run build fab
        onView(withId(R.id.floating_action_button))
            .perform(click())

        // Mock build call
        val build = Mocks.runningBuild()
        `when`(teamCityService.build(anyString())).thenReturn(Single.just(build))

        // Click on show button of queued build snack bar
        onView(withText(R.string.text_show_build)).perform(click())

        // Check build is opened
        intended(
            allOf(
                hasComponent(BuildDetailsActivity::class.java.name),
                hasExtras(hasEntry(equalTo(BundleExtractorValues.BUILD), equalTo(build)))
            )
        )
    }

    @Test
    fun testUserCanSeeErrorSnackbarWhenOpenRecentlyQueuedBuildIfThereAreErrors() {
        `when`(
            teamCityService.listBuilds(
                anyString(),
                anyString()
            )
        ).thenReturn(Single.just(Builds(0, emptyList())))

        // Preparing stubbing intent
        val resultData = Intent()
        resultData.putExtra(RunBuildRouter.EXTRA_HREF, "href")
        val result = Instrumentation.ActivityResult(Activity.RESULT_OK, resultData)

        activityRule.launchActivity(null)

        // Open build type
        onView(withText("build type"))
            .perform(click())

        // Set up result stubbing
        intending(hasComponent(RunBuildActivity::class.java.name)).respondWith(result)

        // Pressing run build fab
        onView(withId(R.id.floating_action_button))
            .perform(click())

        // Mock build call
        `when`(teamCityService.build(anyString())).thenReturn(Single.error(RuntimeException()))

        // Click on show button of queued build snack bar
        onView(withText(R.string.text_show_build)).perform(click())

        // Check error snack bar
        onView(withText(R.string.error_opening_build)).check(matches(isDisplayed()))

        // Click on retry button
        onView(withText(R.string.download_artifact_retry_snack_bar_retry_button)).perform(click())

        // Check error snack bar again
        onView(withText(R.string.error_opening_build)).check(matches(isDisplayed()))
    }

    @Test
    fun testUserCanOpenFilterBuilds() {
        `when`(
            teamCityService.listBuilds(
                anyString(),
                anyString()
            )
        ).thenReturn(Single.just(Builds(0, emptyList())))

        activityRule.launchActivity(null)

        // Open build type
        onView(withText("build type"))
            .perform(click())

        // Pressing filter builds toolbar item
        onView(withId(R.id.filter_builds))
            .perform(click())

        // Check filter builds activity is opened
        intended(
            allOf(
                hasComponent(FilterBuildsActivity::class.java.name),
                hasExtras(hasEntry(equalTo(EXTRA_BUILD_TYPE_ID), equalTo("build_type_id")))
            )
        )
    }

    @Test
    fun testUserCanSeeSnackBarIfBuildFiltersHaveBeenApplied() {
        `when`(
            teamCityService.listBuilds(
                anyString(),
                anyString()
            )
        ).thenReturn(Single.just(Builds(0, emptyList())))

        // Preparing stubbing intent
        val resultData = Intent()
        val filter = BuildListFilterImpl()
        filter.setFilter(FilterBuildsView.FILTER_CANCELLED)
        filter.setBranch("branch")
        filter.setPersonal(true)
        filter.setPinned(true)
        resultData.putExtra(FilterBuildsRouter.EXTRA_FILTER, filter)
        val result = Instrumentation.ActivityResult(Activity.RESULT_OK, resultData)

        activityRule.launchActivity(null)

        // Open build type
        onView(withText("build type"))
            .perform(click())

        // Set up result stubbing
        intending(hasComponent(FilterBuildsActivity::class.java.name)).respondWith(result)

        // Pressing filter builds toolbar item
        onView(withId(R.id.filter_builds))
            .perform(click())

        // Check snack bar text
        onView(withText(R.string.text_filters_applied)).check(matches(isDisplayed()))

        // Check data was loaded with new filter
        verify(teamCityService).listBuilds(
            eq("build_type_id"),
            eq("canceled:true,branch:name:branch,personal:true,pinned:true,count:10")
        )
    }

    @Test
    fun testUserCannotClickOnSection() {
        activityRule.launchActivity(null)

        // Open build type
        onView(withText("build type"))
            .perform(click())

        intended(
            allOf(
                hasComponent(BuildListActivity::class.java.name),
                hasExtras(
                    allOf(
                        hasEntry(
                            equalTo(BundleExtractorValues.BUILD_LIST_FILTER),
                            equalTo<Any>(null)
                        ),
                        hasEntry(equalTo(BundleExtractorValues.ID), equalTo("build_type_id")),
                        hasEntry(equalTo(BundleExtractorValues.NAME), equalTo("build type"))
                    )
                )
            )
        )

        // Clicking on header
        onView(withRecyclerView(R.id.build_recycler_view).atPositionOnView(0, R.id.section_text))
            .check(matches(withText("22 June")))
            .perform(click())

        assertNoUnverifiedIntents()
    }
}
