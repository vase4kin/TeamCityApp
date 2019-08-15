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

package com.github.vase4kin.teamcityapp.navigation.dagger

import android.content.Context
import com.github.vase4kin.teamcityapp.api.Repository
import com.github.vase4kin.teamcityapp.base.list.view.BaseListView
import com.github.vase4kin.teamcityapp.base.list.view.ViewHolderFactory
import com.github.vase4kin.teamcityapp.navigation.data.NavigationDataManager
import com.github.vase4kin.teamcityapp.navigation.data.NavigationDataManagerImpl
import com.github.vase4kin.teamcityapp.navigation.data.NavigationDataModel
import com.github.vase4kin.teamcityapp.navigation.tracker.NavigationTracker
import com.github.vase4kin.teamcityapp.navigation.tracker.NavigationTrackerImpl
import com.github.vase4kin.teamcityapp.navigation.view.NavigationAdapter
import com.github.vase4kin.teamcityapp.navigation.view.NavigationView
import com.github.vase4kin.teamcityapp.navigation.view.NavigationViewHolderFactory
import com.github.vase4kin.teamcityapp.navigation.view.RateTheAppViewHolderFactory
import com.github.vase4kin.teamcityapp.remote.RemoteService
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntKey
import dagger.multibindings.IntoMap

@Module
class NavigationBaseModule {

    @Provides
    internal fun providesNavigationDataManager(repository: Repository, context: Context, remoteService: RemoteService): NavigationDataManager {
        return NavigationDataManagerImpl(repository, context, remoteService)
    }

    @Provides
    internal fun providesNavigationAdapter(viewHolderFactories: Map<Int, @JvmSuppressWildcards ViewHolderFactory<NavigationDataModel>>): NavigationAdapter {
        return NavigationAdapter(viewHolderFactories)
    }

    @IntoMap
    @IntKey(BaseListView.TYPE_DEFAULT)
    @Provides
    internal fun providesNavigationViewHolderFactory(): ViewHolderFactory<NavigationDataModel> {
        return NavigationViewHolderFactory()
    }

    @IntoMap
    @IntKey(NavigationView.TYPE_RATE_THE_APP)
    @Provides
    internal fun providesRateTheAppViewHolderFactory(): ViewHolderFactory<NavigationDataModel> {
        return RateTheAppViewHolderFactory()
    }

    @Provides
    internal fun providesFirebaseViewTracker(firebaseAnalytics: FirebaseAnalytics): NavigationTracker {
        return NavigationTrackerImpl(firebaseAnalytics)
    }
}
