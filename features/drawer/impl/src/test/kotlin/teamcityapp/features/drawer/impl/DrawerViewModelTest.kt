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

package teamcityapp.features.drawer.impl
import androidx.lifecycle.ViewModelStore
import app.cash.turbine.test
import java.io.IOException
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import teamcityapp.features.drawer.api.*
import teamcityapp.features.drawer.impl.tracker.DrawerTracker

@OptIn(ExperimentalCoroutinesApi::class)
class DrawerViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val store = ViewModelStore()
    private val a = DrawerAccount(DrawerAccountId("https://one", "Guest user"), false, false)
    private val b = DrawerAccount(DrawerAccountId("https://two", "Guest user"), true, true)
    private class Repo : DrawerRepository {
        var reads = 0
        val writes = mutableListOf<DrawerAccountId>()
        var load: suspend () -> List<DrawerAccount> = { emptyList() }
        var switch: suspend (DrawerAccountId) -> AccountSelection = { AccountSelection(emptyList(), SelectionOutcome.Changed) }
        override suspend fun accounts(): List<DrawerAccount> {
            reads++
            return load()
        }
        override suspend fun select(id: DrawerAccountId): AccountSelection {
            writes += id
            return switch(id)
        }
    }
    private class Tracker : DrawerTracker {
        var switched = 0
        override fun trackChangeAccount() {
            switched++
        }
        var views = 0
        val opened = mutableListOf<String>()
        override fun trackView() {
            views++
        }
        override fun trackOpenPrivacy() {
            opened += "privacy"
        }
        override fun trackRateTheApp() {
            opened += "rate"
        }
        override fun trackOpenAbout() {
            opened += "about"
        }
        override fun trackOpenAddNewAccount() {
            opened += "add"
        }
        override fun trackOpenManageAccounts() {
            opened += "manage"
        }
        override fun trackOpenSettings() {
            opened += "settings"
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
    private fun vm(r: Repo, t: Tracker = Tracker()) = DrawerViewModel(r, t).also { store.put("drawer", it) }

    @Test fun screenAndMenuEventsTrackWithoutReadingOrSwitchingAccounts() = runTest(dispatcher) {
        val r = repo()
        val tracker = Tracker()
        val model = vm(r, tracker)
        runCurrent()
        assertEquals(0, tracker.views)
        assertTrue(tracker.opened.isEmpty())
        model.onScreenViewed()
        model.onAddAccountClicked()
        model.onManageAccountsClicked()
        model.onSettingsClicked()
        model.onAboutClicked()
        model.onPrivacyClicked()
        model.onRateClicked()
        model.onScreenViewed()
        assertEquals(2, tracker.views)
        assertEquals(listOf("add", "manage", "settings", "about", "privacy", "rate"), tracker.opened)
        assertEquals(0, tracker.switched)
        assertEquals(0, r.reads)
        assertTrue(r.writes.isEmpty())
    }

    @Test fun loadingIsCollectionDrivenAndPreservesImmutableAccountOrder() = runTest(dispatcher) {
        val r = repo()
        val vm = vm(r)
        runCurrent()
        assertEquals(0, r.reads)
        vm.state.test {
            assertEquals(DrawerUiState(), awaitItem())
            assertEquals(DrawerAccountsUiState.Content(listOf(a, b)), awaitItem().accounts)
        }
    }

    @Test fun noAccountsProducesEmptyState() = runTest(dispatcher) {
        vm(Repo()).state.test {
            awaitItem()
            assertEquals(DrawerAccountsUiState.Empty, awaitItem().accounts)
        }
    }

    @Test fun losingCollectorsImmediatelyCancelsPendingReadWithoutError() = runTest(dispatcher) {
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
        assertEquals(DrawerAccountsUiState.Loading, vm.state.value.accounts)
        r.load = { listOf(a) }
        vm.state.test {
            awaitItem()
            assertEquals(DrawerAccountsUiState.Content(listOf(a)), awaitItem().accounts)
        }
        assertEquals(2, r.reads)
    }

    @Test fun configurationChangeRetainsCompletedSnapshotWhileRereading() = runTest(dispatcher) {
        val r = repo()
        val vm = vm(r)
        vm.state.test {
            awaitItem()
            awaitItem()
        }
        runCurrent()
        r.load = { awaitCancellation() }
        vm.state.test {
            assertEquals(DrawerAccountsUiState.Content(listOf(a, b)), awaitItem().accounts)
            runCurrent()
            expectNoEvents()
        }
    }

    @Test fun returnFromAccountCreationReadsTheNewSnapshot() = runTest(dispatcher) {
        val r = repo()
        val vm = vm(r)
        vm.state.test {
            awaitItem()
            awaitItem()
        }
        runCurrent()
        r.load = { listOf(b) }
        vm.state.test {
            awaitItem()
            assertEquals(DrawerAccountsUiState.Content(listOf(b)), awaitItem().accounts)
        }
    }

    @Test fun loadFailureOffersExplicitRetry() = runTest(dispatcher) {
        val r = repo()
        r.load = { throw IOException() }
        val vm = vm(r)
        vm.state.test {
            awaitItem()
            assertEquals(DrawerAccountsUiState.Error, awaitItem().accounts)
            r.load = { listOf(a) }
            vm.retry()
            assertEquals(DrawerAccountsUiState.Loading, awaitItem().accounts)
            assertEquals(DrawerAccountsUiState.Content(listOf(a)), awaitItem().accounts)
        }
    }

    @Test fun successfulSwitchUpdatesSnapshotTracksOnceAndClaimsNavigationOnce() = runTest(dispatcher) {
        val r = repo()
        val selected = listOf(a.copy(isActive = true), b.copy(isActive = false))
        r.switch = { AccountSelection(selected, SelectionOutcome.Changed) }
        val t = Tracker()
        val vm = vm(r, t)
        vm.state.test {
            awaitItem()
            awaitItem()
            vm.select(a.id)
            runCurrent()
            assertEquals(DrawerAccountsUiState.Content(selected), vm.state.value.accounts)
            assertTrue(vm.state.value.openHome)
            assertFalse(vm.state.value.canInteract)
            assertTrue(vm.consumeHome())
            assertFalse(vm.consumeHome())
            runCurrent()
            cancelAndIgnoreRemainingEvents()
        }
        assertEquals(1, t.switched)
        assertFalse(vm.state.value.openHome)
    }

    @Test fun alreadyActiveSelectionIsANoopEvenWhenCalledDirectly() = runTest(dispatcher) {
        val r = repo()
        val vm = vm(r)
        vm.state.test {
            awaitItem()
            awaitItem()
            vm.select(b.id)
            runCurrent()
            expectNoEvents()
        }
        assertTrue(r.writes.isEmpty())
    }

    @Test fun loadingAndUnknownIdentityCannotWrite() = runTest(dispatcher) {
        val r = repo()
        val vm = vm(r)
        vm.select(a.id)
        runCurrent()
        assertTrue(r.writes.isEmpty())
        vm.state.test {
            awaitItem()
            awaitItem()
            vm.select(DrawerAccountId("missing", "missing"))
            vm.retry()
            vm.retrySelection()
            runCurrent()
            expectNoEvents()
        }
        assertTrue(r.writes.isEmpty())
    }

    @Test fun failedSwitchRetainsAccountsAndRetriesTheSameIdentity() = runTest(dispatcher) {
        val r = repo()
        r.switch = { throw IOException() }
        val t = Tracker()
        val vm = vm(r, t)
        vm.state.test {
            awaitItem()
            awaitItem()
            vm.select(a.id)
            runCurrent()
            assertEquals(AccountSwitchUiState.Error(a.id), vm.state.value.selection)
            assertEquals(DrawerAccountsUiState.Content(listOf(a, b)), vm.state.value.accounts)
            assertFalse(vm.state.value.openHome)
            assertEquals(0, t.switched)
            r.switch = { AccountSelection(listOf(a.copy(isActive = true)), SelectionOutcome.Changed) }
            vm.retrySelection()
            runCurrent()
            cancelAndIgnoreRemainingEvents()
        }
        assertEquals(listOf(a.id, a.id), r.writes)
        assertEquals(1, t.switched)
    }

    @Test fun concurrentlyActivatedAccountDoesNotTrackOrNavigateAgain() = runTest(dispatcher) {
        val r = repo()
        r.switch = { AccountSelection(listOf(a.copy(isActive = true)), SelectionOutcome.Current) }
        val t = Tracker()
        val vm = vm(r, t)
        vm.state.test {
            awaitItem()
            awaitItem()
            vm.select(a.id)
            runCurrent()
            assertFalse(vm.state.value.openHome)
            assertEquals(AccountSwitchUiState.Idle, vm.state.value.selection)
            cancelAndIgnoreRemainingEvents()
        }
        assertEquals(0, t.switched)
    }

    @Test fun deletedAccountRefreshesRowsAndOffersDismissibleFailure() = runTest(dispatcher) {
        val r = repo()
        r.switch = { AccountSelection(listOf(b), SelectionOutcome.Missing) }
        val t = Tracker()
        val vm = vm(r, t)
        vm.state.test {
            awaitItem()
            awaitItem()
            vm.select(a.id)
            runCurrent()
            assertEquals(DrawerAccountsUiState.Content(listOf(b)), vm.state.value.accounts)
            assertEquals(AccountSwitchUiState.Missing, vm.state.value.selection)
            assertFalse(vm.state.value.openHome)
            vm.dismissMissing()
            runCurrent()
            assertEquals(AccountSwitchUiState.Idle, vm.state.value.selection)
            cancelAndIgnoreRemainingEvents()
        }
        assertEquals(0, t.switched)
    }

    @Test fun duplicateConcurrentAndPendingNavigationSelectionsAreIgnored() = runTest(dispatcher) {
        val r = repo()
        val pending = CompletableDeferred<AccountSelection>()
        r.switch = { pending.await() }
        val vm = vm(r)
        vm.state.test {
            awaitItem()
            awaitItem()
            vm.select(a.id)
            vm.select(a.id)
            vm.select(b.id)
            runCurrent()
            assertEquals(listOf(a.id), r.writes)
            assertFalse(vm.state.value.canInteract)
            pending.complete(AccountSelection(listOf(a.copy(isActive = true)), SelectionOutcome.Changed))
            runCurrent()
            vm.select(a.id)
            runCurrent()
            assertEquals(listOf(a.id), r.writes)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test fun switchContinuesWithoutCollectorsAndNavigationSurvivesReturn() = runTest(dispatcher) {
        val r = repo()
        val pending = CompletableDeferred<AccountSelection>()
        r.switch = { pending.await() }
        val vm = vm(r)
        vm.state.test {
            awaitItem()
            awaitItem()
            vm.select(a.id)
            runCurrent()
            cancelAndIgnoreRemainingEvents()
        }
        runCurrent()
        pending.complete(AccountSelection(listOf(a.copy(isActive = true)), SelectionOutcome.Changed))
        runCurrent()
        r.load = { listOf(a.copy(isActive = true)) }
        vm.state.test {
            awaitItem()
            runCurrent()
            assertTrue(vm.state.value.openHome)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test fun clearingViewModelCancelsSwitchWithoutTrackingOrAnError() = runTest(dispatcher) {
        val r = repo()
        var cancelled = false
        r.switch = {
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
            vm.select(a.id)
            runCurrent()
            store.clear()
            runCurrent()
            cancelAndIgnoreRemainingEvents()
        }
        assertTrue(cancelled)
        assertEquals(0, t.switched)
    }

    @Test fun readStartedBeforeSwitchCannotRestoreThePreviousActiveAccount() = runTest(dispatcher) {
        val r = repo()
        val vm = vm(r)
        vm.state.test {
            awaitItem()
            awaitItem()
        }
        runCurrent()
        val pending = CompletableDeferred<List<DrawerAccount>>()
        r.load = { pending.await() }
        val selected = listOf(a.copy(isActive = true), b.copy(isActive = false))
        r.switch = { AccountSelection(selected, SelectionOutcome.Changed) }
        vm.state.test {
            awaitItem()
            runCurrent()
            vm.select(a.id)
            runCurrent()
            pending.complete(listOf(a, b))
            runCurrent()
            assertEquals(DrawerAccountsUiState.Content(selected), vm.state.value.accounts)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test fun retryRevalidatesIdentityAfterAnAccountWasRemovedWhileAway() = runTest(dispatcher) {
        val r = repo()
        r.switch = { throw IOException() }
        val vm = vm(r)
        vm.state.test {
            awaitItem()
            awaitItem()
            vm.select(a.id)
            runCurrent()
            cancelAndIgnoreRemainingEvents()
        }
        runCurrent()
        r.load = { listOf(b) }
        r.switch = { AccountSelection(listOf(b), SelectionOutcome.Missing) }
        vm.state.test {
            awaitItem()
            runCurrent()
            vm.retrySelection()
            runCurrent()
            assertEquals(AccountSwitchUiState.Missing, vm.state.value.selection)
            assertFalse(vm.state.value.openHome)
            cancelAndIgnoreRemainingEvents()
        }
        assertEquals(listOf(a.id, a.id), r.writes)
    }
}
