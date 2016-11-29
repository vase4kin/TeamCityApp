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

package com.github.vase4kin.teamcityapp.runbuild.dagger;

import com.github.vase4kin.teamcityapp.api.TeamCityService;
import com.github.vase4kin.teamcityapp.runbuild.interactor.RunBuildInteractor;
import com.github.vase4kin.teamcityapp.runbuild.interactor.RunBuildInteractorImpl;
import com.github.vase4kin.teamcityapp.runbuild.router.RunBuildRouter;
import com.github.vase4kin.teamcityapp.runbuild.router.RunBuildRouterImpl;
import com.github.vase4kin.teamcityapp.runbuild.tracker.RunBuildTracker;
import com.github.vase4kin.teamcityapp.runbuild.tracker.RunBuildTrackerImpl;
import com.github.vase4kin.teamcityapp.runbuild.view.RunBuildActivity;
import com.github.vase4kin.teamcityapp.runbuild.view.RunBuildView;
import com.github.vase4kin.teamcityapp.runbuild.view.RunBuildViewImpl;

import dagger.Module;
import dagger.Provides;

@Module
public class RunBuildModule {

    private RunBuildActivity mActivity;

    public RunBuildModule(RunBuildActivity mActivity) {
        this.mActivity = mActivity;
    }

    @Provides
    RunBuildView providesRunBuildView() {
        return new RunBuildViewImpl(mActivity);
    }

    @Provides
    RunBuildInteractor providesRunBuildInteractor(TeamCityService teamCityService) {
        return new RunBuildInteractorImpl(teamCityService, mActivity.getIntent().getStringExtra(RunBuildInteractor.EXTRA_BUILD_TYPE_ID));
    }

    @Provides
    RunBuildRouter providesRunBuildRouter() {
        return new RunBuildRouterImpl(mActivity);
    }

    @Provides
    RunBuildTracker providesRunBuildTracker() {
        return new RunBuildTrackerImpl();
    }

}
