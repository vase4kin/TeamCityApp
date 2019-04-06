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

    private final OverviewView view;
    private final OverViewInteractor interactor;
    private final OverviewTracker tracker;
    private final OnboardingManager onboardingManager;

    @Inject
    OverviewPresenterImpl(OverviewView view,
                          OverViewInteractor interactor,
                          OverviewTracker tracker,
                          OnboardingManager onboardingManager) {
        this.view = view;
        this.interactor = interactor;
        this.tracker = tracker;
        this.onboardingManager = onboardingManager;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate() {
        view.initViews(this);
        interactor.setListener(this);
        view.showSkeletonView();
        BuildDetails buildDetails = interactor.getBuildDetails();
        loadBuildDetails(buildDetails.isRunning());
    }

    /**
     * Load build details
     *
     * @param update - Force cache update
     */
    private void loadBuildDetails(boolean update) {
        String buildHref = interactor.getBuildDetails().getHref();
        interactor.load(buildHref, this, update);
    }

    @Override
    public void onDestroy() {
        view.unbindViews();
        interactor.unsubscribe();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStart() {
        interactor.subscribeToEventBusEvents();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStop() {
        interactor.unsubsribeFromEventBusEvents();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onResume() {
        BuildDetails buildDetails = interactor.getBuildDetails();
        if (buildDetails.isFinished() && !onboardingManager.isRestartBuildPromptShown()) {
            view.showRestartBuildPrompt(onboardingManager::saveRestartBuildPromptShown);
        } else if (buildDetails.isRunning() && !onboardingManager.isStopBuildPromptShown()) {
            view.showStopBuildPrompt(onboardingManager::saveStopBuildPromptShown);
        } else if (buildDetails.isQueued() && !onboardingManager.isRemoveBuildFromQueuePromptShown()) {
            view.showRemoveBuildFromQueuePrompt(onboardingManager::saveRemoveBuildFromQueuePromptShown);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        BuildDetails buildDetails = interactor.getBuildDetails();
        if (buildDetails.isRunning()) {
            view.createStopBuildOptionsMenu(menu, inflater);
        } else if (buildDetails.isQueued()) {
            view.createRemoveBuildFromQueueOptionsMenu(menu, inflater);
        } else {
            view.createDefaultOptionsMenu(menu, inflater);
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
        return view.onOptionsItemSelected(item);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDataRefreshEvent() {
        view.showRefreshingProgress();
        view.hideErrorView();
        loadBuildDetails(true);
        // TODO: Disable cancel build menu when data is updated and build has finished status
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onNavigateToBuildListEvent(String branchName) {
        interactor.postStartBuildListActivityFilteredByBranchEvent(branchName);
        tracker.trackUserWantsToSeeBuildListFilteredByBranch();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onNavigateToBuildListEvent() {
        interactor.postStartBuildListActivityEvent();
        tracker.trackUserOpensBuildType();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onNavigateToProjectEvent() {
        interactor.postStartProjectActivityEvent();
        tracker.trackUserOpensProject();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCancelBuildContextMenuClick() {
        interactor.postStopBuildEvent();
        tracker.trackUserClickedCancelBuildOption();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onShareButtonClick() {
        interactor.postShareBuildInfoEvent();
        tracker.trackUserSharedBuild();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onRestartBuildButtonClick() {
        interactor.postRestartBuildEvent();
        tracker.trackUserRestartedBuild();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onBranchCardClick(String branch) {
        view.showBranchCardBottomSheetDialog(branch);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onBuildTypeCardClick(String value) {
        view.showBuildTypeCardBottomSheetDialog(value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onProjectCardClick(String value) {
        view.showProjectCardBottomSheetDialog(value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCardClick(String header, String value) {
        view.showDefaultCardBottomSheetDialog(header, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onBottomSheetDismiss() {
        interactor.postFABVisibleEvent();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onBottomSheetShow() {
        interactor.postFABGoneEvent();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onRefresh() {
        view.hideErrorView();
        loadBuildDetails(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onRetry() {
        view.hideErrorView();
        view.showRefreshingProgress();
        loadBuildDetails(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onSuccess(BuildDetails buildDetails) {
        view.hideCards();
        view.hideSkeletonView();
        view.hideRefreshingProgress();
        // Status
        String statusIcon = buildDetails.getStatusIcon();
        String statusText = buildDetails.getStatusText();
        if (buildDetails.isQueued()) {
            view.addWaitReasonStatusCard(statusIcon, statusText);
        } else {
            view.addResultStatusCard(statusIcon, statusText);
        }
        // Cancellation info
        if (buildDetails.hasCancellationInfo()) {
            // Cancelled by user
            if (buildDetails.hasUserInfoWhoCancelledBuild()) {
                String userName = buildDetails.getUserNameWhoCancelledBuild();
                view.addCancelledByCard(statusIcon, userName);
            }
            // Cancellation time
            String cancellationTime = buildDetails.getCancellationTime();
            view.addCancellationTimeCard(cancellationTime);
        }
        // Time
        if (buildDetails.isRunning()) {
            String startTime = buildDetails.getStartDate();
            view.addTimeCard(startTime);
        } else if (buildDetails.isQueued()) {
            String queuedTime = buildDetails.getQueuedDate();
            view.addQueuedTimeCard(queuedTime);
        } else {
            String finishTime = buildDetails.getFinishTime();
            view.addTimeCard(finishTime);
        }
        // Estimated start time
        if (buildDetails.isQueued() && !TextUtils.isEmpty(buildDetails.getEstimatedStartTime())) {
            String estimatedStartTime = buildDetails.getEstimatedStartTime();
            view.addEstimatedTimeToStartCard(estimatedStartTime);
        }
        // Branch
        if (!TextUtils.isEmpty(buildDetails.getBranchName())) {
            String branchName = buildDetails.getBranchName();
            view.addBranchCard(branchName);
        }
        // Agent
        if (buildDetails.hasAgentInfo()) {
            String agentName = buildDetails.getAgentName();
            view.addAgentCard(agentName);
        }
        // Triggered by
        if (buildDetails.isTriggeredByVcs()) {
            String vcsName = buildDetails.getTriggeredDetails();
            view.addTriggeredByCard(vcsName);
        } else if (buildDetails.isTriggeredByUnknown()) {
            String unknownConfigurationInfo = buildDetails.getTriggeredDetails();
            view.addTriggeredByCard(unknownConfigurationInfo);
        } else if (buildDetails.isTriggeredByUser()) {
            String triggeredUserNameInfo = buildDetails.getUserNameOfUserWhoTriggeredBuild();
            view.addTriggeredByCard(triggeredUserNameInfo);
        } else if (buildDetails.isRestarted()) {
            String restartedUserInfo = buildDetails.getUserNameOfUserWhoTriggeredBuild();
            view.addRestartedByCard(restartedUserInfo);
        } else if (buildDetails.isTriggeredByBuildType()) {
            String buildTypeName = buildDetails.getNameOfTriggeredBuildType();
            view.addTriggeredByCard(buildTypeName);
        } else {
            view.addTriggeredByUnknownTriggerTypeCard();
        }

        // Is personal build
        if (buildDetails.isPersonal()) {
            String userWhoTriggeredBuild = buildDetails.getUserNameOfUserWhoTriggeredBuild();
            view.addPersonalCard(userWhoTriggeredBuild);
        }

        // has build type info
        if (buildDetails.hasBuildTypeInfo()) {
            String buildTypeName = buildDetails.getBuildTypeName();
            view.addBuildTypeNameCard(buildTypeName);
            String projectName = buildDetails.getProjectName();
            view.addBuildTypeProjectNameCard(projectName);
        }

        // Show all added cards
        view.showCards();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onFail(String errorMessage) {
        view.hideCards();
        view.hideSkeletonView();
        view.hideRefreshingProgress();
        view.showErrorView();
    }
}
