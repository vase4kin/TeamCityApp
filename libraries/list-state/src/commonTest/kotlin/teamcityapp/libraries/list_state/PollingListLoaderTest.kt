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

import kotlin.test.*
import kotlin.test.Test
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import kotlinx.coroutines.withContext

@OptIn(ExperimentalCoroutinesApi::class)
class PollingListLoaderTest {
    private fun TestScope.share(loader: RefreshableListLoader<String, String>) = loader.pollingState
        .stateIn(backgroundScope, SharingStarted.WhileSubscribed(0), PolledListState("account", ListUiState.Loading))
    private fun TestScope.observe(state: kotlinx.coroutines.flow.StateFlow<*>) = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { state.collect {} }
    private fun TestScope.tick() {
        advanceTimeBy(30_000)
        runCurrent()
    }

    @Test fun returnRetainsRowsAndWaitsAFullIntervalBeforeChecking() = runTest {
        val calls = mutableListOf<Boolean>()
        val loader = RefreshableListLoader(flowOf("account"), 30_000) { _, force ->
            calls += force
            listOf("cached")
        }
        val state = share(loader)
        val collector = observe(state)
        runCurrent()
        collector.cancel()
        runCurrent()
        tick()
        observe(state)
        runCurrent()
        assertEquals(listOf(false), calls)
        assertEquals(ListUiState.Content(listOf("cached")), state.value.list)
        tick()
        assertEquals(listOf(false, true), calls)
        assertFalse(state.value.updatesAvailable)
    }

    @Test fun sameCountChangedRowsAreOfferedUntilUserAppliesLatestSnapshot() = runTest {
        var current = listOf("build:running:10%")
        var calls = 0
        val loader = RefreshableListLoader(flowOf("account"), 30_000) { _, _ ->
            calls++
            current
        }
        val state = share(loader)
        observe(state)
        runCurrent()
        current = listOf("build:running:50%")
        tick()
        assertTrue(state.value.updatesAvailable)
        assertEquals(ListUiState.Content(listOf("build:running:10%")), state.value.list)
        current = listOf("build:finished")
        tick()
        loader.applyUpdates()
        runCurrent()
        assertEquals(ListUiState.Content(current), state.value.list)
        assertFalse(state.value.updatesAvailable)
        assertEquals(3, calls)
    }

    @Test fun emptyListsOfferNewItemsAndRemovedItemsCanBeApplied() = runTest {
        var current = emptyList<String>()
        val loader = RefreshableListLoader(flowOf("account"), 30_000) { _, _ -> current }
        val state = share(loader)
        observe(state)
        runCurrent()
        current = listOf("queued")
        tick()
        assertEquals(ListUiState.Empty(), state.value.list)
        assertTrue(state.value.updatesAvailable)
        loader.applyUpdates()
        runCurrent()
        current = emptyList()
        tick()
        assertEquals(ListUiState.Content(listOf("queued")), state.value.list)
        loader.applyUpdates()
        runCurrent()
        assertEquals(ListUiState.Empty(), state.value.list)
    }

    @Test fun pendingUpdatesSurviveVisibilityGapsWithoutAnImmediateReload() = runTest {
        var current = listOf("old")
        var calls = 0
        val loader = RefreshableListLoader(flowOf("account"), 30_000) { _, _ ->
            calls++
            current
        }
        val state = share(loader)
        val collector = observe(state)
        runCurrent()
        current = listOf("new")
        tick()
        collector.cancel()
        runCurrent()
        tick()
        observe(state)
        runCurrent()
        assertEquals(2, calls)
        assertTrue(state.value.updatesAvailable)
        loader.applyUpdates()
        runCurrent()
        assertEquals(ListUiState.Content(current), state.value.list)
    }

    @Test fun aServerRevertRemovesThePendingUpdateNotice() = runTest {
        var current = listOf("old")
        val loader = RefreshableListLoader(flowOf("account"), 30_000) { _, _ -> current }
        val state = share(loader)
        observe(state)
        runCurrent()
        current = listOf("new")
        tick()
        assertTrue(state.value.updatesAvailable)
        current = listOf("old")
        tick()
        assertFalse(state.value.updatesAvailable)
    }

