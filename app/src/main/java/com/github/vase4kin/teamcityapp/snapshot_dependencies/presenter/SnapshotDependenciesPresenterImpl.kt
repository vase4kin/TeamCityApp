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

package com.github.vase4kin.teamcityapp.snapshot_dependencies.presenter

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener
import com.github.vase4kin.teamcityapp.base.list.presenter.BaseListPresenterImpl
import com.github.vase4kin.teamcityapp.buildlist.api.Build
import com.github.vase4kin.teamcityapp.buildlist.data.BuildListDataManager
import com.github.vase4kin.teamcityapp.buildlist.data.BuildListDataModel
import com.github.vase4kin.teamcityapp.buildlist.data.BuildListDataModelImpl
import com.github.vase4kin.teamcityapp.buildlist.data.OnBuildListPresenterListener
import com.github.vase4kin.teamcityapp.overview.data.BuildDetails
import com.github.vase4kin.teamcityapp.runningbuilds.view.RunningBuildListView
import com.github.vase4kin.teamcityapp.snapshot_dependencies.model.SnapshotDependenciesInteractor
import com.github.vase4kin.teamcityapp.snapshot_dependencies.model.SnapshotDependenciesValueExtractor
import com.github.vase4kin.teamcityapp.snapshot_dependencies.router.SnapshotDependenciesRouter
import com.github.vase4kin.teamcityapp.snapshot_dependencies.tracker.SnapshotDependenciesTracker
import javax.inject.Inject

class SnapshotDependenciesPresenterImpl @Inject constructor(
    view: RunningBuildListView,
    dataManager: SnapshotDependenciesInteractor,
    tracker: SnapshotDependenciesTracker,
    valueExtractor: SnapshotDependenciesValueExtractor,
    private val router: SnapshotDependenciesRouter
) : BaseListPresenterImpl<BuildListDataModel, BuildDetails, RunningBuildListView, BuildListDataManager, SnapshotDependenciesTracker, SnapshotDependenciesValueExtractor>(
    view,
    dataManager,
    tracker,
    valueExtractor
), OnBuildListPresenterListener {

    /**
     * {@inheritDoc}
     */
    override fun loadData(loadingListener: OnLoadingListener<List<BuildDetails>>, update: Boolean) {
        val buildId = valueExtractor.id
        dataManager.load(buildId, loadingListener, update)
    }

    /**
     * {@inheritDoc}
     */
    override fun initViews() {
        super.initViews()
        view.setOnBuildListPresenterListener(this)
    }

    /**
     * {@inheritDoc}
     */
    override fun createModel(data: MutableList<BuildDetails>): BuildListDataModel {
        return BuildListDataModelImpl(data)
    }

    /**
     * {@inheritDoc}
     */
    override fun onBuildClick(build: Build) {
        if (valueExtractor.isBundleNullOrEmpty) {
            router.openBuildPage(build, null)
        } else {
            val buildTypeName = valueExtractor.name
            router.openBuildPage(build, buildTypeName)
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun onRunBuildFabClick() {
    }

    /**
     * {@inheritDoc}
     */
    override fun onShowQueuedBuildSnackBarClick() {
    }

    /**
     * {@inheritDoc}
     */
    override fun onNavigateToFavorites() {
    }

    /**
     * {@inheritDoc}
     */
    override fun onFilterBuildsOptionMenuClick() {
    }

    /**
     * {@inheritDoc}
     */
    override fun onAddToFavoritesOptionMenuClick() {
    }

    /**
     * {@inheritDoc}
     */
    override fun onResetFiltersSnackBarActionClick() {
    }

    /**
     * {@inheritDoc}
     */
    override fun onLoadMore() {
    }

    /**
     * {@inheritDoc}
     */
    override fun isLoading(): Boolean {
        return false
    }

    override fun hasLoadedAllItems(): Boolean {
        return false
    }
}
