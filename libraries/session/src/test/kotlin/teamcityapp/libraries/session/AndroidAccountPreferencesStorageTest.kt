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
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage
import java.io.File
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
class AndroidAccountPreferencesStorageTest {
    private val crypto = object : CryptoManager {
        override fun encrypt(password: String) = ("encrypted:" + password).encodeToByteArray()
        override fun decrypt(password: ByteArray) = password.decodeToString().removePrefix("encrypted:").encodeToByteArray()
        override fun isFailed(result: ByteArray) = result.isEmpty()
    }
    private val alice = AccountSecret(AccountIdentity("https://teamcity.example/context/", "alice", false), "secret")
    private val bob = AccountSecret(alice.identity.copy(userName = "bob"), "other")

    @Test fun migrationRetainsEncryptedAccountsFavoritesAndActiveSelection() = runTest {
        val context = RuntimeEnvironment.getApplication()
        val legacy = SharedUserStorage.init(context, crypto)
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val accounts = LegacyAndroidAccountStorage(legacy, crypto, dispatcher)
        accounts.saveAndActivate(alice)
        legacy.addBuildTypeToFavorites("existing")
        val ciphertext = legacy.activeUser.passwordAsBytes.copyOf()
        val file = File.createTempFile("account-prefs", ".preferences_pb", context.cacheDir).apply { delete() }
        val data = PreferenceDataStoreFactory.create(scope = backgroundScope, produceFile = { file })
        val bridge = AndroidAccountPreferencesStorage(accounts, legacy, data, dispatcher)
        assertEquals(listOf("existing"), bridge.preferences(alice.identity.key).favoriteIds)
        bridge.updatePreferences(alice.identity.key) { it.copy(favoriteIds = listOf("existing", "new", "new"), runningFavorites = false, agentsConnected = false) }
        assertEquals(listOf("existing", "new"), legacy.activeUser.buildTypeIds)
        assertArrayEquals(ciphertext, legacy.activeUser.passwordAsBytes)
        assertEquals("secret", bridge.active()!!.password)
        assertEquals(alice.identity, bridge.active()!!.identity)
        val recreated = AndroidAccountPreferencesStorage(accounts, legacy, data, dispatcher)
        assertFalse(recreated.preferences(alice.identity.key).runningFavorites)
        assertFalse(recreated.preferences(alice.identity.key).agentsConnected)
        // Simulate a process death between durable DataStore commit and legacy mirroring.
        legacy.activeUser.buildTypeIds = arrayListOf("old")
        assertEquals(listOf("existing", "new"), recreated.preferences(alice.identity.key).favoriteIds)
        assertEquals(listOf("existing", "new"), legacy.activeUser.buildTypeIds)
    }

    @Test fun preferencesRemainAccountScopedAcrossSwitchSignOutAndRemoval() = runTest {
        val context = RuntimeEnvironment.getApplication()
        val legacy = SharedUserStorage.init(context, crypto)
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val accounts = LegacyAndroidAccountStorage(legacy, crypto, dispatcher)
        accounts.saveAndActivate(alice)
        accounts.saveAndActivate(bob)
        val file = File.createTempFile("isolated-prefs", ".preferences_pb", context.cacheDir).apply { delete() }
        val data = PreferenceDataStoreFactory.create(scope = backgroundScope, produceFile = { file })
        val bridge = AndroidAccountPreferencesStorage(accounts, legacy, data, dispatcher)
        bridge.updatePreferences(alice.identity.key) { it.copy(favoriteIds = listOf("alice-only"), queueFavorites = false) }
        assertEquals(bob.identity, bridge.active()!!.identity)
        assertTrue(bridge.preferences(bob.identity.key).favoriteIds.isEmpty())
        bridge.signOut()
        assertNull(bridge.active())
        assertEquals(listOf("alice-only"), bridge.preferences(alice.identity.key).favoriteIds)
        bridge.activate(alice.identity.key)
        assertEquals(listOf("alice-only"), legacy.activeUser.buildTypeIds)
        bridge.remove(alice.identity.key)
        try {
            bridge.preferences(alice.identity.key)
            fail("Removed accounts cannot read retained metadata")
        } catch (_: IllegalArgumentException) {}
        assertTrue(bridge.preferences(bob.identity.key).favoriteIds.isEmpty())
    }

    @Test fun tlsReauthenticationKeepsPreferencesButRemovingAndReaddingDoesNot() = runTest {
        val context = RuntimeEnvironment.getApplication()
        val legacy = SharedUserStorage.init(context, crypto)
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val accounts = LegacyAndroidAccountStorage(legacy, crypto, dispatcher)
        val file = File.createTempFile("consent-prefs", ".preferences_pb", context.cacheDir).apply { delete() }
        val data = PreferenceDataStoreFactory.create(scope = backgroundScope, produceFile = { file })
        val bridge = AndroidAccountPreferencesStorage(accounts, legacy, data, dispatcher)
        bridge.saveAndActivate(alice)
        bridge.updatePreferences(alice.identity.key) { it.copy(favoriteIds = listOf("saved"), queueFavorites = false, shownPrompts = setOf("home.Drawer"), ratingHandled = true) }
        val changed = AccountSecret(alice.identity.copy(sslDisabled = true), "updated")
        bridge.saveAndActivate(changed)
        assertEquals(listOf("saved"), bridge.preferences(changed.identity.key).favoriteIds)
        assertFalse(bridge.preferences(changed.identity.key).queueFavorites)
        assertTrue(bridge.preferences(changed.identity.key).ratingHandled)
        bridge.remove(changed.identity.key)
        bridge.saveAndActivate(changed)
        assertEquals(AccountPreferences(), bridge.preferences(changed.identity.key))
    }
}
