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

package com.github.vase4kin.teamcityapp.agents.presenter

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener
import com.github.vase4kin.teamcityapp.agents.api.Agent
import com.github.vase4kin.teamcityapp.agents.data.AgentDataModel
import com.github.vase4kin.teamcityapp.agents.data.AgentDataModelImpl
import com.github.vase4kin.teamcityapp.agents.data.AgentsDataManager
import com.github.vase4kin.teamcityapp.agents.extractor.AgentsValueExtractor
import com.github.vase4kin.teamcityapp.agenttabs.view.AgentTabsViewModelImpl
import com.github.vase4kin.teamcityapp.base.list.presenter.BaseListPresenterImpl
import com.github.vase4kin.teamcityapp.base.list.view.BaseListView
import com.github.vase4kin.teamcityapp.base.tracker.ViewTracker
import javax.inject.Inject

/**
 * Handles logic of [com.github.vase4kin.teamcityapp.agents.view.AgentListFragment]
 */
class AgentPresenterImpl @Inject constructor(
    view: BaseListView<AgentDataModel>,
    dataManager: AgentsDataManager,
    tracker: ViewTracker,
    valueExtractor: AgentsValueExtractor
) : BaseListPresenterImpl<AgentDataModel, Agent, BaseListView<AgentDataModel>, AgentsDataManager, ViewTracker, AgentsValueExtractor>(
    view,
    dataManager,
    tracker,
    valueExtractor
) {

    override fun initViews() {
        super.initViews()
        view.replaceSkeletonViewContent()
    }

    /**
     * {@inheritDoc}
     */
    public override fun loadData(loadingListener: OnLoadingListener<List<Agent>>, update: Boolean) {
        dataManager.load(valueExtractor.includeDisconnected(), loadingListener, update)
    }

    /**
     * {@inheritDoc}
     */
    public override fun onSuccessCallBack(data: List<Agent>) {
        super.onSuccessCallBack(data)
        val type =
            if (valueExtractor.includeDisconnected()) AgentTabsViewModelImpl.DISCONNECTED_TAB else AgentTabsViewModelImpl.CONNECTED_TAB
        dataManager.postUpdateTabTitleEvent(data.size, type)
    }

    /**
     * {@inheritDoc}
     */
    public override fun createModel(data: List<Agent>): AgentDataModel {
        return AgentDataModelImpl(data)
    }
}
