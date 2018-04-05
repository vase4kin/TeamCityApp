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

import com.github.vase4kin.teamcityapp.buildlog.data.BuildLogInteractor;
import com.github.vase4kin.teamcityapp.buildlog.router.BuildLogRouter;
import com.github.vase4kin.teamcityapp.buildlog.urlprovider.BuildLogUrlProvider;
import com.github.vase4kin.teamcityapp.buildlog.view.BuildLogView;

import org.junit.After;
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
    private BuildLogView mView;

    @Mock
    private BuildLogUrlProvider mBuildLogUrlProvider;

    @Mock
    private BuildLogInteractor mInteractor;

    @Mock
    private BuildLogRouter router;

    private BuildLogPresenterImpl mPresenter;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(mBuildLogUrlProvider.provideUrl()).thenReturn("http://fake-teamcity-url");
        mPresenter = new BuildLogPresenterImpl(mView, mBuildLogUrlProvider, mInteractor, router);
    }

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(mView, mBuildLogUrlProvider, mInteractor, router);
    }

    @Test
    public void testHandleOnCreateViewIfDialogIsNotShown() throws Exception {
        when(mInteractor.isSslDisabled()).thenReturn(false);
        when(mInteractor.isAuthDialogShown()).thenReturn(true);
        mPresenter.onCreateViews();
        verify(mView).initViews(eq(mPresenter));
        verify(mInteractor).isSslDisabled();
        verify(mInteractor).isAuthDialogShown();
        verify(mBuildLogUrlProvider).provideUrl();
        verify(mView).loadBuildLog(eq("http://fake-teamcity-url"));
    }

    @Test
    public void testHandleOnCreateViewIfSslIsDisabled() throws Exception {
        when(mInteractor.isSslDisabled()).thenReturn(true);
        mPresenter.onCreateViews();
        verify(mView).initViews(eq(mPresenter));
        verify(mInteractor).isSslDisabled();
        verify(mView).showSslWarningView();
    }

    @Test
    public void testHandleOnCreateViewIfGuestUser() throws Exception {
        when(mInteractor.isAuthDialogShown()).thenReturn(false);
        when(mInteractor.isSslDisabled()).thenReturn(false);
        when(mInteractor.isGuestUser()).thenReturn(true);
        mPresenter.onCreateViews();
        verify(mView).initViews(eq(mPresenter));
        verify(mInteractor).isSslDisabled();
        verify(mInteractor).isAuthDialogShown();
        verify(mInteractor).isGuestUser();
        verify(mBuildLogUrlProvider).provideUrl();
        verify(mView).loadBuildLog(eq("http://fake-teamcity-url"));
    }

    @Test
    public void testHandleOnCreateViewIfNotGuest() throws Exception {
        when(mInteractor.isGuestUser()).thenReturn(false);
        when(mInteractor.isSslDisabled()).thenReturn(false);
        when(mInteractor.isAuthDialogShown()).thenReturn(false);
        mPresenter.onCreateViews();
        verify(mView).initViews(eq(mPresenter));
        verify(mInteractor).isSslDisabled();
        verify(mInteractor).isGuestUser();
        verify(mInteractor).isAuthDialogShown();
        verify(mView).showAuthView();
    }

    @Test
    public void testHandleOnDestroyView() throws Exception {
        mPresenter.onDestroyViews();
        verify(mView).unBindViews();
        verify(router).unbindCustomsTabs();
    }

    @Test
    public void testLoadBuildLog() throws Exception {
        mPresenter.loadBuildLog();
        verify(mBuildLogUrlProvider).provideUrl();
        verify(mView).loadBuildLog(eq("http://fake-teamcity-url"));
    }

    @Test
    public void testOpenBuildLog() throws Exception {
        mPresenter.onOpenBuildLogInBrowser();
        verify(mBuildLogUrlProvider).provideUrl();
        verify(router).openUrl(eq("http://fake-teamcity-url"));
    }

    @Test
    public void testOnAuthButtonClick() throws Exception {
        mPresenter.onAuthButtonClick();
        verify(mView).hideAuthView();
        verify(mInteractor).setAuthDialogStatus(eq(true));
        verify(mBuildLogUrlProvider).provideUrl();
        verify(mView).loadBuildLog(eq("http://fake-teamcity-url"));
    }
}