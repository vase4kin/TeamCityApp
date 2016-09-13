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

package com.github.vase4kin.teamcityapp.agents.data;

import com.github.vase4kin.teamcityapp.agents.api.Agent;

import java.util.List;

/**
 * Impl of {@link AgentDataModel}
 */
public class AgentDataModelImpl implements AgentDataModel {

    /**
     * Collection of agents
     */
    private List<Agent> mAgents;

    /**
     * @param agents - Collection of agents received from the server
     */
    public AgentDataModelImpl(List<Agent> agents) {
        this.mAgents = agents;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName(int position) {
        return mAgents.get(position).getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getItemCount() {
        return mAgents.size();
    }
}
