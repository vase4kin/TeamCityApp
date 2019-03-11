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

package com.github.vase4kin.teamcityapp.favorites.interactor

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener
import com.github.vase4kin.teamcityapp.api.Repository
import com.github.vase4kin.teamcityapp.api.interfaces.Collectible
import com.github.vase4kin.teamcityapp.base.list.data.BaseListRxDataManagerImpl
import com.github.vase4kin.teamcityapp.navigation.api.BuildType
import com.github.vase4kin.teamcityapp.navigation.api.NavigationItem
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage
import io.reactivex.Observable
import io.reactivex.Single

private val EMPTY_BUILDTYPE = BuildType()

/**
 * Impl of [FavoritesInteractor]
 */
class FavoritesInteractorImpl(private val repository: Repository,
                              private val storage: SharedUserStorage
) : BaseListRxDataManagerImpl<FavoritesInteractorImpl.NavigationItemsList, NavigationItem>(), FavoritesInteractor {

    /**
     * {@inheritDoc}
     */
    override fun loadFavorites(loadingListener: OnLoadingListener<List<NavigationItem>>, update: Boolean) {
        val ids = storage.activeUser.favoriteBuildTypes.keys
        val favoritesObservable = Observable.fromIterable(ids)
                .flatMapSingle { id ->
                    repository.buildType(id, update)
                            .onErrorResumeNext { Single.just(EMPTY_BUILDTYPE) }
                }
                .filter { it != EMPTY_BUILDTYPE }
                .toSortedList { buildType1, buildType2 ->
                    buildType1.projectId.compareTo(buildType2.projectId, ignoreCase = true)
                }
                .flatMapObservable { Observable.fromIterable(it).cast(NavigationItem::class.java) }
                .toList()
                .map { NavigationItemsList(it) }
        load(favoritesObservable, loadingListener)
    }

    /**
     * {@inheritDoc}
     */
    override fun getFavoritesCount(): Int {
        return storage.activeUser.favoriteBuildTypes.size
    }

    class NavigationItemsList(private val items: List<NavigationItem>) : Collectible<NavigationItem> {

        override fun getObjects(): List<NavigationItem> {
            return items
        }
    }
}
