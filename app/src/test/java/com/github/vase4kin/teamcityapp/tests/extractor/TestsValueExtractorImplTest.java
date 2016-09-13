/*
 * Copyright 2016 Andrey Tolpeev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.vase4kin.teamcityapp.tests.extractor;

import android.os.Bundle;

import com.github.vase4kin.teamcityapp.base.extractor.BundleExtractorValues;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

public class TestsValueExtractorImplTest {

    @Mock
    private Bundle mBundle;

    private TestsValueExtractorImpl mValueExtractor;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mValueExtractor = new TestsValueExtractorImpl(mBundle);
    }

    @Test
    public void testGetUrl() throws Exception {
        when(mBundle.getString(BundleExtractorValues.URL)).thenReturn("url");
        assertThat(mValueExtractor.getUrl(), is(equalTo("url")));
    }

    @Test
    public void testGetPassedCount() throws Exception {
        when(mBundle.getInt(BundleExtractorValues.PASSED_COUNT_PARAM)).thenReturn(45);
        assertThat(mValueExtractor.getPassedCount(), is(equalTo(45)));
    }

    @Test
    public void testGetFailedCount() throws Exception {
        when(mBundle.getInt(BundleExtractorValues.FAILED_COUNT_PARAM)).thenReturn(14);
        assertThat(mValueExtractor.getFailedCount(), is(equalTo(14)));
    }

    @Test
    public void testGetIgnoredCount() throws Exception {
        when(mBundle.getInt(BundleExtractorValues.IGNORED_COUNT_PARAM)).thenReturn(89);
        assertThat(mValueExtractor.getIgnoredCount(), is(equalTo(89)));
    }
}