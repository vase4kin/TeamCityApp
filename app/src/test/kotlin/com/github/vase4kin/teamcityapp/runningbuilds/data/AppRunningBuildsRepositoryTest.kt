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

package com.github.vase4kin.teamcityapp.runningbuilds.data

import android.app.Application
import com.github.vase4kin.teamcityapp.api.Repository
import com.github.vase4kin.teamcityapp.buildlist.api.Build
import com.github.vase4kin.teamcityapp.buildlist.api.Builds
import com.github.vase4kin.teamcityapp.builds.data.AppBuildHydrator
import com.github.vase4kin.teamcityapp.builds.data.AppBuildLaunchMapper
import com.github.vase4kin.teamcityapp.builds.data.homeBuildAccountKey
import com.github.vase4kin.teamcityapp.filter_bottom_sheet_dialog.filter.Filter
import com.github.vase4kin.teamcityapp.filter_bottom_sheet_dialog.filter.FilterProvider
import com.github.vase4kin.teamcityapp.home.data.HomeDataManager
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage
import com.google.gson.Gson
import io.reactivex.Single
import io.reactivex.subjects.SingleSubject
import javax.inject.Provider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.test.*
import org.greenrobot.eventbus.EventBus
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import teamcityapp.features.running_builds.api.RunningBuildsFilter
import teamcityapp.features.running_builds.api.RunningBuildsQuery
import teamcityapp.libraries.storage.models.UserAccount

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], manifest = Config.NONE, application = Application::class)
class AppRunningBuildsRepositoryTest {
    private val legacy = mock<Repository>()
    private val storage = mock<SharedUserStorage>()
    private val filters = FilterProvider()
    private val bus = EventBus()
    private var active = account("alice", mutableListOf("A", "B"))
    private var providerCalls = 0
    private var current = legacy

    private fun app(scope: TestScope) = AppRunningBuildsRepository(
        Provider {
            providerCalls++
            current
        },
        storage,
        filters,
        bus,
        AppBuildHydrator(AppBuildLaunchMapper()),
        StandardTestDispatcher(scope.testScheduler)
    ).also {
        whenever(storage.activeUser).thenAnswer { active }
    }
    private fun allQuery() = RunningBuildsQuery(active.homeBuildAccountKey(), RunningBuildsFilter.All)
    private fun favoritesQuery(ids: List<String> = active.buildTypeIds) = RunningBuildsQuery(active.homeBuildAccountKey(), RunningBuildsFilter.Favorites, ids)

    @Test fun allUsesExactEndpointLocatorAndListRefreshFlagWithOneSession() = runTest {
        val app = app(this)
        val row = build("1")
        whenever(legacy.listRunningBuilds("running:true,branch:default:any,personal:false,pinned:false", null, false)).thenReturn(Single.just(Builds(1, listOf(row))))
        whenever(legacy.listRunningBuilds("running:true,branch:default:any,personal:false,pinned:false", null, true)).thenReturn(Single.just(Builds(1, listOf(row))))
        whenever(legacy.build("/builds/1", false)).thenReturn(Single.just(row))
        assertEquals(listOf("1"), app.builds(allQuery(), false).map { it.id })
        assertEquals(listOf("1"), app.builds(allQuery(), true).map { it.id })
        assertEquals(2, providerCalls)
        verify(legacy).listRunningBuilds("running:true,branch:default:any,personal:false,pinned:false", null, false)
        verify(legacy).listRunningBuilds("running:true,branch:default:any,personal:false,pinned:false", null, true)
        verify(legacy, times(2)).build("/builds/1", false)
    }

    @Test fun emptyFavoritesUsesNoSessionOrEndpointAndNeverMutatesSavedIds() = runTest {
        val app = app(this)
        assertTrue(app.builds(favoritesQuery(emptyList()), true).isEmpty())
        assertEquals(0, providerCalls)
        verifyNoInteractions(legacy)
        verify(storage, never()).removeBuildTypeFromFavorites(any())
        verify(storage, never()).addBuildTypeToFavorites(any())
    }

