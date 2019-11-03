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

package teamcityapp.features.test_details.feature.presenter

import teamcityapp.features.test_details.feature.data.TestDetailsDataManager
import teamcityapp.features.test_details.feature.tracker.TestDetailsTracker
import teamcityapp.features.test_details.feature.view.TestDetailsView
import tr.xip.errorview.ErrorView
import javax.inject.Inject

/**
 * Impl of [TestDetailsPresenter]
 */
class TestDetailsPresenterImpl @Inject constructor(
    private val view: TestDetailsView,
    private val dataManager: TestDetailsDataManager,
    private val tracker: TestDetailsTracker,
    private val url: String
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
        if (url.isEmpty()) {
            return
        }
        dataManager.loadData(
            onSuccess = {
                view.hideProgress()
                val testDetails = it.details
                if (testDetails.isEmpty()) {
                    view.showEmptyData()
                } else {
                    view.showTestDetails(testDetails)
                }
            },
            onError = {
                view.hideProgress()
                view.showRetryView(it)
            },
            url = url
        )
    }
}
