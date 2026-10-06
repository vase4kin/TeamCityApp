/*
 * Copyright 2020 Andrey Tolpeev
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

package teamcityapp.features.manage_accounts.impl

import androidx.lifecycle.ViewModelStore
import app.cash.turbine.test
import java.io.IOException
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import teamcityapp.features.manage_accounts.api.*
import teamcityapp.features.manage_accounts.impl.tracker.ManageAccountsTracker

@OptIn(ExperimentalCoroutinesApi::class)
class ManageAccountsViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val store = ViewModelStore()
    private val a = ManagedAccount(ManagedAccountId("https://one", "Guest user"), false, false)
    private val b = ManagedAccount(ManagedAccountId("https://two", "Guest user"), true, true)
    private class Repo : ManageAccountsRepository {
        var reads = 0
        val writes = mutableListOf<ManagedAccountId>()
        var load: suspend () -> List<ManagedAccount> = { emptyList() }
        var delete: suspend (ManagedAccountId) -> AccountRemoval = { AccountRemoval(emptyList(), AccountRemovalOutcome.Login, true) }
        override suspend fun accounts(): List<ManagedAccount> {
            reads++
            return load()
        }
        override suspend fun remove(id: ManagedAccountId): AccountRemoval {
            writes += id
            return delete(id)
        }
    }
    private class Tracker : ManageAccountsTracker {
        var removed = 0
        var views = 0
        var warnings = 0
        override fun trackView() {
            views++
        }
        override fun trackUserClicksOnSslDisabledWarning() {
            warnings++
        }
        override fun trackAccountRemove() {
            removed++
        }
    }

    @Before fun before() {
        Dispatchers.setMain(dispatcher)
    }

    @After fun after() {
        store.clear()
        Dispatchers.resetMain()
    }
    private fun repo() = Repo().apply { load = { listOf(a, b) } }
    private fun vm(r: Repo, t: Tracker = Tracker()) = ManageAccountsViewModel(r, t).also { store.put("accounts", it) }

    @Test fun screenAndSslWarningEventsTrackWithoutLoadingOrRemovingAccounts() = runTest(dispatcher) {
        val r = repo()
        val tracker = Tracker()
        val model = vm(r, tracker)
        runCurrent()
        assertEquals(0, tracker.views)
        assertEquals(0, tracker.warnings)
        model.onScreenViewed()
        model.onSslWarningClicked()
        model.onScreenViewed()
        assertEquals(2, tracker.views)
        assertEquals(1, tracker.warnings)
        assertEquals(0, tracker.removed)
        assertEquals(0, r.reads)
        assertTrue(r.writes.isEmpty())
    }

    @Test fun collectionStartsLoadingAndPreservesAccountOrder() = runTest(dispatcher) {
        val r = repo()
        val vm = vm(r)
        runCurrent()
        assertEquals(0, r.reads)
        vm.state.test {
            assertEquals(ManageAccountsUiState(), awaitItem())
            assertEquals(AccountListUiState.Content(listOf(a, b)), awaitItem().accounts)
        }
    }

    @Test fun emptyStorageIsAnExplicitEmptyState() = runTest(dispatcher) {
        vm(Repo()).state.test {
            awaitItem()
            assertEquals(AccountListUiState.Empty, awaitItem().accounts)
        }
    }

    @Test fun pendingReadCancelsImmediatelyWithoutErrorAndReturnReadsAgain() = runTest(dispatcher) {
        val r = repo()
        var cancelled = false
        r.load = {
            try {
                awaitCancellation()
            } finally {
                cancelled = true
            }
        }
        val vm = vm(r)
        vm.state.test {
            awaitItem()
            runCurrent()
        }
        runCurrent()
        assertTrue(cancelled)
        assertEquals(AccountListUiState.Loading, vm.state.value.accounts)
        r.load = { listOf(a) }
        vm.state.test {
            awaitItem()
            assertEquals(AccountListUiState.Content(listOf(a)), awaitItem().accounts)
        }
        assertEquals(2, r.reads)
    }

    @Test fun configurationChangeRetainsContentWhileRereadWaits() = runTest(dispatcher) {
        val r = repo()
        val vm = vm(r)
        vm.state.test {
            awaitItem()
            awaitItem()
        }
        runCurrent()
        r.load = { awaitCancellation() }
        vm.state.test {
            assertEquals(AccountListUiState.Content(listOf(a, b)), awaitItem().accounts)
            runCurrent()
            expectNoEvents()
        }
    }

    @Test fun returnFromCreateAccountReadsNewAccounts() = runTest(dispatcher) {
        val r = repo()
        val vm = vm(r)
        vm.state.test {
            awaitItem()
            awaitItem()
        }
        runCurrent()
        r.load = { listOf(a) }
        vm.state.test {
            awaitItem()
            assertEquals(AccountListUiState.Content(listOf(a)), awaitItem().accounts)
        }
    }

    @Test fun failedLoadHasAnExplicitRetry() = runTest(dispatcher) {
        val r = repo()
        r.load = { throw IOException() }
        val vm = vm(r)
        vm.state.test {
            awaitItem()
            assertEquals(AccountListUiState.Error, awaitItem().accounts)
            r.load = { listOf(a) }
            vm.retry()
            assertEquals(AccountListUiState.Loading, awaitItem().accounts)
            assertEquals(AccountListUiState.Content(listOf(a)), awaitItem().accounts)
        }
    }

    @Test fun inactiveRemovalUpdatesRowsAndTracksWithoutNavigation() = runTest(dispatcher) {
        val r = repo()
        r.delete = { AccountRemoval(listOf(b), AccountRemovalOutcome.Stay, true) }
        val t = Tracker()
        val vm = vm(r, t)
        vm.state.test {
            awaitItem()
            awaitItem()
            vm.remove(a.id)
            runCurrent()
            cancelAndIgnoreRemainingEvents()
        }
        assertEquals(ManageAccountsUiState(AccountListUiState.Content(listOf(b))), vm.state.value)
        assertEquals(1, t.removed)
    }

    @Test fun activeRemovalNavigatesHomeAndAcknowledgementDoesNotReplayIt() = runTest(dispatcher) {
        val r = repo()
        r.delete = { AccountRemoval(listOf(a.copy(isActive = true)), AccountRemovalOutcome.Home, true) }
        val vm = vm(r)
        vm.state.test {
            awaitItem()
            awaitItem()
            vm.remove(b.id)
            runCurrent()
            assertEquals(AccountDestination.Home, vm.state.value.destination)
            assertFalse(vm.consumeDestination(AccountDestination.Login))
            assertEquals(AccountDestination.Home, vm.state.value.destination)
            assertTrue(vm.consumeDestination(AccountDestination.Home))
            assertFalse(vm.consumeDestination(AccountDestination.Home))
            runCurrent()
            cancelAndIgnoreRemainingEvents()
        }
        assertNull(vm.state.value.destination)
    }

    @Test fun lastRemovalNavigatesLoginAndRejectsAnotherRemovalUntilHandled() = runTest(dispatcher) {
        val r = repo()
        val vm = vm(r)
        vm.state.test {
            awaitItem()
            awaitItem()
            vm.remove(b.id)
            runCurrent()
            vm.remove(a.id)
            runCurrent()
            cancelAndIgnoreRemainingEvents()
        }
        assertEquals(AccountDestination.Login, vm.state.value.destination)
        assertEquals(listOf(b.id), r.writes)
    }

    @Test fun failedRemovalRetainsAccountsAndRetryUsesTheSameIdentity() = runTest(dispatcher) {
        val r = repo()
        r.delete = { throw IOException() }
        val t = Tracker()
        val vm = vm(r, t)
        vm.state.test {
            awaitItem()
            awaitItem()
            vm.remove(a.id)
            runCurrent()
            cancelAndIgnoreRemainingEvents()
        }
        assertEquals(AccountRemovalUiState.Error(a.id), vm.state.value.removal)
        assertEquals(AccountListUiState.Content(listOf(a, b)), vm.state.value.accounts)
        assertEquals(0, t.removed)
        vm.state.test {
            awaitItem()
            r.delete = { AccountRemoval(listOf(b), AccountRemovalOutcome.Stay, true) }
            vm.retryRemoval()
            runCurrent()
            cancelAndIgnoreRemainingEvents()
        }
        assertEquals(listOf(a.id, a.id), r.writes)
        assertEquals(1, t.removed)
    }

    @Test fun cleanupFailureCanRetryAfterTheAccountWasAlreadyRemovedAndTracksOnce() = runTest(dispatcher) {
        val r = repo()
        r.delete = { AccountRemoval(emptyList(), AccountRemovalOutcome.CleanupFailed, true) }
        val t = Tracker()
        val vm = vm(r, t)
        vm.state.test {
            awaitItem()
            awaitItem()
            vm.remove(b.id)
            runCurrent()
            cancelAndIgnoreRemainingEvents()
        }
        assertEquals(AccountListUiState.Empty, vm.state.value.accounts)
        assertEquals(AccountRemovalUiState.Error(b.id), vm.state.value.removal)
        assertNull(vm.state.value.destination)
        vm.state.test {
            awaitItem()
            r.load = { emptyList() }
            r.delete = { AccountRemoval(emptyList(), AccountRemovalOutcome.Login, false) }
            vm.retryRemoval()
            runCurrent()
            cancelAndIgnoreRemainingEvents()
        }
        assertEquals(AccountDestination.Login, vm.state.value.destination)
        assertEquals(1, t.removed)
    }

    @Test fun duplicateAndConcurrentRemovalRequestsAreIgnored() = runTest(dispatcher) {
        val r = repo()
        val pending = CompletableDeferred<AccountRemoval>()
        r.delete = { pending.await() }
        val vm = vm(r)
        vm.state.test {
            awaitItem()
            awaitItem()
            vm.remove(a.id)
            vm.remove(a.id)
            vm.remove(b.id)
            runCurrent()
            assertEquals(listOf(a.id), r.writes)
            assertFalse(vm.state.value.canInteract)
            pending.complete(AccountRemoval(listOf(b), AccountRemovalOutcome.Stay, true))
            runCurrent()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test fun removalContinuesInBackgroundAndNavigationIsRetainedForReturn() = runTest(dispatcher) {
        val r = repo()
        val pending = CompletableDeferred<AccountRemoval>()
        r.delete = { pending.await() }
        val vm = vm(r)
        vm.state.test {
            awaitItem()
            awaitItem()
            vm.remove(b.id)
            runCurrent()
            cancelAndIgnoreRemainingEvents()
        }
        runCurrent()
        pending.complete(AccountRemoval(emptyList(), AccountRemovalOutcome.Login, true))
        runCurrent()
        r.load = { emptyList() }
        vm.state.test {
            awaitItem()
            runCurrent()
            assertEquals(AccountDestination.Login, vm.state.value.destination)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test fun clearingViewModelCancelsRemovalWithoutTrackingOrShowingAnError() = runTest(dispatcher) {
        val r = repo()
        var cancelled = false
        r.delete = {
            try {
                awaitCancellation()
            } finally {
                cancelled = true
            }
        }
        val t = Tracker()
        val vm = vm(r, t)
        vm.state.test {
            awaitItem()
            awaitItem()
            vm.remove(a.id)
            runCurrent()
            store.clear()
            runCurrent()
            cancelAndIgnoreRemainingEvents()
        }
        assertTrue(cancelled)
        assertEquals(0, t.removed)
    }

    @Test fun readStartedBeforeRemovalCannotRestoreDeletedAccounts() = runTest(dispatcher) {
        val r = repo()
        val vm = vm(r)
        vm.state.test {
            awaitItem()
            awaitItem()
        }
        runCurrent()
        val pending = CompletableDeferred<List<ManagedAccount>>()
        r.load = { pending.await() }
        r.delete = { AccountRemoval(listOf(b), AccountRemovalOutcome.Stay, true) }
        vm.state.test {
            awaitItem()
            runCurrent()
            vm.remove(a.id)
            runCurrent()
            pending.complete(listOf(a, b))
            runCurrent()
            assertEquals(AccountListUiState.Content(listOf(b)), vm.state.value.accounts)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test fun missingAccountAndLoadingEventsCannotMutateStorage() = runTest(dispatcher) {
        val r = repo()
        val vm = vm(r)
        vm.remove(a.id)
        vm.retryRemoval()
        runCurrent()
        assertTrue(r.writes.isEmpty())
        vm.state.test {
            awaitItem()
            awaitItem()
            vm.remove(ManagedAccountId("missing", "missing"))
            vm.retry()
            runCurrent()
            expectNoEvents()
        }
        assertTrue(r.writes.isEmpty())
    }
}
