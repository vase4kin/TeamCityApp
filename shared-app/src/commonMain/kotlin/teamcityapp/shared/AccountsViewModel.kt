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

package teamcityapp.shared

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import teamcityapp.libraries.accounts.*
import teamcityapp.libraries.design_system.SharedUiText
import teamcityapp.libraries.list_state.*
import teamcityapp.shared.resources.*

data class AccountsUiState(
    val list: ListUiState<AccountIdentity> = ListUiState.Loading,
    val activeKey: String? = null,
    val busy: Boolean = false,
    val mutationFailed: Boolean = false
) {
    val errorMessage: SharedUiText? = if (mutationFailed || when (list) {
            is ListUiState.Content -> list.refreshFailed
            is ListUiState.Empty -> list.refreshFailed
            else -> false
        }
    ) {
        SharedUiText.Resource(Res.string.journey_account_error)
    } else {
        null
    }
}

/** Account operations are owned by the visit's ViewModel; UI owns navigation and confirmation. */
class AccountsViewModel(private val sessions: AccountSessionManager) : ViewModel() {
    private val loader = RefreshableListLoader(flowOf(Unit)) { _, _ -> sessions.accounts() }
    private data class Operation(val busy: Boolean = false, val failed: Boolean = false)
    private sealed interface Action {
        data class Select(val key: String) : Action
        data class Remove(val key: String) : Action
        data object SignOut : Action
    }
    private val operation = MutableStateFlow(Operation())
    private var pending: Action? = null
    private var job: Job? = null
    val state = combine(loader.state, sessions.state, operation) { list, session, operation ->
        AccountsUiState(list, (session as? AccountSessionState.Active)?.scope?.identity?.key, operation.busy, operation.failed)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(0), AccountsUiState())

    fun select(key: String) = perform(Action.Select(key))
    fun remove(key: String) = perform(Action.Remove(key))
    fun signOut() = perform(Action.SignOut)
    fun retry() {
        val action = pending
        if (action != null) perform(action) else loader.retry()
    }
    private fun perform(action: Action) {
        if (job?.isActive == true) return
        pending = action
        operation.value = Operation(busy = true)
        job = viewModelScope.launch {
            try {
                when (action) {
                    is Action.Select -> sessions.activate(action.key)
                    is Action.Remove -> sessions.remove(action.key)
                    Action.SignOut -> sessions.signOut()
                }
                pending = null
                operation.value = Operation()
                loader.refresh()
            } catch (c: CancellationException) {
                throw c
            } catch (_: Exception) {
                operation.value = Operation(failed = true)
            }
        }
    }
}
