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

package com.github.vase4kin.teamcityapp.new_drawer.viewmodel

import com.github.vase4kin.teamcityapp.custom_tabs.ChromeCustomTabs
import com.github.vase4kin.teamcityapp.new_drawer.view.AboutDrawerItem
import com.github.vase4kin.teamcityapp.new_drawer.view.AccountDrawerItem
import com.github.vase4kin.teamcityapp.new_drawer.view.AccountsDividerDrawerItem
import com.github.vase4kin.teamcityapp.new_drawer.view.BaseDrawerItem
import com.github.vase4kin.teamcityapp.new_drawer.view.BottomDrawerItem
import com.github.vase4kin.teamcityapp.new_drawer.view.DividerDrawerItem
import com.github.vase4kin.teamcityapp.new_drawer.view.ManageAccountsDrawerItem
import com.github.vase4kin.teamcityapp.new_drawer.view.NewAccountDrawerItem
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage

class DrawerViewModel(
    private val sharedUserStorage: SharedUserStorage,
    private val chromeCustomTabs: ChromeCustomTabs,
    private val setAdapter: (items: List<BaseDrawerItem>) -> Unit
) {

    fun onViewCreated() {
        chromeCustomTabs.initCustomsTabs()
        initDrawer()
    }

    private fun initDrawer() {
        // Set title
        val activeAccounts = sharedUserStorage.userAccounts.filter {
            it.isActive
        }.map { AccountDrawerItem(it) }
        val otherAccounts = sharedUserStorage.userAccounts.filterNot {
            it.isActive
        }.map { AccountDrawerItem(it) }
        // add user account to list
        val list = mutableListOf<BaseDrawerItem>()
        list.addAll(activeAccounts)
        if (otherAccounts.isNotEmpty()) {
            list.add(AccountsDividerDrawerItem())
            list.addAll(otherAccounts)
        }
        // add new account item
        list.add(NewAccountDrawerItem())
        // add manage account items
        list.add(ManageAccountsDrawerItem())
        list.add(DividerDrawerItem())
        // add about item
        list.add(AboutDrawerItem())
        list.add(DividerDrawerItem())
        // add bottom
        list.add(BottomDrawerItem())

        // set adapter
        setAdapter(list)
        // set links to open by chrome tabs
    }

    fun onDestroyView() {
        chromeCustomTabs.unbindCustomsTabs()
    }
}
