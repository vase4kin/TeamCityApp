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

package teamcityapp.features.create_account.impl.tracker

import kotlin.test.*
import teamcityapp.libraries.analytics.*

class CreateAccountTrackerImplTest {
    private val records = mutableListOf<AnalyticsEvent>()
    private val analytics = AnalyticsTracker { records += it }
    private fun text(name: String, key: String, value: String) = AnalyticsEvent(name, mapOf(key to AnalyticsValue.Text(value)))
    private fun flag(name: String, key: String, value: Boolean) = AnalyticsEvent(name, mapOf(key to AnalyticsValue.Flag(value)))

    @Test fun addAccountUsesTheSameAuthenticationContractWithItsOwnScreen() {
        val tracker = CreateAccountTrackerImpl(analytics)
        tracker.trackView()
        tracker.trackUserLoginSuccess(false)
        tracker.trackGuestUserLoginSuccess(true)
        tracker.trackUserLoginFailed("unauthorized")
        tracker.trackGuestUserLoginFailed("")
        tracker.trackUserDataSaveFailed()
        assertEquals(
            listOf(
                AnalyticsEvent("screen_create_account"),
                flag("login_user_success", "sslEnabled", false),
                flag("login_guest_user_success", "sslEnabled", true),
                text("login_user_failed", "errorMessage", "unauthorized"),
                text("login_guest_user_failed", "errorMessage", ""),
                text("login_user_failed", "errorMessage", "Failed to save user data!")
            ),
            records
        )
    }
}
