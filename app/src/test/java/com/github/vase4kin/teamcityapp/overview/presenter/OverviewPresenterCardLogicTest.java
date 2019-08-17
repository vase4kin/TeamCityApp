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
import org.junit.Ignore;
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

@Ignore("FIX TESTS")
@RunWith(PowerMockRunner.class)
public class OverviewPresenterCardLogicTest {

    private static final String STATUS_TEXT = "StatusText";
    private static final String STATUS_ICON = "StatusIcon";
    private static final String USER = "user";
    private static final String TIME = "time";
    private static final String TRIGGERED_DETAILS = "triggered_details";

    @Mock
    private OverviewViewImpl mView;

    @Mock
    private OverViewInteractor mInteractor;

    @Mock
    private OverviewTracker mTracker;

    @Mock
    private BuildDetails mBuildDetails;

    @Mock
    private OnboardingManager mOnboardingManager;

    private OverviewPresenterImpl mPresenter;

    @Before
    public void setUp() {
        mPresenter = new OverviewPresenterImpl(mView, mInteractor, mTracker, mOnboardingManager);
        when(mInteractor.getBuildDetails()).thenReturn(mBuildDetails);
    }

    @Test
    public void testOnSuccessViewInteractions() {
        mPresenter.onSuccess(mBuildDetails);
        verify(mView).hideCards();
        verify(mView).hideSkeletonView();
        verify(mView).hideRefreshingProgress();
        verify(mView).showCards();
    }

    @Test
    public void testAddWaitReasonCardIfBuildIsQueued() {
        when(mBuildDetails.isQueued()).thenReturn(true);
        when(mBuildDetails.getStatusText()).thenReturn(STATUS_TEXT);
        when(mBuildDetails.getStatusIcon()).thenReturn(STATUS_ICON);
        mPresenter.onSuccess(mBuildDetails);
        verify(mBuildDetails, times(3)).isQueued();
        verify(mView).addWaitReasonStatusCard(eq(STATUS_ICON), eq(STATUS_TEXT));
        verify(mView, never()).addResultStatusCard(anyString(), anyString());
    }

    @Test
    public void testAddResultStatusCardIfBuildIsNotQueued() {
        when(mBuildDetails.isQueued()).thenReturn(false);
        when(mBuildDetails.getStatusText()).thenReturn(STATUS_TEXT);
        when(mBuildDetails.getStatusIcon()).thenReturn(STATUS_ICON);
        mPresenter.onSuccess(mBuildDetails);
        verify(mBuildDetails, times(3)).isQueued();
        verify(mView).addResultStatusCard(eq(STATUS_ICON), eq(STATUS_TEXT));
        verify(mView, never()).addWaitReasonStatusCard(anyString(), anyString());
    }

    @Test
    public void testNoCancellationCardsAddedIfCancellationInfoIsNotProvided() {
        when(mBuildDetails.hasCancellationInfo()).thenReturn(false);
        mPresenter.onSuccess(mBuildDetails);
        verify(mBuildDetails).hasCancellationInfo();
        verify(mView, never()).addCancelledByCard(anyString(), anyString());
        verify(mView, never()).addCancellationTimeCard(anyString());
    }

    @Test
    public void testAddCancellationCardsIfCancellationInfoIsProvided() {
        when(mBuildDetails.getStatusIcon()).thenReturn(STATUS_ICON);
        when(mBuildDetails.hasCancellationInfo()).thenReturn(true);
        when(mBuildDetails.hasUserInfoWhoCancelledBuild()).thenReturn(true);
        when(mBuildDetails.getUserNameWhoCancelledBuild()).thenReturn(USER);
        when(mBuildDetails.getCancellationTime()).thenReturn(TIME);
        mPresenter.onSuccess(mBuildDetails);
        verify(mBuildDetails).hasCancellationInfo();
        verify(mBuildDetails).hasUserInfoWhoCancelledBuild();
        verify(mBuildDetails).getUserNameWhoCancelledBuild();
        verify(mView).addCancelledByCard(eq(STATUS_ICON), eq(USER));
        verify(mBuildDetails).getCancellationTime();
        verify(mView).addCancellationTimeCard(TIME);
    }

    @Test
    public void testAddTimeCardIfBuildIsRunning() {
        when(mBuildDetails.isRunning()).thenReturn(true);
        when(mBuildDetails.getStartDate()).thenReturn(TIME);
        mPresenter.onSuccess(mBuildDetails);
        verify(mBuildDetails).isRunning();
        verify(mBuildDetails).getStartDate();
        verify(mView).addTimeCard(eq(TIME));
    }

    @Test
    public void testAddTimeCardIfBuildIsQueued() {
        when(mBuildDetails.isRunning()).thenReturn(false);
        when(mBuildDetails.isQueued()).thenReturn(true);
        when(mBuildDetails.getQueuedDate()).thenReturn(TIME);
        mPresenter.onSuccess(mBuildDetails);
        verify(mBuildDetails).isRunning();
        verify(mBuildDetails, times(3)).isQueued();
        verify(mBuildDetails).getQueuedDate();
        verify(mView).addQueuedTimeCard(eq(TIME));
    }

