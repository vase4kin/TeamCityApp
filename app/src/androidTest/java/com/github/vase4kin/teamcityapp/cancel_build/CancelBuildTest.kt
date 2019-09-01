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

package com.github.vase4kin.teamcityapp.cancel_build

import android.content.Intent
import android.os.Bundle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openContextualActionModeOverflowMenu
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.TeamCityApplication
import com.github.vase4kin.teamcityapp.api.TeamCityService
import com.github.vase4kin.teamcityapp.base.extractor.BundleExtractorValues
import com.github.vase4kin.teamcityapp.build_details.api.BuildCancelRequest
import com.github.vase4kin.teamcityapp.build_details.view.BuildDetailsActivity
import com.github.vase4kin.teamcityapp.buildlist.api.Build
import com.github.vase4kin.teamcityapp.dagger.components.AppComponent
import com.github.vase4kin.teamcityapp.dagger.components.RestApiComponent
import com.github.vase4kin.teamcityapp.dagger.modules.AppModule
import com.github.vase4kin.teamcityapp.dagger.modules.FakeTeamCityServiceImpl
import com.github.vase4kin.teamcityapp.dagger.modules.Mocks
import com.github.vase4kin.teamcityapp.dagger.modules.RestApiModule
import com.github.vase4kin.teamcityapp.helper.CustomActivityTestRule
import com.github.vase4kin.teamcityapp.helper.RecyclerViewMatcher.Companion.withRecyclerView
import com.github.vase4kin.teamcityapp.helper.TestUtils
import com.github.vase4kin.teamcityapp.helper.capture
import com.github.vase4kin.teamcityapp.runbuild.interactor.CODE_FORBIDDEN
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage
import io.reactivex.Single
import it.cosenonjaviste.daggermock.DaggerMockRule
import okhttp3.ResponseBody
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.equalTo
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Matchers.anyString
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.Spy
import retrofit2.HttpException
import retrofit2.Response

private const val NAME = "name"

/**
 * Tests for cancel build feature
 */
@RunWith(AndroidJUnit4::class)
class CancelBuildTest {

    @Rule
    @JvmField
    var daggerRule: DaggerMockRule<RestApiComponent> =
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
    var activityRule: CustomActivityTestRule<BuildDetailsActivity> =
        CustomActivityTestRule(BuildDetailsActivity::class.java)

    @Captor
    private lateinit var buildCancelRequestArgumentCaptor: ArgumentCaptor<BuildCancelRequest>

    @Spy
    private val teamCityService: TeamCityService = FakeTeamCityServiceImpl()

    @Spy
    private val build: Build = Mocks.queuedBuild1()

    @Mock
    internal lateinit var responseBody: ResponseBody

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
    fun testUserCanRemoveQueuedBuildFromQueueWhichWasStartedByHim() {
        // Prepare mocks
        `when`(teamCityService.build(anyString())).thenReturn(Single.just(build))
            .thenReturn(Single.just(Mocks.queuedBuild2()))

        `when`(
            teamCityService.cancelBuild(
                Mocks.queuedBuild2().href,
                BuildCancelRequest(false)
            )
        ).thenReturn(Single.just(Mocks.queuedBuild2()))

        val app =
            InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TeamCityApplication
        app.restApiInjector.sharedUserStorage().saveUserAccountAndSetItAsActive(
            Mocks.URL,
            "code-lover",
            "123456",
            false,
            object : SharedUserStorage.OnStorageListener {
                override fun onSuccess() {}

                override fun onFail() {}
            })

        // Prepare intent
        // <! ---------------------------------------------------------------------- !>
        // Passing build object to activity, had to create it for real, Can't pass mock object as serializable in bundle :(
        // <! ---------------------------------------------------------------------- !>
        val intent = Intent()
        val b = Bundle()
        b.putSerializable(BundleExtractorValues.BUILD, Mocks.queuedBuild1())
        b.putString(BundleExtractorValues.NAME, NAME)
        intent.putExtras(b)

        // Start activity
        activityRule.launchActivity(intent)

        // Opening context menu
        openContextualActionModeOverflowMenu()

        // Click on context menu option
        onView(withText(R.string.text_menu_remove_build_from_queue)).perform(click())

        // Check dialog text is displayed
        onView(withText(R.string.text_remove_build_from_queue)).check(matches(isDisplayed()))

        // Click on cancel build
        onView(withText(R.string.text_remove_from_queue_button)).perform(click())

        // Check snack bar is displayed
        onView(withText(R.string.text_build_is_removed_from_queue)).check(matches(isDisplayed()))

        // Checking Result was changed
        onView(
            withRecyclerView(R.id.overview_recycler_view)
                .atPositionOnView(0, R.id.itemTitle)
        )
            .check(matches(withText("This build will not start because there are no compatible agents which can run it")))
    }

