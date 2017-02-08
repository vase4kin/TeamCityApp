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

package com.github.vase4kin.teamcityapp.base.list.extractor;

import android.os.Bundle;

import com.github.vase4kin.teamcityapp.base.extractor.BundleExtractorValues;
import com.github.vase4kin.teamcityapp.buildlist.api.Build;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

public class BaseValueExtractorImplTest {

    @Mock
    private Build mBuild;

    @Mock
    private Bundle mBundle;

    private BaseValueExtractorImpl mValueExtractor;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mValueExtractor = new BaseValueExtractorImpl(mBundle);
    }

    @Test
    public void testGetId() throws Exception {
        when(mBundle.getString(BundleExtractorValues.ID)).thenReturn("id");
        assertThat(mValueExtractor.getId(), is("id"));
    }

    @Test
    public void testGetName() throws Exception {
        when(mBundle.getString(BundleExtractorValues.NAME)).thenReturn("name");
        assertThat(mValueExtractor.getName(), is("name"));
    }

    @Test
    public void testGetBuild() throws Exception {
        when(mBundle.getSerializable(BundleExtractorValues.BUILD)).thenReturn(mBuild);
        assertThat(mValueExtractor.getBuildDetails().toBuild(), is(mBuild));
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void testIsBundleNull() throws Exception {
        mValueExtractor = new BaseValueExtractorImpl(null);
        assertThat(mValueExtractor.isBundleNull(), is(true));
    }
}