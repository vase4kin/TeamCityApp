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

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import teamcityapp.features.manage_accounts.api.ManagedAccountId
import teamcityapp.features.manage_accounts.impl.router.ManageAccountsRouter

internal val accountDialogSaver = Saver<ManageAccountsDialog, List<String>>(
    save = {
        when (it) {
            ManageAccountsDialog.None -> listOf("none")
            ManageAccountsDialog.SslWarning -> listOf("ssl")
            is ManageAccountsDialog.ConfirmRemoval -> listOf("remove", it.id.serverUrl, it.id.userName)
        }
    },
    restore = {
        when (it.first()) {
            "ssl" -> ManageAccountsDialog.SslWarning
            "remove" -> ManageAccountsDialog.ConfirmRemoval(ManagedAccountId(it[1], it[2]))
            else -> ManageAccountsDialog.None
        }
    }
)

@Composable
fun ManageAccountsRoute(router: ManageAccountsRouter, viewModel: ManageAccountsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var dialog by rememberSaveable(stateSaver = accountDialogSaver) { mutableStateOf<ManageAccountsDialog>(ManageAccountsDialog.None) }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LifecycleResumeEffect(viewModel) {
        viewModel.onScreenViewed()
        onPauseOrDispose {}
    }
    LaunchedEffect(state.destination, lifecycle, router) {
        state.destination?.let { destination ->
            lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                if (viewModel.consumeDestination(destination)) router.navigate(destination)
            }
        }
    }
    BackHandler { if (state.canInteract) router.close() }
    ManageAccountsScreen(
        state,
        onRemove = { if (state.canInteract) dialog = ManageAccountsDialog.ConfirmRemoval(it) },
        onSslWarning = {
            if (state.canInteract) {
                viewModel.onSslWarningClicked()
                dialog = ManageAccountsDialog.SslWarning
            }
        },
        onCreateAccount = { if (state.canInteract) router.createAccount() },
        onClose = { if (state.canInteract) router.close() },
        onRetry = viewModel::retry, onRetryRemoval = viewModel::retryRemoval,
        dialog = dialog, onDismissDialog = { dialog = ManageAccountsDialog.None },
        onConfirmRemoval = { id ->
            dialog = ManageAccountsDialog.None
            viewModel.remove(id)
        }
    )
}
