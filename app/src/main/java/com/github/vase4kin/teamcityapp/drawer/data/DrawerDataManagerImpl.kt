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

package com.github.vase4kin.teamcityapp.drawer.data

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener
import com.github.vase4kin.teamcityapp.agents.data.AgentsDataManager
import com.github.vase4kin.teamcityapp.agents.data.AgentsDataManagerImpl
import com.github.vase4kin.teamcityapp.api.Repository
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage
import com.github.vase4kin.teamcityapp.storage.api.UserAccount
import org.greenrobot.eventbus.EventBus

/**
 * Impl of [DrawerDataManager]
 */
open class DrawerDataManagerImpl(
    repository: Repository,
    protected val sharedUserStorage: SharedUserStorage,
    eventBus: EventBus
) : DrawerDataManager {
    private val agentsDataManager: AgentsDataManager

    init {
        this.agentsDataManager = AgentsDataManagerImpl(repository, eventBus)
    }

    /**
     * {@inheritDoc}
     */
    override fun load(loadingListener: OnLoadingListener<List<UserAccount>>) {
        loadingListener.onSuccess(sharedUserStorage.userAccounts)
    }

    /**
     * {@inheritDoc}
     */
    override fun setActiveUser(url: String, userName: String) {
        sharedUserStorage.setUserActive(url, userName)
    }

    /**
     * {@inheritDoc}
     */
    override fun isActiveUser(url: String, userName: String): Boolean {
        val userAccount = sharedUserStorage.activeUser
        return userAccount.teamcityUrl == url && userAccount.userName == userName
    }

    /**
     * {@inheritDoc}
     */
    override fun loadConnectedAgentsCount(loadingListener: OnLoadingListener<Int>) {
        agentsDataManager.loadCount(loadingListener)
    }

    /**
     * {@inheritDoc}
     */
    override fun loadAgentsCount(
        loadingListener: OnLoadingListener<Int>,
        includeDisconnected: Boolean
    ) {
        agentsDataManager.loadCount(loadingListener, includeDisconnected)
    }

    /**
     * {@inheritDoc}
     */
    override fun unsubscribe() {
        agentsDataManager.unsubscribe()
    }
}
