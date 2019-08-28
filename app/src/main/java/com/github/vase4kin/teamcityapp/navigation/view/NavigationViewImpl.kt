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

package com.github.vase4kin.teamcityapp.navigation.view

import android.app.Activity
import android.view.View
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.base.list.view.BaseListViewImpl
import com.github.vase4kin.teamcityapp.navigation.data.NavigationDataModel

/**
 * Impl of [NavigationView]
 */
class NavigationViewImpl(
    view: View,
    activity: Activity,
    @StringRes emptyMessage: Int,
    adapter: NavigationAdapter
) : BaseListViewImpl<NavigationDataModel, NavigationAdapter>(view, activity, emptyMessage, adapter),
    NavigationView {

    private var onNavigationItemClickListener: OnNavigationItemClickListener? = null

    /**
     * {@inheritDoc}
     */
    override fun setNavigationAdapterClickListener(clickListener: OnNavigationItemClickListener) {
        onNavigationItemClickListener = clickListener
    }

    /**
     * {@inheritDoc}
     */
    override fun setTitle(title: String) {
        val actionBar = (activity as AppCompatActivity).supportActionBar
        if (actionBar != null) {
            actionBar.title = title
        }
    }

    override fun hideTheRateApp() {
        adapter.removeRateTheApp()
    }

    /**
     * {@inheritDoc}
     */
    override fun showData(dataModel: NavigationDataModel) {
        adapter.dataModel = dataModel
        adapter.setOnClickListener(
            onNavigationItemClickListener ?: OnNavigationItemClickListener.EMPTY
        )
        recyclerView.adapter = adapter
        recyclerView.adapter?.notifyDataSetChanged()
    }

    /**
     * {@inheritDoc}
     */
    override fun recyclerViewId(): Int {
        return R.id.navigation_recycler_view
    }
}
