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

package com.github.vase4kin.teamcityapp.api.cache

import com.github.vase4kin.teamcityapp.buildlist.api.Build
import io.rx_cache2.internal.Memory
import io.rx_cache2.internal.Persistence
import io.rx_cache2.internal.cache.BuildCacheKeys
import io.rx_cache2.internal.cache.TwoLayersCache
import javax.inject.Inject
import javax.inject.Singleton

/** Fresh collection responses reconcile their own membership against the shared build cache. */
interface BulkBuildCache {
    fun beginBulkBuilds(query: String): BulkBuildRequest
    fun reconcileBulkBuilds(request: BulkBuildRequest, rows: List<Build>)
}

class BulkBuildRequest internal constructor(internal val query: String, internal val version: Long, internal val generation: Long)

@Singleton
internal class SharedBulkBuildCache @Inject constructor(
    private val state: BuildCacheState,
    private val cache: TwoLayersCache,
    private val cacheKeys: BuildCacheKeys,
    private val memory: Memory,
    private val persistence: Persistence
) : BulkBuildCache {
    override fun beginBulkBuilds(query: String): BulkBuildRequest = synchronized(state.lock) { state.begin(query) }

    override fun reconcileBulkBuilds(request: BulkBuildRequest, rows: List<Build>) = synchronized(state.lock) {
        rows.forEach { row ->
            require(!row.id.isNullOrBlank() && !row.href.isNullOrBlank()) { "A build snapshot requires an ID and detail URL" }
        }
        val previous = state.previous(request) ?: return@synchronized
        val presentKeys = rows.map { cacheKeys.buildKey(it.href) }.toSet()
        val current = buildMap<String, Long> {
            rows.associateBy { it.href }.values.forEach { row ->
                val key = cacheKeys.buildKey(row.href)
                // A different list or an individual fetch completed after this request began.
                // Preserve that newer snapshot rather than overwriting it with a late response.
                if ((state.version(key) ?: 0L) <= request.version) {
                    cache.save(BUILD_CACHE_PROVIDER_KEY, row.href, "", row, 3_600_000L, true, false)
                    put(key, requireNotNull(state.version(key)))
                }
            }
        }
        previous.forEach { (key, version) ->
            if (key !in presentKeys && state.version(key) == version) {
                // Exact key eviction also works after a soft memory reference has been lost.
                memory.evict(key)
                persistence.evict(key)
            }
        }
        state.completed(request, current)
    }
}
