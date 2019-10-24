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

package com.github.vase4kin.teamcityapp.account.manage.data

import com.github.vase4kin.teamcityapp.base.list.view.BaseDataModel
import com.github.vase4kin.teamcityapp.storage.api.UserAccount

/**
 * Manages data in [com.github.vase4kin.teamcityapp.account.manage.view.AccountAdapter]
 */
interface AccountDataModel : BaseDataModel {

    /**
     * Get TeamCity server url
     *
     * @param position adapter position
     * @return TeamCity server url
     */
    fun getTeamcityUrl(position: Int): String

    /**
     * Get account user name
     *
     * @param position adapter position
     * @return Account user name
     */
    fun getUserName(position: Int): String

    /**
     * Sort model data with active account first
     */
    fun sort()

    /**
     * Add TeamCity account to the model
     *
     * @param account TeamCity account
     */
    fun add(account: UserAccount)

    /**
     * Remove TeamCity account from the model
     *
     * @param account TeamCity account
     */
    fun remove(account: UserAccount)

    /**
     * Get User account object
     *
     * @param position adapter postion
     * @return [UserAccount]
     */
    operator fun get(position: Int): UserAccount

    /**
     * Is ssl enabled for an account
     */
    fun isSslDisabled(position: Int): Boolean
}
