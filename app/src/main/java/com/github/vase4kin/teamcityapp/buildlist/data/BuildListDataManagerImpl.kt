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
import com.github.vase4kin.teamcityapp.api.Repository
import com.github.vase4kin.teamcityapp.base.list.data.BaseListRxDataManagerImpl
import com.github.vase4kin.teamcityapp.buildlist.api.Build
import com.github.vase4kin.teamcityapp.buildlist.api.Builds
import com.github.vase4kin.teamcityapp.buildlist.filter.BuildListFilter
import com.github.vase4kin.teamcityapp.overview.data.BuildDetails
import com.github.vase4kin.teamcityapp.overview.data.BuildDetailsImpl
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

/**
 * Impl of [BuildListDataManager]
 */
open class BuildListDataManagerImpl(
        protected val repository: Repository,
        private val sharedUserStorage: SharedUserStorage
) : BaseListRxDataManagerImpl<Builds, Build>(), BuildListDataManager {

    /**
     * Load more url
     */
    private var loadMoreUrl: String? = null

    /**
     * {@inheritDoc}
     */
    override fun load(id: String,
                      loadingListener: OnLoadingListener<List<BuildDetails>>,
                      update: Boolean) {
        loadBuilds(repository.listBuilds(id, BuildListFilter.DEFAULT_FILTER_LOCATOR, update), loadingListener, updateSinceBuild = true)
    }

    /**
     * {@inheritDoc}
     */
    override fun load(id: String,
                      filter: BuildListFilter,
                      loadingListener: OnLoadingListener<List<BuildDetails>>,
                      update: Boolean) {
        loadBuilds(repository.listBuilds(id, filter.toLocator(), update), loadingListener, updateSinceBuild = true)
    }

    /**
     * {@inheritDoc}
     */
    override fun canLoadMore(): Boolean {
        return loadMoreUrl != null
    }

    /**
     * {@inheritDoc}
     */
    override fun addToFavorites(buildTypeId: String) {
        sharedUserStorage.addBuildTypeToFavorites(buildTypeId)
    }

    /**
     * {@inheritDoc}
     */
    override fun removeFromFavorites(buildTypeId: String) {
        sharedUserStorage.removeBuildTypeFromFavorites(buildTypeId)
    }

    /**
     * {@inheritDoc}
     */
    override fun hasBuildTypeAsFavorite(buildTypeId: String): Boolean {
        return sharedUserStorage.activeUser.favoriteBuildTypes.containsKey(buildTypeId)
    }

    /**
     * {@inheritDoc}
     */
    private fun loadBuilds(call: Single<Builds>,
                           loadingListener: OnLoadingListener<List<BuildDetails>>,
                           updateSinceBuild: Boolean = false) {
        val buildDetailsList = getBuildDetailsObservable(call)
                // putting them all to the sorted list
                // where queued builds go first
                .toSortedList { build, build2 ->
                    when {
                        build.isQueued == build2.isQueued -> 0
                        build.isQueued -> -1
                        else -> 1
                    }
                }
        loadBuildDetailsList(buildDetailsList, loadingListener, updateSinceBuild)
    }

    protected fun loadBuildDetailsList(call: Single<List<BuildDetails>>,
                                       loadingListener: OnLoadingListener<List<BuildDetails>>,
                                       updateSinceBuild: Boolean = false) {
        subscriptions.clear()
        call
                .doOnSuccess { buildDetailsList ->
                    if (!updateSinceBuild) return@doOnSuccess
                    updateSinceBuild(buildDetailsList)
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                        onSuccess = { loadingListener.onSuccess(it) },
                        onError = { loadingListener.onFail(it.message) }
                )
                .addTo(subscriptions)
    }

    private fun updateSinceBuild(buildDetailsList: List<BuildDetails>) {
        val favoriteBuildTypes = sharedUserStorage.activeUser.favoriteBuildTypes
        if (favoriteBuildTypes.isEmpty()) return
        val failedList = buildDetailsList.filter { it.isFailed }
        if (failedList.isNotEmpty()) {
            val firstBuild = failedList.first()
            val buildTypeId = firstBuild.buildTypeId
            if (favoriteBuildTypes.containsKey(buildTypeId)) {
                val updatedInfo = favoriteBuildTypes.getValue(buildTypeId).copy(sinceBuild = firstBuild.id)
                favoriteBuildTypes[buildTypeId] = updatedInfo
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    protected fun loadNotSortedBuilds(call: Single<Builds>,
                                      loadingListener: OnLoadingListener<List<BuildDetails>>) {
        val buildDetailsList = getBuildDetailsObservable(call).toList()
        loadBuildDetailsList(buildDetailsList, loadingListener)
    }

    protected fun getBuildDetailsObservable(call: Single<Builds>): Observable<BuildDetails> {
        return call
                // converting all received builds to observables
                .flatMapObservable { builds ->
                    if (builds.count == 0) {
                        Observable.fromIterable(emptyList<Build>())
                    } else {
                        loadMoreUrl = builds.nextHref
                        Observable.fromIterable(builds.objects)
                    }
                }
                // returning new updated build observables for each stored build already
                .flatMapSingle { serverBuild -> repository.cacheBuild(serverBuild) }
                .map { BuildDetailsImpl(it) }
                .cast(BuildDetails::class.java)
    }

    /**
     * Load build count
     *
     * @param call            - Retrofit call
     * @param loadingListener - Listener to receive server callbacks
     */
    fun loadCount(call: Single<Builds>, loadingListener: OnLoadingListener<Int>) {
        subscriptions.clear()
        call
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
    override fun loadMore(loadingListener: OnLoadingListener<List<BuildDetails>>) {
        loadBuilds(repository.listMoreBuilds(loadMoreUrl!!), loadingListener)
    }
}
