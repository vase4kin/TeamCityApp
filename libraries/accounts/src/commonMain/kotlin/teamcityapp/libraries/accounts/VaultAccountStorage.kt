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

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** Secure platform storage owns encryption and durable atomic replacement of this payload. */
interface SecureVault {
    suspend fun read(): String?
    suspend fun write(value: String)
}

@Serializable
private class StoredAccount(val identity: AccountIdentity, val password: String, val preferences: AccountPreferences = AccountPreferences())

@Serializable
private class StoredVault(val accounts: List<StoredAccount> = emptyList(), val activeKey: String? = null)

class VaultAccountStorage(private val vault: SecureVault) :
    AccountStorage,
    AccountPreferencesStorage {
    private val mutex = Mutex()
    private val json = Json { ignoreUnknownKeys = true }
    private suspend fun read(): StoredVault = vault.read()?.let { json.decodeFromString<StoredVault>(it) } ?: StoredVault()
    private suspend fun save(value: StoredVault) = vault.write(json.encodeToString(value))
    override suspend fun accounts(): List<AccountIdentity> = mutex.withLock { read().accounts.map { it.identity } }
    override suspend fun active(): AccountSecret? = mutex.withLock {
        val stored = read()
        stored.accounts.firstOrNull { it.identity.key == stored.activeKey }?.let { AccountSecret(it.identity, it.password) }
    }
    override suspend fun secret(key: String): AccountSecret? = mutex.withLock {
        read().accounts.firstOrNull { it.identity.key == key }?.let { AccountSecret(it.identity, it.password) }
    }
    private fun StoredVault.records() = AccountRecords(accounts, activeKey) { it.identity }
    private suspend fun save(records: AccountRecords<StoredAccount>) = save(StoredVault(records.accounts, records.activeKey))
    override suspend fun saveAndActivate(account: AccountSecret) = mutex.withLock {
        val current = read()
        save(current.records().saveAndActivate(StoredAccount(account.identity, account.password, current.accounts.firstOrNull { it.identity.sameAccount(account.identity) }?.preferences ?: AccountPreferences())))
    }
    override suspend fun activate(key: String) = mutex.withLock {
        val current = read()
        save(current.records().activate(key))
    }
    override suspend fun remove(key: String) = mutex.withLock {
        val current = read()
        save(current.records().remove(key))
    }
    override suspend fun signOut() = mutex.withLock {
        val current = read()
        save(current.records().signOut())
    }
    override suspend fun preferences(key: String): AccountPreferences = mutex.withLock {
        requireNotNull(read().accounts.firstOrNull { it.identity.key == key }).preferences
    }
    override suspend fun updatePreferences(key: String, transform: (AccountPreferences) -> AccountPreferences): AccountPreferences = mutex.withLock {
        val current = read()
        val account = requireNotNull(current.accounts.firstOrNull { it.identity.key == key })
        val next = transform(account.preferences).let { it.copy(favoriteIds = it.favoriteIds.distinct()) }
        save(StoredVault(current.accounts.map { if (it.identity.key == key) StoredAccount(it.identity, it.password, next) else it }, current.activeKey))
        next
    }
}
