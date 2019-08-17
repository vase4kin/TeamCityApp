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

package com.github.vase4kin.teamcityapp.snapshot_dependencies.dagger

import android.os.Bundle
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.api.Repository
import com.github.vase4kin.teamcityapp.api.TeamCityService
import com.github.vase4kin.teamcityapp.base.list.view.SimpleSectionedRecyclerViewAdapter
import com.github.vase4kin.teamcityapp.buildlist.data.BuildInteractor
import com.github.vase4kin.teamcityapp.buildlist.data.BuildInteractorImpl
import com.github.vase4kin.teamcityapp.buildlist.view.BuildListAdapter
import com.github.vase4kin.teamcityapp.filter_bottom_sheet_dialog.filter.FilterProvider
import com.github.vase4kin.teamcityapp.runningbuilds.view.RunningBuildListView
import com.github.vase4kin.teamcityapp.runningbuilds.view.RunningBuildsListViewImpl
import com.github.vase4kin.teamcityapp.snapshot_dependencies.model.SnapshotDependenciesInteractor
import com.github.vase4kin.teamcityapp.snapshot_dependencies.model.SnapshotDependenciesInteractorImpl
import com.github.vase4kin.teamcityapp.snapshot_dependencies.model.SnapshotDependenciesValueExtractor
import com.github.vase4kin.teamcityapp.snapshot_dependencies.model.SnapshotDependenciesValueExtractorImpl
import com.github.vase4kin.teamcityapp.snapshot_dependencies.router.SnapshotDependenciesRouter
import com.github.vase4kin.teamcityapp.snapshot_dependencies.router.SnapshotDependenciesRouterImpl
import com.github.vase4kin.teamcityapp.snapshot_dependencies.tracker.SnapshotDependenciesTracker
import com.github.vase4kin.teamcityapp.snapshot_dependencies.tracker.SnapshotDependenciesTrackerImpl
import com.github.vase4kin.teamcityapp.snapshot_dependencies.view.SnapshotDependenciesFragment
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage
import com.google.firebase.analytics.FirebaseAnalytics

import dagger.Module
import dagger.Provides

@Module
class SnapshotDependenciesModule {

    @Provides
    fun providesBuildListRouter(fragment: SnapshotDependenciesFragment): SnapshotDependenciesRouter {
        return SnapshotDependenciesRouterImpl(fragment.requireActivity())
    }

    @Provides
    fun providesBuildListValueExtractor(fragment: SnapshotDependenciesFragment): SnapshotDependenciesValueExtractor {
        return SnapshotDependenciesValueExtractorImpl(fragment.arguments ?: Bundle.EMPTY)
    }

    @Provides
    fun providesBuildInteractor(teamCityService: TeamCityService): BuildInteractor {
        return BuildInteractorImpl(teamCityService)
    }

    @Provides
    fun providesFirebaseBuildListTracker(firebaseAnalytics: FirebaseAnalytics): SnapshotDependenciesTracker {
        return SnapshotDependenciesTrackerImpl(firebaseAnalytics)
    }

    @Provides
    fun providesBuildListDataManager(repository: Repository, storage: SharedUserStorage): SnapshotDependenciesInteractor {
        return SnapshotDependenciesInteractorImpl(repository, storage)
    }

    @Provides
    fun providesBuildListView(fragment: SnapshotDependenciesFragment,
                              adapter: SimpleSectionedRecyclerViewAdapter<BuildListAdapter>): RunningBuildListView {
        // Add text
        return object : RunningBuildsListViewImpl(fragment.view, fragment.activity, R.string.empty_list_message_builds, adapter, FilterProvider()) {
            override fun recyclerViewId(): Int {
                return R.id.snapshot_recycler_view
            }
        }
    }
}
