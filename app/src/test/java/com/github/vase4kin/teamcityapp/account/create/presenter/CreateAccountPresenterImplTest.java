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

package com.github.vase4kin.teamcityapp.account.create.presenter;

import android.text.TextUtils;

import com.github.vase4kin.teamcityapp.account.create.data.CreateAccountDataManager;
import com.github.vase4kin.teamcityapp.account.create.data.CreateAccountDataModel;
import com.github.vase4kin.teamcityapp.account.create.data.CustomOnLoadingListener;
import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener;
import com.github.vase4kin.teamcityapp.account.create.router.CreateAccountRouter;
import com.github.vase4kin.teamcityapp.account.create.tracker.CreateAccountTracker;
import com.github.vase4kin.teamcityapp.account.create.view.CreateAccountView;
import com.github.vase4kin.teamcityapp.account.create.view.OnToolBarNavigationListener;

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
public class CreateAccountPresenterImplTest {

    @Captor
    private ArgumentCaptor<OnToolBarNavigationListener> mOnToolBarNavigationListenerArgumentCaptor;

    @Captor
    private ArgumentCaptor<OnLoadingListener<String>> mOnLoadingListenerArgumentCaptor;

    @Captor
    private ArgumentCaptor<CustomOnLoadingListener<String>> mCustomOnLoadingListenerArgumentCaptor;

    @Mock
    private CreateAccountView mView;

    @Mock
    private CreateAccountDataManager mDataManager;

    @Mock
    private CreateAccountDataModel mDataModel;

    @Mock
    private CreateAccountRouter mRouter;

    @Mock
    private CreateAccountTracker mTracker;

