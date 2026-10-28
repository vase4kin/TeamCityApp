/*
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

import kotlin.test.*
import teamcityapp.libraries.analytics.*

class LoginTrackerImplTest {
    private val records = mutableListOf<AnalyticsEvent>()
    private val analytics = AnalyticsTracker { records += it }
    private fun text(name: String, key: String, value: String) = AnalyticsEvent(name, mapOf(key to AnalyticsValue.Text(value)))
    private fun flag(name: String, key: String, value: Boolean) = AnalyticsEvent(name, mapOf(key to AnalyticsValue.Flag(value)))

    @Test fun loginPreservesScreenTryItOutAndAuthenticationPayloads() {
        val tracker = LoginTrackerImpl(analytics)
        tracker.trackView()
        tracker.trackUserClicksOnTryItOut()
        tracker.trackUserTriesTryItOut()
        tracker.trackUserDeclinesTryingTryItOut()
        tracker.trackUserLoginSuccess(true)
        tracker.trackUserLoginSuccess(false)
        tracker.trackGuestUserLoginSuccess(true)
        tracker.trackGuestUserLoginSuccess(false)
        tracker.trackUserLoginFailed("HTTP 401")
        tracker.trackGuestUserLoginFailed("HTTP 500")
        tracker.trackUserDataSaveFailed()
        assertEquals(
            listOf(
                AnalyticsEvent("screen_first_login"), AnalyticsEvent("event_user_clicks_on_try_it_out"),
                AnalyticsEvent("event_user_tries_try_it_out"), AnalyticsEvent("event_user_declines_try_it_out"),
                flag("login_user_success", "sslEnabled", true), flag("login_user_success", "sslEnabled", false),
                flag("login_guest_user_success", "sslEnabled", true), flag("login_guest_user_success", "sslEnabled", false),
                text("login_user_failed", "errorMessage", "HTTP 401"), text("login_guest_user_failed", "errorMessage", "HTTP 500"),
                text("login_user_failed", "errorMessage", "Failed to save user data!")
            ),
            records
        )
    }
}
