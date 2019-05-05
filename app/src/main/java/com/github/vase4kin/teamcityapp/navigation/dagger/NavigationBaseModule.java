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

package com.github.vase4kin.teamcityapp.navigation.dagger;

import android.content.Context;

import com.github.vase4kin.teamcityapp.api.Repository;
import com.github.vase4kin.teamcityapp.base.list.view.BaseListView;
import com.github.vase4kin.teamcityapp.base.list.view.ViewHolderFactory;
import com.github.vase4kin.teamcityapp.navigation.data.NavigationDataManager;
import com.github.vase4kin.teamcityapp.navigation.data.NavigationDataManagerImpl;
import com.github.vase4kin.teamcityapp.navigation.data.NavigationDataModel;
import com.github.vase4kin.teamcityapp.navigation.tracker.FabricNavigationTrackerImpl;
import com.github.vase4kin.teamcityapp.navigation.tracker.FirebaseNavigationTrackerImpl;
import com.github.vase4kin.teamcityapp.navigation.tracker.NavigationTracker;
import com.github.vase4kin.teamcityapp.navigation.tracker.NavigationTrackerImpl;
import com.github.vase4kin.teamcityapp.navigation.view.NavigationAdapter;
import com.github.vase4kin.teamcityapp.navigation.view.NavigationView;
import com.github.vase4kin.teamcityapp.navigation.view.NavigationViewHolderFactory;
import com.github.vase4kin.teamcityapp.navigation.view.RateTheAppViewHolderFactory;
import com.github.vase4kin.teamcityapp.remote.RemoteService;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.Map;
import java.util.Set;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntKey;
import dagger.multibindings.IntoMap;
import dagger.multibindings.IntoSet;

@Module
public class NavigationBaseModule {

    @Provides
    NavigationDataManager providesNavigationDataManager(Repository repository, Context context, RemoteService remoteService) {
        return new NavigationDataManagerImpl(repository, context, remoteService);
    }

    @Provides
    NavigationAdapter providesNavigationAdapter(Map<Integer, ViewHolderFactory<NavigationDataModel>> viewHolderFactories) {
        return new NavigationAdapter(viewHolderFactories);
    }

    @IntoMap
    @IntKey(BaseListView.TYPE_DEFAULT)
    @Provides
    ViewHolderFactory<NavigationDataModel> providesNavigationViewHolderFactory() {
        return new NavigationViewHolderFactory();
    }

    @IntoMap
    @IntKey(NavigationView.TYPE_RATE_THE_APP)
    @Provides
    ViewHolderFactory<NavigationDataModel> providesRateTheAppViewHolderFactory() {
        return new RateTheAppViewHolderFactory();
    }

    @IntoSet
    @Provides
    NavigationTracker providesFabricViewTracker() {
        return new FabricNavigationTrackerImpl();
    }

    @IntoSet
    @Provides
    NavigationTracker providesFirebaseViewTracker(FirebaseAnalytics firebaseAnalytics) {
        return new FirebaseNavigationTrackerImpl(firebaseAnalytics);
    }

    @Provides
    NavigationTracker providesViewTracker(Set<NavigationTracker> trackers) {
        return new NavigationTrackerImpl(trackers);
    }

}
