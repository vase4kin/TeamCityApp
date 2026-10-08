/*
 * Copyright 2019 Andrey Tolpeev
 * Copyright 2026 Andrey Tolpeev
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

package teamcityapp.features.login.impl.tracker

import com.google.firebase.analytics.FirebaseAnalytics
import javax.inject.Inject
import teamcityapp.features.login.api.LoginTracker
import teamcityapp.libraries.authentication.AuthenticationAnalytics

class LoginTrackerImpl @Inject constructor(private val analytics: FirebaseAnalytics, private val authentication: AuthenticationAnalytics) : LoginTracker {
    override fun trackView() = analytics.logEvent(LoginTracker.SCREEN_NAME, null)
    override fun trackUserLoginSuccess(isSslEnabled: Boolean) = authentication.userSuccess(isSslEnabled)
    override fun trackGuestUserLoginSuccess(isSslEnabled: Boolean) = authentication.guestSuccess(isSslEnabled)
    override fun trackUserLoginFailed(errorMessage: String) = authentication.userFailure(errorMessage)
    override fun trackGuestUserLoginFailed(errorMessage: String) = authentication.guestFailure(errorMessage)
    override fun trackUserDataSaveFailed() = authentication.saveFailure()
    override fun trackUserClicksOnTryItOut() = analytics.logEvent(LoginTracker.EVENT_USER_CLICKS_ON_TRY_IT_OUT, null)
    override fun trackUserTriesTryItOut() = analytics.logEvent(LoginTracker.EVENT_USER_TRIES_TRY_IT_OUT, null)
    override fun trackUserDeclinesTryingTryItOut() = analytics.logEvent(LoginTracker.EVENT_USER_DECLINES_TRY_IT_OUT, null)
}
