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

package com.github.vase4kin.teamcityapp.agents.view

import android.app.Activity
import android.view.View
import androidx.annotation.StringRes
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.agents.data.AgentDataModel
import com.github.vase4kin.teamcityapp.base.list.view.BaseListViewImpl

/**
 * View managing for [AgentListFragment]
 */
class AgentViewImpl(
    private val includeDisconnected: Boolean,
    view: View,
    activity: Activity,
    @StringRes emptyMessage: Int,
    adapter: AgentsAdapter
) : BaseListViewImpl<AgentDataModel, AgentsAdapter>(view, activity, emptyMessage, adapter) {

    /**
     * {@inheritDoc}
     */
    override fun showData(dataModel: AgentDataModel) {
        adapter.dataModel = dataModel
        recyclerView.adapter = adapter
        recyclerView.adapter?.notifyDataSetChanged()
    }

    /**
     * {@inheritDoc}
     */
    override fun replaceSkeletonViewContent() {
        replaceSkeletonViewContent(R.layout.layout_skeleton_agent_list)
    }

    /**
     * {@inheritDoc}
     */
    override fun recyclerViewId(): Int {
        return if (includeDisconnected)
            R.id.disconnected_agents_recycler_view
        else
            R.id.connected_agents_recycler_view
    }
}
