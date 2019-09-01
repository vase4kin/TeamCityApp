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

package com.github.vase4kin.teamcityapp.overview.view

import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openContextualActionModeOverflowMenu
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.swipeUp
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.BundleMatchers.hasEntry
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtras
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.azimolabs.conditionwatcher.ConditionWatcher
import com.azimolabs.conditionwatcher.Instruction
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.TeamCityApplication
import com.github.vase4kin.teamcityapp.api.TeamCityService
import com.github.vase4kin.teamcityapp.base.extractor.BundleExtractorValues
import com.github.vase4kin.teamcityapp.build_details.view.BuildDetailsActivity
import com.github.vase4kin.teamcityapp.buildlist.api.Build
import com.github.vase4kin.teamcityapp.buildlist.api.CanceledInfo
import com.github.vase4kin.teamcityapp.buildlist.api.Triggered
import com.github.vase4kin.teamcityapp.buildlist.api.Triggered.TRIGGER_TYPE_BUILD_TYPE
import com.github.vase4kin.teamcityapp.buildlist.api.Triggered.TRIGGER_TYPE_RESTARTED
import com.github.vase4kin.teamcityapp.buildlist.api.Triggered.TRIGGER_TYPE_USER
import com.github.vase4kin.teamcityapp.buildlist.api.User
import com.github.vase4kin.teamcityapp.buildlist.view.BuildListActivity
import com.github.vase4kin.teamcityapp.dagger.components.AppComponent
import com.github.vase4kin.teamcityapp.dagger.components.RestApiComponent
import com.github.vase4kin.teamcityapp.dagger.modules.AppModule
import com.github.vase4kin.teamcityapp.dagger.modules.FakeTeamCityServiceImpl
import com.github.vase4kin.teamcityapp.dagger.modules.Mocks
import com.github.vase4kin.teamcityapp.dagger.modules.RestApiModule
import com.github.vase4kin.teamcityapp.helper.CustomIntentsTestRule
import com.github.vase4kin.teamcityapp.helper.RecyclerViewMatcher.Companion.withRecyclerView
import com.github.vase4kin.teamcityapp.helper.TestUtils
import com.github.vase4kin.teamcityapp.helper.TestUtils.hasItemsCount
import com.github.vase4kin.teamcityapp.helper.TestUtils.matchToolbarSubTitle
import com.github.vase4kin.teamcityapp.helper.TestUtils.matchToolbarTitle
import com.github.vase4kin.teamcityapp.navigation.view.NavigationActivity
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage
import io.reactivex.Single
import it.cosenonjaviste.daggermock.DaggerMockRule
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.core.AllOf.allOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Matchers.anyString
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.Spy

/**
 * Tests for [OverviewFragment]
 */
@RunWith(AndroidJUnit4::class)
class OverviewFragmentTest {

    @Rule
    @JvmField
    val daggerMockRule = DaggerMockRule(RestApiComponent::class.java, RestApiModule(Mocks.URL))
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
    val activityRule = CustomIntentsTestRule(BuildDetailsActivity::class.java)

    @Spy
    val teamCityService: TeamCityService = FakeTeamCityServiceImpl()

    @Spy
    val build: Build = Mocks.successBuild()

    private val storage: SharedUserStorage
        get() {
            val app =
                InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TeamCityApplication
            return app.appInjector.sharedUserStorage()
        }

    companion object {

        private const val CANCELED_TIME_STAMP = "20161223T151154+0300"
        private const val USER_NAME = "john.117"
        private const val NAME = "John one one seven"
        private const val BUILD_TYPE_NAME = "name"
    }

    @Before
    fun disableOnboarding() {
        TestUtils.disableOnboarding()
        val storage = storage
        storage.clearAll()
        storage.saveGuestUserAccountAndSetItAsActive(Mocks.URL, false)
    }

