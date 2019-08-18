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

package com.github.vase4kin.teamcityapp.account.create.tracker

import android.os.Bundle
import com.github.vase4kin.teamcityapp.base.tracker.BaseFirebaseTracker
import com.google.firebase.analytics.FirebaseAnalytics

open class CreateAccountTrackerImpl(
    firebaseAnalytics: FirebaseAnalytics
) : BaseFirebaseTracker(firebaseAnalytics), CreateAccountTracker {

    /**
     * {@inheritDoc}
     */
    override fun trackUserLoginSuccess(isSslEnabled: Boolean) {
        val bundle = Bundle()
        bundle.putBoolean(CreateAccountTracker.ATTRIBUTE_NAME_SSL_ENABLED, isSslEnabled)
        firebaseAnalytics.logEvent(EVENT_LOGIN_USER_SUCCESS, bundle)
    }

    /**
     * {@inheritDoc}
     */
    override fun trackGuestUserLoginSuccess(isSslEnabled: Boolean) {
        val bundle = Bundle()
        bundle.putBoolean(CreateAccountTracker.ATTRIBUTE_NAME_SSL_ENABLED, isSslEnabled)
        firebaseAnalytics.logEvent(EVENT_LOGIN_GUEST_USER_SUCCESS, bundle)
    }

    /**
     * {@inheritDoc}
     */
    override fun trackUserLoginFailed(errorMessage: String) {
        val bundle = Bundle()
        bundle.putString(CreateAccountTracker.ATTRIBUTE_NAME_ERROR, errorMessage)
        firebaseAnalytics.logEvent(EVENT_LOGIN_USER_FAILED, bundle)
    }

    /**
     * {@inheritDoc}
     */
    override fun trackGuestUserLoginFailed(errorMessage: String) {
        val bundle = Bundle()
        bundle.putString(CreateAccountTracker.ATTRIBUTE_NAME_ERROR, errorMessage)
        firebaseAnalytics.logEvent(EVENT_LOGIN_GUEST_USER_FAILED, bundle)
    }

    /**
     * {@inheritDoc}
     */
    override fun trackUserDataSaveFailed() {
        trackUserLoginFailed(CreateAccountTracker.MESSAGE_ERROR_SAVE_DATE)
    }

    /**
     * {@inheritDoc}
     */
    override fun trackView() {
        firebaseAnalytics.logEvent(CreateAccountTracker.SCREEN_NAME, null)
    }

    companion object {

        /**
         * Login guest user success event
         */
        private const val EVENT_LOGIN_GUEST_USER_SUCCESS = "login_guest_user_success"
        /**
         * Login guest user failed event
         */
        private const val EVENT_LOGIN_GUEST_USER_FAILED = "login_guest_user_failed"
        /**
         * Login user success event
         */
        private const val EVENT_LOGIN_USER_SUCCESS = "login_user_success"
        /**
         * Login user failed event
         */
        private const val EVENT_LOGIN_USER_FAILED = "login_user_failed"
    }
}
