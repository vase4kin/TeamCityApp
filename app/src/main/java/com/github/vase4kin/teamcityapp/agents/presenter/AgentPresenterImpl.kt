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
import com.github.vase4kin.teamcityapp.base.list.presenter.BaseListPresenterImpl
import com.github.vase4kin.teamcityapp.base.list.view.BaseListView
import com.github.vase4kin.teamcityapp.base.tracker.ViewTracker
import com.github.vase4kin.teamcityapp.filter_bottom_sheet_dialog.filter.Filter
import com.github.vase4kin.teamcityapp.filter_bottom_sheet_dialog.filter.FilterProvider
import com.github.vase4kin.teamcityapp.home.data.HomeDataManager
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import javax.inject.Inject

/**
 * Handles logic of [com.github.vase4kin.teamcityapp.agents.view.AgentListFragment]
 */
class AgentPresenterImpl @Inject constructor(
    view: BaseListView<AgentDataModel>,
    dataManager: AgentsDataManager,
    tracker: ViewTracker,
    valueExtractor: AgentsValueExtractor,
    private val filterProvider: FilterProvider,
    private val eventBus: EventBus
) : BaseListPresenterImpl<AgentDataModel, Agent, BaseListView<AgentDataModel>, AgentsDataManager, ViewTracker, AgentsValueExtractor>(
    view,
    dataManager,
    tracker,
    valueExtractor
) {

    /**
     * {@inheritDoc}
     */
    override fun initViews() {
        super.initViews()
        view.replaceSkeletonViewContent()
    }

    /**
     * {@inheritDoc}
     */
    public override fun loadData(loadingListener: OnLoadingListener<List<Agent>>, update: Boolean) {
        when (filterProvider.agentsFilter) {
            Filter.AGENTS_CONNECTED -> dataManager.load(false, loadingListener, update)
            Filter.AGENTS_DISCONNECTED -> dataManager.load(true, loadingListener, update)
            else -> view.hideRefreshAnimation()
        }
    }

    /**
     * {@inheritDoc}
     */
    public override fun createModel(data: List<Agent>): AgentDataModel {
        return AgentDataModelImpl(data)
    }

    /**
     * {@inheritDoc}
     */
    override fun onResume() {
        super.onResume()
        loadData()
        eventBus.register(this)
    }

    /**
     * On pause activity callback
     */
    fun onPause() {
        stopLoadingData()
        eventBus.unregister(this)
    }

    private fun loadData() {
        view.showRefreshAnimation()
        loadData(loadingListener, false)
    }

    private fun stopLoadingData() {
        view.hideRefreshAnimation()
        dataManager.unsubscribe()
    }

    /***
     * Handle receiving post events from [EventBus]
     *
     * @param event [HomeDataManager.RunningBuildsFilterChangedEvent]
     */
    @Suppress("unused")
    @Subscribe
    fun onEvent(@Suppress("UNUSED_PARAMETER") event: HomeDataManager.AgentsFilterChangedEvent) =
        loadData()
}
