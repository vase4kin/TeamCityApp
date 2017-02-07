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

package com.github.vase4kin.teamcityapp.properties.presenter;

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener;
import com.github.vase4kin.teamcityapp.base.list.extractor.BaseValueExtractor;
import com.github.vase4kin.teamcityapp.base.list.view.BaseListView;
import com.github.vase4kin.teamcityapp.navigation.tracker.ViewTracker;
import com.github.vase4kin.teamcityapp.overview.data.BuildDetails;
import com.github.vase4kin.teamcityapp.properties.api.Properties;
import com.github.vase4kin.teamcityapp.properties.data.PropertiesDataManager;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class PropertiesPresenterImplTest {

    @Mock
    private Properties.Property mProperty;

    @Mock
    private BuildDetails mBuildDetails;

    @Mock
    private OnLoadingListener<List<Properties.Property>> mLoadingListener;

    @Mock
    private BaseListView mView;

    @Mock
    private PropertiesDataManager mDataManager;

    @Mock
    private ViewTracker mTracker;

    @Mock
    private BaseValueExtractor mValueExtractor;

    private PropertiesPresenterImpl mPresenter;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        mPresenter = new PropertiesPresenterImpl(mView, mDataManager, mTracker, mValueExtractor);
    }

    @Test
    public void testLoadData() throws Exception {
        when(mValueExtractor.getBuildDetails()).thenReturn(mBuildDetails);
        mPresenter.loadData(mLoadingListener);
        verify(mValueExtractor).getBuildDetails();
        verify(mDataManager).load(eq(mBuildDetails), eq(mLoadingListener));
        verifyNoMoreInteractions(mView, mDataManager, mTracker, mValueExtractor);
    }

    @Test
    public void testCreateModel() throws Exception {
        when(mProperty.getName()).thenReturn("name");
        assertThat(mPresenter.createModel(Collections.singletonList(mProperty)).getName(0), is(equalTo("name")));
    }
}