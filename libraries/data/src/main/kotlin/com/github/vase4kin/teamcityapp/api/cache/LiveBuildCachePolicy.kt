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
import io.rx_cache2.internal.Persistence
import io.rx_cache2.internal.Record

/** RxCache resets Record.lifeTime on reads, so enforce active-build age before that happens. */
internal class LiveBuildCachePolicy(private val nowMillis: () -> Long = System::currentTimeMillis) {
    private fun isLive(record: Record<*>): Boolean = (record.data as? Build)?.state.let { it == "running" || it == "queued" }

    fun expired(record: Record<*>): Boolean = isLive(record) && nowMillis() - record.timeAtWhichWasPersisted >= LIVE_BUILD_MAX_AGE_MILLIS

    fun limitLifetime(record: Record<*>) {
        if (isLive(record)) record.lifeTime = minOf(record.lifeTime ?: LIVE_BUILD_MAX_AGE_MILLIS, LIVE_BUILD_MAX_AGE_MILLIS)
    }

    companion object {
        const val LIVE_BUILD_MAX_AGE_MILLIS = 30_000L
    }
}

/** Apply the same age limit to old disk envelopes, including records from earlier app versions. */
internal class LiveBuildPersistence(
    private val delegate: Persistence,
    private val policy: LiveBuildCachePolicy,
    private val state: BuildCacheState
) : Persistence by delegate {
    override fun <T> retrieveRecord(key: String, isEncrypted: Boolean, encryptKey: String): Record<T>? = synchronized(state.lock) {
        val record = delegate.retrieveRecord<T>(key, isEncrypted, encryptKey) ?: return@synchronized null
        if (policy.expired(record)) return@synchronized null
        if (record.data is Build) state.diskRead(key, record)
        record
    }

    override fun saveRecord(key: String, record: Record<*>, isEncrypted: Boolean, encryptKey: String) = synchronized(state.lock) {
        policy.limitLifetime(record)
        if (record.data is Build) {
            state.diskWrite(key, record) { delegate.saveRecord(key, record, isEncrypted, encryptKey) }
        } else {
            delegate.saveRecord(key, record, isEncrypted, encryptKey)
        }
    }

    override fun evict(key: String) = synchronized(state.lock) {
        delegate.evict(key)
        state.evict(key)
    }

    override fun evictAll() = synchronized(state.lock) {
        delegate.evictAll()
        state.clear()
    }
}
