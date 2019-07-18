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

package com.github.vase4kin.teamcityapp.tests.data

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener
import com.github.vase4kin.teamcityapp.api.Repository
import com.github.vase4kin.teamcityapp.base.list.data.BaseListRxDataManagerImpl
import com.github.vase4kin.teamcityapp.base.tabs.data.OnTextTabChangeEvent
import com.github.vase4kin.teamcityapp.build_details.presenter.BuildDetailsPresenter
import com.github.vase4kin.teamcityapp.tests.api.TestOccurrences
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

import org.greenrobot.eventbus.EventBus

/**
 * Impl of [TestsDataManager]
 */
class TestsDataManagerImpl(private val repository: Repository,
                           private val eventBus: EventBus
) : BaseListRxDataManagerImpl<TestOccurrences, TestOccurrences.TestOccurrence>(), TestsDataManager {

    private var mLoadMoreUrl: String? = null
    private var mLoadingListener: OnLoadingListener<List<TestOccurrences.TestOccurrence>>? = null

    /**
     * {@inheritDoc}
     */
    override fun load(url: String,
                      loadingListener: OnLoadingListener<List<TestOccurrences.TestOccurrence>>,
                      update: Boolean) {
        mLoadingListener = loadingListener
        loadTests(url, loadingListener, update)
    }

    /**
     * Load test
     *
     * @param url             - Tests url
     * @param update          - Force cache update
     * @param loadingListener - Listener to receive server callbacks
     */
    private fun loadTests(url: String,
                          loadingListener: OnLoadingListener<List<TestOccurrences.TestOccurrence>>,
                          update: Boolean) {
        repository.listTestOccurrences(url, update)
                .subscribeOn(Schedulers.io())
                .flatMapObservable {
                    mLoadMoreUrl = it.nextHref
                    Observable.fromIterable(it.objects)
                }
                .toList()
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
    override fun loadFailedTests(url: String,
                                 loadingListener: OnLoadingListener<List<TestOccurrences.TestOccurrence>>,
                                 update: Boolean) {
        load("$url,status:FAILURE,count:10", loadingListener, update)
    }

    /**
     * {@inheritDoc}
     */
    override fun loadFailedTests(url: String) {
        loadFailedTests(url, mLoadingListener!!, false)
    }

    /**
     * {@inheritDoc}
     */
    override fun loadIgnoredTests(url: String) {
        load("$url,status:UNKNOWN,count:10", mLoadingListener!!, false)
    }

    /**
     * {@inheritDoc}
     */
    override fun loadPassedTests(url: String) {
        load("$url,status:SUCCESS,count:10", mLoadingListener!!, false)
    }

    /**
     * {@inheritDoc}
     */
    override fun loadTestDetails(url: String, loadingListener: OnLoadingListener<Int>) {
        repository.listTestOccurrences(url + ",count:" + Integer.MAX_VALUE + "&fields=count", true)
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
    override fun loadMore(loadingListener: OnLoadingListener<List<TestOccurrences.TestOccurrence>>) {
        loadTests(mLoadMoreUrl!!, loadingListener, true)
    }

    /**
     * {@inheritDoc}
     */
    override fun canLoadMore(): Boolean {
        return mLoadMoreUrl != null
    }

    /**
     * {@inheritDoc}
     */
    override fun postChangeTabTitleEvent(size: Int) {
        eventBus.post(OnTextTabChangeEvent(size, BuildDetailsPresenter.TESTS_TAB))
    }
}
