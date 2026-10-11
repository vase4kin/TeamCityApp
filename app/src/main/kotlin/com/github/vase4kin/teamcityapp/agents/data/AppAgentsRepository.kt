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

package com.github.vase4kin.teamcityapp.agents.data

import com.github.vase4kin.teamcityapp.api.Repository
import com.github.vase4kin.teamcityapp.filter_bottom_sheet_dialog.filter.Filter
import com.github.vase4kin.teamcityapp.filter_bottom_sheet_dialog.filter.FilterProvider
import com.github.vase4kin.teamcityapp.home.data.HomeDataManager
import javax.inject.Inject
import javax.inject.Provider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.rx2.await
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import teamcityapp.features.agents.api.Agent
import teamcityapp.features.agents.api.AgentsFilter
import teamcityapp.features.agents.api.AgentsRepository
import teamcityapp.libraries.coroutines.IoDispatcher

/** Adapts the account API and Home's retained quick-filter state for the Agents feature. */
class AppAgentsRepository @Inject constructor(
    private val repository: Provider<Repository>,
    private val filterProvider: FilterProvider,
    private val eventBus: EventBus,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : AgentsRepository {
    override val filter = callbackFlow {
        val observer = FilterObserver(filterProvider) { trySend(it) }
        eventBus.register(observer)
        trySend(observer.currentFilter())
        awaitClose { eventBus.unregister(observer) }
    }.distinctUntilChanged()

    override suspend fun agents(filter: AgentsFilter, forceRefresh: Boolean): List<Agent> = withContext(ioDispatcher) {
        // Resolve the repository for each request so account-session rebuilding stays effective.
        val agents = repository.get().listAgents(filter == AgentsFilter.Disconnected, null, null, forceRefresh).await().objects
        val occurrences = mutableMapOf<String, Int>()
        agents.map { agent ->
            val serverId = agent.id?.takeIf { it.isNotBlank() }
            val identity = serverId ?: "name:${agent.name}"
            val occurrence = occurrences.getOrDefault(identity, 0)
            occurrences[identity] = occurrence + 1
            // Keep real server IDs; older fixtures without IDs still need unique row keys.
            Agent(if (serverId != null && occurrence == 0) serverId else "$identity:$occurrence", agent.name)
        }
    }

    /** Registration is owned by the collecting coroutine, rather than a retained screen. */
    class FilterObserver(private val provider: FilterProvider, private val changed: (AgentsFilter) -> Unit) {
        fun currentFilter() = if (provider.agentsFilter == Filter.AGENTS_DISCONNECTED) AgentsFilter.Disconnected else AgentsFilter.Connected

        @Subscribe
        fun onFilterChanged(@Suppress("UNUSED_PARAMETER") event: HomeDataManager.AgentsFilterChangedEvent) {
            changed(currentFilter())
        }
    }
}
