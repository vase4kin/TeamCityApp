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

package com.github.vase4kin.teamcityapp.runbuild.presenter

import androidx.annotation.VisibleForTesting
import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener
import com.github.vase4kin.teamcityapp.agents.api.Agent
import com.github.vase4kin.teamcityapp.properties.api.Properties
import com.github.vase4kin.teamcityapp.runbuild.interactor.BranchesInteractor
import com.github.vase4kin.teamcityapp.runbuild.interactor.LoadingListenerWithForbiddenSupport
import com.github.vase4kin.teamcityapp.runbuild.interactor.RunBuildInteractor
import com.github.vase4kin.teamcityapp.runbuild.router.RunBuildRouter
import com.github.vase4kin.teamcityapp.runbuild.tracker.RunBuildTracker
import com.github.vase4kin.teamcityapp.runbuild.view.BranchesComponentView
import com.github.vase4kin.teamcityapp.runbuild.view.RunBuildView
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import java.util.*
import javax.inject.Inject

/**
 * Impl of [RunBuildPresenter]
 */
class RunBuildPresenterImpl @Inject constructor(
    private val mView: RunBuildView,
    private val mInteractor: RunBuildInteractor,
    private val mRouter: RunBuildRouter,
    private val mTracker: RunBuildTracker,
    private val mBranchesComponentView: BranchesComponentView,
    private val mBranchesInteractor: BranchesInteractor
) : RunBuildPresenter, RunBuildView.ViewListener {

    /**
     * Selected agent
     */
    @VisibleForTesting
    var mSelectedAgent: Agent? = null
    /**
     * List of available agents
     */
    @VisibleForTesting
    var mAgents: List<Agent> = emptyList()
    /**
     * Build custom parameters
     */
    @VisibleForTesting
    var mProperties: MutableList<Properties.Property> = ArrayList()

    private val subscriptions = CompositeDisposable()

    /**
     * {@inheritDoc}
     */
    override fun onCreate() {
        mView.initViews(this)
        mBranchesComponentView.initViews()
        mBranchesInteractor.loadBranches(object : OnLoadingListener<List<String>> {
            override fun onSuccess(branches: List<String>) {
                mBranchesComponentView.hideBranchesLoadingProgress()
                if (branches.size == 1) {
                    // set this branch as default and disable the field
                    mBranchesComponentView.setupAutoCompleteForSingleBranch(branches)
                } else {
                    // for all other leave the hint as default
                    mBranchesComponentView.setupAutoComplete(branches)
                }
                mBranchesComponentView.showBranchesAutoComplete()
            }

            override fun onFail(errorMessage: String) {
                mBranchesComponentView.hideBranchesLoadingProgress()
                mBranchesComponentView.showNoBranchesAvailable()
            }
        })

        mView.disableAgentSelectionControl()
        mInteractor.loadAgents(object : OnLoadingListener<List<Agent>> {
            override fun onSuccess(agents: List<Agent>) {
                if (agents.isEmpty()) {
                    mAgents = emptyList()
                    mView.hideLoadingAgentsProgress()
                    mView.showNoAgentsAvailable()
                    return
                }
                // Setting dialog list
                Observable.fromIterable(agents)
                    .map { it.name }
                    .toList()
                    .subscribeOn(Schedulers.trampoline())
                    .subscribeBy(
                        onSuccess = { mView.setAgentListDialogWithAgentsList(it) }
                    )
                    .addTo(subscriptions)
                mAgents = agents
                mView.hideLoadingAgentsProgress()
                mView.showSelectedAgentView()
                mView.enableAgentSelectionControl()
            }

            override fun onFail(errorMessage: String) {
                mAgents = emptyList()
                mView.hideLoadingAgentsProgress()
                mView.showNoAgentsAvailable()
            }
        })
    }

    /**
     * {@inheritDoc}
     */
    override fun onDestroy() {
        mInteractor.unsubscribe()
        mBranchesInteractor.unsubscribe()
        subscriptions.clear()
        mView.unbindViews()
        mBranchesComponentView.unbindViews()
    }

    /**
     * {@inheritDoc}
     */
    override fun onResume() {
        mTracker.trackView()
    }

    /**
     * {@inheritDoc}
     */
    override fun onBuildQueue(
        isPersonal: Boolean,
        queueToTheTop: Boolean,
        cleanAllFiles: Boolean
    ) {
        val branchName = mBranchesComponentView.branchName
        mView.showQueuingBuildProgress()
        mInteractor.queueBuild(
            branchName,
            mSelectedAgent,
            isPersonal,
            queueToTheTop,
            cleanAllFiles,
            Properties(mProperties),
            object : LoadingListenerWithForbiddenSupport<String> {

                override fun onSuccess(data: String) {
                    mView.hideQueuingBuildProgress()
                    if (mProperties.isEmpty()) {
                        mTracker.trackUserRunBuildSuccess()
                    } else {
                        mTracker.trackUserRunBuildWithCustomParamsSuccess()
                    }
                    mRouter.closeOnSuccess(data)
                }

                override fun onFail(errorMessage: String) {
                    mView.hideQueuingBuildProgress()
                    mView.showErrorSnackbar()
                    mTracker.trackUserRunBuildFailed()
                }

                override fun onForbiddenError() {
                    mView.hideQueuingBuildProgress()
                    mView.showForbiddenErrorSnackbar()
                    mTracker.trackUserRunBuildFailedForbidden()
                }
            })
    }

    /**
     * {@inheritDoc}
     */
    override fun onAgentSelected(agentPosition: Int) {
        if (mAgents.isEmpty()) return
        mSelectedAgent = mAgents[agentPosition]
    }

    /**
     * {@inheritDoc}
     */
    override fun onAddParameterButtonClick() {
        mView.showAddParameterDialog()
        mTracker.trackUserClicksOnAddNewBuildParamButton()
    }

    /**
     * {@inheritDoc}
     */
    override fun onClearAllParametersButtonClick() {
        mProperties.clear()
        mView.disableClearAllParametersButton()
        mView.showNoneParametersView()
        mView.removeAllParameterViews()
        mTracker.trackUserClicksOnClearAllBuildParamsButton()
    }

    /**
     * {@inheritDoc}
     */
    override fun onParameterAdded(name: String, value: String) {
        val property = Properties.Property(name, value)
        mProperties.add(property)
        mView.hideNoneParametersView()
        mView.enableClearAllParametersButton()
        mView.addParameterView(name, value)
        mTracker.trackUserAddsBuildParam()
    }

    /**
     * {@inheritDoc}
     */
    override fun onClick() {
        mRouter.closeOnCancel()
    }

    override fun onBackPressed() {
        mRouter.closeOnBackButtonPressed()
    }
}