    @Test
    fun testUserCanSeeSuccessBuildDetails() {
        // Prepare mocks
        `when`(teamCityService.build(anyString())).thenReturn(Single.just(build))

        // Prepare intent
        // <! ---------------------------------------------------------------------- !>
        // Passing build object to activity, had to create it for real, Can't pass mock object as serializable in bundle :(
        // <! ---------------------------------------------------------------------- !>
        val intent = Intent()
        val b = Bundle()
        b.putSerializable(BundleExtractorValues.BUILD, Mocks.successBuild())
        b.putString(BundleExtractorValues.NAME, BUILD_TYPE_NAME)
        intent.putExtras(b)

        // Start activity
        activityRule.launchActivity(intent)

        // Checking toolbar title
        matchToolbarTitle("#2459")
        matchToolbarSubTitle("build type name")
        // List has item with header
        onView(withId(R.id.overview_recycler_view)).check(hasItemsCount(7))
        // Checking Result
        onView(withRecyclerView(R.id.overview_recycler_view).atPositionOnView(0, R.id.itemHeader)).check(
            matches(
                withText("Result")
            )
        )
        onView(
            withRecyclerView(R.id.overview_recycler_view).atPositionOnView(
                0,
                R.id.itemTitle
            )
        ).check(matches(withText("Success")))
        // Checking Time
        onView(withRecyclerView(R.id.overview_recycler_view).atPositionOnView(1, R.id.itemHeader)).check(
            matches(
                withText("Time")
            )
        )
        onView(
            withRecyclerView(R.id.overview_recycler_view).atPositionOnView(
                1,
                R.id.itemTitle
            )
        ).check(matches(withText("21 Jun 16 23:00 - 23:30 (30m:)")))
        // Checking Branch
        onView(withRecyclerView(R.id.overview_recycler_view).atPositionOnView(2, R.id.itemHeader)).check(
            matches(
                withText("Branch")
            )
        )
        onView(
            withRecyclerView(R.id.overview_recycler_view).atPositionOnView(
                2,
                R.id.itemTitle
            )
        ).check(matches(withText("refs/heads/master")))
        // Checking Agent
        onView(withRecyclerView(R.id.overview_recycler_view).atPositionOnView(3, R.id.itemHeader)).check(
            matches(
                withText("Agent")
            )
        )
        onView(
            withRecyclerView(R.id.overview_recycler_view).atPositionOnView(
                3,
                R.id.itemTitle
            )
        ).check(matches(withText("agent-love")))

        // Scrolling to last item to make it visible
        onView(withId(R.id.overview_recycler_view)).perform(scrollToPosition<RecyclerView.ViewHolder>(4))

        // Checking Triggered by
        onView(withRecyclerView(R.id.overview_recycler_view).atPositionOnView(4, R.id.itemHeader)).check(
            matches(
                withText("Triggered by")
            )
        )
        onView(
            withRecyclerView(R.id.overview_recycler_view).atPositionOnView(
                4,
                R.id.itemTitle
            )
        ).check(matches(withText("code-lover")))
    }

    @Test
    fun testUserCanSeeFailureMessageIfSmthBadHappens() {
        // Prepare mocks
        `when`(teamCityService.build(anyString())).thenReturn(Single.error(RuntimeException("Fake error happened!")))

        // Prepare intent
        // <! ---------------------------------------------------------------------- !>
        // Passing build object to activity, had to create it for real, Can't pass mock object as serializable in bundle :(
        // <! ---------------------------------------------------------------------- !>
        val intent = Intent()
        val b = Bundle()
        b.putSerializable(BundleExtractorValues.BUILD, Mocks.successBuild())
        b.putString(BundleExtractorValues.NAME, BUILD_TYPE_NAME)
        intent.putExtras(b)

        // Start activity
        activityRule.launchActivity(intent)

        // Checking error
        onView(withText(R.string.error_view_error_text)).check(matches(isDisplayed()))
    }

