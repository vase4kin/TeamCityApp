package com.github.vase4kin.teamcityapp.filter_builds.tracker;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.crashlytics.android.answers.CustomEvent;

import io.fabric.sdk.android.Fabric;

/**
 * Impl of {@link FilterBuildsTracker}
 */
public class FilterBuildsTrackerImpl implements FilterBuildsTracker {

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackUserFilteredBuilds() {
        if (!Fabric.isInitialized()) return;
        Answers.getInstance().logCustom(new CustomEvent(EVENT_RUN_BUILD_BUTTON_PRESSED));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackView() {
        if (!Fabric.isInitialized()) return;
        Answers.getInstance().logContentView(new ContentViewEvent()
                .putContentName(CONTENT_NAME_FILTER_BUILDS));
    }
}
