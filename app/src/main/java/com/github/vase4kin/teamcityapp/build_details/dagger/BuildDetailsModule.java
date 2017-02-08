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

package com.github.vase4kin.teamcityapp.build_details.dagger;

import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.github.vase4kin.teamcityapp.api.TeamCityService;
import com.github.vase4kin.teamcityapp.base.list.extractor.BaseValueExtractor;
import com.github.vase4kin.teamcityapp.base.list.extractor.BaseValueExtractorImpl;
import com.github.vase4kin.teamcityapp.build_details.data.BuildDetailsInteractor;
import com.github.vase4kin.teamcityapp.build_details.data.BuildDetailsInteractorImpl;
import com.github.vase4kin.teamcityapp.build_details.router.BuildDetailsRouter;
import com.github.vase4kin.teamcityapp.build_details.router.BuildDetailsRouterImpl;
import com.github.vase4kin.teamcityapp.build_details.tracker.BuildDetailsTracker;
import com.github.vase4kin.teamcityapp.build_details.tracker.BuildDetailsViewTrackerImpl;
import com.github.vase4kin.teamcityapp.build_details.view.BuildDetailsView;
import com.github.vase4kin.teamcityapp.build_details.view.BuildDetailsViewImpl;
import com.github.vase4kin.teamcityapp.buildlist.data.BuildInteractor;
import com.github.vase4kin.teamcityapp.buildlist.data.BuildInteractorImpl;
import com.github.vase4kin.teamcityapp.runbuild.interactor.RunBuildInteractor;
import com.github.vase4kin.teamcityapp.runbuild.interactor.RunBuildInteractorImpl;
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage;
import com.github.vase4kin.teamcityapp.utils.StatusBarUtils;

import dagger.Module;
import dagger.Provides;
import de.greenrobot.event.EventBus;

@Module
public class BuildDetailsModule {

    private View mView;
    private AppCompatActivity mActivity;

    public BuildDetailsModule(View mView, AppCompatActivity mActivity) {
        this.mView = mView;
        this.mActivity = mActivity;
    }

    @Provides
    BuildDetailsView providesBuildTabsView(StatusBarUtils statusBarUtils, BaseValueExtractor valueExtractor) {
        return new BuildDetailsViewImpl(mView, mActivity, statusBarUtils, valueExtractor);
    }

    @Provides
    BuildDetailsTracker providesViewTracker() {
        return new BuildDetailsViewTrackerImpl();
    }

    @Provides
    BaseValueExtractor providesBuildTabsValueExtractor() {
        return new BaseValueExtractorImpl(mActivity.getIntent().getExtras());
    }

    @Provides
    StatusBarUtils providesStatusBarUtils() {
        return new StatusBarUtils();
    }

    @Provides
    BuildDetailsInteractor providesBaseTabsDataManager(EventBus eventBus,
                                                       BaseValueExtractor valueExtractor,
                                                       SharedUserStorage sharedUserStorage, TeamCityService teamCityService) {
        return new BuildDetailsInteractorImpl(eventBus, valueExtractor, sharedUserStorage, teamCityService);
    }

    @Provides
    BuildDetailsRouter providesBuildTabsRouter() {
        return new BuildDetailsRouterImpl(mActivity);
    }

    @Provides
    RunBuildInteractor providesRunBuildInteractor(TeamCityService teamCityService, BaseValueExtractor valueExtractor) {
        return new RunBuildInteractorImpl(teamCityService, valueExtractor.getBuildDetails().getBuildTypeId());
    }

    @Provides
    BuildInteractor providesBuildInteractor(TeamCityService teamCityService) {
        return new BuildInteractorImpl(teamCityService);
    }
}