    @Test
    @Throws(Exception::class)
    fun testUserCanCopyElementValueFromTheList() {
        // Prepare mocks
        `when`(teamCityService.build(anyString())).thenReturn(Single.just(build))

        // Prepare intent
        // <! ---------------------------------------------------------------------- !>
        // Passing build object to activity, had to create it for real, Can't pass mock object as serializable in bundle :(
        // <! ---------------------------------------------------------------------- !>
        val intent = Intent()
        val b = Bundle()
        b.putSerializable(BundleExtractorValues.BUILD, Mocks.successBuild())
        b.putString(BundleExtractorValues.NAME, BUILD_TYPE_NAME)
        intent.putExtras(b)

        // Start activity
        activityRule.launchActivity(intent)

        // Click on Result view
        onView(withRecyclerView(R.id.overview_recycler_view).atPosition(0)).perform(click())

        // Clicking on copy
        onView(withText(R.string.build_element_copy)).perform(click())

        // Checking toast message
        onView(withText(R.string.build_element_copy_text))
            .check(matches(isDisplayed()))
    }

    @Test
    @Throws(Exception::class)
    fun testUserCanSeeCanceledInfoAsUserRealName() {
        // Prepare mocks
        `when`(teamCityService.build(anyString())).thenReturn(Single.just(build))
        build.canceledInfo = CanceledInfo(CANCELED_TIME_STAMP, User("user.name", "User name"))

        // Prepare intent
        // <! ---------------------------------------------------------------------- !>
        // Passing build object to activity, had to create it for real, Can't pass mock object as serializable in bundle :(
        // <! ---------------------------------------------------------------------- !>
        val intent = Intent()
        val b = Bundle()
        b.putSerializable(BundleExtractorValues.BUILD, Mocks.successBuild())
        b.putString(BundleExtractorValues.NAME, BUILD_TYPE_NAME)
        intent.putExtras(b)

        // Start activity
        activityRule.launchActivity(intent)

        // Checking Canceled by
        onView(withRecyclerView(R.id.overview_recycler_view).atPositionOnView(1, R.id.itemHeader)).check(
            matches(
                withText(R.string.build_canceled_by_text)
            )
        )
        onView(
            withRecyclerView(R.id.overview_recycler_view).atPositionOnView(
                1,
                R.id.itemTitle
            )
        ).check(matches(withText("User name")))
    }

    @Test
    @Throws(Exception::class)
    fun testUserCanSeeCanceledInfoAsUserName() {
        // Prepare mocks
        `when`(teamCityService.build(anyString())).thenReturn(Single.just(build))
        build.canceledInfo = CanceledInfo(CANCELED_TIME_STAMP, User("user.name", null))

        // Prepare intent
        // <! ---------------------------------------------------------------------- !>
        // Passing build object to activity, had to create it for real, Can't pass mock object as serializable in bundle :(
        // <! ---------------------------------------------------------------------- !>
        val intent = Intent()
        val b = Bundle()
        b.putSerializable(BundleExtractorValues.BUILD, Mocks.successBuild())
        b.putString(BundleExtractorValues.NAME, BUILD_TYPE_NAME)
        intent.putExtras(b)

        // Start activity
        activityRule.launchActivity(intent)

        // Checking Canceled by
        onView(withRecyclerView(R.id.overview_recycler_view).atPositionOnView(1, R.id.itemHeader)).check(
            matches(
                withText(R.string.build_canceled_by_text)
            )
        )
        onView(
            withRecyclerView(R.id.overview_recycler_view).atPositionOnView(
                1,
                R.id.itemTitle
            )
        ).check(matches(withText("user.name")))
    }

