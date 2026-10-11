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

package com.github.vase4kin.teamcityapp.buildlist.data

import com.github.vase4kin.teamcityapp.api.Repository
import com.github.vase4kin.teamcityapp.api.TeamCityService
import com.github.vase4kin.teamcityapp.builds.data.AppBuildHydrator
import com.github.vase4kin.teamcityapp.builds.data.AppBuildLaunchMapper
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage
import javax.inject.Inject
import javax.inject.Provider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.rx2.await
import kotlinx.coroutines.withContext
import teamcityapp.features.build_history.api.BuildHistoryPage
import teamcityapp.features.build_history.api.BuildHistoryQuery
import teamcityapp.features.build_history.api.BuildHistoryRepository
import teamcityapp.libraries.builds.BuildLaunchData
import teamcityapp.libraries.coroutines.IoDispatcher

class AppBuildHistoryRepository @Inject constructor(
    private val repository: Provider<Repository>,
    private val service: Provider<TeamCityService>,
    private val storage: SharedUserStorage,
    private val hydrator: AppBuildHydrator,
    private val mapper: AppBuildLaunchMapper,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : BuildHistoryRepository {
    override suspend fun page(query: BuildHistoryQuery, nextHref: String?, forceRefresh: Boolean): BuildHistoryPage = withContext(ioDispatcher) {
        currentCoroutineContext().ensureActive()
        val session = repository.get()
        val page = if (nextHref == null) {
            session.listBuilds(query.configurationId, query.locator, forceRefresh).await()
        } else {
            // Repository owns URL formatting. Continuations always bypass page cache.
            session.listMoreBuilds(nextHref).await()
        }
        currentCoroutineContext().ensureActive()
        val summaries = if (page.count == 0) emptyList() else page.objects.orEmpty().toList()
        val rows = hydrator.hydrate(session, summaries)
        BuildHistoryPage(rows, if (rows.isEmpty()) null else page.nextHref?.takeIf { it.isNotBlank() })
    }

    override suspend fun queuedBuild(href: String): BuildLaunchData = withContext(ioDispatcher) {
        currentCoroutineContext().ensureActive()
        // The legacy Show action deliberately bypasses Repository/RxCache.
        val capturedService = service.get()
        val build = capturedService.build(href).await()
        currentCoroutineContext().ensureActive()
        mapper.toLaunchData(build)
    }

    override suspend fun isFavorite(configurationId: String): Boolean = withContext(ioDispatcher) {
        currentCoroutineContext().ensureActive()
        storage.favoriteBuildTypeIds.contains(configurationId)
    }

    override suspend fun setFavorite(configurationId: String, favorite: Boolean): Unit = withContext(ioDispatcher) {
        currentCoroutineContext().ensureActive()
        if (favorite) storage.addBuildTypeToFavorites(configurationId) else storage.removeBuildTypeFromFavorites(configurationId)
    }
}
