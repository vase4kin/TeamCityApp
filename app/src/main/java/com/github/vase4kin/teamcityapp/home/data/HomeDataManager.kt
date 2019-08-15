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

package com.github.vase4kin.teamcityapp.home.data

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener
import com.github.vase4kin.teamcityapp.drawer.data.DrawerDataManager
import com.github.vase4kin.teamcityapp.filter_bottom_sheet_dialog.filter.Filter
import com.github.vase4kin.teamcityapp.storage.api.UserAccount

/**
 * Data manager to handle root data operations
 */
interface HomeDataManager : DrawerDataManager {

    /**
     * @return Active user account
     */
    val activeUser: UserAccount

    /**
     * @return the count of favorite build types
     */
    val favoritesCount: Int

    /**
     * Clear all webview cookies
     */
    fun clearAllWebViewCookies()

    /**
     * Evict all cache data
     */
    fun evictAllCache()

    /**
     * Set listener
     */
    fun setListener(listener: Listener?)

    /**
     * Subscribe to event bus events
     */
    fun subscribeToEventBusEvents()

    /**
     * Unsubsribe to event bus events
     */
    fun unsubscribeOfEventBusEvents()

    /**
     * Load the number of running builds
     *
     * @param loadingListener - Listener to receive callbacks
     */
    fun loadRunningBuildsCount(loadingListener: OnLoadingListener<Int>)

    /**
     * Load the number of favorite running builds
     *
     * @param loadingListener - Listener to receive callbacks
     */
    fun loadFavoriteRunningBuildsCount(loadingListener: OnLoadingListener<Int>)

    /**
     * Load the number queued builds
     *
     * @param loadingListener - Listener to receive callbacks
     */
    fun loadBuildQueueCount(loadingListener: OnLoadingListener<Int>)

    /**
     * Load the number favorite queued builds
     *
     * @param loadingListener - Listener to receive callbacks
     */
    fun loadFavoriteBuildQueueCount(loadingListener: OnLoadingListener<Int>)

    /**
     * Post @{[RunningBuildsFilterChangedEvent]}
     */
    fun postRunningBuildsFilterChangedEvent()

    /**
     * Post @{[BuildQueueFilterChangedEvent]}
     */
    fun postBuildQueueFilterChangedEvent()

    interface Listener {
        fun onFilterApplied(filter: Filter)
    }

    class RunningBuildsFilterChangedEvent

    class BuildQueueFilterChangedEvent
}
