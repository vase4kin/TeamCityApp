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
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
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
import com.github.vase4kin.teamcityapp.TeamCityApplication
import com.github.vase4kin.teamcityapp.api.TeamCityService
import com.github.vase4kin.teamcityapp.buildlist.api.Build
import com.github.vase4kin.teamcityapp.dagger.components.AppComponent
import com.github.vase4kin.teamcityapp.dagger.components.RestApiComponent
import com.github.vase4kin.teamcityapp.dagger.modules.AppModule
import com.github.vase4kin.teamcityapp.dagger.modules.Mocks
import com.github.vase4kin.teamcityapp.dagger.modules.RestApiModule
import com.github.vase4kin.teamcityapp.helper.CustomActivityTestRule
import com.github.vase4kin.teamcityapp.helper.any
import com.github.vase4kin.teamcityapp.runbuild.api.Branch
import com.github.vase4kin.teamcityapp.runbuild.api.Branches
import com.github.vase4kin.teamcityapp.runbuild.interactor.CODE_FORBIDDEN
import com.github.vase4kin.teamcityapp.runbuild.interactor.EXTRA_BUILD_TYPE_ID
import io.reactivex.Single
import it.cosenonjaviste.daggermock.DaggerMockRule
import okhttp3.ResponseBody
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.instanceOf
import org.hamcrest.Matchers.not
import org.hamcrest.core.AllOf.allOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Matchers.anyString
import org.mockito.Mock
import org.mockito.Mockito.`when`
import retrofit2.HttpException
import retrofit2.Response
import java.util.ArrayList

/**
 * Tests for [RunBuildActivity]
 */
@RunWith(AndroidJUnit4::class)
class RunBuildActivityMockTest {

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
    val activityRule: CustomActivityTestRule<RunBuildActivity> =
        CustomActivityTestRule(RunBuildActivity::class.java)

    @Mock
    lateinit var responseBody: ResponseBody

    @Mock
    lateinit var teamCityService: TeamCityService

    @Before
    fun setUp() {
        val app =
            InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TeamCityApplication
        app.restApiInjector.sharedUserStorage().clearAll()
        app.restApiInjector.sharedUserStorage()
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
        onView(withId(R.id.fab_queue_build)).perform(click())
        // Checking the error snackbar text
        onView(withText(R.string.error_forbidden_error)).check(matches(isDisplayed()))
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
        onView(withId(R.id.fab_queue_build)).perform(click())
        // Checking the error snackbar text
        onView(withText(R.string.error_base_error)).check(matches(isDisplayed()))
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
        onView(withId(R.id.autocomplete_branches))
            .perform(typeText("dev"))
        onData(allOf(`is`(instanceOf<Any>(String::class.java)), `is`<String>("dev1")))
            .inRoot(RootMatchers.withDecorView(not(`is`<View>(activityRule.activity.window.decorView))))
            .perform(click())

        onView(withText("dev1")).perform(click())
        onView(withId(R.id.autocomplete_branches)).check(
            matches(
                allOf(
                    withText("dev1"),
                    isEnabled()
                )
            )
        )
    }
}
