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

package com.github.vase4kin.teamcityapp.filter_builds.dagger;

import com.github.vase4kin.teamcityapp.api.Repository;
import com.github.vase4kin.teamcityapp.filter_builds.router.FilterBuildsRouter;
import com.github.vase4kin.teamcityapp.filter_builds.router.FilterBuildsRouterImpl;
import com.github.vase4kin.teamcityapp.filter_builds.tracker.FilterBuildsTracker;
import com.github.vase4kin.teamcityapp.filter_builds.tracker.FilterBuildsTrackerImpl;
import com.github.vase4kin.teamcityapp.filter_builds.view.FilterBuildsActivity;
import com.github.vase4kin.teamcityapp.filter_builds.view.FilterBuildsView;
import com.github.vase4kin.teamcityapp.filter_builds.view.FilterBuildsViewImpl;
import com.github.vase4kin.teamcityapp.runbuild.interactor.BranchesInteractor;
import com.github.vase4kin.teamcityapp.runbuild.interactor.BranchesInteractorImpl;
import com.github.vase4kin.teamcityapp.runbuild.interactor.RunBuildInteractor;
import com.github.vase4kin.teamcityapp.runbuild.view.BranchesComponentView;
import com.github.vase4kin.teamcityapp.runbuild.view.BranchesComponentViewImpl;

import dagger.Module;
import dagger.Provides;

@Module
public class FilterBuildsModule {

    private FilterBuildsActivity mActivity;

    public FilterBuildsModule(FilterBuildsActivity activity) {
        this.mActivity = activity;
    }

    @Provides
    FilterBuildsView providesFilterBuildsView() {
        return new FilterBuildsViewImpl(mActivity);
    }

    @Provides
    BranchesComponentView providesBranchesComponentView() {
        return new BranchesComponentViewImpl(mActivity);
    }

    @Provides
    BranchesInteractor providesBranchesInteractor(Repository repository) {
        return new BranchesInteractorImpl(repository, mActivity.getIntent().getStringExtra(RunBuildInteractor.EXTRA_BUILD_TYPE_ID));
    }

    @Provides
    FilterBuildsRouter providesFilterBuildsRouter() {
        return new FilterBuildsRouterImpl(mActivity);
    }

    @Provides
    FilterBuildsTracker providesFilterBuildsTracker() {
        return new FilterBuildsTrackerImpl();
    }

}
