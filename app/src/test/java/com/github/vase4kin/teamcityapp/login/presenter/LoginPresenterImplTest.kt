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

package com.github.vase4kin.teamcityapp.login.presenter

import com.github.vase4kin.teamcityapp.account.create.data.CreateAccountDataManager
import com.github.vase4kin.teamcityapp.account.create.data.CustomOnLoadingListener
import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener
import com.github.vase4kin.teamcityapp.login.router.LoginRouter
import com.github.vase4kin.teamcityapp.login.tracker.LoginTracker
import com.github.vase4kin.teamcityapp.login.view.LoginView
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
class LoginPresenterImplTest {

    @Captor
    private lateinit var onLoadingListenerArgumentCaptor: ArgumentCaptor<OnLoadingListener<String>>
    @Captor
    private lateinit var argumentCaptor: ArgumentCaptor<CustomOnLoadingListener<String>>
    @Mock
    private lateinit var view: LoginView
    @Mock
    private lateinit var dataManager: CreateAccountDataManager
    @Mock
    private lateinit var router: LoginRouter
    @Mock
    private lateinit var tracker: LoginTracker
    private lateinit var presenter: LoginPresenterImpl

    @Before
    fun setUp() {
        presenter = LoginPresenterImpl(view, dataManager, router, tracker)
    }

    @After
    fun tearDown() {
        verifyNoMoreInteractions(view, dataManager, router, tracker)
    }

    @Test
    fun testHandleOnCreate() {
        presenter.onCreate()
        verify(view).initViews(eq(presenter))
    }

    @Test
    fun testHandleOnDestroy() {
        presenter.onDestroy()
        verify(view).unbindViews()
    }

    @Test
    fun testHandleOnResume() {
        presenter.onResume()
        verify(tracker).trackView()
    }

    @Test
    fun testOnUserLoginButtonClick() {
        presenter.onUserLoginButtonClick("url", "userName", "password", false)
        verify(view).hideError()
        verify(view).showProgressDialog()
        verify(dataManager).authUser(
            capture(argumentCaptor),
            eq("url"),
            eq("userName"),
            eq("password"),
            eq(false),
            eq(true)
        )

        val customOnLoadingListener = argumentCaptor.value
        customOnLoadingListener.onSuccess("url")

        verify(view).dismissProgressDialog()
        verify(dataManager).saveNewUserAccount(
            eq("url"),
            eq("userName"),
            eq("password"),
            eq(false),
            capture(onLoadingListenerArgumentCaptor)
        )

        val onLoadingListener = onLoadingListenerArgumentCaptor.value
        onLoadingListener.onSuccess("url")

        verify(dataManager).initTeamCityService(eq("url"))
        verify(router).openProjectsRootPageForFirstStart()
        verify(tracker).trackUserLoginSuccess(eq(true))
        verify(view).close()

        onLoadingListener.onFail("error")
        verify(view, times(2)).dismissProgressDialog()
        verify(view).showCouldNotSaveUserError()
        verify(tracker).trackUserDataSaveFailed()
        verify(view).hideKeyboard()

        customOnLoadingListener.onFail(0, "error")
        verify(view, times(3)).dismissProgressDialog()
        verify(view).showError(eq("error"))
        verify(tracker).trackUserLoginFailed(eq("error"))
        verify(view, times(2)).hideKeyboard()
    }

    @Test
    fun testOnUserLoginButtonClickIfServerUrlIsNotProvided() {
        presenter.onUserLoginButtonClick("", "userName", "password", false)
        verify(view).hideError()
        verify(view).showServerUrlCanNotBeEmptyError()
    }

    @Test
    fun testOnUserLoginButtonClickIfUserNameIsNotProvided() {
        presenter.onUserLoginButtonClick("url", "", "password", false)
        verify(view).hideError()
        verify(view).showUserNameCanNotBeEmptyError()
    }

    @Test
    fun testOnUserLoginButtonClickIfPasswordIsNotProvided() {
        presenter.onUserLoginButtonClick("url", "userName", "", false)
        verify(view).hideError()
        verify(view).showPasswordCanNotBeEmptyError()
    }

    @Test
    fun testOnGuestUserLoginButtonClick() {
        presenter.onGuestUserLoginButtonClick("url", false)
        verify(view).hideError()
        verify(view).showProgressDialog()
        verify(dataManager).authGuestUser(capture(argumentCaptor), eq("url"), eq(false), eq(true))

        val listener = argumentCaptor.value
        listener.onSuccess("url")

        verify(view).dismissProgressDialog()
        verify(dataManager).saveGuestUserAccount(eq("url"), eq(false))
        verify(dataManager).initTeamCityService(eq("url"))
        verify(router).openProjectsRootPageForFirstStart()
        verify(tracker).trackGuestUserLoginSuccess(eq(true))
        verify(view).close()

        listener.onFail(0, "error")

        verify(view, times(2)).dismissProgressDialog()
        verify(view).showError(eq("error"))
        verify(tracker).trackGuestUserLoginFailed(eq("error"))
        verify(view).hideKeyboard()

        listener.onFail(401, "error")
        verify(view, times(3)).dismissProgressDialog()
        verify(view, times(1)).showError(eq("error"))
        verify(tracker, times(2)).trackGuestUserLoginFailed(eq("error"))
        verify(view, times(2)).hideKeyboard()
        verify(view).showUnauthorizedInfoDialog()
    }

    @Test
    fun testOnGuestUserLoginButtonClickIfServerUrlIsNotProvided() {
        presenter.onGuestUserLoginButtonClick("", false)
        verify(view).hideError()
        verify(view).showServerUrlCanNotBeEmptyError()
    }

    @Test
    fun testOnDisableSslSwitchClick() {
        presenter.onDisableSslSwitchClick()
        verify(view).showDisableSslWarningDialog()
    }
}
