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

package com.github.vase4kin.teamcityapp.account.manage.viewmodel

import com.github.vase4kin.teamcityapp.account.manage.router.AccountListRouter
import com.github.vase4kin.teamcityapp.account.manage.tracker.ManageAccountsTracker
import com.github.vase4kin.teamcityapp.account.manage.view.AccountItem
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage
import com.xwray.groupie.Group
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder

class ManageAccountsViewModel(
    private val sharedUserStorage: SharedUserStorage,
    private val router: AccountListRouter,
    private val tracker: ManageAccountsTracker,
    val adapter: GroupAdapter<GroupieViewHolder>
) {

    fun onCreate() {
        val items: List<Group> = sharedUserStorage.userAccounts.map { AccountItem(it) }
        adapter.addAll(items)
    }

    fun onResume() {
        tracker.trackView()
    }

    fun onFabClick() {
        router.openCreateNewAccount()
    }
}