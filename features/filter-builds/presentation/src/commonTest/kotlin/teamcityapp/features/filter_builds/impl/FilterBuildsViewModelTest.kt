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

package teamcityapp.features.filter_builds.impl
import androidx.lifecycle.SavedStateHandle
import kotlin.test.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.test.*
import teamcityapp.features.filter_builds.api.*
import teamcityapp.features.filter_builds.presentation.resources.*
@OptIn(ExperimentalCoroutinesApi::class)
class FilterBuildsViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @BeforeTest fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest fun cleanup() {
        Dispatchers.resetMain()
    }

    @Test fun filterPreservesAllValuesIncludingHiddenPinnedFlag() = runTest(dispatcher) {
        val vm = FilterBuildsViewModel(
            object : FilterBuildsRepository {
                override suspend fun branches(buildTypeId: String) = listOf("main", "release")
            },
            SavedStateHandle(),
            TestFilterBuildsTracker
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.state.collect() }
        runCurrent()
        val filter = BuildFilter(BuildStatusFilter.Queued, "release", true, true)
        vm.update(filter)
        runCurrent()
        assertEquals(filter, vm.apply())
        assertEquals(BuildStatusFilter.entries, vm.state.value.statusOptions.map { it.status })
        assertEquals(Res.string.text_filters_none, vm.state.value.statusOptions.last().labelRes)
        assertEquals(Res.string.filter_status_server_error, vm.state.value.statusOptions.single { it.status == BuildStatusFilter.Error }.labelRes)
        assertEquals(listOf("main", "release"), vm.state.value.branches)
        assertEquals(Res.string.text_no_branches_available_to_filter, vm.state.value.branchesMessageRes)
    }

    @Test fun failureIsExplicitAndDoesNotPreventFiltering() = runTest(dispatcher) {
        val vm = FilterBuildsViewModel(
            object : FilterBuildsRepository {
                override suspend fun branches(buildTypeId: String): List<String> = error("branches")
            },
            SavedStateHandle(),
            TestFilterBuildsTracker
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.state.collect() }
        runCurrent()
        assertTrue(vm.state.value.branchesFailed)
        assertEquals(Res.string.branches_unavailable, vm.state.value.branchesMessageRes)
        assertEquals(BuildFilter(), vm.apply())
    }

    @Test fun completedBranchesAndFilterSurviveCollectorRecreation() = runTest(dispatcher) {
        var calls = 0
        val vm = FilterBuildsViewModel(
            object : FilterBuildsRepository {
                override suspend fun branches(buildTypeId: String): List<String> {
                    calls++
                    return listOf("main")
                }
            },
            SavedStateHandle(),
            TestFilterBuildsTracker
        )
        val collector = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.state.collect() }
        runCurrent()
        vm.update(BuildFilter(personal = true))
        collector.cancel()
        runCurrent()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.state.collect() }
        runCurrent()
        assertEquals(1, calls)
        assertTrue(vm.state.value.filter.personal)
    }

    @Test fun losingCollectorCancelsPendingLoadWithoutEmittingError() = runTest(dispatcher) {
        var cancelled = false
        val vm = FilterBuildsViewModel(
            object : FilterBuildsRepository {
                override suspend fun branches(buildTypeId: String): List<String> {
                    try {
                        awaitCancellation()
                    } finally {
                        cancelled = true
                    }
                }
            },
            SavedStateHandle(),
            TestFilterBuildsTracker
        )
        val collector = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.state.collect() }
        runCurrent()
        collector.cancel()
        runCurrent()
        assertTrue(cancelled)
        assertFalse(vm.state.value.branchesFailed)
    }
}

private object TestFilterBuildsTracker : FilterBuildsTracker {
    override fun trackView() {}
    override fun trackUserFilteredBuilds() {}
}
