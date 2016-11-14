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

package com.github.vase4kin.teamcityapp.account.manage.presenter;

import android.support.annotation.NonNull;

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener;
import com.github.vase4kin.teamcityapp.account.manage.data.AccountDataModel;
import com.github.vase4kin.teamcityapp.account.manage.data.AccountDataModelImpl;
import com.github.vase4kin.teamcityapp.account.manage.tracker.ManageAccountsTracker;
import com.github.vase4kin.teamcityapp.account.manage.view.AccountsView;
import com.github.vase4kin.teamcityapp.account.manage.view.OnAccountRemoveListener;
import com.github.vase4kin.teamcityapp.base.list.data.BaseListRxDataManager;
import com.github.vase4kin.teamcityapp.base.list.extractor.BaseValueExtractor;
import com.github.vase4kin.teamcityapp.base.list.presenter.BaseListPresenterImpl;
import com.github.vase4kin.teamcityapp.storage.api.UserAccount;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;

/**
 * Presenter impl for managing {@link com.github.vase4kin.teamcityapp.account.manage.view.AccountListActivity}
 */
public class AccountsPresenterImpl extends BaseListPresenterImpl<
        AccountDataModel,
        UserAccount,
        AccountsView,
        BaseListRxDataManager,
        ManageAccountsTracker,
        BaseValueExtractor> implements OnAccountRemoveListener {

    @Inject
    AccountsPresenterImpl(@NonNull AccountsView view,
                          @NonNull BaseListRxDataManager dataManager,
                          @NonNull ManageAccountsTracker tracker,
                          @NonNull BaseValueExtractor valueExtractor) {
        super(view, dataManager, tracker, valueExtractor);
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void loadData(@NonNull OnLoadingListener<List<UserAccount>> loadingListener) {
        mDataManager.load(Observable.empty(), loadingListener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected AccountDataModel createModel(List<UserAccount> data) {
        return new AccountDataModelImpl(data);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onAccountRemove() {
        mTracker.trackAccountRemove();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initViews() {
        super.initViews();
        mView.setOnAccountRemoveListener(this);
    }
}
