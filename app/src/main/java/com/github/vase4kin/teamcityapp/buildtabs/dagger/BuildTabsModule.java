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

package com.github.vase4kin.teamcityapp.buildtabs.dagger;

import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.github.vase4kin.teamcityapp.api.TeamCityService;
import com.github.vase4kin.teamcityapp.base.list.extractor.BaseValueExtractor;
import com.github.vase4kin.teamcityapp.base.list.extractor.BaseValueExtractorImpl;
import com.github.vase4kin.teamcityapp.buildtabs.data.BuildTabsInteractor;
import com.github.vase4kin.teamcityapp.buildtabs.data.BuildTabsInteractorImpl;
import com.github.vase4kin.teamcityapp.buildtabs.tracker.BuildsTabViewTrackerImpl;
import com.github.vase4kin.teamcityapp.buildtabs.view.BuildTabsView;
import com.github.vase4kin.teamcityapp.buildtabs.view.BuildTabsViewImpl;
import com.github.vase4kin.teamcityapp.navigation.tracker.ViewTracker;
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage;
import com.github.vase4kin.teamcityapp.utils.StatusBarUtils;

import dagger.Module;
import dagger.Provides;
import de.greenrobot.event.EventBus;

@Module
public class BuildTabsModule {

    private View mView;
    private AppCompatActivity mActivity;

    public BuildTabsModule(View mView, AppCompatActivity mActivity) {
        this.mView = mView;
        this.mActivity = mActivity;
    }

    @Provides
    BuildTabsView providesBuildTabsView(StatusBarUtils statusBarUtils, BaseValueExtractor valueExtractor) {
        return new BuildTabsViewImpl(mView, mActivity, statusBarUtils, valueExtractor);
    }

    @Provides
    ViewTracker providesViewTracker() {
        return new BuildsTabViewTrackerImpl();
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
    BuildTabsInteractor providesBaseTabsDataManager(EventBus eventBus,
                                                    BaseValueExtractor valueExtractor,
                                                    SharedUserStorage sharedUserStorage, TeamCityService teamCityService) {
        return new BuildTabsInteractorImpl(eventBus, valueExtractor, sharedUserStorage, teamCityService);
    }
}
