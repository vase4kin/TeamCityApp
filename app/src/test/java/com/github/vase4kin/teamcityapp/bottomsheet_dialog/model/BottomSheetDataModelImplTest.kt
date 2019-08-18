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

package com.github.vase4kin.teamcityapp.bottomsheet_dialog.model

import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.runners.MockitoJUnitRunner

private const val URL = "/app/rest/builds/id:993171/artifacts/content/stdlib-docs.zip"
private const val FILE_NAME = "stdlib-docs.zip"

@RunWith(MockitoJUnitRunner::class)
class BottomSheetDataModelImplTest {

    @Mock
    private lateinit var item: BottomSheetItem

    private lateinit var dataModel: BottomSheetDataModelImpl

    @Before
    fun setUp() {
        `when`(item.title).thenReturn("title")
        dataModel = BottomSheetDataModelImpl(listOf(item))
    }

    @Test
    fun testGetFileName() {
        `when`(item.description).thenReturn(URL)
        assertThat(dataModel.getFileName(0), `is`(FILE_NAME))
    }

    @Test
    fun testGetFileNameIfUrlIsNotCorrect() {
        val url = "url"
        `when`(item.description).thenReturn(url)
        assertThat(dataModel.getFileName(0), `is`(url))
    }
}
