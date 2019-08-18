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

package com.github.vase4kin.teamcityapp.build_details.presenter

import android.os.Bundle
import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener
import com.github.vase4kin.teamcityapp.base.tabs.presenter.BaseTabsPresenterImpl
import com.github.vase4kin.teamcityapp.build_details.data.BuildDetailsInteractor
import com.github.vase4kin.teamcityapp.build_details.data.OnBuildDetailsEventsListener
import com.github.vase4kin.teamcityapp.build_details.router.BuildDetailsRouter
import com.github.vase4kin.teamcityapp.build_details.tracker.BuildDetailsTracker
import com.github.vase4kin.teamcityapp.build_details.view.BuildDetailsView
import com.github.vase4kin.teamcityapp.build_details.view.OnBuildDetailsViewListener
import com.github.vase4kin.teamcityapp.buildlist.api.Build
import com.github.vase4kin.teamcityapp.buildlist.data.BuildInteractor
import com.github.vase4kin.teamcityapp.buildlist.filter.BuildListFilterImpl
import com.github.vase4kin.teamcityapp.filter_builds.view.FilterBuildsView
import com.github.vase4kin.teamcityapp.runbuild.interactor.LoadingListenerWithForbiddenSupport
import com.github.vase4kin.teamcityapp.runbuild.interactor.RunBuildInteractor
import javax.inject.Inject

/**
 * Impl of [BuildDetailsPresenter]
 */
