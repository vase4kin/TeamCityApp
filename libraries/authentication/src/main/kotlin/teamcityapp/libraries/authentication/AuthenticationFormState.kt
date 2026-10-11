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

import androidx.annotation.StringRes
import teamcityapp.libraries.theme.UiText

data class AuthenticationFormState(
    val serverUrl: String = "https://",
    val userName: String = "",
    val password: String = "",
    val guest: Boolean = false,
    val sslDisabled: Boolean = false,
    val busy: Boolean = false,
    val error: AuthenticationError? = null
) {
    val errorMessage: UiText? = when (val value = error) {
        AuthenticationError.EmptyUrl -> UiText.Resource(R.string.server_cannot_be_empty)
        AuthenticationError.EmptyUserName -> UiText.Resource(R.string.server_user_name_cannot_be_empty)
        AuthenticationError.EmptyPassword -> UiText.Resource(R.string.server_password_cannot_be_empty)
        AuthenticationError.SaveFailed -> UiText.Resource(R.string.error_save_account)
        AuthenticationError.DuplicateAccount -> UiText.Dynamic("")
        is AuthenticationError.Server -> UiText.Dynamic(value.message)
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
    data class Server(val message: String) : AuthenticationError
}

enum class AuthenticationWarningText(@get:StringRes val messageRes: Int, val styled: Boolean) {
    Http(R.string.server_not_secure_http, false),
    Ssl(R.string.warning_ssl_dialog_content, true)
}
