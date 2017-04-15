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

package com.github.vase4kin.teamcityapp.agenttabs.dagger;

import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.github.vase4kin.teamcityapp.agenttabs.tracker.AgentTabsViewTracker;
import com.github.vase4kin.teamcityapp.agenttabs.tracker.AgentTabsViewTrackerImpl;
import com.github.vase4kin.teamcityapp.agenttabs.tracker.FabricAgentsTabViewTrackerImpl;
import com.github.vase4kin.teamcityapp.agenttabs.tracker.FirebaseAgentTabsViewTrackerImpl;
import com.github.vase4kin.teamcityapp.agenttabs.view.AgentTabsViewModelImpl;
import com.github.vase4kin.teamcityapp.base.tabs.data.BaseTabsDataManager;
import com.github.vase4kin.teamcityapp.base.tabs.data.BaseTabsDataManagerImpl;
import com.github.vase4kin.teamcityapp.base.tabs.view.BaseTabsViewModel;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.Set;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoSet;
import de.greenrobot.event.EventBus;

@Module
public class AgentsTabsModule {

    private View mView;
    private AppCompatActivity mActivity;

    public AgentsTabsModule(View mView, AppCompatActivity mActivity) {
        this.mView = mView;
        this.mActivity = mActivity;
    }

    @Provides
    BaseTabsViewModel providesBaseTabsViewModel() {
        return new AgentTabsViewModelImpl(mView, mActivity);
    }

    @Provides
    BaseTabsDataManager providesBaseTabsDataManager(EventBus eventBus) {
        return new BaseTabsDataManagerImpl(eventBus);
    }

    @IntoSet
    @Provides
    AgentTabsViewTracker providesFabricViewTracker() {
        return new FabricAgentsTabViewTrackerImpl();
    }

    @IntoSet
    @Provides
    AgentTabsViewTracker providesFirebaseViewTracker(FirebaseAnalytics firebaseAnalytics) {
        return new FirebaseAgentTabsViewTrackerImpl(firebaseAnalytics);
    }

    @Provides
    AgentTabsViewTracker provideViewTracker(Set<AgentTabsViewTracker> trackers) {
        return new AgentTabsViewTrackerImpl(trackers);
    }
}
