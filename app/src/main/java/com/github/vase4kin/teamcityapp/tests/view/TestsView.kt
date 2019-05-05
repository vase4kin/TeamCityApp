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

package com.github.vase4kin.teamcityapp.tests.view

import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem

import androidx.fragment.app.Fragment

import com.github.vase4kin.teamcityapp.base.list.adapter.ViewLoadMore
import com.github.vase4kin.teamcityapp.base.list.view.BaseListView
import com.github.vase4kin.teamcityapp.tests.data.TestsDataModel
import com.mugen.MugenCallbacks

/**
 * View to handle [TestOccurrencesFragment] interactions
 */
interface TestsView : BaseListView<TestsDataModel>, ViewLoadMore<TestsDataModel> {

    /**
     * Invalidate options menu
     */
    fun invalidateOptionsMenu()

    /**
     * {@inheritDoc}
     *
     *
     * See [Fragment.onCreateOptionsMenu] )}
     */
    fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater)

    /**
     * {@inheritDoc}
     *
     * See [Fragment.onPrepareOptionsMenu] )}
     */
    fun onPrepareOptionsMenu(menu: Menu)

    /**
     * {@inheritDoc}
     *
     * See [Fragment.onOptionsItemSelected]
     */
    fun onOptionsItemSelected(item: MenuItem): Boolean

    /**
     * Set listener to receive callbacks
     *
     * @param listener - Listenet to receive callnbacks
     */
    fun setListener(listener: OnTestsPresenterListener)

    /**
     * Set on load more listener
     *
     * @param loadMoreCallbacks - Listener to receive on load more callbacks
     */
    fun setOnLoadMoreListener(loadMoreCallbacks: MugenCallbacks)

    /**
     * Show retry load more snack bar if server error happens
     */
    fun showRetryLoadMoreSnackBar()
}
