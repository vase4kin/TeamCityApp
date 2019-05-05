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

package com.github.vase4kin.teamcityapp.runbuild.view

import com.github.vase4kin.teamcityapp.account.create.view.OnToolBarNavigationListener

/**
 * View to handle interaction with [RunBuildActivity]
 */
interface RunBuildView {

    /**
     * Init views
     *
     * @param listener - to handle view interactions
     */
    fun initViews(listener: ViewListener)

    /**
     * Show posting build progress
     */
    fun showQueuingBuildProgress()

    /**
     * Hide posting build progress
     */
    fun hideQueuingBuildProgress()

    /**
     * Show forbidden error snackbar
     */
    fun showForbiddenErrorSnackbar()

    /**
     * show error snack bar
     */
    fun showErrorSnackbar()

    /**
     * Unbind views
     */
    fun unbindViews()

    /**
     * Disable agent selection control
     */
    fun disableAgentSelectionControl()

    /**
     * Enable agent selection control
     */
    fun enableAgentSelectionControl()

    /**
     * Show selected agent view
     */
    fun showSelectedAgentView()

    /**
     * Set agent selection dialog with agents list
     *
     * @param agents - to set with
     */
    fun setAgentListDialogWithAgentsList(agents: List<String>)

    /**
     * Hide loading progress
     */
    fun hideLoadingAgentsProgress()

    /**
     * Show no agent available
     */
    fun showNoAgentsAvailable()

    /**
     * Show add new parameter dialog
     */
    fun showAddParameterDialog()

    /**
     * Hide parameters none text view
     */
    fun hideNoneParametersView()

    /**
     * Show parameters none text view
     */
    fun showNoneParametersView()

    /**
     * Enable clear all parameters button
     */
    fun enableClearAllParametersButton()

    /**
     * Disable clear all parameters button
     */
    fun disableClearAllParametersButton()

    /**
     * Add parameter view
     *
     * @param name  - Parameter name
     * @param value - Parameter value
     */
    fun addParameterView(name: String, value: String)

    /**
     * Remove all parameter views
     */
    fun removeAllParameterViews()

    /**
     * Listener to receive callbacks to presenter
     */
    interface ViewListener : OnToolBarNavigationListener {

        /**
         * On build queue
         *
         * @param isPersonal    - Is personal flag
         * @param queueToTheTop - Queue to the top
         * @param cleanAllFiles - Clean all files in the checkout directory
         */
        fun onBuildQueue(isPersonal: Boolean,
                         queueToTheTop: Boolean,
                         cleanAllFiles: Boolean)

        /**
         * On agent selected in the dialog list
         *
         * @param agentPosition - item position which was selected
         */
        fun onAgentSelected(agentPosition: Int)

        /**
         * On add parameter button click
         */
        fun onAddParameterButtonClick()

        /**
         * On clear all parameters button click
         */
        fun onClearAllParametersButtonClick()

        /**
         * On parameter added
         *
         * @param name  - parameter name
         * @param value - parameter value
         */
        fun onParameterAdded(name: String, value: String)
    }
}
