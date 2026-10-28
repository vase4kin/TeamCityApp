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
import com.github.vase4kin.teamcityapp.TeamCityApplicationBase
import com.github.vase4kin.teamcityapp.api.TeamCityService
import com.github.vase4kin.teamcityapp.base.extractor.BundleExtractorValues
import com.github.vase4kin.teamcityapp.build_details.api.BuildCancelRequest
import com.github.vase4kin.teamcityapp.buildlist.api.Build
import com.github.vase4kin.teamcityapp.di.modules.FakeTeamCityServiceImpl
import com.github.vase4kin.teamcityapp.di.modules.MockHttpFailure
import com.github.vase4kin.teamcityapp.di.modules.Mocks
import com.github.vase4kin.teamcityapp.helper.CustomActivityTestRule
import com.github.vase4kin.teamcityapp.helper.HiltApiTestRule
import com.github.vase4kin.teamcityapp.helper.TestUtils
import com.github.vase4kin.teamcityapp.helper.awaitDisplayedText
import com.github.vase4kin.teamcityapp.helper.capture
import com.github.vase4kin.teamcityapp.helper.currentRootSnapshot
import com.github.vase4kin.teamcityapp.helper.tapBuildAction
import com.github.vase4kin.teamcityapp.helper.tapBuildDetailsSection
import com.github.vase4kin.teamcityapp.navigation.IncomingDestinationResolver
import com.github.vase4kin.teamcityapp.navigation.MainActivity
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import java.net.HttpURLConnection.HTTP_FORBIDDEN
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
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doSuspendableAnswer
import org.mockito.kotlin.verifyBlocking
import org.mockito.kotlin.whenever

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
        compose.waitUntil(10_000) { compose.onAllNodesWithTag("details:actions").fetchSemanticsNodes(atLeastOneRootRequired = false).isNotEmpty() }
        compose.waitForIdle()
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
    var activityRule: CustomActivityTestRule<MainActivity> =
        CustomActivityTestRule(MainActivity::class.java, IncomingDestinationResolver.BUILD_DETAILS_COMPONENT)

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
        doReturn(build, Mocks.queuedBuild2()).whenever(teamCityService) { build(anyString()) }

        doReturn(Mocks.queuedBuild2()).whenever(teamCityService) {
            cancelBuild(
                Mocks.queuedBuild2().href,
                BuildCancelRequest(false)
            )
        }

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
        compose.tapBuildAction("Cancel")

        // Check dialog text is displayed
        compose.awaitDisplayedText(InstrumentationRegistry.getInstrumentation().targetContext.getString(teamcityapp.features.build_details.impl.R.string.text_remove_build_from_queue))

        // Click on cancel build
        compose.onAllNodesWithText(InstrumentationRegistry.getInstrumentation().targetContext.getString(teamcityapp.features.build_details.impl.R.string.text_remove_from_queue_button)).onFirst().performClick()

        // Check snack bar is displayed
        compose.awaitDisplayedText(InstrumentationRegistry.getInstrumentation().targetContext.getString(teamcityapp.features.build_details.impl.R.string.text_build_is_removed_from_queue))

        // Checking Result was changed
        assertOverviewResult("This build will not start because there are no compatible agents which can run it")
    }

    @Test
    fun testUserCanRemoveQueuedBuildFromQueueWhichWasStartedNotByHim() {
        // Prepare mocks
        doReturn(build, Mocks.queuedBuild2()).whenever(teamCityService) { build(anyString()) }
        doReturn(Mocks.queuedBuild2()).whenever(teamCityService) {
            cancelBuild(
                Mocks.queuedBuild2().href,
                BuildCancelRequest(false)
            )
        }

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
        compose.tapBuildAction("Cancel")

        // Check dialog text is displayed
        compose.awaitDisplayedText(InstrumentationRegistry.getInstrumentation().targetContext.getString(teamcityapp.features.build_details.impl.R.string.text_remove_build_from_queue_2))

        // Click on cancel build
        compose.onAllNodesWithText(InstrumentationRegistry.getInstrumentation().targetContext.getString(teamcityapp.features.build_details.impl.R.string.text_remove_from_queue_button)).onFirst().performClick()

        // Check snack bar is displayed
        compose.awaitDisplayedText(InstrumentationRegistry.getInstrumentation().targetContext.getString(teamcityapp.features.build_details.impl.R.string.text_build_is_removed_from_queue))

        // Checking Result was changed
        assertOverviewResult("This build will not start because there are no compatible agents which can run it")
    }

    @Test
    fun testUserCanSeeForbiddenErrorWhenRemovingBuildFromQueue() {
        // Prepare mocks
        doReturn(build).whenever(teamCityService) { build(anyString()) }
        val httpException = MockHttpFailure(HTTP_FORBIDDEN)
        doSuspendableAnswer { throw httpException }.whenever(teamCityService) {
            cancelBuild(
                build.href,
                BuildCancelRequest(false)
            )
        }

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
        compose.tapBuildAction("Cancel")

        // Click on cancel build
        compose.onAllNodesWithText(InstrumentationRegistry.getInstrumentation().targetContext.getString(teamcityapp.features.build_details.impl.R.string.text_remove_from_queue_button)).onFirst().performClick()

        // Check snack bar is displayed
        compose.awaitDisplayedText(InstrumentationRegistry.getInstrumentation().targetContext.getString(teamcityapp.features.build_details.impl.R.string.error_remove_build_from_queue_forbidden_error))
    }

    @Test
    fun testUserCanSeeServerErrorWhenRemovingBuildFromQueue() {
        // Prepare mocks
        doReturn(build).whenever(teamCityService) { build(anyString()) }
        val httpException = MockHttpFailure(500)
        doSuspendableAnswer { throw httpException }.whenever(teamCityService) {
            cancelBuild(
                build.href,
                BuildCancelRequest(false)
            )
        }

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
        compose.tapBuildAction("Cancel")

        // Click on cancel build
        compose.onAllNodesWithText(InstrumentationRegistry.getInstrumentation().targetContext.getString(teamcityapp.features.build_details.impl.R.string.text_remove_from_queue_button)).onFirst().performClick()

        // Check snack bar is displayed
        compose.awaitDisplayedText(InstrumentationRegistry.getInstrumentation().targetContext.getString(teamcityapp.features.build_details.impl.R.string.error_base_remove_build_from_queue_error))
    }

    @Test
    fun testUserCanStopBuildWhichWasStartedByNotHim() {
        // Prepare mocks
        doReturn(Mocks.runningBuild(), Mocks.failedBuild()).whenever(teamCityService) { build(anyString()) }
        doReturn(Mocks.failedBuild()).whenever(teamCityService) {
            cancelBuild(
                Mocks.failedBuild().href,
                BuildCancelRequest(false)
            )
        }

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
        compose.tapBuildAction("Cancel")

        // Check dialog text is displayed
        compose.awaitDisplayedText(InstrumentationRegistry.getInstrumentation().targetContext.getString(teamcityapp.features.build_details.impl.R.string.text_stop_the_build_2))

        // Click on cancel build
        compose.onAllNodesWithText(InstrumentationRegistry.getInstrumentation().targetContext.getString(teamcityapp.features.build_details.impl.R.string.text_stop_button)).onFirst().performClick()

        // Check snack bar is displayed
        compose.awaitDisplayedText(InstrumentationRegistry.getInstrumentation().targetContext.getString(teamcityapp.features.build_details.impl.R.string.text_build_is_stopped))

        // Checking Result was changed
        assertOverviewResult("Error with smth")
    }

    @Test
    fun testUserCanStopBuildWhichWasStartedByHim() {
        // Prepare mocks
        doReturn(Mocks.runningBuild(), Mocks.failedBuild()).whenever(teamCityService) { build(anyString()) }
        doReturn(Mocks.failedBuild()).whenever(teamCityService) {
            cancelBuild(
                Mocks.failedBuild().href,
                BuildCancelRequest(false)
            )
        }

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
        compose.tapBuildAction("Cancel")

        // Check dialog text is displayed
        compose.awaitDisplayedText(InstrumentationRegistry.getInstrumentation().targetContext.getString(teamcityapp.features.build_details.impl.R.string.text_stop_the_build))

        // Click on cancel build
        compose.onAllNodesWithText(InstrumentationRegistry.getInstrumentation().targetContext.getString(teamcityapp.features.build_details.impl.R.string.text_stop_button)).onFirst().performClick()

        // Check snack bar is displayed
        compose.awaitDisplayedText(InstrumentationRegistry.getInstrumentation().targetContext.getString(teamcityapp.features.build_details.impl.R.string.text_build_is_stopped))

        // Checking Result was changed
        assertOverviewResult("Error with smth")
    }

    @Test
    fun testUserCanSeeForbiddenErrorWhenStoppingBuild() {
        // Prepare mocks
        doReturn(Mocks.runningBuild()).whenever(teamCityService) { build(anyString()) }
        val httpException = MockHttpFailure(HTTP_FORBIDDEN)
        doSuspendableAnswer { throw httpException }.whenever(teamCityService) {
            cancelBuild(
                Mocks.runningBuild().href,
                BuildCancelRequest(false)
            )
        }

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
        compose.tapBuildAction("Cancel")

        // Click on cancel build
        compose.onAllNodesWithText(InstrumentationRegistry.getInstrumentation().targetContext.getString(teamcityapp.features.build_details.impl.R.string.text_stop_button)).onFirst().performClick()

        // Check snack bar is displayed
        compose.awaitDisplayedText(InstrumentationRegistry.getInstrumentation().targetContext.getString(teamcityapp.features.build_details.impl.R.string.error_stop_build_forbidden_error))
    }

    @Test
    fun testUserCanSeeServerErrorWhenStoppingBuild() {
        // Prepare mocks
        doReturn(Mocks.runningBuild()).whenever(teamCityService) { build(anyString()) }
        val httpException = MockHttpFailure(500)
        doSuspendableAnswer { throw httpException }.whenever(teamCityService) {
            cancelBuild(
                Mocks.runningBuild().href,
                BuildCancelRequest(false)
            )
        }

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
        compose.tapBuildAction("Cancel")

        // Click on cancel build
        compose.onAllNodesWithText(InstrumentationRegistry.getInstrumentation().targetContext.getString(teamcityapp.features.build_details.impl.R.string.text_stop_button)).onFirst().performClick()

        // Check snack bar is displayed
        compose.awaitDisplayedText(InstrumentationRegistry.getInstrumentation().targetContext.getString(teamcityapp.features.build_details.impl.R.string.error_base_stop_build_error))
    }

    @Test
    fun testUserCanReAddBuildWhenStoppingIt() {
        // Prepare mocks
        doReturn(Mocks.runningBuild(), Mocks.failedBuild()).whenever(teamCityService) { build(anyString()) }
        doReturn(Mocks.failedBuild()).whenever(teamCityService) {
            cancelBuild(
                Mocks.failedBuild().href,
                BuildCancelRequest(false)
            )
        }

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
        compose.tapBuildAction("Cancel")

        // Check re-add text is displayed and click on it
        compose.awaitDisplayedText(InstrumentationRegistry.getInstrumentation().targetContext.getString(teamcityapp.features.build_details.impl.R.string.text_re_add_build))
        compose.onNodeWithText(InstrumentationRegistry.getInstrumentation().targetContext.getString(teamcityapp.features.build_details.impl.R.string.text_re_add_build)).performClick()

        // Click on cancel build
        compose.onAllNodesWithText(InstrumentationRegistry.getInstrumentation().targetContext.getString(teamcityapp.features.build_details.impl.R.string.text_stop_button)).onFirst().performClick()

        // Check snack bar is displayed
        compose.awaitDisplayedText(InstrumentationRegistry.getInstrumentation().targetContext.getString(teamcityapp.features.build_details.impl.R.string.text_build_is_stopped))

        // Checking Result was changed
        assertOverviewResult("Error with smth")

        // Verify build was re-added
        verifyBlocking(teamCityService) {
            cancelBuild(
                anyString(),
                capture(buildCancelRequestArgumentCaptor)
            )
        }
        val (isReAddIntoQueue) = buildCancelRequestArgumentCaptor.value
        assertThat(isReAddIntoQueue, `is`(equalTo(true)))
    }

    @Test
    fun testUserCanNotReAddBuildToQueueWhenRemovingItFromQueue() {
        // Prepare mocks
        doReturn(build, Mocks.queuedBuild2()).whenever(teamCityService) { build(anyString()) }
        doReturn(Mocks.queuedBuild2()).whenever(teamCityService) {
            cancelBuild(
                Mocks.queuedBuild2().href,
                BuildCancelRequest(false)
            )
        }

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
        compose.tapBuildAction("Cancel")

        // Check re-add text is displayed and click on it
        compose.onAllNodesWithText(InstrumentationRegistry.getInstrumentation().targetContext.getString(teamcityapp.features.build_details.impl.R.string.text_re_add_build)).assertCountEquals(0)

        // Click on cancel build
        compose.onAllNodesWithText(InstrumentationRegistry.getInstrumentation().targetContext.getString(teamcityapp.features.build_details.impl.R.string.text_remove_from_queue_button)).onFirst().performClick()

        // Check snack bar is displayed
        compose.awaitDisplayedText(InstrumentationRegistry.getInstrumentation().targetContext.getString(teamcityapp.features.build_details.impl.R.string.text_build_is_removed_from_queue))

        // Checking Result was changed
        assertOverviewResult("This build will not start because there are no compatible agents which can run it")

        // Verify build wasn't re-added
        verifyBlocking(teamCityService) {
            cancelBuild(
                anyString(),
                capture(buildCancelRequestArgumentCaptor)
            )
        }
        val (isReAddIntoQueue) = buildCancelRequestArgumentCaptor.value
        assertThat(isReAddIntoQueue, `is`(equalTo(false)))
    }
}
