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

package com.github.vase4kin.teamcityapp.runbuild.tracker;

import com.github.vase4kin.teamcityapp.base.tracker.BaseFirebaseTracker;
import com.google.firebase.analytics.FirebaseAnalytics;

/**
 * Impl of {@link RunBuildTracker}
 */
public class FirebaseRunBuildTrackerImpl extends BaseFirebaseTracker implements RunBuildTracker {

    public FirebaseRunBuildTrackerImpl(FirebaseAnalytics firebaseAnalytics) {
        super(firebaseAnalytics);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackUserRunBuildSuccess() {
        firebaseAnalytics.logEvent(EVENT_RUN_BUILD_SUCCESS, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackUserRunBuildWithCustomParamsSuccess() {
        firebaseAnalytics.logEvent(EVENT_RUN_BUILD_SUCCESS_WITH_CUSTOM_PARAMETERS, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackUserRunBuildFailed() {
        firebaseAnalytics.logEvent(EVENT_RUN_BUILD_FAILED, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackUserRunBuildFailedForbidden() {
        firebaseAnalytics.logEvent(EVENT_RUN_BUILD_FAILED_FORBIDDEN, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackView() {
        firebaseAnalytics.logEvent(SCREEN_NAME_RUN_BUILD, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackUserClicksOnAddNewBuildParamButton() {
        firebaseAnalytics.logEvent(EVENT_USER_CLICKS_ADD_NEW_BUILD_PARAMETER_BUTTON, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackUserClicksOnClearAllBuildParamsButton() {
        firebaseAnalytics.logEvent(EVENT_USER_CLICKS_CLEAR_ALL_BUILD_PARAMETERS_BUTTON, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackUserAddsBuildParam() {
        firebaseAnalytics.logEvent(EVENT_USER_ADDS_NEW_BUILD_PARAMETER, null);
    }
}
