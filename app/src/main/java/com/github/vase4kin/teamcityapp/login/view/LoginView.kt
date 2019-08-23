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

package com.github.vase4kin.teamcityapp.login.view

/**
 * View callbacks of [LoginActivity]
 */
interface LoginView {

    /**
     * Init views
     * @param listener
     */
    fun initViews(listener: ViewListener)

    /**
     * Close activity
     */
    fun close()

    /**
     * Show progress loading dialog
     */
    fun showProgressDialog()

    /**
     * Dismiss progress loading dialog
     */
    fun dismissProgressDialog()

    /**
     * Unbind views before activity is destroyed
     */
    fun unbindViews()

    /**
     * Show auth error
     *
     * @param errorMessage - Error message which comes from server response
     */
    fun showError(errorMessage: String)

    /**
     * Hide error
     */
    fun hideError()

    /**
     * Show server url cannot be empty error
     */
    fun showServerUrlCanNotBeEmptyError()

    /**
     * Show server user name cannot be empty error
     */
    fun showUserNameCanNotBeEmptyError()

    /**
     * Show server password cannot be empty error
     */
    fun showPasswordCanNotBeEmptyError()

    /**
     * Show could not save user data error
     */
    fun showCouldNotSaveUserError()

    /**
     * Hide keyboard
     */
    fun hideKeyboard()

    /**
     * Show permissions unauthorized dialog
     */
    fun showUnauthorizedInfoDialog()

    /**
     * Show try it out dialog
     */
    fun showTryItOutDialog(url: String)

    /**
     * Show warning dialog about disabling ssl
     */
    fun showDisableSslWarningDialog()

    /**
     * Show dialog about not secure connection
     */
    fun showNotSecureConnectionDialog(isGuest: Boolean)

    /**
     * Show loading
     */
    fun showTryItOutLoading()

    /**
     * Hide loading
     */
    fun hideTryItOutLoading()

    /**
     * Show try it out text
     */
    fun showTryItOut()

    /**
     * Receiving callback from [LoginViewImpl] to [com.github.vase4kin.teamcityapp.login.presenter.LoginPresenterImpl]
     */
    interface ViewListener {

        /**
         * Handle on login button click for user creation
         *
         * @param serverUrl - TeamCity server url
         * @param userName - User name
         * @param password - User password
         */
        fun onUserLoginButtonClick(serverUrl: String, userName: String, password: String, isSslDisabled: Boolean)

        /**
         * Handle on login button click for guest user creation
         *
         * @param serverUrl - TeamCity server url
         */
        fun onGuestUserLoginButtonClick(serverUrl: String, isSslDisabled: Boolean)

        /**
         * On ignore ssl switch click
         */
        fun onDisableSslSwitchClick()

        fun onAcceptNotSecureConnectionClick(isGuest: Boolean)

        fun onCancelNotSecureConnectionClick()

        fun onTryItOutTextClick()

        fun onTryItOutActionClick()

        fun onDeclineTryItOutActionClick()
    }
}
