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

package com.github.vase4kin.teamcityapp.account.manage.data;

import android.support.annotation.NonNull;

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener;
import com.github.vase4kin.teamcityapp.base.list.data.BaseListRxDataManagerImpl;
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage;
import com.github.vase4kin.teamcityapp.storage.api.UserAccount;

import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Impl of {@link BaseListRxDataManagerImpl} for {@link com.github.vase4kin.teamcityapp.account.manage.presenter.AccountsPresenterImpl}
 */
public class AccountsDataManagerImpl extends BaseListRxDataManagerImpl<SharedUserStorage, UserAccount> {

    private SharedUserStorage mSharedUserStorage;

    public AccountsDataManagerImpl(SharedUserStorage sharedUserStorage) {
        this.mSharedUserStorage = sharedUserStorage;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void load(@NonNull Observable<SharedUserStorage> call, @NonNull final OnLoadingListener<List<UserAccount>> loadingListener) {
        Observable.from(mSharedUserStorage.getObjects())
                .toList()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<UserAccount>>() {
                    @Override
                    public void call(List<UserAccount> userAccounts) {
                        loadingListener.onSuccess(userAccounts);
                    }
                });
    }
}
