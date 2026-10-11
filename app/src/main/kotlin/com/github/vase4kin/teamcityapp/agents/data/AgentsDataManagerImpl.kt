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

package com.github.vase4kin.teamcityapp.agents.data

import com.github.vase4kin.teamcityapp.api.Repository
import com.github.vase4kin.teamcityapp.base.loading.OnLoadingListener
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

/** Keeps Home's callback count contract until Home's state is migrated. */
class AgentsDataManagerImpl(private val repository: Repository) : AgentsDataManager {
    private val subscriptions = CompositeDisposable()

    override fun loadCount(loadingListener: OnLoadingListener<Int>, includeDisconnected: Boolean?) {
        subscriptions.clear()
        repository.listAgents(includeDisconnected, "count", null, false)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = { loadingListener.onSuccess(it.count) },
                onError = { loadingListener.onSuccess(0) }
            ).addTo(subscriptions)
    }

    override fun unsubscribe() = subscriptions.clear()
}
