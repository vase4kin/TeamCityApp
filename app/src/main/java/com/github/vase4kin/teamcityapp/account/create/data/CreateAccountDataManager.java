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

package com.github.vase4kin.teamcityapp.account.create.data;

import android.support.annotation.NonNull;

/**
 * Handling create account data
 */
public interface CreateAccountDataManager {

    /**
     * @param listener - to receive callbacks on {@link com.github.vase4kin.teamcityapp.account.create.presenter.CreateAccountPresenterImpl}
     * @param url      - TeamCity server url
     * @param userName - Username
     * @param password - Password
     * @param isGuestUser - Guest user
     */
    @Deprecated
    void loadData(@NonNull CustomOnLoadingListener<String> listener, String url, String userName, String password, boolean isGuestUser);

    /**
     * Server auth with credentials
     *
     * @param listener - to receive callbacks on {@link com.github.vase4kin.teamcityapp.account.create.presenter.CreateAccountPresenterImpl}
     * @param url      - TeamCity server url
     * @param userName - Username
     * @param password - Password
     */
    void authUser(@NonNull CustomOnLoadingListener<String> listener, String url, String userName, String password);

    /**
     * Server guest auth
     *
     * @param listener - to receive callbacks on {@link com.github.vase4kin.teamcityapp.account.create.presenter.CreateAccountPresenterImpl}
     * @param url      - TeamCity server url
     */
    void authGuestUser(@NonNull CustomOnLoadingListener<String> listener, String url);

    /**
     * Save user account in the local storage
     *
     * @param url - TeamCity server url
     * @param userName - User name
     * @param password - Password
     */
    void saveNewUserAccount(String serverUrl, String userName, String password);

    /**
     * Save guest user account in the local storage
     *
     * @param url - TeamCity server url
     */
    void saveGuestUserAccount(String url);

    /**
     * Init {@link com.github.vase4kin.teamcityapp.dagger.components.RestApiComponent}
     *
     * @param url - TeamCity server url
     */
    void initTeamCityService(String url);
}
