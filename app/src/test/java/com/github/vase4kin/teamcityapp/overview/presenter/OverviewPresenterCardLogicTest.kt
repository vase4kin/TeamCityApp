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

package com.github.vase4kin.teamcityapp.overview.presenter

import com.github.vase4kin.teamcityapp.onboarding.OnboardingManager
import com.github.vase4kin.teamcityapp.overview.data.BuildDetails
import com.github.vase4kin.teamcityapp.overview.data.OverViewInteractor
import com.github.vase4kin.teamcityapp.overview.tracker.OverviewTracker
import com.github.vase4kin.teamcityapp.overview.view.OverviewViewImpl
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Matchers.anyString
import org.mockito.Matchers.eq
import org.mockito.Mock
import org.mockito.Mockito.*
import org.powermock.modules.junit4.PowerMockRunner

@RunWith(PowerMockRunner::class)
class OverviewPresenterCardLogicTest {

    @Mock
    private lateinit var view: OverviewViewImpl

    @Mock
    private lateinit var interactor: OverViewInteractor

    @Mock
    private lateinit var tracker: OverviewTracker

    @Mock
    private lateinit var buildDetails: BuildDetails

    @Mock
    private lateinit var onboardingManager: OnboardingManager

    private lateinit var presenter: OverviewPresenterImpl

    @Before
    fun setUp() {
        presenter = OverviewPresenterImpl(view, interactor, tracker, onboardingManager)
        `when`(interactor.buildDetails).thenReturn(buildDetails)
        `when`(buildDetails.statusText).thenReturn(STATUS_TEXT)
        `when`(buildDetails.statusIcon).thenReturn(STATUS_ICON)
    }

    @Test
    fun testOnSuccessViewInteractions() {
        `when`(buildDetails.finishTime).thenReturn(TIME)
        presenter.onSuccess(buildDetails)
        verify(view).hideCards()
        verify(view).hideSkeletonView()
        verify(view).hideRefreshingProgress()
        verify(view).showCards()
    }

    @Test
    fun testAddWaitReasonCardIfBuildIsQueued() {
        `when`(buildDetails.isQueued).thenReturn(true)
        `when`(buildDetails.queuedDate).thenReturn(TIME)
        presenter.onSuccess(buildDetails)
        verify(buildDetails, times(3)).isQueued
        verify(view).addWaitReasonStatusCard(eq(STATUS_ICON), eq(STATUS_TEXT))
        verify(view, never()).addResultStatusCard(anyString(), anyString())
    }

    @Test
    fun testAddResultStatusCardIfBuildIsNotQueued() {
        `when`(buildDetails.isQueued).thenReturn(false)
        `when`(buildDetails.finishTime).thenReturn(TIME)
        presenter.onSuccess(buildDetails)
        verify(buildDetails, times(3)).isQueued
        verify(view).addResultStatusCard(eq(STATUS_ICON), eq(STATUS_TEXT))
        verify(view, never()).addWaitReasonStatusCard(anyString(), anyString())
    }

    @Test
    fun testNoCancellationCardsAddedIfCancellationInfoIsNotProvided() {
        `when`(buildDetails.hasCancellationInfo()).thenReturn(false)
        `when`(buildDetails.finishTime).thenReturn(TIME)
        presenter.onSuccess(buildDetails)
        verify(buildDetails).hasCancellationInfo()
        verify(view, never()).addCancelledByCard(anyString(), anyString())
        verify(view, never()).addCancellationTimeCard(anyString())
    }

    @Test
    fun testAddCancellationCardsIfCancellationInfoIsProvided() {
        `when`(buildDetails.statusIcon).thenReturn(STATUS_ICON)
        `when`(buildDetails.hasCancellationInfo()).thenReturn(true)
        `when`(buildDetails.hasUserInfoWhoCancelledBuild()).thenReturn(true)
        `when`(buildDetails.userNameWhoCancelledBuild).thenReturn(USER)
        `when`(buildDetails.cancellationTime).thenReturn(TIME)
        `when`(buildDetails.finishTime).thenReturn(TIME)
        presenter.onSuccess(buildDetails)
        verify(buildDetails).hasCancellationInfo()
        verify(buildDetails).hasUserInfoWhoCancelledBuild()
        verify(buildDetails).userNameWhoCancelledBuild
        verify(view).addCancelledByCard(eq(STATUS_ICON), eq(USER))
        verify(buildDetails).cancellationTime
        verify(view).addCancellationTimeCard(TIME)
    }

    @Test
    fun testAddTimeCardIfBuildIsRunning() {
        `when`(buildDetails.isRunning).thenReturn(true)
        `when`(buildDetails.startDate).thenReturn(TIME)
        presenter.onSuccess(buildDetails)
        verify(buildDetails).isRunning
        verify(buildDetails).startDate
        verify(view).addTimeCard(eq(TIME))
    }

    @Test
    fun testAddTimeCardIfBuildIsQueued() {
        `when`(buildDetails.isRunning).thenReturn(false)
        `when`(buildDetails.isQueued).thenReturn(true)
        `when`(buildDetails.queuedDate).thenReturn(TIME)
        presenter.onSuccess(buildDetails)
        verify(buildDetails).isRunning
        verify(buildDetails, times(3)).isQueued
        verify(buildDetails).queuedDate
        verify(view).addQueuedTimeCard(eq(TIME))
    }

