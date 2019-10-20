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

package com.github.vase4kin.teamcityapp.overview.data

import android.content.Context
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.navigation.api.BuildElement
import org.hamcrest.core.Is.`is`
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

class OverviewDataModelImplTest {

    @Mock
    private lateinit var element: BuildElement
    @Mock
    private lateinit var context: Context

    private lateinit var dataModel: OverviewDataModelImpl

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        val elements = listOf(element)
        dataModel = OverviewDataModelImpl(elements, context)
    }

    @Test
    fun testGetIcon() {
        `when`(element.icon).thenReturn(123)
        assertThat(dataModel.getIcon(0), `is`(123))
    }

    @Test
    fun testGetDescription() {
        `when`(element.description).thenReturn("desc")
        assertThat(dataModel.getDescription(0), `is`("desc"))
    }

    @Test
    fun testGetSectionName() {
        `when`(element.sectionName).thenReturn("section")
        assertThat(dataModel.getHeaderName(0), `is`("section"))
    }

    @Test
    fun testGetItemCount() {
        assertThat(dataModel.itemCount, `is`(1))
    }

    @Test
    fun testIsBranchCard() {
        `when`(element.sectionName).thenReturn("section")
        `when`(context.getString(R.string.build_branch_section_text)).thenReturn("section")
        assertThat(dataModel.isBranchCard(0), `is`(true))
    }
}
