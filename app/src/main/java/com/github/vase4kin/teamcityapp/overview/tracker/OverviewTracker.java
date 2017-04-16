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

package com.github.vase4kin.teamcityapp.overview.tracker;

import com.github.vase4kin.teamcityapp.navigation.tracker.ViewTracker;

/**
 * Tracker for {@link com.github.vase4kin.teamcityapp.overview.view.OverviewFragment}
 */
public interface OverviewTracker extends ViewTracker {

    /**
     * Cancel build option menu is clicked event
     */
    String EVENT_CANCEL_BUILD = "cancel_build";

    /**
     * Share build option menu is clicked event
     */
    String EVENT_SHARE_BUILD = "share_build";

    /**
     * Restart build option menu is clicked event
     */
    String EVENT_RESTART_BUILD = "restart_build";

    /**
     * Show builds filtered by branch action is clicked
     */
    String EVENT_SHOW_BUILDS_FILTERED_BY_BRANCH = "show_builds_filtered_by_branch";

    /**
     * Track that Cancel build option menu is clicked
     */
    void trackUserClickedCancelBuildOption();

    /**
     * Track that share build button is clicked
     */
    void trackUserSharedBuild();

    /**
     * Track that restart build button is clicked
     */
    void trackUserRestartedBuild();

    /**
     * Track that user wants to see build list filtered by specific branch name
     */
    void trackUserWantsToSeeBuildListFilteredByBranch();
}
