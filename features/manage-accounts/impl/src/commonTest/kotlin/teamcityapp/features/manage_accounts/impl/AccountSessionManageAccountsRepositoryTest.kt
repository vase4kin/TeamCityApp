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

package teamcityapp.features.manage_accounts.impl

import kotlin.test.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import teamcityapp.features.manage_accounts.api.*
import teamcityapp.libraries.accounts.*

class AccountSessionManageAccountsRepositoryTest {
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

    @Test fun activeRemovalSelectsTheRemainingAccountAndLastRemovalReturnsToLogin() = runTest {
        val sessions = AccountSessionManager(VaultAccountStorage(Vault()), backgroundScope.coroutineContext)
        sessions.saveAndActivate(AccountSecret(guest))
        sessions.saveAndActivate(AccountSecret(named, "password"))
        val old = active(sessions)!!
        val repo = AccountSessionManageAccountsRepository(sessions)
        assertEquals(AccountRemovalOutcome.Home, repo.remove(repo.accounts().single { it.isActive }.id).outcome)
        assertFailsWith<CancellationException> { old.requireCurrent() }
        assertEquals(guest, active(sessions)?.identity)
        assertEquals(AccountRemovalOutcome.Login, repo.remove(repo.accounts().single().id).outcome)
        assertEquals(AccountSessionState.SignedOut, sessions.state.value)
        assertTrue(sessions.accounts().isEmpty())
    }

    @Test fun removingTheLastSavedAccountWhileSignedOutRequestsTheLoginDestination() = runTest {
        val sessions = AccountSessionManager(VaultAccountStorage(Vault()), backgroundScope.coroutineContext)
        sessions.saveAndActivate(AccountSecret(guest))
        sessions.signOut()
        val repo = AccountSessionManageAccountsRepository(sessions)
        assertEquals(AccountRemovalOutcome.Login, repo.remove(repo.accounts().single().id).outcome)
        assertEquals(AccountSessionState.SignedOut, sessions.state.value)
        assertTrue(sessions.accounts().isEmpty())
    }
}
