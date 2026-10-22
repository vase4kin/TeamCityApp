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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import teamcityapp.libraries.accounts.*
import teamcityapp.libraries.authentication.AuthenticationRepository
import teamcityapp.libraries.shared_data.TeamCityAuthentication

/** Public destination data only. Credentials remain in the platform account store. */
interface SavedNavigationStore {
    suspend fun read(): String?
    suspend fun write(snapshot: String)
}

class SharedAppViewModel(storage: AccountStorage, val navigationStore: SavedNavigationStore) : ViewModel() {
    val sessions = AccountSessionManager(storage, viewModelScope.coroutineContext)
    val authentication: AuthenticationRepository = TeamCityAuthentication(sessions)
    private val failure = MutableStateFlow(false)
    val restoreFailed = failure.asStateFlow()
    private var firstNavigation = true
    init {
        restore()
    }
    fun restore() {
        viewModelScope.launch {
            failure.value = false
            try {
                sessions.restore()
            } catch (c: CancellationException) {
                throw c
            } catch (_: Exception) {
                failure.value = true
            }
        }
    }
    fun claimRestoration(): Boolean = firstNavigation.also { firstNavigation = false }
    override fun onCleared() {
        sessions.close()
    }
}
