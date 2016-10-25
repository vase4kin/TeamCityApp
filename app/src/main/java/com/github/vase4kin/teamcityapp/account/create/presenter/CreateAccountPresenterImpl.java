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
import com.github.vase4kin.teamcityapp.account.create.router.CreateAccountRouter;
import com.github.vase4kin.teamcityapp.account.create.tracker.CreateAccountTracker;
import com.github.vase4kin.teamcityapp.account.create.view.CreateAccountView;
import com.github.vase4kin.teamcityapp.account.create.view.OnCreateAccountPresenterListener;

import javax.inject.Inject;

/**
 * Impl of {@link CreateAccountPresenter}
 */
public class CreateAccountPresenterImpl implements CreateAccountPresenter, OnCreateAccountPresenterListener {

    private CreateAccountView mView;
    private CreateAccountDataManager mDataManager;
    private CreateAccountDataModel mDataModel;
    private CreateAccountRouter mRouter;
    private CreateAccountTracker mTracker;

    @Inject
    CreateAccountPresenterImpl(CreateAccountView view,
                               CreateAccountDataManager dataManager,
                               CreateAccountDataModel dataModel,
                               CreateAccountRouter router,
                               CreateAccountTracker tracker) {
        this.mView = view;
        this.mDataManager = dataManager;
        this.mDataModel = dataModel;
        this.mRouter = router;
        this.mTracker = tracker;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleOnCreateView() {
        mView.initViews(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleOnResume() {
        mTracker.trackView();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleOnDestroy() {
        mView.onDestroyView();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validateUserData(String url, final String userName, final String password) {
        mView.hideError();
        if (TextUtils.isEmpty(url)) {
            mView.showServerUrlCanNotBeEmptyError();
            return;
        }
        if (TextUtils.isEmpty(userName)) {
            mView.showUserNameCanNotBeEmptyError();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            mView.showPasswordCanNotBeEmptyError();
            return;
        }
        mView.showProgressDialog();
        if (mDataModel.hasAccountWithUrl(url, userName, password)) {
            mView.showNewAccountExistErrorMessage();
            mView.dismissProgressDialog();
        } else {
            mDataManager.authUser(new CustomOnLoadingListener<String>() {
                @Override
                public void onSuccess(String url) {
                    mDataManager.saveNewUserAccount(url, userName, password);
                    mDataManager.initTeamCityService(url);
                    mTracker.trackUserLoginSuccess();
                    mView.dismissProgressDialog();
                    mView.finish();
                    mRouter.startRootProjectActivityWhenNewAccountIsCreated();
                }

                @Override
                public void onFail(int code, String errorMessage) {
                    mView.showError(errorMessage);
                    mView.dismissProgressDialog();
                    mTracker.trackUserLoginFailed(errorMessage);
                }
            }, url, userName, password);
        }
    }

    @Override
    public void validateGuestUserData(String url) {
        mView.hideError();
        if (TextUtils.isEmpty(url)) {
            mView.showServerUrlCanNotBeEmptyError();
            return;
        }
        mView.showProgressDialog();
        if (mDataModel.hasGuestAccountWithUrl(url)) {
            mView.showNewAccountExistErrorMessage();
            mView.dismissProgressDialog();
        } else {
            mDataManager.authGuestUser(new CustomOnLoadingListener<String>() {
                @Override
                public void onSuccess(String url) {
                    mDataManager.saveGuestUserAccount(url);
                    mDataManager.initTeamCityService(url);
                    mTracker.trackUserLoginSuccess();
                    mView.dismissProgressDialog();
                    mView.finish();
                    mRouter.startRootProjectActivityWhenNewAccountIsCreated();
                }

                @Override
                public void onFail(int code, String errorMessage) {
                    mView.showError(errorMessage);
                    mView.dismissProgressDialog();
                    mTracker.trackUserLoginFailed(errorMessage);
                }
            }, url);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void finish() {
        // If guest user account enabled or not
        if (!mView.isEmailEmpty()) {
            mView.showDiscardDialog();
        } else {
            mView.finish();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onClick() {
        finish();
    }
}
