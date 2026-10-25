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
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import teamcityapp.libraries.accounts.*
import teamcityapp.libraries.authentication.AuthenticationRepository
import teamcityapp.libraries.shared_data.ArtifactFileStore
import teamcityapp.libraries.shared_data.TeamCityAccountGraph
import teamcityapp.libraries.shared_data.TeamCityAuthentication

/** Public destination data only. Credentials remain in the platform account store. */
interface SavedNavigationStore {
    suspend fun read(): String?
    suspend fun write(snapshot: String)
}

internal sealed interface NavigationRestoration {
    data object Unneeded : NavigationRestoration
    data object Failed : NavigationRestoration
    data class Snapshot(val encoded: String?) : NavigationRestoration
}

open class SharedAppViewModel(private val storage: AccountStorage, val navigationStore: SavedNavigationStore, private val responseCache: teamcityapp.libraries.network.ResponseCacheStore? = null, private val clients: teamcityapp.libraries.network.TeamCityClientFactory = teamcityapp.libraries.network.PlatformTeamCityClientFactory) : ViewModel() {
    val sessions = AccountSessionManager(storage, viewModelScope.coroutineContext) { clearResponseCache() }
    val authentication: AuthenticationRepository = TeamCityAuthentication(sessions, clients)
    private val failure = MutableStateFlow(false)
    val restoreFailed = failure.asStateFlow()
    private var firstNavigation = true
    private val graphLock = Mutex()
    private var currentGraph: TeamCityAccountGraph? = null
    private suspend fun clearResponseCache() {
        currentGraph?.invalidateCache()
        currentGraph = null
        try {
            responseCache?.write("{}")
        } catch (c: CancellationException) {
            throw c
        } catch (_: Exception) { /* A disposable cache cannot undo a committed account mutation. */ }
    }

    /** Account resources survive host recreation; the file port must be application-owned. */
    suspend fun graph(account: AccountScope, files: ArtifactFileStore, responseCache: teamcityapp.libraries.network.ResponseCacheStore? = null): TeamCityAccountGraph = graphLock.withLock {
        account.requireCurrent()
        currentGraph?.takeIf { it.account.generation == account.generation }?.let { return@withLock it }
        TeamCityAccountGraph.create(sessions, account, files, responseCache, clients).also {
            account.requireCurrent()
            currentGraph = it
        }
    }
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

    /** Reconciles platform entry points with durable storage without replacing an unchanged owner. */
    suspend fun synchronizeSession(force: Boolean = false) {
        try {
            val persisted = storage.active()?.identity?.key
            val active = (sessions.state.value as? AccountSessionState.Active)?.scope?.identity?.key
            if (force || persisted != active) sessions.restore(forceRefresh = force)
            failure.value = false
        } catch (c: CancellationException) {
            throw c
        } catch (_: Exception) {
            failure.value = true
        }
    }
    internal suspend fun readInitialNavigation(): NavigationRestoration {
        if (!firstNavigation) return NavigationRestoration.Unneeded
        firstNavigation = false
        return try {
            NavigationRestoration.Snapshot(navigationStore.read())
        } catch (c: CancellationException) {
            firstNavigation = true
            throw c
        } catch (_: Exception) {
            firstNavigation = true
            NavigationRestoration.Failed
        }
    }
    internal suspend fun saveNavigation(encoded: String): Boolean = try {
        navigationStore.write(encoded)
        true
    } catch (c: CancellationException) {
        throw c
    } catch (_: Exception) {
        false
    }

    override fun onCleared() {
        sessions.close()
        currentGraph = null
    }
}
