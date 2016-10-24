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

import android.text.TextUtils;

import com.github.vase4kin.teamcityapp.account.create.data.CreateAccountDataManager;
import com.github.vase4kin.teamcityapp.account.create.data.CustomOnLoadingListener;
import com.github.vase4kin.teamcityapp.login.router.LoginRouter;
import com.github.vase4kin.teamcityapp.login.tracker.LoginTracker;
import com.github.vase4kin.teamcityapp.login.view.LoginView;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(TextUtils.class)
public class LoginPresenterImplTest {

    @Captor
    private ArgumentCaptor<CustomOnLoadingListener<String>> mArgumentCaptor;

    @Mock
    private LoginView mView;

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
        PowerMockito.mockStatic(TextUtils.class);
        when(TextUtils.isEmpty(anyString())).then(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                CharSequence charSequence = (CharSequence) invocation.getArguments()[0];
                return charSequence == null || charSequence.length() == 0;
            }
        });
        mPresenter = new LoginPresenterImpl(mView, mDataManager, mRouter, mTracker);
    }

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(mView, mDataManager, mRouter, mTracker);
    }

    @Test
    public void testHandleOnCreate() throws Exception {
        mPresenter.onCreate();
        verify(mView).initViews(eq(mPresenter));
    }

    @Test
    public void testHandleOnDestroy() throws Exception {
        mPresenter.onDestroy();
        verify(mView).unbindViews();
    }

    @Test
    public void testHandleOnResume() throws Exception {
        mPresenter.onResume();
        verify(mTracker).trackView();
    }

    @Test
    public void testHandleOnWindowFocusChanged() throws Exception {
        mPresenter.onWindowFocusChanged(true);
        verify(mView).onWindowFocusChanged(eq(true));
    }

    @Test
    public void testOnUserLoginButtonClick() throws Exception {
        mPresenter.onUserLoginButtonClick("url", "userName", "password");
        verify(mView).hideError();
        verify(mView).showProgressDialog();
        verify(mDataManager).authUser(mArgumentCaptor.capture(), eq("url"), eq("userName"), eq("password"));

        CustomOnLoadingListener<String> listener = mArgumentCaptor.getValue();
        listener.onSuccess("url");

        verify(mView).dismissProgressDialog();
        verify(mDataManager).saveNewUserAccount(eq("url"), eq("userName"), eq("password"));
        verify(mDataManager).initTeamCityService(eq("url"));
        verify(mRouter).openProjectsRootPageForFirstStart();
        verify(mTracker).trackUserLoginSuccess();
        verify(mView).close();

        listener.onFail(0, "error");

        verify(mView, times(2)).dismissProgressDialog();
        verify(mView).showError(eq("error"));
        verify(mTracker).trackUserLoginFailed(eq("error"));
        verify(mView).hideKeyboard();
    }

    @Test
    public void testOnUserLoginButtonClickIfServerUrlIsNotProvided() throws Exception {
        mPresenter.onUserLoginButtonClick("", "userName", "password");
        verify(mView).hideError();
        verify(mView).showServerUrlCanNotBeEmptyError();
    }

    @Test
    public void testOnUserLoginButtonClickIfUserNameIsNotProvided() throws Exception {
        mPresenter.onUserLoginButtonClick("url", "", "password");
        verify(mView).hideError();
        verify(mView).showUserNameCanNotBeEmptyError();
    }

    @Test
    public void testOnUserLoginButtonClickIfPasswordIsNotProvided() throws Exception {
        mPresenter.onUserLoginButtonClick("url", "userName", "");
        verify(mView).hideError();
        verify(mView).showPasswordCanNotBeEmptyError();
    }

    @Test
    public void testOnGuestUserLoginButtonClick() throws Exception {
        mPresenter.onGuestUserLoginButtonClick("url");
        verify(mView).hideError();
        verify(mView).showProgressDialog();
        verify(mDataManager).authGuestUser(mArgumentCaptor.capture(), eq("url"));

        CustomOnLoadingListener<String> listener = mArgumentCaptor.getValue();
        listener.onSuccess("url");

        verify(mView).dismissProgressDialog();
        verify(mDataManager).saveGuestUserAccount(eq("url"));
        verify(mDataManager).initTeamCityService(eq("url"));
        verify(mRouter).openProjectsRootPageForFirstStart();
        verify(mTracker).trackUserLoginSuccess();
        verify(mView).close();

        listener.onFail(0, "error");

        verify(mView, times(2)).dismissProgressDialog();
        verify(mView).showError(eq("error"));
        verify(mTracker).trackUserLoginFailed(eq("error"));
        verify(mView).hideKeyboard();

        listener.onFail(401, "error");
        verify(mView, times(3)).dismissProgressDialog();
        verify(mView, times(2)).showError(eq("error"));
        verify(mTracker, times(2)).trackUserLoginFailed(eq("error"));
        verify(mView, times(2)).hideKeyboard();
        verify(mView).showUnauthorizedInfoDialog();
    }

    @Test
    public void testOnGuestUserLoginButtonClickIfServerUrlIsNotProvided() throws Exception {
        mPresenter.onGuestUserLoginButtonClick("");
        verify(mView).hideError();
        verify(mView).showServerUrlCanNotBeEmptyError();
    }
}