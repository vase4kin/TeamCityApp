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

package com.github.vase4kin.teamcityapp.buildlist.data

import com.github.vase4kin.teamcityapp.api.Repository
import com.github.vase4kin.teamcityapp.api.TeamCityService
import com.github.vase4kin.teamcityapp.buildlist.api.Build
import com.github.vase4kin.teamcityapp.buildlist.api.Builds
import com.github.vase4kin.teamcityapp.builds.data.*
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage
import io.reactivex.Single
import io.reactivex.subjects.SingleSubject
import javax.inject.Provider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test
import org.mockito.kotlin.*
import teamcityapp.features.build_history.api.BuildHistoryPage
import teamcityapp.features.build_history.api.BuildHistoryQuery

@OptIn(ExperimentalCoroutinesApi::class)
class AppBuildHistoryRepositoryTest {
    private val mapper = AppBuildLaunchMapper()
    private val hydrator = AppBuildHydrator(mapper)
    private fun adapter(session: Repository, dispatcher: CoroutineDispatcher, service: TeamCityService = mock(), storage: SharedUserStorage = mock()) = AppBuildHistoryRepository(Provider { session }, Provider { service }, storage, hydrator, mapper, dispatcher)

    @Test fun initialPageKeepsSelectedLocatorAndAllowsPageCache() = runTest {
        val session = mock<Repository>()
        val query = BuildHistoryQuery("Android", "state:queued,personal:false,pinned:any")
        val rows = listOf(draftBuild("finished"), draftBuild("queued", "queued"))
        doReturn(Single.just(draftPage(rows, "/next?locator=opaque"))).whenever(session).listBuilds("Android", query.locator, false)
        rows.forEach { doReturn(Single.just(it)).whenever(session).build(it.href, false) }
        val result = adapter(session, StandardTestDispatcher(testScheduler)).page(query, null, false)
        assertEquals(rows.map { it.id }, result.builds.map { it.id })
        assertEquals("/next?locator=opaque", result.nextHref)
        verify(session).listBuilds("Android", query.locator, false)
        rows.forEach { verify(session).build(it.href, false) }
        verifyNoMoreInteractions(session)
    }

    @Test fun forcedPageDoesNotForceEveryFinishedDetailToRefresh() = runTest {
        val session = mock<Repository>()
        val query = BuildHistoryQuery("Android")
        doReturn(Single.just(draftPage(listOf(draftBuild("1"))))).whenever(session).listBuilds("Android", query.locator, true)
        doReturn(Single.just(draftBuild("1"))).whenever(session).build("/builds/1", false)
        assertEquals("1", adapter(session, StandardTestDispatcher(testScheduler)).page(query, null, true).builds.single().id)
        verify(session).listBuilds("Android", query.locator, true)
        verify(session).build("/builds/1", false)
        verifyNoMoreInteractions(session)
    }

    @Test fun continuationUrlIsForwardedExactlyAndUsesTheUncachedContinuationEndpoint() = runTest {
        val session = mock<Repository>()
        val query = BuildHistoryQuery("Android")
        val url = "/app/rest/builds?locator=buildType:(id:Android),start:10&fields=build(id,href)"
        doReturn(Single.just(draftPage(listOf(draftBuild("1"))))).whenever(session).listMoreBuilds(url)
        doReturn(Single.just(draftBuild("1"))).whenever(session).build("/builds/1", false)
        assertEquals("1", adapter(session, StandardTestDispatcher(testScheduler)).page(query, url, false).builds.single().id)
        verify(session).listMoreBuilds(url)
        verify(session).build("/builds/1", false)
        verifyNoMoreInteractions(session)
    }

    @Test fun emptyCountClearsAnOldCursorWithoutHydratingStaleRows() = runTest {
        val session = mock<Repository>()
        val query = BuildHistoryQuery("Android")
        doReturn(Single.just(draftPage(listOf(draftBuild("stale")), "/stale-cursor", 0))).whenever(session).listBuilds("Android", query.locator, false)
        assertEquals(BuildHistoryPage(emptyList()), adapter(session, StandardTestDispatcher(testScheduler)).page(query, null, false))
        verify(session).listBuilds("Android", query.locator, false)
        verifyNoMoreInteractions(session)
    }

    @Test fun emptyRowsAlsoClearCursorEvenIfServerCountIsNonzero() = runTest {
        val session = mock<Repository>()
        val query = BuildHistoryQuery("Android")
        doReturn(Single.just(draftPage(emptyList(), "/stale-cursor", 2))).whenever(session).listBuilds("Android", query.locator, false)
        assertEquals(BuildHistoryPage(emptyList()), adapter(session, StandardTestDispatcher(testScheduler)).page(query, null, false))
    }

    @Test fun whitespaceOnlyNextHrefEndsPagination() = runTest {
        val session = mock<Repository>()
        val query = BuildHistoryQuery("Android")
        doReturn(Single.just(draftPage(listOf(draftBuild("1")), "  "))).whenever(session).listBuilds("Android", query.locator, false)
        doReturn(Single.just(draftBuild("1"))).whenever(session).build("/builds/1", false)
        assertNull(adapter(session, StandardTestDispatcher(testScheduler)).page(query, null, false).nextHref)
    }

