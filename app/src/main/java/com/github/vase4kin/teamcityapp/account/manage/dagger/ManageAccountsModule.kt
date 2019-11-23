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

package com.github.vase4kin.teamcityapp.account.manage.dagger

import com.github.vase4kin.teamcityapp.account.manage.router.ManageAccountsRouter
import com.github.vase4kin.teamcityapp.account.manage.router.ManageAccountsRouterImpl
import com.github.vase4kin.teamcityapp.account.manage.tracker.ManageAccountsTracker
import com.github.vase4kin.teamcityapp.account.manage.tracker.ManageAccountsTrackerImpl
import com.github.vase4kin.teamcityapp.account.manage.view.ManageAccountsActivity
import com.github.vase4kin.teamcityapp.account.manage.viewmodel.ManageAccountsViewModel
import com.google.firebase.analytics.FirebaseAnalytics
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import dagger.Module
import dagger.Provides
import teamcityapp.cache_manager.CacheManager
import teamcityapp.libraries.storage.Storage

@Module
class ManageAccountsModule {

    @Provides
    fun providesViewFirebaseTracker(firebaseAnalytics: FirebaseAnalytics): ManageAccountsTracker {
        return ManageAccountsTrackerImpl(firebaseAnalytics)
    }

    @Provides
    fun provideAccountListRouter(activity: ManageAccountsActivity): ManageAccountsRouter {
        return ManageAccountsRouterImpl(activity)
    }

    @Provides
    fun providesViewModel(
        activity: ManageAccountsActivity,
        storage: Storage,
        router: ManageAccountsRouter,
        tracker: ManageAccountsTracker,
        cacheManager: CacheManager,
        adapter: GroupAdapter<GroupieViewHolder>
    ): ManageAccountsViewModel {
        val showSslDisabledInfoDialog: () -> Unit = { activity.showSslDisabledInfoDialog() }
        val showRemoveAccountDialog: (onAccountRemove: () -> Unit) -> Unit = {
            activity.showRemoveAccountDialog(it)
        }
        return ManageAccountsViewModel(
            storage,
            router,
            tracker,
            showSslDisabledInfoDialog,
            showRemoveAccountDialog,
            cacheManager,
            adapter
        )
    }

    @Provides
    fun providesAdapter() = GroupAdapter<GroupieViewHolder>()
}
