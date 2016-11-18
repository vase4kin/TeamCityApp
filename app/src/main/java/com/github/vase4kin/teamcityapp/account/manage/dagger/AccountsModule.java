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
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.account.manage.data.AccountDataModel;
import com.github.vase4kin.teamcityapp.account.manage.data.AccountsDataManagerImpl;
import com.github.vase4kin.teamcityapp.account.manage.tracker.AccountsTrackerImpl;
import com.github.vase4kin.teamcityapp.account.manage.tracker.ManageAccountsTracker;
import com.github.vase4kin.teamcityapp.account.manage.view.AccountAdapter;
import com.github.vase4kin.teamcityapp.account.manage.view.AccountViewHolderFactory;
import com.github.vase4kin.teamcityapp.account.manage.view.AccountsView;
import com.github.vase4kin.teamcityapp.account.manage.view.AccountsViewImpl;
import com.github.vase4kin.teamcityapp.base.list.data.BaseListRxDataManager;
import com.github.vase4kin.teamcityapp.base.list.extractor.BaseValueExtractor;
import com.github.vase4kin.teamcityapp.base.list.view.BaseListView;
import com.github.vase4kin.teamcityapp.base.list.view.SimpleSectionedRecyclerViewAdapter;
import com.github.vase4kin.teamcityapp.base.list.view.ViewHolderFactory;
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage;

import java.util.Map;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntKey;
import dagger.multibindings.IntoMap;

@Module
public class AccountsModule {

    private View mView;
    private AppCompatActivity mActivity;

    public AccountsModule(View mView, AppCompatActivity mActivity) {
        this.mView = mView;
        this.mActivity = mActivity;
    }

    @Provides
    BaseListRxDataManager providesBaseListRxDataManager(SharedUserStorage sharedUserStorage) {
        return new AccountsDataManagerImpl(sharedUserStorage);
    }

    @Provides
    AccountsView providesAccountsView(SharedUserStorage sharedUserStorage, SimpleSectionedRecyclerViewAdapter<AccountAdapter> adapter) {
        return new AccountsViewImpl(mView, mActivity, sharedUserStorage, R.string.empty_list_message_accounts, adapter);
    }

    @Provides
    ManageAccountsTracker providesViewTracker() {
        return new AccountsTrackerImpl();
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
    SimpleSectionedRecyclerViewAdapter<AccountAdapter> providesSimpleSectionedRecyclerViewAdapter(Context context, AccountAdapter adapter) {
        return new SimpleSectionedRecyclerViewAdapter<>(context, adapter);
    }

    @IntoMap
    @IntKey(BaseListView.TYPE_DEFAULT)
    @Provides
    ViewHolderFactory<AccountDataModel> providesAccountViewHolderFactory() {
        return new AccountViewHolderFactory();
    }
}
