/*
 * Copyright 2016 Andrey Tolpeev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.vase4kin.teamcityapp.crypto;

import com.facebook.crypto.Crypto;
import com.facebook.crypto.Entity;
import com.facebook.crypto.exception.CryptoInitializationException;
import com.facebook.crypto.exception.KeyChainException;

import java.io.IOException;
import java.util.Arrays;

/**
 * Impl of {@link CryptoManager}
 */
public class CryptoManagerImpl implements CryptoManager {

    private static final Entity passwordEntity = Entity.create("password");
    private static final byte[] EMPTY = "EncryptionFailed".getBytes();
    private Crypto mCrypto;

    public CryptoManagerImpl(Crypto crypto) {
        this.mCrypto = crypto;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] encrypt(String password) {
        if (!mCrypto.isAvailable()) return EMPTY;
        try {
            return mCrypto.encrypt(password.getBytes(), passwordEntity);
        } catch (KeyChainException | CryptoInitializationException | IOException e) {
            return EMPTY;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] decrypt(byte[] password) {
        if (!mCrypto.isAvailable()) return EMPTY;
        try {
            return mCrypto.decrypt(password, passwordEntity);
        } catch (KeyChainException | CryptoInitializationException | IOException e) {
            return EMPTY;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isFailed(byte[] result) {
        return Arrays.equals(EMPTY, result);
    }
}