    @Test
    @Throws(Exception::class)
    fun testUserCanSeeCanceledInfoAsTimeStampOfCancellation() {
        // Prepare mocks
        `when`(teamCityService.build(anyString())).thenReturn(Single.just(build))
        build.canceledInfo = CanceledInfo(CANCELED_TIME_STAMP, User("user.name", null))

        // Prepare intent
        // <! ---------------------------------------------------------------------- !>
        // Passing build object to activity, had to create it for real, Can't pass mock object as serializable in bundle :(
        // <! ---------------------------------------------------------------------- !>
        val intent = Intent()
        val b = Bundle()
        b.putSerializable(BundleExtractorValues.BUILD, Mocks.successBuild())
        b.putString(BundleExtractorValues.NAME, BUILD_TYPE_NAME)
        intent.putExtras(b)

        // Start activity
        activityRule.launchActivity(intent)

        // Checking time stamp
        onView(withRecyclerView(R.id.overview_recycler_view).atPositionOnView(2, R.id.itemHeader)).check(
            matches(
                withText(R.string.build_cancellation_time_text)
            )
        )
        onView(
            withRecyclerView(R.id.overview_recycler_view).atPositionOnView(
                2,
                R.id.itemTitle
            )
        ).check(matches(withText("23 Dec 16 15:11")))
    }

    @Test
    fun testUserCanSeeUserWhatUserTriggeredBuildIfUserHasName() {
        // Prepare mocks
        `when`(teamCityService.build(anyString())).thenReturn(Single.just(build))
        val triggered = Triggered(TRIGGER_TYPE_USER, null, User(USER_NAME, NAME))
        build.triggered = triggered

        // Prepare intent
        // <! ---------------------------------------------------------------------- !>
        // Passing build object to activity, had to create it for real, Can't pass mock object as serializable in bundle :(
        // <! ---------------------------------------------------------------------- !>
        val intent = Intent()
        val b = Bundle()
        b.putSerializable(BundleExtractorValues.BUILD, Mocks.successBuild())
        b.putString(BundleExtractorValues.NAME, BUILD_TYPE_NAME)
        intent.putExtras(b)

        // Start activity
        activityRule.launchActivity(intent)

        // Scrolling to last item to make it visible
        onView(withId(R.id.overview_recycler_view)).perform(scrollToPosition<RecyclerView.ViewHolder>(4))

        // Checking restarted by
        onView(withRecyclerView(R.id.overview_recycler_view).atPositionOnView(4, R.id.itemHeader)).check(
            matches(
                withText(R.string.build_triggered_by_section_text)
            )
        )
        onView(
            withRecyclerView(R.id.overview_recycler_view).atPositionOnView(
                4,
                R.id.itemTitle
            )
        ).check(matches(withText(NAME)))
    }

    @Test
    fun testUserCanSeeUserWhatUserTriggeredBuildIfUserHasOnLyName() {
        // Prepare mocks
        `when`(teamCityService.build(anyString())).thenReturn(Single.just(build))
        val triggered = Triggered(TRIGGER_TYPE_USER, null, User(USER_NAME, null))
        build.triggered = triggered

        // Prepare intent
        // <! ---------------------------------------------------------------------- !>
        // Passing build object to activity, had to create it for real, Can't pass mock object as serializable in bundle :(
        // <! ---------------------------------------------------------------------- !>
        val intent = Intent()
        val b = Bundle()
        b.putSerializable(BundleExtractorValues.BUILD, Mocks.successBuild())
        b.putString(BundleExtractorValues.NAME, BUILD_TYPE_NAME)
        intent.putExtras(b)

        // Start activity
        activityRule.launchActivity(intent)

        // Scrolling to last item to make it visible
        onView(withId(R.id.overview_recycler_view)).perform(scrollToPosition<RecyclerView.ViewHolder>(4))

        // Checking restarted by
        onView(withRecyclerView(R.id.overview_recycler_view).atPositionOnView(4, R.id.itemHeader)).check(
            matches(
                withText(R.string.build_triggered_by_section_text)
            )
        )
        onView(
            withRecyclerView(R.id.overview_recycler_view).atPositionOnView(
                4,
                R.id.itemTitle
            )
        ).check(matches(withText(USER_NAME)))
    }

