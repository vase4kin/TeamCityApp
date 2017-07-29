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

import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener;
import com.github.vase4kin.teamcityapp.agents.api.Agent;
import com.github.vase4kin.teamcityapp.properties.api.Properties;
import com.github.vase4kin.teamcityapp.runbuild.interactor.BranchesInteractor;
import com.github.vase4kin.teamcityapp.runbuild.interactor.LoadingListenerWithForbiddenSupport;
import com.github.vase4kin.teamcityapp.runbuild.interactor.RunBuildInteractor;
import com.github.vase4kin.teamcityapp.runbuild.router.RunBuildRouter;
import com.github.vase4kin.teamcityapp.runbuild.tracker.RunBuildTracker;
import com.github.vase4kin.teamcityapp.runbuild.view.BranchesComponentView;
import com.github.vase4kin.teamcityapp.runbuild.view.RunBuildView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

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

    /**
     * Selected agent
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    @Nullable
    Agent mSelectedAgent;
    /**
     * List of available agents
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    List<Agent> mAgents;
    /**
     * Build custom parameters
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    List<Properties.Property> mProperties = new ArrayList<>();

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

        mView.disableAgentSelectionControl();
        mInteractor.loadAgents(new OnLoadingListener<List<Agent>>() {
            @Override
            public void onSuccess(List<Agent> agents) {
                if (agents.isEmpty()) {
                    mAgents = Collections.emptyList();
                    mView.hideLoadingAgentsProgress();
                    mView.showNoAgentsAvailable();
                    return;
                }
                // Setting dialog list
                Observable.from(agents)
                        .flatMap(new Func1<Agent, Observable<String>>() {
                            @Override
                            public Observable<String> call(Agent agent) {
                                return Observable.just(agent.getName());
                            }
                        })
                        .toList()
                        .subscribeOn(Schedulers.immediate())
                        .subscribe(new Action1<List<String>>() {
                            @Override
                            public void call(List<String> agentNames) {
                                mView.setAgentListDialogWithAgentsList(agentNames);
                            }
                        });
                mAgents = agents;
                mView.hideLoadingAgentsProgress();
                mView.showSelectedAgentView();
                mView.enableAgentSelectionControl();
            }

            @Override
            public void onFail(String errorMessage) {
                mAgents = Collections.emptyList();
                mView.hideLoadingAgentsProgress();
                mView.showNoAgentsAvailable();
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
    public void onBuildQueue(boolean isPersonal,
                             boolean queueToTheTop,
                             boolean cleanAllFiles) {
        String branchName = mBranchesComponentView.getBranchName();
        mView.showQueuingBuildProgress();
        mInteractor.queueBuild(
                branchName,
                mSelectedAgent,
                isPersonal,
                queueToTheTop,
                cleanAllFiles,
                new Properties(mProperties),
                new LoadingListenerWithForbiddenSupport<String>() {

            @Override
            public void onSuccess(String data) {
                mView.hideQueuingBuildProgress();
                if (mProperties.isEmpty()) {
                    mTracker.trackUserRunBuildSuccess();
                } else {
                    mTracker.trackUserRunBuildWithCustomParamsSuccess();
                }
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
    public void onAgentSelected(int agentPosition) {
        if (mAgents.isEmpty()) return;
        mSelectedAgent = mAgents.get(agentPosition);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onAddParameterButtonClick() {
        mView.showAddParameterDialog();
        mTracker.trackUserClicksOnAddNewBuildParamButton();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onClearAllParametersButtonClick() {
        mProperties.clear();
        mView.disableClearAllParametersButton();
        mView.showNoneParametersView();
        mView.removeAllParameterViews();
        mTracker.trackUserClicksOnClearAllBuildParamsButton();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onParameterAdded(String name, String value) {
        Properties.Property property = new Properties.Property(name, value);
        mProperties.add(property);
        mView.hideNoneParametersView();
        mView.enableClearAllParametersButton();
        mView.addParameterView(name, value);
        mTracker.trackUserAddsBuildParam();
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