    @Test fun oneResolvedAccountSessionIsUsedForTheWholeHydrationBatch() = runTest {
        val first = mock<Repository>()
        val nextAccount = mock<Repository>()
        val query = BuildHistoryQuery("Android")
        val pending = SingleSubject.create<Builds>()
        var current = first
        var resolutions = 0
        doReturn(pending).whenever(first).listBuilds("Android", query.locator, false)
        doReturn(Single.just(draftBuild("1"))).whenever(first).build("/builds/1", false)
        val adapter = AppBuildHistoryRepository(
            Provider {
                resolutions++
                current
            },
            Provider { mock<TeamCityService>() },
            mock(),
            hydrator,
            mapper,
            StandardTestDispatcher(testScheduler)
        )
        val request = async { adapter.page(query, null, false) }
        runCurrent()
        current = nextAccount
        pending.onSuccess(draftPage(listOf(draftBuild("1"))))
        assertEquals("1", request.await().builds.single().id)
        assertEquals(1, resolutions)
        verifyNoInteractions(nextAccount)
    }

    @Test fun cancellationDisposesPageSubscriptionAndDoesNotHydrateLateResponse() = runTest {
        val session = mock<Repository>()
        val query = BuildHistoryQuery("Android")
        val pending = SingleSubject.create<Builds>()
        doReturn(pending).whenever(session).listBuilds("Android", query.locator, false)
        val request = async { adapter(session, StandardTestDispatcher(testScheduler)).page(query, null, false) }
        runCurrent()
        request.cancelAndJoin()
        assertFalse(pending.hasObservers())
        pending.onSuccess(draftPage(listOf(draftBuild("late"))))
        runCurrent()
        verify(session).listBuilds("Android", query.locator, false)
        verifyNoMoreInteractions(session)
    }

    @Test fun queuedShowUsesCapturedUncachedServiceAndFullDetailMapping() = runTest {
        val session = mock<Repository>()
        val service = mock<TeamCityService>()
        val detail = draftBuild("queued", "queued", "/queue/1", ",\"number\":\"098\",\"buildType\":{\"id\":\"Android\",\"name\":\"Android build\"},\"snapshot-dependencies\":{\"href\":\"/snapshots\",\"count\":3}")
        doReturn(Single.just(detail)).whenever(service).build("/queue/1")
        val result = adapter(session, StandardTestDispatcher(testScheduler), service).queuedBuild("/queue/1")
        assertEquals("098", result.number)
        assertEquals("Android", result.configuration?.id)
        assertEquals("/snapshots", result.snapshotDependencies?.href)
        assertEquals(3, result.snapshotDependencies?.count)
        verify(service).build("/queue/1")
        verifyNoMoreInteractions(service)
        verifyNoInteractions(session)
    }

    @Test fun cancellationDisposesQueuedShowServiceRequest() = runTest {
        val session = mock<Repository>()
        val service = mock<TeamCityService>()
        val pending = SingleSubject.create<Build>()
        doReturn(pending).whenever(service).build("/queue/1")
        val request = async { adapter(session, StandardTestDispatcher(testScheduler), service).queuedBuild("/queue/1") }
        runCurrent()
        request.cancelAndJoin()
        assertFalse(pending.hasObservers())
        verifyNoInteractions(session)
    }

    @Test fun favoriteMembershipReadsTheCurrentAccountEachTime() = runTest {
        val session = mock<Repository>()
        val storage = mock<SharedUserStorage>()
        doReturn(listOf("Android"), listOf("Other")).whenever(storage).favoriteBuildTypeIds
        val adapter = adapter(session, StandardTestDispatcher(testScheduler), storage = storage)
        assertTrue(adapter.isFavorite("Android"))
        assertFalse(adapter.isFavorite("Android"))
        verify(storage, times(2)).favoriteBuildTypeIds
        verifyNoMoreInteractions(storage)
        verifyNoInteractions(session)
    }

    @Test fun favoriteWritesKeepExistingActiveAccountStorageMethods() = runTest {
        val session = mock<Repository>()
        val storage = mock<SharedUserStorage>()
        val adapter = adapter(session, StandardTestDispatcher(testScheduler), storage = storage)
        adapter.setFavorite("Android", true)
        adapter.setFavorite("Android", false)
        verify(storage).addBuildTypeToFavorites("Android")
        verify(storage).removeBuildTypeFromFavorites("Android")
        verifyNoMoreInteractions(storage)
        verifyNoInteractions(session)
    }

    @Test fun cancelledFavoriteWriteDoesNotChangeStorage() = runTest {
        val session = mock<Repository>()
        val storage = mock<SharedUserStorage>()
        val adapter = adapter(session, StandardTestDispatcher(testScheduler), storage = storage)
        val request = async { adapter.setFavorite("Android", true) }
        request.cancelAndJoin()
        verifyNoInteractions(storage, session)
    }
}
