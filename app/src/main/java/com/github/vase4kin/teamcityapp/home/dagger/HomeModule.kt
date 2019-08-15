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

package com.github.vase4kin.teamcityapp.home.dagger

import android.content.Context
import com.github.vase4kin.teamcityapp.api.Repository
import com.github.vase4kin.teamcityapp.app_navigation.*
import com.github.vase4kin.teamcityapp.drawer.view.DrawerView
import com.github.vase4kin.teamcityapp.filter_bottom_sheet_dialog.filter.FilterProvider
import com.github.vase4kin.teamcityapp.home.data.HomeDataManager
import com.github.vase4kin.teamcityapp.home.data.HomeDataManagerImpl
import com.github.vase4kin.teamcityapp.home.router.HomeRouter
import com.github.vase4kin.teamcityapp.home.router.HomeRouterImpl
import com.github.vase4kin.teamcityapp.home.tracker.HomeTracker
import com.github.vase4kin.teamcityapp.home.tracker.HomeTrackerImpl
import com.github.vase4kin.teamcityapp.home.view.HomeActivity
import com.github.vase4kin.teamcityapp.home.view.HomeView
import com.github.vase4kin.teamcityapp.home.view.HomeViewImpl
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.Module
import dagger.Provides
import io.rx_cache2.internal.RxCache
import org.greenrobot.eventbus.EventBus

@Module
class HomeModule {

    @Provides
    fun providesRootDrawerView(activity: HomeActivity): HomeView {
        return HomeViewImpl(activity, DrawerView.HOME, false)
    }

    @Provides
    fun providesRootDataManager(context: Context,
                                repository: Repository,
                                sharedUserStorage: SharedUserStorage,
                                rxCache: RxCache,
                                eventBus: EventBus): HomeDataManager {
        return HomeDataManagerImpl(repository, sharedUserStorage, rxCache, eventBus)
    }

    @Provides
    fun providesRootRouter(activity: HomeActivity): HomeRouter = HomeRouterImpl(activity)

    @Provides
    fun providesFirebaseRootTracker(firebaseAnalytics: FirebaseAnalytics): HomeTracker {
        return HomeTrackerImpl(firebaseAnalytics)
    }

    @Provides
    fun providesFragmentFactory(): FragmentFactory = FragmentFactoryImpl()

    @Provides
    fun providesAppNavigationInteractor(activity: HomeActivity, fragmentFactory: FragmentFactory): AppNavigationInteractor {
        return AppNavigationInteractorImpl(activity.supportFragmentManager, fragmentFactory)
    }

    @Provides
    fun providesBottomNavigationView(appNavigationInteractor: AppNavigationInteractor, activity: HomeActivity): BottomNavigationView {
        return BottomNavigationViewImpl(appNavigationInteractor, activity)
    }

    @HomeActivityScope
    @Provides
    fun provideFilterProvider(): FilterProvider = FilterProvider()

}
