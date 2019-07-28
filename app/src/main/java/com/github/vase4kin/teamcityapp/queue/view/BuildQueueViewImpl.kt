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

package com.github.vase4kin.teamcityapp.queue.view

import android.app.Activity
import android.view.View

import androidx.annotation.StringRes

import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.base.list.view.SimpleSectionedRecyclerViewAdapter
import com.github.vase4kin.teamcityapp.buildlist.view.BuildListAdapter
import com.github.vase4kin.teamcityapp.filter_bottom_sheet_dialog.Filter
import com.github.vase4kin.teamcityapp.filter_bottom_sheet_dialog.FilterProvider
import com.github.vase4kin.teamcityapp.runningbuilds.view.RunningBuildsListViewImpl

/**
 * View to manage build queue interactions
 */
class BuildQueueViewImpl(view: View,
                         activity: Activity,
                         @StringRes emptyMessage: Int,
                         adapter: SimpleSectionedRecyclerViewAdapter<BuildListAdapter>,
                         filterProvider: FilterProvider
) : RunningBuildsListViewImpl(view, activity, emptyMessage, adapter, filterProvider) {

    /**
     * {@inheritDoc}
     */
    override fun getTitle(): String = mActivity.getString(R.string.build_queue_drawer_item)

    /**
     * {@inheritDoc}
     */
    override fun getEmptyMessage(): Int = when (filterProvider.queuedBuildsFilter) {
        Filter.QUEUE_FAVORITES -> R.string.empty_list_message_favorite_build_queue
        else -> super.getEmptyMessage()
    }
}