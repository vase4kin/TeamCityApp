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

import android.webkit.CookieManager
import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener
import com.github.vase4kin.teamcityapp.agents.data.AgentsDataManagerImpl
import com.github.vase4kin.teamcityapp.api.Repository
import com.github.vase4kin.teamcityapp.queue.data.BuildQueueDataManagerImpl
import com.github.vase4kin.teamcityapp.runningbuilds.data.RunningBuildsDataManagerImpl
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import teamcityapp.cache_manager.CacheManager
import teamcityapp.libraries.storage.models.UserAccount

/**
 * Impl of [HomeDataManager]
 */
class HomeDataManagerImpl(
    repository: Repository,
    private val sharedUserStorage: SharedUserStorage,
    private val cacheManager: CacheManager,
    private val eventBus: EventBus
) : HomeDataManager {

    private val runningBuildsDataManager =
        RunningBuildsDataManagerImpl(repository, sharedUserStorage)
    private val queuedBuildsDataManager = BuildQueueDataManagerImpl(repository, sharedUserStorage)
    private val agentsDataManager = AgentsDataManagerImpl(repository, eventBus)

    private var listener: HomeDataManager.Listener? = null

    override val isModelEmpty: Boolean
        get() = sharedUserStorage.hasUserAccounts().not()
    /**
     * {@inheritDoc}
     */
    override val activeUser: UserAccount
        get() = sharedUserStorage.activeUser

    /**
     * {@inheritDoc}
     */
    override val favoritesCount: Int
        get() = sharedUserStorage.favoriteBuildTypeIds.size

    /**
     * {@inheritDoc}
     */
    override fun clearAllWebViewCookies() {
        CookieManager.getInstance().removeAllCookie()
    }

    /**
     * {@inheritDoc}
     */
    override fun evictAllCache() {
        cacheManager.evictAllCache()
    }

    /**
     * {@inheritDoc}
     */
    override fun setListener(listener: HomeDataManager.Listener?) {
        this.listener = listener
    }

    override fun subscribeToEventBusEvents() {
        eventBus.register(this)
    }

    override fun unsubscribeOfEventBusEvents() {
        eventBus.unregister(this)
    }

    /**
     * {@inheritDoc}
     */
    override fun loadRunningBuildsCount(loadingListener: OnLoadingListener<Int>) {
        runningBuildsDataManager.loadCount(loadingListener)
    }

    /**
     * {@inheritDoc}
     */
    override fun loadFavoriteRunningBuildsCount(loadingListener: OnLoadingListener<Int>) {
        runningBuildsDataManager.loadFavoritesCount(loadingListener)
    }

    /**
     * {@inheritDoc}
     */
    override fun loadBuildQueueCount(loadingListener: OnLoadingListener<Int>) {
        queuedBuildsDataManager.loadCount(loadingListener)
    }

    /**
     * {@inheritDoc}
     */
    override fun loadFavoriteBuildQueueCount(loadingListener: OnLoadingListener<Int>) {
        queuedBuildsDataManager.loadFavoritesCount(loadingListener)
    }

    /**
     * {@inheritDoc}
     */
    override fun unsubscribe() {
        runningBuildsDataManager.unsubscribe()
        queuedBuildsDataManager.unsubscribe()
    }

    /**
     * {@inheritDoc}
     */
    override fun postRunningBuildsFilterChangedEvent() {
        eventBus.post(HomeDataManager.RunningBuildsFilterChangedEvent())
    }

    /**
     * {@inheritDoc}
     */
    override fun postBuildQueueFilterChangedEvent() {
        eventBus.post(HomeDataManager.BuildQueueFilterChangedEvent())
    }

    /**
     * {@inheritDoc}
     */
    override fun postAgentsFilterChangedEvent() {
        eventBus.post(HomeDataManager.AgentsFilterChangedEvent())
    }

    override fun loadAgentsCount(
        loadingListener: OnLoadingListener<Int>,
        includeDisconnected: Boolean
    ) {
        agentsDataManager.loadCount(loadingListener, includeDisconnected)
    }

    /***
     * Handle receiving post events from [EventBus]
     *
     * @param event [FilterAppliedEvent]
     */
    @Subscribe
    fun onEvent(event: FilterAppliedEvent) {
        listener?.onFilterApplied(event.filter)
    }
}
