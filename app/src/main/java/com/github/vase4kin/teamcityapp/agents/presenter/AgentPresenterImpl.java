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

package com.github.vase4kin.teamcityapp.agents.presenter;

import androidx.annotation.NonNull;

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener;
import com.github.vase4kin.teamcityapp.agents.api.Agent;
import com.github.vase4kin.teamcityapp.agents.data.AgentDataModel;
import com.github.vase4kin.teamcityapp.agents.data.AgentDataModelImpl;
import com.github.vase4kin.teamcityapp.agents.data.AgentsDataManager;
import com.github.vase4kin.teamcityapp.agents.extractor.AgentsValueExtractor;
import com.github.vase4kin.teamcityapp.agenttabs.view.AgentTabsViewModelImpl;
import com.github.vase4kin.teamcityapp.base.list.presenter.BaseListPresenterImpl;
import com.github.vase4kin.teamcityapp.base.list.view.BaseListView;
import com.github.vase4kin.teamcityapp.base.tracker.ViewTracker;

import java.util.List;

import javax.inject.Inject;

/**
 * Handles logic of {@link com.github.vase4kin.teamcityapp.agents.view.BaseAgentListFragment}
 */
public class AgentPresenterImpl extends BaseListPresenterImpl<
        AgentDataModel,
        Agent,
        BaseListView,
        AgentsDataManager,
        ViewTracker,
        AgentsValueExtractor> {

    @Inject
    AgentPresenterImpl(@NonNull BaseListView view,
                       @NonNull AgentsDataManager dataManager,
                       @NonNull ViewTracker tracker,
                       @NonNull AgentsValueExtractor valueExtractor) {
        super(view, dataManager, tracker, valueExtractor);
    }

    @Override
    protected void initViews() {
        super.initViews();
        mView.replaceSkeletonViewContent();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadData(@NonNull OnLoadingListener<List<Agent>> loadingListener, boolean update) {
        mDataManager.load(mValueExtractor.includeDisconnected(), loadingListener, update);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onSuccessCallBack(List<Agent> data) {
        super.onSuccessCallBack(data);
        int type = mValueExtractor.includeDisconnected() ? AgentTabsViewModelImpl.DISCONNECTED_TAB : AgentTabsViewModelImpl.CONNECTED_TAB;
        mDataManager.postUpdateTabTitleEvent(data.size(), type);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected AgentDataModel createModel(List<Agent> data) {
        return new AgentDataModelImpl(data);
    }
}
