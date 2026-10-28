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

import kotlin.test.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import teamcityapp.features.drawer.api.*
import teamcityapp.libraries.accounts.*

class AccountSessionDrawerRepositoryTest {
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

    @Test fun currentSelectionDoesNotReplaceTheAccountGeneration() = runTest {
        val sessions = AccountSessionManager(VaultAccountStorage(Vault()), backgroundScope.coroutineContext)
        sessions.saveAndActivate(AccountSecret(guest))
        val scope = active(sessions)
        val repo = AccountSessionDrawerRepository(sessions)
        assertEquals(SelectionOutcome.Current, repo.select(repo.accounts().single().id).outcome)
        assertSame(scope, active(sessions))
    }

    @Test fun failedPersistenceKeepsTheOldGenerationAndCanBeRetried() = runTest {
        val vault = Vault()
        val sessions = AccountSessionManager(VaultAccountStorage(vault), backgroundScope.coroutineContext)
        sessions.saveAndActivate(AccountSecret(guest))
        sessions.saveAndActivate(AccountSecret(named, "password"))
        val old = active(sessions)
        val repo = AccountSessionDrawerRepository(sessions)
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
        val repo = AccountSessionDrawerRepository(sessions)
        repo.signOut()
        assertEquals(AccountSessionState.SignedOut, sessions.state.value)
        assertEquals(listOf(guest), sessions.accounts())
        assertFalse(repo.accounts().single().isActive)
        assertEquals(SelectionOutcome.Changed, repo.select(repo.accounts().single().id).outcome)
    }
}
