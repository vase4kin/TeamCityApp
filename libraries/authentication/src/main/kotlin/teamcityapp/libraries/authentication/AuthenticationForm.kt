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

import android.text.Spanned
import android.text.style.URLSpan
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentDataType
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDataType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import teamcityapp.libraries.theme.ErrorNotice
import teamcityapp.libraries.theme.TeamCitySwitch
import teamcityapp.libraries.theme.TeamCityTheme

/** Shared account fields; each screen owns state, validation, dialogs and submission. */
@Composable
fun AuthenticationForm(state: AuthenticationFormState, onChange: (AuthenticationFormState) -> Unit, onSslChange: (Boolean) -> Unit, onSubmit: () -> Unit, modifier: Modifier = Modifier, spaced: Boolean = false, duplicateMessage: String = "", horizontalPadding: Dp = 0.dp) {
    var urlText by remember { mutableStateOf(TextFieldValue(state.serverUrl, TextRange(state.serverUrl.length))) }
    val focus = LocalFocusManager.current
    val submit = {
        focus.clearFocus()
        if (!state.busy) onSubmit()
    }
    val error = when (val value = state.error) {
        AuthenticationError.EmptyUrl -> stringResource(R.string.server_cannot_be_empty)
        AuthenticationError.EmptyUserName -> stringResource(R.string.server_user_name_cannot_be_empty)
        AuthenticationError.EmptyPassword -> stringResource(R.string.server_password_cannot_be_empty)
        AuthenticationError.SaveFailed -> stringResource(R.string.error_save_account)
        AuthenticationError.DuplicateAccount -> duplicateMessage
        is AuthenticationError.Server -> value.message
        null -> null
    }
    val urlError = state.error == AuthenticationError.EmptyUrl
    val userError = state.error == AuthenticationError.EmptyUserName
    val passwordError = state.error == AuthenticationError.EmptyPassword
    val globalError = error != null && !urlError && !userError && !passwordError
    Column(modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (globalError) {
            ErrorNotice(error.orEmpty(), modifier = Modifier.fillMaxWidth().padding(horizontal = horizontalPadding).testTag("auth:error"))
        }
        // Preserve the account fields' existing opt-out from platform autofill.
        OutlinedTextField(
            value = urlText.copy(text = state.serverUrl), onValueChange = {
                urlText = it
                onChange(state.copy(serverUrl = it.text))
            },
            modifier = Modifier.fillMaxWidth().padding(horizontal = horizontalPadding).semantics { contentDataType = ContentDataType.None }.testTag("auth:url"), label = { Text(stringResource(R.string.server_field_hint)) },
            singleLine = true, enabled = !state.busy, shape = MaterialTheme.shapes.medium, isError = urlError,
            supportingText = if (urlError) {
                { Text(error.orEmpty(), Modifier.testTag("auth:error")) }
            } else {
                null
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = if (state.guest) ImeAction.Done else ImeAction.Next),
            keyboardActions = KeyboardActions(onDone = { submit() })
        )
        TeamCitySwitch(stringResource(R.string.text_guest_user_switch), state.guest, {
            focus.clearFocus()
            onChange(state.copy(guest = it))
        }, Modifier.testTag("auth:guest"), !state.busy, PaddingValues(start = horizontalPadding, end = horizontalPadding, top = if (spaced) 16.dp else 8.dp))
        if (!state.guest) {
            OutlinedTextField(
                state.userName, { onChange(state.copy(userName = it)) }, Modifier.fillMaxWidth().padding(horizontal = horizontalPadding).padding(top = if (spaced) 16.dp else 0.dp).semantics { contentDataType = ContentDataType.None }.testTag("auth:username"), label = { Text(stringResource(R.string.hint_user_name)) }, singleLine = true, enabled = !state.busy, shape = MaterialTheme.shapes.medium, isError = userError,
                supportingText = if (userError) {
                    { Text(error.orEmpty(), Modifier.testTag("auth:error")) }
                } else {
                    null
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )
            OutlinedTextField(
                state.password, { onChange(state.copy(password = it)) }, Modifier.fillMaxWidth().padding(horizontal = horizontalPadding).padding(top = if (spaced) 16.dp else 0.dp).semantics { contentDataType = ContentDataType.None }.testTag("auth:password"), label = { Text(stringResource(R.string.hint_password)) }, singleLine = true, enabled = !state.busy, shape = MaterialTheme.shapes.medium, isError = passwordError,
                supportingText = if (passwordError) {
                    { Text(error.orEmpty(), Modifier.testTag("auth:error")) }
                } else {
                    null
                },
                visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done), keyboardActions = KeyboardActions(onDone = { submit() })
            )
        }
        TeamCitySwitch(stringResource(R.string.text_disable_ssl_switch), state.sslDisabled, onSslChange, Modifier.testTag("auth:ssl"), !state.busy, PaddingValues(start = horizontalPadding, end = horizontalPadding, top = if (spaced) 16.dp else 8.dp))
    }
}

@Composable
fun AuthenticationWarning(http: Boolean = false, onAccept: () -> Unit, onDecline: () -> Unit) {
    AlertDialog(
        onDismissRequest = {},
        modifier = Modifier.testTag("auth:warning"),
        shape = MaterialTheme.shapes.large,
        title = { Text(stringResource(R.string.warning_ssl_dialog_title)) },
        text = { if (http) Text(stringResource(R.string.server_not_secure_http)) else Text(sslWarningText()) },
        confirmButton = { TextButton(onClick = onAccept) { Text(stringResource(R.string.dialog_ok_title)) } },
        dismissButton = { TextButton(onClick = onDecline) { Text(stringResource(R.string.warning_ssl_dialog_negative)) } }
    )
}

@Composable
fun AuthenticationProgress(message: String, title: String? = null) {
    AlertDialog(
        onDismissRequest = {},
        modifier = Modifier.testTag("auth:progress"),
        shape = MaterialTheme.shapes.large,
        title = title?.let { { Text(it) } },
        text = {
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                CircularProgressIndicator(Modifier.size(48.dp))
                Text(message, Modifier.padding(start = 24.dp))
            }
        },
        confirmButton = {}
    )
}

@Preview @Composable
private fun FormPreview() {
    TeamCityTheme { AuthenticationForm(AuthenticationFormState(), {}, {}, {}) }
}

@Composable
private fun sslWarningText() = styledAuthenticationText(LocalResources.current.getText(R.string.warning_ssl_dialog_content))

@Composable
fun styledAuthenticationText(text: CharSequence) = run {
    val color = MaterialTheme.colorScheme.primary
    remember(text, color) {
        buildAnnotatedString {
            append(text.toString())
            if (text is Spanned) {
                text.getSpans(0, text.length, URLSpan::class.java).forEach { span ->
                    addStyle(SpanStyle(color = color, textDecoration = TextDecoration.Underline), text.getSpanStart(span), text.getSpanEnd(span))
                }
            }
        }
    }
}
