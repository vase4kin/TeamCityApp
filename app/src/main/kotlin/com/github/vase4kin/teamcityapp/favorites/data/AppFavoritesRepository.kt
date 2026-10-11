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

package com.github.vase4kin.teamcityapp.favorites.data

import com.github.vase4kin.teamcityapp.api.Repository
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage
import javax.inject.Inject
import javax.inject.Provider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.rx2.await
import kotlinx.coroutines.withContext
import teamcityapp.features.favorites.api.FavoriteConfigurations
import teamcityapp.features.favorites.api.FavoritesRepository
import teamcityapp.libraries.build_configurations.BuildConfigurationSummary
import teamcityapp.libraries.build_configurations.ProjectReference
import teamcityapp.libraries.coroutines.IoDispatcher

/** Captures saved IDs and one API session without changing persisted favorites on failure. */
class AppFavoritesRepository @Inject constructor(
    private val storage: SharedUserStorage,
    private val repository: Provider<Repository>,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : FavoritesRepository {
    override suspend fun favorites(forceRefresh: Boolean): FavoriteConfigurations = withContext(ioDispatcher) {
        val savedIds = storage.favoriteBuildTypeIds.toList()
        if (savedIds.isEmpty()) return@withContext FavoriteConfigurations(savedIds, emptyList())

        // Every request in this batch uses this captured account's API/cache graph.
        val session = repository.get()
        val results = coroutineScope {
            // Keep legacy parallel fetches. awaitAll retains saved-ID order despite completion order.
            savedIds.map { savedId ->
                async {
                    currentCoroutineContext().ensureActive()
                    try {
                        val configuration = session.buildType(savedId, forceRefresh).await()
                        currentCoroutineContext().ensureActive()
                        FavoriteResult(
                            savedId,
                            BuildConfigurationSummary(
                                id = configuration.id?.takeIf { it.isNotBlank() } ?: savedId,
                                name = configuration.name.orEmpty(),
                                description = configuration.description,
                                project = ProjectReference(configuration.projectId.orEmpty(), configuration.projectName.orEmpty())
                            )
                        )
                    } catch (cancelled: CancellationException) {
                        throw cancelled
                    } catch (_: Exception) {
                        currentCoroutineContext().ensureActive()
                        FavoriteResult(savedId, null)
                    }
                }
            }.awaitAll()
        }
        currentCoroutineContext().ensureActive()
        FavoriteConfigurations(
            savedIds,
            results.mapNotNull { it.configuration },
            results.filter { it.configuration == null }.map { it.savedId }
        )
    }
}

private data class FavoriteResult(val savedId: String, val configuration: BuildConfigurationSummary?)
