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

package com.github.vase4kin.teamcityapp.storage

import androidx.annotation.VisibleForTesting
import com.github.vase4kin.teamcityapp.storage.api.UserAccount

/**
 * Users factory
 */
object UsersFactory {

    internal const val GUEST_USER_USER_NAME = "Guest user"
    private const val EMPTY_STRING = ""
    @VisibleForTesting
    val EMPTY_USER = UserAccount(EMPTY_STRING, EMPTY_STRING, isGuestUser = true).apply { isActive = true }

    /**
     * @return Guest user
     */
    internal fun guestUser(serverUrl: String): UserAccount {
        return UserAccount(serverUrl, GUEST_USER_USER_NAME, isGuestUser = true).apply { isActive = true }
    }

    /**
     * @return Registered user
     */
    internal fun user(serverUrl: String, userName: String, password: ByteArray): UserAccount {
        return UserAccount(serverUrl, userName, isGuestUser = false)
                .apply {
                    this.passwordAsBytes = password
                    this.isActive = true
                }
    }

    /**
     * @return User for equal operations
     */
    internal fun user(serverUrl: String, userName: String): UserAccount {
        return UserAccount(serverUrl, userName, isGuestUser = false)
    }

    /**
     * @return Registered user with decrypted password
     */
    internal fun user(userAccount: UserAccount, decryptedPassword: ByteArray): UserAccount {
        return UserAccount(
                userAccount.teamcityUrl,
                userAccount.userName,
                userAccount.isGuestUser).apply {
            this.passwordAsBytes = decryptedPassword
            this.isActive = userAccount.isActive
        }
    }
}
