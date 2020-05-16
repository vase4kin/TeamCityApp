/*
 * Copyright 2020 Andrey Tolpeev
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

package com.github.vase4kin.teamcityapp.base.list.view

import android.app.Activity
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.Unbinder
import com.github.vase4kin.teamcityapp.R
import io.supercharge.shimmerlayout.ShimmerLayout
import tr.xip.errorview.ErrorView

/**
 * Base list view impl
 */
abstract class BaseListViewImpl<T : BaseDataModel, RA : RecyclerView.Adapter<*>>(
    protected var view: View,
    protected var activity: Activity,
    @StringRes protected open val emptyMessage: Int,
    protected var adapter: RA
) : BaseListView<T> {

    @BindView(R.id.my_recycler_view)
    lateinit var recyclerView: RecyclerView
    @BindView(R.id.swiperefresh)
    lateinit var swipeRefreshLayout: SwipeRefreshLayout
    @BindView(R.id.error_view)
    lateinit var errorView: ErrorView
    @BindView(android.R.id.empty)
    lateinit var mEmpty: LinearLayout
    @BindView(R.id.empty_title)
    lateinit var emptyTitle: TextView
    @BindView(R.id.skeleton_view)
    lateinit var skeletonView: ViewGroup

    lateinit var unbinder: Unbinder

    /**
     * {@inheritDoc}
     */
    override val isSkeletonViewVisible: Boolean
        get() = skeletonView.visibility == View.VISIBLE

    /**
     * {@inheritDoc}
     */
    override fun initViews(listener: BaseListView.ViewListener) {
        unbinder = ButterKnife.bind(this, view)
        // <!----Setting id for testing purpose----->!
        recyclerView.id = recyclerViewId()
        emptyTitle.id = emptyTitleId()
        // <!--------------------------------------->!
        errorView.setImageTint(Color.LTGRAY)
        errorView.setRetryListener(listener)
        swipeRefreshLayout.setOnRefreshListener(listener)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        // FIX for empty
        mEmpty.visibility = View.GONE
    }

    /**
     * {@inheritDoc}
     */
    override fun enableSwipeToRefresh() {
        swipeRefreshLayout.isEnabled = true
    }

    /**
     * {@inheritDoc}
     */
    override fun disableSwipeToRefresh() {
        swipeRefreshLayout.isEnabled = false
    }

    /**
     * {@inheritDoc}
     */
    override fun hideRefreshAnimation() {
        swipeRefreshLayout.isRefreshing = false
    }

    override fun showRefreshAnimation() {
        swipeRefreshLayout.isRefreshing = true
    }

    /**
     * {@inheritDoc}
     */
    override fun showErrorView() {
        errorView.visibility = View.VISIBLE
        errorView.setSubtitle(R.string.error_view_error_text)
    }

    /**
     * {@inheritDoc}
     */
    override fun hideErrorView() {
        errorView.visibility = View.GONE
    }

    /**
     * {@inheritDoc}
     */
    override fun showEmpty() {
        emptyTitle.setText(emptyMessage)
        mEmpty.visibility = View.VISIBLE
    }

    /**
     * {@inheritDoc}
     */
    override fun hideEmpty() {
        mEmpty.visibility = View.GONE
    }

    /**
     * {@inheritDoc}
     */
    override fun unbindViews() {
        unbinder.unbind()
    }

    /**
     * {@inheritDoc}
     */
    override fun showSkeletonView() {
        skeletonView.visibility = View.VISIBLE
        (skeletonView.getChildAt(0) as ShimmerLayout).startShimmerAnimation()
    }

    /**
     * {@inheritDoc}
     */
    override fun hideSkeletonView() {
        skeletonView.visibility = View.GONE
        (skeletonView.getChildAt(0) as ShimmerLayout).stopShimmerAnimation()
    }

    /**
     * {@inheritDoc}
     */
    override fun replaceSkeletonViewContent() {
    }

    /**
     * {@inheritDoc}
     */
    protected fun replaceSkeletonViewContent(@LayoutRes layout: Int) {
        skeletonView.removeAllViewsInLayout()
        LayoutInflater.from(activity).inflate(layout, skeletonView)
    }

    /**
     * Provide recycler view id for each view impl to easy determine them by Espresso
     */
    @IdRes
    protected abstract fun recyclerViewId(): Int

    /**
     * Provide empty title view id for each view impl to easy determine them by Espresso
     */
    @IdRes
    protected open fun emptyTitleId(): Int {
        return R.id.empty_title
    }
}
