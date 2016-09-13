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

package com.github.vase4kin.teamcityapp.buildlog.presenter;

import com.github.vase4kin.teamcityapp.buildlog.urlprovider.BuildLogUrlProvider;
import com.github.vase4kin.teamcityapp.buildlog.view.BuildLogViewModel;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class BuildLogPresenterImplTest {

    @Mock
    private BuildLogViewModel mViewModel;

    @Mock
    private BuildLogUrlProvider mBuildLogUrlProvider;

    private BuildLogPresenterImpl mPresenter;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(mBuildLogUrlProvider.provideUrl()).thenReturn("http://fake-teamcity-url");
        mPresenter = new BuildLogPresenterImpl(mViewModel, mBuildLogUrlProvider);
    }

    @Test
    public void testHandleOnCreateView() throws Exception {
        mPresenter.onCreateViews();
        verify(mViewModel).initViews(eq(mPresenter));
        verify(mBuildLogUrlProvider).provideUrl();
        verify(mViewModel).loadBuildLog(eq("http://fake-teamcity-url"));
        verifyNoMoreInteractions(mViewModel, mBuildLogUrlProvider);
    }

    @Test
    public void testHandleOnDestroyView() throws Exception {
        mPresenter.onDestroyViews();
        verify(mViewModel).unBindViews();
        verifyNoMoreInteractions(mViewModel, mBuildLogUrlProvider);
    }

    @Test
    public void testLoadBuildLog() throws Exception {
        mPresenter.loadBuildLog();
        verify(mBuildLogUrlProvider).provideUrl();
        verify(mViewModel).loadBuildLog(eq("http://fake-teamcity-url"));
        verifyNoMoreInteractions(mViewModel, mBuildLogUrlProvider);
    }
}