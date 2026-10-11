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

package com.github.vase4kin.teamcityapp.restart_build

import android.content.Intent
import android.os.Bundle
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.BundleMatchers.hasEntry
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtras
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDisplayingAtLeast
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.TeamCityApplicationBase
import com.github.vase4kin.teamcityapp.api.TeamCityService
import com.github.vase4kin.teamcityapp.base.extractor.BundleExtractorValues
import com.github.vase4kin.teamcityapp.buildlist.api.Build
import com.github.vase4kin.teamcityapp.dagger.modules.Mocks
import com.github.vase4kin.teamcityapp.helper.CustomIntentsTestRule
import com.github.vase4kin.teamcityapp.helper.HiltApiTestRule
import com.github.vase4kin.teamcityapp.helper.TestUtils
import com.github.vase4kin.teamcityapp.helper.any
import com.github.vase4kin.teamcityapp.helper.awaitDisplayedText
import com.github.vase4kin.teamcityapp.helper.capture
import com.github.vase4kin.teamcityapp.helper.currentRootSnapshot
import com.github.vase4kin.teamcityapp.helper.tapBuildDetailsTab
import com.github.vase4kin.teamcityapp.navigation.IncomingDestinationResolver
import com.github.vase4kin.teamcityapp.navigation.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.reactivex.Single
import java.net.HttpURLConnection.HTTP_FORBIDDEN
import okhttp3.ResponseBody
import org.hamcrest.CoreMatchers
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.`is`
import org.hamcrest.core.AllOf.allOf
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
import teamcityapp.features.properties.repository.models.Properties

private const val BRANCH_NAME = "refs/heads/dev"
private const val PROPERTY_NAME = "property"
private const val PROPERTY_VALUE = "true"
private const val BUILD_TYPE_NAME = "name"

