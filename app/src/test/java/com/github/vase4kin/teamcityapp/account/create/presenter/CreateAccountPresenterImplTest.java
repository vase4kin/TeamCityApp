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

import com.github.vase4kin.teamcityapp.account.create.data.CreateAccountDataManager;
import com.github.vase4kin.teamcityapp.account.create.data.CreateAccountDataModel;
import com.github.vase4kin.teamcityapp.account.create.data.CustomOnLoadingListener;
import com.github.vase4kin.teamcityapp.account.create.router.CreateAccountRouter;
import com.github.vase4kin.teamcityapp.account.create.tracker.CreateAccountTracker;
import com.github.vase4kin.teamcityapp.account.create.view.CreateAccountView;
import com.github.vase4kin.teamcityapp.account.create.view.OnToolBarNavigationListener;

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
import static org.mockito.Mockito.when;

public class CreateAccountPresenterImplTest {

    @Captor
    private ArgumentCaptor<OnToolBarNavigationListener> mOnToolBarNavigationListenerArgumentCaptor;

    @Captor
    private ArgumentCaptor<CustomOnLoadingListener<String>> mOnLoadingListenerArgumentCaptor;

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
        mPresenter = new CreateAccountPresenterImpl(mView, mDataManager, mDataModel, mRouter, mTracker);
    }

    @Test
    public void testHandleOnCreateView() throws Exception {
        when(mView.isEmailEmpty()).thenReturn(true);
        mPresenter.handleOnCreateView();
        verify(mView).initViews(eq(mPresenter));
        verifyNoMoreInteractions(mView, mDataManager, mDataModel, mTracker);
    }

    @Test
    public void testOnClick() throws Exception {
        when(mView.isEmailEmpty()).thenReturn(false);
        mPresenter.onClick();
        verify(mView).isEmailEmpty();
        verify(mView).showDiscardDialog();
        verifyNoMoreInteractions(mView, mDataManager, mDataModel, mTracker);
    }

    @Test
    public void testHandleOnDestroyView() throws Exception {
        mPresenter.handleOnDestroy();
        verify(mView).onDestroyView();
        verifyNoMoreInteractions(mView, mDataManager, mDataModel, mTracker);
    }

    @Test
    public void testValidateUrlIfAccountExist() throws Exception {
        when(mDataModel.hasAccountWithUrl("url")).thenReturn(true);
        mPresenter.validateUrl("url");
        verify(mView).showProgressDialog();
        verify(mDataModel).hasAccountWithUrl(eq("url"));
        verify(mView).showNewAccountExistErrorMessage();
        verifyNoMoreInteractions(mView, mDataManager, mDataModel, mTracker);
    }

    @Test
    public void testValidateUrlIfAccountIsNotExist() throws Exception {
        when(mDataModel.hasAccountWithUrl("url")).thenReturn(false);
        mPresenter.validateUrl("url");

        verify(mDataModel).hasAccountWithUrl(eq("url"));
        verify(mDataManager).loadData(mOnLoadingListenerArgumentCaptor.capture(), eq("url"));
        verify(mView).showProgressDialog();

        CustomOnLoadingListener<String> listener = mOnLoadingListenerArgumentCaptor.getValue();
        listener.onSuccess("url");
        verify(mDataManager).createNewUserAccount(eq("url"));
        verify(mDataManager).initTeamCityService(eq("url"));
        verify(mView).dismissProgressDialog();
        verify(mView).finish();
        verify(mRouter).startRootProjectActivityWhenNewAccountIsCreated();
        verify(mTracker).trackUserLoginSuccess();

        listener.onFail(0, "error");
        verify(mView).setErrorText(eq("error"));
        verify(mView, times(2)).dismissProgressDialog();
        verify(mTracker).trackUserLoginFailed(eq("error"));

        verifyNoMoreInteractions(mView, mDataManager, mDataModel, mTracker);
    }

    @Test
    public void testDismissWithDiscardDialogIfEmailIsNotEmpty() throws Exception {
        when(mView.isEmailEmpty()).thenReturn(false);
        mPresenter.finish();
        verify(mView).isEmailEmpty();
        verify(mView).showDiscardDialog();
        verifyNoMoreInteractions(mView, mDataManager, mDataModel, mTracker);
    }

    @Test
    public void testDismissWithDiscardDialogIfEmailIsEmpty() throws Exception {
        when(mView.isEmailEmpty()).thenReturn(true);
        mPresenter.finish();
        verify(mView).isEmailEmpty();
        verify(mView).finish();
        verifyNoMoreInteractions(mView, mDataManager, mDataModel, mTracker);
    }

    @Test
    public void testHandleOnResume() throws Exception {
        mPresenter.handleOnResume();
        verify(mTracker).trackView();
        verifyNoMoreInteractions(mView, mDataManager, mDataModel, mTracker);
    }
}