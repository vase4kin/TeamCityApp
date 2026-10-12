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

package com.github.vase4kin.teamcityapp.integration.features.home.data

import com.github.vase4kin.teamcityapp.api.Repository
import com.github.vase4kin.teamcityapp.integration.filters.filter.FilterProvider
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage
import javax.inject.Inject
import javax.inject.Provider
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.rx2.await
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import teamcityapp.features.filter_bottom_sheet.api.QuickFilter
import teamcityapp.features.home.api.*
import teamcityapp.libraries.coroutines.IoDispatcher
import teamcityapp.libraries.onboarding.OnboardingManager
import teamcityapp.libraries.session.homeBuildAccountKey
import teamcityapp.libraries.session.requireCurrentHomeBuildAccount

class AppHomeRepository @Inject constructor(
    private val repository: Provider<Repository>,
    private val storage: SharedUserStorage,
    private val filters: FilterProvider,
    private val events: EventBus,
    private val onboarding: OnboardingManager,
    @IoDispatcher private val io: CoroutineDispatcher
) : HomeRepository {
    override val query = callbackFlow {
        val observer = QueryObserver(storage, filters) { trySend(it) }
        events.register(observer)
        trySend(observer.current())
        awaitClose { events.unregister(observer) }
    }.distinctUntilChanged()

    override suspend fun counts(query: HomeQuery): HomeCounts = withContext(io) {
        requireCurrentHomeBuildAccount(storage, query.accountKey)
        val session = repository.get()
        requireCurrentHomeBuildAccount(storage, query.accountKey)
        coroutineScope {
            suspend fun count(load: suspend () -> Int): HomeCount = try {
                val value = load()
                requireCurrentHomeBuildAccount(storage, query.accountKey)
                HomeCount.Available(value)
            } catch (error: Exception) {
                if (error is CancellationException) throw error
                HomeCount.Unavailable
            }
            suspend fun favoriteCount(load: suspend (String) -> Int) = coroutineScope { query.favoriteIds.map { async { load(it) } }.awaitAll().sum() }
            val running = async {
                count {
                    if (query.runningFilter == QuickFilter.RunningFavorites) {
                        favoriteCount { session.listRunningBuilds("$RUNNING_LOCATOR,buildType:$it", "count", false).await().count }
                    } else {
                        session.listRunningBuilds(RUNNING_LOCATOR, "count", false).await().count
                    }
                }
            }
            val queued = async {
                count {
                    if (query.queueFilter == QuickFilter.QueuedFavorites) {
                        favoriteCount { session.listQueueBuilds("buildType:$it", "count", false).await().count }
                    } else {
                        session.listQueueBuilds(null, "count", false).await().count
                    }
                }
            }
            val agents = async { count { session.listAgents(query.agentsFilter == QuickFilter.AgentsDisconnected, "count", null, false).await().count } }
            val result = HomeCounts(HomeCount.Available(query.favoriteIds.size), running.await(), queued.await(), agents.await())
            requireCurrentHomeBuildAccount(storage, query.accountKey)
            result
        }
    }
    override suspend fun pendingPrompts() = withContext(io) {
        buildSet {
            if (!onboarding.isNavigationDrawerPromptShown) add(HomePrompt.Drawer)
            if (!onboarding.isAddFavPromptShown) add(HomePrompt.Favorites)
            if (!onboarding.isRunningBuildsFilterPromptShown) add(HomePrompt.Running)
            if (!onboarding.isBuildsQueueFilterPromptShown) add(HomePrompt.Queue)
            if (!onboarding.isAgentsFilterPromptShown) add(HomePrompt.Agents)
        }
    }
    override suspend fun acknowledgePrompt(prompt: HomePrompt) = withContext(io) {
        when (prompt) {
            HomePrompt.Drawer -> onboarding.saveNavigationDrawerPromptShown()
            HomePrompt.Favorites -> onboarding.saveAddFavPromptShown()
            HomePrompt.Running -> onboarding.saveRunningBuildsFilterPromptShown()
            HomePrompt.Queue -> onboarding.saveBuildsQueueFilterPromptShown()
            HomePrompt.Agents -> onboarding.saveAgentsFilterPromptShown()
        }
    }
    class QueryObserver(private val storage: SharedUserStorage, private val filters: FilterProvider, private val changed: (HomeQuery) -> Unit) {
        fun current(): HomeQuery {
            val account = storage.activeUser
            return HomeQuery(account.homeBuildAccountKey(), account.buildTypeIds.toList(), QuickFilter.entries[filters.runningBuildsFilter.ordinal], QuickFilter.entries[filters.queuedBuildsFilter.ordinal], QuickFilter.entries[filters.agentsFilter.ordinal], HomeAccount(account.userName, account.teamcityUrl))
        }

        @Subscribe fun filterApplied(@Suppress("UNUSED_PARAMETER") event: FilterAppliedEvent) = changed(current())
    }
    private companion object {
        const val RUNNING_LOCATOR = "running:true,branch:default:any,personal:false,pinned:false"
    }
}
