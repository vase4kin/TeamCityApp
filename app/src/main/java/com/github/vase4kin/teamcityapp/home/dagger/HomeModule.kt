package com.github.vase4kin.teamcityapp.home.dagger

import teamcityapp.libraries.utils.requireScreenOwner
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
import teamcityapp.libraries.cache_manager.CacheManager
import teamcityapp.libraries.chrome_tabs.ChromeCustomTabs
import teamcityapp.libraries.chrome_tabs.ChromeCustomTabsImpl
import teamcityapp.libraries.onboarding.OnboardingManager
import teamcityapp.libraries.storage.Storage

@Module
@InstallIn(ActivityComponent::class)
object HomeModule {

    @Provides
    fun provideOwner(owner: Activity): HomeActivity = owner.requireScreenOwner<HomeActivity>()

    @Provides
    fun providesRootDrawerView(activity: HomeActivity): HomeView {
        return HomeViewImpl(activity)
    }

    @Provides
    fun providesRootDataManager(
        repository: Repository,
        sharedUserStorage: SharedUserStorage,
        cacheManager: CacheManager,
        eventBus: EventBus
    ): HomeDataManager {
        return HomeDataManagerImpl(repository, sharedUserStorage, cacheManager, eventBus)
    }

    @Provides
    fun providesFirebaseRootTracker(firebaseAnalytics: FirebaseAnalytics): HomeTracker {
        return HomeTrackerImpl(firebaseAnalytics)
    }

    @Provides
    fun providesFragmentFactory(): FragmentFactory = FragmentFactoryImpl()

    @Provides
    @ActivityScoped
    fun providesAppNavigationInteractor(
        activity: HomeActivity,
        fragmentFactory: FragmentFactory
    ): AppNavigationInteractor {
        return AppNavigationInteractorImpl(activity.supportFragmentManager, fragmentFactory)
    }

    @Provides
    fun providesBottomNavigationView(
        appNavigationInteractor: AppNavigationInteractor,
        activity: HomeActivity
    ): BottomNavigationView {
        return BottomNavigationViewImpl(appNavigationInteractor, activity)
    }

    @Provides
    @ActivityScoped
    fun provideFilterProvider(): FilterProvider = FilterProvider()

    @Provides
    @Named("HomeActivity")
    @ActivityScoped
    fun provideChromeTabs(activity: HomeActivity): ChromeCustomTabs =
        ChromeCustomTabsImpl(activity)

    @Provides
    @Named("HomeActivity")
    fun providesBuildLogInteractor(activity: HomeActivity, storage: Storage): BuildLogInteractor {
        return BuildLogInteractorImpl(
            storage,
            activity,
            activity.intent.extras
        )
    }

    @Provides
    @ActivityScoped
    fun provideHomeRouter(activity: HomeActivity): HomeRouter {
        return HomeRouterImpl(activity)
    }

    @Provides
    fun provideHomePresenterImpl(
        view: HomeView,
        dataManager: HomeDataManager,
        tracker: HomeTracker,
        @Named("HomeActivity") interactor: BuildLogInteractor,
        onboardingManager: OnboardingManager,
        bottomNavigationView: BottomNavigationView,
        filterProvider: FilterProvider
    ): HomePresenterImpl =
        HomePresenterImpl(
            view,
            dataManager,
            tracker,
            interactor,
            onboardingManager,
            bottomNavigationView,
            filterProvider
        )
}
