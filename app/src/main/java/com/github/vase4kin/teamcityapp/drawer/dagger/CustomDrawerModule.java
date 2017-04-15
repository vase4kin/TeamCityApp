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

import com.github.vase4kin.teamcityapp.api.Repository;
import com.github.vase4kin.teamcityapp.drawer.data.DrawerDataManager;
import com.github.vase4kin.teamcityapp.drawer.data.DrawerDataManagerImpl;
import com.github.vase4kin.teamcityapp.drawer.router.DrawerRouter;
import com.github.vase4kin.teamcityapp.drawer.router.DrawerRouterImpl;
import com.github.vase4kin.teamcityapp.drawer.tracker.DrawerTracker;
import com.github.vase4kin.teamcityapp.drawer.tracker.DrawerTrackerImpl;
import com.github.vase4kin.teamcityapp.drawer.tracker.FabricDrawerTrackerImpl;
import com.github.vase4kin.teamcityapp.drawer.tracker.FirebaseDrawerTrackerImpl;
import com.github.vase4kin.teamcityapp.drawer.view.CustomAnimationDrawerViewImpl;
import com.github.vase4kin.teamcityapp.drawer.view.DrawerView;
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.Set;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoSet;

@Module
public class CustomDrawerModule {

    private AppCompatActivity mActivity;
    private boolean mIsBackArrowEnabled;
    private int mDrawerSelection;

    public CustomDrawerModule(AppCompatActivity activity, boolean isBackArrowEnabled, int drawerSelection) {
        this.mActivity = activity;
        this.mIsBackArrowEnabled = isBackArrowEnabled;
        this.mDrawerSelection = drawerSelection;
    }

    @Provides
    DrawerView providesDrawerView() {
        return new CustomAnimationDrawerViewImpl(mActivity, mDrawerSelection, mIsBackArrowEnabled);
    }

    @Provides
    DrawerDataManager providesDrawerDataManager(Repository repository, SharedUserStorage sharedUserStorage) {
        return new DrawerDataManagerImpl(repository, sharedUserStorage);
    }

    @Provides
    DrawerRouter providesDrawerRouter() {
        return new DrawerRouterImpl(mActivity);
    }

    @IntoSet
    @Provides
    DrawerTracker providesFabricViewTracker() {
        return new FabricDrawerTrackerImpl();
    }

    @IntoSet
    @Provides
    DrawerTracker providesFirebaseViewTracker(FirebaseAnalytics firebaseAnalytics) {
        return new FirebaseDrawerTrackerImpl(firebaseAnalytics);
    }

    @Provides
    DrawerTracker providesViewTracker(Set<DrawerTracker> trackers) {
        return new DrawerTrackerImpl(trackers);
    }
}
