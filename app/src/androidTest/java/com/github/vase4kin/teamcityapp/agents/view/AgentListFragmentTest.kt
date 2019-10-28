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

package com.github.vase4kin.teamcityapp.agents.view

import android.content.Context
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withChild
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.TeamCityApplication
import com.github.vase4kin.teamcityapp.agents.api.Agents
import com.github.vase4kin.teamcityapp.api.TeamCityService
import com.github.vase4kin.teamcityapp.dagger.components.AppComponent
import com.github.vase4kin.teamcityapp.dagger.components.RestApiComponent
import com.github.vase4kin.teamcityapp.dagger.modules.AppModule
import com.github.vase4kin.teamcityapp.dagger.modules.FakeTeamCityServiceImpl
import com.github.vase4kin.teamcityapp.dagger.modules.Mocks
import com.github.vase4kin.teamcityapp.dagger.modules.RestApiModule
import com.github.vase4kin.teamcityapp.helper.CustomActivityTestRule
import com.github.vase4kin.teamcityapp.helper.RecyclerViewMatcher.Companion.withRecyclerView
import com.github.vase4kin.teamcityapp.helper.TestUtils
import com.github.vase4kin.teamcityapp.helper.TestUtils.Companion.hasItemsCount
import com.github.vase4kin.teamcityapp.helper.TestUtils.Companion.matchHomeToolbarTitle
import com.github.vase4kin.teamcityapp.helper.any
import com.github.vase4kin.teamcityapp.home.view.HomeActivity
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage
import io.reactivex.Single
import it.cosenonjaviste.daggermock.DaggerMockRule
import org.hamcrest.core.AllOf.allOf
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.`when`
import org.mockito.Spy

@RunWith(AndroidJUnit4::class)
class AgentListFragmentTest {

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
    var activityRule = CustomActivityTestRule(HomeActivity::class.java)

    @Spy
    private val teamCityService: TeamCityService = FakeTeamCityServiceImpl()

    private val storage: SharedUserStorage
        get() {
            val app =
                InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TeamCityApplication
            return app.appInjector.sharedUserStorage()
        }

    private val context: Context
        get() = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext

    companion object {
        @JvmStatic
        @BeforeClass
        fun disableOnboarding() {
            TestUtils.disableOnboarding()
        }
    }

    @Before
    fun setUp() {
        val storage = storage
        storage.clearAll()
        storage.saveGuestUserAccountAndSetItAsActive(Mocks.URL, false)
    }

    @Test
    fun testUserCanSeeUseFiltersToLoadBuilds() {
        activityRule.launchActivity(null)

        // Click on agents tab
        clickOnAgentsTab()

        // Check badge
        checkAgentsTabBadgeCount("3")

        // checking connected
        onView(allOf(withId(R.id.agents_recycler_view), isDisplayed()))
            .check(hasItemsCount(3))
        onView(
            allOf(
                withRecyclerView(R.id.agents_recycler_view).atPositionOnView(
                    0,
                    R.id.title
                ), isDisplayed()
            )
        )
            .check(matches(withText("agent 1")))
        onView(
            withRecyclerView(R.id.agents_recycler_view).atPositionOnView(
                1,
                R.id.title
            )
        )
            .check(matches(withText("agent 2")))
        onView(
            withRecyclerView(R.id.agents_recycler_view).atPositionOnView(
                2,
                R.id.title
            )
        )
            .check(matches(withText("agent 3")))

        // filter agents to show disconnected
        onView(allOf(withId(R.id.home_floating_action_button), isDisplayed())).perform(click())
        onView(withText(R.string.text_show_disconnected)).perform(click())

        // check snack bar text
        onView(withText(R.string.text_agents_filters_applied)).check(matches(isDisplayed()))

        // Check badge
        checkAgentsTabBadgeCount("1")

        // checking disconnected
        onView(allOf(withId(R.id.agents_recycler_view), isDisplayed()))
            .check(hasItemsCount(1))
        onView(
            withRecyclerView(R.id.agents_recycler_view).atPositionOnView(
                0,
                R.id.title
            )
        )
            .check(matches(withText("Mac mini 3434")))
    }

    @Test
    fun testUserCanSeeUpdatedToolbar() {
        activityRule.launchActivity(null)

        // Click on build queue tab
        clickOnAgentsTab()

        val toolbarTitle = context.getString(R.string.agents_drawer_item)
        matchHomeToolbarTitle(toolbarTitle)
    }

    @Test
    fun testUserCanSeeFailureMessageForConnectedAgents() {
        // Prepare data
        `when`(
            teamCityService.listAgents(
                any(),
                any(),
                any()
            )
        ).thenReturn(Single.error(RuntimeException("smth bad happend!")))

        activityRule.launchActivity(null)

        // Click on agents tab
        clickOnAgentsTab()

        checkAgentsTabBadgeCount("0")

        onView(withText(R.string.error_view_error_text)).check(matches(isDisplayed()))

        // filter builds to show all
        onView(allOf(withId(R.id.home_floating_action_button), isDisplayed())).perform(click())
        onView(withText(R.string.text_show_disconnected)).perform(click())

        checkAgentsTabBadgeCount("0")

        onView(withText(R.string.error_view_error_text)).check(matches(isDisplayed()))
    }

    @Test
    fun testUserCanSeeFailureMessageForDisconnectedAgents() {
        // Prepare data
        `when`(
            teamCityService.listAgents(
                any(),
                any(),
                any()
            )
        ).thenReturn(Single.error(RuntimeException("smth bad happend!")))

        activityRule.launchActivity(null)

        // Click on agents tab
        clickOnAgentsTab()

        checkAgentsTabBadgeCount("0")

        onView(withText(R.string.error_view_error_text)).check(matches(isDisplayed()))
    }

    @Test
    fun testUserCanSeeEmptyDataMessageIfBuildQueueIsEmpty() {
        `when`(
            teamCityService.listAgents(
                any(),
                any(),
                any()
            )
        ).thenReturn(Single.just(Agents(0, emptyList())))

        activityRule.launchActivity(null)

        // Click on agents tab
        clickOnAgentsTab()

        checkAgentsTabBadgeCount("0")
        onView(withId(R.id.agents_empty_title_view)).check(matches(isDisplayed()))
            .check(matches(withText(R.string.empty_list_message_agents)))

        // filter builds to show all
        onView(allOf(withId(R.id.home_floating_action_button), isDisplayed())).perform(click())
        onView(withText(R.string.text_show_disconnected)).perform(click())

        checkAgentsTabBadgeCount("0")
        onView(withId(R.id.agents_empty_title_view)).check(matches(isDisplayed()))
            .check(matches(withText(R.string.empty_list_message_agents_disconnected)))
    }

    private fun clickOnAgentsTab() {
        onView(
            withChild(
                allOf(
                    withId(R.id.bottom_navigation_small_item_title),
                    withText(R.string.agents_drawer_item)
                )
            )
        )
            .perform(click())
    }

    private fun checkAgentsTabBadgeCount(count: String) {
        onView(
            allOf(
                withChild(allOf(withId(R.id.bottom_navigation_notification), withText(count))),
                withChild(
                    allOf(
                        withId(R.id.bottom_navigation_small_item_title),
                        withText(R.string.agents_drawer_item)
                    )
                )
            )
        )
            .check(matches(isDisplayed()))
    }
}
