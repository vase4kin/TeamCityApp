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

package teamcityapp.shared

import androidx.lifecycle.ViewModelStore
import kotlin.test.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import teamcityapp.libraries.accounts.*
import teamcityapp.libraries.list_state.ListUiState

@OptIn(ExperimentalCoroutinesApi::class)
class AccountsViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val store = ViewModelStore()
    private class Vault : SecureVault {
        var value: String? = null
        var reads = 0
        var writes = 0
        var fail = false
        var blockReads: CompletableDeferred<Unit>? = null
        var blockWrites: CompletableDeferred<Unit>? = null
        override suspend fun read(): String? {
            reads++
            blockReads?.await()
            check(!fail)
            return value
        }
        override suspend fun write(value: String) {
            writes++
            blockWrites?.await()
            check(!fail)
            this.value = value
        }
    }
    private val alice = AccountIdentity("https://teamcity.example", "alice", false)
    private val bob = alice.copy(userName = "bob")

    @BeforeTest fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest fun cleanup() {
        store.clear()
        Dispatchers.resetMain()
    }
    private fun vm(manager: AccountSessionManager) = AccountsViewModel(manager).also { store.put("accounts", it) }
    private fun TestScope.observe(vm: AccountsViewModel) = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.state.collect {} }

    @Test fun accountLoadingIsCollectionDrivenAndRetainsCompletedRowsAcrossGaps() = runTest(dispatcher) {
        val vault = Vault()
        val manager = AccountSessionManager(VaultAccountStorage(vault), backgroundScope.coroutineContext)
        manager.saveAndActivate(AccountSecret(alice, "a"))
        vault.reads = 0
        val vm = vm(manager)
        runCurrent()
        assertEquals(0, vault.reads)
        var collector = observe(vm)
        runCurrent()
        assertEquals(listOf(alice), (vm.state.value.list as ListUiState.Content).items)
        val reads = vault.reads
        collector.cancel()
        runCurrent()
        collector = observe(vm)
        runCurrent()
        assertEquals(reads, vault.reads)
        collector.cancel()
    }

    @Test fun initialFailureAndEmptyStorageHaveDistinctRetryableStates() = runTest(dispatcher) {
        val vault = Vault().apply { fail = true }
        val manager = AccountSessionManager(VaultAccountStorage(vault), backgroundScope.coroutineContext)
        val vm = vm(manager)
        observe(vm)
        runCurrent()
        assertEquals(ListUiState.Error, vm.state.value.list)
        vault.fail = false
        vm.retry()
        runCurrent()
        assertEquals(ListUiState.Empty(), vm.state.value.list)
    }

    @Test fun selectingAnotherUserOnTheSameServerReplacesTheActiveScope() = runTest(dispatcher) {
        val manager = AccountSessionManager(VaultAccountStorage(Vault()), backgroundScope.coroutineContext)
        manager.saveAndActivate(AccountSecret(alice, "a"))
        manager.saveAndActivate(AccountSecret(bob, "b"))
        val previous = (manager.state.value as AccountSessionState.Active).scope
        val vm = vm(manager)
        observe(vm)
        runCurrent()
        vm.select(alice.key)
        runCurrent()
        assertEquals(alice.key, vm.state.value.activeKey)
        assertFailsWith<CancellationException> { previous.requireCurrent() }
    }

    @Test fun failedMutationKeepsAccountsAndRetriesTheExactRequestedRemoval() = runTest(dispatcher) {
        val vault = Vault()
        val manager = AccountSessionManager(VaultAccountStorage(vault), backgroundScope.coroutineContext)
        manager.saveAndActivate(AccountSecret(alice, "a"))
        manager.saveAndActivate(AccountSecret(bob, "b"))
        val vm = vm(manager)
        observe(vm)
        runCurrent()
        vault.fail = true
        vm.remove(bob.key)
        runCurrent()
        assertTrue(vm.state.value.mutationFailed)
        assertEquals(2, (vm.state.value.list as ListUiState.Content).items.size)
        assertEquals(bob.key, vm.state.value.activeKey)
        vault.fail = false
        vm.retry()
        runCurrent()
        assertFalse(vm.state.value.mutationFailed)
        assertEquals(listOf(alice), (vm.state.value.list as ListUiState.Content).items)
        assertEquals(alice.key, vm.state.value.activeKey)
    }

    @Test fun repeatedSignOutWhileSavingRunsOneMutationAndKeepsSavedAccounts() = runTest(dispatcher) {
        val vault = Vault()
        val manager = AccountSessionManager(VaultAccountStorage(vault), backgroundScope.coroutineContext)
        manager.saveAndActivate(AccountSecret(alice, "a"))
        val vm = vm(manager)
        observe(vm)
        runCurrent()
        val writes = vault.writes
        vault.blockWrites = CompletableDeferred()
        vm.signOut()
        runCurrent()
        assertTrue(vm.state.value.busy)
        vm.signOut()
        runCurrent()
        assertEquals(writes + 1, vault.writes)
        vault.blockWrites!!.complete(Unit)
        runCurrent()
        assertNull(vm.state.value.activeKey)
        assertEquals(listOf(alice), (vm.state.value.list as ListUiState.Content).items)
    }

    @Test fun leavingDuringAnUnfinishedLoadCancelsWithoutTurningCancellationIntoAnError() = runTest(dispatcher) {
        val vault = Vault()
        val manager = AccountSessionManager(VaultAccountStorage(vault), backgroundScope.coroutineContext)
        manager.saveAndActivate(AccountSecret(alice, "a"))
        vault.blockReads = CompletableDeferred()
        val vm = vm(manager)
        val first = observe(vm)
        runCurrent()
        assertEquals(ListUiState.Loading, vm.state.value.list)
        first.cancel()
        runCurrent()
        vault.blockReads = null
        observe(vm)
        runCurrent()
        assertEquals(listOf(alice), (vm.state.value.list as ListUiState.Content).items)
        assertFalse(vm.state.value.mutationFailed)
    }
}
