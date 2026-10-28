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
import kotlinx.coroutines.test.runTest

class AccountPreferencesStorageTest {
    private class Vault : SecureVault {
        var value: String? = null
        var fail = false
        override suspend fun read() = value
        override suspend fun write(value: String) {
            check(!fail)
            this.value = value
        }
    }

    @Test fun phaseTwoVaultWithoutPreferencesMigratesWithoutChangingCredentialsOrSelection() = runTest {
        val vault = Vault().apply { value = """{"accounts":[{"identity":{"serverUrl":"https://ci.example/","userName":"alice","guest":false},"password":"fixture"}],"activeKey":"[\"https://ci.example/\",\"alice\",false,false]"}""" }
        val storage = VaultAccountStorage(vault)
        val account = assertNotNull(storage.active())
        assertEquals("fixture", account.password)
        assertEquals(AccountPreferences(), storage.preferences(account.identity.key))
        storage.updatePreferences(account.identity.key) { it.copy(favoriteIds = listOf("C", "C"), runningFavorites = false) }
        val restored = VaultAccountStorage(vault)
        assertEquals(listOf("C"), restored.preferences(account.identity.key).favoriteIds)
        assertEquals("fixture", restored.active()?.password)
        assertEquals(account.identity, restored.active()?.identity)
    }

    @Test fun replacingCredentialsAndSigningOutPreservePreferencesButRemovalClearsThem() = runTest {
        val vault = Vault()
        val storage = VaultAccountStorage(vault)
        val alice = AccountIdentity("https://ci.example/", "alice", false)
        val bob = alice.copy(userName = "bob")
        storage.saveAndActivate(AccountSecret(alice, "old"))
        storage.updatePreferences(alice.key) { it.copy(favoriteIds = listOf("A"), queueFavorites = false) }
        storage.saveAndActivate(AccountSecret(bob, "bob"))
        assertEquals(AccountPreferences(), storage.preferences(bob.key))
        val replacement = alice.copy(sslDisabled = true)
        storage.saveAndActivate(AccountSecret(replacement, "new"))
        assertEquals(listOf("A"), storage.preferences(replacement.key).favoriteIds)
        assertEquals("new", storage.secret(replacement.key)?.password)
        storage.signOut()
        assertNull(storage.active())
        assertFalse(VaultAccountStorage(vault).preferences(replacement.key).queueFavorites)
        storage.remove(replacement.key)
        assertFails { storage.preferences(replacement.key) }
        assertEquals(AccountPreferences(), storage.preferences(bob.key))
    }

    @Test fun failedAtomicWriteDoesNotChangeStoredPreferencesOrActiveIdentity() = runTest {
        val vault = Vault()
        val storage = VaultAccountStorage(vault)
        val alice = AccountIdentity("https://ci.example/", "alice", false)
        storage.saveAndActivate(AccountSecret(alice, "fixture"))
        val before = vault.value
        vault.fail = true
        assertFails { storage.updatePreferences(alice.key) { it.copy(favoriteIds = listOf("A")) } }
        assertEquals(before, vault.value)
        assertEquals(AccountPreferences(), storage.preferences(alice.key))
        assertEquals(alice, storage.active()?.identity)
    }
}
