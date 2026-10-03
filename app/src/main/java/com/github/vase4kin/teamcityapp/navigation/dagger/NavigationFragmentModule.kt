package com.github.vase4kin.teamcityapp.navigation.dagger

import teamcityapp.libraries.utils.requireScreenOwner
import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.Fragment
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
import com.github.vase4kin.teamcityapp.navigation.view.NavigationAdapter
import com.github.vase4kin.teamcityapp.navigation.view.NavigationListFragment
import com.github.vase4kin.teamcityapp.navigation.view.NavigationView
import com.github.vase4kin.teamcityapp.navigation.view.NavigationViewHolderFactory
import com.github.vase4kin.teamcityapp.navigation.view.NavigationViewImpl
import com.github.vase4kin.teamcityapp.navigation.view.RateTheAppViewHolderFactory
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dagger.multibindings.IntKey
import dagger.multibindings.IntoMap
import javax.inject.Named
import teamcityapp.libraries.remote.RemoteService

@Module
@InstallIn(FragmentComponent::class)
object NavigationFragmentModule {

    @Provides
    fun provideOwner(owner: Fragment): NavigationListFragment = owner.requireScreenOwner<NavigationListFragment>()

    @Provides
    @Named("NavigationListFragment")
    fun providesNavigationView(
        fragment: NavigationListFragment,
        @Named("NavigationListFragment") adapter: NavigationAdapter
    ): NavigationView {
        return NavigationViewImpl(
            fragment.requireView(),
            fragment.requireActivity(),
            R.string.empty_list_message_projects_or_build_types,
            adapter
        )
    }

    @Provides
    @Named("NavigationListFragment")
    fun providesNavigationValueExtractor(fragment: NavigationListFragment): NavigationValueExtractor {
        return NavigationValueExtractorImpl(fragment.arguments ?: Bundle.EMPTY)
    }

    @Provides
    @Named("NavigationListFragment")
    fun providesNavigationRouter(fragment: NavigationListFragment): NavigationRouter {
        return NavigationRouterImpl(fragment.requireActivity())
    }

    @Provides
    @Named("NavigationListFragment")
    fun providesNavigationDataManager(
        repository: Repository,
        fragment: NavigationListFragment,
        remoteService: RemoteService
    ): NavigationDataManager {
        return NavigationDataManagerImpl(repository, fragment.requireContext(), remoteService)
    }

    @Provides
    @Named("NavigationListFragment")
    fun providesNavigationAdapter(@Named("NavigationListFragment") viewHolderFactories: Map<Int, @JvmSuppressWildcards ViewHolderFactory<NavigationDataModel>>): NavigationAdapter {
        return NavigationAdapter(viewHolderFactories)
    }

    @Provides
    @Named("NavigationListFragment")
    @IntoMap
    @IntKey(BaseListView.TYPE_DEFAULT)
    fun providesNavigationViewHolderFactory(): ViewHolderFactory<NavigationDataModel> {
        return NavigationViewHolderFactory()
    }

    @Provides
    @Named("NavigationListFragment")
    @IntoMap
    @IntKey(NavigationView.TYPE_RATE_THE_APP)
    fun providesRateTheAppViewHolderFactory(): ViewHolderFactory<NavigationDataModel> {
        return RateTheAppViewHolderFactory()
    }

    @Provides
    @Named("NavigationListFragment")
    fun providesFirebaseViewTracker(firebaseAnalytics: FirebaseAnalytics): NavigationTracker {
        return NavigationTrackerImpl(firebaseAnalytics)
    }

    @Provides
    @Named("NavigationListFragment")
    fun provideNavigationPresenterImpl(
        @Named("NavigationListFragment") view: NavigationView,
        @Named("NavigationListFragment") dataManager: NavigationDataManager,
        @Named("NavigationListFragment") tracker: NavigationTracker,
        @Named("NavigationListFragment") valueExtractor: NavigationValueExtractor,
        @Named("NavigationListFragment") router: NavigationRouter
    ): NavigationPresenterImpl = NavigationPresenterImpl(view, dataManager, tracker, valueExtractor, router)
}
