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

package com.github.vase4kin.teamcityapp.tests.view;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.github.vase4kin.teamcityapp.base.list.view.BaseListView;
import com.github.vase4kin.teamcityapp.buildlist.view.OnLoadMoreListener;
import com.github.vase4kin.teamcityapp.tests.data.TestsDataModel;

/**
 * View to handle {@link TestOccurrencesFragment} interactions
 */
public interface TestsView extends BaseListView<TestsDataModel> {

    /**
     * Invalidate options menu
     */
    void invalidateOptionsMenu();

    /**
     * {@inheritDoc}
     * <p>
     * See {@link android.support.v4.app.Fragment#onCreateOptionsMenu(Menu, MenuInflater)} )}
     */
    void onCreateOptionsMenu(Menu menu, MenuInflater inflater);

    /**
     * {@inheritDoc}
     *
     * See {@link android.support.v4.app.Fragment#onPrepareOptionsMenu(Menu)} )}
     */
    void onPrepareOptionsMenu(Menu menu);

    /**
     * {@inheritDoc}
     *
     * See {@link android.support.v4.app.Fragment#onOptionsItemSelected(MenuItem)}
     */
    boolean onOptionsItemSelected(MenuItem item);

    /**
     * Set listener to receive callbacks
     *
     * @param listener - Listenet to receive callnbacks
     */
    void setListener(OnTestsPresenterListener listener);

    /**
     * Set on load more listener
     *
     * @param mOnLoadMoreListener - Listener to receive on load more callbacks
     */
    void setOnLoadMoreListener(OnLoadMoreListener mOnLoadMoreListener);

    /**
     * Add load more item
     */
    void addLoadMoreItem();

    /**
     * Remove load more item
     */
    void removeLoadMoreItem();

    /**
     * Add more builds
     *
     * @param dataModel - Data model to add
     */
    void addMoreBuilds(TestsDataModel dataModel);

    /**
     * Show retry load more snack bar if server error happens
     */
    void showRetryLoadMoreSnackBar();
}
