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

package com.github.vase4kin.teamcityapp.home.data

import android.app.Application
import com.github.vase4kin.teamcityapp.agents.api.Agents
import com.github.vase4kin.teamcityapp.api.Repository
import com.github.vase4kin.teamcityapp.buildlist.api.Builds
import com.github.vase4kin.teamcityapp.integration.features.home.data.AppHomeRepository
import com.github.vase4kin.teamcityapp.integration.filters.filter.FilterProvider
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage
import io.reactivex.Single
import javax.inject.Provider
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.greenrobot.eventbus.EventBus
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import teamcityapp.features.filter_bottom_sheet.api.QuickFilter
import teamcityapp.features.home.api.*
import teamcityapp.libraries.onboarding.OnboardingManager
import teamcityapp.libraries.session.homeBuildAccountKey
import teamcityapp.libraries.storage.models.UserAccount

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], manifest = Config.NONE, application = Application::class)
class AppHomeRepositoryTest {
    private val legacy = mock<Repository>()
    private val storage = mock<SharedUserStorage>()
    private val onboarding = mock<OnboardingManager>()
    private var active = UserAccount("https://same-server", "alice", byteArrayOf(), false, true).apply {
        addBuildType("A")
        addBuildType("B")
    }
    private var providerCalls = 0
    private fun app(scope: TestScope) = AppHomeRepository(
        Provider {
            providerCalls++
            legacy
        },
        storage,
        FilterProvider(),
        EventBus(),
        onboarding,
        StandardTestDispatcher(scope.testScheduler)
    ).also { whenever(storage.activeUser).thenAnswer { active } }
    private fun query() = HomeQuery(active.homeBuildAccountKey(), active.buildTypeIds.toList(), QuickFilter.RunningAll, QuickFilter.QueuedAll, QuickFilter.AgentsConnected)

    @Test fun countsUseOneAccountSessionAndIndependentFailuresPreserveSuccessfulSections() = runTest {
        val app = app(this)
        whenever(legacy.listRunningBuilds(any(), eq("count"), eq(false))).thenReturn(Single.just(Builds(5, emptyList())))
        whenever(legacy.listQueueBuilds(null, "count", false)).thenReturn(Single.error(RuntimeException("offline")))
        whenever(legacy.listAgents(false, "count", null, false)).thenReturn(Single.just(Agents(3, emptyList())))
        assertEquals(HomeCounts(HomeCount.Available(2), HomeCount.Available(5), HomeCount.Unavailable, HomeCount.Available(3)), app.counts(query()))
        assertEquals(1, providerCalls)
    }

    @Test fun favoriteCountsSumAllSavedIdsWithoutMutatingTheirPersistence() = runTest {
        val app = app(this)
        whenever(legacy.listRunningBuilds(any(), eq("count"), eq(false))).thenReturn(Single.just(Builds(2, emptyList())))
        whenever(legacy.listQueueBuilds(any(), eq("count"), eq(false))).thenReturn(Single.just(Builds(3, emptyList())))
        whenever(legacy.listAgents(false, "count", null, false)).thenReturn(Single.just(Agents(0, emptyList())))
        val result = app.counts(query().copy(runningFilter = QuickFilter.RunningFavorites, queueFilter = QuickFilter.QueuedFavorites))
        assertEquals(HomeCount.Available(4), result.running)
        assertEquals(HomeCount.Available(6), result.queue)
        assertEquals(listOf("A", "B"), active.buildTypeIds)
        verify(storage, never()).removeBuildTypeFromFavorites(any())
    }

    @Test fun hidingDisposesEveryPendingRxCountRequest() = runTest {
        var disposed = 0
        val app = app(this)
        whenever(legacy.listRunningBuilds(any(), eq("count"), eq(false))).thenReturn(Single.never<Builds>().doOnDispose { disposed++ })
        whenever(legacy.listQueueBuilds(null, "count", false)).thenReturn(Single.never<Builds>().doOnDispose { disposed++ })
        whenever(legacy.listAgents(false, "count", null, false)).thenReturn(Single.never<Agents>().doOnDispose { disposed++ })
        val pending = async { app.counts(query()) }
        runCurrent()
        pending.cancel()
        runCurrent()
        assertEquals(3, disposed)
    }

    @Test fun anOldSameServerQueryIsRejectedBeforeResolvingTheNewAccountsApi() = runTest {
        val app = app(this)
        val previous = query()
        active = UserAccount("https://same-server", "bob", byteArrayOf(), false, true)
        try {
            app.counts(previous)
            fail("old query accepted")
        } catch (_: CancellationException) { }
        assertEquals(0, providerCalls)
        verifyNoInteractions(legacy)
    }

    @Test fun initialQueryCapturesAccountIdentityAndFavoritesTogether() = runTest {
        val app = app(this)
        val query = app.query.first()
        assertEquals(active.homeBuildAccountKey(), query.accountKey)
        assertEquals(HomeAccount(active.userName, active.teamcityUrl), query.account)
        assertEquals(listOf("A", "B"), query.favoriteIds)
        assertEquals(QuickFilter.RunningFavorites, query.runningFilter)
    }
}
