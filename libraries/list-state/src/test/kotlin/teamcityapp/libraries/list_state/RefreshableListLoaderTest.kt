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

package teamcityapp.libraries.list_state

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RefreshableListLoaderTest {
    @Test
    fun initialLoadingStartsOnlyWithACollector() = runTest {
        val calls = mutableListOf<Boolean>()
        val loader = RefreshableListLoader(flowOf(Unit)) { _, force ->
            calls += force
            listOf("agent")
        }
        val state = share(loader)
        runCurrent()
        assertTrue(calls.isEmpty())
        assertEquals(ListUiState.Loading, state.value)
        observe(state)
        runCurrent()
        assertEquals(listOf(false), calls)
        assertEquals(ListUiState.Content(listOf("agent")), state.value)
    }

    @Test
    fun initialFailureIsRetainedUntilRetryLoadsFreshData() = runTest {
        val calls = mutableListOf<Boolean>()
        val loader = RefreshableListLoader(flowOf(Unit)) { _, force ->
            calls += force
            if (!force) error("offline")
            listOf("recovered")
        }
        val state = share(loader)
        val collector = observe(state)
        runCurrent()
        assertEquals(ListUiState.Error, state.value)
        collector.cancel()
        runCurrent()
        observe(state)
        runCurrent()
        assertEquals(listOf(false), calls)
        loader.retry()
        runCurrent()
        assertEquals(listOf(false, true), calls)
        assertEquals(ListUiState.Content(listOf("recovered")), state.value)
    }

    @Test
    fun refreshRetainsRowsAndReportsFailureWithoutClearingContent() = runTest {
        val refreshed = CompletableDeferred<List<String>>()
        val loader = RefreshableListLoader(flowOf(Unit)) { _, force ->
            if (force) refreshed.await() else listOf("cached")
        }
        val state = share(loader)
        observe(state)
        runCurrent()
        loader.refresh()
        runCurrent()
        assertEquals(ListUiState.Content(listOf("cached"), isRefreshing = true), state.value)
        refreshed.completeExceptionally(IllegalStateException("offline"))
        runCurrent()
        assertEquals(ListUiState.Content(listOf("cached"), refreshFailed = true), state.value)
    }

    @Test
    fun emptyContentHasIndependentRefreshAndFailureStates() = runTest {
        val refreshed = CompletableDeferred<List<String>>()
        val loader = RefreshableListLoader(flowOf(Unit)) { _, force ->
            if (force) refreshed.await() else emptyList()
        }
        val state = share(loader)
        observe(state)
        runCurrent()
        assertEquals(ListUiState.Empty(), state.value)
        loader.refresh()
        runCurrent()
        assertEquals(ListUiState.Empty(isRefreshing = true), state.value)
        refreshed.completeExceptionally(IllegalStateException("offline"))
        runCurrent()
        assertEquals(ListUiState.Empty(refreshFailed = true), state.value)
    }

    @Test
    fun losingLastCollectorCancelsAndResubscriptionRestartsPendingLoad() = runTest {
        var calls = 0
        var cancellations = 0
        val loader = RefreshableListLoader(flowOf(Unit)) { _, _ ->
            calls++
            if (calls == 1) {
                try {
                    awaitCancellation()
                } finally {
                    cancellations++
                }
            }
            listOf("resumed")
        }
        val state = share(loader)
        val collector = observe(state)
        runCurrent()
        collector.cancel()
        runCurrent()
        assertEquals(1, cancellations)
        assertEquals(ListUiState.Loading, state.value)
        observe(state)
        runCurrent()
        assertEquals(2, calls)
        assertEquals(ListUiState.Content(listOf("resumed")), state.value)
    }

    @Test
    fun completedContentSurvivesACollectionGapWithoutReloading() = runTest {
        var calls = 0
        val loader = RefreshableListLoader(flowOf(Unit)) { _, _ ->
            calls++
            listOf("retained")
        }
        val state = share(loader)
        val collector = observe(state)
        runCurrent()
        collector.cancel()
        runCurrent()
        observe(state)
        runCurrent()
        assertEquals(1, calls)
        assertEquals(ListUiState.Content(listOf("retained")), state.value)
    }

    @Test
    fun pendingRefreshResumesAfterCollectionGap() = runTest {
        val calls = mutableListOf<Boolean>()
        val loader = RefreshableListLoader(flowOf(Unit)) { _, force ->
            calls += force
            if (calls.size == 2) awaitCancellation()
            listOf(if (force) "fresh" else "cached")
        }
        val state = share(loader)
        val collector = observe(state)
        runCurrent()
        loader.refresh()
        runCurrent()
        collector.cancel()
        runCurrent()
        observe(state)
        runCurrent()
        assertEquals(listOf(false, true, true), calls)
        assertEquals(ListUiState.Content(listOf("fresh")), state.value)
    }

    @Test
    fun queryReplacementCancelsOldLoadAndHidesPreviousRows() = runTest {
        val queries = MutableStateFlow("first")
        val newResult = CompletableDeferred<List<String>>()
        var cancelled = false
        val loader = RefreshableListLoader(queries) { query, _ ->
            when (query) {
                "first" -> listOf("old")

                "second" -> try {
                    awaitCancellation()
                } finally {
                    cancelled = true
                }

                else -> newResult.await()
            }
        }
        val state = share(loader)
        observe(state)
        runCurrent()
        queries.value = "second"
        runCurrent()
        assertEquals(ListUiState.Loading, state.value)
        queries.value = "third"
        runCurrent()
        assertTrue(cancelled)
        newResult.complete(listOf("new"))
        runCurrent()
        assertEquals(ListUiState.Content(listOf("new")), state.value)
    }

    @Test
    fun repeatedRefreshCancelsOlderRequestAndKeepsOnlyLatestResult() = runTest {
        var refreshes = 0
        var cancellations = 0
        val result = CompletableDeferred<List<String>>()
        val loader = RefreshableListLoader(flowOf(Unit)) { _, force ->
            if (!force) {
                listOf("cached")
            } else {
                refreshes++
                if (refreshes == 1) {
                    try {
                        awaitCancellation()
                    } finally {
                        cancellations++
                    }
                }
                result.await()
            }
        }
        val state = share(loader)
        observe(state)
        runCurrent()
        loader.refresh()
        runCurrent()
        loader.refresh()
        runCurrent()
        assertEquals(1, cancellations)
        result.complete(listOf("latest"))
        runCurrent()
        assertEquals(ListUiState.Content(listOf("latest")), state.value)
    }

    @Test
    fun multipleCollectorsShareRequestsAndOnlyLastCancellationStopsLoading() = runTest {
        var calls = 0
        var cancellations = 0
        val loader = RefreshableListLoader<Unit, String>(flowOf(Unit)) { _, _ ->
            calls++
            try {
                awaitCancellation()
            } finally {
                cancellations++
            }
        }
        val state = share(loader)
        val first = observe(state)
        val second = observe(state)
        runCurrent()
        assertEquals(1, calls)
        first.cancel()
        runCurrent()
        assertEquals(0, cancellations)
        second.cancel()
        runCurrent()
        assertEquals(1, cancellations)
    }

    @Test
    fun refreshRequestedWhileUnobservedRunsWhenCollectionReturns() = runTest {
        val calls = mutableListOf<Boolean>()
        val loader = RefreshableListLoader(flowOf(Unit)) { _, force ->
            calls += force
            listOf(if (force) "fresh" else "cached")
        }
        val state = share(loader)
        val collector = observe(state)
        runCurrent()
        collector.cancel()
        runCurrent()
        loader.refresh()
        runCurrent()
        assertEquals(listOf(false), calls)
        observe(state)
        runCurrent()
        assertEquals(listOf(false, true), calls)
        assertEquals(ListUiState.Content(listOf("fresh")), state.value)
    }

    @Test
    fun reloadUsesNormalCachePolicyAndStillReplacesPendingRequests() = runTest {
        val calls = mutableListOf<Boolean>()
        val loader = RefreshableListLoader(flowOf(Unit)) { _, force ->
            calls += force
            listOf("result ${calls.size}")
        }
        val state = share(loader)
        observe(state)
        runCurrent()
        loader.reload()
        runCurrent()
        assertEquals(listOf(false, false), calls)
        assertEquals(ListUiState.Content(listOf("result 2")), state.value)
    }

    @Test
    fun filterChangeUsesNormalLoadEvenAfterPreviousQueryWasRefreshed() = runTest {
        val queries = MutableStateFlow("first")
        val calls = mutableListOf<Pair<String, Boolean>>()
        val loader = RefreshableListLoader(queries) { query, force ->
            calls += query to force
            listOf(query)
        }
        val state = share(loader)
        observe(state)
        runCurrent()
        loader.refresh()
        runCurrent()
        queries.value = "second"
        runCurrent()
        assertEquals(listOf("first" to false, "first" to true, "second" to false), calls)
        assertEquals(ListUiState.Content(listOf("second")), state.value)
    }

    @Test
    fun synchronousRefreshFromInsideLoaderCannotPublishObsoleteResult() = runTest {
        var calls = 0
        val states = mutableListOf<ListUiState<String>>()
        lateinit var loader: RefreshableListLoader<Unit, String>
        loader = RefreshableListLoader(flowOf(Unit)) { _, _ ->
            calls++
            if (calls == 1) {
                loader.refresh()
                listOf("obsolete")
            } else {
                listOf("current")
            }
        }
        val state = share(loader)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            state.collect { states += it }
        }
        runCurrent()
        assertEquals(2, calls)
        assertEquals(ListUiState.Content(listOf("current")), state.value)
        assertTrue(states.none { it is ListUiState.Content && "obsolete" in it.items })
    }

    @Test
    fun returningToEarlierQueryLoadsAgainWithoutItsPreviousRefreshFailure() = runTest {
        val queries = MutableStateFlow("first")
        val calls = mutableListOf<String>()
        val loader = RefreshableListLoader(queries) { query, force ->
            calls += query
            if (force) error("offline")
            listOf(query)
        }
        val state = share(loader)
        observe(state)
        runCurrent()
        loader.refresh()
        runCurrent()
        assertEquals(ListUiState.Content(listOf("first"), refreshFailed = true), state.value)
        queries.value = "second"
        runCurrent()
        queries.value = "first"
        runCurrent()
        assertEquals(listOf("first", "first", "second", "first"), calls)
        assertEquals(ListUiState.Content(listOf("first")), state.value)
    }

    @Test
    fun failureReturnedAfterCancellationDoesNotBecomeAnError() = runTest {
        val queries = MutableStateFlow("first")
        val oldRequest = CompletableDeferred<Unit>()
        val states = mutableListOf<ListUiState<String>>()
        val loader = RefreshableListLoader(queries) { query, _ ->
            if (query == "first") {
                withContext(NonCancellable) { oldRequest.await() }
                error("obsolete failure")
            }
            listOf("current")
        }
        val state = share(loader)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            state.collect { states += it }
        }
        runCurrent()
        queries.value = "second"
        runCurrent()
        oldRequest.complete(Unit)
        runCurrent()
        assertEquals(ListUiState.Content(listOf("current")), state.value)
        assertTrue(ListUiState.Error !in states)
    }

    @Test
    fun queryStateTagsLoadingAndContentAtomicallyAcrossFilterSwitches() = runTest {
        val queries = MutableStateFlow("first")
        val newResult = CompletableDeferred<List<String>>()
        val states = mutableListOf<Pair<String, ListUiState<String>>>()
        val loader = RefreshableListLoader(queries) { query, _ ->
            if (query == "first") listOf("old") else newResult.await()
        }
        val state = loader.queryState.stateIn(
            backgroundScope,
            SharingStarted.WhileSubscribed(0),
            "first" to ListUiState.Loading
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            state.collect { states += it }
        }
        runCurrent()
        queries.value = "second"
        runCurrent()
        assertEquals("second" to ListUiState.Loading, state.value)
        newResult.complete(listOf("new"))
        runCurrent()
        assertEquals("second" to ListUiState.Content(listOf("new")), state.value)
        val secondStates = states.filter { it.first == "second" }
        assertEquals(
            listOf("second" to ListUiState.Loading, "second" to ListUiState.Content(listOf("new"))),
            secondStates
        )
    }

    private fun <Q, T> TestScope.share(loader: RefreshableListLoader<Q, T>) = loader.state.stateIn(
        backgroundScope,
        SharingStarted.WhileSubscribed(0),
        ListUiState.Loading
    )

    private fun TestScope.observe(state: StateFlow<*>) = backgroundScope.launch(
        UnconfinedTestDispatcher(testScheduler)
    ) { state.collect {} }
}
