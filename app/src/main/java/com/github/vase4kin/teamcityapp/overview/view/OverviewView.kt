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

package com.github.vase4kin.teamcityapp.overview.view

import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import teamcityapp.libraries.onboarding.OnboardingManager
import tr.xip.errorview.ErrorView

interface OverviewView {

    /**
     * Init views and set listener [ViewListener]
     *
     * @param listener - listener to receive UI updates
     */
    fun initViews(listener: ViewListener)

    /**
     * Show skeleton view
     */
    fun showSkeletonView()

    /**
     * Hide skeleton view
     */
    fun hideSkeletonView()

    /**
     * Show refreshing progress
     */
    fun showRefreshingProgress()

    /**
     * Hide refreshing progress
     */
    fun hideRefreshingProgress()

    /**
     * Show error view with error message
     */
    fun showErrorView()

    /**
     * Hide error view
     */
    fun hideErrorView()

    /**
     * Unbind views
     */
    fun unbindViews()

    /**
     * Add wait reason card
     *
     * @param icon - icon to setup with
     * @param waitReason - text with setup with
     */
    fun addWaitReasonStatusCard(icon: Int, waitReason: String)

    /**
     * Add result status card
     *
     * @param icon - icon to setup with
     * @param result - text with setup with
     */
    fun addResultStatusCard(icon: Int, result: String)

    /**
     * Add cancelled by card
     *
     * @param icon - icon to setup with
     * @param userName - text with setup with
     */
    fun addCancelledByCard(icon: Int, userName: String)

    /**
     * Add cancellation time card
     *
     * @param cancellationTime - text with setup with
     */
    fun addCancellationTimeCard(cancellationTime: String)

    /**
     * Add time card
     *
     * @param time - text with setup with
     */
    fun addTimeCard(time: String)

    /**
     * Add queued time card
     *
     * @param time - text with setup with
     */
    fun addQueuedTimeCard(time: String)

    /**
     * Add estimated time to start card
     *
     * @param time - text with setup with
     */
    fun addEstimatedTimeToStartCard(time: String)

    /**
     * Add branch card
     *
     * @param branchName - branch name with setup with
     */
    fun addBranchCard(branchName: String)

    /**
     * Add agent card
     *
     * @param agentName - agent name with setup with
     */
    fun addAgentCard(agentName: String)

    /**
     * Add triggered by card
     *
     * @param triggeredBy - trigger name with setup with
     */
    fun addTriggeredByCard(triggeredBy: String)

    /**
     * Add triggered by unknown card
     */
    fun addTriggeredByUnknownTriggerTypeCard()

    /**
     * Add card showing that build is personal
     *
     * @param userName - user who triggered personal build
     */
    fun addPersonalCard(userName: String)

    /**
     * Add restarted by card
     *
     * @param restartedBy - restarted with setup with
     */
    fun addRestartedByCard(restartedBy: String)

    /**
     * Add card showing build's build type
     *
     * @param buildTypeName - build's build type name
     */
    fun addBuildTypeNameCard(buildTypeName: String)

    /**
     * Add card showing build's project
     *
     * @param buildTypeProjectName - build's project name
     */
    fun addBuildTypeProjectNameCard(buildTypeProjectName: String)

    /**
     * Show build details cards
     */
    fun showCards()

    /**
     * Hide build details cards
     */
    fun hideCards()

    /**
     * {@inheritDoc}
     *
     *
     * See [Fragment.onCreateOptionsMenu] )}
     */
    fun createStopBuildOptionsMenu(menu: Menu, inflater: MenuInflater)

    /**
     * {@inheritDoc}
     *
     *
     * See [Fragment.onCreateOptionsMenu] )}
     */
    fun createRemoveBuildFromQueueOptionsMenu(menu: Menu, inflater: MenuInflater)

    /**
     * {@inheritDoc}
     *
     *
     * See [Fragment.onCreateOptionsMenu] )}
     */
    fun createDefaultOptionsMenu(menu: Menu, inflater: MenuInflater)

    /**
     * {@inheritDoc}
     *
     *
     * See [Fragment.onOptionsItemSelected]
     */
    fun onOptionsItemSelected(item: MenuItem): Boolean

    /**
     * Show bottom sheet dialog for default card
     *
     * @param header - header of the card
     * @param description - description of the card
     */
    fun showDefaultCardBottomSheetDialog(header: String, description: String)

    /**
     * Show bottom sheet dialog for branch card
     *
     * @param description - description of the card
     */
    fun showBranchCardBottomSheetDialog(description: String)

    /**
     * Show bottom sheet dialog for build type card
     *
     * @param description - description of the card
     */
    fun showBuildTypeCardBottomSheetDialog(description: String)

    /**
     * Show bottom sheet dialog for project card
     *
     * @param description - description of the card
     */
    fun showProjectCardBottomSheetDialog(description: String)

    /**
     * Show stop build onboarding prompt
     *
     * @param listener - Listener to know when prompt is shown
     */
    fun showStopBuildPrompt(listener: OnboardingManager.OnPromptShownListener)

    /**
     * Show restart build onboarding prompt
     *
     * @param listener - Listener to know when prompt is shown
     */
    fun showRestartBuildPrompt(listener: OnboardingManager.OnPromptShownListener)

    /**
     * Show remove build from queue onboarding prompt
     *
     * @param listener - Listener to know when prompt is shown
     */
    fun showRemoveBuildFromQueuePrompt(listener: OnboardingManager.OnPromptShownListener)

    /**
     * Listener to handle interactions between view and presenter
     */
    interface ViewListener : ErrorView.RetryListener, SwipeRefreshLayout.OnRefreshListener {
        /**
         * On stop build context menu clicked
         */
        fun onCancelBuildContextMenuClick()

        /**
         * On menu share button click
         */
        fun onShareButtonClick()

        /**
         * On menu restart build button click
         */
        fun onRestartBuildButtonClick()

        /**
         * Open browser
         */
        fun onOpenBrowser()

        /**
         * On card click
         */
        fun onCardClick(header: String, value: String)

        /**
         * On branch card click
         */
        fun onBranchCardClick(value: String)

        /**
         * On build type card click
         */
        fun onBuildTypeCardClick(value: String)

        /**
         * On project card click
         */
        fun onProjectCardClick(value: String)

        /**
         * On bottom sheet show
         */
        fun onBottomSheetShow()

        /**
         * On bottom sheet dismiss
         */
        fun onBottomSheetDismiss()
    }
}
