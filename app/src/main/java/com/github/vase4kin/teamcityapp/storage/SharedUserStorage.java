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

package com.github.vase4kin.teamcityapp.storage;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.github.vase4kin.teamcityapp.api.interfaces.Collectible;
import com.github.vase4kin.teamcityapp.crypto.CryptoManager;
import com.github.vase4kin.teamcityapp.storage.api.UserAccount;
import com.github.vase4kin.teamcityapp.storage.api.UsersContainer;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

/**
 * SharePreferences json based storage for user accounts
 */
public class SharedUserStorage implements Collectible<UserAccount> {

    private static final String SHARED_PREF_NAME = "UserAccounts";
    private Context mContext;
    private CryptoManager mCryptoManager;

    private UsersContainer usersContainer;

    private SharedUserStorage(Context context, CryptoManager cryptoManager) {
        this.mContext = context;
        this.mCryptoManager = cryptoManager;
        initUserContainer();
    }

    public static SharedUserStorage init(Context context, CryptoManager cryptoManager) {
        return new SharedUserStorage(context, cryptoManager);
    }

    /**
     * @return default shared preferences instance
     */
    private SharedPreferences getSharedPreferences() {
        return mContext.getSharedPreferences(SHARED_PREF_NAME, 0);
    }

    /**
     * Init user container
     */
    private void initUserContainer() {
        String json = getSharedPreferences().getString(SHARED_PREF_NAME, null);
        if (json != null) {
            usersContainer = new Gson().fromJson(json, UsersContainer.class);
        } else {
            usersContainer = new UsersContainer();
            List<UserAccount> userAccounts = new ArrayList<>();
            usersContainer.setUsersAccounts(userAccounts);
            commitUserChanges();
        }
    }

