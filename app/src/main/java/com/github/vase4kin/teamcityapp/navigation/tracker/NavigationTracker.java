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

package com.github.vase4kin.teamcityapp.navigation.tracker;

import com.github.vase4kin.teamcityapp.base.tracker.ViewTracker;

/**
 * Navigation tracker
 */
public interface NavigationTracker extends ViewTracker {

    /**
     * Screen name
     */
    String SCREEN_NAME = "screen_project";

    /**
     * Event 1
     */
    String EVENT_RATE_LATER = "rate_cancel";

    /**
     * Event 2
     */
    String EVENT_RATE_NOW = "rate_now";

    /**
     * Event 3
     */
    String EVENT_RATE_SHOW = "rate_show";

    /**
     * Track that user clicked on rate later
     */
    void trackUserClickedOnRateCancel();

    /**
     * Track that user clicked on rate now
     */
    void trackUserClickedOnRateNow();

    /**
     * Track user saw rate the app
     */
    void trackUserSawRateTheApp();
}
