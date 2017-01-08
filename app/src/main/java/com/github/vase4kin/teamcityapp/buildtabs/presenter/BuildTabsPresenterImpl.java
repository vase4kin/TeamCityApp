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

package com.github.vase4kin.teamcityapp.buildtabs.presenter;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener;
import com.github.vase4kin.teamcityapp.base.tabs.presenter.BaseTabsPresenterImpl;
import com.github.vase4kin.teamcityapp.buildlist.api.Build;
import com.github.vase4kin.teamcityapp.buildlist.data.BuildInteractor;
import com.github.vase4kin.teamcityapp.buildtabs.data.BuildTabsInteractor;
import com.github.vase4kin.teamcityapp.buildtabs.data.OnBuildTabsEventsListener;
import com.github.vase4kin.teamcityapp.buildtabs.router.BuildTabsRouter;
import com.github.vase4kin.teamcityapp.buildtabs.tracker.BuildTabsTracker;
import com.github.vase4kin.teamcityapp.buildtabs.view.BuildTabsView;
import com.github.vase4kin.teamcityapp.buildtabs.view.OnBuildTabsViewListener;
import com.github.vase4kin.teamcityapp.properties.api.Properties;
import com.github.vase4kin.teamcityapp.runbuild.interactor.LoadingListenerWithForbiddenSupport;
import com.github.vase4kin.teamcityapp.runbuild.interactor.RunBuildInteractor;

import javax.inject.Inject;

/**
 * Impl of {@link BuildTabsPresenter}
 */
public class BuildTabsPresenterImpl extends BaseTabsPresenterImpl<BuildTabsView, BuildTabsInteractor, BuildTabsTracker>
        implements BuildTabsPresenter, OnBuildTabsEventsListener, OnBuildTabsViewListener {

    /**
     * Queued build href
     */
    private String mQueuedBuildHref;

    private BuildTabsRouter mRouter;
    private RunBuildInteractor mRunBuildInteractor;
    private BuildInteractor mBuildInteractor;

    @Inject
    BuildTabsPresenterImpl(@NonNull BuildTabsView view,
                           @NonNull BuildTabsTracker tracker,
                           @NonNull BuildTabsInteractor dataManager,
                           @NonNull BuildTabsRouter router,
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
                mRouter.reopenBuildTabsActivity(build);
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
        Properties properties = mInteractor.getBuildProperties();
        String branchName = mInteractor.getBuildBranchName();
        mView.showRestartingBuildProgressDialog();
        mRunBuildInteractor.queueBuild(branchName, properties, new LoadingListenerWithForbiddenSupport<String>() {
            @Override
            public void onForbiddenError() {
                // track
                mView.hideRestartingBuildProgressDialog();
                mView.showForbiddenToRestartBuildSnackBar();
            }

            @Override
            public void onSuccess(String queuedBuildHref) {
                mQueuedBuildHref = queuedBuildHref;
                // track
                mView.hideRestartingBuildProgressDialog();
                mView.showBuildRestartSuccessSnackBar();
            }

            @Override
            public void onFail(String errorMessage) {
                // track
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
                // track
                mView.hideBuildLoadingProgress();
                mRouter.reopenBuildTabsActivity(queuedBuild);
            }

            @Override
            public void onFail(String errorMessage) {
                // track
                mView.hideBuildLoadingProgress();
                mView.showOpeningBuildErrorSnackBar();
            }
        });
    }

    /**
     * Show forbidden to cancel build snack bar
     */
    private void showForbiddenToCancelBuildSnackBar() {
        if (mInteractor.isBuildRunning()) {
            mView.showForbiddenToStopBuildSnackBar();
        } else {
            mView.showForbiddenToRemoveBuildFromQueueSnackBar();
        }
    }

    /**
     * Show build is cancelled snack bar
     */
    private void showBuildIsCancelledSnackBar() {
        if (mInteractor.isBuildRunning()) {
            mView.showBuildIsStoppedSnackBar();
        } else {
            mView.showBuildIsRemovedFromQueueSnackBar();
        }
    }

    /**
     * Show build isn't cancelled due an error snack bar
     */
    private void showBuildIsCancelledErrorSnackBar() {
        if (mInteractor.isBuildRunning()) {
            mView.showBuildIsStoppedErrorSnackBar();
        } else {
            mView.showBuildIsRemovedFromQueueErrorSnackBar();
        }
    }

    /**
     * Show you are about to cancel build dialog
     */
    private void showYouAreAboutToCancelBuildDialog() {
        if (mInteractor.isBuildRunning()) {
            mView.showYouAreAboutToStopBuildDialog();
        } else {
            mView.showYouAreAboutToRemoveBuildFromQueueDialog();
        }
    }

    /**
     * Show you are about to cancel build which wasn't triggered by you dialog
     */
    private void showYouAreAboutToCancelBuildDialogTriggeredNotByYou() {
        if (mInteractor.isBuildRunning()) {
            mView.showYouAreAboutToStopNotYoursBuildDialog();
        } else {
            mView.showYouAreAboutToRemoveBuildFromQueueTriggeredNotByYouDialog();
        }
    }

    /**
     * Show stop/removing from queue build progress
     */
    private void showProgress() {
        if (mInteractor.isBuildRunning()) {
            mView.showStoppingBuildProgressDialog();
        } else {
            mView.showRemovingBuildFromQueueProgressDialog();
        }
    }

    /**
     * Hide stop/removing from queue build progress
     */
    private void hideProgress() {
        if (mInteractor.isBuildRunning()) {
            mView.hideStoppingBuildProgressDialog();
        } else {
            mView.hideRemovingBuildFromQueueProgressDialog();
        }
    }

}
