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

package com.github.vase4kin.teamcityapp.root.extractor;

import android.os.Bundle;

import com.github.vase4kin.teamcityapp.base.extractor.BundleExtractorValues;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RootValueExtractorImplTest {

    @Mock
    private Bundle mBundle;

    private RootBundleValueManagerImpl mValueExtractor;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mValueExtractor = new RootBundleValueManagerImpl(mBundle);
    }

    @Test
    public void testIsRequiredToReloadIfBundleIsNull() throws Exception {
        mValueExtractor = new RootBundleValueManagerImpl(null);
        assertThat(mValueExtractor.isRequiredToReload(), is(equalTo(false)));
    }

    @Test
    public void testIsRequiredToReloadIfBundleIsNotNull() throws Exception {
        mValueExtractor = new RootBundleValueManagerImpl(mBundle);
        when(mBundle.getBoolean(BundleExtractorValues.IS_REQUIRED_TO_RELOAD, false)).thenReturn(true);
        assertThat(mValueExtractor.isRequiredToReload(), is(equalTo(true)));
    }

    @Test
    public void testIsNewAccountCreatedIfBundleIsNull() throws Exception {
        mValueExtractor = new RootBundleValueManagerImpl(null);
        assertThat(mValueExtractor.isNewAccountCreated(), is(equalTo(false)));
    }

    @Test
    public void testIsNewAccountCreatedIfBundleIsNotNull() throws Exception {
        mValueExtractor = new RootBundleValueManagerImpl(mBundle);
        when(mBundle.getBoolean(BundleExtractorValues.IS_NEW_ACCOUNT_CREATED, false)).thenReturn(true);
        assertThat(mValueExtractor.isNewAccountCreated(), is(equalTo(true)));
    }

    @Test
    public void testRemoveIsNewAccountCreated() throws Exception {
        mValueExtractor.removeIsNewAccountCreated();
        verify(mBundle).remove(BundleExtractorValues.IS_NEW_ACCOUNT_CREATED);
    }

    @Test
    public void testRemoveIsRequiredToReload() throws Exception {
        mValueExtractor.removeIsRequiredToReload();
        verify(mBundle).remove(BundleExtractorValues.IS_REQUIRED_TO_RELOAD);
    }
}