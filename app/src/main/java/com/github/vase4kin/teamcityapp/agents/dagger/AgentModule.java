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

package com.github.vase4kin.teamcityapp.agents.dagger;

import android.support.v4.app.Fragment;
import android.view.View;

import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.agents.data.AgentDataModel;
import com.github.vase4kin.teamcityapp.agents.data.AgentsDataManager;
import com.github.vase4kin.teamcityapp.agents.data.AgentsDataManagerImpl;
import com.github.vase4kin.teamcityapp.agents.extractor.AgentsValueExtractor;
import com.github.vase4kin.teamcityapp.agents.extractor.AgentsValueExtractorImpl;
import com.github.vase4kin.teamcityapp.agents.view.AgentViewHolderFactory;
import com.github.vase4kin.teamcityapp.agents.view.AgentViewImpl;
import com.github.vase4kin.teamcityapp.agents.view.AgentsAdapter;
import com.github.vase4kin.teamcityapp.api.TeamCityService;
import com.github.vase4kin.teamcityapp.base.list.view.BaseListView;
import com.github.vase4kin.teamcityapp.base.list.view.ViewHolderFactory;

import java.util.Map;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntKey;
import dagger.multibindings.IntoMap;
import de.greenrobot.event.EventBus;

@Module
public class AgentModule {

    private Fragment mFragment;
    private View mView;

    public AgentModule(Fragment mFragment, View mView) {
        this.mFragment = mFragment;
        this.mView = mView;
    }

    @Provides
    AgentsDataManager providesAgentsDataManager(TeamCityService teamCityService, EventBus eventBus) {
        return new AgentsDataManagerImpl(teamCityService, eventBus);
    }

    @Provides
    BaseListView providesBaseListView(AgentsValueExtractor agentsValueExtractor, AgentsAdapter adapter) {
        return new AgentViewImpl(
                agentsValueExtractor,
                mView,
                mFragment.getActivity(),
                R.string.empty_list_message_agents,
                adapter);
    }

    @Provides
    AgentsValueExtractor providesAgentsValueExtractor() {
        return new AgentsValueExtractorImpl(mFragment.getArguments());
    }

    @Provides
    AgentsAdapter providesAgentsAdapter(Map<Integer, ViewHolderFactory<AgentDataModel>> viewHolderFactories) {
        return new AgentsAdapter(viewHolderFactories);
    }

    @IntoMap
    @IntKey(BaseListView.TYPE_DEFAULT)
    @Provides
    ViewHolderFactory<AgentDataModel> providesAgentViewHolderFactory() {
        return new AgentViewHolderFactory();
    }
}