    @Test
    public void testAddTimeCardIfBuildIsCompleted() {
        when(mBuildDetails.isRunning()).thenReturn(false);
        when(mBuildDetails.isQueued()).thenReturn(false);
        when(mBuildDetails.getFinishTime()).thenReturn(TIME);
        mPresenter.onSuccess(mBuildDetails);
        verify(mBuildDetails).isRunning();
        verify(mBuildDetails, times(3)).isQueued();
        verify(mBuildDetails).getFinishTime();
        verify(mView).addTimeCard(eq(TIME));
    }

    @Test
    public void testAddEstimatedTimeCardIfBuildIsNotQueued() {
        when(mBuildDetails.isQueued()).thenReturn(false);
        mPresenter.onSuccess(mBuildDetails);
        verify(mBuildDetails, never()).getEstimatedStartTime();
        verify(mView, never()).addEstimatedTimeToStartCard(anyString());
    }

    @Test
    public void testAddEstimatedTimeCardIfBuildDoesNotHaveEstimatedTime() {
        when(mBuildDetails.isQueued()).thenReturn(true);
        when(mBuildDetails.getEstimatedStartTime()).thenReturn("");
        mPresenter.onSuccess(mBuildDetails);
        verify(mBuildDetails).getEstimatedStartTime();
        verify(mView, never()).addEstimatedTimeToStartCard(anyString());
    }

    @Test
    public void testAddEstimatedTimeCardIfBuildHasEstimatedTime() {
        when(mBuildDetails.isQueued()).thenReturn(true);
        when(mBuildDetails.getEstimatedStartTime()).thenReturn(TIME);
        mPresenter.onSuccess(mBuildDetails);
        verify(mBuildDetails, times(2)).getEstimatedStartTime();
        verify(mView).addEstimatedTimeToStartCard(TIME);
    }

    @Test
    public void testAddBranchCardIfBuildDoesNotHaveBranchName() {
        when(mBuildDetails.getBranchName()).thenReturn("");
        mPresenter.onSuccess(mBuildDetails);
        verify(mBuildDetails).getBranchName();
        verify(mView, never()).addBranchCard(anyString());
    }

    @Test
    public void testAddBranchCardIfBuildHasBranchName() {
        when(mBuildDetails.getBranchName()).thenReturn("branch");
        mPresenter.onSuccess(mBuildDetails);
        verify(mBuildDetails, times(2)).getBranchName();
        verify(mView).addBranchCard("branch");
    }

    @Test
    public void testAddAgentCardIfBuildDoesNotHaveAgentInfo() {
        when(mBuildDetails.hasAgentInfo()).thenReturn(false);
        mPresenter.onSuccess(mBuildDetails);
        verify(mBuildDetails).hasAgentInfo();
        verify(mBuildDetails, never()).getAgentName();
        verify(mView, never()).addAgentCard(anyString());

    }

    @Test
    public void testAddAgentCardIfBuildHasAgentInfo() {
        when(mBuildDetails.hasAgentInfo()).thenReturn(true);
        when(mBuildDetails.getAgentName()).thenReturn("agent");
        mPresenter.onSuccess(mBuildDetails);
        verify(mBuildDetails).hasAgentInfo();
        verify(mBuildDetails).getAgentName();
        verify(mView).addAgentCard("agent");
    }

    @Test
    public void testAddTriggeredByCardIfBuildWasTriggeredByVcs() {
        when(mBuildDetails.isTriggeredByVcs()).thenReturn(true);
        when(mBuildDetails.getTriggeredDetails()).thenReturn(TRIGGERED_DETAILS);
        mPresenter.onSuccess(mBuildDetails);
        verify(mBuildDetails).isTriggeredByVcs();
        verify(mBuildDetails).getTriggeredDetails();
        verify(mView).addTriggeredByCard(eq(TRIGGERED_DETAILS));
    }

    @Test
    public void testAddTriggeredByCardIfBuildWasTriggeredByUnknown() {
        when(mBuildDetails.isTriggeredByVcs()).thenReturn(false);
        when(mBuildDetails.isTriggeredByUnknown()).thenReturn(true);
        when(mBuildDetails.getTriggeredDetails()).thenReturn(TRIGGERED_DETAILS);
        mPresenter.onSuccess(mBuildDetails);
        verify(mBuildDetails).isTriggeredByVcs();
        verify(mBuildDetails).isTriggeredByUnknown();
        verify(mBuildDetails).getTriggeredDetails();
        verify(mView).addTriggeredByCard(eq(TRIGGERED_DETAILS));
    }

