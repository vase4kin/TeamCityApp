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

package com.github.vase4kin.teamcityapp.buildlist.tracker

import com.github.vase4kin.teamcityapp.base.tracker.ViewTracker

/**
 * Tracking for build list
 */
interface BuildListTracker : ViewTracker {

    /**
     * Track run build button is pressed
     */
    fun trackRunBuildButtonPressed()

    /**
     * Track user wants to see queued build details
     */
    fun trackUserWantsToSeeQueuedBuildDetails()

    companion object {

        /**
         * Build list screen name
         */
        const val SCREEN_NAME_BUILD_LIST = "screen_build_list"

        /**
         * Snapshot dependencies
         */
        const val SCREEN_NAME_SNAPSHOT_DEPENDECIES = "screen_snapshot_dependencies"

        /**
         * Event for run build button pressed
         */
        const val EVENT_RUN_BUILD_BUTTON_PRESSED = "run_build_fab_click"

        /**
         * Event for show queued build details
         */
        const val EVENT_SHOW_QUEUED_BUILD_DETAILS = "build_list_show_queued_details"
    }
}
