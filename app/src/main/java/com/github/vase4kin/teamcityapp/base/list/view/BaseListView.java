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

package com.github.vase4kin.teamcityapp.base.list.view;

import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;

import tr.xip.errorview.ErrorView;

/**
 * Base list view interactions
 */
public interface BaseListView<T extends BaseDataModel> {

    /**
     * Default view holder type
     */
    int TYPE_DEFAULT = 0;

    /**
     * Load more view holder type
     */
    int TYPE_LOAD_MORE = 1;

    void initViews(@NonNull ViewListener listener);

    /**
     * Show progress loading wheel
     */
    void showProgressWheel();

    /**
     * Hide progress loading wheel
     */
    void hideProgressWheel();

    /**
     * @return Is progress loading or not
     */
    boolean isProgressWheelShown();

    /**
     * Enable swipe to refresh layout
     */
    void enableSwipeToRefresh();

    /**
     * Disable swipe to refresh layout
     */
    void disableSwipeToRefresh();

    /**
     * Hide swipe refresh layout animation
     */
    void hideRefreshAnimation();

    /**
     * Show swipe refresh layout animation
     */
    void showRefreshAnimation();

    /**
     * Show error view with error message
     */
    void showErrorView();

    /**
     * Hide error view
     */
    void hideErrorView();

    /**
     * Show empty view
     */
    void showEmpty();

    /**
     * Hide empty view
     */
    void hideEmpty();

    /**
     * Unbind views
     */
    void unbindViews();

    /**
     * Enable recycler view
     */
    void enableRecyclerView();

    /**
     * Disable recycler view
     */
    void disableRecyclerView();

    /**
     * Show model data
     *
     * @param dataModel - Model data
     */
    void showData(T dataModel);

    interface ViewListener extends ErrorView.RetryListener, SwipeRefreshLayout.OnRefreshListener {
    }
}
