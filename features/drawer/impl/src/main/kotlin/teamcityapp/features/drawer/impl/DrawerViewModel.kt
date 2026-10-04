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

package teamcityapp.features.drawer.impl
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import teamcityapp.features.drawer.api.*
import teamcityapp.features.drawer.impl.tracker.DrawerTracker

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DrawerViewModel @Inject constructor(
    private val repository: DrawerRepository,
    private val tracker: DrawerTracker
) : ViewModel() {
    private val snapshot = MutableStateFlow<DrawerAccountsUiState>(DrawerAccountsUiState.Loading)
    private val selection = MutableStateFlow<AccountSwitchUiState>(AccountSwitchUiState.Idle)
    private val home = MutableStateFlow(false)
    private val refresh = MutableStateFlow(0)
    private var revision = 0
    private data class Read(val revision: Int, val accounts: DrawerAccountsUiState)
    val state = channelFlow {
        launch {
            refresh.flatMapLatest {
                val version = revision
                flow { emit(repository.accounts()) }
                    .map<List<DrawerAccount>, DrawerAccountsUiState> { it.toUiState() }
                    .catch {
                        if (it is CancellationException) throw it
                        emit(DrawerAccountsUiState.Error)
                    }
                    .map { Read(version, it) }
            }.collect { if (it.revision == revision) snapshot.value = it.accounts }
        }
        combine(snapshot, selection, home, ::DrawerUiState).collect { send(it) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(0), DrawerUiState())
    fun onScreenViewed() {
        tracker.trackView()
    }
    fun onAboutClicked() {
        tracker.trackOpenAbout()
    }
    fun onSettingsClicked() {
        tracker.trackOpenSettings()
    }
    fun onAddAccountClicked() {
        tracker.trackOpenAddNewAccount()
    }
    fun onManageAccountsClicked() {
        tracker.trackOpenManageAccounts()
    }
    fun onPrivacyClicked() {
        tracker.trackOpenPrivacy()
    }
    fun onRateClicked() {
        tracker.trackRateTheApp()
    }

    fun retry() {
        if (snapshot.value != DrawerAccountsUiState.Error) return
        revision++
        snapshot.value = DrawerAccountsUiState.Loading
        refresh.value++
    }
    fun select(id: DrawerAccountId) {
        if (selection.value is AccountSwitchUiState.Switching || home.value) return
        val account = (snapshot.value as? DrawerAccountsUiState.Content)?.accounts?.firstOrNull { it.id == id }
        if (account?.isActive != false && (selection.value as? AccountSwitchUiState.Error)?.id != id) return
        revision++
        selection.value = AccountSwitchUiState.Switching(id)
        viewModelScope.launch {
            val result = try {
                repository.select(id)
            } catch (error: CancellationException) {
                selection.value = AccountSwitchUiState.Idle
                throw error
            } catch (error: Exception) {
                selection.value = AccountSwitchUiState.Error(id)
                return@launch
            }
            snapshot.value = result.accounts.toUiState()
            selection.value = if (result.outcome == SelectionOutcome.Missing) AccountSwitchUiState.Missing else AccountSwitchUiState.Idle
            if (result.outcome == SelectionOutcome.Changed) {
                tracker.trackChangeAccount()
                home.value = true
            }
        }
    }
    fun retrySelection() {
        (selection.value as? AccountSwitchUiState.Error)?.id?.let(::select)
    }
    fun dismissMissing() {
        if (selection.value == AccountSwitchUiState.Missing) selection.value = AccountSwitchUiState.Idle
    }

    /** Claim before the platform launch; a rapid resume must not replay navigation. */
    fun consumeHome(): Boolean = home.compareAndSet(true, false)
    private fun List<DrawerAccount>.toUiState(): DrawerAccountsUiState = if (isEmpty()) DrawerAccountsUiState.Empty else DrawerAccountsUiState.Content(toList())
}
