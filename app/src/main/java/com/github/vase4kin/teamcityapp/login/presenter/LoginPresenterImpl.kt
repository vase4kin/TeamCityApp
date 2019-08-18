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
import javax.inject.Inject

private const val UNAUTHORIZED_STATUS_CODE = 401

/**
 * Impl for [LoginPresenter]
 */
class LoginPresenterImpl @Inject constructor(
    private val view: LoginView,
    private val dataManager: CreateAccountDataManager,
    private val router: LoginRouter,
    private val tracker: LoginTracker
) : LoginPresenter, LoginView.ViewListener {

    private var loginInfo: LoginInfo? = null

    /**
     * {@inheritDoc}
     */
    override fun onCreate() {
        view.initViews(this)
    }

    /**
     * {@inheritDoc}
     */
    override fun onDestroy() {
        view.unbindViews()
        clearLoginInfo()
    }

    /**
     * {@inheritDoc}
     */
    override fun onResume() {
        tracker.trackView()
    }

    /**
     * {@inheritDoc}
     */
    override fun onUserLoginButtonClick(
        serverUrl: String,
        userName: String,
        password: String,
        isSslDisabled: Boolean
    ) {
        view.hideError()
        if (serverUrl.isEmpty()) {
            view.showServerUrlCanNotBeEmptyError()
            return
        }
        if (userName.isEmpty()) {
            view.showUserNameCanNotBeEmptyError()
            return
        }
        if (password.isEmpty()) {
            view.showPasswordCanNotBeEmptyError()
            return
        }
        loginInfo = LoginInfo(serverUrl, userName, password, isSslDisabled)
        authUser(serverUrl, userName, password, isSslDisabled, checkSecureConnection = true)
    }

    /**
     * {@inheritDoc}
     */
    override fun onGuestUserLoginButtonClick(serverUrl: String, isSslDisabled: Boolean) {
        view.hideError()
        if (serverUrl.isEmpty()) {
            view.showServerUrlCanNotBeEmptyError()
            return
        }
        loginInfo = LoginInfo(serverUrl = serverUrl, isSslDisabled = isSslDisabled)
        authGuestUser(serverUrl, isSslDisabled, checkSecureConnection = true)
    }

    /**
     * {@inheritDoc}
     */
    override fun onDisableSslSwitchClick() {
        view.showDisableSslWarningDialog()
    }

    /**
     * {@inheritDoc}
     */
    override fun onAcceptNotSecureConnectionClick(isGuest: Boolean) {
        val loginInfo = loginInfo ?: return
        if (isGuest) {
            authGuestUser(loginInfo.serverUrl, loginInfo.isSslDisabled)
        } else {
            authUser(loginInfo.serverUrl, loginInfo.userName, loginInfo.password, loginInfo.isSslDisabled)
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun onCancelNotSecureConnectionClick() {
        clearLoginInfo()
    }

    private fun authUser(
        serverUrl: String,
        userName: String,
        password: String,
        isSslDisabled: Boolean,
        checkSecureConnection: Boolean = false
    ) {
        view.showProgressDialog()
        dataManager.authUser(object : CustomOnLoadingListener<String> {
            override fun onSuccess(serverUrl: String) {
                view.dismissProgressDialog()
                dataManager.saveNewUserAccount(serverUrl, userName, password, isSslDisabled, object : OnLoadingListener<String> {
                    override fun onSuccess(serverUrl: String) {
                        dataManager.initTeamCityService(serverUrl)
                        router.openProjectsRootPageForFirstStart()
                        tracker.trackUserLoginSuccess(!isSslDisabled)
                        view.close()
                        clearLoginInfo()
                    }

                    override fun onFail(errorMessage: String) {
                        view.dismissProgressDialog()
                        view.showCouldNotSaveUserError()
                        tracker.trackUserDataSaveFailed()
                        view.hideKeyboard()
                        clearLoginInfo()
                    }
                })
            }

            override fun onFail(statusCode: Int, errorMessage: String) {
                view.dismissProgressDialog()
                tracker.trackUserLoginFailed(errorMessage)
                view.hideKeyboard()
                if (statusCode == CreateAccountDataManager.ERROR_CODE_HTTP_NOT_SECURE) {
                    view.showNotSecureConnectionDialog(false)
                } else {
                    view.showError(errorMessage)
                    clearLoginInfo()
                }
            }
        }, serverUrl, userName, password, isSslDisabled, checkSecureConnection)
    }

    private fun authGuestUser(serverUrl: String, isSslDisabled: Boolean, checkSecureConnection: Boolean = false) {
        view.showProgressDialog()
        dataManager.authGuestUser(object : CustomOnLoadingListener<String> {
            override fun onSuccess(serverUrl: String) {
                view.dismissProgressDialog()
                dataManager.saveGuestUserAccount(serverUrl, isSslDisabled)
                dataManager.initTeamCityService(serverUrl)
                router.openProjectsRootPageForFirstStart()
                tracker.trackGuestUserLoginSuccess(!isSslDisabled)
                view.close()
                clearLoginInfo()
            }

            override fun onFail(statusCode: Int, errorMessage: String) {
                view.dismissProgressDialog()
                tracker.trackGuestUserLoginFailed(errorMessage)
                view.hideKeyboard()
                when (statusCode) {
                    UNAUTHORIZED_STATUS_CODE -> {
                        view.showUnauthorizedInfoDialog()
                        clearLoginInfo()
                    }
                    CreateAccountDataManager.ERROR_CODE_HTTP_NOT_SECURE -> view.showNotSecureConnectionDialog(true)
                    else -> {
                        view.showError(errorMessage)
                        clearLoginInfo()
                    }
                }
            }
        }, serverUrl, isSslDisabled, checkSecureConnection)
    }

    private fun clearLoginInfo() {
        loginInfo = null
    }
}

private data class LoginInfo(
    val serverUrl: String,
    val userName: String = "",
    val password: String = "",
    val isSslDisabled: Boolean
)
