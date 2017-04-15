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

package com.github.vase4kin.teamcityapp.root.tracker;

import com.github.vase4kin.teamcityapp.drawer.tracker.DrawerTracker;

/**
 * Root tracker
 */
public interface RootTracker extends DrawerTracker {

    /**
     * Content name to track
     */
    String CONTENT_NAME_ROOT = "Projects root";

    /**
     * Custom rate app event
     */
    String EVENT_RATE_APP = "Rate app";

    /**
     * Custom status key for rate app event
     */
    String KEY_EVENT_STATUS = "Status";

    /**
     * Custom rated value
     */
    String STATUS_RATED = "Rated";

    /**
     * Custom not rated value
     */
    String STATUS_NOT_RATED = "Not rated";

    /**
     * Custom rate later value
     */
    String STATUS_LATER = "Rated later";

    /**
     * Track user rated the app
     */
    void trackUserRatedTheApp();

    /**
     * Track user didn't rate the app
     */
    void trackUserDidNotRateTheApp();

    /**
     * Track user decided to rate the app later
     */
    void trackUserDecidedToRateTheAppLater();
}
