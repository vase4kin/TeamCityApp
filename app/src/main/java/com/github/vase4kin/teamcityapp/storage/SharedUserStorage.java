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
import android.support.annotation.NonNull;

import com.github.vase4kin.teamcityapp.api.interfaces.Collectible;
import com.github.vase4kin.teamcityapp.storage.api.UserAccount;
import com.github.vase4kin.teamcityapp.storage.api.UsersContainer;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

/**
 * SharePreferences json based storage for user accounts
 */
public class SharedUserStorage implements Collectible<UserAccount> {

    private Context context;
    private static final String GUEST_USER_USER_NAME = "Guest user";
    private static final String SHARED_PREF_NAME = "UserAccounts";
    private static final String EMPTY_STRING = "";

    private UsersContainer usersContainer;

    private SharedUserStorage(Context context) {
        this.context = context;
        initUserContainer();
    }

    public static SharedUserStorage init(Context context) {
        return new SharedUserStorage(context);
    }

    /**
     * @return default shared preferences instance
     */
    private SharedPreferences getSharedPreferences() {
        return context.getSharedPreferences(SHARED_PREF_NAME, 0);
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
     * Do we have user account with url
     *
     * @param url - TC url
     */
    public boolean hasAccountWithUrl(String url) {
        for (UserAccount userAccount : usersContainer.getUsersAccounts()) {
            if (userAccount.getTeamcityUrl().equals(url)) {
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

    public void createNewUserAccountAndSetItAsActive(String baseUrl) {
        setActiveUserNotActive();
        UserAccount userAccount = new UserAccount(GUEST_USER_USER_NAME, baseUrl, true);
        usersContainer.getUsersAccounts().add(userAccount);
        commitUserChanges();
    }

    /**
     * @return Active user
     * <p>
     * If user doesn't exist return fake one
     */
    @NonNull
    public UserAccount getActiveUser() {
        for (UserAccount userAccount : usersContainer.getUsersAccounts()) {
            if (userAccount.isActive()) {
                return userAccount;
            }
        }
        return new UserAccount(EMPTY_STRING, EMPTY_STRING, true);
    }

    /**
     * Set active user to inactive state
     */
    private void setActiveUserNotActive() {
        for (UserAccount userAccount : usersContainer.getUsersAccounts()) {
            if (userAccount.isActive()) {
                userAccount.setIsActive(false);
            }
        }
        commitUserChanges();
    }

    /**
     * Set active user with url
     *
     * @param url - TC url
     */
    public void setUserActiveWithEmail(String url) {
        setActiveUserNotActive();
        for (UserAccount userAccount : usersContainer.getUsersAccounts()) {
            if (userAccount.getTeamcityUrl().equals(url)) {
                userAccount.setIsActive(true);
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
            if (accountToRemove.getTeamcityUrl().equals(userAccount.getTeamcityUrl())) {
                usersContainer.getUsersAccounts().remove(accountToRemove);
            }
        }
        if (usersContainer.getUsersAccounts().size() > 0) {
            usersContainer.getUsersAccounts().get(0).setIsActive(true);
        }
        commitUserChanges();
    }

    /**
     * Save all changes
     */
    private void commitUserChanges() {
        String usersJson = new Gson().toJson(usersContainer);
        getSharedPreferences().edit().putString(SHARED_PREF_NAME, usersJson).commit();
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
}
