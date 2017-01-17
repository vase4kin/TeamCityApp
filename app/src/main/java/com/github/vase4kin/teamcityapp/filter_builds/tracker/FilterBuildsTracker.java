package com.github.vase4kin.teamcityapp.filter_builds.tracker;


import com.github.vase4kin.teamcityapp.navigation.tracker.ViewTracker;

/**
 * Filter builds tracking class
 */
public interface FilterBuildsTracker extends ViewTracker {

    /**
     * Filter builds content name
     */
    String CONTENT_NAME_FILTER_BUILDS = "Filter builds";

    /**
     * Event when user filtered builds
     */
    String EVENT_RUN_BUILD_BUTTON_PRESSED = "Builds have been filtered";

    /**
     * Track user filter builds
     */
    void trackUserFilteredBuilds();
}
