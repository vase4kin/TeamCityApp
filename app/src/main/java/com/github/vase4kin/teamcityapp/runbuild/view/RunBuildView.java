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

package com.github.vase4kin.teamcityapp.runbuild.view;

import com.github.vase4kin.teamcityapp.account.create.view.OnToolBarNavigationListener;

import java.util.List;

/**
 * View to handle interaction with {@link RunBuildActivity}
 */
public interface RunBuildView {

    /**
     * Init views
     *
     * @param listener - to handle view interactions
     */
    void initViews(ViewListener listener);

    /**
     * Show posting build progress
     */
    void showQueuingBuildProgress();

    /**
     * Hide posting build progress
     */
    void hideQueuingBuildProgress();

    /**
     * Show forbidden error snackbar
     */
    void showForbiddenErrorSnackbar();

    /**
     * show error snack bar
     */
    void showErrorSnackbar();

    /**
     * Unbind views
     */
    void unbindViews();

    /**
     * Disable agent selection control
     */
    void disableAgentSelectionControl();

    /**
     * Enable agent selection control
     */
    void enableAgentSelectionControl();

    /**
     * Show selected agent view
     */
    void showSelectedAgentView();

    /**
     * Set agent selection dialog with agents list
     *
     * @param agents - to set with
     */
    void setAgentListDialogWithAgentsList(List<String> agents);

    /**
     * Hide loading progress
     */
    void hideLoadingAgentsProgress();

    /**
     * Show no agent available
     */
    void showNoAgentsAvailable();

    /**
     * Listener to receive callbacks to presenter
     */
    interface ViewListener extends OnToolBarNavigationListener {

        /**
         * On build queue
         *
         * @param isPersonal    - Is personal flag
         * @param queueToTheTop - Queue to the top
         * @param cleanAllFiles - Clean all files in the checkout directory
         */
        void onBuildQueue(boolean isPersonal,
                          boolean queueToTheTop,
                          boolean cleanAllFiles);

        /**
         * On agent selected in the dialog list
         *
         * @param agentPosition - item position which was selected
         */
        void onAgentSelected(int agentPosition);
    }
}