    @Test fun favoriteRequestsAreParallelAndRetainSavedIdAndServerOrderDespiteCompletionOrder() = runTest {
        val app = app(this)
        val first = SingleSubject.create<Builds>()
        val second = SingleSubject.create<Builds>()
        whenever(legacy.listRunningBuilds("running:true,branch:default:any,personal:false,pinned:false,buildType:A", null, true)).thenReturn(first)
        whenever(legacy.listRunningBuilds("running:true,branch:default:any,personal:false,pinned:false,buildType:B", null, true)).thenReturn(second)
        listOf("1", "2", "3").forEach { id -> whenever(legacy.build("/builds/$id", false)).thenReturn(Single.just(build(id))) }
        val pending = async { app.builds(favoritesQuery(), true) }
        runCurrent()
        assertTrue(first.hasObservers())
        assertTrue(second.hasObservers())
        second.onSuccess(Builds(1, listOf(build("3"))))
        first.onSuccess(Builds(2, listOf(build("2"), build("1"))))
        assertEquals(listOf("2", "1", "3"), pending.await().map { it.id })
        assertEquals(1, providerCalls)
    }

    @Test fun favoritesCopyInputIdsBeforeSuspendingAndNormalReturnAllowsCache() = runTest {
        val app = app(this)
        val ids = mutableListOf("A", "B")
        val first = SingleSubject.create<Builds>()
        val second = SingleSubject.create<Builds>()
        whenever(legacy.listRunningBuilds("running:true,branch:default:any,personal:false,pinned:false,buildType:A", null, false)).thenReturn(first)
        whenever(legacy.listRunningBuilds("running:true,branch:default:any,personal:false,pinned:false,buildType:B", null, false)).thenReturn(second)
        val pending = async { app.builds(favoritesQuery(ids), false) }
        runCurrent()
        ids.clear()
        first.onSuccess(Builds(0, emptyList()))
        second.onSuccess(Builds(0, emptyList()))
        assertTrue(pending.await().isEmpty())
        verify(legacy).listRunningBuilds("running:true,branch:default:any,personal:false,pinned:false,buildType:A", null, false)
        verify(legacy).listRunningBuilds("running:true,branch:default:any,personal:false,pinned:false,buildType:B", null, false)
    }

    @Test fun cancellingFavoriteBatchDisposesEveryPendingListSubscription() = runTest {
        val app = app(this)
        var disposed = 0
        whenever(legacy.listRunningBuilds("running:true,branch:default:any,personal:false,pinned:false,buildType:A", null, false)).thenReturn(Single.never<Builds>().doOnDispose { disposed++ })
        whenever(legacy.listRunningBuilds("running:true,branch:default:any,personal:false,pinned:false,buildType:B", null, false)).thenReturn(Single.never<Builds>().doOnDispose { disposed++ })
        val pending = async { app.builds(favoritesQuery(), false) }
        runCurrent()
        pending.cancel()
        runCurrent()
        assertEquals(2, disposed)
    }

    @Test fun oneFavoriteEndpointFailureCancelsOthersAndKeepsStorageUntouched() = runTest {
        val app = app(this)
        val failed = SingleSubject.create<Builds>()
        var disposed = false
        whenever(legacy.listRunningBuilds("running:true,branch:default:any,personal:false,pinned:false,buildType:A", null, false)).thenReturn(failed)
        whenever(legacy.listRunningBuilds("running:true,branch:default:any,personal:false,pinned:false,buildType:B", null, false)).thenReturn(Single.never<Builds>().doOnDispose { disposed = true })
        supervisorScope {
            val pending = async { app.builds(favoritesQuery(), false) }
            runCurrent()
            val expected = IllegalStateException("offline")
            failed.onError(expected)
            val actual = runCatching { pending.await() }.exceptionOrNull()
            assertEquals(expected::class, actual?.let { it::class })
            assertEquals(expected.message, actual?.message)
        }
        assertTrue(disposed)
        assertEquals(listOf("A", "B"), active.buildTypeIds)
        verify(storage, never()).removeBuildTypeFromFavorites(any())
    }

    @Test fun runningDetailsUseFreshHydrationAndRetainTheCompleteLaunchSnapshot() = runTest {
        val app = app(this)
        val row = build("1", "running")
        val detail = Gson().fromJson("""{"id":"1","href":"/builds/1","state":"running","buildTypeId":"top","buildType":{"id":"nested","name":"Config"},"properties":{"property":[]},"testOccurrences":{"href":"/tests","count":2,"failed":2}}""", Build::class.java)
        whenever(legacy.listRunningBuilds("running:true,branch:default:any,personal:false,pinned:false", null, false)).thenReturn(Single.just(Builds(1, listOf(row))))
        whenever(legacy.build("/builds/1", true)).thenReturn(Single.just(detail))
        val loaded = app.builds(allQuery(), false).single()
        assertEquals("top", loaded.buildTypeId)
        assertEquals("nested", loaded.configuration?.id)
        assertNotNull(loaded.properties)
        assertEquals(2, loaded.tests?.count)
        verify(legacy).build("/builds/1", true)
        verify(legacy, never()).build("/builds/1", false)
    }

