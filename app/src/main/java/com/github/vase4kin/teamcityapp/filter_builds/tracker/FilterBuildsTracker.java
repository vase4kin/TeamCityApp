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

package com.github.vase4kin.teamcityapp.filter_builds.tracker;


import com.github.vase4kin.teamcityapp.base.tracker.ViewTracker;

/**
 * Filter builds tracking class
 */
public interface FilterBuildsTracker extends ViewTracker {

    /**
     * Filter builds screen name
     */
    String SCREEN_NAME_FILTER_BUILDS = "screen_filter_builds";

    /**
     * Event when user filtered builds
     */
    String EVENT_RUN_BUILD_BUTTON_PRESSED = "build_list_filters_applied";

    /**
     * Track user filter builds
     */
    void trackUserFilteredBuilds();
}
