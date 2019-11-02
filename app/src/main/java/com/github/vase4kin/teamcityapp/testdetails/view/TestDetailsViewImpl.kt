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

package com.github.vase4kin.teamcityapp.testdetails.view

import android.graphics.Color
import android.os.Build
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.Unbinder
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.utils.StatusBarUtils
import tr.xip.errorview.ErrorView
import javax.inject.Inject

/**
 * Impl of [TestDetailsView]
 */
class TestDetailsViewImpl @Inject constructor(
    private val activity: TestDetailsActivity,
    private val statusBarUtils: StatusBarUtils
) :
    TestDetailsView, OnActionModeListener {

    @BindView(R.id.test_occurrence_details)
    lateinit var testDetailsTextView: TextView
    @BindView(R.id.error_view)
    lateinit var errorView: ErrorView
    @BindView(R.id.progress_wheel)
    lateinit var progressWheel: ProgressBar
    @BindView(R.id.toolbar)
    lateinit var toolbar: Toolbar
    @BindView(R.id.empty)
    lateinit var empty: TextView

    lateinit var unbinder: Unbinder

    /**
     * {@inheritDoc}
     */
    override fun initViews(retryListener: ErrorView.RetryListener) {
        unbinder = ButterKnife.bind(this, activity)
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
        toolbar.setNavigationIcon(R.drawable.ic_close_black_24dp)
        toolbar.setNavigationOnClickListener {
            finish()
        }
        statusBarUtils.changeStatusBarColor(activity, R.color.failed_tool_bar_color)
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

    override fun unBindViews() {
        unbinder.unbind()
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
