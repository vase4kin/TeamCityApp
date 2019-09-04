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

package com.github.vase4kin.teamcityapp.account.create.data

/**
 * Handling create account data
 */
interface CreateAccountDataManager {

    /**
     * Server auth with credentials
     *
     * @param listener - to receive callbacks on [com.github.vase4kin.teamcityapp.account.create.presenter.CreateAccountPresenterImpl]
     * @param url - TeamCity server url
     * @param userName - Username
     * @param password - Password
     * @param isSslDisabled - ssl state
     */
    fun authUser(
        listener: CustomOnLoadingListener<String>,
        url: String,
        userName: String,
        password: String,
        isSslDisabled: Boolean,
        checkSecureConnection: Boolean
    )

    /**
     * Server guest auth
     *
     * @param listener - to receive callbacks on [com.github.vase4kin.teamcityapp.account.create.presenter.CreateAccountPresenterImpl]
     * @param url - TeamCity server url
     * @param isSslDisabled - ssl state
     */
    fun authGuestUser(
        listener: CustomOnLoadingListener<String>,
        url: String,
        isSslDisabled: Boolean,
        checkSecureConnection: Boolean
    )

    /**
     * Save user account in the local storage
     *
     * @param url - TeamCity server url
     * @param userName - User name
     * @param password - Password
     * @param isSslDisabled - ssl state
     * @param listener to receive data save callbacks§
     */
    fun saveNewUserAccount(
        serverUrl: String,
        userName: String,
        password: String,
        isSslDisabled: Boolean,
        listener: OnLoadingListener<String>
    )

    /**
     * Save guest user account in the local storage
     *
     * @param url - TeamCity server url
     * @param isSslDisabled - ssl state
     */
    fun saveGuestUserAccount(url: String, isSslDisabled: Boolean)

    /**
     * Init [com.github.vase4kin.teamcityapp.dagger.components.RestApiComponent]
     *
     * @param url - TeamCity server url
     */
    fun initTeamCityService(url: String)

    companion object {

        /**
         * Code for not secure http
         */
        const val ERROR_CODE_HTTP_NOT_SECURE = 1001
    }
}
