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

import android.app.Application
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import teamcityapp.libraries.accounts.*
import teamcityapp.libraries.security.CryptoManager

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class LegacyAndroidAccountStorageTest {
    @Test fun identityKeysKeepTheInstalledGsonEncoding() {
        val identity = AccountIdentity("https://teamcity.example/context/", "a\"b\n<>&\u2028\u2029", false, true)
        val legacy = com.google.gson.JsonArray().apply {
            add(identity.serverUrl)
            add(identity.userName)
            add(identity.guest)
            add(identity.sslDisabled)
        }.toString()
        assertEquals(legacy, identity.key)
    }
    private class Crypto : CryptoManager {
        var fail = false
        override fun encrypt(password: String) = if (fail) byteArrayOf() else ("encrypted:" + password).encodeToByteArray()
        override fun decrypt(password: ByteArray) = if (fail) byteArrayOf() else password.decodeToString().removePrefix("encrypted:").encodeToByteArray()
        override fun isFailed(result: ByteArray) = result.isEmpty()
    }
    private val crypto = Crypto()
    private val context get() = RuntimeEnvironment.getApplication()
    private fun store() = SharedUserStorage.init(context, crypto)
    private val alice = AccountSecret(AccountIdentity("https://teamcity.example", "alice", false), "first")
    private val bob = AccountSecret(alice.identity.copy(userName = "bob"), "second")

    @Test fun installedPrivateAccountsRetainCiphertextFavoritesAndIdentityKeys() = runTest {
        val legacy = store()
        legacy.saveUserAccountAndSetItAsActive(
            alice.identity.serverUrl,
            "alice",
            "first",
            true,
            object : SharedUserStorage.OnStorageListener {
                override fun onSuccess() {}
                override fun onFail() = error("save failed")
            }
        )
        legacy.addBuildTypeToFavorites("saved-build")
        val original = legacy.userAccounts.single().passwordAsBytes.copyOf()
        val adapter = LegacyAndroidAccountStorage(legacy, crypto, UnconfinedTestDispatcher(testScheduler))
        val account = adapter.active()!!
        assertEquals("first", account.password)
        assertTrue(account.identity.sslDisabled)
        val key = com.google.gson.JsonArray().apply {
            add(alice.identity.serverUrl)
            add("alice")
            add(false)
            add(true)
        }.toString()
        assertEquals(key, account.identity.key)
        adapter.saveAndActivate(bob)
        adapter.activate(account.identity.key)
        assertEquals("first", adapter.active()!!.password)
        assertArrayEquals(original, legacy.userAccounts.first().passwordAsBytes)
        assertEquals(listOf("saved-build"), legacy.activeUser.buildTypeIds)
        assertEquals("first", LegacyAndroidAccountStorage(store(), crypto, UnconfinedTestDispatcher(testScheduler)).active()!!.password)
    }

    @Test fun updatingAnAccountPreservesItsFavoritesWithoutDuplicateRows() = runTest {
        val legacy = store()
        val adapter = LegacyAndroidAccountStorage(legacy, crypto, UnconfinedTestDispatcher(testScheduler))
        adapter.saveAndActivate(alice)
        legacy.addBuildTypeToFavorites("saved")
        adapter.saveAndActivate(AccountSecret(alice.identity.copy(sslDisabled = true), "changed"))
        assertEquals(1, adapter.accounts().size)
        assertEquals("changed", adapter.active()!!.password)
        assertEquals(listOf("saved"), legacy.activeUser.buildTypeIds)
    }

    @Test fun signOutAndRelaunchKeepSavedAccountsAndRemovingTheActiveSelectsTheNext() = runTest {
        val adapter = LegacyAndroidAccountStorage(store(), crypto, UnconfinedTestDispatcher(testScheduler))
        adapter.saveAndActivate(alice)
        adapter.saveAndActivate(bob)
        adapter.signOut()
        assertNull(adapter.active())
        assertEquals(2, adapter.accounts().size)
        val restored = LegacyAndroidAccountStorage(store(), crypto, UnconfinedTestDispatcher(testScheduler))
        assertNull(restored.active())
        restored.activate(bob.identity.key)
        restored.remove(bob.identity.key)
        assertEquals(alice.identity, restored.active()!!.identity)
        restored.remove(alice.identity.key)
        assertNull(restored.active())
    }

    @Test fun failedEncryptionLeavesThePriorPersistedAccountUntouched() = runTest {
        val adapter = LegacyAndroidAccountStorage(store(), crypto, UnconfinedTestDispatcher(testScheduler))
        adapter.saveAndActivate(alice)
        crypto.fail = true
        try {
            adapter.saveAndActivate(bob)
            fail("Expected encryption failure")
        } catch (_: IllegalStateException) {}
        crypto.fail = false
        assertEquals(alice.identity, adapter.active()!!.identity)
        assertEquals(listOf(alice.identity), adapter.accounts())
    }

    @Test fun guestPasswordsAreNeverPassedToTheCipherAndTheLegacyGuestMarkerIsAccepted() = runTest {
        val adapter = LegacyAndroidAccountStorage(store(), crypto, UnconfinedTestDispatcher(testScheduler))
        crypto.fail = true
        val guest = AccountIdentity("https://guest.example", "Guest user", true)
        adapter.saveAndActivate(AccountSecret(guest))
        assertEquals("", adapter.active()!!.password)
        assertEquals(guest, adapter.active()!!.identity)
    }
}
