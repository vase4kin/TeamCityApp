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

package com.github.vase4kin.teamcityapp.runbuild.tracker

import com.github.vase4kin.teamcityapp.base.tracker.BaseFirebaseTracker
import com.google.firebase.analytics.FirebaseAnalytics

/**
 * Impl of [RunBuildTracker]
 */
class RunBuildTrackerImpl(firebaseAnalytics: FirebaseAnalytics) : BaseFirebaseTracker(firebaseAnalytics), RunBuildTracker {

    /**
     * {@inheritDoc}
     */
    override fun trackUserRunBuildSuccess() {
        firebaseAnalytics.logEvent(RunBuildTracker.EVENT_RUN_BUILD_SUCCESS, null)
    }

    /**
     * {@inheritDoc}
     */
    override fun trackUserRunBuildWithCustomParamsSuccess() {
        firebaseAnalytics.logEvent(RunBuildTracker.EVENT_RUN_BUILD_SUCCESS_WITH_CUSTOM_PARAMETERS, null)
    }

    /**
     * {@inheritDoc}
     */
    override fun trackUserRunBuildFailed() {
        firebaseAnalytics.logEvent(RunBuildTracker.EVENT_RUN_BUILD_FAILED, null)
    }

    /**
     * {@inheritDoc}
     */
    override fun trackUserRunBuildFailedForbidden() {
        firebaseAnalytics.logEvent(RunBuildTracker.EVENT_RUN_BUILD_FAILED_FORBIDDEN, null)
    }

    /**
     * {@inheritDoc}
     */
    override fun trackView() {
        firebaseAnalytics.logEvent(RunBuildTracker.SCREEN_NAME_RUN_BUILD, null)
    }

    /**
     * {@inheritDoc}
     */
    override fun trackUserClicksOnAddNewBuildParamButton() {
        firebaseAnalytics.logEvent(RunBuildTracker.EVENT_USER_CLICKS_ADD_NEW_BUILD_PARAMETER_BUTTON, null)
    }

    /**
     * {@inheritDoc}
     */
    override fun trackUserClicksOnClearAllBuildParamsButton() {
        firebaseAnalytics.logEvent(RunBuildTracker.EVENT_USER_CLICKS_CLEAR_ALL_BUILD_PARAMETERS_BUTTON, null)
    }

    /**
     * {@inheritDoc}
     */
    override fun trackUserAddsBuildParam() {
        firebaseAnalytics.logEvent(RunBuildTracker.EVENT_USER_ADDS_NEW_BUILD_PARAMETER, null)
    }
}