/**
 * Tests for restart build feature
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class RestartBuildTest {
    @JvmField
    @Rule(order = 4)
    val compose = createEmptyComposeRule()

    private fun openOverviewMenu() {
        compose.waitUntil(10_000) { compose.onAllNodesWithTag("overview:list").fetchSemanticsNodes(atLeastOneRootRequired = false).isNotEmpty() }
        compose.waitForIdle()
        compose.waitUntil(10_000) {
            compose.onAllNodesWithContentDescription(InstrumentationRegistry.getInstrumentation().targetContext.getString(teamcityapp.features.build_details.impl.R.string.action_more)).fetchSemanticsNodes(atLeastOneRootRequired = false).isNotEmpty()
        }
        compose.onNodeWithContentDescription(InstrumentationRegistry.getInstrumentation().targetContext.getString(teamcityapp.features.build_details.impl.R.string.action_more)).performClick()
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
    val activityRule: CustomIntentsTestRule<MainActivity> =
        CustomIntentsTestRule(MainActivity::class.java, IncomingDestinationResolver.BUILD_DETAILS_COMPONENT)

    @Captor
    lateinit var buildArgumentCaptor: ArgumentCaptor<Build>

    @Mock
    lateinit var teamCityService: TeamCityService

    @Spy
    val build = Mocks.failedBuild()

    val build2 = Mocks.queuedBuild2()

    @Mock
    lateinit var responseBody: ResponseBody

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
            teamCityService.listAgents(
                any(),
                any(),
                any()
            )
        ).thenReturn(Single.just(Mocks.connectedAgents()))
        `when`(
            teamCityService.listArtifacts(
                any(),
                any()
            )
        ).thenReturn(Single.just(Mocks.artifacts()))
        `when`(teamCityService.listTestOccurrences(any())).thenReturn(Single.just(Mocks.ignoredTests()))
        `when`(teamCityService.listChanges(any())).thenReturn(Single.just(Mocks.changes()))
    }

    @Test
    fun testUserCanRestartBuildWithTheSameParameters() {
        // Prepare mocks
        `when`(teamCityService.build(anyString())).thenReturn(Single.just(build))
            .thenReturn(Single.just(build2))
        `when`(teamCityService.queueBuild(any())).thenReturn(
            Single.just(
                Mocks.queuedBuild2()
            )
        )

        // Prepare intent
        // <! ---------------------------------------------------------------------- !>
        // Passing build object to activity, had to create it for real, Can't pass mock object as serializable in bundle :(
        // <! ---------------------------------------------------------------------- !>
        val intent = Intent()
        val b = Bundle()
        val buildToRestart = Mocks.failedBuild()
        val property = Properties.Property(PROPERTY_NAME, PROPERTY_VALUE)
        buildToRestart.branchName = BRANCH_NAME
        buildToRestart.properties =
            Properties(
                listOf(property)
            )
        `when`(teamCityService.build(anyString())).thenReturn(Single.just(buildToRestart)).thenReturn(Single.just(build2))
        b.putSerializable(BundleExtractorValues.BUILD, buildToRestart)
        b.putString(BundleExtractorValues.NAME, BUILD_TYPE_NAME)
        intent.putExtras(b)

        // Start activity
        activityRule.launchActivity(intent)

        // Opening context menu
        openOverviewMenu()

        // Click on context menu option
        compose.onAllNodesWithText(InstrumentationRegistry.getInstrumentation().targetContext.getString(R.string.text_menu_restart_build)).onFirst().performClick()

        // Check dialog text is displayed
        compose.awaitDisplayedText(InstrumentationRegistry.getInstrumentation().targetContext.getString(R.string.text_restart_the_build))

        // Click on restart button
        compose.onAllNodesWithText(InstrumentationRegistry.getInstrumentation().targetContext.getString(R.string.text_restart_button)).onFirst().performClick()

        // Check snack bar is displayed
        compose.awaitDisplayedText(InstrumentationRegistry.getInstrumentation().targetContext.getString(R.string.text_build_is_restarted))

        // Checking passed params
        verify(teamCityService).queueBuild(capture(buildArgumentCaptor))
        val capturedBuild = buildArgumentCaptor.value
        assertThat(capturedBuild.branchName, `is`(equalTo(BRANCH_NAME)))
        assertThat(capturedBuild.properties!!.properties.size, `is`(equalTo(1)))
        val capturedProperty = capturedBuild.properties!!.properties[0]
        assertThat(capturedProperty.name, `is`(equalTo(PROPERTY_NAME)))
        assertThat(capturedProperty.value, `is`(equalTo(PROPERTY_VALUE)))

        // Click on show button of queued build snack bar
        compose.onAllNodesWithText(InstrumentationRegistry.getInstrumentation().targetContext.getString(R.string.text_show_build)).onFirst().performClick()

        // Check build is opened
        compose.waitUntil(10_000) {
            (currentRootSnapshot().entries.last().destination as? teamcityapp.features.build_details.api.BuildDetailsDestination)?.let {
                it.forceOverviewRefresh && it.build == com.github.vase4kin.teamcityapp.builds.data.AppBuildLaunchMapper().toLaunchData(build2)
            } == true
        }
        val destination = currentRootSnapshot().entries.last().destination as teamcityapp.features.build_details.api.BuildDetailsDestination
        org.junit.Assert.assertEquals(com.github.vase4kin.teamcityapp.builds.data.AppBuildLaunchMapper().toLaunchData(build2), destination.build)
        org.junit.Assert.assertTrue(destination.forceOverviewRefresh)

        // Checking Result was changed
        assertOverviewResult("This build will not start because there are no compatible agents which can run it")
    }

    @Test
    fun testUserCanRestartBuildWithTheSameParametersButFailedToOpenItThen() {
        // Prepare mocks
        val httpException = HttpException(Response.error<Build>(500, responseBody))
        `when`(teamCityService.build(anyString())).thenReturn(Single.just(build))
            .thenReturn(Single.error(httpException))
            .thenReturn(Single.error(httpException))
        `when`(teamCityService.queueBuild(any())).thenReturn(
            Single.just(
                Mocks.queuedBuild2()
            )
        )

        // Prepare intent
        // <! ---------------------------------------------------------------------- !>
        // Passing build object to activity, had to create it for real, Can't pass mock object as serializable in bundle :(
        // <! ---------------------------------------------------------------------- !>
        val intent = Intent()
        val b = Bundle()
        val buildToRestart = Mocks.failedBuild()
        val property = Properties.Property(PROPERTY_NAME, PROPERTY_VALUE)
        buildToRestart.branchName = BRANCH_NAME
        buildToRestart.properties =
            Properties(
                listOf(property)
            )
        `when`(teamCityService.build(anyString())).thenReturn(Single.just(buildToRestart)).thenReturn(Single.error(httpException)).thenReturn(Single.error(httpException))
        b.putSerializable(BundleExtractorValues.BUILD, buildToRestart)
        b.putString(BundleExtractorValues.NAME, BUILD_TYPE_NAME)
        intent.putExtras(b)

        // Start activity
        activityRule.launchActivity(intent)

        // Opening context menu
        openOverviewMenu()

        // Click on context menu option
        compose.onAllNodesWithText(InstrumentationRegistry.getInstrumentation().targetContext.getString(R.string.text_menu_restart_build)).onFirst().performClick()

        // Click on restart button
        compose.onAllNodesWithText(InstrumentationRegistry.getInstrumentation().targetContext.getString(R.string.text_restart_button)).onFirst().performClick()

        // Check snack bar is displayed
        compose.awaitDisplayedText(InstrumentationRegistry.getInstrumentation().targetContext.getString(R.string.text_build_is_restarted))

        // Click on show button of queued build snack bar
        compose.onAllNodesWithText(InstrumentationRegistry.getInstrumentation().targetContext.getString(R.string.text_show_build)).onFirst().performClick()

        // Check error snack bar
        compose.awaitDisplayedText(InstrumentationRegistry.getInstrumentation().targetContext.getString(R.string.error_opening_build))

        // Click on retry button
        compose.waitUntil(10_000) {
            compose.onAllNodesWithText(InstrumentationRegistry.getInstrumentation().targetContext.getString(R.string.download_artifact_retry_snack_bar_retry_button)).fetchSemanticsNodes(atLeastOneRootRequired = false).isNotEmpty()
        }
        compose.onAllNodesWithText(InstrumentationRegistry.getInstrumentation().targetContext.getString(R.string.download_artifact_retry_snack_bar_retry_button)).onFirst().performClick()

        // Check error snack bar
        compose.awaitDisplayedText(InstrumentationRegistry.getInstrumentation().targetContext.getString(R.string.error_opening_build))
    }

    @Test
    fun testUserCanSeeForbiddenErrorWhenRestartingBuild() {
        // Prepare mocks
        `when`(teamCityService.build(anyString())).thenReturn(Single.just(build))
        val httpException = HttpException(Response.error<Build>(HTTP_FORBIDDEN, responseBody))
        `when`(teamCityService.queueBuild(any())).thenReturn(
            Single.error(
                httpException
            )
        )

        // Prepare intent
        // <! ---------------------------------------------------------------------- !>
        // Passing build object to activity, had to create it for real, Can't pass mock object as serializable in bundle :(
        // <! ---------------------------------------------------------------------- !>
        val intent = Intent()
        val b = Bundle()
        b.putSerializable(BundleExtractorValues.BUILD, Mocks.failedBuild())
        b.putString(BundleExtractorValues.NAME, BUILD_TYPE_NAME)
        intent.putExtras(b)

        // Start activity
        activityRule.launchActivity(intent)

        // Opening context menu
        openOverviewMenu()

        // Click on context menu option
        compose.onAllNodesWithText(InstrumentationRegistry.getInstrumentation().targetContext.getString(R.string.text_menu_restart_build)).onFirst().performClick()

        // Click on restart button
        compose.onAllNodesWithText(InstrumentationRegistry.getInstrumentation().targetContext.getString(R.string.text_restart_button)).onFirst().performClick()

        // Check snack bar is displayed
        compose.awaitDisplayedText(InstrumentationRegistry.getInstrumentation().targetContext.getString(R.string.error_restart_build_forbidden_error))
    }

    @Test
    fun testUserCanSeeServerErrorWhenRestartingBuild() {
        // Prepare mocks
        `when`(teamCityService.build(anyString())).thenReturn(Single.just(build))
        val httpException = HttpException(Response.error<Build>(500, responseBody))
        `when`(teamCityService.queueBuild(any())).thenReturn(
            Single.error(
                httpException
            )
        )

        // Prepare intent
        // <! ---------------------------------------------------------------------- !>
        // Passing build object to activity, had to create it for real, Can't pass mock object as serializable in bundle :(
        // <! ---------------------------------------------------------------------- !>
        val intent = Intent()
        val b = Bundle()
        b.putSerializable(BundleExtractorValues.BUILD, Mocks.failedBuild())
        b.putString(BundleExtractorValues.NAME, BUILD_TYPE_NAME)
        intent.putExtras(b)

        // Start activity
        activityRule.launchActivity(intent)

        // Opening context menu
        openOverviewMenu()

        // Click on context menu option
        compose.onAllNodesWithText(InstrumentationRegistry.getInstrumentation().targetContext.getString(R.string.text_menu_restart_build)).onFirst().performClick()

        // Click on restart button
        compose.onAllNodesWithText(InstrumentationRegistry.getInstrumentation().targetContext.getString(R.string.text_restart_button)).onFirst().performClick()

        // Check snack bar is displayed
        compose.awaitDisplayedText(InstrumentationRegistry.getInstrumentation().targetContext.getString(R.string.error_base_restart_build_error))
    }
}
