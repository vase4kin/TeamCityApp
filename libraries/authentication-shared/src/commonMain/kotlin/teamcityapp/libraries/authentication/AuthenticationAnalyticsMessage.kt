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

/** Keep explicit legacy messages; typed transport failures supply only public, stable labels. */
fun AuthenticationResult.Failure.analyticsMessage(): String = message.ifEmpty {
    when (val failure = error) {
        AuthenticationError.InvalidUrl -> "Invalid server URL"

        AuthenticationError.Connection -> "Connection failed"

        is AuthenticationError.Http -> "HTTP ${failure.status}"

        else -> when {
            statusCode == -1 -> "Server is not secure (HTTP)"
            statusCode > 0 -> "HTTP $statusCode"
            else -> ""
        }
    }
}
