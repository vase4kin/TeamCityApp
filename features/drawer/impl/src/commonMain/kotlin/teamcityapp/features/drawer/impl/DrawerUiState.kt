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

package teamcityapp.features.drawer.impl

import org.jetbrains.compose.resources.StringResource
import teamcityapp.features.drawer.api.DrawerAccount
import teamcityapp.features.drawer.api.DrawerAccountId
import teamcityapp.features.drawer.impl.resources.*
sealed interface DrawerAccountsUiState {
    data object Loading : DrawerAccountsUiState
    data object Empty : DrawerAccountsUiState
    data object Error : DrawerAccountsUiState
    data class Content(val accounts: List<DrawerAccount>) : DrawerAccountsUiState
}
sealed interface AccountSwitchUiState {
    data object Idle : AccountSwitchUiState
    data class Switching(val id: DrawerAccountId) : AccountSwitchUiState
    data class Error(val id: DrawerAccountId) : AccountSwitchUiState
    data object SigningOut : AccountSwitchUiState
    data object SignOutError : AccountSwitchUiState
    data object Missing : AccountSwitchUiState
}
data class DrawerUiState(
    val accounts: DrawerAccountsUiState = DrawerAccountsUiState.Loading,
    val selection: AccountSwitchUiState = AccountSwitchUiState.Idle,
    val openHome: Boolean = false
) {
    val accountsMessage: StringResource? = when (accounts) {
        DrawerAccountsUiState.Loading -> Res.string.drawer_loading
        DrawerAccountsUiState.Empty -> Res.string.drawer_empty_accounts
        DrawerAccountsUiState.Error -> Res.string.drawer_load_error
        is DrawerAccountsUiState.Content -> null
    }

    val selectionMessage: StringResource? = when (selection) {
        is AccountSwitchUiState.Switching -> Res.string.drawer_switching
        is AccountSwitchUiState.Error -> Res.string.drawer_switch_error
        AccountSwitchUiState.Missing -> Res.string.drawer_missing_account
        AccountSwitchUiState.SigningOut -> Res.string.drawer_signing_out
        AccountSwitchUiState.SignOutError -> Res.string.drawer_sign_out_error
        AccountSwitchUiState.Idle -> null
    }
    val canInteract: Boolean get() = selection !is AccountSwitchUiState.Switching && selection != AccountSwitchUiState.SigningOut && !openHome
}
