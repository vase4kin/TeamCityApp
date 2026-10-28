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

package com.github.vase4kin.teamcityapp.storage

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.vase4kin.teamcityapp.storage.api.UsersContainer
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import teamcityapp.libraries.security.CryptoManagerImpl
import teamcityapp.libraries.storage.models.UserAccount

@RunWith(AndroidJUnit4::class)
class SharedUserStorageEncryptionTest {

    @Test
    fun preservesLegacyPreferencesAndUsesNewStore() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val oldPreferences = context.getSharedPreferences("UserAccounts", 0)
        context.getSharedPreferences("UserAccountsKeystoreV1", 0).edit().clear().commit()
        val guest = UserAccount("https://guest.example", "Guest user", byteArrayOf(), true, false)
        val legacy = UserAccount("https://server.example", "user", byteArrayOf(1, 2, 3), false, true)
        val container = UsersContainer().apply { usersAccounts = listOf(guest, legacy) }
        val oldJson = Gson().toJson(container)
        oldPreferences.edit().putString("UserAccounts", oldJson).commit()

        val storage = SharedUserStorage.init(context, CryptoManagerImpl())
        assertFalse(storage.hasUserAccounts())
        assertFalse(storage.hasAccountWithUrl(legacy.teamcityUrl, legacy.userName))
        assertEquals(oldJson, oldPreferences.getString("UserAccounts", null))

        var saved = false
        storage.saveUserAccountAndSetItAsActive(
            legacy.teamcityUrl,
            legacy.userName,
            "new password",
            false,
            object : SharedUserStorage.OnStorageListener {
                override fun onSuccess() {
                    saved = true
                }
                override fun onFail() {}
            }
        )
        assertTrue(saved)
        assertEquals("new password", SharedUserStorage.init(context, CryptoManagerImpl()).activeUser.passwordAsString)
        assertEquals(oldJson, oldPreferences.getString("UserAccounts", null))
    }
}
