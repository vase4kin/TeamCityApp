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

package com.github.vase4kin.teamcityapp.overview.tracker

import com.github.vase4kin.teamcityapp.base.tracker.ViewTracker

/**
 * Tracker for [com.github.vase4kin.teamcityapp.overview.view.OverviewFragment]
 */
interface OverviewTracker : ViewTracker {

    /**
     * Track that Cancel build option menu is clicked
     */
    fun trackUserClickedCancelBuildOption()

    /**
     * Track that share build button is clicked
     */
    fun trackUserSharedBuild()

    /**
     * Track that restart build button is clicked
     */
    fun trackUserRestartedBuild()

    /**
     * Track that open browser button is clicked
     */
    fun trackUserOpenBrowser()

    /**
     * Track that user wants to see build list filtered by specific branch name
     */
    fun trackUserWantsToSeeBuildListFilteredByBranch()

    /**
     * Track that open build type event is clicked
     */
    fun trackUserOpensBuildType()

    /**
     * Track that open project type event is clicked
     */
    fun trackUserOpensProject()

    companion object {

        /**
         * Cancel build option menu is clicked event
         */
        const val EVENT_CANCEL_BUILD = "cancel_build"

        /**
         * Share build option menu is clicked event
         */
        const val EVENT_SHARE_BUILD = "share_build"

        /**
         * Restart build option menu is clicked event
         */
        const val EVENT_RESTART_BUILD = "restart_build"

        /**
         * Open browser build option menu is clicked event
         */
        const val EVENT_OPEN_BROWSER_BUILD = "open_browser_build"

        /**
         * Show builds filtered by branch action is clicked
         */
        const val EVENT_SHOW_BUILDS_FILTERED_BY_BRANCH = "show_builds_filtered_by_branch"

        /**
         * Open build_type is clicked
         */
        const val EVENT_OPEN_BUILD_TYPE = "open_build_type"
        /**
         * Open project is clicked
         */
        const val EVENT_OPEN_PROJECT = "open_project"
    }
}
