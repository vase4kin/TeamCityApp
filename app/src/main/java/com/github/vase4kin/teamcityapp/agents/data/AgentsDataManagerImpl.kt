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

package com.github.vase4kin.teamcityapp.agents.data

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener
import com.github.vase4kin.teamcityapp.agents.api.Agent
import com.github.vase4kin.teamcityapp.agents.api.Agents
import com.github.vase4kin.teamcityapp.api.Repository
import com.github.vase4kin.teamcityapp.base.list.data.BaseListRxDataManagerImpl
import com.github.vase4kin.teamcityapp.base.tabs.data.OnTextTabChangeEvent
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

import org.greenrobot.eventbus.EventBus

/**
 * Impl of [AgentsDataManager]
 */
class AgentsDataManagerImpl(
        private val repository: Repository,
        private val eventBus: EventBus
) : BaseListRxDataManagerImpl<Agents, Agent>(), AgentsDataManager {

    /**
     * {@inheritDoc}
     */
    override fun load(includeDisconnected: Boolean?,
                      loadingListener: OnLoadingListener<List<Agent>>,
                      update: Boolean) {
        load(repository.listAgents(includeDisconnected, null, null, update), loadingListener)
    }

    /**
     * {@inheritDoc}
     */
    override fun loadCount(loadingListener: OnLoadingListener<Int>) {
        subscriptions.clear()
        repository.listAgents(null, "count", null, false)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                        onSuccess = { loadingListener.onSuccess(it.count) },
                        onError = { loadingListener.onSuccess(0) }
                )
                .addTo(subscriptions)
    }

    /**
     * {@inheritDoc}
     */
    override fun postUpdateTabTitleEvent(size: Int, type: Int) {
        eventBus.post(OnTextTabChangeEvent(size, type))
    }
}
