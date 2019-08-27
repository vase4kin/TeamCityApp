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

package com.github.vase4kin.teamcityapp.base.list.extractor

import android.os.Bundle
import com.github.vase4kin.teamcityapp.base.extractor.BundleExtractorValues
import com.github.vase4kin.teamcityapp.buildlist.api.Build
import com.github.vase4kin.teamcityapp.buildlist.filter.BuildListFilter
import org.hamcrest.core.Is.`is`
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.runners.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class BaseValueExtractorImplTest {

    @Mock
    private lateinit var build: Build
    @Mock
    private lateinit var bundle: Bundle
    @Mock
    private lateinit var filter: BuildListFilter
    private lateinit var valueExtractor: BaseValueExtractorImpl

    @Before
    fun setUp() {
        valueExtractor = BaseValueExtractorImpl(bundle)
    }

    @Test
    fun testGetId() {
        `when`(bundle.getString(BundleExtractorValues.ID)).thenReturn("id")
        assertThat(valueExtractor.id, `is`("id"))
    }

    @Test
    fun testGetName() {
        `when`(bundle.getString(BundleExtractorValues.NAME)).thenReturn("name")
        assertThat(valueExtractor.name, `is`("name"))
    }

    @Test
    fun testGetBuild() {
        `when`(bundle.getSerializable(BundleExtractorValues.BUILD)).thenReturn(build)
        assertThat(valueExtractor.buildDetails.toBuild(), `is`(build))
    }

    @Test
    fun testGetBuildListFilter() {
        `when`(bundle.getSerializable(BundleExtractorValues.BUILD_LIST_FILTER)).thenReturn(filter)
        assertThat(valueExtractor.buildListFilter, `is`(filter))
    }

    @Test
    fun testIsBundleNull() {
        `when`(bundle.isEmpty).thenReturn(true)
        val valueExtractor: BaseValueExtractor = BaseValueExtractorImpl(bundle)
        assertThat(valueExtractor.isBundleNullOrEmpty, `is`(true))
    }
}
