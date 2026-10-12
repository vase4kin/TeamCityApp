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

import io.rx_cache2.internal.Record
import java.util.WeakHashMap
import javax.inject.Inject
import javax.inject.Singleton

/** Shared by both cache layers so conditional invalidation cannot race a newer write. */
@Singleton
internal class BuildCacheState @Inject constructor() {
    val lock = Any()
    private var sequence = 0L
    private var generation = 0L
    private val recordVersions = WeakHashMap<Record<*>, Long>()
    private val currentVersions = mutableMapOf<String, Long>()
    private val diskVersions = mutableMapOf<String, Long>()
    private val queries = mutableMapOf<String, QuerySnapshot>()

    // All methods are called while holding lock. Loading a record from disk preserves its
    // revision; merely opening Details must not acquire ownership of another list's snapshot.
    fun memoryWrite(key: String, record: Record<*>): Boolean {
        val restoredVersion = recordVersions[record]
        if (restoredVersion != null) return currentVersions[key] == restoredVersion
        val version = ++sequence
        recordVersions[record] = version
        currentVersions[key] = version
        return true
    }

    fun diskRead(key: String, record: Record<*>) {
        val version = diskVersions.getOrPut(key) { 0L }
        currentVersions.putIfAbsent(key, version)
        recordVersions[record] = version
    }

    fun diskWrite(key: String, record: Record<*>, write: () -> Unit) {
        val version = recordVersions[record] ?: return
        // SaveRecord writes memory first. An intervening invalidation or newer memory write
        // must not let its delayed disk write resurrect the old snapshot.
        if (currentVersions[key] != version) return
        write()
        diskVersions[key] = version
    }

    fun version(key: String): Long? = currentVersions[key]

    fun evict(key: String) {
        currentVersions.remove(key)
        diskVersions.remove(key)
    }

    fun clear() {
        generation++
        currentVersions.clear()
        diskVersions.clear()
        // Keep weak tokens for in-flight records: their delayed disk writes must be rejected.
        queries.clear()
    }

    fun begin(query: String) = BulkBuildRequest(query, ++sequence, generation)

    fun previous(request: BulkBuildRequest): Map<String, Long>? {
        if (request.generation != generation) return null
        val previous = queries[request.query]
        if (previous != null && previous.requestVersion > request.version) return null
        return previous?.buildVersions.orEmpty()
    }

    fun completed(request: BulkBuildRequest, builds: Map<String, Long>) {
        queries[request.query] = QuerySnapshot(request.version, builds)
    }

    private data class QuerySnapshot(val requestVersion: Long, val buildVersions: Map<String, Long>)
}
