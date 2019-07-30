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

package com.github.vase4kin.teamcityapp.snapshot_dependencies.presenter;

import androidx.annotation.NonNull;

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener;
import com.github.vase4kin.teamcityapp.base.list.presenter.BaseListPresenterImpl;
import com.github.vase4kin.teamcityapp.buildlist.api.Build;
import com.github.vase4kin.teamcityapp.buildlist.data.BuildListDataManager;
import com.github.vase4kin.teamcityapp.buildlist.data.BuildListDataModel;
import com.github.vase4kin.teamcityapp.buildlist.data.BuildListDataModelImpl;
import com.github.vase4kin.teamcityapp.buildlist.data.OnBuildListPresenterListener;
import com.github.vase4kin.teamcityapp.overview.data.BuildDetails;
import com.github.vase4kin.teamcityapp.runningbuilds.view.RunningBuildListView;
import com.github.vase4kin.teamcityapp.snapshot_dependencies.model.SnapshotDependenciesInteractor;
import com.github.vase4kin.teamcityapp.snapshot_dependencies.model.SnapshotDependenciesValueExtractor;
import com.github.vase4kin.teamcityapp.snapshot_dependencies.router.SnapshotDependenciesRouter;
import com.github.vase4kin.teamcityapp.snapshot_dependencies.tracker.SnapshotDependenciesTracker;

import java.util.List;

import javax.inject.Inject;

public class SnapshotDependenciesPresenterImpl extends BaseListPresenterImpl<
        BuildListDataModel,
        BuildDetails,
        RunningBuildListView,
        BuildListDataManager,
        SnapshotDependenciesTracker,
        SnapshotDependenciesValueExtractor> implements OnBuildListPresenterListener {

    private SnapshotDependenciesRouter mRouter;

    @Inject
    SnapshotDependenciesPresenterImpl(@NonNull RunningBuildListView view,
                                      @NonNull SnapshotDependenciesInteractor dataManager,
                                      @NonNull SnapshotDependenciesTracker tracker,
                                      @NonNull SnapshotDependenciesValueExtractor valueExtractor,
                                      @NonNull SnapshotDependenciesRouter mRouter) {
        super(view, dataManager, tracker, valueExtractor);
        this.mRouter = mRouter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadData(@NonNull OnLoadingListener<List<BuildDetails>> loadingListener, boolean update) {
        String buildId = valueExtractor.getId();
        dataManager.load(buildId, loadingListener, update);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initViews() {
        super.initViews();
        view.setOnBuildListPresenterListener(this);
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

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onShowQueuedBuildSnackBarClick() {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onNavigateToFavorites() {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onFilterBuildsOptionMenuClick() {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onAddToFavoritesOptionMenuClick() {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onResetFiltersSnackBarActionClick() {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onLoadMore() {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isLoading() {
        return false;
    }

    @Override
    public boolean hasLoadedAllItems() {
        return false;
    }
}
