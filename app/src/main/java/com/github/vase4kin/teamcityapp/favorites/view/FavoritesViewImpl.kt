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

package com.github.vase4kin.teamcityapp.favorites.view

import android.app.Activity
import android.view.View
import androidx.annotation.StringRes

import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.base.list.view.BaseListViewImpl
import com.github.vase4kin.teamcityapp.base.list.view.SimpleSectionedRecyclerViewAdapter
import com.github.vase4kin.teamcityapp.navigation.api.NavigationItem
import com.github.vase4kin.teamcityapp.navigation.data.NavigationDataModel
import com.github.vase4kin.teamcityapp.navigation.view.NavigationActivity
import com.github.vase4kin.teamcityapp.navigation.view.NavigationAdapter
import com.github.vase4kin.teamcityapp.navigation.view.OnNavigationItemClickListener

import java.util.ArrayList

class FavoritesViewImpl(
    view: View,
    activity: Activity,
    @StringRes emptyMessage: Int,
    adapter: SimpleSectionedRecyclerViewAdapter<NavigationAdapter>
) : BaseListViewImpl<NavigationDataModel, SimpleSectionedRecyclerViewAdapter<NavigationAdapter>>(
    view,
    activity,
    emptyMessage,
    adapter
), FavoritesView {

    private var listener: FavoritesView.ViewListener? = null

    /**
     * {@inheritDoc}
     */
    override fun setViewListener(listener: FavoritesView.ViewListener) {
        this.listener = listener
    }

    /**
     * {@inheritDoc}
     */
    override fun showData(dataModel: NavigationDataModel) {
        val baseAdapter = adapter.baseAdapter
        baseAdapter.dataModel = dataModel
        baseAdapter.setOnClickListener(object : OnNavigationItemClickListener {
            override fun onClick(navigationItem: NavigationItem) {
                listener?.onClick(navigationItem)
            }

            override fun onRateCancelButtonClick() {
            }

            override fun onRateNowButtonClick() {
            }
        })

        val sections = ArrayList<SimpleSectionedRecyclerViewAdapter.Section>()

        if (dataModel.itemCount != 0) {
            for (i in 0 until dataModel.itemCount) {
                val projectName = dataModel.getProjectName(i)
                if (sections.size != 0) {
                    val prevSection = sections[sections.size - 1]
                    if (prevSection.title != projectName) {
                        sections.add(SimpleSectionedRecyclerViewAdapter.Section(i, projectName))
                    }
                } else {
                    sections.add(SimpleSectionedRecyclerViewAdapter.Section(i, projectName))
                }
            }
            adapter.setListener { position ->
                val projectName = dataModel.getProjectName(position)
                val projectId = dataModel.getProjectId(position)
                NavigationActivity.start(projectName, projectId, activity)
            }
        }
        val userStates = arrayOfNulls<SimpleSectionedRecyclerViewAdapter.Section>(sections.size)
        adapter.setSections(sections.toArray(userStates))

        recyclerView.adapter = adapter
        recyclerView.adapter?.notifyDataSetChanged()
    }

    /**
     * {@inheritDoc}
     */
    override fun recyclerViewId(): Int {
        return R.id.favorites_recycler_view
    }

    /**
     * {@inheritDoc}
     */
    override fun emptyTitleId(): Int {
        return R.id.favorites_empty_title_view
    }
}
