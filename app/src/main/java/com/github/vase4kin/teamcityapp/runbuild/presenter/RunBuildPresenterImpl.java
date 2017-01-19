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
import com.github.vase4kin.teamcityapp.runbuild.interactor.BranchesInteractor;
import com.github.vase4kin.teamcityapp.runbuild.interactor.LoadingListenerWithForbiddenSupport;
import com.github.vase4kin.teamcityapp.runbuild.interactor.RunBuildInteractor;
import com.github.vase4kin.teamcityapp.runbuild.router.RunBuildRouter;
import com.github.vase4kin.teamcityapp.runbuild.tracker.RunBuildTracker;
import com.github.vase4kin.teamcityapp.runbuild.view.BranchesComponentView;
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
    private RunBuildTracker mTracker;
    private BranchesComponentView mBranchesComponentView;
    private BranchesInteractor mBranchesInteractor;

    @Inject
    RunBuildPresenterImpl(RunBuildView view,
                          RunBuildInteractor interactor,
                          RunBuildRouter router,
                          RunBuildTracker tracker,
                          BranchesComponentView branchesComponentView,
                          BranchesInteractor branchesInteractor) {
        this.mView = view;
        this.mInteractor = interactor;
        this.mRouter = router;
        this.mTracker = tracker;
        this.mBranchesComponentView = branchesComponentView;
        this.mBranchesInteractor = branchesInteractor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate() {
        mView.initViews(this);
        mBranchesComponentView.initViews();
        mBranchesInteractor.loadBranches(new OnLoadingListener<List<String>>() {
            @Override
            public void onSuccess(List<String> branches) {
                mBranchesComponentView.hideBranchesLoadingProgress();
                if (branches.size() == 1) {
                    // set this branch as default and disable the field
                    mBranchesComponentView.setupAutoCompleteForSingleBranch(branches);
                } else {
                    // for all other leave the hint as default
                    mBranchesComponentView.setupAutoComplete(branches);
                }
                mBranchesComponentView.showBranchesAutoComplete();
            }

            @Override
            public void onFail(String errorMessage) {
                mBranchesComponentView.hideBranchesLoadingProgress();
                mBranchesComponentView.showNoBranchesAvailable();
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDestroy() {
        mInteractor.unsubscribe();
        mBranchesInteractor.unsubscribe();
        mView.unbindViews();
        mBranchesComponentView.unbindViews();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onResume() {
        mTracker.trackView();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onBuildQueue() {
        String branchName = mBranchesComponentView.getBranchName();
        mView.showQueuingBuildProgress();
        mInteractor.queueBuild(branchName, new LoadingListenerWithForbiddenSupport<String>() {

            @Override
            public void onSuccess(String data) {
                mView.hideQueuingBuildProgress();
                mTracker.trackUserRunBuildSuccess();
                mRouter.closeOnSuccess(data);
            }

            @Override
            public void onFail(String errorMessage) {
                mView.hideQueuingBuildProgress();
                mView.showErrorSnackbar();
                mTracker.trackUserRunBuildFailed();
            }

            @Override
            public void onForbiddenError() {
                mView.hideQueuingBuildProgress();
                mView.showForbiddenErrorSnackbar();
                mTracker.trackUserRunBuildFailedForbidden();
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

    @Override
    public void onBackPressed() {
        mRouter.closeOnBackButtonPressed();
    }
}
