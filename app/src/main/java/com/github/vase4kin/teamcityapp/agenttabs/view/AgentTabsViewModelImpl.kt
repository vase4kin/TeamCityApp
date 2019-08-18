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

package com.github.vase4kin.teamcityapp.agenttabs.view

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.agents.view.AgentListFragment
import com.github.vase4kin.teamcityapp.base.tabs.view.BaseTabsViewModelImpl
import com.github.vase4kin.teamcityapp.base.tabs.view.FragmentAdapter

class AgentTabsViewModelImpl(view: View, activity: AppCompatActivity) : BaseTabsViewModelImpl(view, activity) {

    override fun addFragments(fragmentAdapter: FragmentAdapter) {
        fragmentAdapter.add(R.string.tab_connected, AgentListFragment.newInstance(false))
        fragmentAdapter.add(R.string.tab_disconnected, AgentListFragment.newInstance(true))
    }

    override fun initViews() {
        super.initViews()
        val actionBar = activity.supportActionBar ?: return
        actionBar.setTitle(R.string.agents_drawer_item)
    }

    companion object {
        const val CONNECTED_TAB = 0
        const val DISCONNECTED_TAB = 1
    }
}
