/*
 * Copyright 2019 Andrey Tolpeev
 * Copyright 2026 Andrey Tolpeev
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

import com.github.vase4kin.teamcityapp.api.Repository
import com.github.vase4kin.teamcityapp.base.loading.OnLoadingListener
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

/** Compatibility boundary for Home's callback badges until its native shell state is migrated. */
class HomeBuildCounts(private val repository: Repository, private val storage: SharedUserStorage) {
    private val running = CompositeDisposable()
    private val queue = CompositeDisposable()

    fun loadRunning(listener: OnLoadingListener<Int>, favorites: Boolean = false) {
        running.clear()
        val count = if (favorites) {
            favoriteCount { id -> repository.listRunningBuilds("$RUNNING_LOCATOR,buildType:$id", "count", false).map { it.count } }
        } else {
            repository.listRunningBuilds(RUNNING_LOCATOR, "count", false).map { it.count }
        }
        load(count, listener, running)
    }

    fun loadQueue(listener: OnLoadingListener<Int>, favorites: Boolean = false) {
        queue.clear()
        val count = if (favorites) {
            favoriteCount { id -> repository.listQueueBuilds("buildType:$id", "count", false).map { it.count } }
        } else {
            repository.listQueueBuilds(null, "count", false).map { it.count }
        }
        load(count, listener, queue)
    }

    private fun favoriteCount(count: (String) -> Single<Int>): Single<Int> {
        val ids = storage.favoriteBuildTypeIds.toList()
        return Observable.fromIterable(ids).flatMapSingle { count(it) }.toList().map { it.sum() }
    }

    private fun load(count: Single<Int>, listener: OnLoadingListener<Int>, subscriptions: CompositeDisposable) {
        count.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(onSuccess = listener::onSuccess, onError = { listener.onSuccess(0) })
            .addTo(subscriptions)
    }

    fun unsubscribe() {
        running.clear()
        queue.clear()
    }

    private companion object {
        const val RUNNING_LOCATOR = "running:true,branch:default:any,personal:false,pinned:false"
    }
}
