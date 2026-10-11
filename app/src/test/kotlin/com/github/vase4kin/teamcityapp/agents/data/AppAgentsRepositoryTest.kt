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

package com.github.vase4kin.teamcityapp.agents.data

import android.app.Application
import com.github.vase4kin.teamcityapp.agents.api.Agent as LegacyAgent
import com.github.vase4kin.teamcityapp.agents.api.Agents
import com.github.vase4kin.teamcityapp.api.Repository
import com.github.vase4kin.teamcityapp.filter_bottom_sheet_dialog.filter.Filter
import com.github.vase4kin.teamcityapp.filter_bottom_sheet_dialog.filter.FilterProvider
import com.github.vase4kin.teamcityapp.home.data.HomeDataManager
import io.reactivex.Single
import javax.inject.Provider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.*
import org.greenrobot.eventbus.EventBus
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import teamcityapp.features.agents.api.Agent
import teamcityapp.features.agents.api.AgentsFilter

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], manifest = Config.NONE, application = Application::class)
class AppAgentsRepositoryTest {
    @Test fun mapsRowsAndPreservesConnectedAndDisconnectedRequestPolicies() = runTest {
        val legacy = mock<Repository>()
        val rows = listOf(LegacyAgent("Linux").apply { id = "42" }, LegacyAgent("Mac"), LegacyAgent("Mac"))
        whenever(legacy.listAgents(false, null, null, false)).thenReturn(Single.just(Agents(3, rows)))
        whenever(legacy.listAgents(true, null, null, true)).thenReturn(Single.just(Agents(0, emptyList())))
        val repository = AppAgentsRepository(Provider { legacy }, FilterProvider(), EventBus(), StandardTestDispatcher(testScheduler))
        assertEquals(listOf(Agent("42", "Linux"), Agent("name:Mac:0", "Mac"), Agent("name:Mac:1", "Mac")), repository.agents(AgentsFilter.Connected, false))
        assertTrue(repository.agents(AgentsFilter.Disconnected, true).isEmpty())
        verify(legacy).listAgents(false, null, null, false)
        verify(legacy).listAgents(true, null, null, true)
    }

    @Test fun cancellationDisposesTheRxSubscription() = runTest {
        val legacy = mock<Repository>()
        var disposed = false
        whenever(legacy.listAgents(false, null, null, false)).thenReturn(Single.never<Agents>().doOnDispose { disposed = true })
        val repository = AppAgentsRepository(Provider { legacy }, FilterProvider(), EventBus(), StandardTestDispatcher(testScheduler))
        val pending = async { repository.agents(AgentsFilter.Connected, false) }
        runCurrent()
        pending.cancel()
        runCurrent()
        assertTrue(disposed)
    }

    @Test fun filterObservationUsesLatestProviderAndUnregistersWhenCollectionEnds() = runTest {
        val provider = FilterProvider()
        val bus = EventBus()
        val repository = AppAgentsRepository(Provider { mock<Repository>() }, provider, bus, StandardTestDispatcher(testScheduler))
        val observed = async { repository.filter.take(2).toList() }
        runCurrent()
        assertTrue(bus.hasSubscriberForEvent(HomeDataManager.AgentsFilterChangedEvent::class.java))
        bus.post(HomeDataManager.AgentsFilterChangedEvent())
        runCurrent()
        provider.agentsFilter = Filter.AGENTS_DISCONNECTED
        bus.post(HomeDataManager.AgentsFilterChangedEvent())
        assertEquals(listOf(AgentsFilter.Connected, AgentsFilter.Disconnected), observed.await())
        assertFalse(bus.hasSubscriberForEvent(HomeDataManager.AgentsFilterChangedEvent::class.java))
        assertEquals(listOf(AgentsFilter.Disconnected), repository.filter.take(1).toList())
    }

    @Test fun cancellingFilterObservationUnregistersItsSubscriber() = runTest {
        val bus = EventBus()
        val repository = AppAgentsRepository(Provider { mock<Repository>() }, FilterProvider(), bus, StandardTestDispatcher(testScheduler))
        val pending = async { repository.filter.toList() }
        runCurrent()
        pending.cancel()
        runCurrent()
        assertFalse(bus.hasSubscriberForEvent(HomeDataManager.AgentsFilterChangedEvent::class.java))
    }

    @Test fun resolvesTheCurrentAccountRepositoryForEveryRequest() = runTest {
        val first = mock<Repository>()
        val second = mock<Repository>()
        whenever(first.listAgents(false, null, null, false)).thenReturn(Single.just(Agents(0, emptyList())))
        whenever(second.listAgents(false, null, null, false)).thenReturn(Single.just(Agents(0, emptyList())))
        var current = first
        val repository = AppAgentsRepository(Provider { current }, FilterProvider(), EventBus(), StandardTestDispatcher(testScheduler))
        repository.agents(AgentsFilter.Connected, false)
        current = second
        repository.agents(AgentsFilter.Connected, false)
        verify(first).listAgents(false, null, null, false)
        verify(second).listAgents(false, null, null, false)
    }
}
