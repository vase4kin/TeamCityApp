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

package com.github.vase4kin.teamcityapp.account.manage.presenter

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener
import com.github.vase4kin.teamcityapp.account.manage.data.AccountDataModel
import com.github.vase4kin.teamcityapp.account.manage.data.AccountDataModelImpl
import com.github.vase4kin.teamcityapp.account.manage.data.AccountsDataManager
import com.github.vase4kin.teamcityapp.account.manage.router.AccountListRouter
import com.github.vase4kin.teamcityapp.account.manage.tracker.ManageAccountsTracker
import com.github.vase4kin.teamcityapp.account.manage.view.AccountsView
import com.github.vase4kin.teamcityapp.base.list.extractor.BaseValueExtractor
import com.github.vase4kin.teamcityapp.base.list.presenter.BaseListPresenterImpl
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage
import com.github.vase4kin.teamcityapp.storage.api.UserAccount
import io.reactivex.Observable
import javax.inject.Inject

/**
 * Presenter impl for managing [com.github.vase4kin.teamcityapp.account.manage.view.AccountListActivity]
 */
class AccountsPresenterImpl @Inject constructor(
        view: AccountsView,
        dataManager: AccountsDataManager,
        tracker: ManageAccountsTracker,
        valueExtractor: BaseValueExtractor,
        private val router: AccountListRouter) : BaseListPresenterImpl<AccountDataModel, UserAccount, AccountsView, AccountsDataManager, ManageAccountsTracker, BaseValueExtractor>(view, dataManager, tracker, valueExtractor), AccountsView.ViewListener {

    /**
     * {@inheritDoc}
     */
    override fun loadData(loadingListener: OnLoadingListener<List<UserAccount>>, update: Boolean) {
        dataManager.load(Observable.empty<SharedUserStorage>().singleOrError(), loadingListener)
    }

    /**
     * {@inheritDoc}
     */
    override fun createModel(data: List<UserAccount>): AccountDataModel {
        return AccountDataModelImpl(data)
    }

    /**
     * {@inheritDoc}
     */
    override fun onActiveAccountRemoved(account: UserAccount) {
        tracker.trackAccountRemove()
        view.removeAccount(account)
        dataManager.removeAccount(account)
        dataManager.makeAnotherAccountActive()
        router.openHome()
    }

    /**
     * {@inheritDoc}
     */
    override fun onNotActiveAccountRemoved(account: UserAccount) {
        tracker.trackAccountRemove()
        view.removeAccount(account)
        dataManager.removeAccount(account)
    }

    override fun onLastAccountRemoved(account: UserAccount) {
        tracker.trackAccountRemove()
        view.removeAccount(account)
        dataManager.removeAccount(account)
        router.openLogin()
    }

    /**
     * {@inheritDoc}
     */
    override fun initViews() {
        super.initViews()
        view.setOnViewListener(this)
    }

    /**
     * {@inheritDoc}
     */
    override fun onAccountClick(account: UserAccount) {
        view.showRemoveAccountDialog(account, dataManager.isLastAcccount())
    }
}
