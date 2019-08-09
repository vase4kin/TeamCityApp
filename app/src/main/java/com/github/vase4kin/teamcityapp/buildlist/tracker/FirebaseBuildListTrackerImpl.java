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

import com.github.vase4kin.teamcityapp.base.tracker.BaseFirebaseTracker;
import com.google.firebase.analytics.FirebaseAnalytics;

/**
 * Tracker firebase impl of {@link BuildListTracker}
 */
public class FirebaseBuildListTrackerImpl extends BaseFirebaseTracker implements BuildListTracker {

    private final String screenName;

    public FirebaseBuildListTrackerImpl(FirebaseAnalytics firebaseAnalytics, String screenName) {
        super(firebaseAnalytics);
        this.screenName = screenName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackView() {
        firebaseAnalytics.logEvent(screenName, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackRunBuildButtonPressed() {
        firebaseAnalytics.logEvent(EVENT_RUN_BUILD_BUTTON_PRESSED, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackUserWantsToSeeQueuedBuildDetails() {
        firebaseAnalytics.logEvent(EVENT_SHOW_QUEUED_BUILD_DETAILS, null);
    }
}
