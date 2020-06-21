/*
 * Copyright 2020 Andrey Tolpeev
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

package teamcityapp.features.manage_accounts.viewmodel

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.xwray.groupie.Group
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import teamcityapp.features.manage_accounts.router.ManageAccountsRouter
import teamcityapp.features.manage_accounts.tracker.ManageAccountsTracker
import teamcityapp.features.manage_accounts.view.AccountItemFactory
import teamcityapp.libraries.cache_manager.CacheManager
import teamcityapp.libraries.storage.Storage
import teamcityapp.libraries.storage.models.UserAccount

class ManageAccountsViewModel(
    private val storage: Storage,
    private val router: ManageAccountsRouter,
    private val tracker: ManageAccountsTracker,
    private val cacheManager: CacheManager,
    private val itemFactory: AccountItemFactory,
    val adapter: GroupAdapter<GroupieViewHolder>
) : LifecycleObserver {

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {
        val items: List<Group> = createItems()
        adapter.updateAsync(items)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        tracker.trackView()
    }

    fun onFabClick() {
        router.openCreateNewAccount()
    }

    private fun createItems(): List<Group> = storage.userAccounts.map {
        itemFactory.create(
            userAccount = it,
            onAccountRemove = onAccountRemove(it)
        )
    }

    @VisibleForTesting
    internal fun onAccountRemove(account: UserAccount): () -> Unit = {
        when {
            storage.userAccounts.size == 1 -> {
                tracker.trackAccountRemove()
                storage.removeUserAccount(account)
                cacheManager.evictAllCache()
                router.openLogin()
            }
            account.isActive -> {
                tracker.trackAccountRemove()
                storage.removeUserAccount(account)
                storage.setOtherUserActive()
                router.openHome()
            }
            else -> {
                tracker.trackAccountRemove()
                storage.removeUserAccount(account)
                val items: List<Group> = createItems()
                adapter.updateAsync(items)
            }
        }
    }
}
