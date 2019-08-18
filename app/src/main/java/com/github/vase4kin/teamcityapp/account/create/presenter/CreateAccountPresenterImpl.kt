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
import javax.inject.Inject

/**
 * Impl of [CreateAccountPresenter]
 */
class CreateAccountPresenterImpl @Inject constructor(
    private val view: CreateAccountView,
    private val dataManager: CreateAccountDataManager,
    private val dataModel: CreateAccountDataModel,
    private val router: CreateAccountRouter,
    private val tracker: CreateAccountTracker
) : CreateAccountPresenter, CreateAccountView.ViewListener {

    /**
     * {@inheritDoc}
     */
    override fun handleOnCreateView() {
        view.initViews(this)
    }

    /**
     * {@inheritDoc}
     */
    override fun handleOnResume() {
        tracker.trackView()
    }

    /**
     * {@inheritDoc}
     */
    override fun handleOnDestroy() {
        view.onDestroyView()
    }

    /**
     * {@inheritDoc}
     */
    override fun validateUserData(url: String, userName: String, password: String, isSslDisabled: Boolean) {
        view.hideError()
        if (url.isEmpty()) {
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
        view.showProgressDialog()
        if (dataModel.hasAccountWithUrl(url, userName)) {
            view.showNewAccountExistErrorMessage()
            view.dismissProgressDialog()
        } else {
            dataManager.authUser(object : CustomOnLoadingListener<String> {
                override fun onSuccess(url: String) {
                    dataManager.saveNewUserAccount(
                        url,
                        userName,
                        password,
                        isSslDisabled,
                        object : OnLoadingListener<String> {
                            override fun onSuccess(serverUrl: String) {
                                dataManager.initTeamCityService(serverUrl)
                                tracker.trackUserLoginSuccess(!isSslDisabled)
                                view.dismissProgressDialog()
                                view.finish()
                                router.startRootProjectActivityWhenNewAccountIsCreated()
                            }

                            override fun onFail(errorMessage: String) {
                                view.showCouldNotSaveUserError()
                                view.dismissProgressDialog()
                                tracker.trackUserDataSaveFailed()
                            }
                        })
                }

                override fun onFail(code: Int, errorMessage: String) {
                    view.showError(errorMessage)
                    view.dismissProgressDialog()
                    tracker.trackUserLoginFailed(errorMessage)
                }
            }, url, userName, password, isSslDisabled, false)
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun validateGuestUserData(url: String, isSslDisabled: Boolean) {
        view.hideError()
        if (url.isEmpty()) {
            view.showServerUrlCanNotBeEmptyError()
            return
        }
        view.showProgressDialog()
        if (dataModel.hasGuestAccountWithUrl(url)) {
            view.showNewAccountExistErrorMessage()
            view.dismissProgressDialog()
        } else {
            dataManager.authGuestUser(object : CustomOnLoadingListener<String> {
                override fun onSuccess(url: String) {
                    dataManager.saveGuestUserAccount(url, isSslDisabled)
                    dataManager.initTeamCityService(url)
                    tracker.trackGuestUserLoginSuccess(!isSslDisabled)
                    view.dismissProgressDialog()
                    view.finish()
                    router.startRootProjectActivityWhenNewAccountIsCreated()
                }

                override fun onFail(statusCode: Int, errorMessage: String) {
                    view.showError(errorMessage)
                    view.dismissProgressDialog()
                    tracker.trackGuestUserLoginFailed(errorMessage)
                }
            }, url, isSslDisabled, false)
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun finish() {
        // If guest user account enabled or not
        if (!view.isEmailEmpty) {
            view.showDiscardDialog()
        } else {
            view.finish()
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun onClick() {
        finish()
    }

    /**
     * {@inheritDoc}
     */
    override fun onDisableSslSwitchClick() {
        view.showDisableSslWarningDialog()
    }
}