    @Test
    fun testUserCanRemoveQueuedBuildFromQueueWhichWasStartedNotByHim() {
        // Prepare mocks
        `when`(teamCityService.build(anyString())).thenReturn(Single.just(build))
            .thenReturn(Single.just(Mocks.queuedBuild2()))
        `when`(
            teamCityService.cancelBuild(
                Mocks.queuedBuild2().href,
                BuildCancelRequest(false)
            )
        ).thenReturn(Single.just(Mocks.queuedBuild2()))

        // Prepare intent
        // <! ---------------------------------------------------------------------- !>
        // Passing build object to activity, had to create it for real, Can't pass mock object as serializable in bundle :(
        // <! ---------------------------------------------------------------------- !>
        val intent = Intent()
        val b = Bundle()
        b.putSerializable(BundleExtractorValues.BUILD, Mocks.queuedBuild1())
        b.putString(BundleExtractorValues.NAME, NAME)
        intent.putExtras(b)

        // Start activity
        activityRule.launchActivity(intent)

        // Opening context menu
        openContextualActionModeOverflowMenu()

        // Click on context menu option
        onView(withText(R.string.text_menu_remove_build_from_queue)).perform(click())

        // Check dialog text is displayed
        onView(withText(R.string.text_remove_build_from_queue_2)).check(matches(isDisplayed()))

        // Click on cancel build
        onView(withText(R.string.text_remove_from_queue_button)).perform(click())

        // Check snack bar is displayed
        onView(withText(R.string.text_build_is_removed_from_queue)).check(matches(isDisplayed()))

        // Checking Result was changed
        onView(
            withRecyclerView(R.id.overview_recycler_view)
                .atPositionOnView(0, R.id.itemTitle)
        )
            .check(matches(withText("This build will not start because there are no compatible agents which can run it")))
    }

    @Test
    fun testUserCanSeeForbiddenErrorWhenRemovingBuildFromQueue() {
        // Prepare mocks
        `when`(teamCityService.build(anyString())).thenReturn(Single.just(build))
        val httpException = HttpException(Response.error<Build>(CODE_FORBIDDEN, responseBody))
        `when`(
            teamCityService.cancelBuild(
                build.href,
                BuildCancelRequest(false)
            )
        ).thenReturn(Single.error(httpException))

        // Prepare intent
        // <! ---------------------------------------------------------------------- !>
        // Passing build object to activity, had to create it for real, Can't pass mock object as serializable in bundle :(
        // <! ---------------------------------------------------------------------- !>
        val intent = Intent()
        val b = Bundle()
        b.putSerializable(BundleExtractorValues.BUILD, Mocks.queuedBuild1())
        b.putString(BundleExtractorValues.NAME, NAME)
        intent.putExtras(b)

        // Start activity
        activityRule.launchActivity(intent)

        // Opening context menu
        openContextualActionModeOverflowMenu()

        // Click on context menu option
        onView(withText(R.string.text_menu_remove_build_from_queue)).perform(click())

        // Click on cancel build
        onView(withText(R.string.text_remove_from_queue_button)).perform(click())

        // Check snack bar is displayed
        onView(withText(R.string.error_remove_build_from_queue_forbidden_error)).check(
            matches(
                isDisplayed()
            )
        )
    }

