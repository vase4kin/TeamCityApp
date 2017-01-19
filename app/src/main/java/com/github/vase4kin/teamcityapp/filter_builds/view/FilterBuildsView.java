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

package com.github.vase4kin.teamcityapp.filter_builds.view;


import com.github.vase4kin.teamcityapp.account.create.view.OnToolBarNavigationListener;

/**
 * {@link FilterBuildsActivity} view interactions
 */
public interface FilterBuildsView {

    // Available build filters
    int FILTER_SUCCESS = 0;
    int FILTER_FAILED = 1;
    int FILTER_ERROR = 2;
    int FILTER_CANCELLED = 3;
    int FILTER_FAILED_TO_START = 4;
    int FILTER_RUNNING = 5;
    int FILTER_QUEUED = 6;
    int FILTER_NONE = 7;

    /**
     * Init views
     *
     * @param listener - to handle view interactions
     */
    void initViews(ViewListener listener);

    /**
     * Unbind views
     */
    void unbindViews();

    /**
     * Hide switch which enables pinned build filter
     */
    void hideSwitchForPinnedFilter();

    /**
     * Show switch which enables pinned build filter
     */
    void showSwitchForPinnedFilter();

    /**
     * Listener to receive callbacks to presenter
     */
    interface ViewListener extends OnToolBarNavigationListener {

        /**
         * On filter float action button click
         *
         * @param filter     - how to filter builds
         * @param isPersonal - show personal
         * @param isPinned   - show pinned
         */
        void onFilterFabClick(int filter,
                              boolean isPersonal,
                              boolean isPinned);

        /**
         * On queued filter selected
         */
        void onQueuedFilterSelected();

        /**
         * On other filters selected (not queued)
         */
        void onOtherFiltersSelected();
    }
}
