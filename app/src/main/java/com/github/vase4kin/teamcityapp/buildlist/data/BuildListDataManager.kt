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

package com.github.vase4kin.teamcityapp.buildlist.data

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener
import com.github.vase4kin.teamcityapp.base.list.data.BaseListRxDataManager
import com.github.vase4kin.teamcityapp.buildlist.api.Build
import com.github.vase4kin.teamcityapp.buildlist.api.Builds
import com.github.vase4kin.teamcityapp.buildlist.filter.BuildListFilter
import com.github.vase4kin.teamcityapp.overview.data.BuildDetails

/**
 * Data manager for [com.github.vase4kin.teamcityapp.buildlist.view.BuildListActivity]
 */
interface BuildListDataManager : BaseListRxDataManager<Builds, Build> {

    /**
     * Load more builds
     *
     * @param loadingListener - Listener to receive callbacks on [com.github.vase4kin.teamcityapp.buildlist.presenter.BuildListPresenterImpl]
     */
    fun loadMore(loadingListener: OnLoadingListener<List<BuildDetails>>)

    /**
     * {@inheritDoc}
     */
    fun load(id: String,
             loadingListener: OnLoadingListener<List<BuildDetails>>,
             update: Boolean)

    /**
     * {@inheritDoc}
     */
    fun load(id: String,
             filter: BuildListFilter,
             loadingListener: OnLoadingListener<List<BuildDetails>>,
             update: Boolean)

    /**
     * Is there any more builds to load
     */
    fun canLoadMore(): Boolean

    /**
     * Add build type id to favorites
     *
     * @param buildTypeId - build type id
     */
    fun addToFavorites(buildTypeId: String)

    /**
     * Remove build type id from favorites
     *
     * @param buildTypeId - build type id
     */
    fun removeFromFavorites(buildTypeId: String)

    /**
     * @param buildTypeId - build type id
     *
     *
     * return {true} if user has this build type id
     */
    fun hasBuildTypeAsFavorite(buildTypeId: String): Boolean
}
