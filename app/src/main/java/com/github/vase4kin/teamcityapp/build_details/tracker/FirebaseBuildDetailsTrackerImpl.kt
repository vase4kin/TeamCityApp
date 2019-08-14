/*
 * Copyright 2019 Andrey Tolpeev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.vase4kin.teamcityapp.build_details.tracker

import android.os.Bundle

import com.github.vase4kin.teamcityapp.base.tracker.BaseFirebaseTracker
import com.google.firebase.analytics.FirebaseAnalytics

/**
 * Tracker firebase impl
 */
class FirebaseBuildDetailsTrackerImpl(
        firebaseAnalytics: FirebaseAnalytics
) : BaseFirebaseTracker(firebaseAnalytics), BuildDetailsTracker {

    /**
     * {@inheritDoc}
     */
    override fun trackView() {
        firebaseAnalytics.logEvent(BuildDetailsTracker.SCREEN_NAME, null)
    }

    /**
     * {@inheritDoc}
     */
    override fun trackUserConfirmedCancel(isReAddToTheQueue: Boolean) {
        val bundle = Bundle()
        bundle.putBoolean(BuildDetailsTracker.PARAM_IS_RE_ADD_TO_QUEUE, isReAddToTheQueue)
        firebaseAnalytics.logEvent(BuildDetailsTracker.EVENT_USER_CONFIRMED_BUILD_CANCELLATION, bundle)
    }

    /**
     * {@inheritDoc}
     */
    override fun trackUserGetsForbiddenErrorOnBuildCancel() {
        firebaseAnalytics.logEvent(BuildDetailsTracker.EVENT_USER_GETS_FORBIDDEN_ERROR_ON_BUILD_CANCELLATION, null)
    }

    /**
     * {@inheritDoc}
     */
    override fun trackUserGetsServerErrorOnBuildCancel() {
        firebaseAnalytics.logEvent(BuildDetailsTracker.EVENT_USER_GETS_SERVER_ERROR_ON_BUILD_CANCELLATION, null)
    }

    /**
     * {@inheritDoc}
     */
    override fun trackUserCanceledBuildSuccessfully() {
        firebaseAnalytics.logEvent(BuildDetailsTracker.EVENT_USER_CANCELLED_BUILD_SUCCESSFULLY, null)
    }

    /**
     * {@inheritDoc}
     */
    override fun trackUserGetsForbiddenErrorOnBuildRestart() {
        firebaseAnalytics.logEvent(BuildDetailsTracker.EVENT_USER_GETS_FORBIDDEN_ERROR_ON_BUILD_RESTARTING, null)
    }

    /**
     * {@inheritDoc}
     */
    override fun trackUserGetsServerErrorOnBuildRestart() {
        firebaseAnalytics.logEvent(BuildDetailsTracker.EVENT_USER_GETS_SERVER_ERROR_ON_BUILD_RESTARTING, null)
    }

    /**
     * {@inheritDoc}
     */
    override fun trackUserRestartedBuildSuccessfully() {
        firebaseAnalytics.logEvent(BuildDetailsTracker.EVENT_USER_RESTARTED_BUILD_SUCCESSFULLY, null)
    }

    /**
     * {@inheritDoc}
     */
    override fun trackUserWantsToSeeQueuedBuildDetails() {
        firebaseAnalytics.logEvent(BuildDetailsTracker.EVENT_SHOW_QUEUED_BUILD_DETAILS_AFTER_RESTARTING, null)
    }

    /**
     * {@inheritDoc}
     */
    override fun trackUserFailedToSeeQueuedBuildDetails() {
        firebaseAnalytics.logEvent(BuildDetailsTracker.EVENT_SHOW_QUEUED_BUILD_DETAILS_FAILED_AFTER_RESTARTING, null)
    }
}
