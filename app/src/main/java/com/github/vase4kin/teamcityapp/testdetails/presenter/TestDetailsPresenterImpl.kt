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

package com.github.vase4kin.teamcityapp.testdetails.presenter

import android.text.TextUtils
import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener
import com.github.vase4kin.teamcityapp.testdetails.data.TestDetailsDataManager
import com.github.vase4kin.teamcityapp.testdetails.extractor.TestDetailsValueExtractor
import com.github.vase4kin.teamcityapp.testdetails.tracker.TestDetailsTracker
import com.github.vase4kin.teamcityapp.testdetails.view.TestDetailsView
import com.github.vase4kin.teamcityapp.tests.api.TestOccurrences
import tr.xip.errorview.ErrorView
import javax.inject.Inject

/**
 * Impl of [com.github.vase4kin.teamcityapp.tests.presenter.TestsPresenter]
 */
class TestDetailsPresenterImpl @Inject constructor(
        private val view: TestDetailsView,
        private val dataManager: TestDetailsDataManager,
        private val tracker: TestDetailsTracker,
        private val valueExtractor: TestDetailsValueExtractor
) : TestDetailsPresenter, ErrorView.RetryListener {

    /**
     * {@inheritDoc}
     */
    override fun onCreate() {
        view.initViews(this)
        view.showProgress()
        loadData()
    }

    /**
     * {@inheritDoc}
     */
    override fun onDestroy() {
        view.unBindViews()
        dataManager.unsubscribe()
    }

    /**
     * {@inheritDoc}
     */
    override fun onBackPressed() {
        view.finish()
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
    override fun onRetry() {
        loadData()
    }

    /**
     * Load test details data
     */
    private fun loadData() {
        dataManager.loadData(object : OnLoadingListener<TestOccurrences.TestOccurrence> {
            override fun onSuccess(data: TestOccurrences.TestOccurrence) {
                view.hideProgress()
                val testDetails = data.details
                if (TextUtils.isEmpty(testDetails)) {
                    view.showEmptyData()
                } else {
                    view.showTestDetails(testDetails)
                }
            }

            override fun onFail(errorMessage: String) {
                view.hideProgress()
                view.showRetryView(errorMessage)
            }
        }, valueExtractor.testUrl)
    }
}
