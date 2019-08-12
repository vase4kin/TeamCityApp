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

package com.github.vase4kin.teamcityapp.runbuild.interactor

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener
import com.github.vase4kin.teamcityapp.agents.api.Agent
import com.github.vase4kin.teamcityapp.api.Repository
import com.github.vase4kin.teamcityapp.buildlist.api.Build
import com.github.vase4kin.teamcityapp.properties.api.Properties
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException

/**
 * Impl of [RunBuildInteractor]
 */
class RunBuildInteractorImpl(
        private val repository: Repository,
        private val buildTypeId: String
) : RunBuildInteractor {
    /**
     * To handle rx subscriptions
     */
    private val subscriptions = CompositeDisposable()

    /**
     * {@inheritDoc}
     */
    override fun queueBuild(branchName: String,
                            agent: Agent?,
                            isPersonal: Boolean,
                            queueToTheTop: Boolean,
                            cleanAllFiles: Boolean,
                            properties: Properties,
                            loadingListener: LoadingListenerWithForbiddenSupport<String>) {
        val build = Build()
        build.branchName = branchName
        build.buildTypeId = buildTypeId
        build.isPersonal = isPersonal
        build.isQueueAtTop = queueToTheTop
        build.isCleanSources = cleanAllFiles
        if (agent != null) {
            build.agent = agent
        }
        if (!properties.objects.isEmpty()) {
            build.properties = properties
        }
        queueBuild(build, loadingListener)
    }

    /**
     * {@inheritDoc}
     */
    override fun queueBuild(branchName: String?, properties: Properties?, loadingListener: LoadingListenerWithForbiddenSupport<String>) {
        val build = Build()
        build.buildTypeId = buildTypeId
        build.branchName = branchName
        build.properties = properties
        queueBuild(build, loadingListener)
    }

    /**
     * Queue build
     *
     * @param build           - Build to queue
     * @param loadingListener - listener to receive callbacks on UI
     */
    private fun queueBuild(build: Build, loadingListener: LoadingListenerWithForbiddenSupport<String>) {
        repository.queueBuild(build)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                        onSuccess = { loadingListener.onSuccess(it.href) },
                        onError = { e ->
                            if (e is HttpException) {
                                if (e.code() == CODE_FORBIDDEN) {
                                    loadingListener.onForbiddenError()
                                } else {
                                    loadingListener.onFail(e.message ?: "")
                                }
                            } else {
                                loadingListener.onFail(e.message ?: "")
                            }
                        }
                )
                .addTo(subscriptions)
    }

    /**
     * {@inheritDoc}
     */
    override fun unsubscribe() {
        subscriptions.clear()
    }

    /**
     * {@inheritDoc}
     */
    override fun loadAgents(loadingListener: OnLoadingListener<List<Agent>>) {
        repository.buildType(buildTypeId, false)
                .subscribeOn(Schedulers.io())
                .flatMap { buildType ->
                    val isCompatibleAgentsSupported = buildType.compatibleAgents != null
                    if (isCompatibleAgentsSupported) {
                        // TODO: No hardcode
                        val locator = String.format("compatible:(buildType:(id:%s))", buildTypeId)
                        repository.listAgents(null, null, locator, false)
                    } else {
                        repository.listAgents(false, null, null, false)
                    }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                        onSuccess = { loadingListener.onSuccess(it.objects) },
                        onError = { loadingListener.onFail(it.message ?: "") }
                )
                .addTo(subscriptions)
    }
}
