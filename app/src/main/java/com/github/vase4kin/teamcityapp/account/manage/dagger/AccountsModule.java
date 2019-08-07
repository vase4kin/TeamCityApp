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

package com.github.vase4kin.teamcityapp.account.manage.dagger;

import android.content.Context;

import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.account.manage.data.AccountDataModel;
import com.github.vase4kin.teamcityapp.account.manage.data.AccountsDataManager;
import com.github.vase4kin.teamcityapp.account.manage.data.AccountsDataManagerImpl;
import com.github.vase4kin.teamcityapp.account.manage.router.AccountListRouter;
import com.github.vase4kin.teamcityapp.account.manage.router.AccountListRouterImpl;
import com.github.vase4kin.teamcityapp.account.manage.tracker.FirebaseManageAccountsTrackerImpl;
import com.github.vase4kin.teamcityapp.account.manage.tracker.ManageAccountsTracker;
import com.github.vase4kin.teamcityapp.account.manage.view.AccountAdapter;
import com.github.vase4kin.teamcityapp.account.manage.view.AccountListActivity;
import com.github.vase4kin.teamcityapp.account.manage.view.AccountViewHolderFactory;
import com.github.vase4kin.teamcityapp.account.manage.view.AccountsView;
import com.github.vase4kin.teamcityapp.account.manage.view.AccountsViewImpl;
import com.github.vase4kin.teamcityapp.base.list.extractor.BaseValueExtractor;
import com.github.vase4kin.teamcityapp.base.list.view.BaseListView;
import com.github.vase4kin.teamcityapp.base.list.view.SimpleSectionedRecyclerViewAdapter;
import com.github.vase4kin.teamcityapp.base.list.view.ViewHolderFactory;
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.Map;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntKey;
import dagger.multibindings.IntoMap;

@Module
public class AccountsModule {

    @Provides
    AccountsDataManager providesBaseListRxDataManager(SharedUserStorage sharedUserStorage) {
        return new AccountsDataManagerImpl(sharedUserStorage);
    }

    @Provides
    AccountsView providesAccountsView(AccountListActivity activity,
                                      SimpleSectionedRecyclerViewAdapter<AccountAdapter> adapter) {
        return new AccountsViewImpl(activity.findViewById(android.R.id.content), activity, R.string.empty_list_message_accounts, adapter);
    }

    @Provides
    BaseValueExtractor providesBaseValueExtractor() {
        return BaseValueExtractor.STUB;
    }

    @Provides
    AccountAdapter providesAccountAdapter(Map<Integer, ViewHolderFactory<AccountDataModel>> viewHolderFactories) {
        return new AccountAdapter(viewHolderFactories);
    }

    @Provides
    SimpleSectionedRecyclerViewAdapter<AccountAdapter> providesSimpleSectionedRecyclerViewAdapter(Context context,
                                                                                                  AccountAdapter adapter) {
        return new SimpleSectionedRecyclerViewAdapter<>(context, adapter);
    }

    @IntoMap
    @IntKey(BaseListView.TYPE_DEFAULT)
    @Provides
    ViewHolderFactory<AccountDataModel> providesAccountViewHolderFactory() {
        return new AccountViewHolderFactory();
    }

    @Provides
    ManageAccountsTracker providesViewFirebaseTracker(FirebaseAnalytics firebaseAnalytics) {
        return new FirebaseManageAccountsTrackerImpl(firebaseAnalytics);
    }

    @Provides
    AccountListRouter provideAccountListRouter(AccountListActivity activity) {
        return new AccountListRouterImpl(activity);
    }
}
