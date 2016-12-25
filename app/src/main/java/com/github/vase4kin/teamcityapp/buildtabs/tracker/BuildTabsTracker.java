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

package com.github.vase4kin.teamcityapp.buildtabs.tracker;

import com.github.vase4kin.teamcityapp.navigation.tracker.ViewTracker;

/**
 * Tracker for {@link com.github.vase4kin.teamcityapp.buildtabs.view.BuildTabsActivity}
 */
public interface BuildTabsTracker extends ViewTracker {

    /**
     * Event content name
     */
    String EVENT_CONTENT_NAME = "Build details";

    /**
     * User confirmed build cancellation
     */
    String EVENT_USER_CONFIRMED_BUILD_CANCELLATION = "Confirmed build cancellation";

    /**
     * User gets forbidden error on build cancellation
     */
    String EVENT_USER_GETS_FORBIDDEN_ERROR_ON_BUILD_CANCELLATION = "Forbidden error on build cancellation";

    /**
     * User gets server error on build cancellation
     */
    String EVENT_USER_GETS_SERVER_ERROR_ON_BUILD_CANCELLATION = "Server error on build cancellation";

    /**
     * User cancelled build successfully
     */
    String EVENT_USER_CANCELLED_BUILD_SUCCESSFULLY = "Build cancelled successfully";

    /**
     * Track user confirmed build cancellation
     */
    void trackUserConfirmedCancel();

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
}
