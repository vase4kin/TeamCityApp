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

package com.github.vase4kin.teamcityapp.favorites.interactor;

import android.support.annotation.NonNull;

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener;
import com.github.vase4kin.teamcityapp.api.Repository;
import com.github.vase4kin.teamcityapp.api.interfaces.Collectible;
import com.github.vase4kin.teamcityapp.base.list.data.BaseListRxDataManagerImpl;
import com.github.vase4kin.teamcityapp.navigation.api.BuildType;
import com.github.vase4kin.teamcityapp.navigation.api.NavigationItem;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.functions.Func1;

/**
 * Impl of {@link FavoritesInteractor}
 */
public class FavoritesInteractorImpl extends BaseListRxDataManagerImpl<FavoritesInteractorImpl.NavigationItemsList, NavigationItem> implements FavoritesInteractor {

    private final Repository repository;

    public FavoritesInteractorImpl(Repository repository) {
        this.repository = repository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadFavorites(@NonNull OnLoadingListener<List<NavigationItem>> loadingListener, final boolean update) {
        List<String> ids = new ArrayList<>();
        ids.add("ApacheAnt_BuildAntUsingMave1");
        ids.add("bt132");
        ids.add("bt133");
        Observable<NavigationItemsList> favoritesObservable = Observable.from(ids)
                .flatMap(new Func1<String, Observable<NavigationItem>>() {
                    @Override
                    public Observable<NavigationItem> call(String id) {
                        return repository.buildType(id, update)
                                .onErrorResumeNext(new Func1<Throwable, Observable<? extends BuildType>>() {
                                    @Override
                                    public Observable<? extends BuildType> call(Throwable throwable) {
                                        return Observable.empty();
                                    }
                                })
                                .flatMap(new Func1<BuildType, Observable<NavigationItem>>() {
                                    @Override
                                    public Observable<NavigationItem> call(BuildType buildType) {
                                        return Observable.just((NavigationItem) buildType);
                                    }
                                });
                    }
                })
                .toList()
                .flatMap(new Func1<List<NavigationItem>, Observable<NavigationItemsList>>() {
                    @Override
                    public Observable<NavigationItemsList> call(List<NavigationItem> navigationItems) {
                        return Observable.just(new NavigationItemsList(navigationItems));
                    }
                });
        load(favoritesObservable, loadingListener);
    }

    static class NavigationItemsList implements Collectible<NavigationItem> {

        private final List<NavigationItem> items;

        NavigationItemsList(List<NavigationItem> items) {
            this.items = items;
        }

        @Override
        public List<NavigationItem> getObjects() {
            return items;
        }
    }
}
