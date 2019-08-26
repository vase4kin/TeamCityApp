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

package com.github.vase4kin.teamcityapp.base.list.view

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

import tr.xip.errorview.ErrorView

/**
 * Base list view interactions
 */
interface BaseListView<T : BaseDataModel> {

    /**
     * @return the visibility flag of skeleton view
     */
    val isSkeletonViewVisible: Boolean

    fun initViews(listener: ViewListener)

    /**
     * Enable swipe to refresh layout
     */
    fun enableSwipeToRefresh()

    /**
     * Disable swipe to refresh layout
     */
    fun disableSwipeToRefresh()

    /**
     * Hide swipe refresh layout animation
     */
    fun hideRefreshAnimation()

    /**
     * Show swipe refresh layout animation
     */
    fun showRefreshAnimation()

    /**
     * Show error view with error message
     */
    fun showErrorView()

    /**
     * Hide error view
     */
    fun hideErrorView()

    /**
     * Show empty view
     */
    fun showEmpty()

    /**
     * Hide empty view
     */
    fun hideEmpty()

    /**
     * Unbind views
     */
    fun unbindViews()

    /**
     * Show model data
     *
     * @param dataModel - Model data
     */
    fun showData(dataModel: T)

    /**
     * Show skeleton view
     */
    fun showSkeletonView()

    /**
     * Hide skeleton view
     */
    fun hideSkeletonView()

    /**
     * Replace skeletonView content
     */
    fun replaceSkeletonViewContent()

    interface ViewListener : ErrorView.RetryListener, SwipeRefreshLayout.OnRefreshListener

    companion object {

        /**
         * Default view holder type
         */
        const val TYPE_DEFAULT = 0

        /**
         * Load more view holder type
         */
        const val TYPE_LOAD_MORE = 1
    }
}