    @Test fun pollingFailureRetainsRowsAndSuccessfulCheckRecoversWithoutReplacingThem() = runTest {
        var fail = false
        val loader = RefreshableListLoader(flowOf("account"), 30_000) { _, _ ->
            if (fail) error("offline")
            listOf("cached")
        }
        val state = share(loader)
        observe(state)
        runCurrent()
        fail = true
        tick()
        assertEquals(ListUiState.Content(listOf("cached"), refreshFailed = true), state.value.list)
        fail = false
        tick()
        assertEquals(ListUiState.Content(listOf("cached")), state.value.list)
        assertFalse(state.value.updatesAvailable)
    }

    @Test fun hidingCancelsAnInFlightCheckWithoutShowingFailure() = runTest {
        var cancelled = false
        val loader = RefreshableListLoader(flowOf("account"), 30_000) { _, force ->
            if (force) {
                try {
                    awaitCancellation()
                } finally {
                    cancelled = true
                }
            }
            listOf("cached")
        }
        val state = share(loader)
        val collector = observe(state)
        runCurrent()
        tick()
        collector.cancel()
        runCurrent()
        assertTrue(cancelled)
        assertEquals(ListUiState.Content(listOf("cached")), state.value.list)
    }

    @Test fun queryChangeRejectsANonCooperativeOldAccountCheck() = runTest {
        val query = MutableStateFlow("account")
        val old = CompletableDeferred<List<String>>()
        val loader = RefreshableListLoader(query, 30_000) { q, force ->
            if (q == "account" && force) withContext(NonCancellable) { old.await() } else listOf(q)
        }
        val state = share(loader)
        observe(state)
        runCurrent()
        tick()
        query.value = "other-account"
        runCurrent()
        old.complete(listOf("stale"))
        runCurrent()
        assertEquals("other-account", state.value.query)
        assertEquals(ListUiState.Content(listOf("other-account")), state.value.list)
        assertFalse(state.value.updatesAvailable)
    }

    @Test fun explicitRefreshReplacesRowsAndClearsPendingUpdates() = runTest {
        var current = listOf("old")
        val loader = RefreshableListLoader(flowOf("account"), 30_000) { _, _ -> current }
        val state = share(loader)
        observe(state)
        runCurrent()
        current = listOf("new")
        tick()
        current = listOf("latest")
        loader.refresh()
        runCurrent()
        assertEquals(ListUiState.Content(current), state.value.list)
        assertFalse(state.value.updatesAvailable)
    }

    @Test fun refreshRejectsANonCooperativeOutdatedCheck() = runTest {
        val stale = CompletableDeferred<List<String>>()
        var calls = 0
        val loader = RefreshableListLoader(flowOf("account"), 30_000) { _, _ ->
            calls++
            when (calls) {
                1 -> listOf("old")
                2 -> withContext(NonCancellable) { stale.await() }
                else -> listOf("explicit refresh")
            }
        }
        val state = share(loader)
        observe(state)
        runCurrent()
        tick()
        loader.refresh()
        runCurrent()
        stale.complete(listOf("stale"))
        runCurrent()
        assertEquals(ListUiState.Content(listOf("explicit refresh")), state.value.list)
        assertFalse(state.value.updatesAvailable)
    }

    @Test fun initialFailureWaitsForRetryInsteadOfPolling() = runTest {
        var calls = 0
        val loader = RefreshableListLoader<String, String>(flowOf("account"), 30_000) { _, force ->
            calls++
            if (!force) error("offline")
            listOf("fresh")
        }
        val state = share(loader)
        observe(state)
        runCurrent()
        tick()
        assertEquals(1, calls)
        assertEquals(ListUiState.Error, state.value.list)
        loader.retry()
        runCurrent()
        assertEquals(ListUiState.Content(listOf("fresh")), state.value.list)
    }
}
