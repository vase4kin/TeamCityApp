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

package com.github.vase4kin.teamcityapp.tests.extractor

import android.os.Bundle
import com.github.vase4kin.teamcityapp.base.extractor.BundleExtractorValues
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
class TestsValueExtractorImplTest {

    @Mock
    private lateinit var bundle: Bundle
    private lateinit var valueExtractor: TestsValueExtractorImpl

    @Before
    fun setUp() {
        valueExtractor = TestsValueExtractorImpl(bundle)
    }

    @Test
    fun testGetUrl() {
        `when`(bundle.getString(BundleExtractorValues.URL)).thenReturn("url")
        assertThat(valueExtractor.url, `is`(equalTo("url")))
    }

    @Test
    fun testGetPassedCount() {
        `when`(bundle.getInt(BundleExtractorValues.PASSED_COUNT_PARAM)).thenReturn(45)
        assertThat(valueExtractor.passedCount, `is`(equalTo(45)))
    }

    @Test
    fun testGetFailedCount() {
        `when`(bundle.getInt(BundleExtractorValues.FAILED_COUNT_PARAM)).thenReturn(14)
        assertThat(valueExtractor.failedCount, `is`(equalTo(14)))
    }

    @Test
    fun testGetIgnoredCount() {
        `when`(bundle.getInt(BundleExtractorValues.IGNORED_COUNT_PARAM)).thenReturn(89)
        assertThat(valueExtractor.ignoredCount, `is`(equalTo(89)))
    }
}