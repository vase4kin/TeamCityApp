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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import teamcityapp.features.login.presentation.resources.*
import teamcityapp.libraries.authentication.*
import teamcityapp.libraries.theme.TeamCityDimensions
import teamcityapp.libraries.theme.TeamCityLoadingButton
import teamcityapp.libraries.theme.TeamCityLoadingIndicator
import teamcityapp.libraries.theme.TeamCityTheme

enum class LoginDialog { None, Ssl, Demo }

@Composable
fun SharedLoginScreen(state: LoginUiState, onChange: (AuthenticationFormState) -> Unit, onSubmit: () -> Unit, onSslChange: (Boolean) -> Unit, onDemo: () -> Unit, dialog: LoginDialog = LoginDialog.None, onConfirm: () -> Unit = {}, onDecline: () -> Unit = {}, onConfirmHttp: () -> Unit = {}, onDeclineHttp: () -> Unit = {}, onDismissUnauthorized: () -> Unit = {}) {
    Surface(color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            Column(Modifier.widthIn(max = TeamCityDimensions.formMaxWidth).fillMaxSize().safeDrawingPadding().imePadding().verticalScroll(rememberScrollState()).testTag("login:scroll").padding(top = TeamCityDimensions.sectionSpacing), horizontalAlignment = Alignment.CenterHorizontally) {
                Image(painterResource(Res.drawable.ic_launcher), null, Modifier.size(TeamCityDimensions.loginLogoSize))
                Text(stringResource(Res.string.app_name), Modifier.padding(top = TeamCityDimensions.contentPadding), style = MaterialTheme.typography.headlineLarge)
                Text(stringResource(Res.string.text_app_description), Modifier.padding(top = TeamCityDimensions.smallSpacing), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                SharedAuthenticationForm(state.form, onChange, onSslChange, onSubmit, Modifier.fillMaxWidth().padding(top = TeamCityDimensions.sectionSpacing).testTag("login:form"), horizontalPadding = TeamCityDimensions.sectionSpacing)
                TeamCityLoadingButton(onSubmit, stringResource(Res.string.text_login_button), stringResource(state.submitLabel), state.form.busy && !state.demoSigningIn, Modifier.fillMaxWidth().padding(TeamCityDimensions.sectionSpacing).heightIn(min = TeamCityDimensions.controlMinHeight).testTag("login:submit"), enabled = !state.form.busy, shape = MaterialTheme.shapes.large)
                if (state.demoLoading) TeamCityLoadingIndicator(Modifier.padding(bottom = TeamCityDimensions.sectionSpacing).testTag("login:demo-loading"))
                if (state.demo?.available == true) {
                    Card(Modifier.fillMaxWidth().padding(start = TeamCityDimensions.sectionSpacing, end = TeamCityDimensions.sectionSpacing, bottom = TeamCityDimensions.sectionSpacing).testTag("login:demo-card"), shape = MaterialTheme.shapes.large, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer), elevation = CardDefaults.cardElevation(TeamCityDimensions.lowElevation)) {
                        Row(Modifier.padding(TeamCityDimensions.contentPadding), verticalAlignment = Alignment.CenterVertically) {
                            Icon(painterResource(Res.drawable.ic_lightbulb_outline_black_24dp), null, Modifier.size(TeamCityDimensions.largeIconSize))
                            Text(stringResource(Res.string.text_try_it_out), Modifier.padding(start = TeamCityDimensions.contentPadding), style = MaterialTheme.typography.bodyLarge)
                        }
                        TeamCityLoadingButton(onDemo, stringResource(Res.string.text_button_try_it_out), stringResource(state.demoLabel), state.form.busy && state.demoSigningIn, Modifier.align(Alignment.End).padding(end = TeamCityDimensions.contentPadding).testTag("login:demo"), enabled = !state.form.busy, outlined = true, shape = MaterialTheme.shapes.large)
                    }
                }
            }
        }
    }
    when {
        state.httpConfirmation -> SharedAuthenticationWarning(message = requireNotNull(state.httpWarning), onAccept = onConfirmHttp, onDecline = onDeclineHttp)
        state.guestUnauthorized -> AlertDialog(onDismissRequest = onDismissUnauthorized, modifier = Modifier.testTag("login:unauthorized"), shape = MaterialTheme.shapes.large, title = { Text(stringResource(Res.string.info_unauthorized_dialog_title)) }, text = { Text(stringResource(Res.string.info_unauthorized_dialog_content)) }, confirmButton = { TextButton(onDismissUnauthorized) { Text(stringResource(Res.string.dialog_ok)) } })
        dialog == LoginDialog.Ssl -> SharedAuthenticationWarning(onAccept = onConfirm, onDecline = onDecline)
        dialog == LoginDialog.Demo -> AlertDialog(onDismissRequest = {}, modifier = Modifier.testTag("login:demo-dialog"), shape = MaterialTheme.shapes.large, title = { Text(stringResource(Res.string.info_try_it_out_title)) }, text = { Text(styledAuthenticationHtml(stringResource(Res.string.info_try_it_out_dialog_content, state.demo?.url.orEmpty()))) }, confirmButton = { TextButton(onConfirm) { Text(stringResource(Res.string.dialog_try_it_out_title)) } }, dismissButton = { TextButton(onDecline) { Text(stringResource(Res.string.dialog_cancel)) } })
    }
}

@Preview @Composable
private fun LoginPreview() {
    TeamCityTheme { SharedLoginScreen(LoginUiState(demoLoading = false), {}, {}, {}, {}) }
}
