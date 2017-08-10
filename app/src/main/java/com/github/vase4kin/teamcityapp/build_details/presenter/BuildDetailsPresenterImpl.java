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

package com.github.vase4kin.teamcityapp.build_details.presenter;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener;
import com.github.vase4kin.teamcityapp.base.tabs.presenter.BaseTabsPresenterImpl;
import com.github.vase4kin.teamcityapp.build_details.data.BuildDetailsInteractor;
import com.github.vase4kin.teamcityapp.build_details.data.OnBuildDetailsEventsListener;
import com.github.vase4kin.teamcityapp.build_details.router.BuildDetailsRouter;
import com.github.vase4kin.teamcityapp.build_details.tracker.BuildDetailsTracker;
import com.github.vase4kin.teamcityapp.build_details.view.BuildDetailsView;
import com.github.vase4kin.teamcityapp.build_details.view.OnBuildDetailsViewListener;
import com.github.vase4kin.teamcityapp.buildlist.api.Build;
import com.github.vase4kin.teamcityapp.buildlist.data.BuildInteractor;
import com.github.vase4kin.teamcityapp.buildlist.filter.BuildListFilter;
import com.github.vase4kin.teamcityapp.buildlist.filter.BuildListFilterImpl;
import com.github.vase4kin.teamcityapp.filter_builds.view.FilterBuildsView;
import com.github.vase4kin.teamcityapp.properties.api.Properties;
import com.github.vase4kin.teamcityapp.runbuild.interactor.LoadingListenerWithForbiddenSupport;
import com.github.vase4kin.teamcityapp.runbuild.interactor.RunBuildInteractor;

import javax.inject.Inject;

/**
 * Impl of {@link BuildDetailsPresenter}
 */
