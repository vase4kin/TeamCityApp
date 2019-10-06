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

package com.github.vase4kin.teamcityapp.home.view

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasData
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.TeamCityApplication
import com.github.vase4kin.teamcityapp.api.TeamCityService
import com.github.vase4kin.teamcityapp.dagger.components.AppComponent
import com.github.vase4kin.teamcityapp.dagger.components.RestApiComponent
import com.github.vase4kin.teamcityapp.dagger.modules.AppModule
import com.github.vase4kin.teamcityapp.dagger.modules.FakeTeamCityServiceImpl
import com.github.vase4kin.teamcityapp.dagger.modules.Mocks
import com.github.vase4kin.teamcityapp.dagger.modules.RestApiModule
import com.github.vase4kin.teamcityapp.helper.CustomIntentsTestRule
import com.github.vase4kin.teamcityapp.helper.RecyclerViewMatcher.Companion.withRecyclerView
import com.github.vase4kin.teamcityapp.helper.TestUtils
import com.github.vase4kin.teamcityapp.navigation.api.BuildType
import com.github.vase4kin.teamcityapp.navigation.api.BuildTypes
import com.github.vase4kin.teamcityapp.navigation.api.NavigationNode
import com.github.vase4kin.teamcityapp.navigation.api.Project
import com.github.vase4kin.teamcityapp.navigation.api.Projects
import com.github.vase4kin.teamcityapp.remote.RemoteService
import io.reactivex.Single
import it.cosenonjaviste.daggermock.DaggerMockRule
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.core.AllOf.allOf
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Matchers.anyString
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Spy

/**
 * Tests for [HomeActivity]
 */
@RunWith(AndroidJUnit4::class)
class RateTheAppTest {

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

    @Mock
    lateinit var remoteService: RemoteService

