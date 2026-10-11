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

package com.github.vase4kin.teamcityapp.cancel_build

import android.content.Intent
import android.os.Bundle
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.TeamCityApplicationBase
import com.github.vase4kin.teamcityapp.api.TeamCityService
import com.github.vase4kin.teamcityapp.base.extractor.BundleExtractorValues
import com.github.vase4kin.teamcityapp.build_details.api.BuildCancelRequest
import com.github.vase4kin.teamcityapp.build_details.view.BuildDetailsActivity
import com.github.vase4kin.teamcityapp.buildlist.api.Build
import com.github.vase4kin.teamcityapp.dagger.modules.FakeTeamCityServiceImpl
import com.github.vase4kin.teamcityapp.dagger.modules.Mocks
import com.github.vase4kin.teamcityapp.helper.CustomActivityTestRule
import com.github.vase4kin.teamcityapp.helper.HiltApiTestRule
import com.github.vase4kin.teamcityapp.helper.TestUtils
import com.github.vase4kin.teamcityapp.helper.capture
import com.github.vase4kin.teamcityapp.runbuild.interactor.CODE_FORBIDDEN
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.reactivex.Single
import okhttp3.ResponseBody
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.`is`
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.Spy
import retrofit2.HttpException
import retrofit2.Response

private const val NAME = "name"

/**
 * Tests for cancel build feature
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class CancelBuildTest {
    @JvmField
    @Rule(order = 4)
    val compose = createEmptyComposeRule()

    private fun openOverviewMenu() {
        compose.waitUntil(10_000) { compose.onAllNodesWithTag("overview:list").fetchSemanticsNodes(atLeastOneRootRequired = false).isNotEmpty() }
        compose.waitForIdle()
        compose.waitUntil(10_000) {
            runCatching { onView(withContentDescription("More options")).check(matches(isDisplayed())) }.isSuccess
        }
        openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getInstrumentation().targetContext)
    }

    private fun assertOverviewResult(expected: String) {
        compose.waitUntil(10_000) { compose.onAllNodes(hasTestTag("overview:row:Result").or(hasTestTag("overview:row:WaitReason"))).fetchSemanticsNodes(atLeastOneRootRequired = false).isNotEmpty() }
        compose.onNodeWithTag("overview:list").performScrollToNode(hasTestTag("overview:row:Result").or(hasTestTag("overview:row:WaitReason")))
        compose.waitUntil(10_000) {
            compose.onAllNodes(hasTestTag("overview:row:Result").or(hasTestTag("overview:row:WaitReason")).and(hasText(expected, substring = true)))
                .fetchSemanticsNodes(atLeastOneRootRequired = false).isNotEmpty()
        }
        compose.onNode(hasTestTag("overview:row:Result").or(hasTestTag("overview:row:WaitReason"))).assertTextContains(expected).assertIsDisplayed()
    }

    @JvmField
    @Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @JvmField
    @Rule(order = 1)
    val mockitoRule = org.mockito.junit.MockitoJUnit.rule().strictness(org.mockito.quality.Strictness.LENIENT)

    @JvmField
    @Rule(order = 2)
    val apiRule = HiltApiTestRule(hiltRule) { teamCityService }

    @Rule(order = 3)
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
            InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TeamCityApplicationBase
        app.appInjector.sharedUserStorage().clearAll()
        app.appInjector.sharedUserStorage()
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
            InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TeamCityApplicationBase
        app.appInjector.sharedUserStorage().saveUserAccountAndSetItAsActive(
            Mocks.URL,
            "code-lover",
            "123456",
            false,
            object : SharedUserStorage.OnStorageListener {
                override fun onSuccess() {}

                override fun onFail() {}
            }
        )

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
        openOverviewMenu()

        // Click on context menu option
        onView(withText(R.string.text_menu_remove_build_from_queue)).perform(click())

        // Check dialog text is displayed
        onView(withText(R.string.text_remove_build_from_queue)).check(matches(isDisplayed()))

        // Click on cancel build
        onView(withText(R.string.text_remove_from_queue_button)).perform(click())

        // Check snack bar is displayed
        onView(withText(R.string.text_build_is_removed_from_queue)).check(matches(isDisplayed()))

        // Checking Result was changed
        assertOverviewResult("This build will not start because there are no compatible agents which can run it")
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
        openOverviewMenu()

        // Click on context menu option
        onView(withText(R.string.text_menu_remove_build_from_queue)).perform(click())

        // Check dialog text is displayed
        onView(withText(R.string.text_remove_build_from_queue_2)).check(matches(isDisplayed()))

        // Click on cancel build
        onView(withText(R.string.text_remove_from_queue_button)).perform(click())

        // Check snack bar is displayed
        onView(withText(R.string.text_build_is_removed_from_queue)).check(matches(isDisplayed()))

        // Checking Result was changed
        assertOverviewResult("This build will not start because there are no compatible agents which can run it")
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
        openOverviewMenu()

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
        openOverviewMenu()

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
        openOverviewMenu()

        // Click on context menu option
        onView(withText(R.string.text_menu_stop_build)).perform(click())

        // Check dialog text is displayed
        onView(withText(R.string.text_stop_the_build_2)).check(matches(isDisplayed()))

        // Click on cancel build
        onView(withText(R.string.text_stop_button)).perform(click())

        // Check snack bar is displayed
        onView(withText(R.string.text_build_is_stopped)).check(matches(isDisplayed()))

        // Checking Result was changed
        assertOverviewResult("Error with smth")
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
            InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TeamCityApplicationBase
        app.appInjector.sharedUserStorage().saveUserAccountAndSetItAsActive(
            Mocks.URL,
            "code-lover",
            "123456",
            false,
            object : SharedUserStorage.OnStorageListener {
                override fun onSuccess() {}

                override fun onFail() {}
            }
        )

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
        openOverviewMenu()

        // Click on context menu option
        onView(withText(R.string.text_menu_stop_build)).perform(click())

        // Check dialog text is displayed
        onView(withText(R.string.text_stop_the_build)).check(matches(isDisplayed()))

        // Click on cancel build
        onView(withText(R.string.text_stop_button)).perform(click())

        // Check snack bar is displayed
        onView(withText(R.string.text_build_is_stopped)).check(matches(isDisplayed()))

        // Checking Result was changed
        assertOverviewResult("Error with smth")
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
        openOverviewMenu()

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
        openOverviewMenu()

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
        openOverviewMenu()

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
        assertOverviewResult("Error with smth")

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
        openOverviewMenu()

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
        assertOverviewResult("This build will not start because there are no compatible agents which can run it")

        // Verify build wasn't re-added
        verify(teamCityService).cancelBuild(
            anyString(),
            capture(buildCancelRequestArgumentCaptor)
        )
        val (isReAddIntoQueue) = buildCancelRequestArgumentCaptor.value
        assertThat(isReAddIntoQueue, `is`(equalTo(false)))
    }
}
