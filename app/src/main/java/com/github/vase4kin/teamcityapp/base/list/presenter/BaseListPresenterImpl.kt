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

package com.github.vase4kin.teamcityapp.base.list.presenter

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener
import com.github.vase4kin.teamcityapp.base.api.Jsonable
import com.github.vase4kin.teamcityapp.base.list.data.BaseListRxDataManager
import com.github.vase4kin.teamcityapp.base.list.extractor.BaseValueExtractor
import com.github.vase4kin.teamcityapp.base.list.view.BaseDataModel
import com.github.vase4kin.teamcityapp.base.list.view.BaseListView
import com.github.vase4kin.teamcityapp.base.tracker.ViewTracker

/**
 * Base impl of [BaseListPresenter]
 */
abstract class BaseListPresenterImpl<T : BaseDataModel, S : Jsonable, VM : BaseListView<T>, DM : BaseListRxDataManager<*, *>, VT : ViewTracker, BE : BaseValueExtractor>(
    protected var view: VM,
    protected var dataManager: DM,
    protected var tracker: VT,
    protected var valueExtractor: BE
) : BaseListPresenter {

    /**
     * On loading listener
     */
    protected var loadingListener: OnLoadingListener<List<S>> =
        object : OnLoadingListener<List<S>> {
            override fun onSuccess(data: List<S>) {
                onSuccessCallBack(data)
            }

            override fun onFail(errorMessage: String) {
                onFailCallBack(errorMessage)
            }
        }

    /**
     * {@inheritDoc}
     */
    override fun onViewsCreated() {
        initViews()
        view.showSkeletonView()
        view.disableSwipeToRefresh()
        loadDataOnViewsCreated()
    }

    protected open fun loadDataOnViewsCreated() {
        loadData(loadingListener, false)
    }

    /**
     * Use this one to load data with DataManager
     *
     * @param loadingListener - Receive server callbacks
     * @param update          - Force cache update (if it's supported)
     */
    protected abstract fun loadData(loadingListener: OnLoadingListener<List<S>>, update: Boolean)

    /**
     * Init views, register listeners
     */
    protected open fun initViews() {
        val listener = object : BaseListView.ViewListener {
            override fun onRefresh() {
                onSwipeToRefresh()
            }

            override fun onRetry() {
                view.showRefreshAnimation()
                onSwipeToRefresh()
            }
        }
        view.initViews(listener)
    }

    /**
     * On swipe to refresh
     */
    protected open fun onSwipeToRefresh() {
        view.hideErrorView()
        view.hideEmpty()
        loadData(loadingListener, true)
    }

    /**
     * {@inheritDoc}
     */
    override fun onViewsDestroyed() {
        view.unbindViews()
        dataManager.unsubscribe()
    }

    /**
     * {@inheritDoc}
     */
    override fun onResume() {
        tracker.trackView()
    }

    /**
     * {@inheritDoc}
     */
    protected open fun onSuccessCallBack(data: List<S>) {
        view.hideErrorView()
        if (data.isEmpty()) {
            view.showEmpty()
        } else {
            view.hideEmpty()
        }
        val model = createModel(data)
        view.showData(model)
        onCompleteLoading()
    }

    /**
     * Create data model for list
     *
     * @return Data model
     */
    protected abstract fun createModel(data: List<S>): T

    protected open fun onFailCallBack(errorMessage: String) {
        val model = createModel(emptyList())
        view.showData(model)
        view.hideEmpty()
        view.showErrorView()
        onCompleteLoading()
    }

    /**
     * Base views interaction on server request completion
     */
    private fun onCompleteLoading() {
        if (view.isSkeletonViewVisible) {
            view.hideSkeletonView()
        }
        view.enableSwipeToRefresh()
        view.hideRefreshAnimation()
    }
}
