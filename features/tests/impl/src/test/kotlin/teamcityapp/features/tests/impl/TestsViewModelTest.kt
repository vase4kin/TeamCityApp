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

package teamcityapp.features.tests.impl

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelStore
import androidx.paging.testing.asSnapshot
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import teamcityapp.features.tests.api.*

@OptIn(ExperimentalCoroutinesApi::class)
class TestsViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val store = ViewModelStore()
    private val repository = FakeTestsRepository()
    private val handle = SavedStateHandle(mapOf("url" to "build:42", "passedCount" to 12, "failedCount" to 2, "ignoredCount" to 2))

    @Before fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After fun tearDown() {
        store.clear()
        Dispatchers.resetMain()
    }
    private fun viewModel() = TestsViewModel(handle, repository).also { store.put("tests", it) }
    private fun TestScope.observeCount(vm: TestsViewModel) = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.count.collect {} }

    @Test fun defaultFailedFilterAndCountsPreserveIncomingArguments() = runTest(dispatcher) {
        val vm = viewModel()
        assertEquals(TestsFilter.Failed, vm.selection.value.filter)
        assertEquals(TestsCounts(12, 2, 2), vm.counts)
        assertEquals(R.string.tests_empty_failed, vm.selection.value.presentation.emptyMessageRes)
        assertEquals(teamcityapp.libraries.theme.UiText.Resource(R.string.tests_section_failed, listOf(2)), vm.selection.value.presentation.sectionTitles[TestsFilter.Failed])
        assertEquals(listOf(R.string.tests_filter_failed, R.string.tests_filter_passed, R.string.tests_filter_ignored), vm.selection.value.presentation.filterOptions.map { it.labelRes })
        runCurrent()
        assertTrue(repository.requests.isEmpty())
        assertEquals(0, repository.countCalls)
    }

    @Test fun defaultFailedFilterIsKeptWhenOnlyPassedTestsExistAndUnavailableFiltersAreIgnored() = runTest(dispatcher) {
        handle["failedCount"] = 0
        handle["ignoredCount"] = 0
        val vm = viewModel()
        assertEquals(TestsFilter.Failed, vm.selection.value.filter)
        vm.selectFilter(TestsFilter.Ignored)
        assertEquals(TestsFilter.Failed, vm.selection.value.filter)
        vm.selectFilter(TestsFilter.Passed)
        assertEquals(TestsFilter.Passed, vm.selection.value.filter)
        assertEquals(R.string.tests_empty_passed, vm.selection.value.presentation.emptyMessageRes)
    }

    @Test fun selectedFilterSurvivesSavedStateRestoration() = runTest(dispatcher) {
        handle[TestsViewModel.ARG_FILTER] = TestsFilter.Ignored.name
        assertEquals(TestsFilter.Ignored, viewModel().selection.value.filter)
    }

    @Test fun countCollectionLoadsIndependentlyOfPages() = runTest(dispatcher) {
        val vm = viewModel()
        observeCount(vm)
        runCurrent()
        assertEquals(TestsCountState.Available(16), vm.count.value)
        assertEquals(1, repository.countCalls)
        assertTrue(repository.requests.isEmpty())
    }

    @Test fun completedCountSurvivesLongResubscriptionAndFilterChanges() = runTest(dispatcher) {
        val vm = viewModel()
        val collector = observeCount(vm)
        runCurrent()
        collector.cancel()
        advanceTimeBy(6_000)
        runCurrent()
        vm.selectFilter(TestsFilter.Passed)
        observeCount(vm)
        runCurrent()
        assertEquals(1, repository.countCalls)
        assertEquals(TestsCountState.Available(16), vm.count.value)
    }

    @Test fun pendingCountSurvivesShortConfigurationGap() = runTest(dispatcher) {
        val response = CompletableDeferred<Int>()
        repository.loadCount = { response.await() }
        val vm = viewModel()
        val collector = observeCount(vm)
        runCurrent()
        collector.cancel()
        advanceTimeBy(1_000)
        observeCount(vm)
        response.complete(20)
        runCurrent()
        assertEquals(TestsCountState.Available(20), vm.count.value)
        assertEquals(1, repository.countCalls)
    }

    @Test fun countFailureIsExplicitAndRetryDoesNotReloadPages() = runTest(dispatcher) {
        repository.loadCount = { error("offline") }
        val vm = viewModel()
        observeCount(vm)
        runCurrent()
        assertEquals(TestsCountState.Unavailable, vm.count.value)
        repository.loadCount = { 25 }
        vm.retryCount()
        runCurrent()
        assertEquals(TestsCountState.Available(25), vm.count.value)
        assertTrue(repository.requests.isEmpty())
    }

    @Test fun prolongedInactivityCancelsCountWithoutRenderingFailure() = runTest(dispatcher) {
        var cancelled = false
        repository.loadCount = {
            try {
                awaitCancellation()
            } finally {
                cancelled = true
            }
        }
        val vm = viewModel()
        val collector = observeCount(vm)
        runCurrent()
        collector.cancel()
        advanceTimeBy(5_001)
        runCurrent()
        assertTrue(cancelled)
        assertEquals(TestsCountState.Loading, vm.count.value)
    }

    @Test fun paginationFollowsServerContinuationAndAppendBypassesCache() = runTest(dispatcher) {
        repository.load = { request ->
            if (request.next == null) {
                TestsPage((0..9).map { testOccurrence("$it") }, "opaque next")
            } else {
                TestsPage((10..19).map { testOccurrence("$it") }, null)
            }
        }
        val vm = viewModel()
        val rows = vm.selection.value.pages.asSnapshot { scrollTo(18) }
        assertEquals((0..19).map { testOccurrence("$it") }, rows)
        assertEquals(listOf(null, "opaque next"), repository.requests.map { it.next })
        assertEquals(listOf(false, true), repository.requests.map { it.force })
    }

    @Test fun completedPagesSurviveConfigurationCollectorsWithoutDuplicateRequests() = runTest(dispatcher) {
        val vm = viewModel()
        assertEquals(listOf(testOccurrence()), vm.selection.value.pages.asSnapshot())
        assertEquals(listOf(testOccurrence()), vm.selection.value.pages.asSnapshot())
        assertEquals(1, repository.requests.size)
    }

    @Test fun pullRefreshBypassesCacheForSameSelectedFilter() = runTest(dispatcher) {
        val vm = viewModel()
        vm.selection.value.pages.asSnapshot {
            vm.prepareRefresh()
            refresh()
        }
        assertEquals(listOf(false, true), repository.requests.map { it.force })
        assertTrue(repository.requests.all { it.filter == TestsFilter.Failed })
    }

    @Test fun changingFilterCancelsPreviousRequestAndResetsContinuationAndCachePolicy() = runTest(dispatcher) {
        var cancelled = false
        repository.load = { request ->
            if (request.filter == TestsFilter.Failed) {
                try {
                    awaitCancellation()
                } finally {
                    cancelled = true
                }
            }
            TestsPage(listOf(testOccurrence("2", TestStatus.Passed)), null)
        }
        val vm = viewModel()
        backgroundScope.launch { vm.selection.value.pages.asSnapshot() }
        runCurrent()
        vm.prepareRefresh()
        vm.selectFilter(TestsFilter.Passed)
        runCurrent()
        assertTrue(cancelled)
        assertEquals(TestsFilter.Passed, vm.selection.value.filter)
        assertEquals(R.string.tests_empty_passed, vm.selection.value.presentation.emptyMessageRes)
        assertEquals(listOf(testOccurrence("2", TestStatus.Passed)), vm.selection.value.pages.asSnapshot())
        assertEquals(FakeTestsRepository.Request("build:42", TestsFilter.Passed, null, false), repository.requests.last())
        assertEquals(TestsFilter.Passed.name, handle.get<String>(TestsViewModel.ARG_FILTER))
    }

    @Test fun clearingViewModelCancelsPendingPagingWork() = runTest(dispatcher) {
        var cancelled = false
        repository.load = {
            try {
                awaitCancellation()
            } finally {
                cancelled = true
            }
        }
        val vm = viewModel()
        backgroundScope.launch { vm.selection.value.pages.asSnapshot() }
        runCurrent()
        store.clear()
        runCurrent()
        assertTrue(cancelled)
    }
}
