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

package com.github.vase4kin.teamcityapp.build_details.data

import com.github.vase4kin.teamcityapp.agents.api.Agent
import com.github.vase4kin.teamcityapp.api.Repository
import com.github.vase4kin.teamcityapp.base.loading.OnLoadingListener
import com.github.vase4kin.teamcityapp.runbuild.interactor.LoadingListenerWithForbiddenSupport
import com.github.vase4kin.teamcityapp.runbuild.interactor.RunBuildInteractor
import com.github.vase4kin.teamcityapp.runbuild.interactor.RunBuildInteractorImpl
import teamcityapp.features.properties.repository.models.Properties

/** Native BuildDetails restart resolves the currently loaded configuration at invocation time. */
class BuildDetailsRunBuildInteractor internal constructor(
    private val arguments: BuildDetailsArguments,
    private val create: (String) -> RunBuildInteractor
) : RunBuildInteractor {
    constructor(repository: Repository, arguments: BuildDetailsArguments) : this(arguments, { RunBuildInteractorImpl(repository, it) })
    private var configurationId: String? = null
    private var delegate: RunBuildInteractor? = null
    private fun current(): RunBuildInteractor {
        val id = arguments.current.buildTypeId
        if (configurationId != id || delegate == null) {
            delegate?.unsubscribe()
            configurationId = id
            delegate = create(id)
        }
        return requireNotNull(delegate)
    }
    override fun queueBuild(branchName: String, agent: Agent?, isPersonal: Boolean, queueToTheTop: Boolean, cleanAllFiles: Boolean, properties: Properties, loadingListener: LoadingListenerWithForbiddenSupport<String>) = current().queueBuild(branchName, agent, isPersonal, queueToTheTop, cleanAllFiles, properties, loadingListener)
    override fun queueBuild(branchName: String?, properties: Properties?, loadingListener: LoadingListenerWithForbiddenSupport<String>) = current().queueBuild(branchName, properties, loadingListener)
    override fun loadAgents(loadingListener: OnLoadingListener<List<Agent>>) = current().loadAgents(loadingListener)
    override fun unsubscribe() {
        delegate?.unsubscribe()
        delegate = null
        configurationId = null
    }
}
