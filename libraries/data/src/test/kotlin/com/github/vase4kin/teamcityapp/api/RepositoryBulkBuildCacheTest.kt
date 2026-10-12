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

package com.github.vase4kin.teamcityapp.api

import com.github.vase4kin.teamcityapp.api.cache.CacheProviders
import com.github.vase4kin.teamcityapp.api.cache.SharedRxCache
import com.github.vase4kin.teamcityapp.buildlist.api.Build
import com.github.vase4kin.teamcityapp.buildlist.api.Builds
import com.google.gson.Gson
import io.reactivex.Single
import io.reactivex.subjects.SingleSubject
import io.rx_cache2.DynamicKey
import io.rx_cache2.EvictDynamicKey
import io.rx_cache2.internal.RxCache
import io.victoralbertos.jolyglot.GsonSpeaker
import kotlinx.coroutines.test.StandardTestDispatcher
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.mockito.kotlin.*
import teamcityapp.libraries.data.builds.BUILD_LAUNCH_FIELDS
import teamcityapp.libraries.remote.url.UrlFormatter

class RepositoryBulkBuildCacheTest {
    @get:Rule val folder = TemporaryFolder()
    private val service = mock<TeamCityService>()
    private val formatter = mock<UrlFormatter>()
    private var elapsed = 0L
    private val cache by lazy { SharedRxCache.create(folder.newFolder(), GsonSpeaker()) { System.currentTimeMillis() + elapsed } }
    private fun repository(shared: SharedRxCache = cache) = RepositoryImpl(service, shared.providers, formatter, StandardTestDispatcher())
    private val locator = "running:true,branch:default:any,personal:false,pinned:false"

    @Test fun aHundredRunningOrQueuedSnapshotsSeedTheExistingIndividualCacheWithoutDetailRequests() {
        for (state in listOf("running", "queued")) {
            val repo = repository()
            val rows = (1..100).map { build("$state-$it", state) }
            if (state == "running") {
                whenever(service.listRunningBuilds(locator, BUILD_LAUNCH_FIELDS)).thenReturn(Single.just(Builds(100, rows)))
                repo.listRunningBuilds(locator, BUILD_LAUNCH_FIELDS, true).blockingGet()
            } else {
                whenever(service.listQueueBuilds(null, BUILD_LAUNCH_FIELDS)).thenReturn(Single.just(Builds(100, rows)))
                repo.listQueueBuilds(null, BUILD_LAUNCH_FIELDS, true).blockingGet()
            }
            rows.forEach { row ->
                val loaded = repo.build(row.href, false).blockingGet()
                assertEquals(Gson().toJsonTree(row), Gson().toJsonTree(loaded))
            }
        }
        verify(service).listRunningBuilds(locator, BUILD_LAUNCH_FIELDS)
        verify(service).listQueueBuilds(null, BUILD_LAUNCH_FIELDS)
        verifyNoMoreInteractions(service)
        verifyNoInteractions(formatter)
    }

    @Test fun freshPollReplacesTheSnapshotIncludingChangedResults() {
        val repo = repository()
        val old = build("1", "running", 2)
        whenever(service.listRunningBuilds(locator, BUILD_LAUNCH_FIELDS)).thenReturn(Single.just(Builds(1, listOf(old))))
        repo.listRunningBuilds(locator, BUILD_LAUNCH_FIELDS, false).blockingGet()
        val updated = build("1", "running", 7)
        whenever(service.listRunningBuilds(locator, BUILD_LAUNCH_FIELDS)).thenReturn(Single.just(Builds(1, listOf(updated))))
        repo.listRunningBuilds(locator, BUILD_LAUNCH_FIELDS, true).blockingGet()
        assertEquals(7, repo.build(old.href, false).blockingGet().testOccurrences!!.count)
        verify(service, times(2)).listRunningBuilds(locator, BUILD_LAUNCH_FIELDS)
        verify(service, never()).build(any())
    }

    @Test fun collectionCacheHitsDoNotRenewActiveSnapshotAge() {
        val repo = repository()
        val row = build("1", "running")
        whenever(service.listRunningBuilds(locator, BUILD_LAUNCH_FIELDS)).thenReturn(Single.just(Builds(1, listOf(row))))
        repo.listRunningBuilds(locator, BUILD_LAUNCH_FIELDS, false).blockingGet()
        elapsed = 31_000
        repo.listRunningBuilds(locator, BUILD_LAUNCH_FIELDS, false).blockingGet()
        stubDetails(row.href, build("1", "finished", 9))
        assertEquals("finished", repo.build(row.href, false).blockingGet().state)
        verify(service).build(row.href)
    }

    @Test fun cachedCollectionNeverOverwritesMoreRecentIndividualDetails() {
        val repo = repository()
        val row = build("1", "running")
        whenever(service.listRunningBuilds(locator, BUILD_LAUNCH_FIELDS)).thenReturn(Single.just(Builds(1, listOf(row))))
        repo.listRunningBuilds(locator, BUILD_LAUNCH_FIELDS, false).blockingGet()
        stubDetails(row.href, build("1", "finished", 9))
        repo.build(row.href, true).blockingGet()
        repo.listRunningBuilds(locator, BUILD_LAUNCH_FIELDS, false).blockingGet()
        assertEquals("finished", repo.build(row.href, false).blockingGet().state)
        verify(service).build(row.href)
    }

