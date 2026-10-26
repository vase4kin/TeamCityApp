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
import teamcityapp.features.build_details.api.*
import teamcityapp.features.build_overview.api.BuildOverviewRepository
import teamcityapp.libraries.builds.*

@OptIn(ExperimentalCoroutinesApi::class)
class BuildReferenceCountsTest {
    private val dispatcher = StandardTestDispatcher()
    private val store = ViewModelStore()
    private val build = BuildLaunchData("7", "/app/rest/builds/id:7", changes = BuildCollectionLink("/app/rest/changes?locator=build:(id:7)", 1), tests = BuildTests("/app/rest/tests?locator=build:(id:7)", 1, 1, 0, 2))

    @BeforeTest fun before() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest fun after() {
        store.clear()
        Dispatchers.resetMain()
    }
    private fun model(load: suspend (BuildDetailsSection) -> Int): BuildReferenceViewModel = BuildReferenceViewModel(
        build.href,
        object : BuildOverviewRepository {
            override suspend fun build(href: String, forceRefresh: Boolean) = this@BuildReferenceCountsTest.build
        },
        object : BuildDetailsMetadataRepository {
            override suspend fun count(build: BuildLaunchData, tab: BuildDetailsSection) = load(tab)
            override suspend fun pendingPrompts() = emptySet<BuildDetailsPrompt>()
            override suspend fun acknowledgePrompt(prompt: BuildDetailsPrompt) {}
        }
    ).also { store.put("reference", it) }

    @Test fun optionalFailureKeepsBuildAndOtherCountAndRetryOnlyReloadsFailedSection() = runTest(dispatcher) {
        val calls = mutableListOf<BuildDetailsSection>()
        var fail = true
        val vm = model { section ->
            calls += section
            if (section == BuildDetailsSection.Changes && fail) error("count unavailable")
            if (section == BuildDetailsSection.Changes) 1 else 2
        }
        val observer = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.state.collect {} }
        runCurrent()
        assertEquals(build, vm.state.value.build)
        assertEquals(BuildDetailsCount.Unavailable, vm.state.value.sections.first { it.section == BuildDetailsSection.Changes }.count)
        assertEquals(BuildDetailsCount.Available(2), vm.state.value.sections.first { it.section == BuildDetailsSection.Tests }.count)
        fail = false
        vm.retryCount(BuildDetailsSection.Changes)
        runCurrent()
        assertEquals(BuildDetailsCount.Available(1), vm.state.value.sections.first { it.section == BuildDetailsSection.Changes }.count)
        assertEquals(1, calls.count { it == BuildDetailsSection.Tests })
        observer.cancel()
        runCurrent()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.state.collect {} }
        runCurrent()
        assertEquals(3, calls.size)
    }

    @Test fun leavingTheVisitCancelsPendingCountsAndResumesWithoutDiscardingCompletedCount() = runTest(dispatcher) {
        var cancelled = 0
        val calls = mutableListOf<BuildDetailsSection>()
        val vm = model { section ->
            calls += section
            if (section == BuildDetailsSection.Changes) {
                try {
                    awaitCancellation()
                } finally {
                    cancelled++
                }
            } else {
                2
            }
        }
        val observer = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.state.collect {} }
        runCurrent()
        observer.cancel()
        runCurrent()
        assertEquals(1, cancelled)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.state.collect {} }
        runCurrent()
        assertEquals(1, calls.count { it == BuildDetailsSection.Tests })
        assertEquals(2, calls.count { it == BuildDetailsSection.Changes })
    }
}
