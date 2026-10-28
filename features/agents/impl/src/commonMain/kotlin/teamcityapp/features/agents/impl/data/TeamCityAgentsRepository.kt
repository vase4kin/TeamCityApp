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

package teamcityapp.features.agents.impl.data

import kotlinx.coroutines.flow.*
import teamcityapp.features.agents.api.*
import teamcityapp.libraries.accounts.*
import teamcityapp.libraries.network.*

class TeamCityAgentsRepository(private val account: AccountScope, private val api: TeamCityApi, private val preferences: AccountPreferenceState) : AgentsRepository {
    override val filter = preferences.state.map { if (it.agentsConnected) AgentsFilter.Connected else AgentsFilter.Disconnected }.distinctUntilChanged()
    override suspend fun agents(filter: AgentsFilter, forceRefresh: Boolean): List<Agent> = account.run {
        val occurrences = mutableMapOf<String, Int>()
        api.allDocuments(api.apiPath("agents?includeDisconnected=${filter == AgentsFilter.Disconnected}&fields=agent(id,name),nextHref"), "agent", forceRefresh).map { value ->
            val name = value.text("name").orEmpty()
            val index = occurrences[name] ?: 0
            occurrences[name] = index + 1
            Agent(value.text("id")?.takeIf(String::isNotBlank) ?: "name:$name:$index", name)
        }
    }
}
