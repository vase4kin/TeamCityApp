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

package com.github.vase4kin.teamcityapp.home.data

import android.content.Context
import android.webkit.CookieManager
import com.github.vase4kin.teamcityapp.TeamCityApplication
import com.github.vase4kin.teamcityapp.api.Repository
import com.github.vase4kin.teamcityapp.drawer.data.DrawerDataManagerImpl
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage
import com.github.vase4kin.teamcityapp.storage.api.UserAccount
import io.rx_cache2.internal.RxCache
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

/**
 * Impl of [HomeDataManager]
 */
class HomeDataManagerImpl(private val context: Context,
                          repository: Repository,
                          sharedUserStorage: SharedUserStorage,
                          private val rxCache: RxCache,
                          private val eventBus: EventBus) : DrawerDataManagerImpl(repository, sharedUserStorage, eventBus), HomeDataManager {

    private var listener: HomeDataManager.Listener? = null

    /**
     * {@inheritDoc}
     */
    override fun getActiveUser(): UserAccount {
        return mSharedUserStorage.activeUser
    }

    /**
     * {@inheritDoc}
     */
    override fun initTeamCityService() {
        (context.applicationContext as TeamCityApplication).buildRestApiInjectorWithBaseUrl(mSharedUserStorage.activeUser.teamcityUrl)
    }

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
        rxCache.evictAll().subscribe()
    }

    /**
     * {@inheritDoc}
     */
    override fun setListener(listener: HomeDataManager.Listener?) {
        this.listener = listener
    }

    override fun onsubscribe() {
        eventBus.register(this)
    }

    override fun unsubscribe() {
        eventBus.unregister(this)
    }

    /***
     * Handle receiving post events from [EventBus]
     *
     * @param event [HomeDataManager.FilterAppliedEvent]
     */
    @Subscribe
    fun onEvent(event: HomeDataManager.FilterAppliedEvent) {
        listener?.onFilterApplied()
    }
}