    @Test
    fun testUserCanSeeWhatUserRestartedBuildIfUserHasName() {
        // Prepare mocks
        `when`(teamCityService.build(anyString())).thenReturn(Single.just(build))
        val triggered = Triggered(TRIGGER_TYPE_RESTARTED, null, User(USER_NAME, NAME))
        build.triggered = triggered

        // Prepare intent
        // <! ---------------------------------------------------------------------- !>
        // Passing build object to activity, had to create it for real, Can't pass mock object as serializable in bundle :(
        // <! ---------------------------------------------------------------------- !>
        val intent = Intent()
        val b = Bundle()
        b.putSerializable(BundleExtractorValues.BUILD, Mocks.successBuild())
        b.putString(BundleExtractorValues.NAME, BUILD_TYPE_NAME)
        intent.putExtras(b)

        // Start activity
        activityRule.launchActivity(intent)

        // Scrolling to last item to make it visible
        onView(withId(R.id.overview_recycler_view)).perform(scrollToPosition<RecyclerView.ViewHolder>(4))

        // Checking restarted by
        onView(withRecyclerView(R.id.overview_recycler_view).atPositionOnView(4, R.id.itemHeader)).check(
            matches(
                withText(R.string.build_restarted_by_section_text)
            )
        )
        onView(
            withRecyclerView(R.id.overview_recycler_view).atPositionOnView(
                4,
                R.id.itemTitle
            )
        ).check(matches(withText(NAME)))
    }

    @Test
    fun testUserCanSeeWhatUserRestartedBuildIfUserHasOnlyUserName() {
        // Prepare mocks
        `when`(teamCityService.build(anyString())).thenReturn(Single.just(build))
        val triggered = Triggered(TRIGGER_TYPE_RESTARTED, null, User(USER_NAME, null))
        build.triggered = triggered

        // Prepare intent
        // <! ---------------------------------------------------------------------- !>
        // Passing build object to activity, had to create it for real, Can't pass mock object as serializable in bundle :(
        // <! ---------------------------------------------------------------------- !>
        val intent = Intent()
        val b = Bundle()
        b.putSerializable(BundleExtractorValues.BUILD, Mocks.successBuild())
        b.putString(BundleExtractorValues.NAME, BUILD_TYPE_NAME)
        intent.putExtras(b)

        // Start activity
        activityRule.launchActivity(intent)

        // Scrolling to last item to make it visible
        onView(withId(R.id.overview_recycler_view)).perform(scrollToPosition<RecyclerView.ViewHolder>(4))

        // Checking restarted by
        onView(withRecyclerView(R.id.overview_recycler_view).atPositionOnView(4, R.id.itemHeader)).check(
            matches(
                withText(R.string.build_restarted_by_section_text)
            )
        )
        onView(
            withRecyclerView(R.id.overview_recycler_view).atPositionOnView(
                4,
                R.id.itemTitle
            )
        ).check(matches(withText(USER_NAME)))
    }

    @Test
    fun testUserCanSeeWhatConfigurationTriggeredBuildIfConfigurationIsDeleted() {
        // Prepare mocks
        `when`(teamCityService.build(anyString())).thenReturn(Single.just(build))
        val triggered = Triggered(TRIGGER_TYPE_BUILD_TYPE, null, null)
        build.triggered = triggered

        // Prepare intent
        // <! ---------------------------------------------------------------------- !>
        // Passing build object to activity, had to create it for real, Can't pass mock object as serializable in bundle :(
        // <! ---------------------------------------------------------------------- !>
        val intent = Intent()
        val b = Bundle()
        b.putSerializable(BundleExtractorValues.BUILD, Mocks.successBuild())
        b.putString(BundleExtractorValues.NAME, BUILD_TYPE_NAME)
        intent.putExtras(b)

        // Start activity
        activityRule.launchActivity(intent)

        // Scrolling to last item to make it visible
        onView(withId(R.id.overview_recycler_view)).perform(scrollToPosition<RecyclerView.ViewHolder>(4))

        // Checking restarted by
        onView(withRecyclerView(R.id.overview_recycler_view).atPositionOnView(4, R.id.itemHeader)).check(
            matches(
                withText(R.string.build_triggered_by_section_text)
            )
        )
        onView(
            withRecyclerView(R.id.overview_recycler_view).atPositionOnView(
                4,
                R.id.itemTitle
            )
        ).check(matches(withText(R.string.triggered_deleted_configuration_text)))
    }

