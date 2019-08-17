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

package com.github.vase4kin.teamcityapp.tests.presenter

import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.annotation.VisibleForTesting
import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener
import com.github.vase4kin.teamcityapp.base.list.presenter.BaseListPresenterImpl
import com.github.vase4kin.teamcityapp.base.tracker.ViewTracker
import com.github.vase4kin.teamcityapp.tests.api.TestOccurrences
import com.github.vase4kin.teamcityapp.tests.data.TestsDataManager
import com.github.vase4kin.teamcityapp.tests.data.TestsDataModel
import com.github.vase4kin.teamcityapp.tests.data.TestsDataModelImpl
import com.github.vase4kin.teamcityapp.tests.extractor.TestsValueExtractor
import com.github.vase4kin.teamcityapp.tests.router.TestsRouter
import com.github.vase4kin.teamcityapp.tests.view.OnTestsPresenterListener
import com.github.vase4kin.teamcityapp.tests.view.TestsView
import com.mugen.MugenCallbacks
import javax.inject.Inject

/**
 * Impl of [TestsPresenter]
 */
class TestsPresenterImpl @Inject constructor(
        view: TestsView,
        dataManager: TestsDataManager,
        tracker: ViewTracker,
        valueExtractor: TestsValueExtractor,
        private val router: TestsRouter
) : BaseListPresenterImpl<TestsDataModel, TestOccurrences.TestOccurrence, TestsView, TestsDataManager, ViewTracker, TestsValueExtractor>(view, dataManager, tracker, valueExtractor), TestsPresenter, OnTestsPresenterListener {

    @VisibleForTesting
    var isLoadMoreLoading = false

    /**
     * {@inheritDoc}
     */
    public override fun loadData(loadingListener: OnLoadingListener<List<TestOccurrences.TestOccurrence>>, update: Boolean) {
        dataManager.loadFailedTests(valueExtractor.url, loadingListener, update)
    }

    /**
     * {@inheritDoc}
     */
    public override fun createModel(data: List<TestOccurrences.TestOccurrence>): TestsDataModel {
        return TestsDataModelImpl(data.toMutableList())
    }

    /**
     * {@inheritDoc}
     */
    public override fun initViews() {
        super.initViews()
        view.setListener(this)
        view.setOnLoadMoreListener(object : MugenCallbacks {
            override fun onLoadMore() {
                isLoadMoreLoading = true
                view.addLoadMore()
                dataManager.loadMore(object : OnLoadingListener<List<TestOccurrences.TestOccurrence>> {
                    override fun onSuccess(data: List<TestOccurrences.TestOccurrence>) {
                        view.removeLoadMore()
                        view.addMoreBuilds(TestsDataModelImpl(data.toMutableList()))
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
    override fun onViewsCreated() {
        super.onViewsCreated()
        dataManager.loadTestDetails(valueExtractor.url, object : OnLoadingListener<Int> {
            override fun onSuccess(data: Int) {
                dataManager.postChangeTabTitleEvent(data)
            }

            override fun onFail(errorMessage: String) {}
        })
        view.invalidateOptionsMenu()
    }

    /**
     * {@inheritDoc}
     */
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        view.onCreateOptionsMenu(menu, inflater)
    }

    /**
     * {@inheritDoc}
     */
    override fun onPrepareOptionsMenu(menu: Menu) {
        view.onPrepareOptionsMenu(menu)
    }

    /**
     * {@inheritDoc}
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        view.showRefreshAnimation()
        view.hideErrorView()
        view.hideEmpty()
        view.showData(TestsDataModelImpl(mutableListOf()))
        return view.onOptionsItemSelected(item)
    }

    /**
     * {@inheritDoc}
     */
    override fun loadFailedTests() {
        dataManager.loadFailedTests(valueExtractor.url)
    }

    /**
     * {@inheritDoc}
     */
    override fun loadSuccessTests() {
        dataManager.loadPassedTests(valueExtractor.url)
    }

    /**
     * {@inheritDoc}
     */
    override fun loadIgnoredTests() {
        dataManager.loadIgnoredTests(valueExtractor.url)
    }

    /**
     * {@inheritDoc}
     */
    override fun onFailedTestClick(url: String) {
        router.openFailedTest(url)
    }
}