    @Test
    public void testAddTriggeredByCardIfBuildWasTriggeredByUser() {
        when(mBuildDetails.isTriggeredByVcs()).thenReturn(false);
        when(mBuildDetails.isTriggeredByUnknown()).thenReturn(false);
        when(mBuildDetails.isTriggeredByUser()).thenReturn(true);
        when(mBuildDetails.getUserNameOfUserWhoTriggeredBuild()).thenReturn(USER);
        mPresenter.onSuccess(mBuildDetails);
        verify(mBuildDetails).isTriggeredByVcs();
        verify(mBuildDetails).isTriggeredByUnknown();
        verify(mBuildDetails).isTriggeredByUser();
        verify(mBuildDetails).getUserNameOfUserWhoTriggeredBuild();
        verify(mView).addTriggeredByCard(eq(USER));
    }

    @Test
    public void testAddTriggeredByCardIfBuildWasRestarted() {
        when(mBuildDetails.isTriggeredByVcs()).thenReturn(false);
        when(mBuildDetails.isTriggeredByUnknown()).thenReturn(false);
        when(mBuildDetails.isTriggeredByUser()).thenReturn(false);
        when(mBuildDetails.isRestarted()).thenReturn(true);
        when(mBuildDetails.getUserNameOfUserWhoTriggeredBuild()).thenReturn(USER);
        mPresenter.onSuccess(mBuildDetails);
        verify(mBuildDetails).isTriggeredByVcs();
        verify(mBuildDetails).isTriggeredByUnknown();
        verify(mBuildDetails).isTriggeredByUser();
        verify(mBuildDetails).isRestarted();
        verify(mBuildDetails).getUserNameOfUserWhoTriggeredBuild();
        verify(mView).addRestartedByCard(eq(USER));
    }

    @Test
    public void testAddTriggeredByCardIfBuildWasTriggeredByBuildType() {
        when(mBuildDetails.isTriggeredByVcs()).thenReturn(false);
        when(mBuildDetails.isTriggeredByUnknown()).thenReturn(false);
        when(mBuildDetails.isTriggeredByUser()).thenReturn(false);
        when(mBuildDetails.isRestarted()).thenReturn(false);
        when(mBuildDetails.isTriggeredByBuildType()).thenReturn(true);
        when(mBuildDetails.getNameOfTriggeredBuildType()).thenReturn("buildType");
        mPresenter.onSuccess(mBuildDetails);
        verify(mBuildDetails).isTriggeredByVcs();
        verify(mBuildDetails).isTriggeredByUnknown();
        verify(mBuildDetails).isTriggeredByUser();
        verify(mBuildDetails).isRestarted();
        verify(mBuildDetails).isTriggeredByBuildType();
        verify(mBuildDetails).getNameOfTriggeredBuildType();
        verify(mView).addTriggeredByCard(eq("buildType"));
    }

    @Test
    public void testAddTriggeredByCardIfBuildWasTriggeredByUnknownTrigger() {
        when(mBuildDetails.isTriggeredByVcs()).thenReturn(false);
        when(mBuildDetails.isTriggeredByUnknown()).thenReturn(false);
        when(mBuildDetails.isTriggeredByUser()).thenReturn(false);
        when(mBuildDetails.isRestarted()).thenReturn(false);
        when(mBuildDetails.isTriggeredByBuildType()).thenReturn(false);
        mPresenter.onSuccess(mBuildDetails);
        verify(mBuildDetails).isTriggeredByVcs();
        verify(mBuildDetails).isTriggeredByUnknown();
        verify(mBuildDetails).isTriggeredByUser();
        verify(mBuildDetails).isRestarted();
        verify(mBuildDetails).isTriggeredByBuildType();
        verify(mView).addTriggeredByUnknownTriggerTypeCard();
    }

    @Test
    public void testAddPersonalCardIfBuildWasTriggeredByUser() {
        when(mBuildDetails.isPersonal()).thenReturn(true);
        when(mBuildDetails.getUserNameOfUserWhoTriggeredBuild()).thenReturn(USER);
        mPresenter.onSuccess(mBuildDetails);
        verify(mBuildDetails).isPersonal();
        verify(mBuildDetails).getUserNameOfUserWhoTriggeredBuild();
        verify(mView).addPersonalCard(eq(USER));
    }

    @Test
    public void testAddBuildTypeAndProjectCard() {
        when(mBuildDetails.hasBuildTypeInfo()).thenReturn(true);
        when(mBuildDetails.getBuildTypeName()).thenReturn("bt_name");
        when(mBuildDetails.getProjectName()).thenReturn("p_name");
        mPresenter.onSuccess(mBuildDetails);
        verify(mBuildDetails).hasBuildTypeInfo();
        verify(mBuildDetails).getBuildTypeName();
        verify(mView).addBuildTypeNameCard(eq("bt_name"));
        verify(mBuildDetails).getProjectName();
        verify(mView).addBuildTypeProjectNameCard(eq("p_name"));
    }

}