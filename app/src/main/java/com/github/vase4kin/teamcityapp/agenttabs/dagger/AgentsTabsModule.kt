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

package com.github.vase4kin.teamcityapp.agenttabs.dagger

import android.view.View
import com.github.vase4kin.teamcityapp.agenttabs.tracker.AgentTabsViewTracker
import com.github.vase4kin.teamcityapp.agenttabs.tracker.AgentTabsViewTrackerImpl
import com.github.vase4kin.teamcityapp.agenttabs.view.AgentTabsActivity
import com.github.vase4kin.teamcityapp.agenttabs.view.AgentTabsViewModelImpl
import com.github.vase4kin.teamcityapp.base.tabs.data.BaseTabsDataManager
import com.github.vase4kin.teamcityapp.base.tabs.data.BaseTabsDataManagerImpl
import com.github.vase4kin.teamcityapp.base.tabs.view.BaseTabsViewModel
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.Module
import dagger.Provides
import org.greenrobot.eventbus.EventBus

@Module
class AgentsTabsModule {

    @Provides
    fun providesBaseTabsViewModel(activity: AgentTabsActivity): BaseTabsViewModel {
        return AgentTabsViewModelImpl(activity.findViewById<View>(android.R.id.content), activity)
    }

    @Provides
    fun providesBaseTabsDataManager(eventBus: EventBus): BaseTabsDataManager {
        return BaseTabsDataManagerImpl(eventBus)
    }

    @Provides
    fun providesFirebaseViewTracker(firebaseAnalytics: FirebaseAnalytics): AgentTabsViewTracker {
        return AgentTabsViewTrackerImpl(firebaseAnalytics)
    }
}
