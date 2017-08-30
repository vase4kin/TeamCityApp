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

package com.github.vase4kin.teamcityapp.buildlist.presenter;

import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener;
import com.github.vase4kin.teamcityapp.base.list.extractor.BaseValueExtractor;
import com.github.vase4kin.teamcityapp.base.list.presenter.BaseListPresenterImpl;
import com.github.vase4kin.teamcityapp.buildlist.api.Build;
import com.github.vase4kin.teamcityapp.buildlist.data.BuildInteractor;
import com.github.vase4kin.teamcityapp.buildlist.data.BuildListDataManager;
import com.github.vase4kin.teamcityapp.buildlist.data.BuildListDataModel;
import com.github.vase4kin.teamcityapp.buildlist.data.BuildListDataModelImpl;
import com.github.vase4kin.teamcityapp.buildlist.data.OnBuildListPresenterListener;
import com.github.vase4kin.teamcityapp.buildlist.filter.BuildListFilter;
import com.github.vase4kin.teamcityapp.buildlist.router.BuildListRouter;
import com.github.vase4kin.teamcityapp.buildlist.tracker.BuildListTracker;
import com.github.vase4kin.teamcityapp.buildlist.view.BuildListView;
import com.github.vase4kin.teamcityapp.onboarding.OnboardingManager;
import com.github.vase4kin.teamcityapp.overview.data.BuildDetails;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

public class BuildListPresenterImpl<V extends BuildListView, DM extends BuildListDataManager> extends BaseListPresenterImpl<
        BuildListDataModel,
        BuildDetails,
        V,
        DM,
        BuildListTracker,
        BaseValueExtractor> implements BuildListPresenter, OnBuildListPresenterListener {

    private BuildListRouter mRouter;
    private BuildInteractor mBuildInteractor;
    private OnboardingManager onboardingManager;
    @VisibleForTesting
    boolean mIsLoadMoreLoading = false;
    /**
     * Saved local queued build href
     */
    @VisibleForTesting
    String mQueuedBuildHref;

    @Inject
    public BuildListPresenterImpl(@NonNull V view,
                                  @NonNull DM dataManager,
                                  @NonNull BuildListTracker tracker,
                                  @NonNull BaseValueExtractor valueExtractor,
                                  @NonNull BuildListRouter router,
                                  @NonNull BuildInteractor interactor,
                                  @NonNull OnboardingManager onboardingManager) {
        super(view, dataManager, tracker, valueExtractor);
        this.mRouter = router;
        this.mBuildInteractor = interactor;
        this.onboardingManager = onboardingManager;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadData(@NonNull OnLoadingListener<List<BuildDetails>> loadingListener, boolean update) {
        String buildTypeId = mValueExtractor.getId();
        BuildListFilter filter = mValueExtractor.getBuildListFilter();
        if (filter != null) {
            mDataManager.load(buildTypeId, filter, loadingListener, update);
        } else {
            mDataManager.load(buildTypeId, loadingListener, update);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initViews() {
        super.initViews();
        if (!mValueExtractor.isBundleNull()) {
            mView.setTitle(mValueExtractor.getName());
        }
        mView.setOnBuildListPresenterListener(this);
        mView.showRunBuildFloatActionButton();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mView.isBuildListOpen() && !onboardingManager.isRunBuildPromptShown()) {
            mView.showRunBuildPrompt(new OnboardingManager.OnPromptShownListener() {
                @Override
                public void onPromptShown() {
                    onboardingManager.saveRunBuildPromptShown();
                    showFilterBuildsPrompt();
                }
            });
        }
    }

    /**
     * Show filter builds prompt
     */
    private void showFilterBuildsPrompt() {
        if (!onboardingManager.isFilterBuildsPromptShown()) {
            mView.showFilterBuildsPrompt(new OnboardingManager.OnPromptShownListener() {
                @Override
                public void onPromptShown() {
                    onboardingManager.saveFilterBuildsPromptShown();
                }
            });
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onBuildClick(Build build) {
        if (mValueExtractor.isBundleNull()) {
            mRouter.openBuildPage(build, null);
        } else {
            String buildTypeName = mValueExtractor.getName();
            mRouter.openBuildPage(build, buildTypeName);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onRunBuildFabClick() {
        mRouter.openRunBuildPage(mValueExtractor.getId());
        mTracker.trackRunBuildButtonPressed();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onShowQueuedBuildSnackBarClick() {
        mTracker.trackUserWantsToSeeQueuedBuildDetails();
        mView.showBuildLoadingProgress();
        mBuildInteractor.loadBuild(mQueuedBuildHref, new OnLoadingListener<Build>() {
            @Override
            public void onSuccess(Build data) {
                mView.hideBuildLoadingProgress();
                String buildTypeName = mValueExtractor.getName();
                mRouter.openBuildPage(data, buildTypeName);
            }

            @Override
            public void onFail(String errorMessage) {
                mView.hideBuildLoadingProgress();
                mView.showOpeningBuildErrorSnackBar();
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onFilterBuildsOptionMenuClick() {
        mRouter.openFilterBuildsPage(mValueExtractor.getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onResetFiltersSnackBarActionClick() {
        mView.disableSwipeToRefresh();
        mView.showRefreshAnimation();
        mView.hideErrorView();
        mView.hideEmpty();
        mView.showData(new BuildListDataModelImpl(Collections.<BuildDetails>emptyList()));
        mDataManager.load(mValueExtractor.getId(), loadingListener, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onLoadMore() {
        mIsLoadMoreLoading = true;
        mView.addLoadMore();
        mDataManager.loadMore(new OnLoadingListener<List<BuildDetails>>() {
            @Override
            public void onSuccess(List<BuildDetails> data) {
                mView.removeLoadMore();
                mView.addMoreBuilds(new BuildListDataModelImpl(data));
                mIsLoadMoreLoading = false;
            }

            @Override
            public void onFail(String errorMessage) {
                mView.removeLoadMore();
                mView.showRetryLoadMoreSnackBar();
                mIsLoadMoreLoading = false;
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isLoading() {
        return mIsLoadMoreLoading;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasLoadedAllItems() {
        return !mDataManager.canLoadMore();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BuildListDataModel createModel(List<BuildDetails> data) {
        return new BuildListDataModelImpl(data);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onSuccessCallBack(List<BuildDetails> data) {
        super.onSuccessCallBack(data);
        mView.showRunBuildFloatActionButton();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onFailCallBack(String errorMessage) {
        super.onFailCallBack(errorMessage);
        mView.hideRunBuildFloatActionButton();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onRunBuildActivityResult(String queuedBuildHref) {
        this.mQueuedBuildHref = queuedBuildHref;
        mView.showBuildQueuedSuccessSnackBar();
        mView.showRefreshAnimation();
        onSwipeToRefresh();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onFilterBuildsActivityResult(BuildListFilter filter) {
        mView.showBuildFilterAppliedSnackBar();
        mView.disableSwipeToRefresh();
        mView.showRefreshAnimation();
        mView.hideErrorView();
        mView.hideEmpty();
        mView.showData(new BuildListDataModelImpl(Collections.<BuildDetails>emptyList()));
        mDataManager.load(mValueExtractor.getId(), filter, loadingListener, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onViewsDestroyed() {
        super.onViewsDestroyed();
        mBuildInteractor.unsubscribe();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        mView.createOptionsMenu(menu, inflater);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPrepareOptionsMenu(Menu menu) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return mView.onOptionsItemSelected(item);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onSwipeToRefresh() {
        super.onSwipeToRefresh();
        mView.hideFiltersAppliedSnackBar();
    }
}