    @Test
    fun testUserCanSeeWhatUserRestartedBuildIfUserWasDeleted() {
        // Prepare mocks
        `when`(teamCityService.build(anyString())).thenReturn(Single.just(build))
        val triggered = Triggered(TRIGGER_TYPE_RESTARTED, null, null)
        build.triggered = triggered

        // Prepare intent
        // <! ---------------------------------------------------------------------- !>
        // Passing build object to activity, had to create it for real, Can't pass mock object as serializable in bundle :(
        // <! ---------------------------------------------------------------------- !>
        val intent = Intent()
        val b = Bundle()
        b.putSerializable(BundleExtractorValues.BUILD, Mocks.successBuild())
        b.putString(BundleExtractorValues.NAME, BUILD_TYPE_NAME)
        intent.putExtras(b)

        // Start activity
        activityRule.launchActivity(intent)

        // Scrolling to last item to make it visible
        onView(withId(R.id.overview_recycler_view)).perform(scrollToPosition<RecyclerView.ViewHolder>(4))

        // Checking restarted by
        onView(withRecyclerView(R.id.overview_recycler_view).atPositionOnView(4, R.id.itemHeader)).check(
            matches(
                withText(R.string.build_restarted_by_section_text)
            )
        )
        onView(
            withRecyclerView(R.id.overview_recycler_view).atPositionOnView(
                4,
                R.id.itemTitle
            )
        ).check(matches(withText(R.string.triggered_deleted_user_text)))
    }

    @Test
    fun testUserCanSeeWhatUserTriggeredBuildIfUserWasDeleted() {
        // Prepare mocks
        `when`(teamCityService.build(anyString())).thenReturn(Single.just(build))
        val triggered = Triggered(TRIGGER_TYPE_USER, null, null)
        build.triggered = triggered

        // Prepare intent
        // <! ---------------------------------------------------------------------- !>
        // Passing build object to activity, had to create it for real, Can't pass mock object as serializable in bundle :(
        // <! ---------------------------------------------------------------------- !>
        val intent = Intent()
        val b = Bundle()
        b.putSerializable(BundleExtractorValues.BUILD, Mocks.successBuild())
        b.putString(BundleExtractorValues.NAME, BUILD_TYPE_NAME)
        intent.putExtras(b)

        // Start activity
        activityRule.launchActivity(intent)

        // Scrolling to last item to make it visible
        onView(withId(R.id.overview_recycler_view)).perform(scrollToPosition<RecyclerView.ViewHolder>(4))

        // Checking restarted by
        onView(withRecyclerView(R.id.overview_recycler_view).atPositionOnView(4, R.id.itemHeader)).check(
            matches(
                withText(R.string.build_triggered_by_section_text)
            )
        )
        onView(
            withRecyclerView(R.id.overview_recycler_view).atPositionOnView(
                4,
                R.id.itemTitle
            )
        ).check(matches(withText(R.string.triggered_deleted_user_text)))
    }

