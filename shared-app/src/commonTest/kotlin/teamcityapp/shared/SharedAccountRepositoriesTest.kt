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

import kotlin.test.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import teamcityapp.features.drawer.api.*
import teamcityapp.features.manage_accounts.api.*
import teamcityapp.libraries.accounts.*

class SharedAccountRepositoriesTest {
    private class Vault : SecureVault {
        var value: String? = null
        var failWrites = false
        override suspend fun read() = value
        override suspend fun write(value: String) {
            check(!failWrites)
            this.value = value
        }
    }
    private val guest = AccountIdentity("https://teamcity.example", "Guest user", true)
    private val named = guest.copy(guest = false)
    private fun active(sessions: AccountSessionManager) = (sessions.state.value as? AccountSessionState.Active)?.scope

    @Test fun sameServerAndNameRetainDistinctGuestAndMemberKeysForSwitchAndRemoval() = runTest {
        val sessions = AccountSessionManager(VaultAccountStorage(Vault()), backgroundScope.coroutineContext)
        sessions.saveAndActivate(AccountSecret(guest))
        sessions.saveAndActivate(AccountSecret(named, "password"))
        val switcher = SharedAccountSwitcherRepository(sessions)
        val managed = SharedManageAccountsRepository(sessions)
        val rows = switcher.accounts()
        assertEquals(2, rows.size)
        assertEquals(2, rows.map { it.id.identityKey }.toSet().size)
        val old = active(sessions)!!
        val result = switcher.select(rows.single { it.id.identityKey == guest.key }.id)
        assertEquals(SelectionOutcome.Changed, result.outcome)
        assertEquals(guest, active(sessions)?.identity)
        assertFailsWith<CancellationException> { old.requireCurrent() }
        val removal = managed.remove(managed.accounts().single { it.id.identityKey == named.key }.id)
        assertEquals(AccountRemovalOutcome.Stay, removal.outcome)
        assertTrue(removal.removed)
        assertEquals(listOf(guest), sessions.accounts())
        assertEquals(guest, active(sessions)?.identity)
    }

    @Test fun missingOrMismatchedPublicIdentityNeverSelectsOrRemovesAnotherAccount() = runTest {
        val sessions = AccountSessionManager(VaultAccountStorage(Vault()), backgroundScope.coroutineContext)
        sessions.saveAndActivate(AccountSecret(guest))
        val scope = active(sessions)
        val switcher = SharedAccountSwitcherRepository(sessions)
        assertEquals(SelectionOutcome.Missing, switcher.select(DrawerAccountId(guest.serverUrl, guest.userName, "old-key")).outcome)
        val removed = SharedManageAccountsRepository(sessions).remove(ManagedAccountId("https://other.example", guest.userName, guest.key))
        assertFalse(removed.removed)
        assertSame(scope, active(sessions))
        assertEquals(listOf(guest), sessions.accounts())
    }

    @Test fun currentSelectionDoesNotReplaceTheAccountGeneration() = runTest {
        val sessions = AccountSessionManager(VaultAccountStorage(Vault()), backgroundScope.coroutineContext)
        sessions.saveAndActivate(AccountSecret(guest))
        val scope = active(sessions)
        val repo = SharedAccountSwitcherRepository(sessions)
        assertEquals(SelectionOutcome.Current, repo.select(repo.accounts().single().id).outcome)
        assertSame(scope, active(sessions))
    }

    @Test fun activeRemovalSelectsTheRemainingAccountAndLastRemovalReturnsToLogin() = runTest {
        val sessions = AccountSessionManager(VaultAccountStorage(Vault()), backgroundScope.coroutineContext)
        sessions.saveAndActivate(AccountSecret(guest))
        sessions.saveAndActivate(AccountSecret(named, "password"))
        val old = active(sessions)!!
        val repo = SharedManageAccountsRepository(sessions)
        assertEquals(AccountRemovalOutcome.Home, repo.remove(repo.accounts().single { it.isActive }.id).outcome)
        assertFailsWith<CancellationException> { old.requireCurrent() }
        assertEquals(guest, active(sessions)?.identity)
        assertEquals(AccountRemovalOutcome.Login, repo.remove(repo.accounts().single().id).outcome)
        assertEquals(AccountSessionState.SignedOut, sessions.state.value)
        assertTrue(sessions.accounts().isEmpty())
    }

    @Test fun failedPersistenceKeepsTheOldGenerationAndCanBeRetried() = runTest {
        val vault = Vault()
        val sessions = AccountSessionManager(VaultAccountStorage(vault), backgroundScope.coroutineContext)
        sessions.saveAndActivate(AccountSecret(guest))
        sessions.saveAndActivate(AccountSecret(named, "password"))
        val old = active(sessions)
        val repo = SharedAccountSwitcherRepository(sessions)
        val target = repo.accounts().single { !it.isActive }.id
        vault.failWrites = true
        assertFailsWith<IllegalStateException> { repo.select(target) }
        assertSame(old, active(sessions))
        vault.failWrites = false
        assertEquals(SelectionOutcome.Changed, repo.select(target).outcome)
    }

    @Test fun signOutKeepsSavedAccountsForTheSharedAccountPicker() = runTest {
        val sessions = AccountSessionManager(VaultAccountStorage(Vault()), backgroundScope.coroutineContext)
        sessions.saveAndActivate(AccountSecret(guest))
        val repo = SharedAccountSwitcherRepository(sessions)
        repo.signOut()
        assertEquals(AccountSessionState.SignedOut, sessions.state.value)
        assertEquals(listOf(guest), sessions.accounts())
        assertFalse(repo.accounts().single().isActive)
        assertEquals(SelectionOutcome.Changed, repo.select(repo.accounts().single().id).outcome)
    }

    @Test fun removingTheLastSavedAccountWhileSignedOutRequestsTheLoginDestination() = runTest {
        val sessions = AccountSessionManager(VaultAccountStorage(Vault()), backgroundScope.coroutineContext)
        sessions.saveAndActivate(AccountSecret(guest))
        sessions.signOut()
        val repo = SharedManageAccountsRepository(sessions)
        assertEquals(AccountRemovalOutcome.Login, repo.remove(repo.accounts().single().id).outcome)
        assertEquals(AccountSessionState.SignedOut, sessions.state.value)
        assertTrue(sessions.accounts().isEmpty())
    }
}
