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

package teamcityapp.features.home.impl.data

import kotlinx.coroutines.flow.*
import teamcityapp.features.filter_bottom_sheet.api.*
import teamcityapp.features.home.api.*
import teamcityapp.libraries.accounts.*
import teamcityapp.libraries.network.*

class TeamCityHomeRepository(private val account: AccountScope, private val api: TeamCityApi, private val preferences: AccountPreferenceState) : HomeRepository {
    override val query = preferences.state.map { it.homeQuery() }.distinctUntilChanged()
    override suspend fun pendingPrompts() = account.run { HomePrompt.entries.filterNot { "home.${it.name}" in preferences.state.value.shownPrompts }.toSet() }
    override suspend fun acknowledgePrompt(prompt: HomePrompt) {
        preferences.update { it.copy(shownPrompts = it.shownPrompts + "home.${prompt.name}") }
    }
    override suspend fun count(query: HomeQuery, tab: HomeTab): HomeCount = account.run {
        require(query.accountKey == account.identity.key)
        val value = when (tab) {
            HomeTab.Favorites -> query.favoriteIds.size
            HomeTab.Running -> api.countBuilds(false, if (query.runningFilter == QuickFilter.RunningFavorites) query.favoriteIds else null)
            HomeTab.Queue -> api.countBuilds(true, if (query.queueFilter == QuickFilter.QueuedFavorites) query.favoriteIds else null)
            HomeTab.Agents -> api.document(api.apiPath("agents?includeDisconnected=${query.agentsFilter == QuickFilter.AgentsDisconnected}&fields=count"), true).count()
            HomeTab.Projects -> 0
        }
        HomeCount.Available(value)
    }
    private fun AccountPreferences.homeQuery() = HomeQuery(account.identity.key, favoriteIds.toList(), if (runningFavorites) QuickFilter.RunningFavorites else QuickFilter.RunningAll, if (queueFavorites) QuickFilter.QueuedFavorites else QuickFilter.QueuedAll, if (agentsConnected) QuickFilter.AgentsConnected else QuickFilter.AgentsDisconnected, HomeAccount(account.identity.userName, account.identity.serverUrl))
}