    @Test fun runningAndQueuedDiskSnapshotsExpireEvenWithLegacyOneHourEnvelopes() {
        for (state in listOf("running", "queued")) {
            val directory = folder.newFolder()
            val row = build(state, state)
            val original = RxCache.Builder().persistence(directory, GsonSpeaker()).using(CacheProviders::class.java)
            original.build(Single.just(row), DynamicKey(row.href), EvictDynamicKey(false)).blockingGet()
            val reopened = SharedRxCache.create(directory, GsonSpeaker()) { System.currentTimeMillis() + 31_000 }
            val repo = repository(reopened)
            stubDetails(row.href, build(state, "finished"))
            assertEquals("finished", repo.build(row.href, false).blockingGet().state)
            verify(service).build(row.href)
        }
    }

    @Test fun freshBulkSnapshotsSurviveReopeningTheDiskCache() {
        val directory = folder.newFolder()
        val shared = SharedRxCache.create(directory, GsonSpeaker())
        val repo = repository(shared)
        val row = build("1", "queued")
        whenever(service.listQueueBuilds(null, BUILD_LAUNCH_FIELDS)).thenReturn(Single.just(Builds(1, listOf(row))))
        repo.listQueueBuilds(null, BUILD_LAUNCH_FIELDS, false).blockingGet()
        val reopened = repository(SharedRxCache.create(directory, GsonSpeaker()))
        assertEquals("queued", reopened.build(row.href, false).blockingGet().state)
        verify(service, never()).build(any())
    }

    @Test fun explicitRefreshAndAccountCacheEvictionBypassSeededSnapshots() {
        val repo = repository()
        val row = build("1", "queued")
        whenever(service.listQueueBuilds(null, BUILD_LAUNCH_FIELDS)).thenReturn(Single.just(Builds(1, listOf(row))))
        repo.listQueueBuilds(null, BUILD_LAUNCH_FIELDS, false).blockingGet()
        stubDetails(row.href, build("1", "running"))
        assertEquals("running", repo.build(row.href, true).blockingGet().state)
        cache.evictAll().ignoreElements().blockingAwait()
        assertEquals("running", repo.build(row.href, false).blockingGet().state)
        verify(service, times(2)).build(row.href)
    }

    @Test fun countOnlyAndSummaryResponsesCannotSeedIncompleteDetails() {
        val repo = repository()
        val row = build("1", "queued")
        for (fields in listOf(null, "count", "count,build(id,href)")) {
            whenever(service.listQueueBuilds(null, fields)).thenReturn(Single.just(Builds(1, listOf(row))))
            repo.listQueueBuilds(null, fields, false).blockingGet()
        }
        stubDetails(row.href, build("1", "finished"))
        assertEquals("finished", repo.build(row.href, false).blockingGet().state)
        verify(service).build(row.href)
    }

    @Test fun zeroCountAndInvalidRowsDoNotSeedIndividualSnapshots() {
        val repo = repository()
        val row = build("1", "queued")
        whenever(service.listQueueBuilds(null, BUILD_LAUNCH_FIELDS)).thenReturn(Single.just(Builds(0, listOf(row))))
        repo.listQueueBuilds(null, BUILD_LAUNCH_FIELDS, false).blockingGet()
        val invalid = Gson().fromJson("""{"id":"2","state":"queued"}""", Build::class.java)
        whenever(service.listQueueBuilds(null, BUILD_LAUNCH_FIELDS)).thenReturn(Single.just(Builds(2, listOf(row, invalid))))
        repo.listQueueBuilds(null, BUILD_LAUNCH_FIELDS, true).test().awaitDone(10, java.util.concurrent.TimeUnit.SECONDS).assertError { error ->
            (error as? io.reactivex.exceptions.CompositeException)?.exceptions.orEmpty().any { it is IllegalArgumentException && it.message.orEmpty().contains("ID and detail URL") }
        }
        stubDetails(row.href, build("1", "finished"))
        assertEquals("finished", repo.build(row.href, false).blockingGet().state)
        verify(service).build(row.href)
    }

    @Test fun cancelledBulkResponseCannotSeedOldAccountDataAfterEviction() {
        val repo = repository()
        val pending = SingleSubject.create<Builds>()
        val subscribed = java.util.concurrent.CountDownLatch(1)
        whenever(service.listQueueBuilds(null, BUILD_LAUNCH_FIELDS)).thenReturn(pending.doOnSubscribe { subscribed.countDown() })
        val observer = repo.listQueueBuilds(null, BUILD_LAUNCH_FIELDS, true).test()
        assertTrue(subscribed.await(10, java.util.concurrent.TimeUnit.SECONDS))
        assertTrue(pending.hasObservers())
        observer.dispose()
        assertFalse(pending.hasObservers())
        cache.evictAll().ignoreElements().blockingAwait()
        pending.onSuccess(Builds(1, listOf(build("1", "queued"))))
        stubDetails("/builds/1", build("1", "finished"))
        assertEquals("finished", repo.build("/builds/1", false).blockingGet().state)
        verify(service).build("/builds/1")
    }

    private fun stubDetails(href: String, row: Build) {
        whenever(formatter.formatBasicUrl(href)).thenReturn(href)
        whenever(service.build(href)).thenReturn(Single.just(row))
    }
    private fun build(id: String, state: String, tests: Int = 2): Build = Gson().fromJson(
        """{"id":"$id","href":"/builds/$id","state":"$state","number":"123","buildTypeId":"config","buildType":{"id":"config","name":"CI","projectName":"Project"},"testOccurrences":{"href":"/tests/$id","count":$tests,"passed":$tests},"changes":{"href":"/changes/$id","count":1},"properties":{"property":[{"name":"branch","value":"main"}]},"artifacts":{"href":"/artifacts/$id"},"snapshot-dependencies":{"href":"/snapshots/$id","count":1}}""",
        Build::class.java
    )
}