    @Test
    fun testUserCanSeeServerErrorWhenRemovingBuildFromQueue() {
        // Prepare mocks
        `when`(teamCityService.build(anyString())).thenReturn(Single.just(build))
        val httpException = HttpException(Response.error<Build>(500, responseBody))
        `when`(
            teamCityService.cancelBuild(
                build.href,
                BuildCancelRequest(false)
            )
        ).thenReturn(Single.error(httpException))

        // Prepare intent
        // <! ---------------------------------------------------------------------- !>
        // Passing build object to activity, had to create it for real, Can't pass mock object as serializable in bundle :(
        // <! ---------------------------------------------------------------------- !>
        val intent = Intent()
        val b = Bundle()
        b.putSerializable(BundleExtractorValues.BUILD, Mocks.queuedBuild1())
        b.putString(BundleExtractorValues.NAME, NAME)
        intent.putExtras(b)

        // Start activity
        activityRule.launchActivity(intent)

        // Opening context menu
        openContextualActionModeOverflowMenu()

        // Click on context menu option
        onView(withText(R.string.text_menu_remove_build_from_queue)).perform(click())

        // Click on cancel build
        onView(withText(R.string.text_remove_from_queue_button)).perform(click())

        // Check snack bar is displayed
        onView(withText(R.string.error_base_remove_build_from_queue_error)).check(
            matches(
                isDisplayed()
            )
        )
    }

    @Test
    fun testUserCanStopBuildWhichWasStartedByNotHim() {
        // Prepare mocks
        `when`(teamCityService.build(anyString())).thenReturn(Single.just(Mocks.runningBuild()))
            .thenReturn(Single.just(Mocks.failedBuild()))
        `when`(
            teamCityService.cancelBuild(
                Mocks.failedBuild().href,
                BuildCancelRequest(false)
            )
        ).thenReturn(Single.just(Mocks.failedBuild()))

        // Prepare intent
        // <! ---------------------------------------------------------------------- !>
        // Passing build object to activity, had to create it for real, Can't pass mock object as serializable in bundle :(
        // <! ---------------------------------------------------------------------- !>
        val intent = Intent()
        val b = Bundle()
        b.putSerializable(BundleExtractorValues.BUILD, Mocks.runningBuild())
        b.putString(BundleExtractorValues.NAME, NAME)
        intent.putExtras(b)

        // Start activity
        activityRule.launchActivity(intent)

        // Opening context menu
        openContextualActionModeOverflowMenu()

        // Click on context menu option
        onView(withText(R.string.text_menu_stop_build)).perform(click())

        // Check dialog text is displayed
        onView(withText(R.string.text_stop_the_build_2)).check(matches(isDisplayed()))

        // Click on cancel build
        onView(withText(R.string.text_stop_button)).perform(click())

        // Check snack bar is displayed
        onView(withText(R.string.text_build_is_stopped)).check(matches(isDisplayed()))

        // Checking Result was changed
        onView(
            withRecyclerView(R.id.overview_recycler_view)
                .atPositionOnView(0, R.id.itemTitle)
        )
            .check(matches(withText("Error with smth")))
    }

