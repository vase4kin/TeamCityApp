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

package com.github.vase4kin.teamcityapp.queue.presenter;

import androidx.annotation.NonNull;

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener;
import com.github.vase4kin.teamcityapp.base.list.extractor.BaseValueExtractor;
import com.github.vase4kin.teamcityapp.buildlist.data.BuildInteractor;
import com.github.vase4kin.teamcityapp.buildlist.presenter.BuildListPresenterImpl;
import com.github.vase4kin.teamcityapp.buildlist.router.BuildListRouter;
import com.github.vase4kin.teamcityapp.buildlist.tracker.BuildListTracker;
import com.github.vase4kin.teamcityapp.filter_bottom_sheet_dialog.Filter;
import com.github.vase4kin.teamcityapp.filter_bottom_sheet_dialog.FilterProvider;
import com.github.vase4kin.teamcityapp.onboarding.OnboardingManager;
import com.github.vase4kin.teamcityapp.overview.data.BuildDetails;
import com.github.vase4kin.teamcityapp.runningbuilds.data.RunningBuildsDataManager;
import com.github.vase4kin.teamcityapp.runningbuilds.view.RunningBuildListView;

import java.util.List;

import javax.inject.Inject;

/**
 * Presenter to handle {@link com.github.vase4kin.teamcityapp.runningbuilds.view.RunningBuildsFragment}
 */
public class QueueBuildsListPresenterImpl extends BuildListPresenterImpl<RunningBuildListView, RunningBuildsDataManager> {

    @NonNull
    private final FilterProvider filterProvider;

    @Inject
    QueueBuildsListPresenterImpl(@NonNull RunningBuildListView view,
                                 @NonNull RunningBuildsDataManager dataManager,
                                 @NonNull BuildListTracker tracker,
                                 @NonNull BuildListRouter router,
                                 @NonNull BaseValueExtractor valueExtractor,
                                 @NonNull BuildInteractor buildInteractor,
                                 @NonNull OnboardingManager onboardingManager,
                                 @NonNull FilterProvider filterProvider) {
        super(view, dataManager, tracker, valueExtractor, router, buildInteractor, onboardingManager);
        this.filterProvider = filterProvider;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadData(@NonNull OnLoadingListener<List<BuildDetails>> loadingListener, boolean update) {
        Filter currentFilter = filterProvider.getQueuedBuildsFilter();
        if (currentFilter == Filter.QUEUE_FAVORITES) {
            mDataManager.loadFavorites(loadingListener, update);
        } else if (currentFilter == Filter.QUEUE_ALL) {
            mDataManager.load(loadingListener, update);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onResume() {
        super.onResume();
        mView.showRefreshAnimation();
        loadData(loadingListener, false);
    }

    /**
     * On pause activity callback
     */
    public void onPause() {
        mView.hideRefreshAnimation();
        mDataManager.unsubscribe();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadDataOnViewsCreated() {
        // Don't load data when view is created, only on resume
    }
}