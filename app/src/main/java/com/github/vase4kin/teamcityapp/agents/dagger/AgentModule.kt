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

package com.github.vase4kin.teamcityapp.agents.dagger

import android.app.Activity
import android.os.Bundle
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.agents.data.AgentDataModel
import com.github.vase4kin.teamcityapp.agents.data.AgentsDataManager
import com.github.vase4kin.teamcityapp.agents.data.AgentsDataManagerImpl
import com.github.vase4kin.teamcityapp.agents.extractor.AgentsValueExtractor
import com.github.vase4kin.teamcityapp.agents.extractor.AgentsValueExtractorImpl
import com.github.vase4kin.teamcityapp.agents.view.AgentListFragment
import com.github.vase4kin.teamcityapp.agents.view.AgentViewHolderFactory
import com.github.vase4kin.teamcityapp.agents.view.AgentViewImpl
import com.github.vase4kin.teamcityapp.agents.view.AgentsAdapter
import com.github.vase4kin.teamcityapp.api.Repository
import com.github.vase4kin.teamcityapp.base.list.view.BaseListView
import com.github.vase4kin.teamcityapp.base.list.view.ViewHolderFactory
import com.github.vase4kin.teamcityapp.base.tracker.ViewTracker
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntKey
import dagger.multibindings.IntoMap
import org.greenrobot.eventbus.EventBus

@Module
class AgentModule {

    @Provides
    fun providesAgentsDataManager(repository: Repository, eventBus: EventBus): AgentsDataManager {
        return AgentsDataManagerImpl(repository, eventBus)
    }

    @Provides
    fun providesBaseListView(
        fragment: AgentListFragment,
        agentsValueExtractor: AgentsValueExtractor,
        adapter: AgentsAdapter
    ): BaseListView<*> {
        return AgentViewImpl(
            agentsValueExtractor.includeDisconnected(),
            fragment.view!!,
            fragment.activity as Activity,
            R.string.empty_list_message_agents,
            adapter
        )
    }

    @Provides
    fun providesAgentsValueExtractor(fragment: AgentListFragment): AgentsValueExtractor {
        return AgentsValueExtractorImpl(fragment.arguments ?: Bundle.EMPTY)
    }

    @Provides
    fun providesViewTracker(): ViewTracker {
        return ViewTracker.STUB
    }

    @Provides
    fun providesAgentsAdapter(viewHolderFactories: Map<Int, @JvmSuppressWildcards ViewHolderFactory<AgentDataModel>>): AgentsAdapter {
        return AgentsAdapter(viewHolderFactories)
    }

    @IntoMap
    @IntKey(BaseListView.TYPE_DEFAULT)
    @Provides
    fun providesAgentViewHolderFactory(): ViewHolderFactory<AgentDataModel> {
        return AgentViewHolderFactory()
    }
}
