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

package com.github.vase4kin.teamcityapp.properties.presenter

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener
import com.github.vase4kin.teamcityapp.base.tracker.ViewTracker
import com.github.vase4kin.teamcityapp.overview.data.BuildDetails
import com.github.vase4kin.teamcityapp.properties.api.Properties
import com.github.vase4kin.teamcityapp.properties.data.PropertiesDataManager
import com.github.vase4kin.teamcityapp.properties.data.PropertiesValueExtractor
import com.github.vase4kin.teamcityapp.properties.view.PropertiesView
import com.github.vase4kin.teamcityapp.utils.eq
import org.hamcrest.core.Is.`is`
import org.hamcrest.core.IsEqual.equalTo
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.runners.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class PropertiesPresenterImplTest {

    @Mock
    private lateinit var property: Properties.Property
    @Mock
    private lateinit var buildDetails: BuildDetails
    @Mock
    private lateinit var loadingListener: OnLoadingListener<List<Properties.Property>>
    @Mock
    private lateinit var view: PropertiesView
    @Mock
    private lateinit var dataManager: PropertiesDataManager
    @Mock
    private lateinit var tracker: ViewTracker
    @Mock
    private lateinit var valueExtractor: PropertiesValueExtractor
    private lateinit var presenter: PropertiesPresenterImpl

    @Before
    fun setUp() {
        presenter = PropertiesPresenterImpl(view, dataManager, tracker, valueExtractor)
    }

    @Test
    fun testLoadData() {
        `when`(valueExtractor.buildDetails).thenReturn(buildDetails)
        presenter.loadData(loadingListener, false)
        verify(valueExtractor).buildDetails
        verify(dataManager).load(eq(buildDetails), eq(loadingListener))
        verifyNoMoreInteractions(view, dataManager, tracker, valueExtractor)
    }

    @Test
    fun testCreateModel() {
        `when`(property.name).thenReturn("name")
        assertThat(presenter.createModel(listOf(property)).getName(0), `is`(equalTo("name")))
    }
}
