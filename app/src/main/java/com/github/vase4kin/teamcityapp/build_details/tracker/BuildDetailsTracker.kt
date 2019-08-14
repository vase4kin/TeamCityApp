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

package com.github.vase4kin.teamcityapp.build_details.tracker

import com.github.vase4kin.teamcityapp.base.tracker.ViewTracker
import com.github.vase4kin.teamcityapp.build_details.view.BuildDetailsActivity

/**
 * Tracker for [BuildDetailsActivity]
 */
interface BuildDetailsTracker : ViewTracker {

    /**
     * Track user confirmed build cancellation
     *
     * @param isReAddToTheQueue - Re-add build to the queue flag
     */
    fun trackUserConfirmedCancel(isReAddToTheQueue: Boolean)

    /**
     * Track user gets server error on build cancellation
     */
    fun trackUserGetsForbiddenErrorOnBuildCancel()

    /**
     * Track user gets server error on build cancellation
     */
    fun trackUserGetsServerErrorOnBuildCancel()

    /**
     * Track user cancelled build successfully
     */
    fun trackUserCanceledBuildSuccessfully()

    /**
     * Track user gets server error on build restarting
     */
    fun trackUserGetsForbiddenErrorOnBuildRestart()

    /**
     * Track user gets server error on build restarting
     */
    fun trackUserGetsServerErrorOnBuildRestart()

    /**
     * Track user restarted build successfully
     */
    fun trackUserRestartedBuildSuccessfully()

    /**
     * Track user wants to see queued build details after restarting it
     */
    fun trackUserWantsToSeeQueuedBuildDetails()

    /**
     * Track user failed to see queued build details after restarting it
     */
    fun trackUserFailedToSeeQueuedBuildDetails()

    companion object {

        /**
         * Screen name
         */
        const val SCREEN_NAME = "screen_build_details"

        /**
         * User confirmed build cancellation
         */
        const val EVENT_USER_CONFIRMED_BUILD_CANCELLATION = "build_cancel_confirm"

        /**
         * Event param isReAddToTheQueue
         */
        const val PARAM_IS_RE_ADD_TO_QUEUE = "param_re_add_to_the_queue"

        /**
         * User gets forbidden error on build cancellation
         */
        const val EVENT_USER_GETS_FORBIDDEN_ERROR_ON_BUILD_CANCELLATION = "build_cancel_forbidden_error"

        /**
         * User gets server error on build cancellation
         */
        const val EVENT_USER_GETS_SERVER_ERROR_ON_BUILD_CANCELLATION = "build_cancel_server_error"

        /**
         * User cancelled build successfully
         */
        const val EVENT_USER_CANCELLED_BUILD_SUCCESSFULLY = "build_cancel_success"

        /**
         * User gets forbidden error on build restarting
         */
        const val EVENT_USER_GETS_FORBIDDEN_ERROR_ON_BUILD_RESTARTING = "build_restart_forbidden_error"

        /**
         * User gets server error on build restarting
         */
        const val EVENT_USER_GETS_SERVER_ERROR_ON_BUILD_RESTARTING = "build_restart_server_error"

        /**
         * User restarted build successfully
         */
        const val EVENT_USER_RESTARTED_BUILD_SUCCESSFULLY = "build_restart_success"

        /**
         * Event for show queued build details
         */
        const val EVENT_SHOW_QUEUED_BUILD_DETAILS_AFTER_RESTARTING = "build_restart_show_success"

        /**
         * Event for show queued build details
         */
        const val EVENT_SHOW_QUEUED_BUILD_DETAILS_FAILED_AFTER_RESTARTING = "build_restart_show_failure"
    }
}
