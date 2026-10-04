/*
 * Copyright 2020 Andrey Tolpeev
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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import teamcityapp.features.manage_accounts.api.*
import teamcityapp.features.manage_accounts.impl.tracker.ManageAccountsTracker

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ManageAccountsViewModel @Inject constructor(
    private val repository: ManageAccountsRepository,
    private val tracker: ManageAccountsTracker
) : ViewModel() {
    private val snapshot = MutableStateFlow<AccountListUiState>(AccountListUiState.Loading)
    private val removal = MutableStateFlow<AccountRemovalUiState>(AccountRemovalUiState.Idle)
    private val destination = MutableStateFlow<AccountDestination?>(null)
    private val refresh = MutableStateFlow(0)
    private var revision = 0
    private data class Read(val revision: Int, val accounts: AccountListUiState)

    val state: StateFlow<ManageAccountsUiState> = channelFlow {
        // Reread local storage on return; retain the completed snapshot while a new read waits.
        launch {
            refresh.flatMapLatest {
                val version = revision
                flow { emit(repository.accounts()) }
                    .map<List<ManagedAccount>, AccountListUiState> { it.toListState() }
                    .catch {
                        if (it is CancellationException) throw it
                        emit(AccountListUiState.Error)
                    }
                    .map { Read(version, it) }
            }.collect { read ->
                // A read started before removal must not resurrect a deleted account.
                if (read.revision == revision) snapshot.value = read.accounts
            }
        }
        combine(snapshot, removal, destination, ::ManageAccountsUiState).collect { send(it) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis = 0), ManageAccountsUiState())

    fun onScreenViewed() {
        tracker.trackView()
    }
    fun onSslWarningClicked() {
        tracker.trackUserClicksOnSslDisabledWarning()
    }

    fun retry() {
        if (snapshot.value != AccountListUiState.Error) return
        revision++
        snapshot.value = AccountListUiState.Loading
        refresh.value++
    }

    fun remove(id: ManagedAccountId) {
        if (removal.value is AccountRemovalUiState.Removing || destination.value != null) return
        val accounts = (snapshot.value as? AccountListUiState.Content)?.accounts.orEmpty()
        if (accounts.none { it.id == id } && (removal.value as? AccountRemovalUiState.Error)?.id != id) return
        revision++
        removal.value = AccountRemovalUiState.Removing(id)
        viewModelScope.launch {
            val result = try {
                repository.remove(id)
            } catch (error: CancellationException) {
                removal.value = AccountRemovalUiState.Idle
                throw error
            } catch (error: Exception) {
                removal.value = AccountRemovalUiState.Error(id)
                return@launch
            }
            snapshot.value = result.remainingAccounts.toListState()
            removal.value = if (result.outcome == AccountRemovalOutcome.CleanupFailed) AccountRemovalUiState.Error(id) else AccountRemovalUiState.Idle
            destination.value = when (result.outcome) {
                AccountRemovalOutcome.Home -> AccountDestination.Home
                AccountRemovalOutcome.Login -> AccountDestination.Login
                else -> null
            }
            if (result.removed) tracker.trackAccountRemove()
        }
    }

    fun retryRemoval() {
        (removal.value as? AccountRemovalUiState.Error)?.id?.let(::remove)
    }

    /** Claim the destination before launching, so a rapid resume cannot replay it. */
    fun consumeDestination(expected: AccountDestination): Boolean = destination.compareAndSet(expected, null)
    private fun List<ManagedAccount>.toListState(): AccountListUiState = if (isEmpty()) AccountListUiState.Empty else AccountListUiState.Content(toList())
}
