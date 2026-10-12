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
import com.github.vase4kin.teamcityapp.api.RepositoryImpl
import com.github.vase4kin.teamcityapp.api.TeamCityService
import com.github.vase4kin.teamcityapp.api.cache.SharedRxCache
import com.github.vase4kin.teamcityapp.buildlist.api.Build
import com.github.vase4kin.teamcityapp.buildlist.api.Builds
import com.github.vase4kin.teamcityapp.builds.data.completeBuildJson
import com.github.vase4kin.teamcityapp.builds.data.completeLaunch
import com.github.vase4kin.teamcityapp.integration.features.home.data.HomeFilterEvents
import com.github.vase4kin.teamcityapp.integration.features.runningbuilds.data.AppRunningBuildsRepository
import com.github.vase4kin.teamcityapp.integration.filters.filter.Filter
import com.github.vase4kin.teamcityapp.integration.filters.filter.FilterProvider
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import io.reactivex.Single
import io.reactivex.subjects.SingleSubject
import io.victoralbertos.jolyglot.GsonSpeaker
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
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.mockito.kotlin.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import teamcityapp.features.build_overview.impl.data.DefaultBuildOverviewRepository
import teamcityapp.features.running_builds.api.RunningBuildsFilter
import teamcityapp.features.running_builds.api.RunningBuildsQuery
import teamcityapp.libraries.data.builds.BUILD_LAUNCH_FIELDS
import teamcityapp.libraries.data.builds.BuildLaunchMapper
import teamcityapp.libraries.remote.url.UrlFormatter
import teamcityapp.libraries.session.homeBuildAccountKey
import teamcityapp.libraries.storage.models.UserAccount

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], manifest = Config.NONE, application = Application::class)
class AppRunningBuildsRepositoryTest {
    @get:Rule val folder = TemporaryFolder()
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
        BuildLaunchMapper(),
        StandardTestDispatcher(scope.testScheduler)
    ).also {
        whenever(storage.activeUser).thenAnswer { active }
    }
    private fun allQuery() = RunningBuildsQuery(active.homeBuildAccountKey(), RunningBuildsFilter.All)
    private fun favoritesQuery(ids: List<String> = active.buildTypeIds) = RunningBuildsQuery(active.homeBuildAccountKey(), RunningBuildsFilter.Favorites, ids)

    @Test fun allUsesTheBulkProjectionAndListRefreshFlagWithoutDetailRequests() = runTest {
        val app = app(this)
        val row = build("1")
        whenever(legacy.listRunningBuilds("running:true,branch:default:any,personal:false,pinned:false", BUILD_LAUNCH_FIELDS, false)).thenReturn(Single.just(Builds(1, listOf(row))))
        whenever(legacy.listRunningBuilds("running:true,branch:default:any,personal:false,pinned:false", BUILD_LAUNCH_FIELDS, true)).thenReturn(Single.just(Builds(1, listOf(row))))
        assertEquals(listOf("1"), app.builds(allQuery(), false).map { it.id })
        assertEquals(listOf("1"), app.builds(allQuery(), true).map { it.id })
        assertEquals(2, providerCalls)
        verify(legacy).listRunningBuilds("running:true,branch:default:any,personal:false,pinned:false", BUILD_LAUNCH_FIELDS, false)
        verify(legacy).listRunningBuilds("running:true,branch:default:any,personal:false,pinned:false", BUILD_LAUNCH_FIELDS, true)
        verifyNoMoreInteractions(legacy)
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
        whenever(legacy.listRunningBuilds("running:true,branch:default:any,personal:false,pinned:false,buildType:A", BUILD_LAUNCH_FIELDS, true)).thenReturn(first)
        whenever(legacy.listRunningBuilds("running:true,branch:default:any,personal:false,pinned:false,buildType:B", BUILD_LAUNCH_FIELDS, true)).thenReturn(second)
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
        whenever(legacy.listRunningBuilds("running:true,branch:default:any,personal:false,pinned:false,buildType:A", BUILD_LAUNCH_FIELDS, false)).thenReturn(first)
        whenever(legacy.listRunningBuilds("running:true,branch:default:any,personal:false,pinned:false,buildType:B", BUILD_LAUNCH_FIELDS, false)).thenReturn(second)
        val pending = async { app.builds(favoritesQuery(ids), false) }
        runCurrent()
        ids.clear()
        first.onSuccess(Builds(0, emptyList()))
        second.onSuccess(Builds(0, emptyList()))
        assertTrue(pending.await().isEmpty())
        verify(legacy).listRunningBuilds("running:true,branch:default:any,personal:false,pinned:false,buildType:A", BUILD_LAUNCH_FIELDS, false)
        verify(legacy).listRunningBuilds("running:true,branch:default:any,personal:false,pinned:false,buildType:B", BUILD_LAUNCH_FIELDS, false)
    }

    @Test fun cancellingFavoriteBatchDisposesEveryPendingListSubscription() = runTest {
        val app = app(this)
        var disposed = 0
        whenever(legacy.listRunningBuilds("running:true,branch:default:any,personal:false,pinned:false,buildType:A", BUILD_LAUNCH_FIELDS, false)).thenReturn(Single.never<Builds>().doOnDispose { disposed++ })
        whenever(legacy.listRunningBuilds("running:true,branch:default:any,personal:false,pinned:false,buildType:B", BUILD_LAUNCH_FIELDS, false)).thenReturn(Single.never<Builds>().doOnDispose { disposed++ })
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
        whenever(legacy.listRunningBuilds("running:true,branch:default:any,personal:false,pinned:false,buildType:A", BUILD_LAUNCH_FIELDS, false)).thenReturn(failed)
        whenever(legacy.listRunningBuilds("running:true,branch:default:any,personal:false,pinned:false,buildType:B", BUILD_LAUNCH_FIELDS, false)).thenReturn(Single.never<Builds>().doOnDispose { disposed = true })
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

    @Test fun projectedBulkResponseRetainsEveryBuildDetailsLaunchField() = runTest {
        val app = app(this)
        // Emulate the server omitting every field that the request did not select.
        // This catches missing nested fields as well as missing top-level metadata.
        val source = JsonParser.parseString("""{"count":1,"build":[$completeBuildJson]}""").asJsonObject
        whenever(legacy.listRunningBuilds(eq("running:true,branch:default:any,personal:false,pinned:false"), any(), eq(false))).thenAnswer { request ->
            val fields = request.getArgument<String>(1)
            val selected = selectFields(source, fields)
            Single.just(Gson().fromJson(selected, Builds::class.java))
        }
        assertEquals(listOf(completeLaunch()), app.builds(allQuery(), false))
        verify(legacy).listRunningBuilds("running:true,branch:default:any,personal:false,pinned:false", BUILD_LAUNCH_FIELDS, false)
        verifyNoMoreInteractions(legacy)
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
            BuildLaunchMapper(),
            StandardTestDispatcher(testScheduler)
        )
        assertTrue(runCatching { app.builds(query, false) }.exceptionOrNull() is CancellationException)
        assertEquals(1, providerCalls)
        verifyNoInteractions(legacy)
    }

    @Test fun lateOldAccountBulkResponseCannotPublishContent() = runTest {
        val app = app(this)
        val response = SingleSubject.create<Builds>()
        whenever(legacy.listRunningBuilds("running:true,branch:default:any,personal:false,pinned:false", BUILD_LAUNCH_FIELDS, false)).thenReturn(response)
        val query = allQuery()
        val pending = async { app.builds(query, false) }
        runCurrent()
        active = account("bob")
        response.onSuccess(Builds(1, listOf(build("1"))))
        assertTrue(runCatching { pending.await() }.exceptionOrNull() is CancellationException)
        verify(legacy, never()).build(any(), any())
    }

    @Test fun cancellingAllBuildsDisposesThePendingBulkSubscription() = runTest {
        val app = app(this)
        val response = SingleSubject.create<Builds>()
        whenever(legacy.listRunningBuilds("running:true,branch:default:any,personal:false,pinned:false", BUILD_LAUNCH_FIELDS, false)).thenReturn(response)
        val pending = async { app.builds(allQuery(), false) }
        runCurrent()
        assertTrue(response.hasObservers())
        pending.cancel()
        runCurrent()
        assertFalse(response.hasObservers())
        response.onSuccess(Builds(1, listOf(build("late"))))
        assertTrue(pending.isCancelled)
        verify(legacy, never()).build(any(), any())
    }

    @Test fun eachBatchCapturesOneRepositoryAndNextAccountBatchResolvesNewSession() = runTest {
        val app = app(this)
        val next = mock<Repository>()
        val response = SingleSubject.create<Builds>()
        whenever(legacy.listRunningBuilds("running:true,branch:default:any,personal:false,pinned:false", BUILD_LAUNCH_FIELDS, false)).thenReturn(response)
        whenever(next.listRunningBuilds("running:true,branch:default:any,personal:false,pinned:false", BUILD_LAUNCH_FIELDS, false)).thenReturn(Single.just(Builds(0, emptyList())))
        val pending = async { app.builds(allQuery(), false) }
        runCurrent()
        current = next
        response.onSuccess(Builds(1, listOf(build("1"))))
        assertEquals(listOf("1"), pending.await().map { it.id })
        verify(legacy).listRunningBuilds("running:true,branch:default:any,personal:false,pinned:false", BUILD_LAUNCH_FIELDS, false)
        verifyNoMoreInteractions(legacy)
        active = account("bob")
        assertTrue(app.builds(allQuery(), false).isEmpty())
        assertEquals(2, providerCalls)
        verify(next).listRunningBuilds("running:true,branch:default:any,personal:false,pinned:false", BUILD_LAUNCH_FIELDS, false)
    }

    @Test fun queryObservesRetainedFilterFavoritesAccountAndUnregistersAfterCollection() = runTest {
        val app = app(this)
        val observations = async { app.query.take(3).toList() }
        runCurrent()
        assertTrue(bus.hasSubscriberForEvent(HomeFilterEvents.RunningBuildsFilterChangedEvent::class.java))
        bus.post(HomeFilterEvents.RunningBuildsFilterChangedEvent())
        runCurrent()
        filters.runningBuildsFilter = Filter.RUNNING_ALL
        bus.post(HomeFilterEvents.RunningBuildsFilterChangedEvent())
        runCurrent()
        active = account("bob", listOf("C"))
        bus.post(HomeFilterEvents.RunningBuildsFilterChangedEvent())
        val observed = observations.await()
        assertEquals(RunningBuildsFilter.Favorites, observed[0].filter)
        assertEquals(listOf("A", "B"), observed[0].favoriteConfigurationIds)
        assertEquals(RunningBuildsFilter.All, observed[1].filter)
        assertTrue(observed[1].favoriteConfigurationIds.isEmpty())
        assertTrue(observed[2].favoriteConfigurationIds.isEmpty())
        assertNotEquals(observed[1].accountKey, observed[2].accountKey)
        assertFalse(bus.hasSubscriberForEvent(HomeFilterEvents.RunningBuildsFilterChangedEvent::class.java))
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
        assertFalse(bus.hasSubscriberForEvent(HomeFilterEvents.RunningBuildsFilterChangedEvent::class.java))
    }

    @Test fun accountQuerySwitchCancelsOldRxBatchAndOnlyPublishesTheNewAccountsResult() = runTest {
        val app = app(this)
        val alice = account("alice", listOf("A"))
        active = alice
        val oldResponse = SingleSubject.create<Builds>()
        val next = mock<Repository>()
        whenever(legacy.listRunningBuilds("running:true,branch:default:any,personal:false,pinned:false,buildType:A", BUILD_LAUNCH_FIELDS, false)).thenReturn(oldResponse)
        whenever(next.listRunningBuilds("running:true,branch:default:any,personal:false,pinned:false,buildType:C", BUILD_LAUNCH_FIELDS, false)).thenReturn(Single.just(Builds(0, emptyList())))
        val emitted = mutableListOf<List<teamcityapp.libraries.builds.BuildLaunchData>>()
        val collecting = backgroundScope.launch { app.query.collectLatest { emitted += app.builds(it, false) } }
        runCurrent()
        assertTrue(oldResponse.hasObservers())
        active = account("bob", listOf("C"))
        current = next
        bus.post(HomeFilterEvents.RunningBuildsFilterChangedEvent())
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
        assertFalse(bus.hasSubscriberForEvent(HomeFilterEvents.RunningBuildsFilterChangedEvent::class.java))
    }

    @Test fun zeroCountIsAuthoritativeEvenWhenTheResponseContainsRows() = runTest {
        val app = app(this)
        whenever(legacy.listRunningBuilds("running:true,branch:default:any,personal:false,pinned:false", BUILD_LAUNCH_FIELDS, false)).thenReturn(Single.just(Builds(0, listOf(build("1")))))
        assertTrue(app.builds(allQuery(), false).isEmpty())
        verify(legacy, never()).build(any(), any())
    }

    @Test fun projectedListsDoNotReuseCachedLegacySummaries() = runTest {
        val service = mock<TeamCityService>()
        val cache = SharedRxCache.create(folder.newFolder(), GsonSpeaker()).providers
        val session = RepositoryImpl(service, cache, mock<UrlFormatter>(), StandardTestDispatcher(testScheduler))
        current = session
        val app = app(this)
        val locator = "running:true,branch:default:any,personal:false,pinned:false"
        whenever(service.listRunningBuilds(locator, null)).thenReturn(Single.just(Builds(1, listOf(build("123")))))
        session.listRunningBuilds(locator, null, false).blockingGet()
        val complete = Gson().fromJson(completeBuildJson, Build::class.java)
        whenever(service.listRunningBuilds(locator, BUILD_LAUNCH_FIELDS)).thenReturn(Single.just(Builds(1, listOf(complete))))
        assertEquals(listOf(completeLaunch()), app.builds(allQuery(), false))
        val overview = DefaultBuildOverviewRepository(Provider { session }, BuildLaunchMapper(), StandardTestDispatcher(testScheduler))
        assertEquals(completeLaunch(), overview.build(complete.href, false))
        verify(service).listRunningBuilds(locator, null)
        verify(service).listRunningBuilds(locator, BUILD_LAUNCH_FIELDS)
        verifyNoMoreInteractions(service)
    }

    @Test fun aHundredRunningBuildsNeedOnlyOneBulkRequest() = runTest {
        val app = app(this)
        val rows = (1..100).map { build("$it") }
        whenever(legacy.listRunningBuilds("running:true,branch:default:any,personal:false,pinned:false", BUILD_LAUNCH_FIELDS, true)).thenReturn(Single.just(Builds(rows.size, rows)))
        val loaded = app.builds(allQuery(), true)
        assertEquals((1..100).map(Int::toString), loaded.map { it.id })
        assertTrue(loaded.all { it.isRunning })
        verify(legacy).listRunningBuilds("running:true,branch:default:any,personal:false,pinned:false", BUILD_LAUNCH_FIELDS, true)
        verifyNoMoreInteractions(legacy)
        assertEquals(1, providerCalls)
    }

    @Test fun favoritesRetainCompleteProjectedPayloadsWithoutDetailRequests() = runTest {
        val app = app(this)
        val complete = Gson().fromJson(completeBuildJson, Build::class.java)
        whenever(legacy.listRunningBuilds("running:true,branch:default:any,personal:false,pinned:false,buildType:A", BUILD_LAUNCH_FIELDS, true)).thenReturn(Single.just(Builds(1, listOf(complete))))
        whenever(legacy.listRunningBuilds("running:true,branch:default:any,personal:false,pinned:false,buildType:B", BUILD_LAUNCH_FIELDS, true)).thenReturn(Single.just(Builds(0, emptyList())))
        assertEquals(listOf(completeLaunch()), app.builds(favoritesQuery(), true))
        verify(legacy).listRunningBuilds("running:true,branch:default:any,personal:false,pinned:false,buildType:A", BUILD_LAUNCH_FIELDS, true)
        verify(legacy).listRunningBuilds("running:true,branch:default:any,personal:false,pinned:false,buildType:B", BUILD_LAUNCH_FIELDS, true)
        verifyNoMoreInteractions(legacy)
    }

    @Test fun bulkPayloadPreservesAbsentEmptyAndPartialSections() = runTest {
        val app = app(this)
        val page = Gson().fromJson("""{"count":2,"build":[{"id":"1","href":"/builds/1","state":"running"},{"id":"2","href":"/builds/2","state":"running","buildType":{},"properties":{"property":[]},"testOccurrences":{"href":"/tests","count":0},"snapshot-dependencies":{}}]}""", Builds::class.java)
        whenever(legacy.listRunningBuilds("running:true,branch:default:any,personal:false,pinned:false", BUILD_LAUNCH_FIELDS, false)).thenReturn(Single.just(page))
        val loaded = app.builds(allQuery(), false)
        assertNull(loaded[0].configuration)
        assertNull(loaded[0].properties)
        assertNull(loaded[0].tests)
        assertNull(loaded[0].snapshotDependencies)
        assertNotNull(loaded[1].configuration)
        assertEquals(emptyList<teamcityapp.libraries.builds.BuildProperty>(), loaded[1].properties?.items)
        assertEquals(0, loaded[1].tests?.count)
        assertNotNull(loaded[1].snapshotDependencies)
        verify(legacy, never()).build(any(), any())
    }

    @Test fun malformedBulkRowFailsWithoutFallingBackToADetailRequest() = runTest {
        val app = app(this)
        val invalid = Gson().fromJson("""{"id":"missing-href","state":"running"}""", Build::class.java)
        whenever(legacy.listRunningBuilds("running:true,branch:default:any,personal:false,pinned:false", BUILD_LAUNCH_FIELDS, false)).thenReturn(Single.just(Builds(2, listOf(build("1"), invalid))))
        assertTrue(runCatching { app.builds(allQuery(), false) }.exceptionOrNull() is IllegalArgumentException)
        verify(legacy, never()).build(any(), any())
    }

    /** Only named fields and nested selections from the documented TeamCity fields grammar. */
    private fun selectFields(source: JsonObject, fields: String): JsonObject {
        val tokens = Regex("[a-zA-Z][a-zA-Z0-9_-]*|[(),]").findAll(fields).map { it.value }.toList()
        var cursor = 0
        fun selections(): List<SelectedField> = buildList {
            while (cursor < tokens.size && tokens[cursor] != ")") {
                val name = tokens[cursor++]
                val nested = if (cursor < tokens.size && tokens[cursor] == "(") {
                    cursor++
                    selections().also { check(tokens[cursor++] == ")") }
                } else {
                    emptyList()
                }
                add(SelectedField(name, nested))
                if (cursor < tokens.size && tokens[cursor] == ",") cursor++
            }
        }
        fun select(value: JsonElement, requested: List<SelectedField>): JsonElement = when {
            value.isJsonArray -> com.google.gson.JsonArray().apply { value.asJsonArray.forEach { add(select(it, requested)) } }

            value.isJsonObject -> JsonObject().apply {
                requested.forEach { field ->
                    value.asJsonObject[field.name]?.let { item ->
                        add(field.name, if (field.nested.isEmpty()) item.deepCopy() else select(item, field.nested))
                    }
                }
            }

            else -> value.deepCopy()
        }
        val requested = selections()
        check(cursor == tokens.size)
        return select(source, requested).asJsonObject
    }

    private data class SelectedField(val name: String, val nested: List<SelectedField>)

    private fun account(name: String, ids: List<String> = emptyList()) = UserAccount("https://same-server", name, byteArrayOf(), false, true).apply { buildTypeIds = ids }
    private fun build(id: String, state: String = "running") = Gson().fromJson("""{"id":"$id","href":"/builds/$id","state":"$state"}""", Build::class.java)
}
