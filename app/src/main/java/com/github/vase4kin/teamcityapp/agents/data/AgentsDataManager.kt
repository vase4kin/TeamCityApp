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

package com.github.vase4kin.teamcityapp.agents.data

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener
import com.github.vase4kin.teamcityapp.agents.api.Agent
import com.github.vase4kin.teamcityapp.agents.api.Agents
import com.github.vase4kin.teamcityapp.base.list.data.BaseListRxDataManager

/**
 * Data manager for [com.github.vase4kin.teamcityapp.agents.view.AgentListFragment]
 */
interface AgentsDataManager : BaseListRxDataManager<Agents, Agent> {

    /**
     * {@inheritDoc}
     */
    fun load(includeDisconnected: Boolean?,
             loadingListener: OnLoadingListener<List<Agent>>,
             update: Boolean)

    /**
     * Load count of agents
     *
     * @param loadingListener - Listener to handle loading server callbacks
     */
    fun loadCount(loadingListener: OnLoadingListener<Int>)

    /**
     * Post update tab tile event
     *
     * @param size - Size of agents
     * @param type - Agent tab type
     */
    fun postUpdateTabTitleEvent(size: Int, type: Int)
}