    @Test
    fun testAddTimeCardIfBuildIsCompleted() {
        `when`(buildDetails.isRunning).thenReturn(false)
        `when`(buildDetails.isQueued).thenReturn(false)
        `when`(buildDetails.finishTime).thenReturn(TIME)
        presenter.onSuccess(buildDetails)
        verify(buildDetails).isRunning
        verify(buildDetails, times(3)).isQueued
        verify(buildDetails).finishTime
        verify(view).addTimeCard(eq(TIME))
    }

    @Test
    fun testAddEstimatedTimeCardIfBuildIsNotQueued() {
        `when`(buildDetails.isQueued).thenReturn(false)
        `when`(buildDetails.finishTime).thenReturn(TIME)
        presenter.onSuccess(buildDetails)
        verify(buildDetails).estimatedStartTime
        verify(view, never()).addEstimatedTimeToStartCard(anyString())
    }

    @Test
    fun testAddEstimatedTimeCardIfBuildDoesNotHaveEstimatedTime() {
        `when`(buildDetails.isQueued).thenReturn(true)
        `when`(buildDetails.estimatedStartTime).thenReturn("")
        `when`(buildDetails.queuedDate).thenReturn(TIME)
        presenter.onSuccess(buildDetails)
        verify(buildDetails).estimatedStartTime
        verify(view, never()).addEstimatedTimeToStartCard(anyString())
    }

    @Test
    fun testAddEstimatedTimeCardIfBuildHasEstimatedTime() {
        `when`(buildDetails.isQueued).thenReturn(true)
        `when`(buildDetails.estimatedStartTime).thenReturn(TIME)
        `when`(buildDetails.queuedDate).thenReturn(TIME)
        presenter.onSuccess(buildDetails)
        verify(buildDetails).estimatedStartTime
        verify(view).addEstimatedTimeToStartCard(TIME)
    }

    @Test
    fun testAddBranchCardIfBuildDoesNotHaveBranchName() {
        `when`(buildDetails.branchName).thenReturn("")
        `when`(buildDetails.finishTime).thenReturn(TIME)
        presenter.onSuccess(buildDetails)
        verify(buildDetails).branchName
        verify(view, never()).addBranchCard(anyString())
    }

    @Test
    fun testAddBranchCardIfBuildHasBranchName() {
        `when`(buildDetails.branchName).thenReturn("branch")
        `when`(buildDetails.finishTime).thenReturn(TIME)
        presenter.onSuccess(buildDetails)
        verify(buildDetails).branchName
        verify(view).addBranchCard("branch")
    }

    @Test
    fun testAddAgentCardIfBuildDoesNotHaveAgentInfo() {
        `when`(buildDetails.hasAgentInfo()).thenReturn(false)
        `when`(buildDetails.finishTime).thenReturn(TIME)
        presenter.onSuccess(buildDetails)
        verify(buildDetails).hasAgentInfo()
        verify(buildDetails, never()).agentName
        verify(view, never()).addAgentCard(anyString())
    }

    @Test
    fun testAddAgentCardIfBuildHasAgentInfo() {
        `when`(buildDetails.hasAgentInfo()).thenReturn(true)
        `when`(buildDetails.agentName).thenReturn("agent")
        `when`(buildDetails.finishTime).thenReturn(TIME)
        presenter.onSuccess(buildDetails)
        verify(buildDetails).hasAgentInfo()
        verify(buildDetails).agentName
        verify(view).addAgentCard("agent")
    }

    @Test
    fun testAddTriggeredByCardIfBuildWasTriggeredByVcs() {
        `when`(buildDetails.isTriggeredByVcs).thenReturn(true)
        `when`(buildDetails.triggeredDetails).thenReturn(TRIGGERED_DETAILS)
        `when`(buildDetails.finishTime).thenReturn(TIME)
        presenter.onSuccess(buildDetails)
        verify(buildDetails).isTriggeredByVcs
        verify(buildDetails).triggeredDetails
        verify(view).addTriggeredByCard(eq(TRIGGERED_DETAILS))
    }

    @Test
    fun testAddTriggeredByCardIfBuildWasTriggeredByUnknown() {
        `when`(buildDetails.isTriggeredByVcs).thenReturn(false)
        `when`(buildDetails.isTriggeredByUnknown).thenReturn(true)
        `when`(buildDetails.triggeredDetails).thenReturn(TRIGGERED_DETAILS)
        `when`(buildDetails.finishTime).thenReturn(TIME)
        presenter.onSuccess(buildDetails)
        verify(buildDetails).isTriggeredByVcs
        verify(buildDetails).isTriggeredByUnknown
        verify(buildDetails).triggeredDetails
        verify(view).addTriggeredByCard(eq(TRIGGERED_DETAILS))
    }

