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

package com.github.vase4kin.teamcityapp.filter_builds.view

import com.github.vase4kin.teamcityapp.account.create.view.OnToolBarNavigationListener

/**
 * [FilterBuildsActivity] view interactions
 */
interface FilterBuildsView {

    /**
     * Init views
     *
     * @param listener - to handle view interactions
     */
    fun initViews(listener: ViewListener)

    /**
     * Unbind views
     */
    fun unbindViews()

    /**
     * Hide switch which enables pinned build filter
     */
    fun hideSwitchForPinnedFilter()

    /**
     * Show switch which enables pinned build filter
     */
    fun showSwitchForPinnedFilter()

    /**
     * Listener to receive callbacks to presenter
     */
    interface ViewListener : OnToolBarNavigationListener {

        /**
         * On filter float action button click
         *
         * @param filter - how to filter builds
         * @param isPersonal - show personal
         * @param isPinned - show pinned
         */
        fun onFilterFabClick(
            filter: Int,
            isPersonal: Boolean,
            isPinned: Boolean
        )

        /**
         * On queued filter selected
         */
        fun onQueuedFilterSelected()

        /**
         * On other filters selected (not queued)
         */
        fun onOtherFiltersSelected()
    }

    companion object {

        // Available build filters
        const val FILTER_SUCCESS = 0
        const val FILTER_FAILED = 1
        const val FILTER_ERROR = 2
        const val FILTER_CANCELLED = 3
        const val FILTER_FAILED_TO_START = 4
        const val FILTER_RUNNING = 5
        const val FILTER_QUEUED = 6
        const val FILTER_NONE = 7
    }
}
