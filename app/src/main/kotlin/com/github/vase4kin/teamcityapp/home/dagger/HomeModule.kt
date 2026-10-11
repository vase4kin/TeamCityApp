/*
 * Copyright 2026 Andrey Tolpeev
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

import android.app.Activity
import com.github.vase4kin.teamcityapp.api.Repository
import com.github.vase4kin.teamcityapp.app_navigation.AppNavigationInteractor
import com.github.vase4kin.teamcityapp.app_navigation.AppNavigationInteractorImpl
import com.github.vase4kin.teamcityapp.app_navigation.BottomNavigationView
import com.github.vase4kin.teamcityapp.app_navigation.BottomNavigationViewImpl
import com.github.vase4kin.teamcityapp.app_navigation.FragmentFactory
import com.github.vase4kin.teamcityapp.app_navigation.FragmentFactoryImpl
import com.github.vase4kin.teamcityapp.buildlog.data.BuildLogInteractor
import com.github.vase4kin.teamcityapp.buildlog.data.BuildLogInteractorImpl
import com.github.vase4kin.teamcityapp.filter_bottom_sheet_dialog.filter.FilterProvider
import com.github.vase4kin.teamcityapp.home.data.HomeDataManager
import com.github.vase4kin.teamcityapp.home.data.HomeDataManagerImpl
import com.github.vase4kin.teamcityapp.home.presenter.HomePresenterImpl
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
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Named
import org.greenrobot.eventbus.EventBus
import teamcityapp.features.agents.api.AgentsNavigation
import teamcityapp.features.build_queue.api.BuildQueueNavigation
import teamcityapp.features.drawer.api.navigation.DrawerNavigation
import teamcityapp.features.favorites.api.FavoritesNavigation
import teamcityapp.features.navigation.api.NavigationNavigation
import teamcityapp.features.running_builds.api.RunningBuildsNavigation
import teamcityapp.libraries.cache_manager.CacheManager
import teamcityapp.libraries.onboarding.OnboardingManager
import teamcityapp.libraries.storage.Storage
import teamcityapp.libraries.utils.requireScreenOwner

@Module
@InstallIn(ActivityComponent::class)
object HomeModule {

    @Provides
    fun provideOwner(owner: Activity): HomeActivity = owner.requireScreenOwner<HomeActivity>()

    @Provides
    fun providesRootDrawerView(activity: HomeActivity, featureNavigation: teamcityapp.features.filter_bottom_sheet.api.FilterBottomSheetNavigation): HomeView = HomeViewImpl(activity, featureNavigation)

    @Provides
    fun providesRootDataManager(
        repository: Repository,
        sharedUserStorage: SharedUserStorage,
        cacheManager: CacheManager,
        eventBus: EventBus
    ): HomeDataManager = HomeDataManagerImpl(repository, sharedUserStorage, cacheManager, eventBus)

    @Provides
    fun providesFirebaseRootTracker(firebaseAnalytics: FirebaseAnalytics): HomeTracker = HomeTrackerImpl(firebaseAnalytics)

    @Provides
    fun providesFragmentFactory(agentsNavigation: AgentsNavigation, navigation: NavigationNavigation, favorites: FavoritesNavigation, running: RunningBuildsNavigation, queue: BuildQueueNavigation): FragmentFactory = FragmentFactoryImpl(agentsNavigation, navigation, favorites, running, queue)

    @Provides
    @ActivityScoped
    fun providesAppNavigationInteractor(
        activity: HomeActivity,
        fragmentFactory: FragmentFactory
    ): AppNavigationInteractor = AppNavigationInteractorImpl(activity.supportFragmentManager, fragmentFactory)

    @Provides
    fun providesBottomNavigationView(
        appNavigationInteractor: AppNavigationInteractor,
        activity: HomeActivity
    ): BottomNavigationView = BottomNavigationViewImpl(appNavigationInteractor, activity)

    @Provides
    @Named("HomeActivity")
    fun providesBuildLogInteractor(activity: HomeActivity, storage: Storage): BuildLogInteractor = BuildLogInteractorImpl(
        storage,
        activity,
        activity.intent.extras
    )

    @Provides
    @ActivityScoped
    fun provideHomeRouter(activity: HomeActivity, drawerNavigation: DrawerNavigation): HomeRouter = HomeRouterImpl(activity, drawerNavigation)

    @Provides
    fun provideHomePresenterImpl(
        view: HomeView,
        dataManager: HomeDataManager,
        tracker: HomeTracker,
        @Named("HomeActivity") interactor: BuildLogInteractor,
        onboardingManager: OnboardingManager,
        bottomNavigationView: BottomNavigationView,
        filterProvider: FilterProvider
    ): HomePresenterImpl = HomePresenterImpl(
        view,
        dataManager,
        tracker,
        interactor,
        onboardingManager,
        bottomNavigationView,
        filterProvider
    )
}
