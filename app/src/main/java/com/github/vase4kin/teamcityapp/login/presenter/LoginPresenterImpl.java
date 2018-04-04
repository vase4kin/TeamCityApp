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

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.github.vase4kin.teamcityapp.account.create.data.CreateAccountDataManager;
import com.github.vase4kin.teamcityapp.account.create.data.CustomOnLoadingListener;
import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener;
import com.github.vase4kin.teamcityapp.login.router.LoginRouter;
import com.github.vase4kin.teamcityapp.login.tracker.LoginTracker;
import com.github.vase4kin.teamcityapp.login.view.LoginView;

import javax.inject.Inject;

/**
 * Impl for {@link LoginPresenter}
 */
public class LoginPresenterImpl implements LoginPresenter, LoginView.ViewListener, OnLoadingListener<String> {

    private static final int UNAUTHORIZED_STATUS_CODE = 401;

    private final LoginView view;
    private final CreateAccountDataManager dataManager;
    private final LoginRouter router;
    private final LoginTracker tracker;

    @Inject
    LoginPresenterImpl(@NonNull LoginView view,
                       @NonNull CreateAccountDataManager dataManager,
                       @NonNull LoginRouter router,
                       @NonNull LoginTracker tracker) {
        this.view = view;
        this.dataManager = dataManager;
        this.router = router;
        this.tracker = tracker;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate() {
        view.initViews(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDestroy() {
        view.unbindViews();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onResume() {
        tracker.trackView();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        view.onWindowFocusChanged(hasFocus);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onUserLoginButtonClick(String serverUrl,
                                       final String userName,
                                       final String password,
                                       final boolean isSslDisabled) {
        view.hideError();
        if (TextUtils.isEmpty(serverUrl)) {
            view.showServerUrlCanNotBeEmptyError();
            return;
        }
        if (TextUtils.isEmpty(userName)) {
            view.showUserNameCanNotBeEmptyError();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            view.showPasswordCanNotBeEmptyError();
            return;
        }
        view.showProgressDialog();
        dataManager.authUser(new CustomOnLoadingListener<String>() {
            @Override
            public void onSuccess(String serverUrl) {
                view.dismissProgressDialog();
                dataManager.saveNewUserAccount(serverUrl, userName, password, isSslDisabled, LoginPresenterImpl.this);
            }

            @Override
            public void onFail(int statusCode, String errorMessage) {
                view.dismissProgressDialog();
                view.showError(errorMessage);
                tracker.trackUserLoginFailed(errorMessage);
                view.hideKeyboard();
            }
        }, serverUrl, userName, password, isSslDisabled);
    }

    /**
     * On data save success callback
     *
     * @param serverUrl - Server url
     */
    @Override
    public void onSuccess(String serverUrl) {
        dataManager.initTeamCityService(serverUrl);
        router.openProjectsRootPageForFirstStart();
        tracker.trackUserLoginSuccess();
        view.close();
    }

    /**
     * On data save fail callback
     *
     * @param errorMessage - Error message
     */
    @Override
    public void onFail(String errorMessage) {
        view.dismissProgressDialog();
        view.showCouldNotSaveUserError();
        tracker.trackUserDataSaveFailed();
        view.hideKeyboard();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onGuestUserLoginButtonClick(String serverUrl, final boolean isSslDisabled) {
        view.hideError();
        if (TextUtils.isEmpty(serverUrl)) {
            view.showServerUrlCanNotBeEmptyError();
            return;
        }
        view.showProgressDialog();
        dataManager.authGuestUser(new CustomOnLoadingListener<String>() {
            @Override
            public void onSuccess(String serverUrl) {
                view.dismissProgressDialog();
                dataManager.saveGuestUserAccount(serverUrl, isSslDisabled);
                dataManager.initTeamCityService(serverUrl);
                router.openProjectsRootPageForFirstStart();
                tracker.trackGuestUserLoginSuccess();
                view.close();
            }

            @Override
            public void onFail(int statusCode, String errorMessage) {
                view.dismissProgressDialog();
                view.showError(errorMessage);
                tracker.trackGuestUserLoginFailed(errorMessage);
                view.hideKeyboard();
                if (statusCode == UNAUTHORIZED_STATUS_CODE) {
                    view.showUnauthorizedInfoDialog();
                }
            }
        }, serverUrl, isSslDisabled);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDisableSslSwitchClick() {
        view.showDisableSslWarningDialog();
    }
}
