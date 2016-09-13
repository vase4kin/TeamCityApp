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

package com.github.vase4kin.teamcityapp.drawer.dagger;

import android.support.v7.app.AppCompatActivity;

import com.github.vase4kin.teamcityapp.api.TeamCityService;
import com.github.vase4kin.teamcityapp.drawer.data.DrawerDataManager;
import com.github.vase4kin.teamcityapp.drawer.data.DrawerDataManagerImpl;
import com.github.vase4kin.teamcityapp.drawer.router.DrawerRouter;
import com.github.vase4kin.teamcityapp.drawer.router.DrawerRouterImpl;
import com.github.vase4kin.teamcityapp.drawer.view.DrawerView;
import com.github.vase4kin.teamcityapp.drawer.view.DrawerViewImpl;
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage;

import dagger.Module;
import dagger.Provides;

@Module
public class DrawerModule {

    private AppCompatActivity mActivity;
    private boolean mIsBackArrowEnabled;
    private int mDrawerSelection;

    public DrawerModule(AppCompatActivity activity, boolean isBackArrowEnabled, int drawerSelection) {
        this.mActivity = activity;
        this.mIsBackArrowEnabled = isBackArrowEnabled;
        this.mDrawerSelection = drawerSelection;
    }

    @Provides
    DrawerView providesDrawerView() {
        return new DrawerViewImpl(mActivity, mDrawerSelection, mIsBackArrowEnabled);
    }

    @Provides
    DrawerDataManager providesDrawerDataManager(TeamCityService teamCityService, SharedUserStorage sharedUserStorage) {
        return new DrawerDataManagerImpl(teamCityService, sharedUserStorage);
    }

    @Provides
    DrawerRouter providesDrawerRouter() {
        return new DrawerRouterImpl(mActivity);
    }
}