    @Test
    fun testUserCanStopBuildWhichWasStartedByHim() {
        // Prepare mocks
        `when`(teamCityService.build(anyString())).thenReturn(Single.just(Mocks.runningBuild()))
            .thenReturn(Single.just(Mocks.failedBuild()))
        `when`(
            teamCityService.cancelBuild(
                Mocks.failedBuild().href,
                BuildCancelRequest(false)
            )
        ).thenReturn(Single.just(Mocks.failedBuild()))

        val app =
            InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TeamCityApplication
        app.restApiInjector.sharedUserStorage().saveUserAccountAndSetItAsActive(
            Mocks.URL,
            "code-lover",
            "123456",
            false,
            object : SharedUserStorage.OnStorageListener {
                override fun onSuccess() {}

                override fun onFail() {}
            })

        // Prepare intent
        // <! ---------------------------------------------------------------------- !>
        // Passing build object to activity, had to create it for real, Can't pass mock object as serializable in bundle :(
        // <! ---------------------------------------------------------------------- !>
        val intent = Intent()
        val b = Bundle()
        b.putSerializable(BundleExtractorValues.BUILD, Mocks.runningBuild())
        b.putString(BundleExtractorValues.NAME, NAME)
        intent.putExtras(b)

        // Start activity
        activityRule.launchActivity(intent)

        // Opening context menu
        openContextualActionModeOverflowMenu()

        // Click on context menu option
        onView(withText(R.string.text_menu_stop_build)).perform(click())

        // Check dialog text is displayed
        onView(withText(R.string.text_stop_the_build)).check(matches(isDisplayed()))

        // Click on cancel build
        onView(withText(R.string.text_stop_button)).perform(click())

        // Check snack bar is displayed
        onView(withText(R.string.text_build_is_stopped)).check(matches(isDisplayed()))

        // Checking Result was changed
        onView(
            withRecyclerView(R.id.overview_recycler_view)
                .atPositionOnView(0, R.id.itemTitle)
        )
            .check(matches(withText("Error with smth")))
    }

    @Test
    fun testUserCanSeeForbiddenErrorWhenStoppingBuild() {
        // Prepare mocks
        `when`(teamCityService.build(anyString())).thenReturn(Single.just(Mocks.runningBuild()))
        val httpException = HttpException(Response.error<Build>(CODE_FORBIDDEN, responseBody))
        `when`(
            teamCityService.cancelBuild(
                Mocks.runningBuild().href,
                BuildCancelRequest(false)
            )
        ).thenReturn(Single.error(httpException))

        // Prepare intent
        // <! ---------------------------------------------------------------------- !>
        // Passing build object to activity, had to create it for real, Can't pass mock object as serializable in bundle :(
        // <! ---------------------------------------------------------------------- !>
        val intent = Intent()
        val b = Bundle()
        b.putSerializable(BundleExtractorValues.BUILD, Mocks.runningBuild())
        b.putString(BundleExtractorValues.NAME, NAME)
        intent.putExtras(b)

        // Start activity
        activityRule.launchActivity(intent)

        // Opening context menu
        openContextualActionModeOverflowMenu()

        // Click on context menu option
        onView(withText(R.string.text_menu_stop_build)).perform(click())

        // Click on cancel build
        onView(withText(R.string.text_stop_button)).perform(click())

        // Check snack bar is displayed
        onView(withText(R.string.error_stop_build_forbidden_error)).check(matches(isDisplayed()))
    }

    @Test
    fun testUserCanSeeServerErrorWhenStoppingBuild() {
        // Prepare mocks
        `when`(teamCityService.build(anyString())).thenReturn(Single.just(Mocks.runningBuild()))
        val httpException = HttpException(Response.error<Build>(500, responseBody))
        `when`(
            teamCityService.cancelBuild(
                Mocks.runningBuild().href,
                BuildCancelRequest(false)
            )
        ).thenReturn(Single.error(httpException))

        // Prepare intent
        // <! ---------------------------------------------------------------------- !>
        // Passing build object to activity, had to create it for real, Can't pass mock object as serializable in bundle :(
        // <! ---------------------------------------------------------------------- !>
        val intent = Intent()
        val b = Bundle()
        b.putSerializable(BundleExtractorValues.BUILD, Mocks.runningBuild())
        b.putString(BundleExtractorValues.NAME, NAME)
        intent.putExtras(b)

        // Start activity
        activityRule.launchActivity(intent)

        // Opening context menu
        openContextualActionModeOverflowMenu()

        // Click on context menu option
        onView(withText(R.string.text_menu_stop_build)).perform(click())

        // Click on cancel build
        onView(withText(R.string.text_stop_button)).perform(click())

        // Check snack bar is displayed
        onView(withText(R.string.error_base_stop_build_error)).check(matches(isDisplayed()))
    }

