/*
 * Copyright 2019 Andrey Tolpeev
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

package com.github.vase4kin.teamcityapp.runningbuilds.data

import com.github.vase4kin.teamcityapp.api.Repository
import com.github.vase4kin.teamcityapp.builds.data.AppBuildHydrator
import com.github.vase4kin.teamcityapp.builds.data.homeBuildAccountKey
import com.github.vase4kin.teamcityapp.builds.data.requireCurrentHomeBuildAccount
import com.github.vase4kin.teamcityapp.filter_bottom_sheet_dialog.filter.Filter
import com.github.vase4kin.teamcityapp.filter_bottom_sheet_dialog.filter.FilterProvider
import com.github.vase4kin.teamcityapp.home.data.HomeDataManager
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage
import javax.inject.Inject
import javax.inject.Provider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.rx2.await
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import teamcityapp.features.running_builds.api.RunningBuildsFilter
import teamcityapp.features.running_builds.api.RunningBuildsQuery
import teamcityapp.features.running_builds.api.RunningBuildsRepository
import teamcityapp.libraries.builds.BuildLaunchData
import teamcityapp.libraries.coroutines.IoDispatcher

/** Keeps Home's retained quick filters and account API behind the feature contract. */
class AppRunningBuildsRepository @Inject constructor(
    private val repository: Provider<Repository>,
    private val storage: SharedUserStorage,
    private val filterProvider: FilterProvider,
    private val eventBus: EventBus,
    private val hydrator: AppBuildHydrator,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : RunningBuildsRepository {
    override val query = callbackFlow {
        val observer = QueryObserver(storage, filterProvider) { trySend(it) }
        eventBus.register(observer)
        trySend(observer.currentQuery())
        awaitClose { eventBus.unregister(observer) }
    }.distinctUntilChanged()

    override suspend fun builds(query: RunningBuildsQuery, forceRefresh: Boolean): List<BuildLaunchData> = withContext(ioDispatcher) {
        // Copy caller collections before asynchronous requests, then capture one account API for the batch.
        val savedIds = query.favoriteConfigurationIds.toList()
        requireCurrentHomeBuildAccount(storage, query.accountKey)
        if (query.filter == RunningBuildsFilter.Favorites && savedIds.isEmpty()) return@withContext emptyList()
        val session = repository.get()
        requireCurrentHomeBuildAccount(storage, query.accountKey)
        val summaries = if (query.filter == RunningBuildsFilter.All) {
            session.listRunningBuilds(RUNNING_LOCATOR, null, forceRefresh).await().let { page -> if (page.count == 0) emptyList() else page.objects.orEmpty().toList() }
        } else {
            coroutineScope {
                savedIds.map { id ->
                    async { session.listRunningBuilds("$RUNNING_LOCATOR,buildType:$id", null, forceRefresh).await().let { page -> if (page.count == 0) emptyList() else page.objects.orEmpty().toList() } }
                }.awaitAll().flatten()
            }
        }
        requireCurrentHomeBuildAccount(storage, query.accountKey)
        val builds = hydrator.hydrate(session, summaries)
        requireCurrentHomeBuildAccount(storage, query.accountKey)
        builds
    }

    class QueryObserver(
        private val storage: SharedUserStorage,
        private val provider: FilterProvider,
        private val changed: (RunningBuildsQuery) -> Unit
    ) {
        fun currentQuery(): RunningBuildsQuery {
            val filter = if (provider.runningBuildsFilter == Filter.RUNNING_ALL) RunningBuildsFilter.All else RunningBuildsFilter.Favorites
            // One active-user read avoids mixing another account's favorites with this account key.
            val account = storage.activeUser
            val favoriteIds = if (filter == RunningBuildsFilter.Favorites) account.buildTypeIds.toList() else emptyList()
            return RunningBuildsQuery(account.homeBuildAccountKey(), filter, favoriteIds)
        }

        @Subscribe
        fun onFilterChanged(@Suppress("UNUSED_PARAMETER") event: HomeDataManager.RunningBuildsFilterChangedEvent) = changed(currentQuery())
    }

    private companion object {
        const val RUNNING_LOCATOR = "running:true,branch:default:any,personal:false,pinned:false"
    }
}
