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

package com.github.vase4kin.teamcityapp.changes.presenter

import androidx.annotation.VisibleForTesting
import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener
import com.github.vase4kin.teamcityapp.base.list.presenter.BaseListPresenterImpl
import com.github.vase4kin.teamcityapp.base.tracker.ViewTracker
import com.github.vase4kin.teamcityapp.changes.api.Changes
import com.github.vase4kin.teamcityapp.changes.data.ChangesDataManager
import com.github.vase4kin.teamcityapp.changes.data.ChangesDataModel
import com.github.vase4kin.teamcityapp.changes.data.ChangesDataModelImpl
import com.github.vase4kin.teamcityapp.changes.extractor.ChangesValueExtractor
import com.github.vase4kin.teamcityapp.changes.view.ChangesView
import com.mugen.MugenCallbacks
import javax.inject.Inject

/**
 * Presenter manages logic of [com.github.vase4kin.teamcityapp.changes.view.ChangesFragment]
 */
class ChangesPresenterImpl @Inject constructor(
    view: ChangesView,
    dataManager: ChangesDataManager,
    tracker: ViewTracker,
    valueExtractor: ChangesValueExtractor
) : BaseListPresenterImpl<ChangesDataModel, Changes.Change, ChangesView, ChangesDataManager, ViewTracker, ChangesValueExtractor>(
    view,
    dataManager,
    tracker,
    valueExtractor
) {

    @VisibleForTesting
    var isLoadMoreLoading = false

    /**
     * {@inheritDoc}
     */
    public override fun initViews() {
        super.initViews()
        view.setLoadMoreListener(object : MugenCallbacks {
            override fun onLoadMore() {
                isLoadMoreLoading = true
                view.addLoadMore()
                dataManager.loadMore(object : OnLoadingListener<List<Changes.Change>> {
                    override fun onSuccess(data: List<Changes.Change>) {
                        view.removeLoadMore()
                        view.addMoreBuilds(ChangesDataModelImpl(data.toMutableList()))
                        isLoadMoreLoading = false
                    }

                    override fun onFail(errorMessage: String) {
                        view.removeLoadMore()
                        view.showRetryLoadMoreSnackBar()
                        isLoadMoreLoading = false
                    }
                })
            }

            override fun isLoading(): Boolean {
                return isLoadMoreLoading
            }

            override fun hasLoadedAllItems(): Boolean {
                return !dataManager.canLoadMore()
            }
        })
        view.replaceSkeletonViewContent()
    }

    /**
     * {@inheritDoc}
     */
    public override fun loadData(loadingListener: OnLoadingListener<List<Changes.Change>>, update: Boolean) {
        dataManager.loadLimited(valueExtractor.url, loadingListener, update)
    }

    /**
     * {@inheritDoc}
     */
    public override fun createModel(data: MutableList<Changes.Change>): ChangesDataModel {
        return ChangesDataModelImpl(data)
    }

    /**
     * {@inheritDoc}
     */
    override fun onViewsCreated() {
        super.onViewsCreated()
        dataManager.loadTabTitle(valueExtractor.url, object : OnLoadingListener<Int> {
            override fun onSuccess(data: Int) {
                dataManager.postChangeTabTitleEvent(data)
            }

            override fun onFail(errorMessage: String) {}
        })
    }
}
