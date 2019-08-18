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

import android.content.Context
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.account.manage.data.AccountDataModel
import com.github.vase4kin.teamcityapp.account.manage.data.AccountsDataManager
import com.github.vase4kin.teamcityapp.account.manage.data.AccountsDataManagerImpl
import com.github.vase4kin.teamcityapp.account.manage.router.AccountListRouter
import com.github.vase4kin.teamcityapp.account.manage.router.AccountListRouterImpl
import com.github.vase4kin.teamcityapp.account.manage.tracker.FirebaseManageAccountsTrackerImpl
import com.github.vase4kin.teamcityapp.account.manage.tracker.ManageAccountsTracker
import com.github.vase4kin.teamcityapp.account.manage.view.*
import com.github.vase4kin.teamcityapp.base.list.extractor.BaseValueExtractor
import com.github.vase4kin.teamcityapp.base.list.view.BaseListView
import com.github.vase4kin.teamcityapp.base.list.view.SimpleSectionedRecyclerViewAdapter
import com.github.vase4kin.teamcityapp.base.list.view.ViewHolderFactory
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntKey
import dagger.multibindings.IntoMap

@Module
class AccountsModule {

    @Provides
    fun providesBaseListRxDataManager(sharedUserStorage: SharedUserStorage): AccountsDataManager {
        return AccountsDataManagerImpl(sharedUserStorage)
    }

    @Provides
    fun providesAccountsView(
        activity: AccountListActivity,
        adapter: SimpleSectionedRecyclerViewAdapter<AccountAdapter>
    ): AccountsView {
        return AccountsViewImpl(activity.findViewById(android.R.id.content), activity, R.string.empty_list_message_accounts, adapter)
    }

    @Provides
    fun providesBaseValueExtractor(): BaseValueExtractor {
        return BaseValueExtractor.STUB
    }

    @Provides
    fun providesAccountAdapter(viewHolderFactories: Map<Int, @JvmSuppressWildcards ViewHolderFactory<AccountDataModel>>): AccountAdapter {
        return AccountAdapter(viewHolderFactories)
    }

    @Provides
    fun providesSimpleSectionedRecyclerViewAdapter(
        context: Context,
        adapter: AccountAdapter
    ): SimpleSectionedRecyclerViewAdapter<AccountAdapter> {
        return SimpleSectionedRecyclerViewAdapter(context, adapter)
    }

    @IntoMap
    @IntKey(BaseListView.TYPE_DEFAULT)
    @Provides
    fun providesAccountViewHolderFactory(): ViewHolderFactory<AccountDataModel> {
        return AccountViewHolderFactory()
    }

    @Provides
    fun providesViewFirebaseTracker(firebaseAnalytics: FirebaseAnalytics): ManageAccountsTracker {
        return FirebaseManageAccountsTrackerImpl(firebaseAnalytics)
    }

    @Provides
    fun provideAccountListRouter(activity: AccountListActivity): AccountListRouter {
        return AccountListRouterImpl(activity)
    }
}
