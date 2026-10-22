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

package teamcityapp.libraries.session

import com.github.vase4kin.teamcityapp.storage.SharedUserStorage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import teamcityapp.libraries.accounts.*
import teamcityapp.libraries.security.CryptoManager
import teamcityapp.libraries.storage.models.UserAccount

/** Adapts the installed Android encrypted JSON format without exporting its ciphertext to common UI. */
class LegacyAndroidAccountStorage(
    private val storage: SharedUserStorage,
    private val crypto: CryptoManager,
    private val dispatcher: CoroutineDispatcher
) : AccountStorage {
    override suspend fun accounts(): List<AccountIdentity> = withContext(dispatcher) {
        storage.userAccounts.map { it.sharedAccountIdentity() }
    }
    override suspend fun active(): AccountSecret? = withContext(dispatcher) {
        storage.userAccounts.firstOrNull { it.isActive }?.let(::decrypt)
    }
    override suspend fun secret(key: String): AccountSecret? = withContext(dispatcher) {
        storage.userAccounts.firstOrNull { it.sharedAccountIdentity().key == key }?.let(::decrypt)
    }
    override suspend fun saveAndActivate(account: AccountSecret) = withContext(dispatcher) {
        val bytes = if (account.identity.guest) {
            byteArrayOf()
        } else {
            crypto.encrypt(account.password).also {
                check(!crypto.isFailed(it)) { "Account encryption failed" }
            }
        }
        val current = snapshot()
        val existing = current.firstOrNull { it.sharedAccountIdentity().sameAccount(account.identity) }
        val replacement = UserAccount(account.identity.serverUrl, account.identity.userName, bytes, account.identity.guest, true).apply {
            isSslDisabled = account.identity.sslDisabled
            buildTypeIds = ArrayList(existing?.buildTypeIds.orEmpty())
        }
        val result = current.filterNot { it.sharedAccountIdentity().sameAccount(account.identity) }
        result.forEach { it.setIsActive(false) }
        storage.replaceAccountSnapshot(result + replacement)
    }
    override suspend fun activate(key: String) = withContext(dispatcher) {
        val accounts = snapshot()
        require(accounts.any { it.sharedAccountIdentity().key == key }) { "Account no longer exists" }
        accounts.forEach { it.setIsActive(it.sharedAccountIdentity().key == key) }
        storage.replaceAccountSnapshot(accounts)
    }
    override suspend fun remove(key: String) = withContext(dispatcher) {
        val accounts = snapshot()
        val activeRemoved = accounts.any { it.sharedAccountIdentity().key == key && it.isActive }
        val remaining = accounts.filterNot { it.sharedAccountIdentity().key == key }
        if (activeRemoved) remaining.firstOrNull()?.setIsActive(true)
        storage.replaceAccountSnapshot(remaining)
    }
    override suspend fun signOut() = withContext(dispatcher) {
        val accounts = snapshot()
        accounts.forEach { it.setIsActive(false) }
        storage.replaceAccountSnapshot(accounts)
    }
    private fun decrypt(account: UserAccount): AccountSecret {
        val identity = account.sharedAccountIdentity()
        val bytes = if (identity.guest) {
            byteArrayOf()
        } else {
            crypto.decrypt(account.passwordAsBytes).also {
                check(!crypto.isFailed(it)) { "Account decryption failed" }
            }
        }
        return AccountSecret(identity, bytes.decodeToString())
    }
    private fun snapshot(): List<UserAccount> = storage.userAccounts.map { old ->
        UserAccount(old.teamcityUrl, old.userName, old.passwordAsBytes.copyOf(), old.isGuestUser, old.isActive).apply {
            isSslDisabled = old.isSslDisabled
            buildTypeIds = ArrayList(old.buildTypeIds)
        }
    }
}
