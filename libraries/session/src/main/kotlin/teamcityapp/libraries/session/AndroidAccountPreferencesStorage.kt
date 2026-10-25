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

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import teamcityapp.libraries.accounts.*

/**
 * Keeps installed encrypted account JSON intact. Public preferences migrate lazily to DataStore.
 * After migration DataStore is authoritative; a restarted read repairs an interrupted legacy
 * favorites mirror. Passwords and account mutation policy remain in the injected account store.
 */
class AndroidAccountPreferencesStorage(
    private val accounts: AccountStorage,
    private val legacy: SharedUserStorage,
    private val preferences: DataStore<Preferences>,
    private val main: CoroutineDispatcher,
    private val initialPreferences: suspend () -> AccountPreferences = { AccountPreferences() }
) : AccountStorage by accounts,
    AccountPreferencesStorage {
    private val mutex = Mutex()
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun preferences(key: String): AccountPreferences = mutex.withLock {
        read(key).also { mirror(key, it.favoriteIds) }
    }

    override suspend fun updatePreferences(key: String, transform: (AccountPreferences) -> AccountPreferences): AccountPreferences = mutex.withLock {
        val next = transform(read(key)).let { it.copy(favoriteIds = it.favoriteIds.distinct()) }
        // Finish the local durable update even if its destination leaves during persistence.
        withContext(NonCancellable) {
            preferences.edit { it[preferenceKey(key)] = json.encodeToString(next) }
            mirror(key, next.favoriteIds)
        }
        next
    }

    private suspend fun read(key: String): AccountPreferences {
        require(accounts.accounts().any { it.key == key }) { "Account no longer exists" }
        val stored = preferences.data.first()[preferenceKey(key)]
        if (stored != null) return json.decodeFromString<AccountPreferences>(stored)
        val initial = initialPreferences()
        return withContext(main) {
            val account = requireNotNull(legacy.userAccounts.singleOrNull { it.sharedAccountIdentity().key == key })
            initial.copy(favoriteIds = account.buildTypeIds.toList())
        }
    }

    private suspend fun mirror(key: String, favorites: List<String>) = withContext(main) {
        val rows = legacy.userAccounts
        val selected = rows.singleOrNull { it.sharedAccountIdentity().key == key } ?: return@withContext
        if (selected.buildTypeIds == favorites) return@withContext
        // Copy only the public favorites field, preserving ciphertext and every installed field.
        val previous = selected.buildTypeIds
        selected.buildTypeIds = ArrayList(favorites)
        try {
            legacy.replaceAccountSnapshot(rows)
        } catch (failure: Exception) {
            selected.buildTypeIds = previous
            throw failure
        }
    }

    override suspend fun saveAndActivate(account: AccountSecret) = mutex.withLock {
        if (accounts.accounts().none { it.sameAccount(account.identity) }) {
            // A genuinely new account must not inherit metadata left by an interrupted removal.
            preferences.edit { it.remove(stringPreferencesKey("account:${account.identity.copy(sslDisabled = false).key}")) }
        }
        accounts.saveAndActivate(account)
    }

    /** Integration fixtures seed installed preferences afresh before the first common read. */
    @androidx.annotation.VisibleForTesting(otherwise = androidx.annotation.VisibleForTesting.PRIVATE)
    suspend fun resetPreferencesForTesting() = mutex.withLock {
        preferences.edit { it.clear() }
    }

    private suspend fun preferenceKey(key: String): Preferences.Key<String> {
        val identity = requireNotNull(accounts.accounts().singleOrNull { it.key == key })
        // TLS consent may change without changing account ownership or losing saved preferences.
        return stringPreferencesKey("account:${identity.copy(sslDisabled = false).key}")
    }
}
