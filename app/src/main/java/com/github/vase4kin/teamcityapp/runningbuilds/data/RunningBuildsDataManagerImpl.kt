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

package com.github.vase4kin.teamcityapp.runningbuilds.data

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener
import com.github.vase4kin.teamcityapp.api.Repository
import com.github.vase4kin.teamcityapp.buildlist.data.BuildListDataManagerImpl
import com.github.vase4kin.teamcityapp.buildlist.filter.BuildListFilter
import com.github.vase4kin.teamcityapp.buildlist.filter.BuildListFilterImpl
import com.github.vase4kin.teamcityapp.filter_builds.view.FilterBuildsView
import com.github.vase4kin.teamcityapp.overview.data.BuildDetails
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage

/**
 * Impl of [RunningBuildsDataManager]
 */
class RunningBuildsDataManagerImpl(
        repository: Repository,
        storage: SharedUserStorage
) : BuildListDataManagerImpl(repository, storage), RunningBuildsDataManager {

    /**
     * Filter to show only running builds
     */
    private val filter: BuildListFilter

    init {
        // Creating running filter
        filter = BuildListFilterImpl()
        filter.setFilter(FilterBuildsView.FILTER_RUNNING)
    }

    /**
     * {@inheritDoc}
     *
     * TODO: WTF RUNNING BUILDS?
     */
    override fun load(loadingListener: OnLoadingListener<List<BuildDetails>>, update: Boolean) {
        val runningBuilds = getBuildDetailsObservable(repository.listRunningBuilds(filter.toLocator(), null, update))
                .toSortedList { buildDetails, buildDetails2 ->
                    buildDetails.buildTypeId.compareTo(buildDetails2.buildTypeId, ignoreCase = true)
                }
        loadBuildDetailsList(runningBuilds, loadingListener)
    }

    /**
     * {@inheritDoc}
     */
    override fun loadCount(loadingListener: OnLoadingListener<Int>) {
        loadCount(repository.listRunningBuilds(filter.toLocator(), "count", false), loadingListener)
    }
}