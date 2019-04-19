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

package com.github.vase4kin.teamcityapp.account.create.tracker;

import android.os.Bundle;

import com.github.vase4kin.teamcityapp.base.tracker.BaseFirebaseTracker;
import com.google.firebase.analytics.FirebaseAnalytics;

public class FirebaseCreateAccountTrackerImpl extends BaseFirebaseTracker implements CreateAccountTracker {

    /**
     * Login guest user success event
     */
    private static final String EVENT_LOGIN_GUEST_USER_SUCCESS = "login_guest_user_success";
    /**
     * Login guest user failed event
     */
    private static final String EVENT_LOGIN_GUEST_USER_FAILED = "login_guest_user_failed";
    /**
     * Login user success event
     */
    private static final String EVENT_LOGIN_USER_SUCCESS = "login_user_success";
    /**
     * Login user failed event
     */
    private static final String EVENT_LOGIN_USER_FAILED = "login_user_failed";

    public FirebaseCreateAccountTrackerImpl(FirebaseAnalytics firebaseAnalytics) {
        super(firebaseAnalytics);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackUserLoginSuccess(boolean isSslEnabled) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ATTRIBUTE_NAME_SSL_ENABLED, isSslEnabled);
        firebaseAnalytics.logEvent(EVENT_LOGIN_USER_SUCCESS, bundle);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackGuestUserLoginSuccess(boolean isSslEnabled) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ATTRIBUTE_NAME_SSL_ENABLED, isSslEnabled);
        firebaseAnalytics.logEvent(EVENT_LOGIN_GUEST_USER_SUCCESS, bundle);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackUserLoginFailed(String errorMessage) {
        Bundle bundle = new Bundle();
        bundle.putString(ATTRIBUTE_NAME_ERROR, errorMessage);
        firebaseAnalytics.logEvent(EVENT_LOGIN_USER_FAILED, bundle);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackGuestUserLoginFailed(String errorMessage) {
        Bundle bundle = new Bundle();
        bundle.putString(ATTRIBUTE_NAME_ERROR, errorMessage);
        firebaseAnalytics.logEvent(EVENT_LOGIN_GUEST_USER_FAILED, bundle);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackUserDataSaveFailed() {
        trackUserLoginFailed(MESSAGE_ERROR_SAVE_DATE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackView() {
        firebaseAnalytics.logEvent(SCREEN_NAME, null);
    }
}
