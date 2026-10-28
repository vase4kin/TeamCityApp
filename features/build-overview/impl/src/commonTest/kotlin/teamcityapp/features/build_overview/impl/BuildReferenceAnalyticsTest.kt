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

package teamcityapp.features.build_overview.impl

import androidx.lifecycle.ViewModelStore
import kotlin.test.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.test.*
import teamcityapp.features.build_overview.api.BuildOverviewRepository
import teamcityapp.features.build_overview.api.BuildReferenceTracker
import teamcityapp.libraries.builds.BuildLaunchData

@OptIn(ExperimentalCoroutinesApi::class)
class BuildReferenceAnalyticsTest {
    private val dispatcher = StandardTestDispatcher()
    private val store = ViewModelStore()
    private val events = mutableListOf<String>()
    private val tracker = object : BuildReferenceTracker {
        override fun loaded() {
            events += "loaded"
        }
        override fun failed() {
            events += "failed"
        }
    }
    private val build = BuildLaunchData("7", "/app/rest/builds/id:7")
    private var load: suspend () -> BuildLaunchData = { build }
    private var calls = 0
    private fun model() = BuildReferenceViewModel(
        build.href,
        object : BuildOverviewRepository {
            override suspend fun build(href: String, forceRefresh: Boolean): BuildLaunchData {
                calls++
                return load()
            }
        },
        tracker
    ).also { store.put("reference", it) }

    @BeforeTest fun before() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest fun after() {
        store.clear()
        Dispatchers.resetMain()
    }

    @Test fun completedContentDoesNotReplayResolutionWhenCollectedAgain() = runTest(dispatcher) {
        val vm = model()
        var observer = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.state.collect() }
        runCurrent()
        assertEquals(listOf("loaded"), events)
        observer.cancel()
        runCurrent()
        observer = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.state.collect() }
        runCurrent()
        assertEquals(1, calls)
        assertEquals(listOf("loaded"), events)
        observer.cancel()
    }

    @Test fun explicitRetryTracksTheActualNewOutcomeOnce() = runTest(dispatcher) {
        load = { error("unavailable") }
        val vm = model()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.state.collect() }
        runCurrent()
        assertEquals(listOf("failed"), events)
        load = { build }
        vm.retry()
        runCurrent()
        assertEquals(listOf("failed", "loaded"), events)
        assertEquals(2, calls)
    }

    @Test fun lateSuccessAfterCancellationDoesNotEnterTheRestartFunnel() = runTest(dispatcher) {
        val finish = CompletableDeferred<Unit>()
        load = {
            withContext(NonCancellable) { finish.await() }
            build
        }
        val vm = model()
        val observer = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.state.collect() }
        runCurrent()
        observer.cancel()
        runCurrent()
        finish.complete(Unit)
        runCurrent()
        assertTrue(events.isEmpty())
    }

    @Test fun lateFailureAfterCancellationDoesNotEnterTheRestartFunnel() = runTest(dispatcher) {
        val finish = CompletableDeferred<Unit>()
        load = {
            withContext(NonCancellable) { finish.await() }
            error("old account failed")
        }
        val vm = model()
        val observer = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.state.collect() }
        runCurrent()
        observer.cancel()
        runCurrent()
        finish.complete(Unit)
        runCurrent()
        assertTrue(events.isEmpty())
    }
}
