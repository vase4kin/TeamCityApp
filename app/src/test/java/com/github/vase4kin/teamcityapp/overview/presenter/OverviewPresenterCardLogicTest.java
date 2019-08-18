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

package com.github.vase4kin.teamcityapp.overview.presenter;

import com.github.vase4kin.teamcityapp.onboarding.OnboardingManager;
import com.github.vase4kin.teamcityapp.overview.data.BuildDetails;
import com.github.vase4kin.teamcityapp.overview.data.OverViewInteractor;
import com.github.vase4kin.teamcityapp.overview.tracker.OverviewTracker;
import com.github.vase4kin.teamcityapp.overview.view.OverviewViewImpl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
public class OverviewPresenterCardLogicTest {

    private static final String STATUS_TEXT = "StatusText";
    private static final String STATUS_ICON = "StatusIcon";
    private static final String USER = "user";
    private static final String TIME = "time";
    private static final String TRIGGERED_DETAILS = "triggered_details";

    @Mock
    private OverviewViewImpl view;

    @Mock
    private OverViewInteractor interactor;

    @Mock
    private OverviewTracker tracker;

    @Mock
    private BuildDetails buildDetails;

    @Mock
    private OnboardingManager onboardingManager;

    private OverviewPresenterImpl presenter;

    @Before
    public void setUp() {
        presenter = new OverviewPresenterImpl(view, interactor, tracker, onboardingManager);
        when(interactor.getBuildDetails()).thenReturn(buildDetails);
        when(buildDetails.getStatusText()).thenReturn(STATUS_TEXT);
        when(buildDetails.getStatusIcon()).thenReturn(STATUS_ICON);
    }

    @Test
    public void testOnSuccessViewInteractions() {
        when(buildDetails.getFinishTime()).thenReturn(TIME);
        presenter.onSuccess(buildDetails);
        verify(view).hideCards();
        verify(view).hideSkeletonView();
        verify(view).hideRefreshingProgress();
        verify(view).showCards();
    }

    @Test
    public void testAddWaitReasonCardIfBuildIsQueued() {
        when(buildDetails.isQueued()).thenReturn(true);
        when(buildDetails.getQueuedDate()).thenReturn(TIME);
        presenter.onSuccess(buildDetails);
        verify(buildDetails, times(3)).isQueued();
        verify(view).addWaitReasonStatusCard(eq(STATUS_ICON), eq(STATUS_TEXT));
        verify(view, never()).addResultStatusCard(anyString(), anyString());
    }

    @Test
    public void testAddResultStatusCardIfBuildIsNotQueued() {
        when(buildDetails.isQueued()).thenReturn(false);
        when(buildDetails.getFinishTime()).thenReturn(TIME);
        presenter.onSuccess(buildDetails);
        verify(buildDetails, times(3)).isQueued();
        verify(view).addResultStatusCard(eq(STATUS_ICON), eq(STATUS_TEXT));
        verify(view, never()).addWaitReasonStatusCard(anyString(), anyString());
    }

    @Test
    public void testNoCancellationCardsAddedIfCancellationInfoIsNotProvided() {
        when(buildDetails.hasCancellationInfo()).thenReturn(false);
        when(buildDetails.getFinishTime()).thenReturn(TIME);
        presenter.onSuccess(buildDetails);
        verify(buildDetails).hasCancellationInfo();
        verify(view, never()).addCancelledByCard(anyString(), anyString());
        verify(view, never()).addCancellationTimeCard(anyString());
    }

    @Test
    public void testAddCancellationCardsIfCancellationInfoIsProvided() {
        when(buildDetails.getStatusIcon()).thenReturn(STATUS_ICON);
        when(buildDetails.hasCancellationInfo()).thenReturn(true);
        when(buildDetails.hasUserInfoWhoCancelledBuild()).thenReturn(true);
        when(buildDetails.getUserNameWhoCancelledBuild()).thenReturn(USER);
        when(buildDetails.getCancellationTime()).thenReturn(TIME);
        when(buildDetails.getFinishTime()).thenReturn(TIME);
        presenter.onSuccess(buildDetails);
        verify(buildDetails).hasCancellationInfo();
        verify(buildDetails).hasUserInfoWhoCancelledBuild();
        verify(buildDetails).getUserNameWhoCancelledBuild();
        verify(view).addCancelledByCard(eq(STATUS_ICON), eq(USER));
        verify(buildDetails).getCancellationTime();
        verify(view).addCancellationTimeCard(TIME);
    }

    @Test
    public void testAddTimeCardIfBuildIsRunning() {
        when(buildDetails.isRunning()).thenReturn(true);
        when(buildDetails.getStartDate()).thenReturn(TIME);
        presenter.onSuccess(buildDetails);
        verify(buildDetails).isRunning();
        verify(buildDetails).getStartDate();
        verify(view).addTimeCard(eq(TIME));
    }

