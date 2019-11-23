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

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.github.vase4kin.teamcityapp.account.manage.router.ManageAccountsRouter
import com.github.vase4kin.teamcityapp.account.manage.tracker.ManageAccountsTracker
import com.github.vase4kin.teamcityapp.account.manage.view.AccountItem
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage
import com.xwray.groupie.Group
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import teamcityapp.cache_manager.CacheManager
import teamcityapp.libraries.storage.models.UserAccount

class ManageAccountsViewModel(
    private val sharedUserStorage: SharedUserStorage,
    private val router: ManageAccountsRouter,
    private val tracker: ManageAccountsTracker,
    private val showSslDisabledInfoDialog: () -> Unit,
    private val showRemoveAccountDialog: (onAccountRemove: () -> Unit) -> Unit,
    private val cacheManager: CacheManager,
    val adapter: GroupAdapter<GroupieViewHolder>
) : LifecycleObserver {

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {
        val items: List<Group> = createItems()
        adapter.addAll(items)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        tracker.trackView()
    }

    fun onFabClick() {
        router.openCreateNewAccount()
    }

    private fun createItems(): List<Group> = sharedUserStorage.userAccounts.map {
        AccountItem(
            userAccount = it,
            showSslDisabledInfoDialog = {
                tracker.trackUserClicksOnSslDisabledWarning()
                showSslDisabledInfoDialog()
            },
            showRemoveAccountDialog = {
                showRemoveAccountDialog(onAccountRemove(it))
            })
    }

    private fun onAccountRemove(account: UserAccount): () -> Unit = {
        when {
            sharedUserStorage.userAccounts.size == 1 -> {
                tracker.trackAccountRemove()
                sharedUserStorage.removeUserAccount(account)
                cacheManager.evictAllCache()
                router.openLogin()
            }
            account.isActive -> {
                tracker.trackAccountRemove()
                sharedUserStorage.removeUserAccount(account)
                sharedUserStorage.setOtherUserActive()
                router.openHome()
            }
            else -> {
                tracker.trackAccountRemove()
                sharedUserStorage.removeUserAccount(account)
                val items: List<Group> = createItems()
                adapter.updateAsync(items)
            }
        }
    }
}
