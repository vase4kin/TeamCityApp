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

package teamcityapp.libraries.authentication

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import javax.inject.Inject

/** Preserves the account authentication event names shared by both account screens. */
class AuthenticationAnalytics @Inject constructor(private val analytics: FirebaseAnalytics) {
    fun userSuccess(sslEnabled: Boolean) = success("login_user_success", sslEnabled)
    fun guestSuccess(sslEnabled: Boolean) = success("login_guest_user_success", sslEnabled)
    fun userFailure(message: String) = failure("login_user_failed", message)
    fun guestFailure(message: String) = failure("login_guest_user_failed", message)
    fun saveFailure() = userFailure("Failed to save user data!")

    private fun success(event: String, sslEnabled: Boolean) {
        analytics.logEvent(event, Bundle().apply { putBoolean("sslEnabled", sslEnabled) })
    }
    private fun failure(event: String, message: String) {
        analytics.logEvent(event, Bundle().apply { putString("errorMessage", message) })
    }
}
