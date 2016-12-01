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


import com.github.vase4kin.teamcityapp.navigation.tracker.ViewTracker;

/**
 * Tracking for build list
 */
public interface BuildListTracker extends ViewTracker {

    /**
     * Build list content name
     */
    String CONTENT_NAME_BUILD_LIST = "Build list";

    /**
     * Running build list content name
     */
    String CONTENT_NAME_RUNNING_BUILD_LIST = "Running build list";

    /**
     * Queued build list content name
     */
    String CONTENT_NAME_QUEUED_BUILD_LIST = "Build queue list";

    /**
     * Event for run build button pressed
     */
    String EVENT_RUN_BUILD_BUTTON_PRESSED = "Run build button is pressed";

    /**
     * Event for show queued build details
     */
    String EVENT_SHOW_QUEUED_BUILD_DETAILS = "Show queued build details";

    /**
     * Track run build button is pressed
     */
    void trackRunBuildButtonPressed();

    /**
     * Track user wants to see queued build details
     */
    void trackUserWantsToSeeQueuedBuildDetails();
}
