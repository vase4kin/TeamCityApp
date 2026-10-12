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
import com.github.vase4kin.teamcityapp.buildlist.api.Builds
import com.google.gson.Gson
import io.reactivex.Single
import io.rx_cache2.DynamicKey
import io.rx_cache2.EvictDynamicKey
import io.rx_cache2.internal.CacheProviderProxy
import io.rx_cache2.internal.Record
import io.rx_cache2.internal.cache.BuildCacheKeys
import io.victoralbertos.jolyglot.GsonSpeaker
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class BulkBuildCacheTest {
    @get:Rule val folder = TemporaryFolder()
    private fun component(): SharedRxCacheComponent = DaggerSharedRxCacheComponent.builder()
        .directory(folder.newFolder()).serializer(GsonSpeaker()).liveBuildPolicy(LiveBuildCachePolicy())
        .useExpiredData(false).maxCacheSize(100).encryptionKey("").migrations(emptyList()).build()

    @Test fun disappearanceStillInvalidatesAfterMemoryLossAndAnInterveningDiskRead() {
        val graph = component()
        val providers = CacheProviderProxy.create(graph.processor())
        val bulk = graph.bulkBuildCache()
        val row = build("queued")
        val request = bulk.beginBulkBuilds("queue")
        // Execute reconciliation behind the processor's startup gate, as RepositoryImpl does.
        providers.listQueuedBuilds(Single.just(Builds(1, listOf(row))).doOnSuccess { bulk.reconcileBulkBuilds(request, listOf(row)) }, DynamicKey("queue"), EvictDynamicKey(true)).blockingGet()
        graph.memory().evictAll()
        assertEquals("queued", providers.build(Single.error(AssertionError("Restore disk data")), DynamicKey(row.href), EvictDynamicKey(false)).blockingGet().state)
        bulk.reconcileBulkBuilds(bulk.beginBulkBuilds("queue"), emptyList())
        assertEquals("running", providers.build(Single.just(build("running")), DynamicKey(row.href), EvictDynamicKey(false)).blockingGet().state)
    }

    @Test fun disappearancePreservesANewerRunningSnapshotRestoredFromDisk() {
        val graph = component()
        val providers = CacheProviderProxy.create(graph.processor())
        val bulk = graph.bulkBuildCache()
        val queued = build("queued")
        val initial = bulk.beginBulkBuilds("queue")
        providers.listQueuedBuilds(Single.just(Builds(1, listOf(queued))).doOnSuccess { bulk.reconcileBulkBuilds(initial, listOf(queued)) }, DynamicKey("queue"), EvictDynamicKey(true)).blockingGet()
        bulk.reconcileBulkBuilds(bulk.beginBulkBuilds("running"), listOf(build("running")))
        graph.memory().evictAll()
        assertEquals("running", providers.build(Single.error(AssertionError("Restore the newer snapshot")), DynamicKey(queued.href), EvictDynamicKey(false)).blockingGet().state)
        bulk.reconcileBulkBuilds(bulk.beginBulkBuilds("queue"), emptyList())
        assertEquals("running", providers.build(Single.error(AssertionError("Keep the newer snapshot")), DynamicKey(queued.href), EvictDynamicKey(false)).blockingGet().state)
    }

    @Test fun delayedDiskWriteCannotResurrectASnapshotAfterDisappearance() {
        val graph = component()
        val providers = CacheProviderProxy.create(graph.processor())
        val bulk = graph.bulkBuildCache()
        val row = build("running")
        val initial = bulk.beginBulkBuilds("running")
        providers.listRunningBuilds(Single.just(Builds(1, listOf(row))).doOnSuccess { bulk.reconcileBulkBuilds(initial, listOf(row)) }, DynamicKey("running"), EvictDynamicKey(true)).blockingGet()
        val key = BuildCacheKeys(graph.memory(), graph.persistence()).buildKey(row.href)
        // Pause an individual SaveRecord between its memory and persistence writes.
        val delayedRecord = Record(build("running"), true, 3_600_000L)
        graph.memory().put(key, delayedRecord)
        bulk.reconcileBulkBuilds(bulk.beginBulkBuilds("running"), listOf(build("running")))
        bulk.reconcileBulkBuilds(bulk.beginBulkBuilds("running"), emptyList())
        graph.persistence().saveRecord(key, delayedRecord, false, "")
        assertEquals("finished", providers.build(Single.just(build("finished")), DynamicKey(row.href), EvictDynamicKey(false)).blockingGet().state)
    }

    @Test fun clearingTheAccountCacheDropsMembershipAndRejectsAlreadyStartedBulkWrites() {
        val graph = component()
        val providers = CacheProviderProxy.create(graph.processor())
        val bulk = graph.bulkBuildCache()
        val row = build("queued")
        val initial = bulk.beginBulkBuilds("queue")
        providers.listQueuedBuilds(Single.just(Builds(1, listOf(row))).doOnSuccess { bulk.reconcileBulkBuilds(initial, listOf(row)) }, DynamicKey("queue"), EvictDynamicKey(true)).blockingGet()
        val obsolete = bulk.beginBulkBuilds("queue")
        graph.processor().evictAll().ignoreElements().blockingAwait()
        providers.build(Single.just(build("finished")), DynamicKey(row.href), EvictDynamicKey(false)).blockingGet()
        bulk.reconcileBulkBuilds(obsolete, listOf(row))
        bulk.reconcileBulkBuilds(bulk.beginBulkBuilds("queue"), emptyList())
        assertEquals("finished", providers.build(Single.error(AssertionError("Keep the new account's snapshot")), DynamicKey(row.href), EvictDynamicKey(false)).blockingGet().state)
    }

    private fun build(state: String): Build = Gson().fromJson("""{"id":"1","href":"/builds/1","state":"$state"}""", Build::class.java)
}
