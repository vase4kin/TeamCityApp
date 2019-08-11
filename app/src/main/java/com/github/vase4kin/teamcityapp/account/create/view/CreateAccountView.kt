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

package com.github.vase4kin.teamcityapp.account.create.view

/**
 * View to manage [CreateAccountActivity]
 */
interface CreateAccountView {

    /**
     * Check if email is empty
     *
     * @return boolean email field empty or not
     */
    val isEmailEmpty: Boolean

    /**
     * Init views
     *
     * @param listener - Listener to receive callback on [com.github.vase4kin.teamcityapp.account.create.presenter.CreateAccountPresenterImpl]
     */
    fun initViews(listener: ViewListener)

    /**
     * Set error text for text field
     *
     * @param errorMessage - Error message
     */
    fun showError(errorMessage: String)

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
     * Hide error
     */
    fun hideError()

    /**
     * Unbind all views
     */
    fun onDestroyView()

    /**
     * Show progress loading dialog
     */
    fun showProgressDialog()

    /**
     * Hide progress loading dialog
     */
    fun dismissProgressDialog()

    /**
     * Show discard changes dialog
     */
    fun showDiscardDialog()

    /**
     * Finish activity
     */
    fun finish()

    /**
     * Show error message that account exists
     */
    fun showNewAccountExistErrorMessage()

    /**
     * Show warning dialog about disabling ssl
     */
    fun showDisableSslWarningDialog()

    /**
     * On create account listener
     */
    interface ViewListener : OnValidateListener, OnToolBarNavigationListener {

        /**
         * On ignore ssl switch click
         */
        fun onDisableSslSwitchClick()
    }
}
