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

package teamcityapp.features.build_history.impl

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelStore
import androidx.paging.testing.asSnapshot
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import teamcityapp.features.build_history.api.*

/** Paging deliberately caches pending work across collector gaps as well as completed pages. */
@OptIn(ExperimentalCoroutinesApi::class)
class BuildHistoryPagingRetentionTest {
    private val dispatcher = StandardTestDispatcher()
    private val store = ViewModelStore()
    private val repository = FakeHistoryRepository()

    @Before fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @After fun cleanup() {
        store.clear()
        Dispatchers.resetMain()
    }
    private fun viewModel() = BuildHistoryViewModel(SavedStateHandle(mapOf("id" to "configuration")), repository, FakeHistoryOnboarding(), FakeHistoryTracker()).also { store.put("history", it) }

    @Test fun pendingInitialSurvivesCollectorGapAndResumesWithoutDuplicateRequest() = runTest(dispatcher) {
        val response = CompletableDeferred<BuildHistoryPage>()
        var cancelled = false
        repository.loadPage = {
            try {
                response.await()
            } catch (c: CancellationException) {
                cancelled = true
                throw c
            }
        }
        val vm = viewModel()
        val collector = backgroundScope.launch { vm.selection.value.pages.asSnapshot() }
        runCurrent()
        assertEquals(1, repository.requests.size)
        collector.cancel()
        vm.onPaused()
        runCurrent()
        assertFalse(cancelled)
        response.complete(BuildHistoryPage(listOf(historyBuild())))
        runCurrent()
        vm.onConfigurationRecreation()
        vm.onResumed()
        assertEquals(listOf(historyBuild()), vm.selection.value.pages.asSnapshot())
        assertEquals(1, repository.requests.size)
    }

    @Test fun pendingAppendSurvivesCollectorGapAndRetainsBothPagesOnReturn() = runTest(dispatcher) {
        val response = CompletableDeferred<BuildHistoryPage>()
        val first = (0..9).map { historyBuild(it.toString()) }
        val second = (10..19).map { historyBuild(it.toString()) }
        repository.loadPage = { if (it.next == null) BuildHistoryPage(first, "opaque-next") else response.await() }
        val vm = viewModel()
        val collector = backgroundScope.launch { vm.selection.value.pages.asSnapshot { scrollTo(18) } }
        runCurrent()
        advanceTimeBy(1_000)
        runCurrent()
        assertEquals(2, repository.requests.size)
        collector.cancel()
        vm.onPaused()
        runCurrent()
        response.complete(BuildHistoryPage(second))
        runCurrent()
        vm.onResumed()
        assertEquals(first + second, vm.selection.value.pages.asSnapshot())
        advanceTimeBy(1_000)
        runCurrent()
        assertEquals(2, repository.requests.size)
    }

    @Test fun ownerDisposalCancelsPendingInitialRatherThanConvertingCancellationToError() = runTest(dispatcher) {
        var cancelled = false
        repository.loadPage = {
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
        assertEquals(1, repository.requests.size)
    }

    @Test fun ownerDisposalCancelsPendingAppend() = runTest(dispatcher) {
        var cancelled = false
        repository.loadPage = {
            if (it.next == null) {
                BuildHistoryPage((0..9).map { id -> historyBuild(id.toString()) }, "opaque-next")
            } else {
                try {
                    awaitCancellation()
                } finally {
                    cancelled = true
                }
            }
        }
        val vm = viewModel()
        backgroundScope.launch { vm.selection.value.pages.asSnapshot { scrollTo(18) } }
        runCurrent()
        advanceTimeBy(1_000)
        runCurrent()
        assertEquals(2, repository.requests.size)
        store.clear()
        runCurrent()
        assertTrue(cancelled)
    }

    @Test fun replacedQueryRejectsLateAppendFailureAndKeepsNewRows() = runTest(dispatcher) {
        val late = CompletableDeferred<Unit>()
        var cancelled = false
        repository.loadPage = {
            when {
                it.query.locator != BuildHistoryQuery.DEFAULT_LOCATOR -> BuildHistoryPage(listOf(historyBuild("filtered")))

                it.next == null -> BuildHistoryPage((0..9).map { id -> historyBuild(id.toString()) }, "opaque-next")

                else -> try {
                    withContext(NonCancellable) {
                        late.await()
                        error("late old append failure")
                    }
                } finally {
                    cancelled = !currentCoroutineContext().isActive
                }
            }
        }
        val vm = viewModel()
        backgroundScope.launch { vm.selection.value.pages.asSnapshot { scrollTo(18) } }
        runCurrent()
        advanceTimeBy(1_000)
        runCurrent()
        assertEquals(2, repository.requests.size)
        vm.applyFilter("state:queued")
        val filtered = vm.selection.value
        assertEquals(listOf(historyBuild("filtered")), filtered.pages.asSnapshot())
        late.complete(Unit)
        runCurrent()
        assertTrue(cancelled)
        assertEquals("state:queued", vm.selection.value.query.locator)
        assertSame(filtered.pages, vm.selection.value.pages)
        assertEquals(listOf(historyBuild("filtered")), vm.selection.value.pages.asSnapshot())
    }
}
