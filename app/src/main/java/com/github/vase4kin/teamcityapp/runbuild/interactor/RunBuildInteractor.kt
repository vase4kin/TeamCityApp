/*
 * Copyright 2020 Andrey Tolpeev
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
import teamcityapp.features.properties.repository.models.Properties

/**
 * Forbidden code error
 */
const val CODE_FORBIDDEN = 403
/**
 * Extra bundle key
 */
const val EXTRA_BUILD_TYPE_ID = "BuildTypeId"

/**
 * Run build interactor
 */
interface RunBuildInteractor {

    /**
     * Queue new build
     *
     * @param branchName - with branch name
     * @param agent - agent run with
     * @param isPersonal - personal build
     * @param queueToTheTop - queue build to the top
     * @param cleanAllFiles - clean all files in the checkout directory
     * @param properties - properties to use for new build
     * @param loadingListener - listener to receive callbacks
     */
    fun queueBuild(
        branchName: String,
        agent: Agent?,
        isPersonal: Boolean,
        queueToTheTop: Boolean,
        cleanAllFiles: Boolean,
        properties: Properties,
        loadingListener: LoadingListenerWithForbiddenSupport<String>
    )

    /**
     * Queue new build with parameters
     *
     * @param branchName - with branch name
     * @param properties - properties to use for new build
     * @param loadingListener - listener to receive callbacks
     */
    fun queueBuild(
        branchName: String?,
        properties: Properties?,
        loadingListener: LoadingListenerWithForbiddenSupport<String>
    )

    /**
     * Unsubscribe all rx subscriptions
     */
    fun unsubscribe()

    /**
     * Load list of agents
     *
     * @param loadingListener - listener to receive load callbacks
     */
    fun loadAgents(loadingListener: OnLoadingListener<List<Agent>>)
}
