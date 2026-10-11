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

package com.github.vase4kin.teamcityapp.artifact.data

import android.app.Application
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import teamcityapp.features.artifacts.api.ArtifactAction
import teamcityapp.features.artifacts.api.ArtifactDownload

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
@OptIn(ExperimentalCoroutinesApi::class)
class AppArtifactsEventsTest {
    private fun eventBus() = EventBus.builder().logNoSubscriberMessages(false).sendNoSubscriberEvent(false).build()

    @Test fun actionFlowIsColdAndPreservesExactArchiveNamesAndUrlsInOrder() = runTest {
        val bus = eventBus()
        val adapter = AppArtifactsEvents(bus)
        val received = mutableListOf<ArtifactAction>()
        bus.post(ArtifactDownloadEvent("ignored.zip", "ignored"))
        val collector = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { adapter.actions.collect { received.add(it) } }
        bus.post(ArtifactDownloadEvent("report.zip", "opaque/archive!/file"))
        bus.post(ArtifactOpenEvent("report.zip", "opaque/archive!/children"))
        bus.post(ArtifactOpenInBrowserEvent("opaque/metadata/report/index.html"))
        runCurrent()
        assertEquals(
            listOf(
                ArtifactAction.Download(ArtifactDownload("report.zip", "opaque/archive!/file")),
                ArtifactAction.Open("report.zip", "opaque/archive!/children"),
                ArtifactAction.Browser("opaque/metadata/report/index.html")
            ),
            received
        )
        collector.cancelAndJoin()
    }

    @Test fun unsubscribingDisposesTheOldHostAndNewCollectorsDoNotReplayOldActions() = runTest {
        val bus = eventBus()
        val adapter = AppArtifactsEvents(bus)
        val old = mutableListOf<ArtifactAction>()
        val oldCollector = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { adapter.actions.collect { old.add(it) } }
        bus.post(ArtifactOpenEvent("first", "first-url"))
        runCurrent()
        oldCollector.cancelAndJoin()
        bus.post(ArtifactOpenEvent("hidden", "hidden-url"))
        runCurrent()
        val current = mutableListOf<ArtifactAction>()
        val collector = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { adapter.actions.collect { current.add(it) } }
        assertTrue(current.isEmpty())
        bus.post(ArtifactOpenEvent("second", "second-url"))
        runCurrent()
        assertEquals(listOf(ArtifactAction.Open("first", "first-url")), old)
        assertEquals(listOf(ArtifactAction.Open("second", "second-url")), current)
        collector.cancelAndJoin()
    }

    @Test fun downloadFailureStillReachesTheBuildDetailsLegacyEvent() {
        val bus = eventBus()
        val receiver = FailureSubscriber()
        bus.register(receiver)
        AppArtifactsEvents(bus).downloadFailed()
        assertEquals(1, receiver.failures)
        bus.unregister(receiver)
    }

    class FailureSubscriber {
        var failures = 0

        @Subscribe fun failed(@Suppress("UNUSED_PARAMETER") event: ArtifactErrorDownloadingEvent) {
            failures++
        }
    }
}
