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

package com.github.vase4kin.teamcityapp.changes.data

import com.github.vase4kin.teamcityapp.api.Repository
import javax.inject.Inject
import javax.inject.Provider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.rx2.await
import kotlinx.coroutines.withContext
import teamcityapp.features.changes.api.ChangesPage
import teamcityapp.features.changes.api.ChangesRepository
import teamcityapp.libraries.coroutines.IoDispatcher

/** Retains legacy wire DTOs and cache keys while exposing hydrated immutable changes. */
class AppChangesRepository @Inject constructor(
    private val repository: Provider<Repository>,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ChangesRepository {
    override suspend fun changes(url: String, nextHref: String?, forceRefresh: Boolean): ChangesPage = withContext(ioDispatcher) {
        // One account session owns the whole page, including its detail requests.
        val session = repository.get()
        val pageUrl = nextHref ?: "$url,count:10"
        val page = session.listChanges(pageUrl, forceRefresh).await()
        val summaries = if (page.count == 0) emptyList() else page.objects.orEmpty()
        val changes = summaries.mapIndexed { index, summary ->
            val details = session.change(summary.href).await().toChangeDetails()
            // Some old servers/fixtures omit IDs; opaque page URLs keep fallback keys unique.
            if (details.id.isNotBlank()) details else details.copy(id = summary.href?.takeIf { it.isNotBlank() } ?: "$pageUrl:$index")
        }
        ChangesPage(changes, page.nextHref?.takeIf { changes.isNotEmpty() && it.isNotBlank() })
    }

    override suspend fun count(url: String): Int = withContext(ioDispatcher) {
        repository.get().listChanges("$url,count:${Int.MAX_VALUE}&fields=count", true).await().count
    }
}
