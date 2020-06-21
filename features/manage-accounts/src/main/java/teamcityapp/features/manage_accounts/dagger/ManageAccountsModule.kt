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

package teamcityapp.features.manage_accounts.dagger

import com.google.firebase.analytics.FirebaseAnalytics
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import dagger.Module
import dagger.Provides
import teamcityapp.features.manage_accounts.router.ManageAccountsRouter
import teamcityapp.features.manage_accounts.tracker.ManageAccountsTracker
import teamcityapp.features.manage_accounts.tracker.ManageAccountsTrackerImpl
import teamcityapp.features.manage_accounts.view.AccountItemFactory
import teamcityapp.features.manage_accounts.view.AccountItemFactoryImpl
import teamcityapp.features.manage_accounts.view.ManageAccountsActivity
import teamcityapp.features.manage_accounts.viewmodel.ManageAccountsViewModel
import teamcityapp.libraries.cache_manager.CacheManager
import teamcityapp.libraries.storage.Storage

@Module
object ManageAccountsModule {

    @JvmStatic
    @Provides
    fun providesViewFirebaseTracker(firebaseAnalytics: FirebaseAnalytics): ManageAccountsTracker {
        return ManageAccountsTrackerImpl(
            firebaseAnalytics
        )
    }

    @JvmStatic
    @Provides
    fun providesViewModel(
        storage: Storage,
        router: ManageAccountsRouter,
        tracker: ManageAccountsTracker,
        cacheManager: CacheManager,
        adapter: GroupAdapter<GroupieViewHolder>,
        itemsFactory: AccountItemFactory
    ): ManageAccountsViewModel {
        return ManageAccountsViewModel(
            storage,
            router,
            tracker,
            cacheManager,
            itemsFactory,
            adapter
        )
    }

    @JvmStatic
    @Provides
    fun providesAdapter() = GroupAdapter<GroupieViewHolder>()

    @JvmStatic
    @Provides
    fun provideAccountItemFactory(
        tracker: ManageAccountsTracker,
        activity: ManageAccountsActivity
    ): AccountItemFactory {
        val showSslDisabledInfoDialog: () -> Unit = { activity.showSslDisabledInfoDialog() }
        val showRemoveAccountDialog: (onAccountRemove: () -> Unit) -> Unit = {
            activity.showRemoveAccountDialog(it)
        }
        return AccountItemFactoryImpl(
            tracker = tracker,
            showRemoveAccountDialog = showRemoveAccountDialog,
            showSslDisabledInfoDialog = showSslDisabledInfoDialog
        )
    }
}
