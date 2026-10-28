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

import teamcityapp.libraries.analytics.*

class AuthenticationEventTracker(private val analytics: AnalyticsTracker) {
    fun userSuccess(ssl: Boolean) = analytics.track("login_user_success", "sslEnabled" to AnalyticsValue.Flag(ssl))
    fun guestSuccess(ssl: Boolean) = analytics.track("login_guest_user_success", "sslEnabled" to AnalyticsValue.Flag(ssl))
    fun userFailure(message: String) = analytics.track("login_user_failed", "errorMessage" to AnalyticsValue.Text(message))
    fun guestFailure(message: String) = analytics.track("login_guest_user_failed", "errorMessage" to AnalyticsValue.Text(message))
    fun saveFailure() = userFailure("Failed to save user data!")
}
