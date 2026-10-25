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

import teamcityapp.features.drawer.api.*
import teamcityapp.features.manage_accounts.api.*
import teamcityapp.libraries.accounts.*

/** Maps public account identities only; credentials and mutations remain in the session manager. */
internal class NativeAccountSwitcherRepository(private val sessions: AccountSessionManager) : DrawerRepository {
    override val supportsSignOut = true
    override suspend fun accounts(): List<DrawerAccount> {
        val active = (sessions.state.value as? AccountSessionState.Active)?.scope?.identity?.key
        return sessions.accounts().map { DrawerAccount(DrawerAccountId(it.serverUrl, it.userName, it.key), it.key == active, it.sslDisabled) }
    }
    override suspend fun select(id: DrawerAccountId): AccountSelection {
        val identity = sessions.accounts().singleOrNull { it.serverUrl == id.serverUrl && it.userName == id.userName && it.key == id.identityKey }
            ?: return AccountSelection(accounts(), SelectionOutcome.Missing)
        val active = (sessions.state.value as? AccountSessionState.Active)?.scope?.identity?.key
        if (active == identity.key) return AccountSelection(accounts(), SelectionOutcome.Current)
        sessions.activate(identity.key)
        return AccountSelection(accounts(), SelectionOutcome.Changed)
    }
    override suspend fun signOut() = sessions.signOut()
}

internal class NativeManageAccountsRepository(private val sessions: AccountSessionManager) : ManageAccountsRepository {
    override suspend fun accounts(): List<ManagedAccount> {
        val active = (sessions.state.value as? AccountSessionState.Active)?.scope?.identity?.key
        return sessions.accounts().map { ManagedAccount(ManagedAccountId(it.serverUrl, it.userName, it.key), it.key == active, it.sslDisabled) }
    }
    override suspend fun remove(id: ManagedAccountId): AccountRemoval {
        val identity = sessions.accounts().singleOrNull { it.serverUrl == id.serverUrl && it.userName == id.userName && it.key == id.identityKey }
        val active = (sessions.state.value as? AccountSessionState.Active)?.scope?.identity?.key
        if (identity != null) sessions.remove(identity.key)
        val remaining = accounts()
        val outcome = when {
            remaining.isEmpty() -> AccountRemovalOutcome.Login
            identity?.key == active -> AccountRemovalOutcome.Home
            else -> AccountRemovalOutcome.Stay
        }
        return AccountRemoval(remaining, outcome, identity != null)
    }
}
