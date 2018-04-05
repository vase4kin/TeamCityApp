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

import javax.inject.Inject;

/**
 * Impl of {@link CreateAccountPresenter}
 */
public class CreateAccountPresenterImpl implements CreateAccountPresenter, CreateAccountView.ViewListener {

    private final CreateAccountView view;
    private final CreateAccountDataManager dataManager;
    private final CreateAccountDataModel dataModel;
    private final CreateAccountRouter router;
    private final CreateAccountTracker tracker;

    @Inject
    CreateAccountPresenterImpl(CreateAccountView view,
                               CreateAccountDataManager dataManager,
                               CreateAccountDataModel dataModel,
                               CreateAccountRouter router,
                               CreateAccountTracker tracker) {
        this.view = view;
        this.dataManager = dataManager;
        this.dataModel = dataModel;
        this.router = router;
        this.tracker = tracker;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleOnCreateView() {
        view.initViews(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleOnResume() {
        tracker.trackView();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleOnDestroy() {
        view.onDestroyView();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validateUserData(String url, final String userName, final String password, final boolean isSslDisabled) {
        view.hideError();
        if (TextUtils.isEmpty(url)) {
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
        if (dataModel.hasAccountWithUrl(url, userName)) {
            view.showNewAccountExistErrorMessage();
            view.dismissProgressDialog();
        } else {
            dataManager.authUser(new CustomOnLoadingListener<String>() {
                @Override
                public void onSuccess(String url) {
                    dataManager.saveNewUserAccount(url, userName, password, isSslDisabled, new OnLoadingListener<String>() {
                        @Override
                        public void onSuccess(String serverUrl) {
                            dataManager.initTeamCityService(serverUrl);
                            tracker.trackUserLoginSuccess(!isSslDisabled);
                            view.dismissProgressDialog();
                            view.finish();
                            router.startRootProjectActivityWhenNewAccountIsCreated();
                        }

                        @Override
                        public void onFail(String errorMessage) {
                            view.showCouldNotSaveUserError();
                            view.dismissProgressDialog();
                            tracker.trackUserDataSaveFailed();
                        }
                    });
                }

                @Override
                public void onFail(int code, String errorMessage) {
                    view.showError(errorMessage);
                    view.dismissProgressDialog();
                    tracker.trackUserLoginFailed(errorMessage);
                }
            }, url, userName, password, isSslDisabled);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validateGuestUserData(String url, final boolean isSslDisabled) {
        view.hideError();
        if (TextUtils.isEmpty(url)) {
            view.showServerUrlCanNotBeEmptyError();
            return;
        }
        view.showProgressDialog();
        if (dataModel.hasGuestAccountWithUrl(url)) {
            view.showNewAccountExistErrorMessage();
            view.dismissProgressDialog();
        } else {
            dataManager.authGuestUser(new CustomOnLoadingListener<String>() {
                @Override
                public void onSuccess(String url) {
                    dataManager.saveGuestUserAccount(url, isSslDisabled);
                    dataManager.initTeamCityService(url);
                    tracker.trackGuestUserLoginSuccess(!isSslDisabled);
                    view.dismissProgressDialog();
                    view.finish();
                    router.startRootProjectActivityWhenNewAccountIsCreated();
                }

                @Override
                public void onFail(int code, String errorMessage) {
                    view.showError(errorMessage);
                    view.dismissProgressDialog();
                    tracker.trackGuestUserLoginFailed(errorMessage);
                }
            }, url, isSslDisabled);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void finish() {
        // If guest user account enabled or not
        if (!view.isEmailEmpty()) {
            view.showDiscardDialog();
        } else {
            view.finish();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onClick() {
        finish();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDisableSslSwitchClick() {
        view.showDisableSslWarningDialog();
    }
}
