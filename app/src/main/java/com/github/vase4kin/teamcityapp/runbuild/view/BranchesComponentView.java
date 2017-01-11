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


import java.util.List;

/**
 * Interface to handle view interactions with branches view component
 */
public interface BranchesComponentView {

    /**
     * Init views
     */
    void initViews();

    /**
     * Unbind views
     */
    void unbindViews();

    /**
     * Hide branches loading progress
     */
    void hideBranchesLoadingProgress();

    /**
     * Setup autocomplete
     *
     * @param branches - List of branches
     */
    void setupAutoComplete(List<String> branches);

    /**
     * Setup autocomplete for single branch
     *
     * @param branches - List containing single branch
     */
    void setupAutoCompleteForSingleBranch(List<String> branches);

    /**
     * Show no branches available
     */
    void showNoBranchesAvailable();

    /**
     * Show branches auto complete
     */
    void showBranchesAutoComplete();

    /**
     * @return Current selected branch
     */
    String getBranchName();
}