    @Test fun staleSameServerOtherUserQueryIsRejectedBeforeResolvingAnySession() = runTest {
        val app = app(this)
        val stale = allQuery()
        active = account("bob")
        assertTrue(runCatching { app.builds(stale, false) }.exceptionOrNull() is CancellationException)
        assertEquals(0, providerCalls)
        verifyNoInteractions(legacy)
    }

    @Test fun accountChangeInsideProviderResolutionIsRejectedBeforeEndpoint() = runTest {
        whenever(storage.activeUser).thenAnswer { active }
        val query = allQuery()
        val app = AppRunningBuildsRepository(
            Provider {
                providerCalls++
                active = account("bob")
                legacy
            },
            storage,
            filters,
            bus,
            AppBuildHydrator(AppBuildLaunchMapper()),
            StandardTestDispatcher(testScheduler)
        )
        assertTrue(runCatching { app.builds(query, false) }.exceptionOrNull() is CancellationException)
        assertEquals(1, providerCalls)
        verifyNoInteractions(legacy)
    }

    @Test fun lateOldAccountSummaryCannotStartDetailRequests() = runTest {
        val app = app(this)
        val response = SingleSubject.create<Builds>()
        whenever(legacy.listRunningBuilds("running:true,branch:default:any,personal:false,pinned:false", null, false)).thenReturn(response)
        val query = allQuery()
        val pending = async { app.builds(query, false) }
        runCurrent()
        active = account("bob")
        response.onSuccess(Builds(1, listOf(build("1"))))
        assertTrue(runCatching { pending.await() }.exceptionOrNull() is CancellationException)
        verify(legacy, never()).build(any(), any())
    }

    @Test fun lateOldAccountDetailCannotPublishContent() = runTest {
        val app = app(this)
        val response = SingleSubject.create<Build>()
        whenever(legacy.listRunningBuilds("running:true,branch:default:any,personal:false,pinned:false", null, false)).thenReturn(Single.just(Builds(1, listOf(build("1")))))
        whenever(legacy.build("/builds/1", false)).thenReturn(response)
        val query = allQuery()
        val pending = async { app.builds(query, false) }
        runCurrent()
        active = account("bob")
        response.onSuccess(build("1"))
        assertTrue(runCatching { pending.await() }.exceptionOrNull() is CancellationException)
    }

    @Test fun eachBatchCapturesOneRepositoryAndNextAccountBatchResolvesNewSession() = runTest {
        val app = app(this)
        val next = mock<Repository>()
        val response = SingleSubject.create<Builds>()
        whenever(legacy.listRunningBuilds("running:true,branch:default:any,personal:false,pinned:false", null, false)).thenReturn(response)
        whenever(legacy.build("/builds/1", false)).thenReturn(Single.just(build("1")))
        whenever(next.listRunningBuilds("running:true,branch:default:any,personal:false,pinned:false", null, false)).thenReturn(Single.just(Builds(0, emptyList())))
        val pending = async { app.builds(allQuery(), false) }
        runCurrent()
        current = next
        response.onSuccess(Builds(1, listOf(build("1"))))
        assertEquals(listOf("1"), pending.await().map { it.id })
        verify(legacy).build("/builds/1", false)
        active = account("bob")
        assertTrue(app.builds(allQuery(), false).isEmpty())
        assertEquals(2, providerCalls)
        verify(next).listRunningBuilds("running:true,branch:default:any,personal:false,pinned:false", null, false)
    }

