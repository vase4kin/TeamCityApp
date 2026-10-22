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

import org.jetbrains.compose.resources.StringResource
import teamcityapp.libraries.authentication.shared.resources.*
import teamcityapp.libraries.design_system.SharedUiText

data class AuthenticationFormState(
    val serverUrl: String = "https://",
    val userName: String = "",
    val password: String = "",
    val guest: Boolean = false,
    val sslDisabled: Boolean = false,
    val busy: Boolean = false,
    val error: AuthenticationError? = null
) {
    override fun toString() = "AuthenticationFormState(serverUrl=$serverUrl, userName=$userName, password=<redacted>, guest=$guest, sslDisabled=$sslDisabled, busy=$busy, error=$error)"

    val sharedErrorMessage: SharedUiText? = when (val value = error) {
        AuthenticationError.EmptyUrl -> SharedUiText.Resource(Res.string.server_cannot_be_empty)
        AuthenticationError.EmptyUserName -> SharedUiText.Resource(Res.string.server_user_name_cannot_be_empty)
        AuthenticationError.EmptyPassword -> SharedUiText.Resource(Res.string.server_password_cannot_be_empty)
        AuthenticationError.SaveFailed -> SharedUiText.Resource(Res.string.error_save_account)
        AuthenticationError.DuplicateAccount -> SharedUiText.Resource(Res.string.authentication_duplicate_account)
        AuthenticationError.InvalidUrl -> SharedUiText.Resource(Res.string.authentication_invalid_url)
        AuthenticationError.Connection -> SharedUiText.Resource(Res.string.authentication_connection_failed)
        is AuthenticationError.Http -> SharedUiText.Resource(Res.string.authentication_http_error, listOf(value.status))
        is AuthenticationError.Server -> SharedUiText.Dynamic(value.message)
        null -> null
    }
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
    data object InvalidUrl : AuthenticationError
    data object Connection : AuthenticationError
    data class Http(val status: Int) : AuthenticationError
    data class Server(val message: String) : AuthenticationError
}

enum class AuthenticationWarningText(val message: StringResource, val styled: Boolean) {
    Http(Res.string.server_not_secure_http, false),
    Ssl(Res.string.warning_ssl_dialog_content, true)
}
