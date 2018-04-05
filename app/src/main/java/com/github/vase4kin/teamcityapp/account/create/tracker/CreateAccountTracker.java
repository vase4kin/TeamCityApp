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

import com.github.vase4kin.teamcityapp.base.tracker.ViewTracker;

/**
 * Tracking
 */
public interface CreateAccountTracker extends ViewTracker {

    /**
     * Error login attribute
     */
    String ATTRIBUTE_NAME_ERROR = "errorMessage";

    /**
     * Ssl enabled attribute
     */
    String ATTRIBUTE_NAME_SSL_ENABLED = "sslEnabled";

    /**
     * Error message if data is failed to save
     */
    String MESSAGE_ERROR_SAVE_DATE = "Failed to save user data!";

    /**
     * Create account screen name
     */
    String SCREEN_NAME = "screen_create_account";

    /**
     * Track user is logged in
     * @param isSslEnabled
     */
    void trackUserLoginSuccess(boolean isSslEnabled);

    /**
     * Track guest user is logged in
     * @param isSslEnabled
     */
    void trackGuestUserLoginSuccess(boolean isSslEnabled);

    /**
     * Track user is failed to login
     *
     * @param errorMessage - Error message
     */
    void trackUserLoginFailed(String errorMessage);

    /**
     * Track guest user is failed to login
     *
     * @param errorMessage - Error message
     */
    void trackGuestUserLoginFailed(String errorMessage);

    /**
     * Track user data is failed to save
     */
    void trackUserDataSaveFailed();
}
