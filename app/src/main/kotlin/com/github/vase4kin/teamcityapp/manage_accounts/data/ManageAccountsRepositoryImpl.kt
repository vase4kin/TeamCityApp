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

package com.github.vase4kin.teamcityapp.manage_accounts.data

import com.github.vase4kin.teamcityapp.account.session.AccountSession
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import teamcityapp.features.manage_accounts.api.*
import teamcityapp.libraries.cache_manager.CacheManager
import teamcityapp.libraries.coroutines.IoDispatcher
import teamcityapp.libraries.storage.Storage
import teamcityapp.libraries.storage.models.UserAccount

/** Keeps the shared account store and encrypted persisted records behind a coroutine adapter. */
@Singleton
class ManageAccountsRepositoryImpl @Inject constructor(
    private val storage: Storage,
    private val cacheManager: CacheManager,
    private val session: AccountSession,
    @IoDispatcher private val dispatcher: CoroutineDispatcher
) : ManageAccountsRepository {
    private val mutex = Mutex()
    override suspend fun accounts(): List<ManagedAccount> = withContext(dispatcher) {
        mutex.withLock { storage.userAccounts.map(UserAccount::toManagedAccount) }
    }
    override suspend fun remove(id: ManagedAccountId): AccountRemoval = withContext(dispatcher) {
        mutex.withLock {
            coroutineContext.ensureActive()
            val current = storage.userAccounts.toList()
            val account = current.firstOrNull { it.teamcityUrl == id.serverUrl && it.userName == id.userName }
            if (account != null) storage.removeUserAccount(account)
            val remaining = storage.userAccounts.toList()
            when {
                remaining.isEmpty() -> {
                    session.clear()
                    val cleanupFailed = try {
                        cacheManager.evictAllCache()
                        false
                    } catch (error: CancellationException) {
                        throw error
                    } catch (error: Exception) {
                        true
                    }
                    AccountRemoval(emptyList(), if (cleanupFailed) AccountRemovalOutcome.CleanupFailed else AccountRemovalOutcome.Login, account != null)
                }

                account?.isActive == true || remaining.none { it.isActive } -> {
                    session.clear()
                    storage.setOtherUserActive()
                    AccountRemoval(storage.userAccounts.map(UserAccount::toManagedAccount), AccountRemovalOutcome.Home, account != null)
                }

                else -> AccountRemoval(remaining.map(UserAccount::toManagedAccount), AccountRemovalOutcome.Stay, account != null)
            }
        }
    }
}
private fun UserAccount.toManagedAccount() = ManagedAccount(ManagedAccountId(teamcityUrl, userName), isActive, isSslDisabled)
