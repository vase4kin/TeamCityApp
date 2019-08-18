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

package com.github.vase4kin.teamcityapp.artifact.data

import com.github.vase4kin.teamcityapp.artifact.api.File
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
class ArtifactDataModelTest {

    @Mock
    private lateinit var file: File

    private lateinit var dataModel: ArtifactDataModelImpl

    @Before
    fun setUp() {
        `when`(file.size).thenReturn(1L)
        val files = ArrayList<File>()
        files.add(file)
        dataModel = ArtifactDataModelImpl(files)
    }

    @Test
    fun testGetSize() {
        assertThat(dataModel.getSize(0), `is`(1L))
    }

    @Test
    fun testGetFile() {
        assertThat(dataModel.getFile(0), `is`(file))
    }

    @Test
    fun testItemCount() {
        assertThat(dataModel.itemCount, `is`(1))
    }
}
