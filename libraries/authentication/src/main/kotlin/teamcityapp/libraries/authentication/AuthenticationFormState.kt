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

val AuthenticationFormState.errorMessage: UiText? get() = when (val value = this.error) {
    AuthenticationError.EmptyUrl -> UiText.Resource(R.string.server_cannot_be_empty)
    AuthenticationError.EmptyUserName -> UiText.Resource(R.string.server_user_name_cannot_be_empty)
    AuthenticationError.EmptyPassword -> UiText.Resource(R.string.server_password_cannot_be_empty)
    AuthenticationError.SaveFailed -> UiText.Resource(R.string.error_save_account)
    AuthenticationError.DuplicateAccount -> UiText.Dynamic("")
    AuthenticationError.InvalidUrl -> UiText.Resource(R.string.authentication_invalid_url)
    AuthenticationError.Connection -> UiText.Resource(R.string.authentication_connection_failed)
    is AuthenticationError.Http -> UiText.Resource(R.string.authentication_http_error, listOf(value.status))
    is AuthenticationError.Server -> UiText.Dynamic(value.message)
    null -> null
}

@get:StringRes
val AuthenticationWarningText.messageRes: Int get() = when (this) {
    AuthenticationWarningText.Http -> R.string.server_not_secure_http
    AuthenticationWarningText.Ssl -> R.string.warning_ssl_dialog_content
}
