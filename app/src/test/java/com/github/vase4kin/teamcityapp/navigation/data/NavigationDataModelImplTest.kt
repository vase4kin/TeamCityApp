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

package com.github.vase4kin.teamcityapp.navigation.data

import com.github.vase4kin.teamcityapp.navigation.api.NavigationItem
import com.github.vase4kin.teamcityapp.navigation.api.Project
import org.hamcrest.core.Is.`is`
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.runners.MockitoJUnitRunner
import java.util.*

@RunWith(MockitoJUnitRunner::class)
class NavigationDataModelImplTest {

    @Mock
    private lateinit var navigationItem: NavigationItem
    private lateinit var dataModel: NavigationDataModel

    @Before
    fun setUp() {
        val items = mutableListOf(navigationItem)
        dataModel = NavigationDataModelImpl(items)
    }

    @Test
    fun testGetName() {
        `when`(navigationItem.name).thenReturn("name")
        assertThat(dataModel.getName(0), `is`("name"))
    }

    @Test
    fun testGetDescription() {
        `when`(navigationItem.description).thenReturn("desc")
        assertThat(dataModel.getDescription(0), `is`("desc"))
    }

    @Test
    fun testIsProject() {
        val items = ArrayList<NavigationItem>()
        items.add(Project())
        dataModel = NavigationDataModelImpl(items)
        assertThat(dataModel.isProject(0), `is`(true))
    }

    @Test
    fun testGetNavigationItem() {
        assertThat(dataModel.getNavigationItem(0), `is`(navigationItem))
    }

    @Test
    fun testGetItemCount() {
        assertThat(dataModel.itemCount, `is`(1))
    }
}