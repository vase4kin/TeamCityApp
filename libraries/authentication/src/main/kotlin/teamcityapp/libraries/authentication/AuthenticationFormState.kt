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

data class AuthenticationFormState(
    val serverUrl: String = "https://",
    val userName: String = "",
    val password: String = "",
    val guest: Boolean = false,
    val sslDisabled: Boolean = false,
    val busy: Boolean = false,
    val error: AuthenticationError? = null
) {
    fun credentials() = AccountCredentials(serverUrl.trim { it <= ' ' }, userName.trim { it <= ' ' }, password.trim { it <= ' ' }, guest, sslDisabled)
    fun validationError(): AuthenticationError? {
        val input = credentials()
        return when {
            input.serverUrl.isEmpty() -> AuthenticationError.EmptyUrl
            !guest && input.userName.isEmpty() -> AuthenticationError.EmptyUserName
            !guest && input.password.isEmpty() -> AuthenticationError.EmptyPassword
            else -> null
        }
    }
}
sealed interface AuthenticationError {
    data object EmptyUrl : AuthenticationError
    data object EmptyUserName : AuthenticationError
    data object EmptyPassword : AuthenticationError
    data object SaveFailed : AuthenticationError
    data object DuplicateAccount : AuthenticationError
    data class Server(val message: String) : AuthenticationError
}
