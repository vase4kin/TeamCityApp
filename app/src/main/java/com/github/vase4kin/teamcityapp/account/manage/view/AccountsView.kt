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

package com.github.vase4kin.teamcityapp.account.manage.view

import com.github.vase4kin.teamcityapp.account.manage.data.AccountDataModel
import com.github.vase4kin.teamcityapp.base.list.view.BaseListView
import com.github.vase4kin.teamcityapp.storage.api.UserAccount

/**
 * Manages view interactions
 */
interface AccountsView : BaseListView<AccountDataModel> {

    /**
     * Set listener to handle callbacks
     *
     * @param listener - Listener
     */
    fun setOnViewListener(listener: ViewListener)

    fun showRemoveAccountDialog(account: UserAccount, isLastAccount: Boolean)

    fun removeAccount(userAccount: UserAccount)

    /**
     * Listener to handle account events
     */
    interface ViewListener {

        /**
         * On active account remove event
         */
        fun onActiveAccountRemoved(account: UserAccount)

        /**
         * On not account remove event
         */
        fun onNotActiveAccountRemoved(account: UserAccount)

        /**
         * On last account remove event
         */
        fun onLastAccountRemoved(account: UserAccount)

        /**
         * On account click
         */
        fun onAccountClick(account: UserAccount)
    }
}
