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

package teamcityapp.features.test_details.view

import android.graphics.Color
import android.os.Build
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import teamcityapp.features.test_details.R
import teamcityapp.libraries.utils.StatusBarUtils
import tr.xip.errorview.ErrorView
import javax.inject.Inject

/**
 * Impl of [TestDetailsView]
 */
class TestDetailsViewImpl @Inject constructor(
    private val activity: TestDetailsActivity,
    private val statusBarUtils: StatusBarUtils
) : TestDetailsView, OnActionModeListener {

    private lateinit var testDetailsTextView: TextView
    private lateinit var errorView: ErrorView
    private lateinit var progressWheel: ProgressBar
    private lateinit var toolbar: Toolbar
    private lateinit var empty: TextView

    /**
     * {@inheritDoc}
     */
    override fun initViews(retryListener: ErrorView.RetryListener) {
        testDetailsTextView = activity.findViewById(R.id.test_occurrence_details)
        errorView = activity.findViewById(R.id.error_view)
        progressWheel = activity.findViewById(R.id.progress_wheel)
        toolbar = activity.findViewById(R.id.toolbar)
        empty = activity.findViewById(R.id.empty)
        errorView.setRetryListener(retryListener)
        errorView.setImageTint(Color.LTGRAY)
        initToolBar()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            testDetailsTextView.customSelectionActionModeCallback =
                CustomSelectionActionModeCallBackImpl(this)
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun showProgress() {
        progressWheel.visibility = View.VISIBLE
        testDetailsTextView.visibility = View.GONE
        errorView.visibility = View.GONE
    }

    /**
     * Init toolbar
     */
    private fun initToolBar() {
        toolbar.setNavigationIcon(R.drawable.ic_close_white_24dp)
        toolbar.setNavigationOnClickListener {
            finish()
        }
        statusBarUtils.changeStatusBarColor(activity, R.color.tool_bar)
    }

    /**
     * {@inheritDoc}
     */
    override fun hideProgress() {
        progressWheel.visibility = View.GONE
    }

    /**
     * {@inheritDoc}
     */
    override fun showTestDetails(testDetails: String) {
        testDetailsTextView.visibility = View.VISIBLE
        testDetailsTextView.text = testDetails
    }

    /**
     * {@inheritDoc}
     */
    override fun showEmptyData() {
        empty.visibility = View.VISIBLE
    }

    /**
     * {@inheritDoc}
     */
    override fun showRetryView(errorMessage: String) {
        errorView.visibility = View.VISIBLE
        errorView.setSubtitle(errorMessage)
    }

    /**
     * {@inheritDoc}
     */
    override fun finish() {
        activity.finish()
        activity.overridePendingTransition(R.anim.hold, R.anim.slide_out_bottom)
    }

    /**
     * {@inheritDoc}
     */
    override fun onCreate() {
        toolbar.visibility = View.INVISIBLE
    }

    /**
     * {@inheritDoc}
     */
    override fun onDestroy() {
        toolbar.visibility = View.VISIBLE
    }
}
