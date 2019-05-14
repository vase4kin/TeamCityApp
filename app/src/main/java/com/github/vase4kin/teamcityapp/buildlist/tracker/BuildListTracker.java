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

package com.github.vase4kin.teamcityapp.buildlist.tracker;


import com.github.vase4kin.teamcityapp.base.tracker.ViewTracker;

/**
 * Tracking for build list
 */
public interface BuildListTracker extends ViewTracker {

    /**
     * Build list screen name
     */
    String SCREEN_NAME_BUILD_LIST = "screen_build_list";

    /**
     * Running build list screen name
     */
    String SCREEN_NAME_RUNNING_BUILD_LIST = "screen_running_build_list";

    /**
     * Queued build list screen name
     */
    String SCREEN_NAME_QUEUED_BUILD_LIST = "screen_build_queue_list";

    /**
     * Snapshot dependencies
     */
    String SCREEN_NAME_SNAPSHOT_DEPENDECIES = "screen_snapshot_dependencies";

    /**
     * Event for run build button pressed
     */
    String EVENT_RUN_BUILD_BUTTON_PRESSED = "run_build_fab_click";

    /**
     * Event for show queued build details
     */
    String EVENT_SHOW_QUEUED_BUILD_DETAILS = "build_list_show_queued_details";

    /**
     * Track run build button is pressed
     */
    void trackRunBuildButtonPressed();

    /**
     * Track user wants to see queued build details
     */
    void trackUserWantsToSeeQueuedBuildDetails();
}