public class BuildDetailsPresenterImpl extends BaseTabsPresenterImpl<BuildDetailsView, BuildDetailsInteractor, BuildDetailsTracker>
        implements BuildDetailsPresenter, OnBuildDetailsEventsListener, OnBuildDetailsViewListener {

    /**
     * Queued build href
     */
    private String mQueuedBuildHref;

    private BuildDetailsRouter mRouter;
    private RunBuildInteractor mRunBuildInteractor;
    private BuildInteractor mBuildInteractor;

    @Inject
    BuildDetailsPresenterImpl(@NonNull BuildDetailsView view,
                              @NonNull BuildDetailsTracker tracker,
                              @NonNull BuildDetailsInteractor dataManager,
                              @NonNull BuildDetailsRouter router,
                              @NonNull RunBuildInteractor runBuildInteractor,
                              @NonNull BuildInteractor interactor) {
        super(view, tracker, dataManager);
        this.mRouter = router;
        this.mRunBuildInteractor = runBuildInteractor;
        this.mBuildInteractor = interactor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onViewsCreated() {
        super.onViewsCreated();
        mView.setOnBuildTabsViewListener(this);
    }

    @Override
    public void onViewsDestroyed() {
        super.onViewsDestroyed();
        mInteractor.unsubsribe();
        mRunBuildInteractor.unsubscribe();
        mBuildInteractor.unsubscribe();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        mView.onSave(outState);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        mView.onRestore(savedInstanceState);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onResume() {
        super.onResume();
        mInteractor.setOnBuildTabsEventsListener(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPause() {
        super.onPause();
        mInteractor.setOnBuildTabsEventsListener(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onShow() {
        mView.showRunBuildFloatActionButton();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onHide() {
        mView.hideRunBuildFloatActionButton();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCancelBuildActionTriggered() {
        if (mInteractor.isBuildTriggeredByMe()) {
            showYouAreAboutToCancelBuildDialog();
        } else {
            showYouAreAboutToCancelBuildDialogTriggeredNotByYou();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onShareBuildActionTriggered() {
        String buildWebUrl = mInteractor.getWebUrl();
        mRouter.startShareBuildWebUrlActivity(buildWebUrl);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onRestartBuildActionTriggered() {
        mView.showYouAreAboutToRestartBuildDialog();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onTextCopiedActionTriggered() {
        mView.showTextCopiedSnackBar();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onArtifactTabUnSelect() {
        mInteractor.postOnArtifactTabChangeEvent();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onConfirmCancelingBuild(boolean isReAddToTheQueue) {
        mTracker.trackUserConfirmedCancel(isReAddToTheQueue);
        showProgress();
        mInteractor.cancelBuild(new LoadingListenerWithForbiddenSupport<Build>() {
            @Override
            public void onForbiddenError() {
                mTracker.trackUserGetsForbiddenErrorOnBuildCancel();
                hideProgress();
                showForbiddenToCancelBuildSnackBar();
            }

            @Override
            public void onSuccess(Build build) {
                mTracker.trackUserCanceledBuildSuccessfully();
                hideProgress();
                showBuildIsCancelledSnackBar();
                String buildTypeName = mInteractor.getBuildTypeName();
                mRouter.reopenBuildTabsActivity(build, buildTypeName);
            }

            @Override
            public void onFail(String errorMessage) {
                mTracker.trackUserGetsServerErrorOnBuildCancel();
                hideProgress();
                showBuildIsCancelledErrorSnackBar();
                mInteractor.postRefreshOverViewDataEvent();
            }
        }, isReAddToTheQueue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onConfirmRestartBuild() {
        Properties properties = mInteractor.getBuildDetails().getProperties();
        String branchName = mInteractor.getBuildDetails().getBranchName();
        mView.showRestartingBuildProgressDialog();
        mRunBuildInteractor.queueBuild(branchName, properties, new LoadingListenerWithForbiddenSupport<String>() {
            @Override
            public void onForbiddenError() {
                mTracker.trackUserGetsForbiddenErrorOnBuildRestart();
                mView.hideRestartingBuildProgressDialog();
                mView.showForbiddenToRestartBuildSnackBar();
            }

            @Override
            public void onSuccess(String queuedBuildHref) {
                mQueuedBuildHref = queuedBuildHref;
                mTracker.trackUserRestartedBuildSuccessfully();
                mView.hideRestartingBuildProgressDialog();
                mView.showBuildRestartSuccessSnackBar();
            }

            @Override
            public void onFail(String errorMessage) {
                mTracker.trackUserGetsServerErrorOnBuildRestart();
                mView.hideRestartingBuildProgressDialog();
                mView.showBuildRestartErrorSnackBar();
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onShowQueuedBuild() {
        mView.showBuildLoadingProgress();
        mBuildInteractor.loadBuild(mQueuedBuildHref, new OnLoadingListener<Build>() {
            @Override
            public void onSuccess(Build queuedBuild) {
                mTracker.trackUserWantsToSeeQueuedBuildDetails();
                mView.hideBuildLoadingProgress();
                String buildTypeName = mInteractor.getBuildTypeName();
                mRouter.reopenBuildTabsActivity(queuedBuild, buildTypeName);
            }

            @Override
            public void onFail(String errorMessage) {
                mTracker.trackUserFailedToSeeQueuedBuildDetails();
                mView.hideBuildLoadingProgress();
                mView.showOpeningBuildErrorSnackBar();
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStartBuildListActivityFilteredByBranchEventTriggered(String branchName) {
        String name = mInteractor.getBuildTypeName();
        String id = mInteractor.getBuildDetails().getBuildTypeId();
        BuildListFilter filter = new BuildListFilterImpl();
        filter.setFilter(FilterBuildsView.FILTER_NONE);
        filter.setBranch(branchName);
        mRouter.startBuildListActivity(name, id, filter);
    }

    /**
     * Show forbidden to cancel build snack bar
     */
    private void showForbiddenToCancelBuildSnackBar() {
        if (mInteractor.getBuildDetails().isRunning()) {
            mView.showForbiddenToStopBuildSnackBar();
        } else {
            mView.showForbiddenToRemoveBuildFromQueueSnackBar();
        }
    }

    /**
     * Show build is cancelled snack bar
     */
    private void showBuildIsCancelledSnackBar() {
        if (mInteractor.getBuildDetails().isRunning()) {
            mView.showBuildIsStoppedSnackBar();
        } else {
            mView.showBuildIsRemovedFromQueueSnackBar();
        }
    }

    /**
     * Show build isn't cancelled due an error snack bar
     */
    private void showBuildIsCancelledErrorSnackBar() {
        if (mInteractor.getBuildDetails().isRunning()) {
            mView.showBuildIsStoppedErrorSnackBar();
        } else {
            mView.showBuildIsRemovedFromQueueErrorSnackBar();
        }
    }

    /**
     * Show you are about to cancel build dialog
     */
    private void showYouAreAboutToCancelBuildDialog() {
        if (mInteractor.getBuildDetails().isRunning()) {
            mView.showYouAreAboutToStopBuildDialog();
        } else {
            mView.showYouAreAboutToRemoveBuildFromQueueDialog();
        }
    }

    /**
     * Show you are about to cancel build which wasn't triggered by you dialog
     */
    private void showYouAreAboutToCancelBuildDialogTriggeredNotByYou() {
        if (mInteractor.getBuildDetails().isRunning()) {
            mView.showYouAreAboutToStopNotYoursBuildDialog();
        } else {
            mView.showYouAreAboutToRemoveBuildFromQueueTriggeredNotByYouDialog();
        }
    }

    /**
     * Show stop/removing from queue build progress
     */
    private void showProgress() {
        if (mInteractor.getBuildDetails().isRunning()) {
            mView.showStoppingBuildProgressDialog();
        } else {
            mView.showRemovingBuildFromQueueProgressDialog();
        }
    }

    /**
     * Hide stop/removing from queue build progress
     */
    private void hideProgress() {
        if (mInteractor.getBuildDetails().isRunning()) {
            mView.hideStoppingBuildProgressDialog();
        } else {
            mView.hideRemovingBuildFromQueueProgressDialog();
        }
    }

}
