package com.github.vase4kin.teamcityapp.navigation.dagger

import teamcityapp.libraries.utils.requireScreenOwner
import android.app.Activity
import android.os.Bundle
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.api.Repository
import com.github.vase4kin.teamcityapp.base.list.view.BaseListView
import com.github.vase4kin.teamcityapp.base.list.view.ViewHolderFactory
import com.github.vase4kin.teamcityapp.navigation.data.NavigationDataManager
import com.github.vase4kin.teamcityapp.navigation.data.NavigationDataManagerImpl
import com.github.vase4kin.teamcityapp.navigation.data.NavigationDataModel
import com.github.vase4kin.teamcityapp.navigation.extractor.NavigationValueExtractor
import com.github.vase4kin.teamcityapp.navigation.extractor.NavigationValueExtractorImpl
import com.github.vase4kin.teamcityapp.navigation.presenter.NavigationPresenterImpl
import com.github.vase4kin.teamcityapp.navigation.router.NavigationRouter
import com.github.vase4kin.teamcityapp.navigation.router.NavigationRouterImpl
import com.github.vase4kin.teamcityapp.navigation.tracker.NavigationTracker
import com.github.vase4kin.teamcityapp.navigation.tracker.NavigationTrackerImpl
import com.github.vase4kin.teamcityapp.navigation.view.NavigationActivity
import com.github.vase4kin.teamcityapp.navigation.view.NavigationAdapter
import com.github.vase4kin.teamcityapp.navigation.view.NavigationView
import com.github.vase4kin.teamcityapp.navigation.view.NavigationViewHolderFactory
import com.github.vase4kin.teamcityapp.navigation.view.NavigationViewImpl
import com.github.vase4kin.teamcityapp.navigation.view.RateTheAppViewHolderFactory
import com.google.firebase.analytics.FirebaseAnalytics
import teamcityapp.libraries.app_rating.AppRating
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.multibindings.IntKey
import dagger.multibindings.IntoMap
import javax.inject.Named
import teamcityapp.libraries.remote.RemoteService

@Module
@InstallIn(ActivityComponent::class)
object NavigationModule {

    @Provides
    fun provideOwner(owner: Activity): NavigationActivity = owner.requireScreenOwner<NavigationActivity>()

    @Provides
    @Named("NavigationActivity")
    fun providesNavigationView(
        activity: NavigationActivity,
        @Named("NavigationActivity") adapter: NavigationAdapter
    ): NavigationView {
        return NavigationViewImpl(
            activity.findViewById(android.R.id.content),
            activity,
            R.string.empty_list_message_projects_or_build_types,
            adapter
        )
    }

    @Provides
    @Named("NavigationActivity")
    fun providesNavigationValueExtractor(activity: NavigationActivity): NavigationValueExtractor {
        return NavigationValueExtractorImpl(activity.intent.extras ?: Bundle.EMPTY)
    }

    @Provides
    @Named("NavigationActivity")
    fun providesNavigationRouter(activity: NavigationActivity, appRating: AppRating): NavigationRouter {
        return NavigationRouterImpl(activity, appRating)
    }

    @Provides
    @Named("NavigationActivity")
    fun providesNavigationDataManager(
        repository: Repository,
        activity: NavigationActivity,
        remoteService: RemoteService
    ): NavigationDataManager {
        return NavigationDataManagerImpl(repository, activity, remoteService)
    }

    @Provides
    @Named("NavigationActivity")
    fun providesNavigationAdapter(@Named("NavigationActivity") viewHolderFactories: Map<Int, @JvmSuppressWildcards ViewHolderFactory<NavigationDataModel>>): NavigationAdapter {
        return NavigationAdapter(viewHolderFactories)
    }

    @Provides
    @Named("NavigationActivity")
    @IntoMap
    @IntKey(BaseListView.TYPE_DEFAULT)
    fun providesNavigationViewHolderFactory(): ViewHolderFactory<NavigationDataModel> {
        return NavigationViewHolderFactory()
    }

    @Provides
    @Named("NavigationActivity")
    @IntoMap
    @IntKey(NavigationView.TYPE_RATE_THE_APP)
    fun providesRateTheAppViewHolderFactory(): ViewHolderFactory<NavigationDataModel> {
        return RateTheAppViewHolderFactory()
    }

    @Provides
    @Named("NavigationActivity")
    fun providesFirebaseViewTracker(firebaseAnalytics: FirebaseAnalytics): NavigationTracker {
        return NavigationTrackerImpl(firebaseAnalytics)
    }

    @Provides
    @Named("NavigationActivity")
    fun provideNavigationPresenterImpl(
        @Named("NavigationActivity") view: NavigationView,
        @Named("NavigationActivity") dataManager: NavigationDataManager,
        @Named("NavigationActivity") tracker: NavigationTracker,
        @Named("NavigationActivity") valueExtractor: NavigationValueExtractor,
        @Named("NavigationActivity") router: NavigationRouter
    ): NavigationPresenterImpl = NavigationPresenterImpl(view, dataManager, tracker, valueExtractor, router)
}
