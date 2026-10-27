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
import com.github.vase4kin.teamcityapp.di.modules.FakeTeamCityServiceImpl
import com.github.vase4kin.teamcityapp.di.modules.Mocks
import com.github.vase4kin.teamcityapp.helper.CustomActivityTestRule
import com.github.vase4kin.teamcityapp.helper.HiltApiTestRule
import com.github.vase4kin.teamcityapp.helper.TestUtils
import com.github.vase4kin.teamcityapp.helper.any
import com.github.vase4kin.teamcityapp.navigation.IncomingDestinationResolver
import com.github.vase4kin.teamcityapp.navigation.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import org.hamcrest.core.AllOf.allOf
import org.junit.*
import org.junit.runner.RunWith
import org.mockito.Mockito.`when`
import org.mockito.Spy
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doSuspendableAnswer
import org.mockito.kotlin.whenever
import teamcityapp.features.agents.impl.R as AgentsR

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class AgentListFragmentTest {
    @get:org.junit.Rule(order = 6)
    val phaseThreeEvidence = com.github.vase4kin.teamcityapp.helper.PhaseThreeEvidenceRule()

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
    val activityRule = CustomActivityTestRule(MainActivity::class.java, IncomingDestinationResolver.HOME_COMPONENT)

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
        compose.onNodeWithTag("home:filter:AgentsDisconnected").assertIsSelected()
        compose.onNodeWithTag("home:snackbar").assertDoesNotExist()
        compose.onNodeWithText("agent 1").assertDoesNotExist()
        compose.onNodeWithText("agent 2").assertDoesNotExist()
        compose.onNodeWithText("agent 3").assertDoesNotExist()
    }

    @Test fun showsTheAgentsToolbarAndOpensTheDrawer() {
        openAgents()
        assertTextVisible(text(teamcityapp.features.home.impl.R.string.home_activity))
        compose.onNodeWithTag("home:account").performClick()
        compose.onNodeWithTag("drawer:list").assertExists()
    }

    @Test fun failureCanBeRetriedWithoutRecreatingTheFragment() {
        doSuspendableAnswer { throw RuntimeException("offline") }.whenever(teamCityService) { listAgents(any(), any(), any()) }
        openAgents()
        assertTextVisible(text(teamcityapp.libraries.theme.R.string.error_load_message))
        doReturn(Agents(1, listOf(com.github.vase4kin.teamcityapp.agents.api.Agent("Recovered")))).whenever(teamCityService) { listAgents(any(), any(), any()) }
        compose.onNodeWithText(text(teamcityapp.libraries.theme.R.string.action_retry)).performClick()
        assertTextVisible("Recovered")
    }

    @Test fun emptyMessageFollowsTheSelectedFilter() {
        doReturn(Agents(0, emptyList())).whenever(teamCityService) { listAgents(any(), any(), any()) }
        openAgents()
        assertTextVisible(text(AgentsR.string.agents_empty_connected))
        showDisconnected()
        assertTextVisible(text(AgentsR.string.agents_empty_disconnected))
        compose.onNodeWithText(text(AgentsR.string.agents_empty_connected)).assertDoesNotExist()
    }

    @Test fun returningToTheAgentsTabRetainsItsCompletedList() {
        val calls = AtomicInteger()
        doSuspendableAnswer {
            calls.incrementAndGet()
            Agents(1, listOf(com.github.vase4kin.teamcityapp.agents.api.Agent("Visible agent")))
        }.whenever(teamCityService) { listAgents(false, null, null) }
        openAgents()
        assertTextVisible("Visible agent")
        Assert.assertEquals(1, calls.get())
        selectTab("Favorites")
        selectTab("Agents")
        compose.waitForIdle()
        Assert.assertEquals(1, calls.get())
        assertTextVisible("Visible agent")
    }

    @Test fun switchingTabsCancelsAPendingAgentsRequest() {
        val disposed = AtomicBoolean()
        val subscribed = AtomicBoolean()
        doSuspendableAnswer {
            subscribed.set(true)
            try {
                kotlinx.coroutines.awaitCancellation()
            } catch (cancelled: kotlinx.coroutines.CancellationException) {
                disposed.set(true)
                throw cancelled
            }
        }.whenever(teamCityService) { listAgents(false, null, null) }
        openAgents()
        compose.waitUntil(10_000) { subscribed.get() }
        selectTab("Favorites")
        compose.waitUntil(10_000) { disposed.get() }
        doReturn(Agents(1, listOf(com.github.vase4kin.teamcityapp.agents.api.Agent("Returned agent")))).whenever(teamCityService) { listAgents(false, null, null) }
        selectTab("Agents")
        assertTextVisible("Returned agent")
    }

    private fun openAgents() {
        activityRule.launchActivity(null)
        selectTab("Agents")
    }

    private fun selectTab(tab: String) {
        if (tab in listOf("Running", "Queue", "Agents")) compose.onNodeWithTag("home:tab:Activity").performClick()
        compose.onNodeWithTag("home:tab:$tab").performClick()
    }

    private fun showDisconnected() {
        compose.onNodeWithTag("home:filter:AgentsDisconnected").performClick()
    }

    private fun assertTextVisible(value: String) {
        compose.waitUntil(10_000) { compose.onAllNodesWithText(value).fetchSemanticsNodes().isNotEmpty() }
        compose.onAllNodesWithText(value).onFirst().assertIsDisplayed()
    }

    private fun text(id: Int) = InstrumentationRegistry.getInstrumentation().targetContext.getString(id)
}
