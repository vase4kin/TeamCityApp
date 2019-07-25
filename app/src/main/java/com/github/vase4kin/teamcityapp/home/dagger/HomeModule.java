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

package com.github.vase4kin.teamcityapp.home.dagger;

import android.content.Context;

import com.github.vase4kin.teamcityapp.api.Repository;
import com.github.vase4kin.teamcityapp.app_navigation.AppNavigationInteractor;
import com.github.vase4kin.teamcityapp.app_navigation.AppNavigationInteractorImpl;
import com.github.vase4kin.teamcityapp.app_navigation.BottomNavigationView;
import com.github.vase4kin.teamcityapp.app_navigation.BottomNavigationViewImpl;
import com.github.vase4kin.teamcityapp.app_navigation.FragmentFactory;
import com.github.vase4kin.teamcityapp.app_navigation.FragmentFactoryImpl;
import com.github.vase4kin.teamcityapp.drawer.view.DrawerView;
import com.github.vase4kin.teamcityapp.filter_bottom_sheet_dialog.FilterProvider;
import com.github.vase4kin.teamcityapp.home.data.HomeDataManager;
import com.github.vase4kin.teamcityapp.home.data.HomeDataManagerImpl;
import com.github.vase4kin.teamcityapp.home.extractor.HomeBundleValueManager;
import com.github.vase4kin.teamcityapp.home.extractor.HomeBundleValueManagerImpl;
import com.github.vase4kin.teamcityapp.home.router.HomeRouter;
import com.github.vase4kin.teamcityapp.home.router.HomeRouterImpl;
import com.github.vase4kin.teamcityapp.home.tracker.FirebaseHomeTrackerImpl;
import com.github.vase4kin.teamcityapp.home.tracker.HomeTracker;
import com.github.vase4kin.teamcityapp.home.view.HomeActivity;
import com.github.vase4kin.teamcityapp.home.view.HomeView;
import com.github.vase4kin.teamcityapp.home.view.HomeViewImpl;
import com.github.vase4kin.teamcityapp.home.view.OnAccountSwitchListener;
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.greenrobot.eventbus.EventBus;

import dagger.Module;
import dagger.Provides;
import io.rx_cache2.internal.RxCache;

@Module
public class HomeModule {

    @Provides
    HomeView providesRootDrawerView(HomeActivity activity) {
        return new HomeViewImpl(activity, DrawerView.HOME, false);
    }

    @Provides
    HomeDataManager providesRootDataManager(Context context,
                                            Repository repository,
                                            SharedUserStorage sharedUserStorage,
                                            RxCache rxCache,
                                            EventBus eventBus) {
        return new HomeDataManagerImpl(context, repository, sharedUserStorage, rxCache, eventBus);
    }

    @Provides
    HomeRouter providesRootRouter(HomeActivity activity) {
        return new HomeRouterImpl(activity);
    }

    @Provides
    HomeBundleValueManager providesRootValueExtractor(HomeActivity activity) {
        return new HomeBundleValueManagerImpl(activity.getIntent().getExtras());
    }

    @Provides
    OnAccountSwitchListener providesOnAccountSwitchListener(HomeActivity activity) {
        return activity;
    }

    @Provides
    HomeTracker providesFirebaseRootTracker(FirebaseAnalytics firebaseAnalytics) {
        return new FirebaseHomeTrackerImpl(firebaseAnalytics);
    }

    @Provides
    FragmentFactory providesFragmentFactory() {
        return new FragmentFactoryImpl();
    }

    @Provides
    AppNavigationInteractor providesAppNavigationInteractor(HomeActivity activity, FragmentFactory fragmentFactory) {
        return new AppNavigationInteractorImpl(activity.getSupportFragmentManager(), fragmentFactory);
    }

    @Provides
    BottomNavigationView providesBottomNavigationView(AppNavigationInteractor appNavigationInteractor, HomeActivity activity) {
        return new BottomNavigationViewImpl(appNavigationInteractor, activity);
    }

    @HomeActivityScope
    @Provides
    FilterProvider provideFilterProvider() {
        return new FilterProvider();
    }

}
