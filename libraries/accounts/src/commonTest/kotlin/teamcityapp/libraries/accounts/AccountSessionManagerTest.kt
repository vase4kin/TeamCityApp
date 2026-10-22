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

package teamcityapp.libraries.accounts

import kotlin.test.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest

@OptIn(ExperimentalCoroutinesApi::class)
class AccountSessionManagerTest {
    private val alice = AccountIdentity("https://teamcity.example", "alice", false)
    private val bob = AccountIdentity("https://teamcity.example", "bob", false)
    private class MemoryVault : SecureVault {
        var value: String? = null
        var fail = false
        override suspend fun read(): String? = value
        override suspend fun write(value: String) {
            check(!fail) { "Secure storage unavailable" }
            this.value = value
        }
    }

    @Test fun accountKeyEscapesIdentityAndSeparatesUsers() {
        assertNotEquals(alice.key, bob.key)
        val special = AccountIdentity("https://teamcity.example/context/", "a\"b,[]", false)
        assertTrue(special.key.contains("a\\\"b,[]"))
        assertTrue(alice.sameAccount(alice.copy(sslDisabled = true)))
        assertFalse(alice.sameAccount(bob))
    }

    @Test fun secretDebugOutputNeverIncludesPassword() {
        assertFalse(AccountSecret(alice, "never-log-this").toString().contains("never-log-this"))
    }

    @Test fun emptyStorageRestoresSignedOut() = runTest {
        val manager = AccountSessionManager(VaultAccountStorage(MemoryVault()), backgroundScope.coroutineContext)
        manager.restore()
        assertEquals(AccountSessionState.SignedOut, manager.state.value)
    }

    @Test fun switchingUsersOnSameServerCancelsOldWorkAndRejectsLateResults() = runTest {
        val manager = AccountSessionManager(VaultAccountStorage(MemoryVault()), backgroundScope.coroutineContext)
        manager.saveAndActivate(AccountSecret(alice, "a"))
        val old = (manager.state.value as AccountSessionState.Active).scope
        val cancelled = CompletableDeferred<Unit>()
        old.coroutineScope.launch {
            try {
                awaitCancellation()
            } finally {
                cancelled.complete(Unit)
            }
        }
        runCurrent()
        manager.saveAndActivate(AccountSecret(bob, "b"))
        runCurrent()
        assertTrue(cancelled.isCompleted)
        assertFailsWith<CancellationException> { old.requireCurrent() }
        assertEquals(bob, (manager.state.value as AccountSessionState.Active).scope.identity)
    }

    @Test fun duplicateAccountIgnoresTheTransportConsentFlag() = runTest {
        val manager = AccountSessionManager(VaultAccountStorage(MemoryVault()), backgroundScope.coroutineContext)
        assertTrue(manager.saveAndActivate(AccountSecret(alice, "a")))
        assertFalse(manager.saveAndActivate(AccountSecret(alice.copy(sslDisabled = true), "a")))
        assertEquals(listOf(alice), manager.accounts())
    }

    @Test fun failedSecureWriteKeepsPreviousAccountAndScope() = runTest {
        val vault = MemoryVault()
        val manager = AccountSessionManager(VaultAccountStorage(vault), backgroundScope.coroutineContext)
        manager.saveAndActivate(AccountSecret(alice, "a"))
        val old = (manager.state.value as AccountSessionState.Active).scope
        vault.fail = true
        assertFailsWith<IllegalStateException> { manager.saveAndActivate(AccountSecret(bob, "b")) }
        assertSame(old, (manager.state.value as AccountSessionState.Active).scope)
        old.requireCurrent()
    }

    @Test fun restoreReadsPersistedSelectionAndSwitchingCreatesFreshGeneration() = runTest {
        val storage = VaultAccountStorage(MemoryVault())
        val first = AccountSessionManager(storage, backgroundScope.coroutineContext)
        first.saveAndActivate(AccountSecret(alice, "a"))
        first.saveAndActivate(AccountSecret(bob, "b"))
        first.activate(alice.key)
        val before = (first.state.value as AccountSessionState.Active).scope
        first.activate(alice.key)
        assertNotEquals(before.generation, (first.state.value as AccountSessionState.Active).scope.generation)
        assertFailsWith<CancellationException> { before.requireCurrent() }
        first.close()
        val restored = AccountSessionManager(storage, backgroundScope.coroutineContext)
        restored.restore()
        val scope = (restored.state.value as AccountSessionState.Active).scope
        assertEquals(alice, scope.identity)
        assertEquals("a", restored.secret(scope).password)
    }

    @Test fun logoutRetainsAccountsButClearsActiveSelectionAcrossRelaunch() = runTest {
        val storage = VaultAccountStorage(MemoryVault())
        val manager = AccountSessionManager(storage, backgroundScope.coroutineContext)
        manager.saveAndActivate(AccountSecret(alice, "a"))
        val old = (manager.state.value as AccountSessionState.Active).scope
        manager.signOut()
        assertFailsWith<CancellationException> { old.requireCurrent() }
        assertEquals(AccountSessionState.SignedOut, manager.state.value)
        val restored = AccountSessionManager(storage, backgroundScope.coroutineContext)
        restored.restore()
        assertEquals(AccountSessionState.SignedOut, restored.state.value)
        assertEquals(listOf(alice), restored.accounts())
    }

    @Test fun removingActiveAccountSelectsRemainingAccountAndLastRemovalSignsOut() = runTest {
        val manager = AccountSessionManager(VaultAccountStorage(MemoryVault()), backgroundScope.coroutineContext)
        manager.saveAndActivate(AccountSecret(alice, "a"))
        manager.saveAndActivate(AccountSecret(bob, "b"))
        val previous = (manager.state.value as AccountSessionState.Active).scope
        manager.remove(bob.key)
        assertFailsWith<CancellationException> { previous.requireCurrent() }
        assertEquals(alice, (manager.state.value as AccountSessionState.Active).scope.identity)
        manager.remove(alice.key)
        assertEquals(AccountSessionState.SignedOut, manager.state.value)
    }

    @Test fun malformedVaultFailsWithoutSilentlyDeletingAccounts() = runTest {
        val vault = MemoryVault().apply { value = "not json" }
        assertFails { VaultAccountStorage(vault).accounts() }
        assertEquals("not json", vault.value)
    }

    @Test fun cancellationDuringAnAtomicWriteKeepsPersistedAndPublishedAccountsConsistent() = runTest {
        val committed = CompletableDeferred<Unit>()
        val release = CompletableDeferred<Unit>()
        var stored: String? = null
        val vault = object : SecureVault {
            override suspend fun read(): String? = stored
            override suspend fun write(value: String) {
                stored = value
                committed.complete(Unit)
                release.await()
            }
        }
        val manager = AccountSessionManager(VaultAccountStorage(vault), backgroundScope.coroutineContext)
        val request = launch { manager.saveAndActivate(AccountSecret(alice, "a")) }
        committed.await()
        request.cancel()
        release.complete(Unit)
        request.join()
        assertEquals(alice, (manager.state.value as AccountSessionState.Active).scope.identity)
        assertEquals(alice, VaultAccountStorage(vault).active()!!.identity)
    }
}