    @Test
    public void testAddTimeCardIfBuildIsQueued() {
        when(buildDetails.isRunning()).thenReturn(false);
        when(buildDetails.isQueued()).thenReturn(true);
        when(buildDetails.getQueuedDate()).thenReturn(TIME);
        presenter.onSuccess(buildDetails);
        verify(buildDetails).isRunning();
        verify(buildDetails, times(3)).isQueued();
        verify(buildDetails).getQueuedDate();
        verify(view).addQueuedTimeCard(eq(TIME));
    }

    @Test
    public void testAddTimeCardIfBuildIsCompleted() {
        when(buildDetails.isRunning()).thenReturn(false);
        when(buildDetails.isQueued()).thenReturn(false);
        when(buildDetails.getFinishTime()).thenReturn(TIME);
        presenter.onSuccess(buildDetails);
        verify(buildDetails).isRunning();
        verify(buildDetails, times(3)).isQueued();
        verify(buildDetails).getFinishTime();
        verify(view).addTimeCard(eq(TIME));
    }

    @Test
    public void testAddEstimatedTimeCardIfBuildIsNotQueued() {
        when(buildDetails.isQueued()).thenReturn(false);
        when(buildDetails.getFinishTime()).thenReturn(TIME);
        presenter.onSuccess(buildDetails);
        verify(buildDetails).getEstimatedStartTime();
        verify(view, never()).addEstimatedTimeToStartCard(anyString());
    }

    @Test
    public void testAddEstimatedTimeCardIfBuildDoesNotHaveEstimatedTime() {
        when(buildDetails.isQueued()).thenReturn(true);
        when(buildDetails.getEstimatedStartTime()).thenReturn("");
        when(buildDetails.getQueuedDate()).thenReturn(TIME);
        presenter.onSuccess(buildDetails);
        verify(buildDetails).getEstimatedStartTime();
        verify(view, never()).addEstimatedTimeToStartCard(anyString());
    }

    @Test
    public void testAddEstimatedTimeCardIfBuildHasEstimatedTime() {
        when(buildDetails.isQueued()).thenReturn(true);
        when(buildDetails.getEstimatedStartTime()).thenReturn(TIME);
        when(buildDetails.getQueuedDate()).thenReturn(TIME);
        presenter.onSuccess(buildDetails);
        verify(buildDetails).getEstimatedStartTime();
        verify(view).addEstimatedTimeToStartCard(TIME);
    }

    @Test
    public void testAddBranchCardIfBuildDoesNotHaveBranchName() {
        when(buildDetails.getBranchName()).thenReturn("");
        when(buildDetails.getFinishTime()).thenReturn(TIME);
        presenter.onSuccess(buildDetails);
        verify(buildDetails).getBranchName();
        verify(view, never()).addBranchCard(anyString());
    }

    @Test
    public void testAddBranchCardIfBuildHasBranchName() {
        when(buildDetails.getBranchName()).thenReturn("branch");
        when(buildDetails.getFinishTime()).thenReturn(TIME);
        presenter.onSuccess(buildDetails);
        verify(buildDetails).getBranchName();
        verify(view).addBranchCard("branch");
    }

    @Test
    public void testAddAgentCardIfBuildDoesNotHaveAgentInfo() {
        when(buildDetails.hasAgentInfo()).thenReturn(false);
        when(buildDetails.getFinishTime()).thenReturn(TIME);
        presenter.onSuccess(buildDetails);
        verify(buildDetails).hasAgentInfo();
        verify(buildDetails, never()).getAgentName();
        verify(view, never()).addAgentCard(anyString());

    }

    @Test
    public void testAddAgentCardIfBuildHasAgentInfo() {
        when(buildDetails.hasAgentInfo()).thenReturn(true);
        when(buildDetails.getAgentName()).thenReturn("agent");
        when(buildDetails.getFinishTime()).thenReturn(TIME);
        presenter.onSuccess(buildDetails);
        verify(buildDetails).hasAgentInfo();
        verify(buildDetails).getAgentName();
        verify(view).addAgentCard("agent");
    }

    @Test
    public void testAddTriggeredByCardIfBuildWasTriggeredByVcs() {
        when(buildDetails.isTriggeredByVcs()).thenReturn(true);
        when(buildDetails.getTriggeredDetails()).thenReturn(TRIGGERED_DETAILS);
        when(buildDetails.getFinishTime()).thenReturn(TIME);
        presenter.onSuccess(buildDetails);
        verify(buildDetails).isTriggeredByVcs();
        verify(buildDetails).getTriggeredDetails();
        verify(view).addTriggeredByCard(eq(TRIGGERED_DETAILS));
    }

    @Test
    public void testAddTriggeredByCardIfBuildWasTriggeredByUnknown() {
        when(buildDetails.isTriggeredByVcs()).thenReturn(false);
        when(buildDetails.isTriggeredByUnknown()).thenReturn(true);
        when(buildDetails.getTriggeredDetails()).thenReturn(TRIGGERED_DETAILS);
        when(buildDetails.getFinishTime()).thenReturn(TIME);
        presenter.onSuccess(buildDetails);
        verify(buildDetails).isTriggeredByVcs();
        verify(buildDetails).isTriggeredByUnknown();
        verify(buildDetails).getTriggeredDetails();
        verify(view).addTriggeredByCard(eq(TRIGGERED_DETAILS));
    }

