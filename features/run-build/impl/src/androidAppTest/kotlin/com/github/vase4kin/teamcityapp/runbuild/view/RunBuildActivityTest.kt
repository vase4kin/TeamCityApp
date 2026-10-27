/*
 * Copyright 2020 Andrey Tolpeev
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

package com.github.vase4kin.teamcityapp.runbuild.view

import android.content.Intent
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.swipeUp
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.vase4kin.teamcityapp.TeamCityApplicationBase
import com.github.vase4kin.teamcityapp.agents.api.Agent
import com.github.vase4kin.teamcityapp.agents.api.Agents
import com.github.vase4kin.teamcityapp.api.TeamCityService
import com.github.vase4kin.teamcityapp.buildlist.api.Build
import com.github.vase4kin.teamcityapp.di.modules.FakeTeamCityServiceImpl
import com.github.vase4kin.teamcityapp.di.modules.Mocks
import com.github.vase4kin.teamcityapp.helper.CustomActivityTestRule
import com.github.vase4kin.teamcityapp.helper.HiltApiTestRule
import com.github.vase4kin.teamcityapp.helper.capture
import com.github.vase4kin.teamcityapp.navigation.IncomingDestinationResolver.Companion.RUN_BUILD_TYPE_ID
import com.github.vase4kin.teamcityapp.navigation.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import java.util.ArrayList
import okhttp3.ResponseBody
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.not
import org.hamcrest.Matchers.nullValue
import org.hamcrest.core.AllOf.allOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.Spy
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doSuspendableAnswer
import org.mockito.kotlin.verifyBlocking
import org.mockito.kotlin.whenever

private const val PARAMETER_NAME = "version"
private const val PARAMETER_VALUE = "1.3.2"

/**
 * Tests for the RunBuild root destination
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class RunBuildActivityTest {
    @get:Rule(order = 99)
    val phaseFourEvidence = com.github.vase4kin.teamcityapp.helper.PhaseFourEvidenceRule()

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

    @Rule(order = 3)
    @JvmField
    val activityRule: CustomActivityTestRule<MainActivity> =
        CustomActivityTestRule(MainActivity::class.java, "teamcityapp.features.run_build.impl.RunBuildActivity")

    @Captor
    lateinit var buildCaptor: ArgumentCaptor<Build>

    @Mock
    lateinit var responseBody: ResponseBody

    @Spy
    private val teamCityService: TeamCityService = FakeTeamCityServiceImpl()

    @Before
    fun setUp() {
        val app =
            InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TeamCityApplicationBase
        app.appInjector.sharedUserStorage().clearAll()
        app.appInjector.sharedUserStorage()
            .saveGuestUserAccountAndSetItAsActive(Mocks.URL, false)
    }

    @Test
    fun testUserCanSeeSingleBranchChosenIfBuildTypeHasSingleBranchAvailable() {
        // Prepare intent
        val intent = Intent()
        intent.putExtra(RUN_BUILD_TYPE_ID, "href")
        // Starting the activity
        activityRule.launchActivity(intent)
        // Check the branches autocomplete field has branch as master and it's disabled
        compose.onNodeWithTag("branches:input").assertTextEquals("master").assertIsNotEnabled()
    }

    @Test
    fun testUserCanStartTheBuild() {
        // Prepare intent
        val intent = Intent()
        intent.putExtra(RUN_BUILD_TYPE_ID, "href")
        // Starting the activity
        activityRule.launchActivity(intent)
        // Starting the build
        compose.onNodeWithTag("run-build:submit").performClick()
        // Checking triggered build
        verifyBlocking(teamCityService, org.mockito.Mockito.timeout(5_000)) { queueBuild(capture(buildCaptor)) }
        val capturedBuild = buildCaptor.value
        assertThat(capturedBuild.branchName, `is`("master"))
        assertThat(capturedBuild.agent, `is`(nullValue()))
        assertThat(capturedBuild.isPersonal, `is`(equalTo(false)))
        assertThat(capturedBuild.isQueueAtTop, `is`(equalTo(false)))
        assertThat(capturedBuild.isCleanSources, `is`(equalTo(true)))
        // Checking activity is finishing
        compose.waitUntil(5_000) { activityRule.activity.isFinishing }
        assertThat(activityRule.activity.isFinishing, `is`(true))
    }

    @Test
    fun testUserCanSeeChooseAgentIfAgentsAvailable() {
        // Prepare mocks
        val agents = ArrayList<Agent>()
        val agent = Agent("agent 1")
        agents.add(agent)
        doReturn(
            Agents(
                1,
                agents
            )
        ).whenever(teamCityService) { listAgents(false, null, null) }
        // Prepare intent
        val intent = Intent()
        intent.putExtra(RUN_BUILD_TYPE_ID, "href")
        // Starting the activity
        activityRule.launchActivity(intent)
        // Choose agent
        compose.onNodeWithTag("run-build:agent").performClick()
        compose.onNodeWithText("agent 1").performClick()
        // Starting the build
        compose.onNodeWithTag("run-build:submit").performClick()
        // Checking that build was triggered with agent
        verifyBlocking(teamCityService, org.mockito.Mockito.timeout(5_000)) { queueBuild(capture(buildCaptor)) }
        val capturedBuild = buildCaptor.value
        assertThat(capturedBuild.agent, `is`(agent))
        // Checking activity is finishing
        compose.waitUntil(5_000) { activityRule.activity.isFinishing }
        assertThat(activityRule.activity.isFinishing, `is`(true))
    }

    @Test
    fun testUserCanSeeChooseDefaultAgentIfAgentsAvailable() {
        // Prepare intent
        val intent = Intent()
        intent.putExtra(RUN_BUILD_TYPE_ID, "href")
        // Starting the activity
        activityRule.launchActivity(intent)
        // Check hint for selected agent
        compose.onNodeWithText(context.getString(teamcityapp.features.run_build.impl.R.string.hint_default_filter_agent)).assert(hasText(context.getString(teamcityapp.features.run_build.impl.R.string.hint_default_filter_agent)))
    }

    @Test
    fun testUserCanSeeNoAgentsAvailableTextIfNoAgentsAvailable() {
        // Prepare mocks
        doSuspendableAnswer { throw RuntimeException("error") }.whenever(teamCityService) { listAgents(false, null, null) }
        // Prepare intent
        val intent = Intent()
        intent.putExtra(RUN_BUILD_TYPE_ID, "href")
        // Starting the activity
        activityRule.launchActivity(intent)
        // Check no agents
        compose.onNodeWithText(context.getString(teamcityapp.features.run_build.impl.R.string.agents_unavailable)).assertIsDisplayed()
    }

    @Test
    fun testUserCleanAllFilesCheckBoxIsCheckedByDefault() {
        // Prepare intent
        val intent = Intent()
        intent.putExtra(RUN_BUILD_TYPE_ID, "href")
        // Starting the activity
        activityRule.launchActivity(intent)
        // Check clean all files is checked by default
        compose.onNodeWithTag("run-build:options").performClick()
        compose.onNodeWithTag("run-build:clean").performScrollTo().assertIsOn()
    }

    @Test
    fun testUserCanStartTheBuildWithDefaultParams() {
        // Prepare intent
        val intent = Intent()
        intent.putExtra(RUN_BUILD_TYPE_ID, "href")
        // Starting the activity
        activityRule.launchActivity(intent)
        // Check personal
        compose.onNodeWithTag("run-build:options").performClick()
        compose.onNodeWithTag("run-build:personal").performScrollTo().performClick()
        // Check queue to the top
        compose.onNodeWithTag("run-build:top").performScrollTo().performClick()
        // Check clean all files
        compose.onNodeWithTag("run-build:clean").performScrollTo().performClick()
        // Starting the build
        compose.onNodeWithTag("run-build:submit").performClick()
        // Checking triggered build
        verifyBlocking(teamCityService, org.mockito.Mockito.timeout(5_000)) { queueBuild(capture(buildCaptor)) }
        val capturedBuild = buildCaptor.value
        assertThat(capturedBuild.branchName, `is`("master"))
        assertThat(capturedBuild.agent, `is`(nullValue()))
        assertThat(capturedBuild.isPersonal, `is`(equalTo(true)))
        assertThat(capturedBuild.isQueueAtTop, `is`(equalTo(true)))
        assertThat(capturedBuild.isCleanSources, `is`(equalTo(false)))
        // Checking activity is finishing
        compose.waitUntil(5_000) { activityRule.activity.isFinishing }
        assertThat(activityRule.activity.isFinishing, `is`(true))
    }

    @Test
    fun testUserCanStartTheBuildWithCustomParams() {
        // Prepare intent
        val intent = Intent()
        intent.putExtra(RUN_BUILD_TYPE_ID, "href")
        // Starting the activity
        activityRule.launchActivity(intent)
        // Scroll to
        compose.onNodeWithTag("run-build:options").performClick()
        compose.onNodeWithTag("run-build:add").performScrollTo()
        // Add new param
        compose.onNodeWithTag("run-build:add").performScrollTo().performClick()
        // Fill params
        compose.onNodeWithTag("parameter:name").performTextInput(PARAMETER_NAME)
        compose.onNodeWithTag("parameter:value").performTextInput(PARAMETER_VALUE)
        // Add param
        compose.onNodeWithTag("parameter:confirm").performClick()
        // Scroll to
        compose.onNodeWithTag("run-build:add").performScrollTo()
        // Check params on view
        compose.onNodeWithText(PARAMETER_NAME).assert(hasText(PARAMETER_NAME))
        compose.onNodeWithText(PARAMETER_VALUE).assert(hasText(PARAMETER_VALUE))
        // Starting the build
        compose.onNodeWithTag("run-build:submit").performClick()
        // Checking triggered build
        verifyBlocking(teamCityService, org.mockito.Mockito.timeout(5_000)) { queueBuild(capture(buildCaptor)) }
        val capturedBuild = buildCaptor.value
        assertThat(capturedBuild.properties!!.properties.size, `is`(equalTo(1)))
        val capturedProperty = capturedBuild.properties!!.properties[0]
        assertThat(capturedProperty.name, `is`(equalTo(PARAMETER_NAME)))
        assertThat(capturedProperty.value, `is`(equalTo(PARAMETER_VALUE)))
        // Checking activity is finishing
        compose.waitUntil(5_000) { activityRule.activity.isFinishing }
        assertThat(activityRule.activity.isFinishing, `is`(true))
    }

    @Test
    fun testUserCanStartTheBuildWithClearAllCustomParams() {
        // Prepare intent
        val intent = Intent()
        intent.putExtra(RUN_BUILD_TYPE_ID, "href")
        // Starting the activity
        activityRule.launchActivity(intent)
        // Scroll to
        compose.onNodeWithTag("run-build:options").performClick()
        compose.onNodeWithTag("run-build:add").performScrollTo()
        // Add new param
        compose.onNodeWithTag("run-build:add").performScrollTo().performClick()
        // Fill params
        compose.onNodeWithTag("parameter:name").performTextInput(PARAMETER_NAME)
        compose.onNodeWithTag("parameter:value").performTextInput(PARAMETER_VALUE)
        // Add param
        compose.onNodeWithTag("parameter:confirm").performClick()
        // Scroll to
        compose.onNodeWithTag("run-build:add").performScrollTo()
        // Check params on view
        compose.onNodeWithText(PARAMETER_NAME).assert(hasText(PARAMETER_NAME))
        compose.onNodeWithText(PARAMETER_VALUE).assert(hasText(PARAMETER_VALUE))
        // Clear all params
        compose.onNodeWithTag("run-build:clear").performScrollTo().performClick()
        // Starting the build
        compose.onNodeWithTag("run-build:submit").performClick()
        // Checking triggered build
        verifyBlocking(teamCityService, org.mockito.Mockito.timeout(5_000)) { queueBuild(capture(buildCaptor)) }
        val capturedBuild = buildCaptor.value
        assertThat(capturedBuild.properties, `is`(nullValue()))
        // Checking activity is finishing
        compose.waitUntil(5_000) { activityRule.activity.isFinishing }
        assertThat(activityRule.activity.isFinishing, `is`(true))
    }

    @Test
    fun testUserCanNotCreateEmptyBuildParamWithEmptyName() {
        // Prepare intent
        val intent = Intent()
        intent.putExtra(RUN_BUILD_TYPE_ID, "href")
        // Starting the activity
        activityRule.launchActivity(intent)
        // Scroll to
        compose.onNodeWithTag("run-build:options").performClick()
        compose.onNodeWithTag("run-build:add").performScrollTo()
        // Add new param
        compose.onNodeWithTag("run-build:add").performScrollTo().performClick()
        // Fill params
        compose.onNodeWithTag("parameter:name").performTextInput("")
        compose.onNodeWithTag("parameter:value").performTextInput(PARAMETER_VALUE)
        // Add param
        compose.onNodeWithTag("parameter:confirm").performClick()
        // Check error
        compose.onNodeWithText(context.getString(teamcityapp.features.run_build.impl.R.string.text_error_parameter_name)).assertIsDisplayed()
    }
}
