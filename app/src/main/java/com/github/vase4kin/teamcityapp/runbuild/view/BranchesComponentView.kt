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

package com.github.vase4kin.teamcityapp.runbuild.view

/**
 * Interface to handle view interactions with branches view component
 */
interface BranchesComponentView {

    /**
     * @return Current selected branch
     */
    val branchName: String

    /**
     * Init views
     */
    fun initViews()

    /**
     * Unbind views
     */
    fun unbindViews()

    /**
     * Hide branches loading progress
     */
    fun hideBranchesLoadingProgress()

    /**
     * Setup autocomplete
     *
     * @param branches - List of branches
     */
    fun setupAutoComplete(branches: List<String>)

    /**
     * Setup autocomplete for single branch
     *
     * @param branches - List containing single branch
     */
    fun setupAutoCompleteForSingleBranch(branches: List<String>)

    /**
     * Show no branches available
     */
    fun showNoBranchesAvailable()

    /**
     * Show no branches available to filter
     */
    fun showNoBranchesAvailableToFilter()

    /**
     * Show branches auto complete
     */
    fun showBranchesAutoComplete()

    /**
     * Set autocomplete field for filter
     */
    fun setAutocompleteHintForFilter()
}
