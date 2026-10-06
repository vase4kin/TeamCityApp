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

package teamcityapp.features.splash.impl

import androidx.lifecycle.ViewModelStore
import app.cash.turbine.test
import java.io.IOException
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import teamcityapp.features.splash.api.*

@OptIn(ExperimentalCoroutinesApi::class)
class SplashViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val store = ViewModelStore()
    private class Repo : SplashRepository {
        var reads = 0
        var load: suspend () -> Boolean = { false }
        override suspend fun hasAccounts(): Boolean {
            reads++
            return load()
        }
    }

    @Before fun before() {
        Dispatchers.setMain(dispatcher)
    }

    @After fun after() {
        store.clear()
        Dispatchers.resetMain()
    }
    private fun vm(r: Repo) = SplashViewModel(r).also { store.put("splash", it) }

    @Test fun loadingStartsOnlyWithACollectorAndNoAccountsRoutesToLogin() = runTest(dispatcher) {
        val repo = Repo()
        val vm = vm(repo)
        runCurrent()
        assertEquals(0, repo.reads)
        vm.state.test {
            assertEquals(SplashUiState.Loading, awaitItem())
            assertEquals(SplashUiState.Ready(SplashDestination.Login), awaitItem())
        }
        assertEquals(1, repo.reads)
    }

    @Test fun existingAccountsRouteToHome() = runTest(dispatcher) {
        vm(Repo().apply { load = { true } }).state.test {
            awaitItem()
            assertEquals(SplashUiState.Ready(SplashDestination.Home), awaitItem())
        }
    }

    @Test fun losingLastCollectorCancelsPendingWorkImmediatelyAndRestartReadsAgain() = runTest(dispatcher) {
        val repo = Repo()
        var cancelled = false
        repo.load = {
            try {
                awaitCancellation()
            } finally {
                cancelled = true
            }
        }
        val vm = vm(repo)
        vm.state.test {
            awaitItem()
            runCurrent()
        }
        runCurrent()
        assertTrue(cancelled)
        assertEquals(SplashUiState.Loading, vm.state.value)
        repo.load = { true }
        vm.state.test {
            assertEquals(SplashUiState.Loading, awaitItem())
            assertEquals(SplashUiState.Ready(SplashDestination.Home), awaitItem())
        }
        assertEquals(2, repo.reads)
    }

    @Test fun completedStateSurvivesRecreationWithoutAnotherAccountRead() = runTest(dispatcher) {
        val repo = Repo()
        val vm = vm(repo)
        vm.state.test {
            awaitItem()
            awaitItem()
        }
        runCurrent()
        repo.load = { error("must stay cached") }
        vm.state.test {
            assertEquals(SplashUiState.Ready(SplashDestination.Login), awaitItem())
            runCurrent()
            expectNoEvents()
        }
        assertEquals(1, repo.reads)
    }

    @Test fun failureIsRetainedUntilExplicitRetryAndCanRecover() = runTest(dispatcher) {
        val repo = Repo().apply { load = { throw IOException() } }
        val vm = vm(repo)
        vm.state.test {
            awaitItem()
            assertEquals(SplashUiState.Error, awaitItem())
        }
        runCurrent()
        vm.state.test {
            assertEquals(SplashUiState.Error, awaitItem())
            runCurrent()
            assertEquals(1, repo.reads)
            repo.load = { true }
            vm.retry()
            assertEquals(SplashUiState.Loading, awaitItem())
            assertEquals(SplashUiState.Ready(SplashDestination.Home), awaitItem())
        }
        assertEquals(2, repo.reads)
    }

    @Test fun cancellationDoesNotBecomeAnError() = runTest(dispatcher) {
        val vm = vm(Repo().apply { load = { throw CancellationException() } })
        vm.state.test {
            awaitItem()
            runCurrent()
            expectNoEvents()
            assertEquals(SplashUiState.Loading, vm.state.value)
        }
    }

    @Test fun clearingViewModelCancelsPendingRead() = runTest(dispatcher) {
        var cancelled = false
        val vm = vm(
            Repo().apply {
                load = {
                    try {
                        awaitCancellation()
                    } finally {
                        cancelled = true
                    }
                }
            }
        )
        vm.state.test {
            awaitItem()
            runCurrent()
            store.clear()
            runCurrent()
            expectNoEvents()
        }
        assertTrue(cancelled)
    }

    @Test fun wrongOrPrematureDestinationCannotConsumeNavigation() = runTest(dispatcher) {
        val vm = vm(Repo())
        assertFalse(vm.consumeDestination(SplashDestination.Login))
        vm.state.test {
            awaitItem()
            awaitItem()
            assertFalse(vm.consumeDestination(SplashDestination.Home))
            assertTrue(vm.consumeDestination(SplashDestination.Login))
            assertEquals(SplashUiState.Navigated, awaitItem())
            assertFalse(vm.consumeDestination(SplashDestination.Login))
        }
    }

    @Test fun navigationClaimSurvivesResubscriptionAndRetryCannotReplayIt() = runTest(dispatcher) {
        val repo = Repo()
        val vm = vm(repo)
        vm.state.test {
            awaitItem()
            awaitItem()
            assertTrue(vm.consumeDestination(SplashDestination.Login))
            assertEquals(SplashUiState.Navigated, awaitItem())
        }
        runCurrent()
        vm.retry()
        vm.state.test {
            assertEquals(SplashUiState.Navigated, awaitItem())
            runCurrent()
            expectNoEvents()
            assertFalse(vm.consumeDestination(SplashDestination.Login))
        }
        assertEquals(1, repo.reads)
    }

    @Test fun twoCollectorsShareOneReadAndFirstCollectorLeavingDoesNotCancelIt() = runTest(dispatcher) {
        val repo = Repo()
        val pending = CompletableDeferred<Boolean>()
        repo.load = { pending.await() }
        val vm = vm(repo)
        val first = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.state.collect() }
        val second = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.state.collect() }
        runCurrent()
        assertEquals(1, repo.reads)
        first.cancel()
        runCurrent()
        pending.complete(true)
        runCurrent()
        assertEquals(SplashUiState.Ready(SplashDestination.Home), vm.state.value)
        second.cancel()
    }

    @Test fun duplicateRetryDuringLoadingDoesNotCancelOrDuplicateRequest() = runTest(dispatcher) {
        val repo = Repo()
        val pending = CompletableDeferred<Boolean>()
        repo.load = { pending.await() }
        val vm = vm(repo)
        vm.state.test {
            awaitItem()
            runCurrent()
            vm.retry()
            vm.retry()
            runCurrent()
            assertEquals(1, repo.reads)
            pending.complete(false)
            assertEquals(SplashUiState.Ready(SplashDestination.Login), awaitItem())
        }
    }
}
