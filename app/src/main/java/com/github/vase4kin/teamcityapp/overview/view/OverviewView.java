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

package com.github.vase4kin.teamcityapp.overview.view;

import android.support.v4.widget.SwipeRefreshLayout;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.github.vase4kin.teamcityapp.onboarding.OnboardingManager;

import tr.xip.errorview.ErrorView;

public interface OverviewView {

    /**
     * Init views and set listener {@link ViewListener}
     *
     * @param listener - listener to receive UI updates
     */
    void initViews(ViewListener listener);

    /**
     * Show skeleton view
     */
    void showSkeletonView();

    /**
     * Hide skeleton view
     */
    void hideSkeletonView();

    /**
     * Show refreshing progress
     */
    void showRefreshingProgress();

    /**
     * Hide refreshing progress
     */
    void hideRefreshingProgress();

    /**
     * Show error view with error message
     */
    void showErrorView();

    /**
     * Hide error view
     */
    void hideErrorView();

    /**
     * Unbind views
     */
    void unbindViews();

    /**
     * Add wait reason card
     *
     * @param icon       - icon to setup with
     * @param waitReason - text with setup with
     */
    void addWaitReasonStatusCard(String icon, String waitReason);

    /**
     * Add result status card
     *
     * @param icon   - icon to setup with
     * @param result - text with setup with
     */
    void addResultStatusCard(String icon, String result);

    /**
     * Add cancelled by card
     *
     * @param icon     - icon to setup with
     * @param userName - text with setup with
     */
    void addCancelledByCard(String icon, String userName);

    /**
     * Add cancellation time card
     *
     * @param cancellationTime - text with setup with
     */
    void addCancellationTimeCard(String cancellationTime);

    /**
     * Add time card
     *
     * @param time - text with setup with
     */
    void addTimeCard(String time);

    /**
     * Add queued time card
     *
     * @param time - text with setup with
     */
    void addQueuedTimeCard(String time);

    /**
     * Add estimated time to start card
     *
     * @param time - text with setup with
     */
    void addEstimatedTimeToStartCard(String time);

    /**
     * Add branch card
     *
     * @param branchName - branch name with setup with
     */
    void addBranchCard(String branchName);

    /**
     * Add agent card
     *
     * @param agentName - agent name with setup with
     */
    void addAgentCard(String agentName);

    /**
     * Add triggered by card
     *
     * @param triggeredBy - trigger name with setup with
     */
    void addTriggeredByCard(String triggeredBy);

    /**
     * Add triggered by unknown card
     */
    void addTriggeredByUnknownTriggerTypeCard();

    /**
     * Add card showing that build is personal
     *
     * @param userName - user who triggered personal build
     */
    void addPersonalCard(String userName);

    /**
     * Add restarted by card
     *
     * @param restartedBy - restarted with setup with
     */
    void addRestartedByCard(String restartedBy);

    /**
     * Add card showing build's build type
     *
     * @param buildTypeName - build's build type name
     */
    void addBuildTypeNameCard(String buildTypeName);

    /**
     * Add card showing build's project
     *
     * @param buildTypeProjectName - build's project name
     */
    void addBuildTypeProjectNameCard(String buildTypeProjectName);

    /**
     * Show build details cards
     */
    void showCards();

    /**
     * Hide build details cards
     */
    void hideCards();

    /**
     * {@inheritDoc}
     * <p>
     * See {@link android.support.v4.app.Fragment#onCreateOptionsMenu(Menu, MenuInflater)} )}
     */
    void createStopBuildOptionsMenu(Menu menu, MenuInflater inflater);

    /**
     * {@inheritDoc}
     * <p>
     * See {@link android.support.v4.app.Fragment#onCreateOptionsMenu(Menu, MenuInflater)} )}
     */
    void createRemoveBuildFromQueueOptionsMenu(Menu menu, MenuInflater inflater);

    /**
     * {@inheritDoc}
     * <p>
     * See {@link android.support.v4.app.Fragment#onCreateOptionsMenu(Menu, MenuInflater)} )}
     */
    void createDefaultOptionsMenu(Menu menu, MenuInflater inflater);

    /**
     * {@inheritDoc}
     * <p>
     * See {@link android.support.v4.app.Fragment#onOptionsItemSelected(MenuItem)}
     */
    boolean onOptionsItemSelected(MenuItem item);

    /**
     * Show bottom sheet dialog for default card
     *
     * @param header      - header of the card
     * @param description - description of the card
     */
    void showDefaultCardBottomSheetDialog(String header, String description);

    /**
     * Show bottom sheet dialog for branch card
     *
     * @param description - description of the card
     */
    void showBranchCardBottomSheetDialog(String description);

    /**
     * Show bottom sheet dialog for build type card
     *
     * @param description - description of the card
     */
    void showBuildTypeCardBottomSheetDialog(String description);

    /**
     * Show bottom sheet dialog for project card
     *
     * @param description - description of the card
     */
    void showProjectCardBottomSheetDialog(String description);

    /**
     * Show stop build onboarding prompt
     *
     * @param listener - Listener to know when prompt is shown
     */
    void showStopBuildPrompt(OnboardingManager.OnPromptShownListener listener);

    /**
     * Show restart build onboarding prompt
     *
     * @param listener - Listener to know when prompt is shown
     */
    void showRestartBuildPrompt(OnboardingManager.OnPromptShownListener listener);

    /**
     * Show remove build from queue onboarding prompt
     *
     * @param listener - Listener to know when prompt is shown
     */
    void showRemoveBuildFromQueuePrompt(OnboardingManager.OnPromptShownListener listener);

    /**
     * Listener to handle interactions between view and presenter
     */
    interface ViewListener extends ErrorView.RetryListener, SwipeRefreshLayout.OnRefreshListener {
        /**
         * On stop build context menu clicked
         */
        void onCancelBuildContextMenuClick();

        /**
         * On menu share button click
         */
        void onShareButtonClick();

        /**
         * On menu restart build button click
         */
        void onRestartBuildButtonClick();

        /**
         * On card click
         */
        void onCardClick(String header, String value);

        /**
         * On branch card click
         */
        void onBranchCardClick(String value);

        /**
         * On build type card click
         */
        void onBuildTypeCardClick(String value);

        /**
         * On project card click
         */
        void onProjectCardClick(String value);

        /**
         * On bottom sheet show
         */
        void onBottomSheetShow();

        /**
         * On bottom sheet dismiss
         */
        void onBottomSheetDismiss();
    }
}
