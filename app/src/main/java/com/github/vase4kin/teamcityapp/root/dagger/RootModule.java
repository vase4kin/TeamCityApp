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

import com.github.vase4kin.teamcityapp.api.Repository;
import com.github.vase4kin.teamcityapp.app_navigation.AppNavigationInteractor;
import com.github.vase4kin.teamcityapp.app_navigation.AppNavigationInteractorImpl;
import com.github.vase4kin.teamcityapp.app_navigation.BottomNavigationView;
import com.github.vase4kin.teamcityapp.app_navigation.BottomNavigationViewImpl;
import com.github.vase4kin.teamcityapp.app_navigation.FragmentFactory;
import com.github.vase4kin.teamcityapp.app_navigation.FragmentFactoryImpl;
import com.github.vase4kin.teamcityapp.drawer.view.DrawerView;
import com.github.vase4kin.teamcityapp.root.data.RootDataManager;
import com.github.vase4kin.teamcityapp.root.data.RootDataManagerImpl;
import com.github.vase4kin.teamcityapp.root.extractor.RootBundleValueManager;
import com.github.vase4kin.teamcityapp.root.extractor.RootBundleValueManagerImpl;
import com.github.vase4kin.teamcityapp.root.router.RootRouter;
import com.github.vase4kin.teamcityapp.root.router.RootRouterImpl;
import com.github.vase4kin.teamcityapp.root.tracker.FirebaseRootTrackerImpl;
import com.github.vase4kin.teamcityapp.root.tracker.RootTracker;
import com.github.vase4kin.teamcityapp.root.view.OnAccountSwitchListener;
import com.github.vase4kin.teamcityapp.root.view.RootDrawerView;
import com.github.vase4kin.teamcityapp.root.view.RootDrawerViewImpl;
import com.github.vase4kin.teamcityapp.root.view.RootProjectsActivity;
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.greenrobot.eventbus.EventBus;

import dagger.Module;
import dagger.Provides;
import io.rx_cache2.internal.RxCache;

@Module
public class RootModule {

    @Provides
    RootDrawerView providesRootDrawerView(RootProjectsActivity activity) {
        return new RootDrawerViewImpl(activity, DrawerView.PROJECTS, false);
    }

    @Provides
    RootDataManager providesRootDataManager(Context context,
                                            Repository repository,
                                            SharedUserStorage sharedUserStorage,
                                            RxCache rxCache,
                                            EventBus eventBus) {
        return new RootDataManagerImpl(context, repository, sharedUserStorage, rxCache, eventBus);
    }

    @Provides
    RootRouter providesRootRouter(RootProjectsActivity activity) {
        return new RootRouterImpl(activity);
    }

    @Provides
    RootBundleValueManager providesRootValueExtractor(RootProjectsActivity activity) {
        return new RootBundleValueManagerImpl(activity.getIntent().getExtras());
    }

    @Provides
    OnAccountSwitchListener providesOnAccountSwitchListener(RootProjectsActivity activity) {
        return activity;
    }

    @Provides
    RootTracker providesFirebaseRootTracker(FirebaseAnalytics firebaseAnalytics) {
        return new FirebaseRootTrackerImpl(firebaseAnalytics);
    }

    @Provides
    FragmentFactory providesFragmentFactory() {
        return new FragmentFactoryImpl();
    }

    @Provides
    AppNavigationInteractor providesAppNavigationInteractor(RootProjectsActivity activity, FragmentFactory fragmentFactory) {
        return new AppNavigationInteractorImpl(activity.getSupportFragmentManager(), fragmentFactory);
    }

    @Provides
    BottomNavigationView providesBottomNavigationView(AppNavigationInteractor appNavigationInteractor, RootProjectsActivity activity) {
        return new BottomNavigationViewImpl(appNavigationInteractor, activity);
    }

}
