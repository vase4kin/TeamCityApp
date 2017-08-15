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

package com.github.vase4kin.teamcityapp.overview.dagger;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.view.View;

import com.github.vase4kin.teamcityapp.api.Repository;
import com.github.vase4kin.teamcityapp.base.list.extractor.BaseValueExtractor;
import com.github.vase4kin.teamcityapp.base.list.extractor.BaseValueExtractorImpl;
import com.github.vase4kin.teamcityapp.base.list.view.BaseListView;
import com.github.vase4kin.teamcityapp.base.list.view.ViewHolderFactory;
import com.github.vase4kin.teamcityapp.overview.data.OverViewInteractor;
import com.github.vase4kin.teamcityapp.overview.data.OverviewDataModel;
import com.github.vase4kin.teamcityapp.overview.data.OverviewInteractorImpl;
import com.github.vase4kin.teamcityapp.overview.tracker.FabricOverviewTrackerImpl;
import com.github.vase4kin.teamcityapp.overview.tracker.FirebaseOverviewTrackerImpl;
import com.github.vase4kin.teamcityapp.overview.tracker.OverviewTracker;
import com.github.vase4kin.teamcityapp.overview.tracker.OverviewTrackerImpl;
import com.github.vase4kin.teamcityapp.overview.view.OverviewAdapter;
import com.github.vase4kin.teamcityapp.overview.view.OverviewView;
import com.github.vase4kin.teamcityapp.overview.view.OverviewViewHolderFactory;
import com.github.vase4kin.teamcityapp.overview.view.OverviewViewImpl;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.greenrobot.eventbus.EventBus;

import java.util.Map;
import java.util.Set;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntKey;
import dagger.multibindings.IntoMap;
import dagger.multibindings.IntoSet;

@Module
public class OverviewModule {

    private View mView;
    private Fragment mFragment;

    public OverviewModule(View mView, Fragment mFragment) {
        this.mView = mView;
        this.mFragment = mFragment;
    }

    @Provides
    OverViewInteractor providesOverViewDataManager(Repository repository,
                                                   EventBus eventBus,
                                                   BaseValueExtractor valueExtractor,
                                                   Context context) {
        return new OverviewInteractorImpl(repository, eventBus, valueExtractor, context);
    }

    @Provides
    BaseValueExtractor providesBaseValueExtractor() {
        return new BaseValueExtractorImpl(mFragment.getArguments());
    }

    @Provides
    OverviewView providesBaseListView(OverviewAdapter adapter) {
        return new OverviewViewImpl(mView, mFragment.getActivity(), adapter);
    }

    @Provides
    OverviewAdapter providesOverviewAdapter(Map<Integer, ViewHolderFactory<OverviewDataModel>> viewHolderFactories) {
        return new OverviewAdapter(viewHolderFactories);
    }

    @IntoMap
    @IntKey(BaseListView.TYPE_DEFAULT)
    @Provides
    ViewHolderFactory<OverviewDataModel> providesOverviewViewHolderFactory() {
        return new OverviewViewHolderFactory();
    }

    @IntoSet
    @Provides
    OverviewTracker providesFabricViewTracker() {
        return new FabricOverviewTrackerImpl();
    }

    @IntoSet
    @Provides
    OverviewTracker providesFirebaseViewTracker(FirebaseAnalytics firebaseAnalytics) {
        return new FirebaseOverviewTrackerImpl(firebaseAnalytics);
    }

    @Provides
    OverviewTracker providesViewTracker(Set<OverviewTracker> trackers) {
        return new OverviewTrackerImpl(trackers);
    }
}
