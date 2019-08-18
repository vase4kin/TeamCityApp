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

import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener
import com.github.vase4kin.teamcityapp.onboarding.OnboardingManager
import com.github.vase4kin.teamcityapp.overview.data.BuildDetails
import com.github.vase4kin.teamcityapp.overview.data.OverViewInteractor
import com.github.vase4kin.teamcityapp.overview.tracker.OverviewTracker
import com.github.vase4kin.teamcityapp.overview.view.OverviewView
import javax.inject.Inject

/**
 * Impl of [OverviewPresenter]
 */
class OverviewPresenterImpl @Inject internal constructor(
    private val view: OverviewView,
    private val interactor: OverViewInteractor,
    private val tracker: OverviewTracker,
    private val onboardingManager: OnboardingManager
) : OverviewPresenter, OverviewView.ViewListener, OverViewInteractor.OnOverviewEventsListener,
    OnLoadingListener<BuildDetails> {

    /**
     * {@inheritDoc}
     */
    override fun onCreate() {
        view.initViews(this)
        interactor.setListener(this)
        view.showSkeletonView()
        val buildDetails = interactor.buildDetails
        loadBuildDetails(buildDetails.isRunning)
    }

    /**
     * Load build details
     *
     * @param update - Force cache update
     */
    private fun loadBuildDetails(update: Boolean) {
        val buildHref = interactor.buildDetails.href
        interactor.load(buildHref, this, update)
    }

    override fun onDestroy() {
        view.unbindViews()
        interactor.unsubscribe()
    }

    /**
     * {@inheritDoc}
     */
    override fun onStart() {
        interactor.subscribeToEventBusEvents()
    }

    /**
     * {@inheritDoc}
     */
    override fun onStop() {
        interactor.unsubsribeFromEventBusEvents()
    }

    /**
     * {@inheritDoc}
     */
    override fun onResume() {
        val buildDetails = interactor.buildDetails
        if (buildDetails.isFinished && !onboardingManager.isRestartBuildPromptShown) {
            view.showRestartBuildPrompt(object : OnboardingManager.OnPromptShownListener {
                override fun onPromptShown() {
                    onboardingManager.saveRestartBuildPromptShown()
                }
            })
        } else if (buildDetails.isRunning && !onboardingManager.isStopBuildPromptShown) {
            view.showStopBuildPrompt(object : OnboardingManager.OnPromptShownListener {
                override fun onPromptShown() {
                    onboardingManager.saveStopBuildPromptShown()
                }
            })
        } else if (buildDetails.isQueued && !onboardingManager.isRemoveBuildFromQueuePromptShown) {
            view.showRemoveBuildFromQueuePrompt(object : OnboardingManager.OnPromptShownListener {
                override fun onPromptShown() {
                    onboardingManager.saveRemoveBuildFromQueuePromptShown()
                }
            })
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        val buildDetails = interactor.buildDetails
        when {
            buildDetails.isRunning -> view.createStopBuildOptionsMenu(menu, inflater)
            buildDetails.isQueued -> view.createRemoveBuildFromQueueOptionsMenu(menu, inflater)
            else -> view.createDefaultOptionsMenu(menu, inflater)
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun onPrepareOptionsMenu(menu: Menu) {}

    /**
     * {@inheritDoc}
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return view.onOptionsItemSelected(item)
    }

    /**
     * {@inheritDoc}
     */
    override fun onDataRefreshEvent() {
        view.showRefreshingProgress()
        view.hideErrorView()
        loadBuildDetails(true)
        // TODO: Disable cancel build menu when data is updated and build has finished status
    }

    /**
     * {@inheritDoc}
     */
    override fun onNavigateToBuildListEvent(branchName: String) {
        interactor.postStartBuildListActivityFilteredByBranchEvent(branchName)
        tracker.trackUserWantsToSeeBuildListFilteredByBranch()
    }

    /**
     * {@inheritDoc}
     */
    override fun onNavigateToBuildListEvent() {
        interactor.postStartBuildListActivityEvent()
        tracker.trackUserOpensBuildType()
    }

    /**
     * {@inheritDoc}
     */
    override fun onNavigateToProjectEvent() {
        interactor.postStartProjectActivityEvent()
        tracker.trackUserOpensProject()
    }

    /**
     * {@inheritDoc}
     */
    override fun onCancelBuildContextMenuClick() {
        interactor.postStopBuildEvent()
        tracker.trackUserClickedCancelBuildOption()
    }

    /**
     * {@inheritDoc}
     */
    override fun onShareButtonClick() {
        interactor.postShareBuildInfoEvent()
        tracker.trackUserSharedBuild()
    }

    /**
     * {@inheritDoc}
     */
    override fun onRestartBuildButtonClick() {
        interactor.postRestartBuildEvent()
        tracker.trackUserRestartedBuild()
    }

    /**
     * {@inheritDoc}
     */
    override fun onOpenBrowser() {
        interactor.postOpenBrowserEvent()
        // TODO: Track
    }

    /**
     * {@inheritDoc}
     */
    override fun onBranchCardClick(@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE") branch: String) {
        view.showBranchCardBottomSheetDialog(branch)
    }

    /**
     * {@inheritDoc}
     */
    override fun onBuildTypeCardClick(value: String) {
        view.showBuildTypeCardBottomSheetDialog(value)
    }

    /**
     * {@inheritDoc}
     */
    override fun onProjectCardClick(value: String) {
        view.showProjectCardBottomSheetDialog(value)
    }

    /**
     * {@inheritDoc}
     */
    override fun onCardClick(header: String, value: String) {
        view.showDefaultCardBottomSheetDialog(header, value)
    }

    /**
     * {@inheritDoc}
     */
    override fun onBottomSheetDismiss() {
        interactor.postFABVisibleEvent()
    }

    /**
     * {@inheritDoc}
     */
    override fun onBottomSheetShow() {
        interactor.postFABGoneEvent()
    }

    /**
     * {@inheritDoc}
     */
    override fun onRefresh() {
        view.hideErrorView()
        loadBuildDetails(true)
    }

    /**
     * {@inheritDoc}
     */
    override fun onRetry() {
        view.hideErrorView()
        view.showRefreshingProgress()
        loadBuildDetails(true)
    }

    /**
     * {@inheritDoc}
     */
    override fun onSuccess(@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE") buildDetails: BuildDetails) {
        view.hideCards()
        view.hideSkeletonView()
        view.hideRefreshingProgress()
        // Status
        val statusIcon = buildDetails.statusIcon
        val statusText = buildDetails.statusText
        if (buildDetails.isQueued) {
            view.addWaitReasonStatusCard(statusIcon, statusText)
        } else {
            view.addResultStatusCard(statusIcon, statusText)
        }
        // Cancellation info
        if (buildDetails.hasCancellationInfo()) {
            // Cancelled by user
            if (buildDetails.hasUserInfoWhoCancelledBuild()) {
                val userName = buildDetails.userNameWhoCancelledBuild
                view.addCancelledByCard(statusIcon, userName)
            }
            // Cancellation time
            val cancellationTime = buildDetails.cancellationTime
            view.addCancellationTimeCard(cancellationTime)
        }
        // Time
        when {
            buildDetails.isRunning -> {
                val startTime = buildDetails.startDate
                view.addTimeCard(startTime)
            }
            buildDetails.isQueued -> {
                val queuedTime = buildDetails.queuedDate
                view.addQueuedTimeCard(queuedTime)
            }
            else -> {
                val finishTime = buildDetails.finishTime
                view.addTimeCard(finishTime)
            }
        }
        // Estimated start time
        val estimatedStartTime = buildDetails.estimatedStartTime
        if (buildDetails.isQueued && !estimatedStartTime.isNullOrEmpty()) {
            view.addEstimatedTimeToStartCard(estimatedStartTime)
        }
        // Branch
        val branchName = buildDetails.branchName
        if (!branchName.isNullOrEmpty()) {
            view.addBranchCard(branchName)
        }
        // Agent
        if (buildDetails.hasAgentInfo()) {
            val agentName = buildDetails.agentName
            view.addAgentCard(agentName)
        }
        // Triggered by
        when {
            buildDetails.isTriggeredByVcs -> {
                val vcsName = buildDetails.triggeredDetails
                view.addTriggeredByCard(vcsName)
            }
            buildDetails.isTriggeredByUnknown -> {
                val unknownConfigurationInfo = buildDetails.triggeredDetails
                view.addTriggeredByCard(unknownConfigurationInfo)
            }
            buildDetails.isTriggeredByUser -> {
                val triggeredUserNameInfo = buildDetails.userNameOfUserWhoTriggeredBuild
                view.addTriggeredByCard(triggeredUserNameInfo)
            }
            buildDetails.isRestarted -> {
                val restartedUserInfo = buildDetails.userNameOfUserWhoTriggeredBuild
                view.addRestartedByCard(restartedUserInfo)
            }
            buildDetails.isTriggeredByBuildType -> {
                val buildTypeName = buildDetails.nameOfTriggeredBuildType
                view.addTriggeredByCard(buildTypeName)
            }
            else -> view.addTriggeredByUnknownTriggerTypeCard()
        }

        // Is personal build
        if (buildDetails.isPersonal) {
            val userWhoTriggeredBuild = buildDetails.userNameOfUserWhoTriggeredBuild
            view.addPersonalCard(userWhoTriggeredBuild)
        }

        // has build type info
        if (buildDetails.hasBuildTypeInfo()) {
            val buildTypeName = buildDetails.buildTypeName
            view.addBuildTypeNameCard(buildTypeName)
            val projectName = buildDetails.projectName
            view.addBuildTypeProjectNameCard(projectName)
        }

        // Show all added cards
        view.showCards()
    }

    /**
     * {@inheritDoc}
     */
    override fun onFail(errorMessage: String) {
        view.hideCards()
        view.hideSkeletonView()
        view.hideRefreshingProgress()
        view.showErrorView()
    }
}
