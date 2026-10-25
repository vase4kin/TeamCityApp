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

package teamcityapp.features.manage_accounts.impl

import androidx.compose.runtime.Composable
import teamcityapp.features.manage_accounts.api.ManagedAccountId

@Composable
fun ManageAccountsScreen(state: ManageAccountsUiState, onRemove: (ManagedAccountId) -> Unit, onSslWarning: () -> Unit, onCreateAccount: () -> Unit, onClose: () -> Unit, onRetry: () -> Unit = {}, onRetryRemoval: () -> Unit = {}, dialog: ManageAccountsDialog = ManageAccountsDialog.None, onDismissDialog: () -> Unit = {}, onConfirmRemoval: (ManagedAccountId) -> Unit = {}) = SharedManageAccountsScreen(state, onRemove, onSslWarning, onCreateAccount, onClose, onRetry, onRetryRemoval, dialog, onDismissDialog, onConfirmRemoval)
