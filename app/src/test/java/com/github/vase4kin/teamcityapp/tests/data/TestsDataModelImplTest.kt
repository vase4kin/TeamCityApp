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

package com.github.vase4kin.teamcityapp.tests.data

import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.tests.api.TEST_STATUS_FAILURE
import org.hamcrest.core.Is.`is`
import org.hamcrest.core.IsEqual.equalTo
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.runners.MockitoJUnitRunner
import teamcityapp.features.test_details.repository.models.TestOccurrence

@RunWith(MockitoJUnitRunner::class)
class TestsDataModelImplTest {

    @Mock
    private lateinit var test: TestOccurrence
    private lateinit var dataModel: TestsDataModelImpl

    @Before
    fun setUp() {
        val tests = mutableListOf(test)
        dataModel = TestsDataModelImpl(tests)
    }

    @Test
    fun testIsFailed() {
        `when`(test.isFailed).thenReturn(true)
        assertThat(dataModel.isFailed(0), `is`(equalTo(true)))
    }

    @Test
    fun testGetName() {
        `when`(test.name).thenReturn("name")
        assertThat(dataModel.getName(0), `is`(equalTo("name")))
    }

    @Test
    fun testGetStatusIcon() {
        `when`(test.status).thenReturn(TEST_STATUS_FAILURE)
        assertThat(dataModel.getStatusIcon(0), `is`(equalTo(R.drawable.ic_error_black_24dp)))
    }

    @Test
    fun testGetHref() {
        `when`(test.href).thenReturn("href")
        assertThat(dataModel.getHref(0), `is`(equalTo("href")))
    }

    @Test
    fun testGetStatus() {
        `when`(test.status).thenReturn("status")
        assertThat(dataModel.getStatus(0), `is`(equalTo("status")))
    }

    @Test
    fun testGetItemCount() {
        assertThat(dataModel.itemCount, `is`(equalTo(1)))
    }

    @Test
    fun testAddTestDataModel() {
        val testOccurrence = TestOccurrence()
        dataModel.addMoreBuilds(TestsDataModelImpl(mutableListOf(testOccurrence)))
        assertThat(dataModel.itemCount, `is`(equalTo(2)))
    }

    @Test
    fun testIsLoadMore() {
        dataModel = TestsDataModelImpl(mutableListOf(TestsDataModelImpl.LOAD_MORE))
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
