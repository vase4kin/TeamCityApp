package com.github.vase4kin.teamcityapp.favorites.dagger

import teamcityapp.libraries.utils.requireScreenOwner
import android.app.Activity
import androidx.fragment.app.Fragment
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.api.Repository
import com.github.vase4kin.teamcityapp.base.list.view.BaseListView
import com.github.vase4kin.teamcityapp.base.list.view.SimpleSectionedRecyclerViewAdapter
import com.github.vase4kin.teamcityapp.base.list.view.ViewHolderFactory
import com.github.vase4kin.teamcityapp.buildlist.filter.BuildListFilter
import com.github.vase4kin.teamcityapp.favorites.interactor.FavoritesInteractor
import com.github.vase4kin.teamcityapp.favorites.interactor.FavoritesInteractorImpl
import com.github.vase4kin.teamcityapp.favorites.presenter.FavoritesPresenterImpl
import com.github.vase4kin.teamcityapp.favorites.tracker.FavoritesTracker
import com.github.vase4kin.teamcityapp.favorites.tracker.FavoritesTrackerImpl
import com.github.vase4kin.teamcityapp.favorites.view.FavoritesFragment
import com.github.vase4kin.teamcityapp.favorites.view.FavoritesView
import com.github.vase4kin.teamcityapp.favorites.view.FavoritesViewImpl
import com.github.vase4kin.teamcityapp.navigation.data.NavigationDataModel
import com.github.vase4kin.teamcityapp.navigation.extractor.NavigationValueExtractor
import com.github.vase4kin.teamcityapp.navigation.router.NavigationRouter
import com.github.vase4kin.teamcityapp.navigation.router.NavigationRouterImpl
import com.github.vase4kin.teamcityapp.navigation.view.NavigationAdapter
import com.github.vase4kin.teamcityapp.navigation.view.NavigationViewHolderFactory
import com.github.vase4kin.teamcityapp.overview.data.BuildDetails
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dagger.multibindings.IntKey
import dagger.multibindings.IntoMap
import javax.inject.Named

@Module
@InstallIn(FragmentComponent::class)
object FavoritesFragmentModule {

    @Provides
    fun provideOwner(owner: Fragment): FavoritesFragment = owner.requireScreenOwner<FavoritesFragment>()

    @Provides
    fun providesNavigationView(
        fragment: FavoritesFragment,
        adapter: SimpleSectionedRecyclerViewAdapter<NavigationAdapter>
    ): FavoritesView {
        return FavoritesViewImpl(
            fragment.requireView(),
            fragment.requireActivity(),
            R.string.empty_list_message_favorites,
            adapter
        )
    }

    @Provides
    @Named("FavoritesFragment")
    fun providesNavigationRouter(fragment: FavoritesFragment): NavigationRouter {
        return NavigationRouterImpl(fragment.requireActivity())
    }

    @Provides
    @Named("FavoritesFragment")
    fun providesNavigationValueExtractor(): NavigationValueExtractor {
        return object : NavigationValueExtractor {
            override val id: String
                get() = ""
            override val name: String
                get() = ""
            override val buildDetails: BuildDetails
                get() = BuildDetails.STUB
            override val buildListFilter: BuildListFilter?
                get() = null
            override val isBundleNullOrEmpty: Boolean
                get() = true
        }
    }

    @Provides
    fun providesFavoritesInteractor(repository: Repository, storage: SharedUserStorage): FavoritesInteractor {
        return FavoritesInteractorImpl(repository, storage)
    }

    @Provides
    @Named("FavoritesFragment")
    fun providesNavigationAdapter(@Named("FavoritesFragment") viewHolderFactories: Map<Int, @JvmSuppressWildcards ViewHolderFactory<NavigationDataModel>>): NavigationAdapter {
        return NavigationAdapter(viewHolderFactories)
    }

    @Provides
    fun providesSimpleSectionedRecyclerViewAdapter(
        fragment: FavoritesFragment,
        @Named("FavoritesFragment") adapter: NavigationAdapter
    ): SimpleSectionedRecyclerViewAdapter<NavigationAdapter> {
        return SimpleSectionedRecyclerViewAdapter(fragment.requireContext(), adapter)
    }

    @Provides
    @Named("FavoritesFragment")
    @IntoMap
    @IntKey(BaseListView.TYPE_DEFAULT)
    fun providesNavigationViewHolderFactory(): ViewHolderFactory<NavigationDataModel> {
        return NavigationViewHolderFactory()
    }

    @Provides
    fun providesFavoritesTracker(firebaseAnalytics: FirebaseAnalytics): FavoritesTracker {
        return FavoritesTrackerImpl(firebaseAnalytics)
    }

    @Provides
    fun provideFavoritesPresenterImpl(
        view: FavoritesView,
        interactor: FavoritesInteractor,
        tracker: FavoritesTracker,
        @Named("FavoritesFragment") valueExtractor: NavigationValueExtractor,
        @Named("FavoritesFragment") router: NavigationRouter
    ): FavoritesPresenterImpl = FavoritesPresenterImpl(view, interactor, tracker, valueExtractor, router)
}
