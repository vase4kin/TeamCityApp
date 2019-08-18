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

package com.github.vase4kin.teamcityapp.account.create.presenter

import com.github.vase4kin.teamcityapp.account.create.data.CreateAccountDataManager
import com.github.vase4kin.teamcityapp.account.create.data.CreateAccountDataModel
import com.github.vase4kin.teamcityapp.account.create.data.CustomOnLoadingListener
import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener
import com.github.vase4kin.teamcityapp.account.create.router.CreateAccountRouter
import com.github.vase4kin.teamcityapp.account.create.tracker.CreateAccountTracker
import com.github.vase4kin.teamcityapp.account.create.view.CreateAccountView
import com.github.vase4kin.teamcityapp.utils.capture
import com.github.vase4kin.teamcityapp.utils.eq
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.runners.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class CreateAccountPresenterImplTest {

    @Captor
    lateinit var onLoadingListenerArgumentCaptor: ArgumentCaptor<OnLoadingListener<String>>

    @Captor
    lateinit var customOnLoadingListenerArgumentCaptor: ArgumentCaptor<CustomOnLoadingListener<String>>

    @Mock
    lateinit var view: CreateAccountView

    @Mock
    lateinit var dataManager: CreateAccountDataManager

    @Mock
    lateinit var dataModel: CreateAccountDataModel

    @Mock
    lateinit var router: CreateAccountRouter

    @Mock
    lateinit var tracker: CreateAccountTracker

    private lateinit var presenter: CreateAccountPresenterImpl

    @Before
    fun setUp() {
        presenter = CreateAccountPresenterImpl(view, dataManager, dataModel, router, tracker)
    }

    @After
    fun tearDown() {
        verifyNoMoreInteractions(view, dataManager, router, tracker)
    }

    @Test
    fun testHandleOnCreateView() {
        `when`(view.isEmailEmpty).thenReturn(true)
        presenter.handleOnCreateView()
        verify(view).initViews(presenter)
    }

    @Test
    fun testOnClick() {
        `when`(view.isEmailEmpty).thenReturn(false)
        presenter.onClick()
        verify(view).isEmailEmpty
        verify(view).showDiscardDialog()
    }

    @Test
    fun testHandleOnDestroyView() {
        presenter.handleOnDestroy()
        verify(view).onDestroyView()
    }

    @Test
    fun testOnUserLoginButtonClickIfServerUrlIsNotProvided() {
        presenter.validateUserData("", "userName", "password", false)
        verify(view).hideError()
        verify(view).showServerUrlCanNotBeEmptyError()
    }

    @Test
    fun testOnUserLoginButtonClickIfUserNameIsNotProvided() {
        presenter.validateUserData("url", "", "password", false)
        verify(view).hideError()
        verify(view).showUserNameCanNotBeEmptyError()
    }

    @Test
    fun testOnUserLoginButtonClickIfPasswordIsNotProvided() {
        presenter.validateUserData("url", "userName", "", false)
        verify(view).hideError()
        verify(view).showPasswordCanNotBeEmptyError()
    }

    @Test
    fun testValidateUserUrlIfAccountExist() {
        `when`(dataModel.hasAccountWithUrl("url", "userName")).thenReturn(true)
        presenter.validateUserData("url", "userName", "password", false)
        verify(view).hideError()
        verify(view).showProgressDialog()
        verify(dataModel).hasAccountWithUrl("url", "userName")
        verify(view).showNewAccountExistErrorMessage()
        verify(view).dismissProgressDialog()
    }

    @Test
    fun testValidateGuestUserUrlIfAccountExist() {
        `when`(dataModel.hasGuestAccountWithUrl("url")).thenReturn(true)
        presenter.validateGuestUserData("url", false)
        verify(view).hideError()
        verify(view).showProgressDialog()
        verify(dataModel).hasGuestAccountWithUrl("url")
        verify(view).showNewAccountExistErrorMessage()
        verify(view).dismissProgressDialog()
    }

    @Test
    fun testValidateGuestUserUrlIfAccountIsNotExist() {
        `when`(dataModel.hasGuestAccountWithUrl("url")).thenReturn(false)
        presenter.validateGuestUserData("url", false)

        verify(view).hideError()
        verify(view).showProgressDialog()
        verify(dataModel).hasGuestAccountWithUrl("url")
        verify(dataManager).authGuestUser(
            capture(customOnLoadingListenerArgumentCaptor),
            eq("url"),
            eq(false),
            eq(false)
        )

        val listener = customOnLoadingListenerArgumentCaptor.value
        listener.onSuccess("url")
        verify(dataManager).saveGuestUserAccount("url", false)
        verify(dataManager).initTeamCityService("url")
        verify(view).dismissProgressDialog()
        verify(view).finish()
        verify(router).startRootProjectActivityWhenNewAccountIsCreated()
        verify(tracker).trackGuestUserLoginSuccess(true)

        listener.onFail(0, "error")
        verify(view).showError("error")
        verify(view, times(2)).dismissProgressDialog()
        verify(tracker).trackGuestUserLoginFailed("error")
    }

    @Test
    fun testValidateUserUrlIfAccountIsNotExist() {
        `when`(dataModel.hasAccountWithUrl("url", "userName")).thenReturn(false)
        presenter.validateUserData("url", "userName", "password", false)

        verify(view).hideError()
        verify(view).showProgressDialog()
        verify(dataModel).hasAccountWithUrl("url", "userName")
        verify(dataManager).authUser(
            customOnLoadingListenerArgumentCaptor.capture(),
            eq("url"),
            eq("userName"),
            eq("password"),
            eq(false),
            eq(false)
        )

        val customOnLoadingListener = customOnLoadingListenerArgumentCaptor.value
        customOnLoadingListener.onSuccess("url")
        verify(dataManager).saveNewUserAccount(
            eq("url"),
            eq("userName"),
            eq("password"),
            eq(false),
            onLoadingListenerArgumentCaptor.capture()
        )

        val loadingListener = onLoadingListenerArgumentCaptor.value
        loadingListener.onSuccess("url")
        verify(dataManager).initTeamCityService("url")
        verify(view).dismissProgressDialog()
        verify(view).finish()
        verify(router).startRootProjectActivityWhenNewAccountIsCreated()
        verify(tracker).trackUserLoginSuccess(true)

        loadingListener.onFail("")
        verify(view).showCouldNotSaveUserError()
        verify(view, times(2)).dismissProgressDialog()
        verify(tracker).trackUserDataSaveFailed()

        customOnLoadingListener.onFail(0, "error")
        verify(view).showError("error")
        verify(view, times(3)).dismissProgressDialog()
        verify(tracker).trackUserLoginFailed("error")
    }

    @Test
    fun testOnGuestUserLoginButtonClickIfServerUrlIsNotProvided() {
        presenter.validateGuestUserData("", false)
        verify(view).hideError()
        verify(view).showServerUrlCanNotBeEmptyError()
    }

    @Test
    fun testDismissWithDiscardDialogIfEmailIsNotEmpty() {
        `when`(view.isEmailEmpty).thenReturn(false)
        presenter.finish()
        verify(view).isEmailEmpty
        verify(view).showDiscardDialog()
    }

    @Test
    fun testDismissWithDiscardDialogIfEmailIsEmpty() {
        `when`(view.isEmailEmpty).thenReturn(true)
        presenter.finish()
        verify(view).isEmailEmpty
        verify(view).finish()
    }

    @Test
    fun testHandleOnResume() {
        presenter.handleOnResume()
        verify(tracker).trackView()
    }

    @Test
    fun testOnDisableSslSwitchClick() {
        presenter.onDisableSslSwitchClick()
        verify(view).showDisableSslWarningDialog()
    }
}