    @Test
    fun testUserCanSeePersonalInfoIfBuildIsPersonal() {
        // Prepare mocks
        `when`(teamCityService.build(anyString())).thenReturn(Single.just(build))
        val triggered = Triggered(TRIGGER_TYPE_USER, null, User(USER_NAME, null))
        build.triggered = triggered
        build.isPersonal = true

        // Prepare intent
        // <! ---------------------------------------------------------------------- !>
        // Passing build object to activity, had to create it for real, Can't pass mock object as serializable in bundle :(
        // <! ---------------------------------------------------------------------- !>
        val intent = Intent()
        val b = Bundle()
        val successBuild = Mocks.successBuild()
        successBuild.isPersonal = true
        b.putSerializable(BundleExtractorValues.BUILD, Mocks.successBuild())
        b.putString(BundleExtractorValues.NAME, BUILD_TYPE_NAME)
        intent.putExtras(b)

        // Start activity
        activityRule.launchActivity(intent)

        // Scrolling to last item to make it visible
        onView(withId(R.id.overview_recycler_view)).perform(scrollToPosition<RecyclerView.ViewHolder>(5))

        // Checking Personal of
        onView(withRecyclerView(R.id.overview_recycler_view).atPositionOnView(5, R.id.itemHeader)).check(
            matches(
                withText(R.string.build_personal_text)
            )
        )
        onView(
            withRecyclerView(R.id.overview_recycler_view).atPositionOnView(
                5,
                R.id.itemTitle
            )
        ).check(matches(withText(USER_NAME)))
    }

    @Test
    fun testUserCanViewBuildListFilteredByBranch() {
        // Prepare mocks
        `when`(teamCityService.build(anyString())).thenReturn(Single.just(build))

        // Prepare intent
        // <! ---------------------------------------------------------------------- !>
        // Passing build object to activity, had to create it for real, Can't pass mock object as serializable in bundle :(
        // <! ---------------------------------------------------------------------- !>
        val intent = Intent()
        val b = Bundle()
        b.putSerializable(BundleExtractorValues.BUILD, Mocks.successBuild())
        b.putString(BundleExtractorValues.NAME, BUILD_TYPE_NAME)
        intent.putExtras(b)

        // Start activity
        activityRule.launchActivity(intent)

        // Click on Branch card
        onView(withRecyclerView(R.id.overview_recycler_view).atPosition(2)).perform(click())

        // Clicking on show all builds
        onView(withText(R.string.build_element_show_all_builds_built_branch)).perform(click())

        // Check builds list activity is opened
        intended(hasComponent(BuildListActivity::class.java.name))

        // Check buid list filter is applied
        verify(teamCityService).listBuilds(
            eq("Checkstyle_IdeaInspectionsPullRequest"),
            eq("state:any,canceled:any,failedToStart:any,branch:name:refs/heads/master,personal:false,pinned:false,count:10")
        )
    }

    @Test
    fun testUserCanViewBuildTypeFromCard() {
        // Prepare mocks
        `when`(teamCityService.build(anyString())).thenReturn(Single.just(build))

        // Prepare intent
        // <! ---------------------------------------------------------------------- !>
        // Passing build object to activity, had to create it for real, Can't pass mock object as serializable in bundle :(
        // <! ---------------------------------------------------------------------- !>
        val intent = Intent()
        val b = Bundle()
        b.putSerializable(BundleExtractorValues.BUILD, Mocks.successBuild())
        b.putString(BundleExtractorValues.NAME, BUILD_TYPE_NAME)
        intent.putExtras(b)

        // Start activity
        activityRule.launchActivity(intent)

        // Scrolling to last item to make it visible
        onView(withId(R.id.overview_recycler_view)).perform(scrollToPosition<RecyclerView.ViewHolder>(6))

        // Checking configuration  details
        onView(withRecyclerView(R.id.overview_recycler_view).atPositionOnView(5, R.id.itemHeader)).check(
            matches(
                withText(R.string.build_type_by_section_text)
            )
        )
        onView(
            withRecyclerView(R.id.overview_recycler_view).atPositionOnView(
                5,
                R.id.itemTitle
            )
        ).check(matches(withText("build type name")))

        // Click on that card
        onView(withRecyclerView(R.id.overview_recycler_view).atPosition(5)).perform(click())

        // Clicking on show build type
        onView(withText(R.string.build_element_open_build_type)).perform(click())

        intended(
            allOf(
                hasComponent(BuildListActivity::class.java.name),
                hasExtras(
                    allOf(
                        hasEntry(equalTo(BundleExtractorValues.BUILD_LIST_FILTER), equalTo<Any>(null)),
                        hasEntry(equalTo(BundleExtractorValues.ID), equalTo("Checkstyle_IdeaInspectionsPullRequest")),
                        hasEntry(equalTo(BundleExtractorValues.NAME), equalTo("build type name"))
                    )
                )
            )
        )

        // Check buid list filter is applied
        verify(teamCityService).listBuilds(
            eq("Checkstyle_IdeaInspectionsPullRequest"),
            eq("state:any,branch:default:any,personal:any,pinned:any,canceled:any,failedToStart:any,count:10")
        )
    }

