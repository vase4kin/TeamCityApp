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

package teamcityapp.features.about.impl

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
import teamcityapp.features.about.api.AboutRepository
import teamcityapp.features.about.api.AboutServerInfo

@OptIn(ExperimentalCoroutinesApi::class)
class AboutViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val store = ViewModelStore()
    private val info = AboutServerInfo("2026.1", "https://teamcity.example")
    private val content = AboutUiState.Content(ServerDetailsUiState.Available(ServerDetailsUiModel(info.version, info.webUrl)))

    @Before fun setUp() = Dispatchers.setMain(dispatcher)
    @After fun tearDown() { store.clear(); Dispatchers.resetMain() }

    private fun viewModel(load: suspend () -> AboutServerInfo) = AboutViewModel(object : AboutRepository {
        override suspend fun serverInfo() = load()
    }).also { store.put("about", it) }

    @Test fun loadingStartsOnlyWhenStateIsCollected() = runTest(dispatcher) {
        var calls = 0
        val vm = viewModel { calls++; info }
        runCurrent()
        assertEquals(0, calls)
        assertEquals(AboutUiState.Loading, vm.state.value)
        vm.state.test {
            assertEquals(AboutUiState.Loading, awaitItem())
            assertEquals(content, awaitItem())
        }
        assertEquals(1, calls)
    }

    @Test fun completedContentIsReusedOnReturn() = runTest(dispatcher) {
        var calls = 0
        val result = CompletableDeferred<AboutServerInfo>()
        val vm = viewModel { calls++; result.await() }
        vm.state.test {
            assertEquals(AboutUiState.Loading, awaitItem())
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
        val offline = AboutUiState.Content(ServerDetailsUiState.Unavailable)
        vm.state.test {
            assertEquals(AboutUiState.Loading, awaitItem())
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
            assertEquals(AboutUiState.Loading, awaitItem())
            runCurrent()
        }
        runCurrent()
        assertTrue(cancelled)
        assertEquals(AboutUiState.Loading, vm.state.value)
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
            assertEquals(AboutUiState.Loading, awaitItem())
            runCurrent()
        }
        runCurrent()
        assertTrue(cancelled)
        vm.state.test {
            assertEquals(AboutUiState.Loading, awaitItem())
            assertEquals(content, awaitItem())
        }
        assertEquals(2, calls)
    }

    @Test fun multipleCollectorsShareOnePendingRequest() = runTest(dispatcher) {
        var calls = 0
        val result = CompletableDeferred<AboutServerInfo>()
        val vm = viewModel { calls++; result.await() }
        vm.state.test {
            val first = this
            assertEquals(AboutUiState.Loading, awaitItem())
            vm.state.test {
                assertEquals(AboutUiState.Loading, awaitItem())
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
            assertEquals(AboutUiState.Loading, awaitItem())
            runCurrent()
            store.clear()
            runCurrent()
            assertTrue(cancelled)
            expectNoEvents()
        }
        assertEquals(AboutUiState.Loading, vm.state.value)
    }
}