    /**
     * Do we have guest user account with url
     *
     * @param url - TC url
     */
    public boolean hasGuestAccountWithUrl(String url) {
        for (UserAccount userAccount : usersContainer.getUsersAccounts()) {
            if (userAccount.equals(UsersFactory.guestUser(url))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Do we have user account with url
     *
     * @param url - TC url
     */
    public boolean hasAccountWithUrl(String url, String userName) {
        for (UserAccount userAccount : usersContainer.getUsersAccounts()) {
            if (userAccount.equals(UsersFactory.user(url, userName))) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return do we have user accounts or not
     */
    public boolean hasUserAccounts() {
        return !getUserAccounts().isEmpty();
    }

    public void saveGuestUserAccountAndSetItAsActive(String baseUrl, boolean disableSsl) {
        UserAccount userAccount = UsersFactory.guestUser(baseUrl);
        userAccount.setSslDisabled(disableSsl);
        setActiveUserNotActive();
        usersContainer.getUsersAccounts().add(userAccount);
        commitUserChanges();
    }

    public void saveUserAccountAndSetItAsActive(final String baseUrl,
                                                final String userName,
                                                final String password,
                                                boolean disableSsl,
                                                OnStorageListener listener) {
        byte[] encryptedPassword = mCryptoManager.encrypt(password);
        if (!mCryptoManager.isFailed(encryptedPassword)) {
            UserAccount userAccount = UsersFactory.user(baseUrl, userName, encryptedPassword);
            userAccount.setSslDisabled(disableSsl);
            setActiveUserNotActive();
            usersContainer.getUsersAccounts().add(userAccount);
            commitUserChanges();
            listener.onSuccess();
        } else {
            listener.onFail();
        }
    }

    /**
     * @return Active user
     * <p>
     * If user doesn't exist return fake one
     */
    @NonNull
    public UserAccount getActiveUser() {
        for (final UserAccount userAccount : usersContainer.getUsersAccounts()) {
            if (userAccount.isActive()) {
                // backward compatibility for old user accounts
                // Who's gonna set 'Guest user' as real user name for TC user, right?
                if (userAccount.getUserName().equals(UsersFactory.GUEST_USER_USER_NAME)) {
                    UserAccount guestUser = UsersFactory.guestUser(userAccount.getTeamcityUrl());
                    guestUser.setBuildTypeIds(userAccount.getBuildTypeIds());
                    guestUser.setSslDisabled(userAccount.isSslDisabled());
                    return guestUser;
                }
                // Don't need to decrypt password of guest user
                if (userAccount.isGuestUser()) {
                    return userAccount;
                }
                // Decrypting password for user
                byte[] decryptedPassword = mCryptoManager.decrypt(userAccount.getPasswordAsBytes());
                if (!mCryptoManager.isFailed(decryptedPassword)) {
                    UserAccount user = UsersFactory.user(userAccount, decryptedPassword);
                    user.setSslDisabled(userAccount.isSslDisabled());
                    user.setBuildTypeIds(userAccount.getBuildTypeIds());
                    return user;
                } else {
                    return UsersFactory.EMPTY_USER;
                }
            }
        }
        return UsersFactory.EMPTY_USER;
    }

    /**
     * Set active user to inactive state
     */
    private void setActiveUserNotActive() {
        for (UserAccount userAccount : usersContainer.getUsersAccounts()) {
            if (userAccount.isActive()) {
                userAccount.setIsActive(false);
                break;
            }
        }
        commitUserChanges();
    }

    /**
     * Set active user with url
     *
     * @param url - TC url
     */
    public void setUserActive(String url, String userName) {
        setActiveUserNotActive();
        for (UserAccount userAccount : usersContainer.getUsersAccounts()) {
            if (userAccount.equals(UsersFactory.user(url, userName))) {
                userAccount.setIsActive(true);
                break;
            }
        }
        commitUserChanges();
    }

    /**
     * Remove user account
     *
     * @param userAccount - User account to remove
     */
    public void removeUserAccount(UserAccount userAccount) {
        List<UserAccount> modifiedCollection = new ArrayList<>(usersContainer.getUsersAccounts());
        for (UserAccount accountToRemove : modifiedCollection) {
            if (accountToRemove.equals(UsersFactory.user(userAccount.getTeamcityUrl(), userAccount.getUserName()))) {
                usersContainer.getUsersAccounts().remove(accountToRemove);
            }
        }
        commitUserChanges();
    }

    public void addBuildTypeToFavorites(String buildTypeId) {
        for (UserAccount userAccount : usersContainer.getUsersAccounts()) {
            if (userAccount.isActive()) {
                userAccount.addBuildType(buildTypeId);
                commitUserChanges();
                break;
            }
        }
    }

    public void removeBuildTypeFromFavorites(String buildTypeId) {
        for (UserAccount userAccount : usersContainer.getUsersAccounts()) {
            if (userAccount.isActive()) {
                userAccount.getBuildTypeIds().remove(buildTypeId);
                commitUserChanges();
                break;
            }
        }
    }

    public List<String> getFavoriteBuildTypeIds() {
        return getActiveUser().getBuildTypeIds();
    }

    /**
     * Save all changes
     */
    private void commitUserChanges() {
        String usersJson = new Gson().toJson(usersContainer);
        getSharedPreferences().edit().putString(SHARED_PREF_NAME, usersJson).apply();
    }

    public List<UserAccount> getUserAccounts() {
        return usersContainer.getUsersAccounts();
    }

    /**
     * Testing purpose
     */
    public void clearAll() {
        List<UserAccount> userAccounts = usersContainer.getUsersAccounts();
        if (!userAccounts.isEmpty()) {
            userAccounts.clear();
            commitUserChanges();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UserAccount> getObjects() {
        return usersContainer.getUsersAccounts();
    }

    /**
     * On storage listener
     */
    public interface OnStorageListener {

        /**
         * On success user data save
         */
        void onSuccess();

        /**
         * On fail user data save
         */
        void onFail();
    }
}
