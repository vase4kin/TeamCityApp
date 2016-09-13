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

package com.github.vase4kin.teamcityapp.agenttabs.view;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.agents.view.BaseAgentListFragment;
import com.github.vase4kin.teamcityapp.base.tabs.view.BaseTabsViewModelImpl;
import com.github.vase4kin.teamcityapp.base.tabs.view.FragmentAdapter;

public class AgentTabsViewModelImpl extends BaseTabsViewModelImpl {

    public static final int CONNECTED_TAB = 0;
    public static final int DISCONNECTED_TAB = 1;

    public AgentTabsViewModelImpl(View mView, AppCompatActivity mActivity) {
        super(mView, mActivity);
    }

    @Override
    public void addFragments(FragmentAdapter fragmentAdapter) {
        fragmentAdapter.add(R.string.tab_connected, BaseAgentListFragment.newInstance(false));
        fragmentAdapter.add(R.string.tab_disconnected, BaseAgentListFragment.newInstance(true));
    }

    @Override
    public void initViews() {
        super.initViews();
        AppCompatActivity activity = mActivity;
        if (activity.getSupportActionBar() != null) {
            ActionBar actionBar = activity.getSupportActionBar();
            actionBar.setTitle(R.string.agents_drawer_item);
        }
    }
}