class BuildDetailsPresenterImpl @Inject constructor(
    view: BuildDetailsView,
    tracker: BuildDetailsTracker,
    dataManager: BuildDetailsInteractor,
    private val router: BuildDetailsRouter,
    private val runBuildInteractor: RunBuildInteractor,
    private val buildInteractor: BuildInteractor
) : BaseTabsPresenterImpl<BuildDetailsView, BuildDetailsInteractor, BuildDetailsTracker>(view, tracker, dataManager),
    BuildDetailsPresenter, OnBuildDetailsEventsListener, OnBuildDetailsViewListener {

    /**
     * Queued build href
     */
    private var queuedBuildHref: String? = null

    /**
     * {@inheritDoc}
     */
    override fun onViewsCreated() {
        super.onViewsCreated()
        view.setOnBuildTabsViewListener(this)
    }

    override fun onViewsDestroyed() {
        super.onViewsDestroyed()
        interactor.unsubsribe()
        runBuildInteractor.unsubscribe()
        buildInteractor.unsubscribe()
        router.unbindCustomsTabs()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        view.onSave(outState)
    }

    /**
     * {@inheritDoc}
     */
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        view.onRestore(savedInstanceState)
    }

    /**
     * {@inheritDoc}
     */
    override fun onResume() {
        super.onResume()
        interactor.setOnBuildTabsEventsListener(this)
    }

    /**
     * {@inheritDoc}
     */
    override fun onPause() {
        super.onPause()
        interactor.setOnBuildTabsEventsListener(null)
    }

    /**
     * {@inheritDoc}
     */
    override fun onShow() {
        view.showRunBuildFloatActionButton()
    }

    /**
     * {@inheritDoc}
     */
    override fun onHide() {
        view.hideRunBuildFloatActionButton()
    }

    /**
     * {@inheritDoc}
     */
    override fun onCancelBuildActionTriggered() {
        if (interactor.isBuildTriggeredByMe()) {
            showYouAreAboutToCancelBuildDialog()
        } else {
            showYouAreAboutToCancelBuildDialogTriggeredNotByYou()
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun onShareBuildActionTriggered() {
        val buildWebUrl = interactor.getWebUrl()
        router.startShareBuildWebUrlActivity(buildWebUrl)
    }

    /**
     * {@inheritDoc}
     */
    override fun onRestartBuildActionTriggered() {
        view.showYouAreAboutToRestartBuildDialog()
    }

    /**
     * {@inheritDoc}
     */
    override fun onOpenBrowserActionTriggered() {
        val buildWebUrl = interactor.getWebUrl()
        router.openUrlInBrowser(buildWebUrl)
    }

    /**
     * {@inheritDoc}
     */
    override fun onTextCopiedActionTriggered() {
        view.showTextCopiedSnackBar()
    }

    /**
     * {@inheritDoc}
     */
    override fun onErrorDownloadingArtifactActionTriggered() {
        view.showErrorDownloadingArtifactSnackBar()
    }

    /**
     * {@inheritDoc}
     */
    override fun onConfirmCancelingBuild(isReAddToTheQueue: Boolean) {
        tracker.trackUserConfirmedCancel(isReAddToTheQueue)
        showProgress()
        interactor.cancelBuild(object : LoadingListenerWithForbiddenSupport<Build> {
            override fun onForbiddenError() {
                tracker.trackUserGetsForbiddenErrorOnBuildCancel()
                hideProgress()
                showForbiddenToCancelBuildSnackBar()
            }

            override fun onSuccess(@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE") build: Build) {
                tracker.trackUserCanceledBuildSuccessfully()
                hideProgress()
                showBuildIsCancelledSnackBar()
                val buildTypeName = interactor.getBuildTypeName()
                router.reopenBuildTabsActivity(build, buildTypeName)
            }

            override fun onFail(errorMessage: String) {
                tracker.trackUserGetsServerErrorOnBuildCancel()
                hideProgress()
                showBuildIsCancelledErrorSnackBar()
                interactor.postRefreshOverViewDataEvent()
            }
        }, isReAddToTheQueue)
    }

    /**
     * {@inheritDoc}
     */
    override fun onConfirmRestartBuild() {
        val properties = interactor.getBuildDetails().properties
        val branchName = interactor.getBuildDetails().branchName
        view.showRestartingBuildProgressDialog()
        runBuildInteractor.queueBuild(branchName, properties, object : LoadingListenerWithForbiddenSupport<String> {
            override fun onForbiddenError() {
                tracker.trackUserGetsForbiddenErrorOnBuildRestart()
                view.hideRestartingBuildProgressDialog()
                view.showForbiddenToRestartBuildSnackBar()
            }

            override fun onSuccess(data: String) {
                this@BuildDetailsPresenterImpl.queuedBuildHref = data
                tracker.trackUserRestartedBuildSuccessfully()
                view.hideRestartingBuildProgressDialog()
                view.showBuildRestartSuccessSnackBar()
            }

            override fun onFail(errorMessage: String) {
                tracker.trackUserGetsServerErrorOnBuildRestart()
                view.hideRestartingBuildProgressDialog()
                view.showBuildRestartErrorSnackBar()
            }
        })
    }

    /**
     * {@inheritDoc}
     */
    override fun onShowQueuedBuild() {
        view.showBuildLoadingProgress()
        val queuedBuildHref = this@BuildDetailsPresenterImpl.queuedBuildHref ?: return
        buildInteractor.loadBuild(queuedBuildHref, object : OnLoadingListener<Build> {
            override fun onSuccess(queuedBuild: Build) {
                tracker.trackUserWantsToSeeQueuedBuildDetails()
                view.hideBuildLoadingProgress()
                val buildTypeName = interactor.getBuildTypeName()
                router.reopenBuildTabsActivity(queuedBuild, buildTypeName)
            }

            override fun onFail(errorMessage: String) {
                tracker.trackUserFailedToSeeQueuedBuildDetails()
                view.hideBuildLoadingProgress()
                view.showOpeningBuildErrorSnackBar()
            }
        })
    }

    /**
     * {@inheritDoc}
     */
    override fun onStartBuildListActivityFilteredByBranchEventTriggered(branchName: String) {
        val name = interactor.getBuildTypeName()
        val id = interactor.getBuildDetails().buildTypeId
        val filter = BuildListFilterImpl()
        filter.setFilter(FilterBuildsView.FILTER_NONE)
        filter.setBranch(branchName)
        router.startBuildListActivity(name, id, filter)
    }

    /**
     * {@inheritDoc}
     */
    override fun onStartBuildListActivityEventTriggered() {
        val name = interactor.getBuildTypeName()
        val id = interactor.getBuildDetails().buildTypeId
        router.startBuildListActivity(name, id, null)
    }

    /**
     * {@inheritDoc}
     */
    override fun onStartProjectActivityEventTriggered() {
        val projectId = interactor.getProjectId()
        val projectName = interactor.getProjectName()
        router.startProjectActivity(projectName, projectId)
    }

    /**
     * Show forbidden to cancel build snack bar
     */
    private fun showForbiddenToCancelBuildSnackBar() {
        if (interactor.getBuildDetails().isRunning) {
            view.showForbiddenToStopBuildSnackBar()
        } else {
            view.showForbiddenToRemoveBuildFromQueueSnackBar()
        }
    }

    /**
     * Show build is cancelled snack bar
     */
    private fun showBuildIsCancelledSnackBar() {
        if (interactor.getBuildDetails().isRunning) {
            view.showBuildIsStoppedSnackBar()
        } else {
            view.showBuildIsRemovedFromQueueSnackBar()
        }
    }

    /**
     * Show build isn't cancelled due an error snack bar
     */
    private fun showBuildIsCancelledErrorSnackBar() {
        if (interactor.getBuildDetails().isRunning) {
            view.showBuildIsStoppedErrorSnackBar()
        } else {
            view.showBuildIsRemovedFromQueueErrorSnackBar()
        }
    }

    /**
     * Show you are about to cancel build dialog
     */
    private fun showYouAreAboutToCancelBuildDialog() {
        if (interactor.getBuildDetails().isRunning) {
            view.showYouAreAboutToStopBuildDialog()
        } else {
            view.showYouAreAboutToRemoveBuildFromQueueDialog()
        }
    }

    /**
     * Show you are about to cancel build which wasn't triggered by you dialog
     */
    private fun showYouAreAboutToCancelBuildDialogTriggeredNotByYou() {
        if (interactor.getBuildDetails().isRunning) {
            view.showYouAreAboutToStopNotYoursBuildDialog()
        } else {
            view.showYouAreAboutToRemoveBuildFromQueueTriggeredNotByYouDialog()
        }
    }

    /**
     * Show stop/removing from queue build progress
     */
    private fun showProgress() {
        if (interactor.getBuildDetails().isRunning) {
            view.showStoppingBuildProgressDialog()
        } else {
            view.showRemovingBuildFromQueueProgressDialog()
        }
    }

    /**
     * Hide stop/removing from queue build progress
     */
    private fun hideProgress() {
        if (interactor.getBuildDetails().isRunning) {
            view.hideStoppingBuildProgressDialog()
        } else {
            view.hideRemovingBuildFromQueueProgressDialog()
        }
    }
}