    private CreateAccountPresenterImpl mPresenter;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        MockitoAnnotations.initMocks(this);
        PowerMockito.mockStatic(TextUtils.class);
        when(TextUtils.isEmpty(anyString())).then(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                CharSequence charSequence = (CharSequence) invocation.getArguments()[0];
                return charSequence == null || charSequence.length() == 0;
            }
        });
        mPresenter = new CreateAccountPresenterImpl(mView, mDataManager, mDataModel, mRouter, mTracker);
    }

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(mView, mDataManager, mRouter, mTracker);
    }

    @Test
    public void testHandleOnCreateView() throws Exception {
        when(mView.isEmailEmpty()).thenReturn(true);
        mPresenter.handleOnCreateView();
        verify(mView).initViews(eq(mPresenter));
    }

    @Test
    public void testOnClick() throws Exception {
        when(mView.isEmailEmpty()).thenReturn(false);
        mPresenter.onClick();
        verify(mView).isEmailEmpty();
        verify(mView).showDiscardDialog();
    }

    @Test
    public void testHandleOnDestroyView() throws Exception {
        mPresenter.handleOnDestroy();
        verify(mView).onDestroyView();
    }

    @Test
    public void testOnUserLoginButtonClickIfServerUrlIsNotProvided() throws Exception {
        mPresenter.validateUserData("", "userName", "password", false);
        verify(mView).hideError();
        verify(mView).showServerUrlCanNotBeEmptyError();
    }

    @Test
    public void testOnUserLoginButtonClickIfUserNameIsNotProvided() throws Exception {
        mPresenter.validateUserData("url", "", "password", false);
        verify(mView).hideError();
        verify(mView).showUserNameCanNotBeEmptyError();
    }

    @Test
    public void testOnUserLoginButtonClickIfPasswordIsNotProvided() throws Exception {
        mPresenter.validateUserData("url", "userName", "", false);
        verify(mView).hideError();
        verify(mView).showPasswordCanNotBeEmptyError();
    }

    @Test
    public void testValidateUserUrlIfAccountExist() throws Exception {
        when(mDataModel.hasAccountWithUrl("url", "userName")).thenReturn(true);
        mPresenter.validateUserData("url", "userName", "password", false);
        verify(mView).hideError();
        verify(mView).showProgressDialog();
        verify(mDataModel).hasAccountWithUrl(eq("url"), eq("userName"));
        verify(mView).showNewAccountExistErrorMessage();
        verify(mView).dismissProgressDialog();
    }

    @Test
    public void testValidateGuestUserUrlIfAccountExist() throws Exception {
        when(mDataModel.hasGuestAccountWithUrl("url")).thenReturn(true);
        mPresenter.validateGuestUserData("url", false);
        verify(mView).hideError();
        verify(mView).showProgressDialog();
        verify(mDataModel).hasGuestAccountWithUrl(eq("url"));
        verify(mView).showNewAccountExistErrorMessage();
        verify(mView).dismissProgressDialog();
    }

    @Test
    public void testValidateGuestUserUrlIfAccountIsNotExist() throws Exception {
        when(mDataModel.hasGuestAccountWithUrl("url")).thenReturn(false);
        mPresenter.validateGuestUserData("url", false);

        verify(mView).hideError();
        verify(mView).showProgressDialog();
        verify(mDataModel).hasGuestAccountWithUrl(eq("url"));
        verify(mDataManager).authGuestUser(mCustomOnLoadingListenerArgumentCaptor.capture(), eq("url"), eq(false));

        CustomOnLoadingListener<String> listener = mCustomOnLoadingListenerArgumentCaptor.getValue();
        listener.onSuccess("url");
        verify(mDataManager).saveGuestUserAccount(eq("url"), eq(false));
        verify(mDataManager).initTeamCityService(eq("url"));
        verify(mView).dismissProgressDialog();
        verify(mView).finish();
        verify(mRouter).startRootProjectActivityWhenNewAccountIsCreated();
        verify(mTracker).trackGuestUserLoginSuccess();

        listener.onFail(0, "error");
        verify(mView).showError(eq("error"));
        verify(mView, times(2)).dismissProgressDialog();
        verify(mTracker).trackGuestUserLoginFailed(eq("error"));
    }

    @Test
    public void testValidateUserUrlIfAccountIsNotExist() throws Exception {
        when(mDataModel.hasAccountWithUrl("url", "userName")).thenReturn(false);
        mPresenter.validateUserData("url", "userName", "password", false);

        verify(mView).hideError();
        verify(mView).showProgressDialog();
        verify(mDataModel).hasAccountWithUrl(eq("url"), eq("userName"));
        verify(mDataManager).authUser(mCustomOnLoadingListenerArgumentCaptor.capture(), eq("url"), eq("userName"), eq("password"), eq(false));

        CustomOnLoadingListener<String> customOnLoadingListener = mCustomOnLoadingListenerArgumentCaptor.getValue();
        customOnLoadingListener.onSuccess("url");
        verify(mDataManager).saveNewUserAccount(eq("url"), eq("userName"), eq("password"), eq(false), mOnLoadingListenerArgumentCaptor.capture());

        OnLoadingListener<String> loadingListener = mOnLoadingListenerArgumentCaptor.getValue();
        loadingListener.onSuccess("url");
        verify(mDataManager).initTeamCityService(eq("url"));
        verify(mView).dismissProgressDialog();
        verify(mView).finish();
        verify(mRouter).startRootProjectActivityWhenNewAccountIsCreated();
        verify(mTracker).trackUserLoginSuccess();

        loadingListener.onFail("");
        verify(mView).showCouldNotSaveUserError();
        verify(mView, times(2)).dismissProgressDialog();
        verify(mTracker).trackUserDataSaveFailed();

        customOnLoadingListener.onFail(0, "error");
        verify(mView).showError(eq("error"));
        verify(mView, times(3)).dismissProgressDialog();
        verify(mTracker).trackUserLoginFailed(eq("error"));
    }

    @Test
    public void testOnGuestUserLoginButtonClickIfServerUrlIsNotProvided() throws Exception {
        mPresenter.validateGuestUserData("", false);
        verify(mView).hideError();
        verify(mView).showServerUrlCanNotBeEmptyError();
    }

    @Test
    public void testDismissWithDiscardDialogIfEmailIsNotEmpty() throws Exception {
        when(mView.isEmailEmpty()).thenReturn(false);
        mPresenter.finish();
        verify(mView).isEmailEmpty();
        verify(mView).showDiscardDialog();
    }

    @Test
    public void testDismissWithDiscardDialogIfEmailIsEmpty() throws Exception {
        when(mView.isEmailEmpty()).thenReturn(true);
        mPresenter.finish();
        verify(mView).isEmailEmpty();
        verify(mView).finish();
    }

    @Test
    public void testHandleOnResume() throws Exception {
        mPresenter.handleOnResume();
        verify(mTracker).trackView();
    }

    @Test
    public void testOnDisableSslSwitchClick() throws Exception {
        mPresenter.onDisableSslSwitchClick();
        verify(mView).showDisableSslWarningDialog();
    }
}