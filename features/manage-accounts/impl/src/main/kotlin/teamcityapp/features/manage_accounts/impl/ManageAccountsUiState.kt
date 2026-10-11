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

import androidx.annotation.StringRes
import teamcityapp.features.manage_accounts.api.AccountDestination
import teamcityapp.features.manage_accounts.api.ManagedAccount
import teamcityapp.features.manage_accounts.api.ManagedAccountId
import teamcityapp.libraries.resources.R as SharedR

sealed interface AccountListUiState {
    data object Loading : AccountListUiState
    data object Empty : AccountListUiState
    data object Error : AccountListUiState
    data class Content(val accounts: List<ManagedAccount>) : AccountListUiState
}
sealed interface AccountRemovalUiState {
    data object Idle : AccountRemovalUiState
    data class Removing(val id: ManagedAccountId) : AccountRemovalUiState
    data class Error(val id: ManagedAccountId) : AccountRemovalUiState
}
data class ManageAccountsUiState(
    val accounts: AccountListUiState = AccountListUiState.Loading,
    val removal: AccountRemovalUiState = AccountRemovalUiState.Idle,
    val destination: AccountDestination? = null
) {
    val canInteract: Boolean get() = removal !is AccountRemovalUiState.Removing && destination == null
}
sealed interface ManageAccountsDialog {
    @get:StringRes val titleRes: Int? get() = null

    @get:StringRes val confirmLabelRes: Int? get() = null

    @get:StringRes val messageRes: Int? get() = null
    data object None : ManageAccountsDialog
    data object SslWarning : ManageAccountsDialog {
        override val titleRes: Int = SharedR.string.warning_ssl_dialog_title
        override val messageRes: Int = SharedR.string.warning_ssl_dialog_content
        override val confirmLabelRes: Int = android.R.string.ok
    }
    data class ConfirmRemoval(val id: ManagedAccountId) : ManageAccountsDialog {
        override val confirmLabelRes: Int = R.string.dialog_remove_active_account_positive_button_text
        override val messageRes: Int = R.string.dialog_remove_not_active_account_positive_content_text
    }
}
