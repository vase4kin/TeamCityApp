/*
 * Copyright 2019 Andrey Tolpeev
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

package com.github.vase4kin.teamcityapp.overview.data

import android.app.Application
import com.github.vase4kin.teamcityapp.build_details.data.OnOverviewRefreshDataEvent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.greenrobot.eventbus.EventBus
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import teamcityapp.features.build_overview.api.BuildOverviewRequest

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
@OptIn(ExperimentalCoroutinesApi::class)
class AppBuildOverviewEventsTest {
    private fun bus() = EventBus.builder().logNoSubscriberMessages(false).sendNoSubscriberEvent(false).build()

    @Test fun coldSubscriptionMapsRefreshAndActionSheetRequestsWithoutLocalizingBranchValues() = runTest {
        val bus = bus()
        val adapter = AppBuildOverviewEvents(bus)
        bus.post(OnOverviewRefreshDataEvent())
        val received = mutableListOf<BuildOverviewRequest>()
        val collector = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { adapter.requests.collect { received.add(it) } }
        bus.post(OnOverviewRefreshDataEvent())
        bus.post(NavigateToBuildListFilteredByBranchEvent("refs/heads/feature:compose"))
        bus.post(NavigateToBuildListEvent())
        bus.post(NavigateToProjectEvent())
        runCurrent()
        assertEquals(listOf(BuildOverviewRequest.Refresh, BuildOverviewRequest.Branch("refs/heads/feature:compose"), BuildOverviewRequest.Configuration, BuildOverviewRequest.Project), received)
        collector.cancelAndJoin()
    }

    @Test fun cancellationUnregistersTheOldHostAndRecreationDoesNotReplayRequests() = runTest {
        val bus = bus()
        val adapter = AppBuildOverviewEvents(bus)
        val old = mutableListOf<BuildOverviewRequest>()
        val first = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { adapter.requests.collect { old.add(it) } }
        bus.post(OnOverviewRefreshDataEvent())
        runCurrent()
        first.cancelAndJoin()
        bus.post(OnOverviewRefreshDataEvent())
        runCurrent()
        val current = mutableListOf<BuildOverviewRequest>()
        val second = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { adapter.requests.collect { current.add(it) } }
        assertTrue(current.isEmpty())
        bus.post(NavigateToProjectEvent())
        runCurrent()
        assertEquals(listOf(BuildOverviewRequest.Refresh), old)
        assertEquals(listOf(BuildOverviewRequest.Project), current)
        second.cancelAndJoin()
    }

    @Test fun outgoingHostActionsAreNotRebroadcastAsIncomingRequests() = runTest {
        val bus = bus()
        val received = mutableListOf<BuildOverviewRequest>()
        val collector = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { AppBuildOverviewEvents(bus).requests.collect { received.add(it) } }
        bus.post(ShareBuildEvent())
        bus.post(StopBuildEvent())
        bus.post(RestartBuildEvent())
        bus.post(StartProjectActivityEvent())
        runCurrent()
        assertTrue(received.isEmpty())
        collector.cancelAndJoin()
    }
}
