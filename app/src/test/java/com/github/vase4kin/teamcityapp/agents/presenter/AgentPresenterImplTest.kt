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

import android.os.Bundle
import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener
import com.github.vase4kin.teamcityapp.agents.api.Agent
import com.github.vase4kin.teamcityapp.agents.data.AgentsDataManager
import com.github.vase4kin.teamcityapp.agents.extractor.AgentsValueExtractor
import com.github.vase4kin.teamcityapp.base.list.view.BaseListView
import com.github.vase4kin.teamcityapp.base.tracker.ViewTracker
import org.hamcrest.core.Is.`is`
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Matchers.eq
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.powermock.modules.junit4.PowerMockRunner
import java.util.*

@RunWith(PowerMockRunner::class)
class AgentPresenterImplTest {

    @Mock
    private lateinit var data: List<Agent>
    @Mock
    private lateinit var loadingListener: OnLoadingListener<List<Agent>>
    @Mock
    private lateinit var bundle: Bundle
    @Mock
    private lateinit var view: BaseListView<*>
    @Mock
    private lateinit var dataManager: AgentsDataManager
    @Mock
    private lateinit var valueExtractor: AgentsValueExtractor
    @Mock
    private lateinit var tracker: ViewTracker
    private val agent = Agent("name")
    private lateinit var presenter: AgentPresenterImpl

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        presenter = AgentPresenterImpl(view, dataManager, tracker, valueExtractor)
    }

    @Test
    fun testLoadData() {
        `when`(valueExtractor.includeDisconnected()).thenReturn(true)
        presenter.loadData(loadingListener, false)
        verify(valueExtractor).includeDisconnected()
        verify(dataManager).load(true, loadingListener, false)
        verifyNoMoreInteractions(view, dataManager, bundle, tracker, valueExtractor)
    }

    @Test
    fun testAddingNumberOfAgentsOnSuccessCallBackIfAgentsTypeIsDisconnected() {
        `when`(data.size).thenReturn(34)
        `when`(valueExtractor.includeDisconnected()).thenReturn(true)

        presenter.onSuccessCallBack(data)
        verify(valueExtractor).includeDisconnected()
        verify(dataManager).postUpdateTabTitleEvent(eq(34), eq(1))
    }

    @Test
    fun testAddingNumberOfAgentsOnSuccessCallBackIfAgentsTypeIsConnected() {
        `when`(data.size).thenReturn(34)
        `when`(valueExtractor.includeDisconnected()).thenReturn(false)

        presenter.onSuccessCallBack(data)
        verify(valueExtractor).includeDisconnected()
        verify(valueExtractor).includeDisconnected()
        verify(dataManager).postUpdateTabTitleEvent(eq(34), eq(0))
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