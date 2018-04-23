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

package com.github.vase4kin.teamcityapp.favorites.dagger;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.Nullable;
import android.view.View;

import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.api.Repository;
import com.github.vase4kin.teamcityapp.base.list.view.BaseListView;
import com.github.vase4kin.teamcityapp.base.list.view.SimpleSectionedRecyclerViewAdapter;
import com.github.vase4kin.teamcityapp.base.list.view.ViewHolderFactory;
import com.github.vase4kin.teamcityapp.buildlist.filter.BuildListFilter;
import com.github.vase4kin.teamcityapp.favorites.interactor.FavoritesInteractor;
import com.github.vase4kin.teamcityapp.favorites.interactor.FavoritesInteractorImpl;
import com.github.vase4kin.teamcityapp.favorites.tracker.FavoritesTracker;
import com.github.vase4kin.teamcityapp.favorites.tracker.FavoritesTrackerImpl;
import com.github.vase4kin.teamcityapp.favorites.view.FavoritesView;
import com.github.vase4kin.teamcityapp.favorites.view.FavoritesViewImpl;
import com.github.vase4kin.teamcityapp.navigation.data.NavigationDataModel;
import com.github.vase4kin.teamcityapp.navigation.extractor.NavigationValueExtractor;
import com.github.vase4kin.teamcityapp.navigation.router.NavigationRouter;
import com.github.vase4kin.teamcityapp.navigation.router.NavigationRouterImpl;
import com.github.vase4kin.teamcityapp.navigation.view.NavigationAdapter;
import com.github.vase4kin.teamcityapp.navigation.view.NavigationViewHolderFactory;
import com.github.vase4kin.teamcityapp.overview.data.BuildDetails;
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.Map;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntKey;
import dagger.multibindings.IntoMap;

@Module
public class FavoritesModule {

    private View mView;
    private Activity mActivity;

    public FavoritesModule(View view, Activity mActivity) {
        this.mView = view;
        this.mActivity = mActivity;
    }

    @Provides
    FavoritesView providesNavigationView(SimpleSectionedRecyclerViewAdapter<NavigationAdapter> adapter) {
        return new FavoritesViewImpl(mView, mActivity, R.string.empty_list_message_favorites, adapter);
    }

    @Provides
    NavigationRouter providesNavigationRouter() {
        return new NavigationRouterImpl(mActivity);
    }

    @Provides
    NavigationValueExtractor providesNavigationValueExtractor() {
        return new NavigationValueExtractor() {
            @Override
            public String getName() {
                return null;
            }

            @Override
            public String getId() {
                return null;
            }

            @Override
            public BuildDetails getBuildDetails() {
                return null;
            }

            @Nullable
            @Override
            public BuildListFilter getBuildListFilter() {
                return null;
            }

            @Override
            public boolean isBundleNull() {
                return false;
            }
        };
    }

    @Provides
    FavoritesInteractor providesFavoritesInteractor(Repository repository, SharedUserStorage storage) {
        return new FavoritesInteractorImpl(repository, storage);
    }

    @Provides
    NavigationAdapter providesNavigationAdapter(Map<Integer, ViewHolderFactory<NavigationDataModel>> viewHolderFactories) {
        return new NavigationAdapter(viewHolderFactories);
    }

    @Provides
    SimpleSectionedRecyclerViewAdapter<NavigationAdapter> providesSimpleSectionedRecyclerViewAdapter(Context context, NavigationAdapter adapter) {
        return new SimpleSectionedRecyclerViewAdapter<>(context, adapter);
    }

    @IntoMap
    @IntKey(BaseListView.TYPE_DEFAULT)
    @Provides
    ViewHolderFactory<NavigationDataModel> providesNavigationViewHolderFactory() {
        return new NavigationViewHolderFactory();
    }

    @Provides
    FavoritesTracker providesFavoritesTracker(FirebaseAnalytics firebaseAnalytics) {
        return new FavoritesTrackerImpl(firebaseAnalytics);
    }

}
