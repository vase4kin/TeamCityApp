/*
 * Copyright 2019 Andrey Tolpeev
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

package com.github.vase4kin.teamcityapp.crypto

import com.facebook.crypto.Crypto
import com.facebook.crypto.Entity
import com.facebook.crypto.exception.CryptoInitializationException
import com.facebook.crypto.exception.KeyChainException

import java.io.IOException
import java.util.Arrays

/**
 * Impl of [CryptoManager]
 */
class CryptoManagerImpl(private val crypto: Crypto) : CryptoManager {

    private val passwordEntity = Entity.create("password")
    private val empty = "EncryptionFailed".toByteArray()

    /**
     * {@inheritDoc}
     */
    override fun encrypt(password: String): ByteArray {
        if (!crypto.isAvailable) return empty
        return try {
            crypto.encrypt(password.toByteArray(), passwordEntity)
        } catch (e: KeyChainException) {
            empty
        } catch (e: CryptoInitializationException) {
            empty
        } catch (e: IOException) {
            empty
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun decrypt(password: ByteArray): ByteArray {
        if (!crypto.isAvailable) return empty
        return try {
            crypto.decrypt(password, passwordEntity)
        } catch (e: KeyChainException) {
            empty
        } catch (e: CryptoInitializationException) {
            empty
        } catch (e: IOException) {
            empty
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun isFailed(result: ByteArray): Boolean {
        return Arrays.equals(empty, result)
    }
}
