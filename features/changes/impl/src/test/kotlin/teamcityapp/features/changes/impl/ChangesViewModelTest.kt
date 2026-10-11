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

package teamcityapp.features.changes.impl

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelStore
import androidx.paging.testing.asSnapshot
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import teamcityapp.features.changes.api.ChangesPage

@OptIn(ExperimentalCoroutinesApi::class)
class ChangesViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val store = ViewModelStore()
    private val repository = FakeChangesRepository()

    @Before fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After fun tearDown() {
        store.clear()
        Dispatchers.resetMain()
    }
    private fun viewModel() = ChangesViewModel(SavedStateHandle(mapOf("url" to "build:42")), repository).also { store.put("changes", it) }
    private fun TestScope.observe(vm: ChangesViewModel) = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.count.collect {} }

    @Test fun countStartsOnlyWhenObservedAndDoesNotLoadPages() = runTest(dispatcher) {
        val vm = viewModel()
        runCurrent()
        assertEquals(0, repository.countCalls)
        assertTrue(repository.requests.isEmpty())
        observe(vm)
        runCurrent()
        assertEquals(ChangesCountState.Available(1), vm.count.value)
        assertEquals(1, repository.countCalls)
        assertTrue(repository.requests.isEmpty())
    }

    @Test fun completedCountSurvivesConfigurationAndLongResubscription() = runTest(dispatcher) {
        val vm = viewModel()
        val collector = observe(vm)
        runCurrent()
        collector.cancel()
        advanceTimeBy(6_000)
        runCurrent()
        observe(vm)
        runCurrent()
        assertEquals(ChangesCountState.Available(1), vm.count.value)
        assertEquals(1, repository.countCalls)
    }

    @Test fun pendingCountSurvivesShortConfigurationGapWithoutDuplicateRequest() = runTest(dispatcher) {
        val response = CompletableDeferred<Int>()
        repository.loadCount = { response.await() }
        val vm = viewModel()
        val collector = observe(vm)
        runCurrent()
        collector.cancel()
        advanceTimeBy(1_000)
        observe(vm)
        response.complete(3)
        runCurrent()
        assertEquals(ChangesCountState.Available(3), vm.count.value)
        assertEquals(1, repository.countCalls)
    }

    @Test fun unavailableCountIsExplicitAndCanRetryIndependently() = runTest(dispatcher) {
        repository.loadCount = { error("offline") }
        val vm = viewModel()
        observe(vm)
        runCurrent()
        assertEquals(ChangesCountState.Unavailable, vm.count.value)
        repository.loadCount = { 5 }
        vm.retryCount()
        runCurrent()
        assertEquals(ChangesCountState.Available(5), vm.count.value)
        assertEquals(2, repository.countCalls)
        assertTrue(repository.requests.isEmpty())
    }

    @Test fun extendedInactivityCancelsPendingCountWithoutReportingFailure() = runTest(dispatcher) {
        var cancelled = false
        repository.loadCount = {
            try {
                awaitCancellation()
            } finally {
                cancelled = true
            }
        }
        val vm = viewModel()
        val collector = observe(vm)
        runCurrent()
        collector.cancel()
        advanceTimeBy(5_001)
        runCurrent()
        assertTrue(cancelled)
        assertEquals(ChangesCountState.Loading, vm.count.value)
        repository.loadCount = { 2 }
        observe(vm)
        runCurrent()
        assertEquals(ChangesCountState.Available(2), vm.count.value)
    }

    @Test fun lateNoncooperativeCountResponseDoesNotBecomeRetainedContentAfterCancellation() = runTest(dispatcher) {
        val oldResponse = CompletableDeferred<Int>()
        repository.loadCount = { withContext(NonCancellable) { oldResponse.await() } }
        val vm = viewModel()
        val collector = observe(vm)
        runCurrent()
        collector.cancel()
        advanceTimeBy(5_001)
        runCurrent()
        oldResponse.complete(2)
        runCurrent()
        repository.loadCount = { 9 }
        observe(vm)
        runCurrent()
        assertEquals(ChangesCountState.Available(9), vm.count.value)
        assertEquals(2, repository.countCalls)
    }

    @Test fun lazyPagingScrollLoadsOpaqueContinuationWithoutRepeatingInitialPage() = runTest(dispatcher) {
        repository.load = { request ->
            if (request.next == null) {
                ChangesPage((0..9).map { change(it.toString()) }, "/opaque?next=10")
            } else {
                ChangesPage((10..19).map { change(it.toString()) }, null)
            }
        }
        val vm = viewModel()
        val snapshot = vm.changes.asSnapshot { scrollTo(18) }
        assertEquals((0..19).map { change(it.toString()) }, snapshot)
        assertEquals(listOf(null, "/opaque?next=10"), repository.requests.map { it.next })
        assertTrue(repository.requests.none { it.force })
        assertEquals(0, repository.countCalls)
    }

    @Test fun cachedPagesSurviveNewCollectorWithoutDuplicateRequests() = runTest(dispatcher) {
        val vm = viewModel()
        assertEquals(listOf(change()), vm.changes.asSnapshot())
        assertEquals(listOf(change()), vm.changes.asSnapshot())
        assertEquals(1, repository.requests.size)
    }

    @Test fun pullRefreshCreatesFreshGenerationAndBypassesCache() = runTest(dispatcher) {
        val vm = viewModel()
        vm.changes.asSnapshot {
            vm.prepareRefresh()
            refresh()
        }
        assertEquals(listOf(false, true), repository.requests.map { it.force })
        assertEquals(0, repository.countCalls)
    }

    @Test fun clearingFragmentViewModelCancelsPendingPageRequest() = runTest(dispatcher) {
        var cancelled = false
        repository.load = {
            try {
                awaitCancellation()
            } finally {
                cancelled = true
            }
        }
        val vm = viewModel()
        backgroundScope.launch { vm.changes.asSnapshot() }
        runCurrent()
        assertEquals(1, repository.requests.size)
        store.clear()
        runCurrent()
        assertTrue(cancelled)
    }
}
