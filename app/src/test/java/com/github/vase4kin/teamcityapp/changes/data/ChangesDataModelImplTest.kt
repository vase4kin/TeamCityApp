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

package com.github.vase4kin.teamcityapp.changes.data

import com.github.vase4kin.teamcityapp.changes.api.ChangeFiles
import com.github.vase4kin.teamcityapp.changes.api.Changes
import org.hamcrest.core.Is.`is`
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.runners.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class ChangesDataModelImplTest {

    @Mock
    private lateinit var files: List<String>
    @Mock
    private lateinit var changeFiles: ChangeFiles
    @Mock
    private lateinit var change: Changes.Change
    private lateinit var dataModel: ChangesDataModel

    @Before
    fun setUp() {
        val changes = mutableListOf(change)
        dataModel = ChangesDataModelImpl(changes)
    }

    @Test
    fun testGetVersion() {
        `when`(change.version).thenReturn("version")
        assertThat(dataModel.getVersion(0), `is`("version"))
    }

    @Test
    fun testGetUserName() {
        `when`(change.username).thenReturn("username")
        assertThat(dataModel.getUserName(0), `is`("username"))
    }

    @Test
    fun testGetDate() {
        `when`(change.date).thenReturn("date")
        assertThat(dataModel.getDate(0), `is`("date"))
    }

    @Test
    fun testGetItemCount() {
        assertThat(dataModel.itemCount, `is`(1))
    }

    @Test
    fun testGetComment() {
        `when`(change.comment).thenReturn("comment")
        assertThat(dataModel.getComment(0), `is`("comment"))
    }

    @Test
    fun testGetFilesCountIfFilesAreNull() {
        `when`(change.files).thenReturn(null)
        assertThat(dataModel.getFilesCount(0), `is`(0))
    }

    @Test
    fun testGetFilesCountIfFilesAreNotNull() {
        `when`(files.size).thenReturn(34)
        `when`(changeFiles.files).thenReturn(files)
        `when`(change.files).thenReturn(changeFiles)
        assertThat(dataModel.getFilesCount(0), `is`(34))
    }

    @Test
    fun testGetChange() {
        assertThat(dataModel.getChange(0), `is`(change))
    }

    @Test
    fun testIsLoadMore() {
        dataModel = ChangesDataModelImpl(mutableListOf(ChangesDataModelImpl.LOAD_MORE))
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