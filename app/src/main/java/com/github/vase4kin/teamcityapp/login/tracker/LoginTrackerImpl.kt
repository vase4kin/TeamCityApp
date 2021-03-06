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

package com.github.vase4kin.teamcityapp.login.tracker

import com.github.vase4kin.teamcityapp.account.create.tracker.CreateAccountTrackerImpl
import com.google.firebase.analytics.FirebaseAnalytics

/**
 * Tracker fabric impl of [LoginTracker]
 */
class LoginTrackerImpl(firebaseAnalytics: FirebaseAnalytics) :
    CreateAccountTrackerImpl(firebaseAnalytics),
    LoginTracker {

    /**
     * {@inheritDoc}
     */
    override fun trackView() {
        firebaseAnalytics.logEvent(LoginTracker.SCREEN_NAME, null)
    }

    /**
     * {@inheritDoc}
     */
    override fun trackUserClicksOnTryItOut() {
        firebaseAnalytics.logEvent(LoginTracker.EVENT_USER_CLICKS_ON_TRY_IT_OUT, null)
    }

    /**
     * {@inheritDoc}
     */
    override fun trackUserTriesTryItOut() {
        firebaseAnalytics.logEvent(LoginTracker.EVENT_USER_TRIES_TRY_IT_OUT, null)
    }

    /**
     * {@inheritDoc}
     */
    override fun trackUserDeclinesTryingTryItOut() {
        firebaseAnalytics.logEvent(LoginTracker.EVENT_USER_DECLINES_TRY_IT_OUT, null)
    }
}
