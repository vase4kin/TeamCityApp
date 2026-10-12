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
import io.rx_cache2.internal.Record
import io.rx_cache2.internal.cache.memory.ReferenceMapMemory

/** Retains RxCache's soft references, but never exposes an iterator over its live map. */
internal class SnapshotCacheMemory(
    private val policy: LiveBuildCachePolicy = LiveBuildCachePolicy(),
    private val state: BuildCacheState = BuildCacheState()
) : Memory {
    private val delegate = ReferenceMapMemory()
    private val lock get() = state.lock

    override fun <T> getIfPresent(key: String): Record<T>? = synchronized(lock) {
        delegate.getIfPresent<T>(key)?.takeUnless(policy::expired)
    }
    override fun <T> put(key: String, record: Record<T>) = synchronized(lock) {
        policy.limitLifetime(record)
        if (record.data !is Build || state.memoryWrite(key, record)) delegate.put(key, record)
    }
    override fun keySet(): Set<String> = synchronized(lock) { delegate.keySet().toSet() }
    override fun evict(key: String) = synchronized(lock) { delegate.evict(key) }
    override fun evictAll() = synchronized(lock) { delegate.evictAll() }
}
