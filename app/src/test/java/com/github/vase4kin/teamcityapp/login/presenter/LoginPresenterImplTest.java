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

package com.github.vase4kin.teamcityapp.login.presenter;

import com.github.vase4kin.teamcityapp.account.create.data.CreateAccountDataManager;
import com.github.vase4kin.teamcityapp.account.create.data.CustomOnLoadingListener;
import com.github.vase4kin.teamcityapp.login.router.LoginRouter;
import com.github.vase4kin.teamcityapp.login.tracker.LoginTracker;
import com.github.vase4kin.teamcityapp.login.view.LoginView;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class LoginPresenterImplTest {

    @Captor
    private ArgumentCaptor<CustomOnLoadingListener<String>> mArgumentCaptor;

    @Mock
    private LoginView mViewModel;

    @Mock
    private CreateAccountDataManager mDataManager;

    @Mock
    private LoginRouter mRouter;

    @Mock
    private LoginTracker mTracker;

    private LoginPresenterImpl mPresenter;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        mPresenter = new LoginPresenterImpl(mViewModel, mDataManager, mRouter, mTracker);
    }

    @Test
    public void testHandleOnCreate() throws Exception {
        mPresenter.onCreate();
        verify(mViewModel).initViews(eq(mPresenter));
        verifyNoMoreInteractions(mViewModel, mDataManager, mRouter, mTracker);
    }

    @Test
    public void testHandleOnDestroy() throws Exception {
        mPresenter.onDestroy();
        verify(mViewModel).unbindViews();
        verifyNoMoreInteractions(mViewModel, mDataManager, mRouter, mTracker);
    }

    @Test
    public void testHandleOnResume() throws Exception {
        mPresenter.onResume();
        verify(mTracker).trackView();
        verifyNoMoreInteractions(mViewModel, mDataManager, mRouter, mTracker);
    }

    @Test
    public void testHandleOnWindowFocusChanged() throws Exception {
        mPresenter.onWindowFocusChanged(true);
        verify(mViewModel).onWindowFocusChanged(eq(true));
        verifyNoMoreInteractions(mViewModel, mDataManager, mRouter, mTracker);
    }

    @Test
    public void testOnLoginButtonClick() throws Exception {
        mPresenter.onLoginButtonClick("url");
        verify(mViewModel).showProgressDialog();
        verify(mDataManager).loadData(mArgumentCaptor.capture(), eq("url"));

        CustomOnLoadingListener<String> listener = mArgumentCaptor.getValue();
        listener.onSuccess("url");

        verify(mViewModel).dismissProgressDialog();
        verify(mDataManager).createNewUserAccount("url");
        verify(mDataManager).initTeamCityService("url");
        verify(mRouter).openProjectsRootPageForFirstStart();
        verify(mTracker).trackUserLoginSuccess();
        verify(mViewModel).close();

        listener.onFail(0, "error");

        verify(mViewModel, times(2)).dismissProgressDialog();
        verify(mViewModel).setError(eq("error"));
        verify(mTracker).trackUserLoginFailed(eq("error"));
        verify(mViewModel).hideKeyboard();

        verifyNoMoreInteractions(mViewModel, mDataManager, mRouter, mTracker);
    }
}