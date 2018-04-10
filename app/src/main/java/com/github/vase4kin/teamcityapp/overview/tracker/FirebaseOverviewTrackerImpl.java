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

import com.github.vase4kin.teamcityapp.base.tracker.BaseFirebaseTracker;
import com.google.firebase.analytics.FirebaseAnalytics;

/**
 * Impl of {@link OverviewTracker}
 */
public class FirebaseOverviewTrackerImpl extends BaseFirebaseTracker implements OverviewTracker {

    public FirebaseOverviewTrackerImpl(FirebaseAnalytics firebaseAnalytics) {
        super(firebaseAnalytics);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackUserClickedCancelBuildOption() {
        mFirebaseAnalytics.logEvent(EVENT_CANCEL_BUILD, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackUserSharedBuild() {
        mFirebaseAnalytics.logEvent(EVENT_SHARE_BUILD, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackView() {
        // Do nothing
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackUserRestartedBuild() {
        mFirebaseAnalytics.logEvent(EVENT_RESTART_BUILD, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackUserWantsToSeeBuildListFilteredByBranch() {
        mFirebaseAnalytics.logEvent(EVENT_SHOW_BUILDS_FILTERED_BY_BRANCH, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackUserOpensBuildType() {
        mFirebaseAnalytics.logEvent(EVENT_OPEN_BUILD_TYPE, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackUserOpensProject() {
        mFirebaseAnalytics.logEvent(EVENT_OPEN_PROJECT, null);
    }
}
