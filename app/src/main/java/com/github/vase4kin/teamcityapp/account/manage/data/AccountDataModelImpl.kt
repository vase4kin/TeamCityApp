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

import com.github.vase4kin.teamcityapp.storage.api.UserAccount

/**
 * Impl of [AccountDataModel]
 */
class AccountDataModelImpl(private val accounts: MutableList<UserAccount>) : AccountDataModel {

    /**
     * {@inheritDoc}
     */
    override fun getTeamcityUrl(position: Int): String {
        return accounts[position].teamcityUrl
    }

    /**
     * {@inheritDoc}
     */
    override fun getUserName(position: Int): String {
        return accounts[position].userName
    }

    /**
     * {@inheritDoc}
     */
    override fun sort() {
        accounts.sortWith(Comparator { o1, o2 -> if (o1.isActive == o2.isActive) 0 else if (o2.isActive) 1 else -1 })
    }

    /**
     * {@inheritDoc}
     */
    override fun add(account: UserAccount) {
        accounts.add(account)
    }

    /**
     * {@inheritDoc}
     */
    override fun remove(account: UserAccount) {
        accounts.remove(account)
    }

    /**
     * {@inheritDoc}
     */
    override fun get(position: Int): UserAccount {
        return accounts[position]
    }

    /**
     * {@inheritDoc}
     */
    override fun getItemCount(): Int {
        return accounts.size
    }
}
