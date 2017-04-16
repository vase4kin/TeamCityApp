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

package com.github.vase4kin.teamcityapp.build_details.tracker;

import com.github.vase4kin.teamcityapp.build_details.view.BuildDetailsActivity;
import com.github.vase4kin.teamcityapp.navigation.tracker.ViewTracker;

/**
 * Tracker for {@link BuildDetailsActivity}
 */
public interface BuildDetailsTracker extends ViewTracker {

    /**
     * Screen name
     */
    String SCREEN_NAME = "screen_build_details";

    /**
     * User confirmed build cancellation
     */
    String EVENT_USER_CONFIRMED_BUILD_CANCELLATION = "build_cancel_confirm";

    /**
     * Event param isReAddToTheQueue
     */
    String PARAM_IS_RE_ADD_TO_QUEUE = "param_re_add_to_the_queue";

    /**
     * User gets forbidden error on build cancellation
     */
    String EVENT_USER_GETS_FORBIDDEN_ERROR_ON_BUILD_CANCELLATION = "build_cancel_forbidden_error";

    /**
     * User gets server error on build cancellation
     */
    String EVENT_USER_GETS_SERVER_ERROR_ON_BUILD_CANCELLATION = "build_cancel_server_error";

    /**
     * User cancelled build successfully
     */
    String EVENT_USER_CANCELLED_BUILD_SUCCESSFULLY = "build_cancel_success";

    /**
     * User gets forbidden error on build restarting
     */
    String EVENT_USER_GETS_FORBIDDEN_ERROR_ON_BUILD_RESTARTING = "build_restart_forbidden_error";

    /**
     * User gets server error on build restarting
     */
    String EVENT_USER_GETS_SERVER_ERROR_ON_BUILD_RESTARTING = "build_restart_server_error";

    /**
     * User restarted build successfully
     */
    String EVENT_USER_RESTARTED_BUILD_SUCCESSFULLY = "build_restart_success";

    /**
     * Event for show queued build details
     */
    String EVENT_SHOW_QUEUED_BUILD_DETAILS_AFTER_RESTARTING = "build_restart_show_success";

    /**
     * Event for show queued build details
     */
    String EVENT_SHOW_QUEUED_BUILD_DETAILS_FAILED_AFTER_RESTARTING = "build_restart_show_failure";

    /**
     * Track user confirmed build cancellation
     *
     * @param isReAddToTheQueue - Re-add build to the queue flag
     */
    void trackUserConfirmedCancel(boolean isReAddToTheQueue);

    /**
     * Track user gets server error on build cancellation
     */
    void trackUserGetsForbiddenErrorOnBuildCancel();

    /**
     * Track user gets server error on build cancellation
     */
    void trackUserGetsServerErrorOnBuildCancel();

    /**
     * Track user cancelled build successfully
     */
    void trackUserCanceledBuildSuccessfully();

    /**
     * Track user gets server error on build restarting
     */
    void trackUserGetsForbiddenErrorOnBuildRestart();

    /**
     * Track user gets server error on build restarting
     */
    void trackUserGetsServerErrorOnBuildRestart();

    /**
     * Track user restarted build successfully
     */
    void trackUserRestartedBuildSuccessfully();

    /**
     * Track user wants to see queued build details after restarting it
     */
    void trackUserWantsToSeeQueuedBuildDetails();

    /**
     * Track user failed to see queued build details after restarting it
     */
    void trackUserFailedToSeeQueuedBuildDetails();
}