    @Test
    fun testAddTriggeredByCardIfBuildWasTriggeredByUser() {
        `when`(buildDetails.isTriggeredByVcs).thenReturn(false)
        `when`(buildDetails.isTriggeredByUnknown).thenReturn(false)
        `when`(buildDetails.isTriggeredByUser).thenReturn(true)
        `when`(buildDetails.userNameOfUserWhoTriggeredBuild).thenReturn(USER)
        `when`(buildDetails.finishTime).thenReturn(TIME)
        presenter.onSuccess(buildDetails)
        verify(buildDetails).isTriggeredByVcs
        verify(buildDetails).isTriggeredByUnknown
        verify(buildDetails).isTriggeredByUser
        verify(buildDetails).userNameOfUserWhoTriggeredBuild
        verify(view).addTriggeredByCard(eq(USER))
    }

    @Test
    fun testAddTriggeredByCardIfBuildWasRestarted() {
        `when`(buildDetails.isTriggeredByVcs).thenReturn(false)
        `when`(buildDetails.isTriggeredByUnknown).thenReturn(false)
        `when`(buildDetails.isTriggeredByUser).thenReturn(false)
        `when`(buildDetails.isRestarted).thenReturn(true)
        `when`(buildDetails.userNameOfUserWhoTriggeredBuild).thenReturn(USER)
        `when`(buildDetails.finishTime).thenReturn(TIME)
        presenter.onSuccess(buildDetails)
        verify(buildDetails).isTriggeredByVcs
        verify(buildDetails).isTriggeredByUnknown
        verify(buildDetails).isTriggeredByUser
        verify(buildDetails).isRestarted
        verify(buildDetails).userNameOfUserWhoTriggeredBuild
        verify(view).addRestartedByCard(eq(USER))
    }

    @Test
    fun testAddTriggeredByCardIfBuildWasTriggeredByBuildType() {
        `when`(buildDetails.isTriggeredByVcs).thenReturn(false)
        `when`(buildDetails.isTriggeredByUnknown).thenReturn(false)
        `when`(buildDetails.isTriggeredByUser).thenReturn(false)
        `when`(buildDetails.isRestarted).thenReturn(false)
        `when`(buildDetails.isTriggeredByBuildType).thenReturn(true)
        `when`(buildDetails.nameOfTriggeredBuildType).thenReturn("buildType")
        `when`(buildDetails.finishTime).thenReturn(TIME)
        presenter.onSuccess(buildDetails)
        verify(buildDetails).isTriggeredByVcs
        verify(buildDetails).isTriggeredByUnknown
        verify(buildDetails).isTriggeredByUser
        verify(buildDetails).isRestarted
        verify(buildDetails).isTriggeredByBuildType
        verify(buildDetails).nameOfTriggeredBuildType
        verify(view).addTriggeredByCard(eq("buildType"))
    }

    @Test
    fun testAddTriggeredByCardIfBuildWasTriggeredByUnknownTrigger() {
        `when`(buildDetails.isTriggeredByVcs).thenReturn(false)
        `when`(buildDetails.isTriggeredByUnknown).thenReturn(false)
        `when`(buildDetails.isTriggeredByUser).thenReturn(false)
        `when`(buildDetails.isRestarted).thenReturn(false)
        `when`(buildDetails.isTriggeredByBuildType).thenReturn(false)
        `when`(buildDetails.finishTime).thenReturn(TIME)
        presenter.onSuccess(buildDetails)
        verify(buildDetails).isTriggeredByVcs
        verify(buildDetails).isTriggeredByUnknown
        verify(buildDetails).isTriggeredByUser
        verify(buildDetails).isRestarted
        verify(buildDetails).isTriggeredByBuildType
        verify(view).addTriggeredByUnknownTriggerTypeCard()
    }

    @Test
    fun testAddPersonalCardIfBuildWasTriggeredByUser() {
        `when`(buildDetails.isPersonal).thenReturn(true)
        `when`(buildDetails.userNameOfUserWhoTriggeredBuild).thenReturn(USER)
        `when`(buildDetails.finishTime).thenReturn(TIME)
        presenter.onSuccess(buildDetails)
        verify(buildDetails).isPersonal
        verify(buildDetails).userNameOfUserWhoTriggeredBuild
        verify(view).addPersonalCard(eq(USER))
    }

    @Test
    fun testAddBuildTypeAndProjectCard() {
        `when`(buildDetails.hasBuildTypeInfo()).thenReturn(true)
        `when`(buildDetails.buildTypeName).thenReturn("bt_name")
        `when`(buildDetails.projectName).thenReturn("p_name")
        `when`(buildDetails.finishTime).thenReturn(TIME)
        presenter.onSuccess(buildDetails)
        verify(buildDetails).hasBuildTypeInfo()
        verify(buildDetails).buildTypeName
        verify(view).addBuildTypeNameCard(eq("bt_name"))
        verify(buildDetails).projectName
        verify(view).addBuildTypeProjectNameCard(eq("p_name"))
    }

    companion object {
        private const val STATUS_TEXT = "StatusText"
        private const val STATUS_ICON = "StatusIcon"
        private const val USER = "user"
        private const val TIME = "time"
        private const val TRIGGERED_DETAILS = "triggered_details"
    }
}
