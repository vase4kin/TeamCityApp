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

package com.github.vase4kin.teamcityapp.base.tabs.presenter;

import com.github.vase4kin.teamcityapp.base.tabs.data.BaseTabsDataManager;
import com.github.vase4kin.teamcityapp.base.tabs.view.BaseTabsViewModel;
import com.github.vase4kin.teamcityapp.navigation.tracker.ViewTracker;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class BaseTabsPresenterImplTest {

    @Mock
    private BaseTabsViewModel mViewModel;

    @Mock
    private ViewTracker mTracker;

    @Mock
    private BaseTabsDataManager mDataManager;

    private BaseTabsPresenterImpl mPresenter;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        mPresenter = new BaseTabsPresenterImpl<>(mViewModel, mTracker, mDataManager);
    }

    @Test
    public void testHandleOnViewsCreated() throws Exception {
        mPresenter.onViewsCreated();
        verify(mViewModel).initViews();
        verifyNoMoreInteractions(mViewModel, mTracker, mDataManager);
    }

    @Test
    public void testHandleOnViewsDestroyed() throws Exception {
        mPresenter.onViewsDestroyed();
        verify(mViewModel).unBindViews();
        verifyNoMoreInteractions(mViewModel, mTracker, mDataManager);
    }

    @Test
    public void testHandleOnResume() throws Exception {
        mPresenter.onResume();
        verify(mDataManager).registerEventBus();
        verify(mDataManager).setListener(eq(mPresenter));
        verify(mTracker).trackView();
        verifyNoMoreInteractions(mViewModel, mTracker, mDataManager);
    }

    @Test
    public void testHandleOnPause() throws Exception {
        mPresenter.onPause();
        verify(mDataManager).unregisterEventBus();
        verify(mDataManager).setListener(null);
        verifyNoMoreInteractions(mViewModel, mTracker, mDataManager);
    }
}