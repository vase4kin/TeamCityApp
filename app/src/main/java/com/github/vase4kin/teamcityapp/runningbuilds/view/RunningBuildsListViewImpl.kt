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

package com.github.vase4kin.teamcityapp.runningbuilds.view

import android.app.Activity
import android.view.View
import androidx.annotation.StringRes

import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.base.list.view.SimpleSectionedRecyclerViewAdapter
import com.github.vase4kin.teamcityapp.buildlist.data.BuildListDataModel
import com.github.vase4kin.teamcityapp.buildlist.data.OnBuildListPresenterListener
import com.github.vase4kin.teamcityapp.buildlist.view.BuildListActivity
import com.github.vase4kin.teamcityapp.buildlist.view.BuildListAdapter
import com.github.vase4kin.teamcityapp.buildlist.view.BuildListViewImpl
import com.github.vase4kin.teamcityapp.filter_bottom_sheet_dialog.filter.Filter
import com.github.vase4kin.teamcityapp.filter_bottom_sheet_dialog.filter.FilterProvider

import java.util.ArrayList

/**
 * impl of [RunningBuildListView]
 */
open class RunningBuildsListViewImpl(
    view: View,
    activity: Activity,
    @StringRes emptyMessage: Int,
    adapter: SimpleSectionedRecyclerViewAdapter<BuildListAdapter>,
    protected val filterProvider: FilterProvider
) : BuildListViewImpl(view, activity, emptyMessage, adapter), RunningBuildListView {

    /**
     * @return default tool bar title
     */
    protected open val title: String
        get() = activity.getString(R.string.running_builds_drawer_item)

    /**
     * {@inheritDoc}
     */
    override fun showData(dataModel: BuildListDataModel) {
        val baseAdapter = adapter.baseAdapter
        baseAdapter.dataModel = dataModel
        baseAdapter.setOnBuildListPresenterListener(listener ?: OnBuildListPresenterListener.EMPTY)

        val sections = ArrayList<SimpleSectionedRecyclerViewAdapter.Section>()

        if (dataModel.itemCount != 0) {
            for (i in 0 until dataModel.itemCount) {
                val buildTypeTitle = if (dataModel.hasBuildTypeInfo(i))
                    dataModel.getBuildTypeFullName(i)
                else
                    dataModel.getBuildTypeId(i)
                if (sections.size != 0) {
                    val prevSection = sections[sections.size - 1]
                    if (prevSection.title != buildTypeTitle) {
                        sections.add(SimpleSectionedRecyclerViewAdapter.Section(i, buildTypeTitle))
                    }
                } else {
                    sections.add(SimpleSectionedRecyclerViewAdapter.Section(i, buildTypeTitle))
                }
            }
            adapter.setListener { position ->
                val buildTypeName = dataModel.getBuildTypeName(position)
                val buildTypeId = dataModel.getBuildTypeId(position)
                BuildListActivity.start(buildTypeName, buildTypeId, null, activity)
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
    override fun showRunBuildFloatActionButton() {
        // Do not show running build float action button here
    }

    /**
     * {@inheritDoc}
     */
    override fun hideRunBuildFloatActionButton() {
        // Do not show running build float action button here
    }

    /**
     * {@inheritDoc}
     */
    override val emptyMessage: Int
        get() = if (filterProvider.runningBuildsFilter === Filter.RUNNING_FAVORITES) {
            R.string.empty_list_message_favorite_running_builds
        } else {
            R.string.empty_list_message_running_builds
        }

    /**
     * {@inheritDoc}
     */
    override fun emptyTitleId(): Int {
        return R.id.running_empty_title_view
    }

    /**
     * {@inheritDoc}
     */
    override fun recyclerViewId(): Int {
        return R.id.running_builds_recycler_view
    }
}