    @Test
    fun testUserCanReAddBuildWhenStoppingIt() {
        // Prepare mocks
        `when`(teamCityService.build(anyString())).thenReturn(Single.just(Mocks.runningBuild()))
            .thenReturn(Single.just(Mocks.failedBuild()))
        `when`(
            teamCityService.cancelBuild(
                Mocks.failedBuild().href,
                BuildCancelRequest(false)
            )
        ).thenReturn(Single.just(Mocks.failedBuild()))

        // Prepare intent
        // <! ---------------------------------------------------------------------- !>
        // Passing build object to activity, had to create it for real, Can't pass mock object as serializable in bundle :(
        // <! ---------------------------------------------------------------------- !>
        val intent = Intent()
        val b = Bundle()
        b.putSerializable(BundleExtractorValues.BUILD, Mocks.runningBuild())
        b.putString(BundleExtractorValues.NAME, NAME)
        intent.putExtras(b)

        // Start activity
        activityRule.launchActivity(intent)

        // Opening context menu
        openContextualActionModeOverflowMenu()

        // Click on context menu option
        onView(withText(R.string.text_menu_stop_build)).perform(click())

        // Check re-add text is displayed and click on it
        onView(withText(R.string.text_re_add_build))
            .check(matches(isDisplayed()))
            .perform(click())

        // Click on cancel build
        onView(withText(R.string.text_stop_button)).perform(click())

        // Check snack bar is displayed
        onView(withText(R.string.text_build_is_stopped)).check(matches(isDisplayed()))

        // Checking Result was changed
        onView(
            withRecyclerView(R.id.overview_recycler_view)
                .atPositionOnView(0, R.id.itemTitle)
        )
            .check(matches(withText("Error with smth")))

        // Verify build was re-added
        verify(teamCityService).cancelBuild(
            anyString(),
            capture(buildCancelRequestArgumentCaptor)
        )
        val (isReAddIntoQueue) = buildCancelRequestArgumentCaptor.value
        assertThat(isReAddIntoQueue, `is`(equalTo(true)))
    }

    @Test
    fun testUserCanNotReAddBuildToQueueWhenRemovingItFromQueue() {
        // Prepare mocks
        `when`(teamCityService.build(anyString())).thenReturn(Single.just(build))
            .thenReturn(Single.just(Mocks.queuedBuild2()))
        `when`(
            teamCityService.cancelBuild(
                Mocks.queuedBuild2().href,
                BuildCancelRequest(false)
            )
        ).thenReturn(Single.just(Mocks.queuedBuild2()))

        // Prepare intent
        // <! ---------------------------------------------------------------------- !>
        // Passing build object to activity, had to create it for real, Can't pass mock object as serializable in bundle :(
        // <! ---------------------------------------------------------------------- !>
        val intent = Intent()
        val b = Bundle()
        b.putSerializable(BundleExtractorValues.BUILD, Mocks.queuedBuild1())
        b.putString(BundleExtractorValues.NAME, NAME)
        intent.putExtras(b)

        // Start activity
        activityRule.launchActivity(intent)

        // Opening context menu
        openContextualActionModeOverflowMenu()

        // Click on context menu option
        onView(withText(R.string.text_menu_remove_build_from_queue)).perform(click())

        // Check re-add text is displayed and click on it
        onView(withText(R.string.text_re_add_build))
            .check(doesNotExist())

        // Click on cancel build
        onView(withText(R.string.text_remove_from_queue_button)).perform(click())

        // Check snack bar is displayed
        onView(withText(R.string.text_build_is_removed_from_queue)).check(matches(isDisplayed()))

        // Checking Result was changed
        onView(
            withRecyclerView(R.id.overview_recycler_view)
                .atPositionOnView(0, R.id.itemTitle)
        )
            .check(matches(withText("This build will not start because there are no compatible agents which can run it")))

        // Verify build wasn't re-added
        verify(teamCityService).cancelBuild(
            anyString(),
            capture(buildCancelRequestArgumentCaptor)
        )
        val (isReAddIntoQueue) = buildCancelRequestArgumentCaptor.value
        assertThat(isReAddIntoQueue, `is`(equalTo(false)))
    }
}
