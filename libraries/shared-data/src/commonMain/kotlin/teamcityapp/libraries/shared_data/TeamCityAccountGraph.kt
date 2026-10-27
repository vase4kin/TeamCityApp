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

package teamcityapp.libraries.shared_data

import kotlinx.coroutines.Job
import teamcityapp.libraries.accounts.*
import teamcityapp.libraries.network.*

/** One account activation owns the cache and engine, and closes them on replacement. */
class TeamCityAccountGraph(val account: AccountScope, val api: TeamCityApi, val preferences: AccountPreferenceState) {
    init {
        account.coroutineScope.coroutineContext[Job]!!.invokeOnCompletion { api.close() }
    }
    suspend fun invalidateCache() = api.invalidateCache()
    companion object {
        suspend fun create(sessions: AccountSessionManager, account: AccountScope, responseCache: ResponseCacheStore? = null, clients: TeamCityClientFactory = PlatformTeamCityClientFactory): TeamCityAccountGraph {
            val secret = sessions.secret(account)
            val api = TeamCityApi(TeamCityTransport(clients.create(secret.identity), secret, httpConsent = true, cache = TeamCityResponseCache(account.identity.key, responseCache)))
            return TeamCityAccountGraph(account, api, AccountPreferenceState(account, sessions.preferences(account)) { sessions.updatePreferences(account, it) })
        }
    }
}
