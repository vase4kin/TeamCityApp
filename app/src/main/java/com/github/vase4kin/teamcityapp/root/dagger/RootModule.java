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

package com.github.vase4kin.teamcityapp.root.dagger;

import android.content.Context;

import com.github.vase4kin.teamcityapp.api.TeamCityService;
import com.github.vase4kin.teamcityapp.drawer.view.DrawerView;
import com.github.vase4kin.teamcityapp.root.data.RootDataManager;
import com.github.vase4kin.teamcityapp.root.data.RootDataManagerImpl;
import com.github.vase4kin.teamcityapp.root.extractor.RootBundleValueManager;
import com.github.vase4kin.teamcityapp.root.extractor.RootBundleValueManagerImpl;
import com.github.vase4kin.teamcityapp.root.router.RootRouter;
import com.github.vase4kin.teamcityapp.root.router.RootRouterImpl;
import com.github.vase4kin.teamcityapp.root.tracker.RootTracker;
import com.github.vase4kin.teamcityapp.root.tracker.RootTrackerImpl;
import com.github.vase4kin.teamcityapp.root.view.OnAccountSwitchListener;
import com.github.vase4kin.teamcityapp.root.view.RootDrawerView;
import com.github.vase4kin.teamcityapp.root.view.RootDrawerViewImpl;
import com.github.vase4kin.teamcityapp.root.view.RootProjectsActivity;
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage;

import dagger.Module;
import dagger.Provides;

@Module
public class RootModule {

    private RootProjectsActivity mActivity;

    public RootModule(RootProjectsActivity activity) {
        this.mActivity = activity;
    }

    @Provides
    RootDrawerView providesRootDrawerView() {
        return new RootDrawerViewImpl(mActivity, DrawerView.PROJECTS, false);
    }

    @Provides
    RootDataManager providesRootDataManager(Context context, TeamCityService teamCityService, SharedUserStorage sharedUserStorage) {
        return new RootDataManagerImpl(context, teamCityService, sharedUserStorage);
    }

    @Provides
    RootRouter providesRootRouter() {
        return new RootRouterImpl(mActivity);
    }

    @Provides
    RootBundleValueManager providesRootValueExtractor() {
        return new RootBundleValueManagerImpl(mActivity.getIntent().getExtras());
    }

    @Provides
    OnAccountSwitchListener providesOnAccountSwitchListener() {
        return mActivity;
    }

    @Provides
    RootTracker providesRootTracker() {
        return new RootTrackerImpl();
    }
}
