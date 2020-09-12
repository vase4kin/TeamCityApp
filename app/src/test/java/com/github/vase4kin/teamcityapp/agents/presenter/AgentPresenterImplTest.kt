/*
 * Copyright 2020 Andrey Tolpeev
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

import android.os.Bundle
import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener
import com.github.vase4kin.teamcityapp.agents.api.Agent
import com.github.vase4kin.teamcityapp.agents.data.AgentDataModel
import com.github.vase4kin.teamcityapp.agents.data.AgentsDataManager
import com.github.vase4kin.teamcityapp.agents.extractor.AgentsValueExtractor
import com.github.vase4kin.teamcityapp.base.list.view.BaseListView
import com.github.vase4kin.teamcityapp.base.tracker.ViewTracker
import com.github.vase4kin.teamcityapp.filter_bottom_sheet_dialog.filter.FilterProvider
import org.greenrobot.eventbus.EventBus
import org.hamcrest.core.Is.`is`
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.junit.MockitoJUnitRunner
import java.util.ArrayList

@RunWith(MockitoJUnitRunner::class)
class AgentPresenterImplTest {

    @Mock
    private lateinit var data: List<Agent>

    @Mock
    private lateinit var loadingListener: OnLoadingListener<List<Agent>>

    @Mock
    private lateinit var bundle: Bundle

    @Mock
    private lateinit var view: BaseListView<AgentDataModel>

    @Mock
    private lateinit var dataManager: AgentsDataManager

    @Mock
    private lateinit var valueExtractor: AgentsValueExtractor

    @Mock
    private lateinit var tracker: ViewTracker

    @Mock
    private lateinit var eventBus: EventBus
    private val agent = Agent("name")
    private val filterProvider = FilterProvider()
    private lateinit var presenter: AgentPresenterImpl

    @Before
    fun setUp() {
        presenter =
            AgentPresenterImpl(view, dataManager, tracker, valueExtractor, filterProvider, eventBus)
    }

    @Test
    fun testCreateModel() {
        val agents = ArrayList<Agent>()
        agents.add(agent)
        val dataModel = presenter.createModel(agents)
        assertThat(dataModel.getName(0), `is`("name"))
        assertThat(dataModel.itemCount, `is`(1))
        verifyNoMoreInteractions(view, dataManager, tracker, valueExtractor)
    }
}
