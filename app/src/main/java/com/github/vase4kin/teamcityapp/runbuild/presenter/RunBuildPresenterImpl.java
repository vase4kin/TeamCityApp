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

package com.github.vase4kin.teamcityapp.runbuild.presenter;

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener;
import com.github.vase4kin.teamcityapp.runbuild.interactor.RunBuildInteractor;
import com.github.vase4kin.teamcityapp.runbuild.interactor.RunBuildLoadingListener;
import com.github.vase4kin.teamcityapp.runbuild.router.RunBuildRouter;
import com.github.vase4kin.teamcityapp.runbuild.view.RunBuildView;

import java.util.List;

import javax.inject.Inject;

/**
 * Impl of {@link RunBuildPresenter}
 */
public class RunBuildPresenterImpl implements RunBuildPresenter, RunBuildView.ViewListener {

    private RunBuildView mView;
    private RunBuildInteractor mInteractor;
    private RunBuildRouter mRouter;

    @Inject
    RunBuildPresenterImpl(RunBuildView view,
                          RunBuildInteractor interactor,
                          RunBuildRouter router) {
        this.mView = view;
        this.mInteractor = interactor;
        this.mRouter = router;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate() {
        mView.initViews(this);
        mInteractor.loadBranches(new OnLoadingListener<List<String>>() {
            @Override
            public void onSuccess(List<String> branches) {
                mView.hideBranchesLoadingProgress();
                if (branches.size() == 1) {
                    // set this branch as default and disable the field
                    mView.setupAutoCompleteForSingleBranch(branches);
                } else {
                    // for all other leave the hint as default
                    mView.setupAutoComplete(branches);
                }
                mView.showBranchesAutoComplete();
            }

            @Override
            public void onFail(String errorMessage) {
                mView.hideBranchesLoadingProgress();
                mView.showNoBranchesAvailable();
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDestroy() {
        mInteractor.unsubscribe();
        mView.unbindViews();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onBuildQueue(String branchName) {
        mView.showQueuingBuildProgress();
        mInteractor.queueBuild(branchName, new RunBuildLoadingListener<String>() {

            @Override
            public void onSuccess(String data) {
                mView.hideQueuingBuildProgress();
                mRouter.closeOnSuccess(data);
            }

            @Override
            public void onFail(String errorMessage) {
                mView.hideQueuingBuildProgress();
                mView.showErrorSnackbar();
            }

            @Override
            public void onForbiddenError() {
                mView.hideQueuingBuildProgress();
                mView.showUnauthorizedErrorSnackbar();
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onClick() {
        mRouter.closeOnCancel();
    }
}