    private val sharedPreferences: SharedPreferences
        get() {
            val app =
                InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TeamCityApplication
            return app.getSharedPreferences("rateTheAppPref", Context.MODE_PRIVATE)
        }

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
        app.appInjector.sharedUserStorage().clearAll()
        app.restApiInjector.sharedUserStorage()
            .saveGuestUserAccountAndSetItAsActive(Mocks.URL, false)
        sharedPreferences.edit().clear().commit()
    }

    @Test
    fun testUserCanSeeRateTheApp() {
        // Prepare data
        val project = Project()
        project.name = "New project"
        project.description = "Contains a lof of projects"
        val buildType = BuildType()
        buildType.setId("build_type_id")
        buildType.name = "Build and run tests"
        val navigationNode = NavigationNode(
            Projects(listOf(project)),
            BuildTypes(listOf(buildType))
        )
        `when`(teamCityService.listBuildTypes(anyString())).thenCallRealMethod()
            .thenReturn(Single.just(navigationNode))
        `when`(remoteService.isNotChurn()).thenReturn(true)

        activityRule.launchActivity(null)

        // Checking data
        onView(withId(R.id.navigation_recycler_view)).check(TestUtils.hasItemsCount(3))
        onView(withRecyclerView(R.id.navigation_recycler_view).atPositionOnView(0, R.id.itemTitle))
            .check(matches(withText("Project")))
        onView(
            withRecyclerView(R.id.navigation_recycler_view).atPositionOnView(
                0,
                R.id.itemSubTitle
            )
        )
            .check(matches(withText("Description")))
        onView(withRecyclerView(R.id.navigation_recycler_view).atPositionOnView(1, R.id.title))
            .check(matches(withText(R.string.rate_title)))
        onView(
            withRecyclerView(R.id.navigation_recycler_view).atPositionOnView(
                1,
                R.id.subTitle
            )
        )
            .check(matches(withText(R.string.rate_description)))

        onView(withRecyclerView(R.id.navigation_recycler_view).atPositionOnView(2, R.id.itemTitle))
            .check(matches(withText("build type")))
    }

    @Test
    fun testUserCanCancelRateTheApp() {
        // Prepare data
        val project = Project()
        project.name = "New project"
        project.description = "Contains a lof of projects"
        val buildType = BuildType()
        buildType.setId("build_type_id")
        buildType.name = "Build and run tests"
        val navigationNode = NavigationNode(
            Projects(listOf(project)),
            BuildTypes(listOf(buildType))
        )
        `when`(teamCityService.listBuildTypes(anyString())).thenCallRealMethod()
            .thenReturn(Single.just(navigationNode))
        `when`(remoteService.isNotChurn()).thenReturn(true)

        activityRule.launchActivity(null)

        // Cancel rate the app
        onView(withId(R.id.button_cancel)).perform(click())

        // Check the flag is set
        assertThat(sharedPreferences.getBoolean("rated", false), `is`(equalTo(true)))

        // Checking data
        onView(withId(R.id.navigation_recycler_view)).check(TestUtils.hasItemsCount(2))
        onView(withRecyclerView(R.id.navigation_recycler_view).atPositionOnView(0, R.id.itemTitle))
            .check(matches(withText("Project")))
        onView(
            withRecyclerView(R.id.navigation_recycler_view).atPositionOnView(
                0,
                R.id.itemSubTitle
            )
        )
            .check(matches(withText("Description")))
        onView(withRecyclerView(R.id.navigation_recycler_view).atPositionOnView(1, R.id.itemTitle))
            .check(matches(withText("build type")))

        // Click on projects
        onView(withText("Project"))
            .perform(click())

        // Check Project data
        onView(withId(R.id.navigation_recycler_view)).check(TestUtils.hasItemsCount(2))
        onView(withRecyclerView(R.id.navigation_recycler_view).atPositionOnView(0, R.id.itemTitle))
            .check(matches(withText("New project")))
        onView(
            withRecyclerView(R.id.navigation_recycler_view).atPositionOnView(
                0,
                R.id.itemSubTitle
            )
        )
            .check(matches(withText("Contains a lof of projects")))
        onView(withRecyclerView(R.id.navigation_recycler_view).atPositionOnView(1, R.id.itemTitle))
            .check(matches(withText("Build and run tests")))
    }

    @Ignore
    @Test
    fun testUserCanRateTheApp() {
        // Prepare data
        val project = Project()
        project.name = "New project"
        project.description = "Contains a lof of projects"
        val buildType = BuildType()
        buildType.setId("build_type_id")
        buildType.name = "Build and run tests"
        val navigationNode = NavigationNode(
            Projects(listOf(project)),
            BuildTypes(listOf(buildType))
        )
        `when`(teamCityService.listBuildTypes(anyString())).thenCallRealMethod()
            .thenReturn(Single.just(navigationNode))
        `when`(remoteService.isNotChurn()).thenReturn(true)

        activityRule.launchActivity(null)

        // Rate the app
        onView(withId(R.id.button_rate)).perform(click())

        // Check the flag is set
        assertThat(sharedPreferences.getBoolean("rated", false), `is`(equalTo(true)))

        // Check filter builds activity is opened
        intended(
            allOf(
                hasAction(Intent.ACTION_VIEW),
                hasData("market://details?id=com.github.vase4kin.teamcityapp.mock.debug")
            )
        )
    }

    @Test
    fun testUserCanCancelRateTheApp_Regression() {
        // Prepare data
        val project = Project()
        project.name = "New project"
        project.description = "Contains a lof of projects"
        val buildType = BuildType()
        buildType.setId("build_type_id")
        buildType.name = "Build and run tests"
        val navigationNode = NavigationNode(
            Projects(listOf(project)),
            BuildTypes(listOf(buildType))
        )
        `when`(teamCityService.listBuildTypes(anyString())).thenCallRealMethod()
            .thenReturn(Single.just(navigationNode))
        `when`(remoteService.isNotChurn()).thenReturn(true)

        activityRule.launchActivity(null)

        // Cancel rate the app
        onView(withId(R.id.button_cancel)).perform(click())

        // Check the flag is set
        assertThat(sharedPreferences.getBoolean("rated", false), `is`(equalTo(true)))

        // Checking data
        onView(withId(R.id.navigation_recycler_view)).check(TestUtils.hasItemsCount(2))
        onView(withRecyclerView(R.id.navigation_recycler_view).atPositionOnView(0, R.id.itemTitle))
            .check(matches(withText("Project")))
        onView(
            withRecyclerView(R.id.navigation_recycler_view).atPositionOnView(
                0,
                R.id.itemSubTitle
            )
        )
            .check(matches(withText("Description")))
        onView(withRecyclerView(R.id.navigation_recycler_view).atPositionOnView(1, R.id.itemTitle))
            .check(matches(withText("build type")))

        // Click on projects
        onView(withText("build type"))
            .perform(click())

        // no crash
        // user can see buildtype
        // Check toolbar
        TestUtils.matchToolbarTitle("build type")
    }

    @Test
    fun testUserCanNotSeeRateTheAppIfProjectIsEmpty() {
        // Prepare data
        val navigationNode = NavigationNode(
            Projects(emptyList()),
            BuildTypes(emptyList())
        )
        `when`(teamCityService.listBuildTypes(anyString())).thenReturn(Single.just(navigationNode))
        `when`(remoteService.isNotChurn()).thenReturn(true)

        activityRule.launchActivity(null)

        // Check showing empty
        onView(withId(R.id.empty_title))
            .check(matches(isDisplayed()))
            .check(matches(withText(R.string.empty_list_message_projects_or_build_types)))
    }
}
