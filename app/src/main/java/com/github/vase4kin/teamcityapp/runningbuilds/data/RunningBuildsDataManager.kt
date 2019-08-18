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

package com.github.vase4kin.teamcityapp.runningbuilds.data

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener
import com.github.vase4kin.teamcityapp.buildlist.data.BuildListDataManager
import com.github.vase4kin.teamcityapp.overview.data.BuildDetails

/**
 * Data manager handles running builds server operations
 */
interface RunningBuildsDataManager : BuildListDataManager {

    /**
     * Load running builds
     *
     * @param update - Force cache update
     * @param loadingListener - Listener to receive server callbacks
     */
    fun load(loadingListener: OnLoadingListener<List<BuildDetails>>, update: Boolean)

    /**
     * Load favorite running builds
     *
     * @param update - Force cache update
     * @param loadingListener - Listener to receive server callbacks
     */
    fun loadFavorites(loadingListener: OnLoadingListener<List<BuildDetails>>, update: Boolean)

    /**
     * Load running builds count
     *
     * @param loadingListener - Listener to receive server callbacks
     */
    fun loadCount(loadingListener: OnLoadingListener<Int>)

    /**
     * Load favorite running builds count
     *
     * @param loadingListener - Listener to receive server callbacks
     */
    fun loadFavoritesCount(loadingListener: OnLoadingListener<Int>)

    fun buildTypeIdLocator(buildTypeId: String): String = "buildType:$buildTypeId"
}