    @Test
    public void testAddTriggeredByCardIfBuildWasTriggeredByUser() {
        when(buildDetails.isTriggeredByVcs()).thenReturn(false);
        when(buildDetails.isTriggeredByUnknown()).thenReturn(false);
        when(buildDetails.isTriggeredByUser()).thenReturn(true);
        when(buildDetails.getUserNameOfUserWhoTriggeredBuild()).thenReturn(USER);
        when(buildDetails.getFinishTime()).thenReturn(TIME);
        presenter.onSuccess(buildDetails);
        verify(buildDetails).isTriggeredByVcs();
        verify(buildDetails).isTriggeredByUnknown();
        verify(buildDetails).isTriggeredByUser();
        verify(buildDetails).getUserNameOfUserWhoTriggeredBuild();
        verify(view).addTriggeredByCard(eq(USER));
    }

    @Test
    public void testAddTriggeredByCardIfBuildWasRestarted() {
        when(buildDetails.isTriggeredByVcs()).thenReturn(false);
        when(buildDetails.isTriggeredByUnknown()).thenReturn(false);
        when(buildDetails.isTriggeredByUser()).thenReturn(false);
        when(buildDetails.isRestarted()).thenReturn(true);
        when(buildDetails.getUserNameOfUserWhoTriggeredBuild()).thenReturn(USER);
        when(buildDetails.getFinishTime()).thenReturn(TIME);
        presenter.onSuccess(buildDetails);
        verify(buildDetails).isTriggeredByVcs();
        verify(buildDetails).isTriggeredByUnknown();
        verify(buildDetails).isTriggeredByUser();
        verify(buildDetails).isRestarted();
        verify(buildDetails).getUserNameOfUserWhoTriggeredBuild();
        verify(view).addRestartedByCard(eq(USER));
    }

    @Test
    public void testAddTriggeredByCardIfBuildWasTriggeredByBuildType() {
        when(buildDetails.isTriggeredByVcs()).thenReturn(false);
        when(buildDetails.isTriggeredByUnknown()).thenReturn(false);
        when(buildDetails.isTriggeredByUser()).thenReturn(false);
        when(buildDetails.isRestarted()).thenReturn(false);
        when(buildDetails.isTriggeredByBuildType()).thenReturn(true);
        when(buildDetails.getNameOfTriggeredBuildType()).thenReturn("buildType");
        when(buildDetails.getFinishTime()).thenReturn(TIME);
        presenter.onSuccess(buildDetails);
        verify(buildDetails).isTriggeredByVcs();
        verify(buildDetails).isTriggeredByUnknown();
        verify(buildDetails).isTriggeredByUser();
        verify(buildDetails).isRestarted();
        verify(buildDetails).isTriggeredByBuildType();
        verify(buildDetails).getNameOfTriggeredBuildType();
        verify(view).addTriggeredByCard(eq("buildType"));
    }

    @Test
    public void testAddTriggeredByCardIfBuildWasTriggeredByUnknownTrigger() {
        when(buildDetails.isTriggeredByVcs()).thenReturn(false);
        when(buildDetails.isTriggeredByUnknown()).thenReturn(false);
        when(buildDetails.isTriggeredByUser()).thenReturn(false);
        when(buildDetails.isRestarted()).thenReturn(false);
        when(buildDetails.isTriggeredByBuildType()).thenReturn(false);
        when(buildDetails.getFinishTime()).thenReturn(TIME);
        presenter.onSuccess(buildDetails);
        verify(buildDetails).isTriggeredByVcs();
        verify(buildDetails).isTriggeredByUnknown();
        verify(buildDetails).isTriggeredByUser();
        verify(buildDetails).isRestarted();
        verify(buildDetails).isTriggeredByBuildType();
        verify(view).addTriggeredByUnknownTriggerTypeCard();
    }

    @Test
    public void testAddPersonalCardIfBuildWasTriggeredByUser() {
        when(buildDetails.isPersonal()).thenReturn(true);
        when(buildDetails.getUserNameOfUserWhoTriggeredBuild()).thenReturn(USER);
        when(buildDetails.getFinishTime()).thenReturn(TIME);
        presenter.onSuccess(buildDetails);
        verify(buildDetails).isPersonal();
        verify(buildDetails).getUserNameOfUserWhoTriggeredBuild();
        verify(view).addPersonalCard(eq(USER));
    }

    @Test
    public void testAddBuildTypeAndProjectCard() {
        when(buildDetails.hasBuildTypeInfo()).thenReturn(true);
        when(buildDetails.getBuildTypeName()).thenReturn("bt_name");
        when(buildDetails.getProjectName()).thenReturn("p_name");
        when(buildDetails.getFinishTime()).thenReturn(TIME);
        presenter.onSuccess(buildDetails);
        verify(buildDetails).hasBuildTypeInfo();
        verify(buildDetails).getBuildTypeName();
        verify(view).addBuildTypeNameCard(eq("bt_name"));
        verify(buildDetails).getProjectName();
        verify(view).addBuildTypeProjectNameCard(eq("p_name"));
    }

}