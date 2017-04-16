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

package com.github.vase4kin.teamcityapp.build_details.tracker;

import android.os.Bundle;

import com.github.vase4kin.teamcityapp.base.tracker.BaseFirebaseTracker;
import com.google.firebase.analytics.FirebaseAnalytics;

/**
 * Tracker firebase impl
 */
public class FirebaseBuildDetailsTrackerImpl extends BaseFirebaseTracker implements BuildDetailsTracker {

    public FirebaseBuildDetailsTrackerImpl(FirebaseAnalytics firebaseAnalytics) {
        super(firebaseAnalytics);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackView() {
        mFirebaseAnalytics.logEvent(SCREEN_NAME, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackUserConfirmedCancel(boolean isReAddToTheQueue) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(PARAM_IS_RE_ADD_TO_QUEUE, isReAddToTheQueue);
        mFirebaseAnalytics.logEvent(EVENT_USER_CONFIRMED_BUILD_CANCELLATION, bundle);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackUserGetsForbiddenErrorOnBuildCancel() {
        mFirebaseAnalytics.logEvent(EVENT_USER_GETS_FORBIDDEN_ERROR_ON_BUILD_CANCELLATION, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackUserGetsServerErrorOnBuildCancel() {
        mFirebaseAnalytics.logEvent(EVENT_USER_GETS_SERVER_ERROR_ON_BUILD_CANCELLATION, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackUserCanceledBuildSuccessfully() {
        mFirebaseAnalytics.logEvent(EVENT_USER_CANCELLED_BUILD_SUCCESSFULLY, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackUserGetsForbiddenErrorOnBuildRestart() {
        mFirebaseAnalytics.logEvent(EVENT_USER_GETS_FORBIDDEN_ERROR_ON_BUILD_RESTARTING, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackUserGetsServerErrorOnBuildRestart() {
        mFirebaseAnalytics.logEvent(EVENT_USER_GETS_SERVER_ERROR_ON_BUILD_RESTARTING, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackUserRestartedBuildSuccessfully() {
        mFirebaseAnalytics.logEvent(EVENT_USER_RESTARTED_BUILD_SUCCESSFULLY, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackUserWantsToSeeQueuedBuildDetails() {
        mFirebaseAnalytics.logEvent(EVENT_SHOW_QUEUED_BUILD_DETAILS_AFTER_RESTARTING, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackUserFailedToSeeQueuedBuildDetails() {
        mFirebaseAnalytics.logEvent(EVENT_SHOW_QUEUED_BUILD_DETAILS_FAILED_AFTER_RESTARTING, null);
    }
}
