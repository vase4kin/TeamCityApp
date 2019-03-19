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

package com.github.vase4kin.teamcityapp.agents.view;

import android.app.Activity;
import android.view.View;

import androidx.annotation.StringRes;

import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.agents.data.AgentDataModel;
import com.github.vase4kin.teamcityapp.agents.extractor.AgentsValueExtractor;
import com.github.vase4kin.teamcityapp.base.list.view.BaseListViewImpl;

/**
 * View managing for {@link BaseAgentListFragment}
 */
public class AgentViewImpl extends BaseListViewImpl<AgentDataModel, AgentsAdapter> {

    private AgentsValueExtractor mValueExtractor;

    public AgentViewImpl(AgentsValueExtractor valueExtractor,
                         View view,
                         Activity activity,
                         @StringRes int emptyMessage,
                         AgentsAdapter adapter) {
        super(view, activity, emptyMessage, adapter);
        this.mValueExtractor = valueExtractor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showData(AgentDataModel dataModel) {
        mAdapter.setDataModel(dataModel);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.getAdapter().notifyDataSetChanged();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void replaceSkeletonViewContent() {
        replaceSkeletonViewContent(R.layout.layout_skeleton_agent_list);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int recyclerViewId() {
        return mValueExtractor.includeDisconnected()
                ? R.id.disconnected_agents_recycler_view
                : R.id.connected_agents_recycler_view;
    }
}
