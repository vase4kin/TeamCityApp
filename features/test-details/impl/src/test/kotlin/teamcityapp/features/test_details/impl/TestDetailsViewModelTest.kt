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

package teamcityapp.features.test_details.impl

import androidx.lifecycle.ViewModelStore
import app.cash.turbine.test
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import teamcityapp.features.test_details.api.TestDetailsRepository
import teamcityapp.features.test_details.impl.tracker.TestDetailsTracker
import androidx.lifecycle.SavedStateHandle

@OptIn(ExperimentalCoroutinesApi::class)
class TestDetailsViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val store = ViewModelStore()
    private val info = "Test details <tag> & literal text"
    private val content = TestDetailsUiState.Content(info)
    private var trackedViews = 0
    private val tracker = object : TestDetailsTracker {
        override fun trackView() { trackedViews++ }
    }

    @Before fun setUp() = Dispatchers.setMain(dispatcher)
    @After fun tearDown() { store.clear(); Dispatchers.resetMain() }

    private fun viewModel(load: suspend () -> String) = TestDetailsViewModel(SavedStateHandle(mapOf(TestDetailsViewModel.ARG_TEST_URL to "/test")), object : TestDetailsRepository {
        override suspend fun testDetails(url: String) = load()
    }, tracker).also { store.put("details", it) }

    @Test fun screenEntryTracksWithoutStartingALoad() = runTest(dispatcher) {
        var calls = 0
        val vm = viewModel { calls++; info }
        runCurrent()
        assertEquals(0, trackedViews)
        vm.onScreenViewed()
        assertEquals(1, trackedViews)
        assertEquals(0, calls)
    }

    @Test fun screenReentryTracksWithTheRetainedViewModel() = runTest(dispatcher) {
        var calls = 0
        val vm = viewModel { calls++; info }
        vm.onScreenViewed()
        vm.state.test {
            assertEquals(TestDetailsUiState.Loading, awaitItem())
            assertEquals(content, awaitItem())
        }
        runCurrent()
        assertEquals(1, trackedViews)
        vm.onScreenViewed()
        vm.state.test {
            assertEquals(content, awaitItem())
            runCurrent()
            expectNoEvents()
        }
        assertEquals(2, trackedViews)
        assertEquals(1, calls)
    }

    @Test fun loadingStartsOnlyWhenStateIsCollected() = runTest(dispatcher) {
        var calls = 0
        val vm = viewModel { calls++; info }
        runCurrent()
        assertEquals(0, calls)
        assertEquals(TestDetailsUiState.Loading, vm.state.value)
        vm.state.test {
            assertEquals(TestDetailsUiState.Loading, awaitItem())
            assertEquals(content, awaitItem())
        }
        assertEquals(1, calls)
        assertEquals(0, trackedViews)
    }

    @Test fun completedContentIsReusedOnReturn() = runTest(dispatcher) {
        var calls = 0
        val result = CompletableDeferred<String>()
        val vm = viewModel { calls++; result.await() }
        vm.state.test {
            assertEquals(TestDetailsUiState.Loading, awaitItem())
            runCurrent()
            result.complete(info)
            assertEquals(content, awaitItem())
        }
        runCurrent()
        vm.state.test {
            assertEquals(content, awaitItem())
            runCurrent()
            expectNoEvents()
        }
        assertEquals(1, calls)
    }

    @Test fun errorIsExplicitAndOfflineContentIsReused() = runTest(dispatcher) {
        var calls = 0
        val vm = viewModel { calls++; error("offline") }
        val offline = TestDetailsUiState.Error
        vm.state.test {
            assertEquals(TestDetailsUiState.Loading, awaitItem())
            assertEquals(offline, awaitItem())
        }
        runCurrent()
        vm.state.test {
            assertEquals(offline, awaitItem())
            runCurrent()
            expectNoEvents()
        }
        assertEquals(1, calls)
    }

    @Test fun losingLastSubscriberCancelsWithoutRenderingFailure() = runTest(dispatcher) {
        var cancelled = false
        val vm = viewModel { try { awaitCancellation() } finally { cancelled = true } }
        vm.state.test {
            assertEquals(TestDetailsUiState.Loading, awaitItem())
            runCurrent()
        }
        runCurrent()
        assertTrue(cancelled)
        assertEquals(TestDetailsUiState.Loading, vm.state.value)
    }

    @Test fun returningAfterCancellationStartsFreshRequest() = runTest(dispatcher) {
        var calls = 0
        var cancelled = false
        val vm = viewModel {
            if (++calls == 1) {
                try { awaitCancellation() } finally { cancelled = true }
            }
            info
        }
        vm.state.test {
            assertEquals(TestDetailsUiState.Loading, awaitItem())
            runCurrent()
        }
        runCurrent()
        assertTrue(cancelled)
        vm.state.test {
            assertEquals(TestDetailsUiState.Loading, awaitItem())
            assertEquals(content, awaitItem())
        }
        assertEquals(2, calls)
    }

    @Test fun multipleCollectorsShareOnePendingRequest() = runTest(dispatcher) {
        var calls = 0
        val result = CompletableDeferred<String>()
        val vm = viewModel { calls++; result.await() }
        vm.state.test {
            val first = this
            assertEquals(TestDetailsUiState.Loading, awaitItem())
            vm.state.test {
                assertEquals(TestDetailsUiState.Loading, awaitItem())
                runCurrent()
                assertEquals(1, calls)
                result.complete(info)
                assertEquals(content, awaitItem())
                assertEquals(content, first.awaitItem())
            }
        }
    }

    @Test fun clearingViewModelCancelsLoading() = runTest(dispatcher) {
        var cancelled = false
        val vm = viewModel { try { awaitCancellation() } finally { cancelled = true } }
        vm.state.test {
            assertEquals(TestDetailsUiState.Loading, awaitItem())
            runCurrent()
            store.clear()
            runCurrent()
            assertTrue(cancelled)
            expectNoEvents()
        }
        assertEquals(TestDetailsUiState.Loading, vm.state.value)
    }
    @Test fun emptyResponseIsExplicitAndRetained() = runTest(dispatcher) {
        var calls = 0
        val vm = viewModel { calls++; "" }
        vm.state.test {
            assertEquals(TestDetailsUiState.Loading, awaitItem())
            assertEquals(TestDetailsUiState.Empty, awaitItem())
        }
        runCurrent()
        vm.state.test { assertEquals(TestDetailsUiState.Empty, awaitItem()); runCurrent(); expectNoEvents() }
        assertEquals(1, calls)
    }

    @Test fun missingUrlNeverCallsRepository() = runTest(dispatcher) {
        val vm = TestDetailsViewModel(SavedStateHandle(), object : TestDetailsRepository {
            override suspend fun testDetails(url: String): String = error("Missing URL must not load")
        }, tracker).also { store.put("details", it) }
        vm.onScreenViewed()
        assertEquals(1, trackedViews)
        vm.state.test {
            assertEquals(TestDetailsUiState.InvalidInput, awaitItem())
            runCurrent()
            expectNoEvents()
            vm.retry()
            runCurrent()
            expectNoEvents()
        }
    }

    @Test fun retryReplacesErrorWithLoadingAndContent() = runTest(dispatcher) {
        var calls = 0
        val result = CompletableDeferred<String>()
        val vm = viewModel { if (++calls == 1) error("offline") else result.await() }
        vm.state.test {
            assertEquals(TestDetailsUiState.Loading, awaitItem())
            assertEquals(TestDetailsUiState.Error, awaitItem())
            vm.retry()
            assertEquals(TestDetailsUiState.Loading, awaitItem())
            vm.retry() // No parallel retry while a request is pending.
            result.complete(info)
            assertEquals(content, awaitItem())
            vm.retry() // Completed content must not be needlessly reloaded.
            runCurrent()
            expectNoEvents()
        }
        assertEquals(2, calls)
        assertEquals(0, trackedViews)
    }

    @Test fun retryCanFailAgainThenRecover() = runTest(dispatcher) {
        var calls = 0
        val result = CompletableDeferred<String>()
        val vm = viewModel { if (++calls < 3) error("offline") else result.await() }
        vm.state.test {
            assertEquals(TestDetailsUiState.Loading, awaitItem())
            assertEquals(TestDetailsUiState.Error, awaitItem())
            vm.retry()
            // A fast failure may conflate loading; the resulting state remains retryable.
            runCurrent()
            cancelAndIgnoreRemainingEvents()
        }
        runCurrent()
        assertEquals(TestDetailsUiState.Error, vm.state.value)
        vm.state.test {
            assertEquals(TestDetailsUiState.Error, awaitItem())
            vm.retry()
            assertEquals(TestDetailsUiState.Loading, awaitItem())
            result.complete(info)
            assertEquals(content, awaitItem())
        }
        assertEquals(3, calls)
    }
}