    @Test fun queryObservesRetainedFilterFavoritesAccountAndUnregistersAfterCollection() = runTest {
        val app = app(this)
        val observations = async { app.query.take(3).toList() }
        runCurrent()
        assertTrue(bus.hasSubscriberForEvent(HomeDataManager.RunningBuildsFilterChangedEvent::class.java))
        bus.post(HomeDataManager.RunningBuildsFilterChangedEvent())
        runCurrent()
        filters.runningBuildsFilter = Filter.RUNNING_ALL
        bus.post(HomeDataManager.RunningBuildsFilterChangedEvent())
        runCurrent()
        active = account("bob", listOf("C"))
        bus.post(HomeDataManager.RunningBuildsFilterChangedEvent())
        val observed = observations.await()
        assertEquals(RunningBuildsFilter.Favorites, observed[0].filter)
        assertEquals(listOf("A", "B"), observed[0].favoriteConfigurationIds)
        assertEquals(RunningBuildsFilter.All, observed[1].filter)
        assertTrue(observed[1].favoriteConfigurationIds.isEmpty())
        assertTrue(observed[2].favoriteConfigurationIds.isEmpty())
        assertNotEquals(observed[1].accountKey, observed[2].accountKey)
        assertFalse(bus.hasSubscriberForEvent(HomeDataManager.RunningBuildsFilterChangedEvent::class.java))
    }

    @Test fun resubscriptionReadsLatestFavoritesAndPreviousQueriesAreImmutableSnapshots() = runTest {
        val app = app(this)
        val old = app.query.first()
        active.buildTypeIds.clear()
        active.addBuildType("C")
        val latest = app.query.first()
        assertEquals(listOf("A", "B"), old.favoriteConfigurationIds)
        assertEquals(listOf("C"), latest.favoriteConfigurationIds)
        assertEquals(old.accountKey, latest.accountKey)
    }

    @Test fun cancellingQueryObservationUnregistersItsSubscriber() = runTest {
        val app = app(this)
        val pending = async { app.query.toList() }
        runCurrent()
        pending.cancel()
        runCurrent()
        assertFalse(bus.hasSubscriberForEvent(HomeDataManager.RunningBuildsFilterChangedEvent::class.java))
    }

    @Test fun accountQuerySwitchCancelsOldRxBatchAndOnlyPublishesTheNewAccountsResult() = runTest {
        val app = app(this)
        val alice = account("alice", listOf("A"))
        active = alice
        val oldResponse = SingleSubject.create<Builds>()
        val next = mock<Repository>()
        whenever(legacy.listRunningBuilds("running:true,branch:default:any,personal:false,pinned:false,buildType:A", null, false)).thenReturn(oldResponse)
        whenever(next.listRunningBuilds("running:true,branch:default:any,personal:false,pinned:false,buildType:C", null, false)).thenReturn(Single.just(Builds(0, emptyList())))
        val emitted = mutableListOf<List<teamcityapp.libraries.builds.BuildLaunchData>>()
        val collecting = backgroundScope.launch { app.query.collectLatest { emitted += app.builds(it, false) } }
        runCurrent()
        assertTrue(oldResponse.hasObservers())
        active = account("bob", listOf("C"))
        current = next
        bus.post(HomeDataManager.RunningBuildsFilterChangedEvent())
        runCurrent()
        assertFalse(oldResponse.hasObservers())
        oldResponse.onSuccess(Builds(1, listOf(build("old"))))
        runCurrent()
        assertEquals(listOf(emptyList<teamcityapp.libraries.builds.BuildLaunchData>()), emitted)
        assertEquals(listOf("A"), alice.buildTypeIds)
        assertEquals(listOf("C"), active.buildTypeIds)
        assertEquals(2, providerCalls)
        verify(legacy, never()).build(any(), any())
        collecting.cancel()
        runCurrent()
        assertFalse(bus.hasSubscriberForEvent(HomeDataManager.RunningBuildsFilterChangedEvent::class.java))
    }

    @Test fun zeroCountIsAuthoritativeEvenWhenTheResponseContainsRows() = runTest {
        val app = app(this)
        whenever(legacy.listRunningBuilds("running:true,branch:default:any,personal:false,pinned:false", null, false)).thenReturn(Single.just(Builds(0, listOf(build("1")))))
        assertTrue(app.builds(allQuery(), false).isEmpty())
        verify(legacy, never()).build(any(), any())
    }

    private fun account(name: String, ids: List<String> = emptyList()) = UserAccount("https://same-server", name, byteArrayOf(), false, true).apply { buildTypeIds = ids }
    private fun build(id: String, state: String = "finished") = Gson().fromJson("""{"id":"$id","href":"/builds/$id","state":"$state"}""", Build::class.java)
}
