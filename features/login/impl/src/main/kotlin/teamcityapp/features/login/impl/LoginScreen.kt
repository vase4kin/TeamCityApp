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

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import teamcityapp.libraries.authentication.*
import teamcityapp.libraries.theme.TeamCityTheme

enum class LoginDialog { None, Ssl, Demo }

@Composable
fun LoginScreen(state: LoginUiState, onChange: (AuthenticationFormState) -> Unit, onSubmit: () -> Unit, onSslChange: (Boolean) -> Unit, onDemo: () -> Unit, dialog: LoginDialog = LoginDialog.None, onConfirm: () -> Unit = {}, onDecline: () -> Unit = {}, onConfirmHttp: () -> Unit = {}, onDeclineHttp: () -> Unit = {}, onDismissUnauthorized: () -> Unit = {}) {
    Surface(color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            Column(Modifier.widthIn(max = 560.dp).fillMaxSize().safeDrawingPadding().imePadding().verticalScroll(rememberScrollState()).testTag("login:scroll").padding(top = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Image(painterResource(R.drawable.ic_launcher), null, Modifier.size(96.dp))
                Text(stringResource(R.string.app_name), Modifier.padding(top = 16.dp), style = MaterialTheme.typography.headlineLarge)
                Text(stringResource(R.string.text_app_description), Modifier.padding(top = 8.dp), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                AuthenticationForm(state.form, onChange, onSslChange, onSubmit, Modifier.fillMaxWidth().padding(top = 24.dp).testTag("login:form"), horizontalPadding = 24.dp)
                Button(onSubmit, Modifier.fillMaxWidth().padding(24.dp).heightIn(min = 56.dp).testTag("login:submit"), enabled = !state.form.busy, shape = MaterialTheme.shapes.large) { Text(stringResource(R.string.text_login_button)) }
                if (state.demoLoading) CircularProgressIndicator(Modifier.padding(bottom = 24.dp).testTag("login:demo-loading"))
                if (state.demo?.available == true) {
                    Card(Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, bottom = 24.dp).testTag("login:demo-card"), shape = MaterialTheme.shapes.large, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer), elevation = CardDefaults.cardElevation(1.dp)) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(painterResource(R.drawable.ic_lightbulb_outline_black_24dp), null, Modifier.size(32.dp))
                            Text(stringResource(R.string.text_try_it_out), Modifier.padding(start = 16.dp), style = MaterialTheme.typography.bodyLarge)
                        }
                        OutlinedButton(onDemo, Modifier.align(Alignment.End).padding(end = 16.dp).testTag("login:demo"), enabled = !state.form.busy, shape = MaterialTheme.shapes.large) { Text(stringResource(R.string.text_button_try_it_out)) }
                    }
                }
            }
        }
    }
    when {
        state.form.busy -> AuthenticationProgress(stringResource(R.string.text_progress_bar_loading))
        state.httpConfirmation -> AuthenticationWarning(http = true, onAccept = onConfirmHttp, onDecline = onDeclineHttp)
        state.guestUnauthorized -> AlertDialog(onDismissRequest = onDismissUnauthorized, modifier = Modifier.testTag("login:unauthorized"), shape = MaterialTheme.shapes.large, title = { Text(stringResource(R.string.info_unauthorized_dialog_title)) }, text = { Text(stringResource(R.string.info_unauthorized_dialog_content)) }, confirmButton = { TextButton(onDismissUnauthorized) { Text(stringResource(android.R.string.ok)) } })
        dialog == LoginDialog.Ssl -> AuthenticationWarning(onAccept = onConfirm, onDecline = onDecline)
        dialog == LoginDialog.Demo -> AlertDialog(onDismissRequest = {}, modifier = Modifier.testTag("login:demo-dialog"), shape = MaterialTheme.shapes.large, title = { Text(stringResource(R.string.info_try_it_out_title)) }, text = { Text(styledAuthenticationText(androidx.core.text.HtmlCompat.fromHtml(stringResource(R.string.info_try_it_out_dialog_content, state.demo?.url.orEmpty()), androidx.core.text.HtmlCompat.FROM_HTML_MODE_COMPACT))) }, confirmButton = { TextButton(onConfirm) { Text(stringResource(R.string.dialog_try_it_out_title)) } }, dismissButton = { TextButton(onDecline) { Text(stringResource(teamcityapp.libraries.authentication.R.string.warning_ssl_dialog_negative)) } })
    }
}

@Preview @Composable
private fun LoginPreview() {
    TeamCityTheme { LoginScreen(LoginUiState(demoLoading = false), {}, {}, {}, {}) }
}
