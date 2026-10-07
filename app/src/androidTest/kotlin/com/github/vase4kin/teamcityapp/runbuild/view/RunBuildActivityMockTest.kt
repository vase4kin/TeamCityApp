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

package com.github.vase4kin.teamcityapp.runbuild.view

import android.content.Intent
import android.view.View
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.TeamCityApplicationBase
import com.github.vase4kin.teamcityapp.api.TeamCityService
import com.github.vase4kin.teamcityapp.buildlist.api.Build
import com.github.vase4kin.teamcityapp.dagger.modules.Mocks
import com.github.vase4kin.teamcityapp.helper.CustomActivityTestRule
import com.github.vase4kin.teamcityapp.helper.HiltApiTestRule
import com.github.vase4kin.teamcityapp.helper.any
import com.github.vase4kin.teamcityapp.runbuild.api.Branch
import com.github.vase4kin.teamcityapp.runbuild.api.Branches
import com.github.vase4kin.teamcityapp.runbuild.interactor.CODE_FORBIDDEN
import com.github.vase4kin.teamcityapp.runbuild.interactor.EXTRA_BUILD_TYPE_ID
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.reactivex.Single
import java.util.ArrayList
import okhttp3.ResponseBody
import org.hamcrest.Matchers.instanceOf
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.not
import org.hamcrest.core.AllOf.allOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Mockito.`when`
import retrofit2.HttpException
import retrofit2.Response
import teamcityapp.features.run_build.impl.RunBuildActivity

/**
 * Tests for [RunBuildActivity]
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class RunBuildActivityMockTest {
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
    val activityRule: CustomActivityTestRule<RunBuildActivity> =
        CustomActivityTestRule(RunBuildActivity::class.java)

    @Mock
    lateinit var responseBody: ResponseBody

    @Mock
    lateinit var teamCityService: TeamCityService

    @Before
    fun setUp() {
        val app =
            InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TeamCityApplicationBase
        app.appInjector.sharedUserStorage().clearAll()
        app.appInjector.sharedUserStorage()
            .saveGuestUserAccountAndSetItAsActive(Mocks.URL, false)
    }

    @Test
    fun testUserCanSeeErrorForbiddenSnackBarIfServerReturnsAnError() {
        // Prepare mocks
        val httpException = HttpException(Response.error<Build>(CODE_FORBIDDEN, responseBody))
        `when`(teamCityService.queueBuild(any())).thenReturn(
            Single.error(
                httpException
            )
        )
        `when`(teamCityService.listAgents(any(), any(), any())).thenReturn(Single.just(Mocks.connectedAgents()))
        `when`(teamCityService.listBranches(any())).thenReturn(Single.just(Branches(emptyList())))
        `when`(teamCityService.buildType(any())).thenReturn(Single.just(Mocks.buildTypeMock()))
        // Prepare intent
        val intent = Intent()
        intent.putExtra(EXTRA_BUILD_TYPE_ID, "href")
        // Starting the activity
        activityRule.launchActivity(intent)
        // Starting the build
        compose.onNodeWithTag("run-build:submit").performClick()
        // Checking the error snackbar text
        compose.onNodeWithText(context.getString(teamcityapp.features.run_build.impl.R.string.error_forbidden_error)).assertIsDisplayed()
    }

    @Test
    fun testUserCanSeeErrorSnackBarIfServerReturnsAnError() {
        // Prepare mocks
        `when`(teamCityService.queueBuild(any())).thenReturn(
            Single.error(
                RuntimeException("error")
            )
        )
        `when`(teamCityService.listAgents(any(), any(), any())).thenReturn(Single.just(Mocks.connectedAgents()))
        `when`(teamCityService.listBranches(any())).thenReturn(Single.just(Branches(emptyList())))
        `when`(teamCityService.buildType(any())).thenReturn(Single.just(Mocks.buildTypeMock()))
        // Prepare intent
        val intent = Intent()
        intent.putExtra(EXTRA_BUILD_TYPE_ID, "href")
        // Starting the activity
        activityRule.launchActivity(intent)
        // Starting the build
        compose.onNodeWithTag("run-build:submit").performClick()
        // Checking the error snackbar text
        compose.onNodeWithText(context.getString(teamcityapp.features.run_build.impl.R.string.error_base_error)).assertIsDisplayed()
    }

    @Test
    fun testUserCanSeeMultipleBranchesIfBuildTypeHasMultipleAvailable() {
        // Prepare mocks
        val branches = ArrayList<Branch>()
        branches.add(Branch("dev1"))
        branches.add(Branch("dev2"))
        `when`(teamCityService.listBranches(anyString())).thenReturn(Single.just(Branches(branches)))
        `when`(teamCityService.listAgents(any(), any(), any())).thenReturn(Single.just(Mocks.connectedAgents()))
        `when`(teamCityService.buildType(any())).thenReturn(Single.just(Mocks.buildTypeMock()))
        // Prepare intent
        val intent = Intent()
        intent.putExtra(EXTRA_BUILD_TYPE_ID, "href")
        // Starting the activity
        activityRule.launchActivity(intent)
        // Choose branch from autocomplete and verify it is appeared
        compose.onNodeWithTag("branches:input")
            .performTextInput("dev")

        compose.onNodeWithText("dev1").performClick()
        compose.onNodeWithTag("branches:input").assertTextEquals("dev1").assertIsEnabled()
    }
}
