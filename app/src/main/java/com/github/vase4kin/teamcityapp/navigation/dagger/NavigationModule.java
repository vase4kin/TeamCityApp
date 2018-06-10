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

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.api.Repository;
import com.github.vase4kin.teamcityapp.base.list.view.BaseListView;
import com.github.vase4kin.teamcityapp.base.list.view.ViewHolderFactory;
import com.github.vase4kin.teamcityapp.navigation.data.NavigationDataManager;
import com.github.vase4kin.teamcityapp.navigation.data.NavigationDataManagerImpl;
import com.github.vase4kin.teamcityapp.navigation.data.NavigationDataModel;
import com.github.vase4kin.teamcityapp.navigation.extractor.NavigationValueExtractor;
import com.github.vase4kin.teamcityapp.navigation.extractor.NavigationValueExtractorImpl;
import com.github.vase4kin.teamcityapp.navigation.router.NavigationRouter;
import com.github.vase4kin.teamcityapp.navigation.router.NavigationRouterImpl;
import com.github.vase4kin.teamcityapp.navigation.tracker.FabricNavigationTrackerImpl;
import com.github.vase4kin.teamcityapp.navigation.tracker.FirebaseNavigationTrackerImpl;
import com.github.vase4kin.teamcityapp.navigation.tracker.NavigationTracker;
import com.github.vase4kin.teamcityapp.navigation.tracker.NavigationTrackerImpl;
import com.github.vase4kin.teamcityapp.navigation.view.NavigationAdapter;
import com.github.vase4kin.teamcityapp.navigation.view.NavigationView;
import com.github.vase4kin.teamcityapp.navigation.view.NavigationViewHolderFactory;
import com.github.vase4kin.teamcityapp.navigation.view.NavigationViewImpl;
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
public class NavigationModule {

    private View mView;
    private Activity mActivity;
    private Bundle mBundle;

    public NavigationModule(View view, Activity mActivity, Bundle mBundle) {
        this.mView = view;
        this.mActivity = mActivity;
        this.mBundle = mBundle;
    }

    @Provides
    NavigationView providesNavigationView(NavigationAdapter adapter) {
        return new NavigationViewImpl(mView, mActivity, R.string.empty_list_message_projects_or_build_types, adapter);
    }

    @Provides
    NavigationValueExtractor providesNavigationValueExtractor() {
        return new NavigationValueExtractorImpl(mBundle);
    }

    @Provides
    NavigationRouter providesNavigationRouter() {
        return new NavigationRouterImpl(mActivity);
    }

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
