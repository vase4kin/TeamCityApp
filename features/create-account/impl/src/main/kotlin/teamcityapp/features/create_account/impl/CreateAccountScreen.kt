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

package teamcityapp.features.create_account.impl

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import teamcityapp.libraries.authentication.*
import teamcityapp.libraries.theme.*

enum class CreateAccountDialog { None, Ssl, Discard }

@Composable
fun CreateAccountScreen(state: CreateAccountUiState, onChange: (AuthenticationFormState) -> Unit, onSubmit: () -> Unit, onSslChange: (Boolean) -> Unit, onClose: () -> Unit, dialog: CreateAccountDialog = CreateAccountDialog.None, onConfirm: () -> Unit = {}, onDecline: () -> Unit = {}) {
    val scrollState = rememberScrollState()
    TeamCityScreen(stringResource(R.string.add_new_account_dialog_title), onClose, bottomBar = {
        TeamCityBottomActionSurface(scrollState.canScrollForward, Modifier.testTag("create-account:bottom-action")) {
            Button(onSubmit, Modifier.fillMaxWidth().padding(16.dp).heightIn(min = 56.dp).testTag("create-account:submit"), enabled = !state.form.busy) {
                Text(stringResource(R.string.add_new_account_dialog_create_account_button_text))
            }
        }
    }) { modifier ->
        Column(modifier) {
            Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(scrollState).testTag("create-account:scroll").padding(16.dp), horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                Card(Modifier.widthIn(max = 560.dp).fillMaxWidth().testTag("create-account:form"), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
                    AuthenticationForm(state.form, onChange, onSslChange, onSubmit, Modifier.padding(vertical = 24.dp), duplicateMessage = stringResource(R.string.add_new_account_dialog_account_exist_error_message), horizontalPadding = 24.dp)
                }
            }
        }
    }
    when {
        state.form.busy -> AuthenticationProgress(stringResource(R.string.progress_dialog_content), stringResource(R.string.progress_dialog_title))
        dialog == CreateAccountDialog.Ssl -> AuthenticationWarning(onAccept = onConfirm, onDecline = onDecline)
        dialog == CreateAccountDialog.Discard -> AlertDialog(onDismissRequest = onDecline, modifier = Modifier.testTag("create-account:discard"), shape = MaterialTheme.shapes.large, text = { Text(stringResource(R.string.discard_dialog_content)) }, confirmButton = { TextButton(onConfirm) { Text(stringResource(R.string.discard_dialog_positive_button_text)) } }, dismissButton = { TextButton(onDecline) { Text(stringResource(R.string.discard_dialog_negative_button_text)) } })
    }
}

@Preview @Composable
private fun CreatePreview() {
    TeamCityTheme { CreateAccountScreen(CreateAccountUiState(), {}, {}, {}, {}) }
}
