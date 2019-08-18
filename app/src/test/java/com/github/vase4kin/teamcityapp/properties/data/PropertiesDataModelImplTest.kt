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

package com.github.vase4kin.teamcityapp.properties.data

import com.github.vase4kin.teamcityapp.properties.api.Properties
import org.hamcrest.core.Is.`is`
import org.hamcrest.core.IsEqual.equalTo
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.runners.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class PropertiesDataModelImplTest {

    @Mock
    private lateinit var property: Properties.Property
    private lateinit var dataModel: PropertiesDataModel

    @Before
    fun setUp() {
        val properties = listOf(property)
        dataModel = PropertiesDataModelImpl(properties)
    }

    @Test
    fun testGetName() {
        `when`(property.name).thenReturn("name")
        assertThat(dataModel.getName(0), `is`(equalTo("name")))
    }

    @Test
    fun testGetValueIfEmpty() {
        `when`(property.value).thenReturn("")
        assertThat(dataModel.getValue(0), `is`(equalTo(PropertiesDataModelImpl.EMPTY)))
    }

    @Test
    fun testGetValueIfNotEmpty() {
        `when`(property.value).thenReturn("value")
        assertThat(dataModel.getValue(0), `is`(equalTo("value")))
    }

    @Test
    fun testIsEmpty() {
        `when`(property.value).thenReturn(PropertiesDataModelImpl.EMPTY)
        assertThat(dataModel.isEmpty(0), `is`(equalTo(true)))
    }

    @Test
    fun testGetItemCount() {
        assertThat(dataModel.itemCount, `is`(1))
    }
}
