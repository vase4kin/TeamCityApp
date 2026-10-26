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

package teamcityapp.libraries.security

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CryptoManagerImplTest {

    private val crypto = CryptoManagerImpl()

    @Test
    fun encryptAndDecryptWithPersistedKey() {
        val encrypted = crypto.encrypt("EncryptionFailed")

        assertFalse(crypto.isFailed(encrypted))
        val decrypted = CryptoManagerImpl().decrypt(encrypted)
        assertFalse(crypto.isFailed(decrypted))
        assertArrayEquals("EncryptionFailed".toByteArray(), decrypted)
    }

    @Test
    fun rejectsLegacyAndTamperedCiphertext() {
        val legacy = byteArrayOf(1, 2, 3)
        assertTrue(crypto.isFailed(crypto.decrypt(legacy)))

        val encrypted = crypto.encrypt("password")
        encrypted[encrypted.lastIndex] = (encrypted.last().toInt() xor 1).toByte()
        assertTrue(crypto.isFailed(crypto.decrypt(encrypted)))
    }
}
