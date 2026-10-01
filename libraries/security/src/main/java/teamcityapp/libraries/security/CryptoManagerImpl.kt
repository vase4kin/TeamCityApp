/*
 * Copyright 2020 Andrey Tolpeev
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

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.io.IOException
import java.security.GeneralSecurityException
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/** Encrypts account passwords with an app-owned Android Keystore key. */
class CryptoManagerImpl : CryptoManager {

    // Invalid UTF-8 cannot be a password supplied through the String API.
    private val failed = byteArrayOf(-1, -2, -1)

    override fun encrypt(password: String): ByteArray = try {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        val iv = cipher.iv
        val ciphertext = cipher.doFinal(password.toByteArray(Charsets.UTF_8))
        FORMAT + iv + ciphertext
    } catch (e: GeneralSecurityException) {
        failed
    } catch (e: IOException) {
        failed
    }

    override fun decrypt(password: ByteArray): ByteArray {
        if (!isCurrentFormat(password)) return failed
        return try {
            val ivStart = FORMAT.size
            val iv = password.copyOfRange(ivStart, ivStart + IV_SIZE)
            val ciphertext = password.copyOfRange(ivStart + IV_SIZE, password.size)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), GCMParameterSpec(TAG_BITS, iv))
            cipher.doFinal(ciphertext)
        } catch (e: GeneralSecurityException) {
            failed
        } catch (e: IOException) {
            failed
        }
    }

    override fun isFailed(result: ByteArray): Boolean = result.contentEquals(failed)

    private fun isCurrentFormat(password: ByteArray): Boolean =
        password.size >= FORMAT.size + IV_SIZE + TAG_BITS / 8 &&
            password.copyOfRange(0, FORMAT.size).contentEquals(FORMAT)

    @Synchronized
    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance(KEYSTORE)
        keyStore.load(null)
        val existing = keyStore.getKey(KEY_ALIAS, null)
        if (existing != null) return existing as SecretKey

        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE)
        generator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
        )
        return generator.generateKey()
    }

    private companion object {
        const val KEYSTORE = "AndroidKeyStore"
        const val KEY_ALIAS = "teamcityapp_account_password_v1"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val IV_SIZE = 12
        const val TAG_BITS = 128
        val FORMAT = "TeamCityAppPassword\u0001".toByteArray(Charsets.US_ASCII)
    }
}
