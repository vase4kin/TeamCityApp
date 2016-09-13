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

import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.account.manage.data.AccountsDataManagerImpl;
import com.github.vase4kin.teamcityapp.account.manage.tracker.AccountsTrackerImpl;
import com.github.vase4kin.teamcityapp.account.manage.view.AccountsView;
import com.github.vase4kin.teamcityapp.account.manage.view.AccountsViewImpl;
import com.github.vase4kin.teamcityapp.base.list.data.BaseListRxDataManager;
import com.github.vase4kin.teamcityapp.navigation.tracker.ViewTracker;
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage;

import dagger.Module;
import dagger.Provides;

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
    AccountsView providesAccountsView(SharedUserStorage sharedUserStorage) {
        return new AccountsViewImpl(mView, mActivity, sharedUserStorage, R.string.empty_list_message_accounts);
    }

    @Provides
    ViewTracker providesViewTracker() {
        return new AccountsTrackerImpl();
    }
}
