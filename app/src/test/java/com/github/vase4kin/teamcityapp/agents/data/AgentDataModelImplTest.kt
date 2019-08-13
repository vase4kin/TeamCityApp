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

package com.github.vase4kin.teamcityapp.agents.data

import com.github.vase4kin.teamcityapp.agents.api.Agent
import org.hamcrest.core.Is.`is`
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.MockitoAnnotations
import java.util.*

class AgentDataModelImplTest {

    private val agent = Agent("name")
    private lateinit var dataModel: AgentDataModelImpl

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        val agents = ArrayList<Agent>()
        agents.add(agent)
        dataModel = AgentDataModelImpl(agents)
    }

    @Test
    fun testGetName() {
        assertThat(dataModel.getName(0), `is`("name"))
    }

    @Test
    fun testGetItemCount() {
        assertThat(dataModel.itemCount, `is`(1))
    }
}