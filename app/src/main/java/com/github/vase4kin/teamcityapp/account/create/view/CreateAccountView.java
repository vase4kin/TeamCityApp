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

package com.github.vase4kin.teamcityapp.account.create.view;

/**
 * View to manage {@link CreateAccountActivity}
 */
public interface CreateAccountView {

    /**
     * Init views
     *
     * @param listener - Listener to receive callback on {@link com.github.vase4kin.teamcityapp.account.create.presenter.CreateAccountPresenterImpl}
     */
    void initViews(ViewListener listener);

    /**
     * Set error text for text field
     *
     * @param errorMessage - Error message
     */
    void showError(String errorMessage);

    /**
     * Show server url cannot be empty error
     */
    void showServerUrlCanNotBeEmptyError();

    /**
     * Show server user name cannot be empty error
     */
    void showUserNameCanNotBeEmptyError();

    /**
     * Show server password cannot be empty error
     */
    void showPasswordCanNotBeEmptyError();

    /**
     * Show could not save user data error
     */
    void showCouldNotSaveUserError();

    /**
     * Hide error
     */
    void hideError();

    /**
     * Unbind all views
     */
    void onDestroyView();

    /**
     * Show progress loading dialog
     */
    void showProgressDialog();

    /**
     * Hide progress loading dialog
     */
    void dismissProgressDialog();

    /**
     * Show discard changes dialog
     */
    void showDiscardDialog();

    /**
     * Check if email is empty
     *
     * @return boolean email field empty or not
     */
    boolean isEmailEmpty();

    /**
     * Finish activity
     */
    void finish();

    /**
     * Show error message that account exists
     */
    void showNewAccountExistErrorMessage();

    /**
     * Show warning dialog about disabling ssl
     */
    void showDisableSslWarningDialog();

    /**
     * On create account listener
     */
    interface ViewListener extends OnValidateListener, OnToolBarNavigationListener {

        /**
         * On ignore ssl switch click
         */
        void onDisableSslSwitchClick();
    }
}
