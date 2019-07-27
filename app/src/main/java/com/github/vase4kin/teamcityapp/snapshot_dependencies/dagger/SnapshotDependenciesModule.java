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

package com.github.vase4kin.teamcityapp.snapshot_dependencies.dagger;

import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.api.Repository;
import com.github.vase4kin.teamcityapp.api.TeamCityService;
import com.github.vase4kin.teamcityapp.base.list.view.SimpleSectionedRecyclerViewAdapter;
import com.github.vase4kin.teamcityapp.buildlist.data.BuildInteractor;
import com.github.vase4kin.teamcityapp.buildlist.data.BuildInteractorImpl;
import com.github.vase4kin.teamcityapp.buildlist.view.BuildListAdapter;
import com.github.vase4kin.teamcityapp.filter_bottom_sheet_dialog.FilterProvider;
import com.github.vase4kin.teamcityapp.runningbuilds.view.RunningBuildListView;
import com.github.vase4kin.teamcityapp.runningbuilds.view.RunningBuildsListViewImpl;
import com.github.vase4kin.teamcityapp.snapshot_dependencies.model.SnapshotDependenciesInteractor;
import com.github.vase4kin.teamcityapp.snapshot_dependencies.model.SnapshotDependenciesInteractorImpl;
import com.github.vase4kin.teamcityapp.snapshot_dependencies.model.SnapshotDependenciesValueExtractor;
import com.github.vase4kin.teamcityapp.snapshot_dependencies.model.SnapshotDependenciesValueExtractorImpl;
import com.github.vase4kin.teamcityapp.snapshot_dependencies.router.SnapshotDependenciesRouter;
import com.github.vase4kin.teamcityapp.snapshot_dependencies.router.SnapshotDependenciesRouterImpl;
import com.github.vase4kin.teamcityapp.snapshot_dependencies.tracker.SnapshotDependenciesTracker;
import com.github.vase4kin.teamcityapp.snapshot_dependencies.tracker.SnapshotDependenciesTrackerImpl;
import com.github.vase4kin.teamcityapp.snapshot_dependencies.view.SnapshotDependenciesFragment;
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage;
import com.google.firebase.analytics.FirebaseAnalytics;

import dagger.Module;
import dagger.Provides;

@Module
public class SnapshotDependenciesModule {

    @Provides
    SnapshotDependenciesRouter providesBuildListRouter(SnapshotDependenciesFragment fragment) {
        return new SnapshotDependenciesRouterImpl(fragment.requireActivity());
    }

    @Provides
    SnapshotDependenciesValueExtractor providesBuildListValueExtractor(SnapshotDependenciesFragment fragment) {
        return new SnapshotDependenciesValueExtractorImpl(fragment.getArguments());
    }

    @Provides
    BuildInteractor providesBuildInteractor(TeamCityService teamCityService) {
        return new BuildInteractorImpl(teamCityService);
    }

    @Provides
    SnapshotDependenciesTracker providesFirebaseBuildListTracker(FirebaseAnalytics firebaseAnalytics) {
        return new SnapshotDependenciesTrackerImpl(firebaseAnalytics);
    }

    @Provides
    SnapshotDependenciesInteractor providesBuildListDataManager(Repository repository, SharedUserStorage storage) {
        return new SnapshotDependenciesInteractorImpl(repository, storage);
    }

    @Provides
    RunningBuildListView providesBuildListView(SnapshotDependenciesFragment fragment,
                                               SimpleSectionedRecyclerViewAdapter<BuildListAdapter> adapter) {
        // Add text
        return new RunningBuildsListViewImpl(fragment.getView(), fragment.getActivity(), R.string.empty_list_message_builds, adapter, new FilterProvider()) {
            @Override
            protected int recyclerViewId() {
                return R.id.snapshot_recycler_view;
            }
        };
    }
}
