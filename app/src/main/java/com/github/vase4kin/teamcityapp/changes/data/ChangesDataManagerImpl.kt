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

package com.github.vase4kin.teamcityapp.changes.data

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener
import com.github.vase4kin.teamcityapp.api.Repository
import com.github.vase4kin.teamcityapp.base.list.data.BaseListRxDataManagerImpl
import com.github.vase4kin.teamcityapp.base.tabs.data.OnTextTabChangeEvent
import com.github.vase4kin.teamcityapp.build_details.presenter.BuildDetailsPresenter
import com.github.vase4kin.teamcityapp.changes.api.Changes
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus

/**
 * Impl of [ChangesDataManager]
 */
class ChangesDataManagerImpl(
    private val repository: Repository,
    private val eventBus: EventBus
) : BaseListRxDataManagerImpl<Changes, Changes.Change>(), ChangesDataManager {

    private var loadMoreUrl: String? = null

    /**
     * {@inheritDoc}
     */
    override fun loadTabTitle(
        url: String,
        loadingListener: OnLoadingListener<Int>
    ) {
        repository.listChanges(url + ",count:" + Integer.MAX_VALUE + "&fields=count", true)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                        onSuccess = { loadingListener.onSuccess(it.count) },
                        onError = { loadingListener.onSuccess(0) }
                )
                .addTo(subscriptions)
    }

    /**
     * {@inheritDoc}
     */
    override fun loadLimited(
        url: String,
        loadingListener: OnLoadingListener<List<Changes.Change>>,
        update: Boolean
    ) {
        load("$url,count:10", loadingListener, update)
    }

    /**
     * {@inheritDoc}
     */
    override fun load(
        url: String,
        loadingListener: OnLoadingListener<List<Changes.Change>>,
        update: Boolean
    ) {
        repository.listChanges(url, update)
                .subscribeOn(Schedulers.io())
                .flatMapObservable {
                    if (it.count == 0) {
                        Observable.fromIterable(emptyList())
                    } else {
                        loadMoreUrl = it.nextHref
                        Observable.fromIterable(it.objects)
                    }
                }
                .flatMapSingle { repository.change(it.href) }
                .toList()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                        onSuccess = { loadingListener.onSuccess(it) },
                        onError = { loadingListener.onFail(it.message ?: "") }
                ).addTo(subscriptions)
    }

    /**
     * {@inheritDoc}
     */
    override fun loadMore(loadingListener: OnLoadingListener<List<Changes.Change>>) {
        load(loadMoreUrl!!, loadingListener, false)
    }

    /**
     * {@inheritDoc}
     */
    override fun canLoadMore(): Boolean {
        return loadMoreUrl != null
    }

    /**
     * {@inheritDoc}
     */
    override fun postChangeTabTitleEvent(size: Int?) {
        eventBus.post(OnTextTabChangeEvent(size!!, BuildDetailsPresenter.CHANGES_TAB))
    }
}
