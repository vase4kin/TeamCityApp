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

package com.github.vase4kin.teamcityapp.drawer.data
import com.github.vase4kin.teamcityapp.account.session.AccountSession
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import teamcityapp.features.drawer.api.*
import teamcityapp.libraries.coroutines.IoDispatcher
import teamcityapp.libraries.storage.Storage
import teamcityapp.libraries.storage.models.UserAccount

/** Coroutine boundary around the existing encrypted account store. */
@Singleton
class DrawerRepositoryImpl @Inject constructor(
    private val storage: Storage,
    private val session: AccountSession,
    @IoDispatcher private val dispatcher: CoroutineDispatcher
) : DrawerRepository {
    private val mutex = Mutex()
    override suspend fun accounts(): List<DrawerAccount> = withContext(dispatcher) {
        mutex.withLock { storage.userAccounts.map(UserAccount::toDrawerAccount) }
    }
    override suspend fun select(id: DrawerAccountId): AccountSelection = withContext(dispatcher) {
        mutex.withLock {
            coroutineContext.ensureActive()
            val account = storage.userAccounts.firstOrNull { it.teamcityUrl == id.serverUrl && it.userName == id.userName }
            val outcome = when {
                account == null -> SelectionOutcome.Missing

                account.isActive -> SelectionOutcome.Current

                else -> {
                    storage.setUserActive(id.serverUrl, id.userName)
                    session.clear()
                    SelectionOutcome.Changed
                }
            }
            AccountSelection(storage.userAccounts.map(UserAccount::toDrawerAccount), outcome)
        }
    }
}
private fun UserAccount.toDrawerAccount() = DrawerAccount(DrawerAccountId(teamcityUrl, userName), isActive, isSslDisabled)
