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

package com.github.vase4kin.teamcityapp.buildlist.dagger;

import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.api.Repository;
import com.github.vase4kin.teamcityapp.api.TeamCityService;
import com.github.vase4kin.teamcityapp.base.list.extractor.BaseValueExtractor;
import com.github.vase4kin.teamcityapp.base.list.extractor.BaseValueExtractorImpl;
import com.github.vase4kin.teamcityapp.base.list.view.SimpleSectionedRecyclerViewAdapter;
import com.github.vase4kin.teamcityapp.buildlist.data.BuildInteractor;
import com.github.vase4kin.teamcityapp.buildlist.data.BuildInteractorImpl;
import com.github.vase4kin.teamcityapp.buildlist.data.BuildListDataManager;
import com.github.vase4kin.teamcityapp.buildlist.data.BuildListDataManagerImpl;
import com.github.vase4kin.teamcityapp.buildlist.router.BuildListRouter;
import com.github.vase4kin.teamcityapp.buildlist.router.BuildListRouterImpl;
import com.github.vase4kin.teamcityapp.buildlist.tracker.BuildListTracker;
import com.github.vase4kin.teamcityapp.buildlist.tracker.BuildListTrackerImpl;
import com.github.vase4kin.teamcityapp.buildlist.tracker.FabricBuildListTrackerImpl;
import com.github.vase4kin.teamcityapp.buildlist.tracker.FirebaseBuildListTrackerImpl;
import com.github.vase4kin.teamcityapp.buildlist.view.BuildListAdapter;
import com.github.vase4kin.teamcityapp.buildlist.view.BuildListView;
import com.github.vase4kin.teamcityapp.buildlist.view.BuildListViewImpl;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.Set;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoSet;

@Module
public class BuildListModule {

    private View mView;
    private AppCompatActivity mActivity;

    public BuildListModule(View mView, AppCompatActivity mActivity) {
        this.mView = mView;
        this.mActivity = mActivity;
    }

    @Provides
    BuildListDataManager providesBuildListDataManager(Repository repository) {
        return new BuildListDataManagerImpl(repository);
    }

    @Provides
    BuildListView providesBuildListView(SimpleSectionedRecyclerViewAdapter<BuildListAdapter> adapter) {
        return new BuildListViewImpl(mView, mActivity, R.string.empty_list_message_builds, adapter);
    }

    @Provides
    BuildListRouter providesBuildListRouter() {
        return new BuildListRouterImpl(mActivity);
    }

    @Provides
    BaseValueExtractor providesBuildListValueExtractor() {
        return new BaseValueExtractorImpl(mActivity.getIntent().getExtras());
    }

    @Provides
    BuildInteractor providesBuildInteractor(TeamCityService teamCityService) {
        return new BuildInteractorImpl(teamCityService);
    }

    @IntoSet
    @Provides
    BuildListTracker providesFabricBuildListTracker() {
        return new FabricBuildListTrackerImpl(BuildListTracker.SCREEN_NAME_BUILD_LIST);
    }

    @IntoSet
    @Provides
    BuildListTracker providesFirebaseBuildListTracker(FirebaseAnalytics firebaseAnalytics) {
        return new FirebaseBuildListTrackerImpl(firebaseAnalytics, BuildListTracker.SCREEN_NAME_BUILD_LIST);
    }

    @Provides
    BuildListTracker providesBuildListTracker(Set<BuildListTracker> trackers) {
        return new BuildListTrackerImpl(trackers);
    }

}
