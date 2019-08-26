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

package com.github.vase4kin.teamcityapp.favorites.dagger

import android.app.Activity
import android.content.Context
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.api.Repository
import com.github.vase4kin.teamcityapp.base.list.view.BaseListView
import com.github.vase4kin.teamcityapp.base.list.view.SimpleSectionedRecyclerViewAdapter
import com.github.vase4kin.teamcityapp.base.list.view.ViewHolderFactory
import com.github.vase4kin.teamcityapp.buildlist.filter.BuildListFilter
import com.github.vase4kin.teamcityapp.favorites.interactor.FavoritesInteractor
import com.github.vase4kin.teamcityapp.favorites.interactor.FavoritesInteractorImpl
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
import dagger.multibindings.IntKey
import dagger.multibindings.IntoMap

@Module
class FavoritesFragmentModule {

    @Provides
    fun providesNavigationView(
        fragment: FavoritesFragment,
        adapter: SimpleSectionedRecyclerViewAdapter<NavigationAdapter>
    ): FavoritesView {
        return FavoritesViewImpl(
            fragment.view!!,
            fragment.activity,
            R.string.empty_list_message_favorites,
            adapter
        )
    }

    @Provides
    fun providesNavigationRouter(fragment: FavoritesFragment): NavigationRouter {
        return NavigationRouterImpl(fragment.activity as Activity)
    }

    @Provides
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
    fun providesFavoritesInteractor(
        repository: Repository,
        storage: SharedUserStorage
    ): FavoritesInteractor {
        return FavoritesInteractorImpl(repository, storage)
    }

    @Provides
    fun providesNavigationAdapter(
        viewHolderFactories: Map<Int, @JvmSuppressWildcards ViewHolderFactory<NavigationDataModel>>
    ): NavigationAdapter {
        return NavigationAdapter(viewHolderFactories)
    }

    @Provides
    fun providesSimpleSectionedRecyclerViewAdapter(
        context: Context,
        adapter: NavigationAdapter
    ): SimpleSectionedRecyclerViewAdapter<NavigationAdapter> {
        return SimpleSectionedRecyclerViewAdapter(context, adapter)
    }

    @IntoMap
    @IntKey(BaseListView.TYPE_DEFAULT)
    @Provides
    fun providesNavigationViewHolderFactory(): ViewHolderFactory<NavigationDataModel> {
        return NavigationViewHolderFactory()
    }

    @Provides
    fun providesFavoritesTracker(firebaseAnalytics: FirebaseAnalytics): FavoritesTracker {
        return FavoritesTrackerImpl(firebaseAnalytics)
    }
}