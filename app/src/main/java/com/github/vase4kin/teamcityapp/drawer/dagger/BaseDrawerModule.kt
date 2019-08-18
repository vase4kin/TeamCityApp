/*
 * Copyright 2019 Andrey Tolpeev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.vase4kin.teamcityapp.drawer.dagger

import com.github.vase4kin.teamcityapp.api.Repository
import com.github.vase4kin.teamcityapp.drawer.data.DrawerDataManager
import com.github.vase4kin.teamcityapp.drawer.data.DrawerDataManagerImpl
import com.github.vase4kin.teamcityapp.drawer.tracker.DrawerTracker
import com.github.vase4kin.teamcityapp.drawer.tracker.DrawerTrackerImpl
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.Module
import dagger.Provides
import org.greenrobot.eventbus.EventBus

@Module
class BaseDrawerModule {

    @Provides
    fun providesDrawerDataManager(
        repository: Repository,
        sharedUserStorage: SharedUserStorage,
        eventBus: EventBus
    ): DrawerDataManager {
        return DrawerDataManagerImpl(repository, sharedUserStorage, eventBus)
    }

    @Provides
    fun providesFirebaseViewTracker(firebaseAnalytics: FirebaseAnalytics): DrawerTracker {
        return DrawerTrackerImpl(firebaseAnalytics)
    }
}
