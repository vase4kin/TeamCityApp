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
import io.reactivex.subjects.SingleSubject
import io.rx_cache2.DynamicKey
import io.rx_cache2.EvictDynamicKey
import io.rx_cache2.internal.Record
import io.rx_cache2.internal.RxCache
import io.victoralbertos.jolyglot.GsonSpeaker
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class SharedRxCacheTest {
    @get:Rule val folder = TemporaryFolder()

    @Test fun evictionIteratorSurvivesConcurrentWritesRemovalsAndFullEviction() {
        val memory = SnapshotCacheMemory()
        val record = Record("cached", true, 60_000L)
        memory.put("first", record)
        memory.put("second", record)
        // RxCache's Action iterates this set after the memory boundary returns it.
        val iterator = memory.keySet().iterator()
        val writer = Executors.newSingleThreadExecutor()
        try {
            writer.submit {
                memory.put("third", record)
                memory.evict("first")
                memory.evictAll()
                memory.put("fourth", record)
            }.get(10, TimeUnit.SECONDS)
            assertEquals(setOf("first", "second"), iterator.asSequence().toSet())
            assertEquals(setOf("fourth"), memory.keySet())
            assertSame(record, memory.getIfPresent<String>("fourth"))
            assertNull(memory.getIfPresent<String>("first"))
        } finally {
            writer.shutdownNow()
        }
    }

    @Test fun aHundredConcurrentForcedRefreshesKeepLoadersParallelAndCompleteWithoutCacheErrors() {
        val cache = SharedRxCache.create(folder.newFolder(), GsonSpeaker()).providers
        val count = 100
        val old = (1..count).map { build(it, "old") }
        old.forEach { cache.build(Single.just(it), DynamicKey(it.href), EvictDynamicKey(false)).blockingGet() }
        // Also populate a different provider to exercise the shared key set.
        cache.listRunningBuilds(Single.just(Builds(count, old)), DynamicKey("running"), EvictDynamicKey(false)).blockingGet()
        val subscribed = CountDownLatch(count)
        val loaders = (1..count).map { SingleSubject.create<Build>() }
        val observers = loaders.mapIndexed { index, loader ->
            cache.build(loader.doOnSubscribe { subscribed.countDown() }, DynamicKey(old[index].href), EvictDynamicKey(true)).test()
        }
        // Every upstream must be subscribed before any response completes: no network serialization.
        assertTrue(subscribed.await(10, TimeUnit.SECONDS))
        assertTrue(loaders.all { it.hasObservers() })
        val start = CountDownLatch(1)
        val workers = Executors.newFixedThreadPool(8)
        try {
            val completions = loaders.mapIndexed { index, loader ->
                workers.submit {
                    check(start.await(10, TimeUnit.SECONDS))
                    loader.onSuccess(build(index + 1, "fresh"))
                }
            }
            start.countDown()
            completions.forEach { it.get(10, TimeUnit.SECONDS) }
            observers.forEachIndexed { index, observer ->
                observer.awaitDone(10, TimeUnit.SECONDS).assertComplete().assertNoErrors()
                    .assertValue { it.id == "${index + 1}" && it.number == "fresh" }
                cache.build(Single.error(AssertionError("Cached build should avoid the network")), DynamicKey(old[index].href), EvictDynamicKey(false))
                    .test().assertComplete().assertNoErrors().assertValue { it.number == "fresh" }
            }
            cache.listRunningBuilds(Single.error(AssertionError("Unrelated provider should remain cached")), DynamicKey("running"), EvictDynamicKey(false))
                .test().assertComplete().assertNoErrors().assertValue { it.count == count }
        } finally {
            observers.forEach { it.dispose() }
            workers.shutdownNow()
        }
    }

    @Test fun diskRecordsRemainCompatibleWithTheOriginalRxCacheInBothDirections() {
        val directory = folder.newFolder()
        val key = DynamicKey("/builds/1")
        val legacy = RxCache.Builder().persistence(directory, GsonSpeaker()).using(CacheProviders::class.java)
        legacy.build(Single.just(build(1, "legacy")), key, EvictDynamicKey(false)).blockingGet()
        val shared = SharedRxCache.create(directory, GsonSpeaker()).providers
        assertEquals("legacy", shared.build(Single.error(AssertionError("Read legacy disk data")), key, EvictDynamicKey(false)).blockingGet().number)
        shared.build(Single.just(build(1, "updated")), key, EvictDynamicKey(true)).blockingGet()
        val reopened = RxCache.Builder().persistence(directory, GsonSpeaker()).using(CacheProviders::class.java)
        assertEquals("updated", reopened.build(Single.error(AssertionError("Read updated disk data")), key, EvictDynamicKey(false)).blockingGet().number)
    }

    @Test fun cancellingARefreshDisposesTheLoaderAndRetainsTheCompletedCachedBuild() {
        val cache = SharedRxCache.create(folder.newFolder(), GsonSpeaker()).providers
        val key = DynamicKey("/builds/1")
        cache.build(Single.just(build(1, "cached")), key, EvictDynamicKey(false)).blockingGet()
        val pending = SingleSubject.create<Build>()
        val observer = cache.build(pending, key, EvictDynamicKey(true)).test()
        assertTrue(pending.hasObservers())
        observer.dispose()
        assertFalse(pending.hasObservers())
        pending.onSuccess(build(1, "obsolete"))
        assertEquals("cached", cache.build(Single.error(AssertionError("Use the retained cache")), key, EvictDynamicKey(false)).blockingGet().number)
    }

    @Test fun fullEvictionClearsMemoryAndDiskForEveryProvider() {
        val directory = folder.newFolder()
        val shared = SharedRxCache.create(directory, GsonSpeaker())
        val key = DynamicKey("/builds/1")
        shared.providers.build(Single.just(build(1, "cached")), key, EvictDynamicKey(false)).blockingGet()
        shared.providers.listRunningBuilds(Single.just(Builds(1, listOf(build(1, "cached")))), DynamicKey("running"), EvictDynamicKey(false)).blockingGet()
        shared.evictAll().ignoreElements().blockingAwait()
        assertEquals("fresh", shared.providers.build(Single.just(build(1, "fresh")), key, EvictDynamicKey(false)).blockingGet().number)
        val reopened = SharedRxCache.create(directory, GsonSpeaker()).providers
        val loaded = reopened.listRunningBuilds(Single.just(Builds(0, emptyList())), DynamicKey("running"), EvictDynamicKey(false)).blockingGet()
        assertEquals(0, loaded.count)
    }

    private fun build(id: Int, number: String): Build = Gson().fromJson(
        """{"id":"$id","href":"/builds/$id","state":"running","number":"$number"}""",
        Build::class.java
    )
}
