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
 * Model to handle interactions with local storage
 */
interface CreateAccountDataModel {

    /**
     * Check if local storage has guest account with the same data
     *
     * @param url - TeamCity server url
     * @return boolean if account exists or not
     */
    fun hasGuestAccountWithUrl(url: String): Boolean

    /**
     * Check if local storage has the account with the same data
     *
     * @param url      - TeamCity server url
     * @param userName - User name
     * @return boolean if account exists or not
     */
    fun hasAccountWithUrl(url: String, userName: String): Boolean
}
