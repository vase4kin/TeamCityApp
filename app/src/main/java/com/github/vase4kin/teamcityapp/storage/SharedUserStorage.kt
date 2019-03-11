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

import android.content.Context
import android.content.SharedPreferences
import com.github.vase4kin.teamcityapp.api.interfaces.Collectible
import com.github.vase4kin.teamcityapp.crypto.CryptoManager
import com.github.vase4kin.teamcityapp.storage.api.UserAccount
import com.github.vase4kin.teamcityapp.storage.api.UsersContainer
import com.github.vase4kin.teamcityapp.storage.api.addBuildType
import com.google.gson.Gson
import java.util.*

/**
 * SharePreferences json based storage for user accounts
 */
class SharedUserStorage private constructor(
        private val mContext: Context,
        private val mCryptoManager: CryptoManager) : Collectible<UserAccount> {

    private var usersContainer: UsersContainer = UsersContainer()

    /**
     * @return default shared preferences instance
     */
    private val sharedPreferences: SharedPreferences
        get() = mContext.getSharedPreferences(SHARED_PREF_NAME, 0)

    /**
     * @return Active user
     *
     *
     * If user doesn't exist return fake one
     */
    // backward compatibility for old user accounts
    // Who's gonna set 'Guest user' as real user name for TC user, right?
    // Don't need to decrypt password of guest user
    // Decrypting password for user
    val activeUser: UserAccount
        get() {
            for (userAccount in usersContainer.usersAccounts) {
                if (userAccount.isActive) {
                    if (userAccount.userName == UsersFactory.GUEST_USER_USER_NAME) {
                        return UsersFactory.guestUser(userAccount.teamcityUrl)
                                .copy(favoriteBuildTypes = userAccount.favoriteBuildTypes, isSslDisabled = userAccount.isSslDisabled)
                    }
                    if (userAccount.isGuestUser) {
                        return userAccount
                    }
                    val decryptedPassword = mCryptoManager.decrypt(userAccount.passwordAsBytes)
                    return if (!mCryptoManager.isFailed(decryptedPassword)) {
                        UsersFactory.user(userAccount, decryptedPassword)
                                .copy(favoriteBuildTypes = userAccount.favoriteBuildTypes, isSslDisabled = userAccount.isSslDisabled)
                    } else {
                        UsersFactory.EMPTY_USER
                    }
                }
            }
            return UsersFactory.EMPTY_USER
        }

    val userAccounts: List<UserAccount> get() = usersContainer.usersAccounts

    init {
        initUserContainer()
    }

    /**
     * Init user container
     */
    private fun initUserContainer() {
        val json = sharedPreferences.getString(SHARED_PREF_NAME, null)
        if (json != null) {
            usersContainer = Gson().fromJson(json, UsersContainer::class.java)
        } else {
            usersContainer = UsersContainer()
            commitUserChanges()
        }
    }

    /**
     * Do we have guest user account with url
     *
     * @param url - TC url
     */
    fun hasGuestAccountWithUrl(url: String): Boolean {
        for (userAccount in usersContainer.usersAccounts) {
            if (userAccount == UsersFactory.guestUser(url)) {
                return true
            }
        }
        return false
    }

    /**
     * Do we have user account with url
     *
     * @param url - TC url
     */
    fun hasAccountWithUrl(url: String, userName: String): Boolean {
        for (userAccount in usersContainer.usersAccounts) {
            if (userAccount == UsersFactory.user(url, userName)) {
                return true
            }
        }
        return false
    }

    /**
     * @return do we have user accounts or not
     */
    fun hasUserAccounts() = !userAccounts.isEmpty()

    fun saveGuestUserAccountAndSetItAsActive(baseUrl: String, disableSsl: Boolean) {
        val userAccount = UsersFactory.guestUser(baseUrl)
        userAccount.isSslDisabled = disableSsl
        setActiveUserNotActive()
        usersContainer.usersAccounts.add(userAccount)
        commitUserChanges()
    }

    fun saveUserAccountAndSetItAsActive(baseUrl: String,
                                        userName: String,
                                        password: String,
                                        disableSsl: Boolean,
                                        listener: OnStorageListener) {
        val encryptedPassword = mCryptoManager.encrypt(password)
        if (!mCryptoManager.isFailed(encryptedPassword)) {
            val userAccount = UsersFactory.user(baseUrl, userName, encryptedPassword)
            userAccount.isSslDisabled = disableSsl
            setActiveUserNotActive()
            usersContainer.usersAccounts.add(userAccount)
            commitUserChanges()
            listener.onSuccess()
        } else {
            listener.onFail()
        }
    }

    /**
     * Set active user to inactive state
     */
    private fun setActiveUserNotActive() {
        for (userAccount in usersContainer.usersAccounts) {
            if (userAccount.isActive) {
                userAccount.isActive = false
                break
            }
        }
        commitUserChanges()
    }

    /**
     * Set active user with url
     *
     * @param url - TC url
     */
    fun setUserActive(url: String, userName: String) {
        setActiveUserNotActive()
        for (userAccount in usersContainer.usersAccounts) {
            if (userAccount == UsersFactory.user(url, userName)) {
                userAccount.isActive = true
                break
            }
        }
        commitUserChanges()
    }

    /**
     * Remove user account
     *
     * @param userAccount - User account to remove
     */
    fun removeUserAccount(userAccount: UserAccount) {
        val modifiedCollection = ArrayList(usersContainer.usersAccounts)
        for (accountToRemove in modifiedCollection) {
            if (accountToRemove == UsersFactory.user(userAccount.teamcityUrl, userAccount.userName)) {
                usersContainer.usersAccounts.remove(accountToRemove)
            }
        }
        if (usersContainer.usersAccounts.size > 0) {
            usersContainer.usersAccounts[0].isActive = true
        }
        commitUserChanges()
    }

    fun addBuildTypeToFavorites(buildTypeId: String) {
        for (userAccount in usersContainer.usersAccounts) {
            if (userAccount.isActive) {
                userAccount.addBuildType(buildTypeId)
                commitUserChanges()
                break
            }
        }
    }

    fun removeBuildTypeFromFavorites(buildTypeId: String) {
        for ((_, _, _, _, isActive, _, favoriteBuildTypes1) in usersContainer.usersAccounts) {
            if (isActive) {
                favoriteBuildTypes1.remove(buildTypeId)
                commitUserChanges()
                break
            }
        }
    }

    /**
     * Save all changes
     */
    private fun commitUserChanges() {
        val usersJson = Gson().toJson(usersContainer)
        sharedPreferences.edit().putString(SHARED_PREF_NAME, usersJson).apply()
    }

    /**
     * Testing purpose
     */
    fun clearAll() {
        val userAccounts = usersContainer.usersAccounts
        if (!userAccounts.isEmpty()) {
            userAccounts.clear()
            commitUserChanges()
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun getObjects(): List<UserAccount> {
        return usersContainer.usersAccounts
    }

    /**
     * On storage listener
     */
    interface OnStorageListener {

        /**
         * On success user data save
         */
        fun onSuccess()

        /**
         * On fail user data save
         */
        fun onFail()
    }

    companion object {

        private const val SHARED_PREF_NAME = "UserAccounts"

        fun init(context: Context, cryptoManager: CryptoManager): SharedUserStorage {
            return SharedUserStorage(context, cryptoManager)
        }
    }
}
