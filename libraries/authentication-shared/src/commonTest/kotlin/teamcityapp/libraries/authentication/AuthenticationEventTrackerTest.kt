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

package teamcityapp.libraries.authentication

import kotlin.test.*
import teamcityapp.libraries.analytics.*

class AuthenticationEventTrackerTest {
    @Test fun sharedAuthenticationRetainsSuccessFlagsFailuresAndSaveFailure() {
        val events = mutableListOf<AnalyticsEvent>()
        val tracker = AuthenticationEventTracker(AnalyticsTracker { events += it })
        tracker.userSuccess(false)
        tracker.guestSuccess(true)
        tracker.userFailure("user failure")
        tracker.guestFailure("guest failure")
        tracker.saveFailure()
        assertEquals(
            listOf(
                AnalyticsEvent("login_user_success", mapOf("sslEnabled" to AnalyticsValue.Flag(false))),
                AnalyticsEvent("login_guest_user_success", mapOf("sslEnabled" to AnalyticsValue.Flag(true))),
                AnalyticsEvent("login_user_failed", mapOf("errorMessage" to AnalyticsValue.Text("user failure"))),
                AnalyticsEvent("login_guest_user_failed", mapOf("errorMessage" to AnalyticsValue.Text("guest failure"))),
                AnalyticsEvent("login_user_failed", mapOf("errorMessage" to AnalyticsValue.Text("Failed to save user data!")))
            ),
            events
        )
    }
}
