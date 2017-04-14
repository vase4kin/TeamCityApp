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

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.crashlytics.android.answers.LoginEvent;

import io.fabric.sdk.android.Fabric;

/**
 * Impl of {@link CreateAccountTracker}
 */
public class FabricCreateAccountTrackerImpl implements CreateAccountTracker {

    /**
     * Login user method
     */
    private static final String METHOD_USER_LOGIN = "User";
    /**
     * Guest user login method
     */
    private static final String METHOD_GUEST_USER_LOGIN = "GuestUser";
    /**
     * Create account content name
     */
    private static final String CONTENT_NAME = "Create account";

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackUserLoginSuccess() {
        if (!Fabric.isInitialized()) return;
        Answers.getInstance().logLogin(new LoginEvent()
                .putMethod(METHOD_USER_LOGIN)
                .putSuccess(true));
    }

    @Override
    public void trackGuestUserLoginSuccess() {
        if (!Fabric.isInitialized()) return;
        Answers.getInstance().logLogin(new LoginEvent()
                .putMethod(METHOD_GUEST_USER_LOGIN)
                .putSuccess(true));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackUserLoginFailed(String errorMessage) {
        if (!Fabric.isInitialized()) return;
        Answers.getInstance().logLogin(new LoginEvent()
                .putMethod(METHOD_USER_LOGIN)
                .putSuccess(false)
                .putCustomAttribute(ATTRIBUTE_NAME_ERROR, errorMessage));
    }

    @Override
    public void trackGuestUserLoginFailed(String errorMessage) {
        if (!Fabric.isInitialized()) return;
        Answers.getInstance().logLogin(new LoginEvent()
                .putMethod(METHOD_GUEST_USER_LOGIN)
                .putSuccess(false)
                .putCustomAttribute(ATTRIBUTE_NAME_ERROR, errorMessage));
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
        if (!Fabric.isInitialized()) return;
        Answers.getInstance().logContentView(new ContentViewEvent()
                .putContentName(CONTENT_NAME));
    }
}
