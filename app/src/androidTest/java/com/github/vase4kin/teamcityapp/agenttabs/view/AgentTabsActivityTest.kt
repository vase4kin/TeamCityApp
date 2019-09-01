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

package com.github.vase4kin.teamcityapp.agenttabs.view

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
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
import com.github.vase4kin.teamcityapp.helper.CustomActivityTestRule
import com.github.vase4kin.teamcityapp.helper.RecyclerViewMatcher.withRecyclerView
import com.github.vase4kin.teamcityapp.helper.TestUtils.hasItemsCount
import com.github.vase4kin.teamcityapp.helper.TestUtils.matchToolbarTitle
import it.cosenonjaviste.daggermock.DaggerMockRule
import org.hamcrest.core.AllOf.allOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Spy

/**
 * Tests for [AgentTabsActivity]
 */
@RunWith(AndroidJUnit4::class)
class AgentTabsActivityTest {

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
    val activityRule: CustomActivityTestRule<AgentTabsActivity> =
        CustomActivityTestRule(AgentTabsActivity::class.java)

    @Spy
    private val teamCityService: TeamCityService = FakeTeamCityServiceImpl()

    @Before
    fun setUp() {
        val app =
            InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TeamCityApplication
        app.restApiInjector.sharedUserStorage().clearAll()
        app.restApiInjector.sharedUserStorage()
            .saveGuestUserAccountAndSetItAsActive(Mocks.URL, false)
    }

    @Test
    fun testUserCanSeeSuccessfullyLoadedAgents() {
        activityRule.launchActivity(null)

        // checking toolbar title
        matchToolbarTitle("Agents")
        // checking connected tab title
        onView(withText("Connected (3)"))
            .check(matches(isDisplayed()))
        // checking connected
        onView(allOf(withId(R.id.connected_agents_recycler_view), isDisplayed()))
            .check(hasItemsCount(3))
        onView(
            allOf(
                withRecyclerView(R.id.connected_agents_recycler_view).atPositionOnView(
                    0,
                    R.id.itemTitle
                ), isDisplayed()
            )
        )
            .check(matches(withText("agent 1")))
        onView(
            withRecyclerView(R.id.connected_agents_recycler_view).atPositionOnView(
                1,
                R.id.itemTitle
            )
        )
            .check(matches(withText("agent 2")))
        onView(
            withRecyclerView(R.id.connected_agents_recycler_view).atPositionOnView(
                2,
                R.id.itemTitle
            )
        )
            .check(matches(withText("agent 3")))

        // checking disconnected tab title
        onView(withText("Disconnected (1)"))
            .check(matches(isDisplayed()))
            .perform(click())
        // checking disconnected
        onView(allOf(withId(R.id.disconnected_agents_recycler_view), isDisplayed()))
            .check(hasItemsCount(1))
        onView(
            withRecyclerView(R.id.disconnected_agents_recycler_view).atPositionOnView(
                0,
                R.id.itemTitle
            )
        )
            .check(matches(withText("Mac mini 3434")))
    }
}
