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

import com.google.firebase.analytics.FirebaseAnalytics;

public class FirebaseCreateAccountTrackerImpl implements CreateAccountTracker {

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
    /**
     * Screen name
     */
    private static final String SCREEN_NAME = "screen_create_account";

    private final FirebaseAnalytics mFirebaseAnalytics;

    public FirebaseCreateAccountTrackerImpl(FirebaseAnalytics firebaseAnalytics) {
        this.mFirebaseAnalytics = firebaseAnalytics;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackUserLoginSuccess() {
        mFirebaseAnalytics.logEvent(EVENT_LOGIN_USER_SUCCESS, null);
    }

    @Override
    public void trackGuestUserLoginSuccess() {
        mFirebaseAnalytics.logEvent(EVENT_LOGIN_GUEST_USER_SUCCESS, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackUserLoginFailed(String errorMessage) {
        Bundle bundle = new Bundle();
        bundle.putString(ATTRIBUTE_NAME_ERROR, errorMessage);
        mFirebaseAnalytics.logEvent(EVENT_LOGIN_USER_FAILED, bundle);
    }

    @Override
    public void trackGuestUserLoginFailed(String errorMessage) {
        Bundle bundle = new Bundle();
        bundle.putString(ATTRIBUTE_NAME_ERROR, errorMessage);
        mFirebaseAnalytics.logEvent(EVENT_LOGIN_GUEST_USER_FAILED, bundle);
    }

    @Override
    public void trackUserDataSaveFailed() {
        trackUserLoginFailed(MESSAGE_ERROR_SAVE_DATE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackView() {
        mFirebaseAnalytics.logEvent(SCREEN_NAME, null);
    }
}
