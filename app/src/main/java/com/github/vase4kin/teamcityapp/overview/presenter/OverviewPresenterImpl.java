/*
 * Copyright 2016 Andrey Tolpeev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.vase4kin.teamcityapp.overview.presenter;

import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener;
import com.github.vase4kin.teamcityapp.onboarding.OnboardingManager;
import com.github.vase4kin.teamcityapp.overview.data.BuildDetails;
import com.github.vase4kin.teamcityapp.overview.data.OverViewInteractor;
import com.github.vase4kin.teamcityapp.overview.tracker.OverviewTracker;
import com.github.vase4kin.teamcityapp.overview.view.OverviewView;

import javax.inject.Inject;

/**
 * Impl of {@link OverviewPresenter}
 */
public class OverviewPresenterImpl implements OverviewPresenter,
        OverviewView.ViewListener,
        OverViewInteractor.OnOverviewEventsListener, OnLoadingListener<BuildDetails> {

    private OverviewView mView;
    private OverViewInteractor mInteractor;
    private OverviewTracker mTracker;
    private OnboardingManager mOnboardingManager;

    @Inject
    OverviewPresenterImpl(OverviewView view,
                          OverViewInteractor interactor,
                          OverviewTracker tracker,
                          OnboardingManager onboardingManager) {
        this.mView = view;
        this.mInteractor = interactor;
        this.mTracker = tracker;
        this.mOnboardingManager = onboardingManager;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate() {
        mView.initViews(this);
        mInteractor.setListener(this);
        mView.showSkeletonView();
        BuildDetails buildDetails = mInteractor.getBuildDetails();
        loadBuildDetails(buildDetails.isRunning());
    }

    /**
     * Load build details
     *
     * @param update - Force cache update
     */
    private void loadBuildDetails(boolean update) {
        String buildHref = mInteractor.getBuildDetails().getHref();
        mInteractor.load(buildHref, this, update);
    }

    @Override
    public void onDestroy() {
        mView.unbindViews();
        mInteractor.unsubscribe();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStart() {
        mInteractor.subscribeToEventBusEvents();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStop() {
        mInteractor.unsubsribeFromEventBusEvents();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onResume() {
        BuildDetails buildDetails = mInteractor.getBuildDetails();
        if (buildDetails.isFinished() && !mOnboardingManager.isRestartBuildPromptShown()) {
            mView.showRestartBuildPrompt(new OnboardingManager.OnPromptShownListener() {
                @Override
                public void onPromptShown() {
                    mOnboardingManager.saveRestartBuildPromptShown();
                }
            });
        } else if (buildDetails.isRunning() && !mOnboardingManager.isStopBuildPromptShown()) {
            mView.showStopBuildPrompt(new OnboardingManager.OnPromptShownListener() {
                @Override
                public void onPromptShown() {
                    mOnboardingManager.saveStopBuildPromptShown();
                }
            });
        } else if (buildDetails.isQueued() && !mOnboardingManager.isRemoveBuildFromQueuePromptShown()) {
            mView.showRemoveBuildFromQueuePrompt(new OnboardingManager.OnPromptShownListener() {
                @Override
                public void onPromptShown() {
                    mOnboardingManager.saveRemoveBuildFromQueuePromptShown();
                }
            });
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        BuildDetails buildDetails = mInteractor.getBuildDetails();
        if (buildDetails.isRunning()) {
            mView.createStopBuildOptionsMenu(menu, inflater);
        } else if (buildDetails.isQueued()) {
            mView.createRemoveBuildFromQueueOptionsMenu(menu, inflater);
        } else {
            mView.createDefaultOptionsMenu(menu, inflater);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPrepareOptionsMenu(Menu menu) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return mView.onOptionsItemSelected(item);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDataRefreshEvent() {
        mView.showRefreshingProgress();
        mView.hideErrorView();
        loadBuildDetails(true);
        // TODO: Disable cancel build menu when data is updated and build has finished status
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onNavigateToBuildListEvent(String branchName) {
        mInteractor.postStartBuildListActivityFilteredByBranchEvent(branchName);
        mTracker.trackUserWantsToSeeBuildListFilteredByBranch();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onNavigateToBuildListEvent() {
        mInteractor.postStartBuildListActivityEvent();
//        TODO: mTracker.trackUserWantsToSeeBuildListFilteredByBranch();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onNavigateToProjectEvent() {
        mInteractor.postStartProjectActivityEvent();
//        TODO: mTracker.trackUserWantsToSeeBuildListFilteredByBranch();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCancelBuildContextMenuClick() {
        mInteractor.postStopBuildEvent();
        mTracker.trackUserClickedCancelBuildOption();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onShareButtonClick() {
        mInteractor.postShareBuildInfoEvent();
        mTracker.trackUserSharedBuild();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onRestartBuildButtonClick() {
        mInteractor.postRestartBuildEvent();
        mTracker.trackUserRestartedBuild();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onBranchCardClick(String branch) {
        mView.showBranchCardBottomSheetDialog(branch);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onBuildTypeCardClick(String value) {
        mView.showBuildTypeCardBottomSheetDialog(value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onProjectCardClick(String value) {
        mView.showProjectCardBottomSheetDialog(value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCardClick(String header, String value) {
        mView.showDefaultCardBottomSheetDialog(header, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onBottomSheetDismiss() {
        mInteractor.postFABVisibleEvent();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onBottomSheetShow() {
        mInteractor.postFABGoneEvent();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onRefresh() {
        mView.hideErrorView();
        loadBuildDetails(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onRetry() {
        mView.hideErrorView();
        mView.showRefreshingProgress();
        loadBuildDetails(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onSuccess(BuildDetails buildDetails) {
        mView.hideCards();
        mView.hideSkeletonView();
        mView.hideRefreshingProgress();
        // Status
        String statusIcon = buildDetails.getStatusIcon();
        String statusText = buildDetails.getStatusText();
        if (buildDetails.isQueued()) {
            mView.addWaitReasonStatusCard(statusIcon, statusText);
        } else {
            mView.addResultStatusCard(statusIcon, statusText);
        }
        // Cancellation info
        if (buildDetails.hasCancellationInfo()) {
            // Cancelled by user
            if (buildDetails.hasUserInfoWhoCancelledBuild()) {
                String userName = buildDetails.getUserNameWhoCancelledBuild();
                mView.addCancelledByCard(statusIcon, userName);
            }
            // Cancellation time
            String cancellationTime = buildDetails.getCancellationTime();
            mView.addCancellationTimeCard(cancellationTime);
        }
        // Time
        if (buildDetails.isRunning()) {
            String startTime = buildDetails.getStartDate();
            mView.addTimeCard(startTime);
        } else if (buildDetails.isQueued()) {
            String queuedTime = buildDetails.getQueuedDate();
            mView.addQueuedTimeCard(queuedTime);
        } else {
            String finishTime = buildDetails.getFinishTime();
            mView.addTimeCard(finishTime);
        }
        // Estimated start time
        if (buildDetails.isQueued() && !TextUtils.isEmpty(buildDetails.getEstimatedStartTime())) {
            String estimatedStartTime = buildDetails.getEstimatedStartTime();
            mView.addEstimatedTimeToStartCard(estimatedStartTime);
        }
        // Branch
        if (!TextUtils.isEmpty(buildDetails.getBranchName())) {
            String branchName = buildDetails.getBranchName();
            mView.addBranchCard(branchName);
        }
        // Agent
        if (buildDetails.hasAgentInfo()) {
            String agentName = buildDetails.getAgentName();
            mView.addAgentCard(agentName);
        }
        // Triggered by
        if (buildDetails.isTriggeredByVcs()) {
            String vcsName = buildDetails.getTriggeredDetails();
            mView.addTriggeredByCard(vcsName);
        } else if (buildDetails.isTriggeredByUnknown()) {
            String unknownConfigurationInfo = buildDetails.getTriggeredDetails();
            mView.addTriggeredByCard(unknownConfigurationInfo);
        } else if (buildDetails.isTriggeredByUser()) {
            String triggeredUserNameInfo = buildDetails.getUserNameOfUserWhoTriggeredBuild();
            mView.addTriggeredByCard(triggeredUserNameInfo);
        } else if (buildDetails.isRestarted()) {
            String restartedUserInfo = buildDetails.getUserNameOfUserWhoTriggeredBuild();
            mView.addRestartedByCard(restartedUserInfo);
        } else if (buildDetails.isTriggeredByBuildType()) {
            String buildTypeName = buildDetails.getNameOfTriggeredBuildType();
            mView.addTriggeredByCard(buildTypeName);
        } else {
            mView.addTriggeredByUnknownTriggerTypeCard();
        }

        // Is personal build
        if (buildDetails.isPersonal()) {
            String userWhoTriggeredBuild = buildDetails.getUserNameOfUserWhoTriggeredBuild();
            mView.addPersonalCard(userWhoTriggeredBuild);
        }

        // has build type info
        if (buildDetails.hasBuildTypeInfo()) {
            String buildTypeName = buildDetails.getBuildTypeName();
            mView.addBuildTypeNameCard(buildTypeName);
            String projectName = buildDetails.getProjectName();
            mView.addBuildTypeProjectNameCard(projectName);
        }

        // Show all added cards
        mView.showCards();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onFail(String errorMessage) {
        mView.hideCards();
        mView.hideSkeletonView();
        mView.hideRefreshingProgress();
        mView.showErrorView();
    }
}
