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

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
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
import io.reactivex.Single
import it.cosenonjaviste.daggermock.DaggerMockRule
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Matchers.anyString
import org.mockito.Mockito.`when`
import org.mockito.Spy

/**
 * Tests for [HomeActivity]
 */
@RunWith(AndroidJUnit4::class)
class HomeActivityTest {

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
    }

    @Test
    fun testUserCanSeeProjectsData() {
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

        activityRule.launchActivity(null)

        // Checking toolbar title
        TestUtils.matchToolbarTitle("Projects")

        // Checking projects data
        onView(withId(R.id.navigation_recycler_view)).check(TestUtils.hasItemsCount(2))
        onView(withRecyclerView(R.id.navigation_recycler_view).atPositionOnView(0, R.id.title))
            .check(matches(withText("Project")))
        onView(
            withRecyclerView(R.id.navigation_recycler_view).atPositionOnView(
                0,
                R.id.subTitle
            )
        )
            .check(matches(withText("Description")))
        onView(withRecyclerView(R.id.navigation_recycler_view).atPositionOnView(1, R.id.title))
            .check(matches(withText("build type")))
        // Click on projects
        onView(withText("Project"))
            .perform(click())
        // Check toolbar
        TestUtils.matchToolbarTitle("Project")
        // Check Project data
        onView(withId(R.id.navigation_recycler_view)).check(TestUtils.hasItemsCount(2))
        onView(withRecyclerView(R.id.navigation_recycler_view).atPositionOnView(0, R.id.title))
            .check(matches(withText("New project")))
        onView(
            withRecyclerView(R.id.navigation_recycler_view).atPositionOnView(
                0,
                R.id.subTitle
            )
        )
            .check(matches(withText("Contains a lof of projects")))
        onView(withRecyclerView(R.id.navigation_recycler_view).atPositionOnView(1, R.id.title))
            .check(matches(withText("Build and run tests")))
    }
}
