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

package com.github.vase4kin.teamcityapp.account.manage.data

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener
import com.github.vase4kin.teamcityapp.base.list.data.BaseListRxDataManager
import com.github.vase4kin.teamcityapp.base.list.data.BaseListRxDataManagerImpl
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage
import com.github.vase4kin.teamcityapp.storage.api.UserAccount
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

/**
 * Impl of [BaseListRxDataManagerImpl] for [com.github.vase4kin.teamcityapp.account.manage.presenter.AccountsPresenterImpl]
 */
class AccountsDataManagerImpl(
        private val sharedUserStorage: SharedUserStorage
) : BaseListRxDataManagerImpl<SharedUserStorage, UserAccount>(), AccountsDataManager {

    /**
     * {@inheritDoc}
     */
    override fun load(call: Single<SharedUserStorage>, loadingListener: OnLoadingListener<List<UserAccount>>) {
        Observable.fromIterable(sharedUserStorage.objects)
                .toList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                        onSuccess = { loadingListener.onSuccess(it) },
                        onError = { loadingListener.onFail(it.message) }
                )
                .addTo(subscriptions)
    }

    /**
     * {@inheritDoc}
     */
    override fun isLastAcccount(): Boolean {
        return sharedUserStorage.userAccounts.size == 1
    }

    /**
     * {@inheritDoc}
     */
    override fun removeAccount(account: UserAccount) {
        sharedUserStorage.removeUserAccount(account)
    }
}

interface AccountsDataManager : BaseListRxDataManager<SharedUserStorage, UserAccount> {

    fun removeAccount(account: UserAccount)

    fun isLastAcccount(): Boolean
}
