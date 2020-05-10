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

/**
 * Crypto manager
 */
interface CryptoManager {

    /**
     * Encrypt password
     *
     * @param password to encrypt
     * @return Encrypted password in byte array
     */
    fun encrypt(password: String): ByteArray

    /**
     * Decrypt password
     *
     * @param password to encrypt
     * @return Decrypted password in byte array
     */
    fun decrypt(password: ByteArray): ByteArray

    /**
     * Is encryption/decryption failed
     *
     * @param result - encryption/decryption result
     * @return true if encryption/decryption is failed
     */
    fun isFailed(result: ByteArray): Boolean
}
