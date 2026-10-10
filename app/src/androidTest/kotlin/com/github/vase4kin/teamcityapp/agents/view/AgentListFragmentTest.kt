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

package com.github.vase4kin.teamcityapp.agents.view

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.TeamCityApplicationBase
import com.github.vase4kin.teamcityapp.agents.api.Agents
import com.github.vase4kin.teamcityapp.api.TeamCityService
import com.github.vase4kin.teamcityapp.dagger.modules.FakeTeamCityServiceImpl
import com.github.vase4kin.teamcityapp.dagger.modules.Mocks
import com.github.vase4kin.teamcityapp.helper.CustomActivityTestRule
import com.github.vase4kin.teamcityapp.helper.HiltApiTestRule
import com.github.vase4kin.teamcityapp.helper.TestUtils
import com.github.vase4kin.teamcityapp.helper.any
import com.github.vase4kin.teamcityapp.home.view.HomeActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.reactivex.Single
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import org.hamcrest.core.AllOf.allOf
import org.junit.*
import org.junit.runner.RunWith
import org.mockito.Mockito.`when`
import org.mockito.Spy
import teamcityapp.features.agents.impl.R as AgentsR
import teamcityapp.features.filter_bottom_sheet.impl.R as FilterR

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class AgentListFragmentTest {
    @JvmField
    @Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @JvmField
    @Rule(order = 1)
    val mockitoRule = org.mockito.junit.MockitoJUnit.rule().strictness(org.mockito.quality.Strictness.LENIENT)

    @JvmField
    @Rule(order = 2)
    val apiRule = HiltApiTestRule(hiltRule) { teamCityService }

    @JvmField
    @Rule(order = 3)
    val activityRule = CustomActivityTestRule(HomeActivity::class.java)

    @JvmField
    @Rule(order = 4)
    val compose = createEmptyComposeRule()

    @Spy private val teamCityService: TeamCityService = FakeTeamCityServiceImpl()

    companion object {
        @JvmStatic @BeforeClass
        fun disableOnboarding() = TestUtils.disableOnboarding()
    }

    @Before fun setUp() {
        val app = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TeamCityApplicationBase
        app.appInjector.sharedUserStorage().apply {
            clearAll()
            saveGuestUserAccountAndSetItAsActive(Mocks.URL, false)
        }
    }

    @Test fun filtersReplaceConnectedRowsWithDisconnectedRows() {
        openAgents()
        assertTextVisible("agent 1")
        compose.onNodeWithText("agent 2").assertIsDisplayed()
        compose.onNodeWithText("agent 3").assertIsDisplayed()
        showDisconnected()
        assertTextVisible("Mac mini 3434")
        compose.onNodeWithText("agent 1").assertDoesNotExist()
        compose.onNodeWithText("agent 2").assertDoesNotExist()
        compose.onNodeWithText("agent 3").assertDoesNotExist()
    }

    @Test fun showsTheAgentsToolbarAndOpensTheDrawer() {
        openAgents()
        assertTextVisible(text(AgentsR.string.agents_title))
        compose.onNodeWithTag("agents:drawer").performClick()
        compose.onNodeWithTag("drawer:list").assertExists()
    }

    @Test fun failureCanBeRetriedWithoutRecreatingTheFragment() {
        `when`(teamCityService.listAgents(any(), any(), any())).thenReturn(Single.error(RuntimeException("offline")))
        openAgents()
        assertTextVisible(text(teamcityapp.libraries.theme.R.string.error_load_message))
        `when`(teamCityService.listAgents(any(), any(), any())).thenReturn(Single.just(Agents(1, listOf(com.github.vase4kin.teamcityapp.agents.api.Agent("Recovered")))))
        compose.onNodeWithText(text(teamcityapp.libraries.theme.R.string.action_retry)).performClick()
        assertTextVisible("Recovered")
    }

    @Test fun emptyMessageFollowsTheSelectedFilter() {
        `when`(teamCityService.listAgents(any(), any(), any())).thenReturn(Single.just(Agents(0, emptyList())))
        openAgents()
        assertTextVisible(text(AgentsR.string.agents_empty_connected))
        showDisconnected()
        assertTextVisible(text(AgentsR.string.agents_empty_disconnected))
        compose.onNodeWithText(text(AgentsR.string.agents_empty_connected)).assertDoesNotExist()
    }

    @Test fun returningToTheAgentsTabReloadsItsCompletedList() {
        val calls = AtomicInteger()
        `when`(teamCityService.listAgents(false, null, null)).thenAnswer {
            calls.incrementAndGet()
            Single.just(Agents(1, listOf(com.github.vase4kin.teamcityapp.agents.api.Agent("Visible agent"))))
        }
        openAgents()
        assertTextVisible("Visible agent")
        Assert.assertEquals(1, calls.get())
        selectTab(R.id.favorites)
        selectTab(R.id.agents)
        compose.waitUntil(10_000) { calls.get() >= 2 }
        Assert.assertEquals(2, calls.get())
        assertTextVisible("Visible agent")
    }

    @Test fun switchingTabsCancelsAPendingAgentsRequest() {
        val disposed = AtomicBoolean()
        val subscribed = AtomicBoolean()
        `when`(teamCityService.listAgents(false, null, null)).thenReturn(
            Single.never<Agents>().doOnSubscribe { subscribed.set(true) }.doOnDispose { disposed.set(true) }
        )
        openAgents()
        compose.waitUntil(10_000) { subscribed.get() }
        selectTab(R.id.favorites)
        compose.waitUntil(10_000) { disposed.get() }
        `when`(teamCityService.listAgents(false, null, null)).thenReturn(
            Single.just(Agents(1, listOf(com.github.vase4kin.teamcityapp.agents.api.Agent("Returned agent"))))
        )
        selectTab(R.id.agents)
        assertTextVisible("Returned agent")
    }

    private fun openAgents() {
        activityRule.launchActivity(null)
        selectTab(R.id.agents)
    }

    private fun selectTab(id: Int) {
        onView(allOf(withId(id), isDescendantOfA(withId(R.id.navigation)), isDisplayed())).perform(click())
    }

    private fun showDisconnected() {
        onView(allOf(withId(R.id.home_floating_action_button), isDisplayed())).perform(click())
        compose.onNodeWithText(text(FilterR.string.text_show_disconnected)).performClick()
    }

    private fun assertTextVisible(value: String) {
        compose.waitUntil(10_000) { compose.onAllNodesWithText(value).fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText(value).assertIsDisplayed()
    }

    private fun text(id: Int) = InstrumentationRegistry.getInstrumentation().targetContext.getString(id)
}
