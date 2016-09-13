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

package com.github.vase4kin.teamcityapp.overview.presenter;

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener;
import com.github.vase4kin.teamcityapp.base.list.extractor.BaseValueExtractor;
import com.github.vase4kin.teamcityapp.buildlist.api.Build;
import com.github.vase4kin.teamcityapp.navigation.api.BuildElement;
import com.github.vase4kin.teamcityapp.overview.data.OverViewDataManager;
import com.github.vase4kin.teamcityapp.overview.view.OverviewViewImpl;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class OverviewPresenterImplTest {

    @Mock
    private OnLoadingListener<List<BuildElement>> mLoadingListener;

    @Mock
    private Build mBuild;

    @Mock
    private OverviewViewImpl mView;

    @Mock
    private OverViewDataManager mDataManager;

    @Mock
    private BaseValueExtractor mValueExtractor;

    private OverviewPresenterImpl mPresenter;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mPresenter = new OverviewPresenterImpl(mView, mDataManager, mValueExtractor);
    }

    @Test
    public void testLoadData() throws Exception {
        when(mBuild.getHref()).thenReturn("url");
        when(mValueExtractor.getBuild()).thenReturn(mBuild);
        mPresenter.loadData(mLoadingListener);
        verify(mValueExtractor).getBuild();
        verify(mDataManager).load(eq("url"), eq(mLoadingListener));
        verifyNoMoreInteractions(mView, mDataManager, mValueExtractor);
    }

    @Test
    public void testCreateModel() throws Exception {
        List<BuildElement> elements = Collections.emptyList();
        assertThat(mPresenter.createModel(elements).getItemCount(), is(0));
    }
}