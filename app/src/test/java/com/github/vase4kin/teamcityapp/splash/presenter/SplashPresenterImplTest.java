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

package com.github.vase4kin.teamcityapp.splash.presenter;

import com.github.vase4kin.teamcityapp.splash.data.SplashDataManager;
import com.github.vase4kin.teamcityapp.splash.router.SplashRouter;
import com.github.vase4kin.teamcityapp.splash.view.SplashView;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class SplashPresenterImplTest {

    @Mock
    private SplashRouter mRouter;

    @Mock
    private SplashDataManager mDataManager;

    @Mock
    private SplashView mViewModel;

    private SplashPresenterImpl mPresenter;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mPresenter = new SplashPresenterImpl(mRouter, mDataManager, mViewModel);
    }

    @Test
    public void testHandleOnCreateIfThereAreAccounts() {
        when(mDataManager.hasUserAccounts()).thenReturn(true);
        mPresenter.onCreate();
        verify(mDataManager).hasUserAccounts();
        verify(mRouter).openProjectsRootPage();
        verify(mViewModel).close();
        verifyNoMoreInteractions(mDataManager, mRouter, mViewModel);
    }

    @Test
    public void testHandleOnCreateIfThereAreNoAccounts() {
        when(mDataManager.hasUserAccounts()).thenReturn(false);
        mPresenter.onCreate();
        verify(mDataManager).hasUserAccounts();
        verify(mRouter).openLoginPage();
        verify(mViewModel).close();
        verifyNoMoreInteractions(mDataManager, mRouter, mViewModel);
    }
}