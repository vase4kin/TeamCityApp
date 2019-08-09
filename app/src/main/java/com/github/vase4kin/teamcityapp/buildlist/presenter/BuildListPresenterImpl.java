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

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

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
        String buildTypeId = valueExtractor.getId();
        BuildListFilter filter = valueExtractor.getBuildListFilter();
        if (filter != null) {
            dataManager.load(buildTypeId, filter, loadingListener, update);
        } else {
            dataManager.load(buildTypeId, loadingListener, update);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initViews() {
        super.initViews();
        if (!valueExtractor.isBundleNullOrEmpty()) {
            view.setTitle(valueExtractor.getName());
        }
        view.setOnBuildListPresenterListener(this);
        view.showRunBuildFloatActionButton();
    }

    @Override
    public void onResume() {
        super.onResume();
        showRunBuildPrompt();
    }

    /**
     * Show run build prompt
     */
    private void showRunBuildPrompt() {
        if (view.isBuildListOpen() && !onboardingManager.isRunBuildPromptShown()) {
            view.showRunBuildPrompt(() -> {
                onboardingManager.saveRunBuildPromptShown();
                showFilterBuildsPrompt();
            });
        }
    }

    /**
     * Show filter builds prompt
     */
    private void showFilterBuildsPrompt() {
        if (!onboardingManager.isFilterBuildsPromptShown()) {
            view.showFilterBuildsPrompt(() -> {
                onboardingManager.saveFilterBuildsPromptShown();
                showFavPrompt();
            });
        }
    }

    /**
     * Show fav prompt
     */
    private void showFavPrompt() {
        if (!onboardingManager.isFavPromptShown()) {
            view.showFavPrompt(() -> onboardingManager.saveFavPromptShown());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onBuildClick(Build build) {
        if (valueExtractor.isBundleNullOrEmpty()) {
            mRouter.openBuildPage(build, null);
        } else {
            String buildTypeName = valueExtractor.getName();
            mRouter.openBuildPage(build, buildTypeName);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onRunBuildFabClick() {
        mRouter.openRunBuildPage(valueExtractor.getId());
        tracker.trackRunBuildButtonPressed();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onShowQueuedBuildSnackBarClick() {
        tracker.trackUserWantsToSeeQueuedBuildDetails();
        view.showBuildLoadingProgress();
        mBuildInteractor.loadBuild(mQueuedBuildHref, new OnLoadingListener<Build>() {
            @Override
            public void onSuccess(Build data) {
                view.hideBuildLoadingProgress();
                String buildTypeName = valueExtractor.getName();
                mRouter.openBuildPage(data, buildTypeName);
            }

            @Override
            public void onFail(String errorMessage) {
                view.hideBuildLoadingProgress();
                view.showOpeningBuildErrorSnackBar();
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onNavigateToFavorites() {
        mRouter.openFavorites();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onFilterBuildsOptionMenuClick() {
        mRouter.openFilterBuildsPage(valueExtractor.getId());
    }

    @Override
    public void onAddToFavoritesOptionMenuClick() {
        String buildTypeId = valueExtractor.getId();
        if (dataManager.hasBuildTypeAsFavorite(buildTypeId)) {
            dataManager.removeFromFavorites(buildTypeId);
            view.showRemoveFavoritesSnackBar();
        } else {
            dataManager.addToFavorites(valueExtractor.getId());
            view.showAddToFavoritesSnackBar();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onResetFiltersSnackBarActionClick() {
        view.disableSwipeToRefresh();
        view.showRefreshAnimation();
        view.hideErrorView();
        view.hideEmpty();
        view.showData(new BuildListDataModelImpl(Collections.<BuildDetails>emptyList()));
        dataManager.load(valueExtractor.getId(), loadingListener, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onLoadMore() {
        mIsLoadMoreLoading = true;
        view.addLoadMore();
        dataManager.loadMore(new OnLoadingListener<List<BuildDetails>>() {
            @Override
            public void onSuccess(List<BuildDetails> data) {
                view.removeLoadMore();
                view.addMoreBuilds(new BuildListDataModelImpl(data));
                mIsLoadMoreLoading = false;
            }

            @Override
            public void onFail(String errorMessage) {
                view.removeLoadMore();
                view.showRetryLoadMoreSnackBar();
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
        return !dataManager.canLoadMore();
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
        view.showRunBuildFloatActionButton();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onFailCallBack(String errorMessage) {
        super.onFailCallBack(errorMessage);
        view.hideRunBuildFloatActionButton();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onRunBuildActivityResult(String queuedBuildHref) {
        this.mQueuedBuildHref = queuedBuildHref;
        view.showBuildQueuedSuccessSnackBar();
        view.showRefreshAnimation();
        onSwipeToRefresh();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onFilterBuildsActivityResult(BuildListFilter filter) {
        view.showBuildFilterAppliedSnackBar();
        view.disableSwipeToRefresh();
        view.showRefreshAnimation();
        view.hideErrorView();
        view.hideEmpty();
        view.showData(new BuildListDataModelImpl(Collections.<BuildDetails>emptyList()));
        dataManager.load(valueExtractor.getId(), filter, loadingListener, true);
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
        String buildTypeId = valueExtractor.getId();
        if (dataManager.hasBuildTypeAsFavorite(buildTypeId)) {
            view.createFavOptionsMenu(menu, inflater);
        } else {
            view.createNotFavOptionsMenu(menu, inflater);
        }
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
        return view.onOptionsItemSelected(item);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onSwipeToRefresh() {
        super.onSwipeToRefresh();
        view.hideFiltersAppliedSnackBar();
    }
}
