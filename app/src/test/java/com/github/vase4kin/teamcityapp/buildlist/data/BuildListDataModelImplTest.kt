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

package com.github.vase4kin.teamcityapp.buildlist.data

import com.github.vase4kin.teamcityapp.buildlist.api.Build
import com.github.vase4kin.teamcityapp.overview.data.BuildDetails
import com.github.vase4kin.teamcityapp.overview.data.BuildDetailsImpl
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
class BuildListDataModelImplTest {

    @Mock
    private lateinit var build: Build
    @Mock
    private lateinit var buildDetails: BuildDetails
    private lateinit var dataModel: BuildListDataModelImpl

    @Before
    fun setUp() {
        val buildList = ArrayList<BuildDetails>()
        buildList.add(buildDetails)
        dataModel = BuildListDataModelImpl(buildList)
    }

    @Test
    fun testGetBuildNumberIfItIsNull() {
        `when`(buildDetails.number).thenReturn(null)
        assertThat(dataModel.getBuildNumber(0), `is`(""))
    }

    @Test
    fun testGetBuildNumberIfItIsNotNull() {
        `when`(buildDetails.number).thenReturn("123")
        assertThat(dataModel.getBuildNumber(0), `is`("#123"))
    }

    @Test
    fun testGetBranchName() {
        `when`(buildDetails.branchName).thenReturn("branch")
        assertThat(dataModel.getBranchName(0), `is`("branch"))
    }

    @Test
    fun testGetBuildStatusIcon() {
        `when`(buildDetails.statusIcon).thenReturn("icon")
        assertThat(dataModel.getBuildStatusIcon(0), `is`("icon"))
    }

    @Test
    fun testGetStatusText() {
        `when`(buildDetails.statusText).thenReturn("text")
        assertThat(dataModel.getStatusText(0), `is`("text"))
    }

    @Test
    fun testGetBuild() {
        `when`(buildDetails.toBuild()).thenReturn(build)
        assertThat(dataModel.getBuild(0), `is`(build))
    }

    @Test
    fun testAddDataModel() {
        val build = BuildDetailsImpl(this.build)
        val dataModel = BuildListDataModelImpl(mutableListOf(build))
        this.dataModel.addMoreBuilds(dataModel)
        assertThat(this.dataModel.getBuild(1), `is`(this.build))
        assertThat(this.dataModel.itemCount, `is`(2))
    }

    @Test
    fun testGetItemCount() {
        assertThat(dataModel.itemCount, `is`(1))
    }

    @Test
    fun testGetStartDate() {
        `when`(buildDetails.startDateFormattedAsHeader).thenReturn("date")
        assertThat(dataModel.getStartDate(0), `is`("date"))
    }

    @Test
    fun testGetBuildTypeId() {
        `when`(buildDetails.buildTypeId).thenReturn("id")
        assertThat(dataModel.getBuildTypeId(0), `is`("id"))
    }

    @Test
    fun testHasBuildTypeInfo() {
        `when`(buildDetails.hasBuildTypeInfo()).thenReturn(false)
        assertThat(dataModel.hasBuildTypeInfo(0), `is`(false))
    }

    @Test
    fun testGetBuildTypeName() {
        `when`(buildDetails.buildTypeFullName).thenReturn("name")
        assertThat(dataModel.getBuildTypeFullName(0), `is`("name"))
    }

    @Test
    fun testHasBranch() {
        `when`(buildDetails.branchName).thenReturn("")
        assertThat(dataModel.hasBranch(0), `is`(true))
    }

    @Test
    fun testIsPersonal() {
        `when`(buildDetails.isPersonal).thenReturn(true)
        assertThat(dataModel.isPersonal(0), `is`(true))
    }

    @Test
    fun testIsPinned() {
        `when`(buildDetails.isPinned).thenReturn(true)
        assertThat(dataModel.isPinned(0), `is`(true))
    }

    @Test
    fun testIsQueued() {
        `when`(buildDetails.isQueued).thenReturn(true)
        assertThat(dataModel.isQueued(0), `is`(true))
    }

    @Test
    fun testIsLoadMore() {
        dataModel = BuildListDataModelImpl(mutableListOf(BuildListDataModelImpl.LOAD_MORE))
        assertThat(dataModel.isLoadMore(0), `is`(true))
    }

    @Test
    fun testLoadMore() {
        dataModel.addLoadMore()
        assertThat(dataModel.isLoadMore(1), `is`(true))
        assertThat(dataModel.itemCount, `is`(2))
        dataModel.removeLoadMore()
        assertThat(dataModel.isLoadMore(0), `is`(false))
        assertThat(dataModel.itemCount, `is`(1))
    }
}