    @Test
    fun testUserCanViewProjectFromCard() {
        // Prepare mocks
        `when`(teamCityService.build(anyString())).thenReturn(Single.just(build))

        // Prepare intent
        // <! ---------------------------------------------------------------------- !>
        // Passing build object to activity, had to create it for real, Can't pass mock object as serializable in bundle :(
        // <! ---------------------------------------------------------------------- !>
        val intent = Intent()
        val b = Bundle()
        b.putSerializable(BundleExtractorValues.BUILD, Mocks.successBuild())
        b.putString(BundleExtractorValues.NAME, BUILD_TYPE_NAME)
        intent.putExtras(b)

        // Start activity
        activityRule.launchActivity(intent)

        // Scrolling to last item to make it visible
        onView(withId(R.id.overview_recycler_view)).perform(scrollToPosition<RecyclerView.ViewHolder>(6))
        onView(withId(android.R.id.content)).perform(swipeUp())

        // Checking configuration  details
        onView(withRecyclerView(R.id.overview_recycler_view).atPositionOnView(6, R.id.itemHeader)).check(
            matches(
                withText(R.string.build_project_by_section_text)
            )
        )
        onView(
            withRecyclerView(R.id.overview_recycler_view).atPositionOnView(
                6,
                R.id.itemTitle
            )
        ).check(matches(withText("project name")))

        // Click on that card
        onView(withRecyclerView(R.id.overview_recycler_view).atPositionOnView(6, R.id.itemHeader)).perform(click())

        // Clicking on show project
        onView(withText(R.string.build_element_open_project)).perform(click())

        intended(
            allOf(
                hasComponent(NavigationActivity::class.java.name),
                hasExtras(
                    allOf(
                        hasEntry(equalTo(BundleExtractorValues.ID), equalTo("projectId")),
                        hasEntry(equalTo(BundleExtractorValues.NAME), equalTo("project name"))
                    )
                )
            )
        )

        // Check buid list filter is applied
        verify(teamCityService).listBuildTypes(eq("projectId"))
    }

    @Test
    fun testUserCanSeeSuccessRestartBuildOnboardingForSuccessBuild() {
        // Prepare mocks
        TestUtils.enableOnboarding()
        storage.saveGuestUserAccountAndSetItAsActive(Mocks.URL, false)
        `when`(teamCityService.build(anyString())).thenReturn(Single.just(build))

        // Prepare intent
        // <! ---------------------------------------------------------------------- !>
        // Passing build object to activity, had to create it for real, Can't pass mock object as serializable in bundle :(
        // <! ---------------------------------------------------------------------- !>
        val intent = Intent()
        val b = Bundle()
        b.putSerializable(BundleExtractorValues.BUILD, Mocks.successBuild())
        b.putString(BundleExtractorValues.NAME, BUILD_TYPE_NAME)
        intent.putExtras(b)

        // Start activity
        activityRule.launchActivity(intent)

        ConditionWatcher.waitForCondition(object : Instruction() {
            override fun getDescription(): String {
                return "No onboarding view is opened"
            }

            override fun checkCondition(): Boolean {
                return try {
                    onView(withId(R.id.material_target_prompt_view)).check(matches(isDisplayed()))
                    true
                } catch (ignored: Exception) {
                    false
                }
            }
        })

        openContextualActionModeOverflowMenu()

        onView(withId(R.id.material_target_prompt_view)).check(doesNotExist())
    }
}

fun <T> eq(obj: T): T = Mockito.eq<T>(obj)
