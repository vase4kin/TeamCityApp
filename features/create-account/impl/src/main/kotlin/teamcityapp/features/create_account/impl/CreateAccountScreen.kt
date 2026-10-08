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
    TeamCityScreen(stringResource(R.string.add_new_account_dialog_title), onClose, appBarHeight = 56.dp, actions = {
        IconButton(onSubmit, enabled = !state.form.busy, modifier = Modifier.testTag("create-account:submit")) {
            Icon(androidx.compose.ui.res.painterResource(R.drawable.ic_done_24px), stringResource(R.string.add_new_account_dialog_create_account_button_text))
        }
    }) { modifier ->
        Column(modifier.imePadding().verticalScroll(rememberScrollState()).testTag("create-account:scroll")) {
            Surface(Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface) {
                AuthenticationForm(state.form, onChange, onSslChange, onSubmit, Modifier.padding(16.dp), spaced = true, duplicateMessage = stringResource(R.string.add_new_account_dialog_account_exist_error_message))
            }
            HorizontalDivider()
        }
    }
    when {
        state.form.busy -> AuthenticationProgress(stringResource(R.string.progress_dialog_content), stringResource(R.string.progress_dialog_title))
        dialog == CreateAccountDialog.Ssl -> AuthenticationWarning(onAccept = onConfirm, onDecline = onDecline)
        dialog == CreateAccountDialog.Discard -> AlertDialog(onDismissRequest = onDecline, modifier = Modifier.testTag("create-account:discard"), shape = RoundedCornerShape(4.dp), text = { Text(stringResource(R.string.discard_dialog_content)) }, confirmButton = { TextButton(onConfirm) { Text(stringResource(R.string.discard_dialog_positive_button_text).uppercase()) } }, dismissButton = { TextButton(onDecline) { Text(stringResource(R.string.discard_dialog_negative_button_text).uppercase()) } })
    }
}

@Preview @Composable
private fun CreatePreview() {
    TeamCityTheme { CreateAccountScreen(CreateAccountUiState(), {}, {}, {}, {}) }
}
