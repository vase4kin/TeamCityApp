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

package teamcityapp.features.login.impl

import androidx.compose.runtime.Composable
import teamcityapp.libraries.authentication.AuthenticationFormState

/** Android keeps this entry point and its existing navigation/analytics integration. */
@Composable
fun LoginScreen(state: LoginUiState, onChange: (AuthenticationFormState) -> Unit, onSubmit: () -> Unit, onSslChange: (Boolean) -> Unit, onDemo: () -> Unit, dialog: LoginDialog = LoginDialog.None, onConfirm: () -> Unit = {}, onDecline: () -> Unit = {}, onConfirmHttp: () -> Unit = {}, onDeclineHttp: () -> Unit = {}, onDismissUnauthorized: () -> Unit = {}) = SharedLoginScreen(state, onChange, onSubmit, onSslChange, onDemo, dialog, onConfirm, onDecline, onConfirmHttp, onDeclineHttp, onDismissUnauthorized)

val LoginUiState.submitLabelRes: Int get() = if (form.guest) R.string.connecting else R.string.signing_in
val LoginUiState.demoLabelRes: Int get() = R.string.connecting
