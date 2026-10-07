/*
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

package com.github.vase4kin.teamcityapp.runbuild.data

import com.github.vase4kin.teamcityapp.agents.api.Agent
import com.github.vase4kin.teamcityapp.api.Repository
import com.github.vase4kin.teamcityapp.buildlist.api.Build
import javax.inject.Inject
import javax.inject.Provider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.rx2.await
import retrofit2.HttpException
import teamcityapp.features.filter_builds.api.FilterBuildsRepository
import teamcityapp.features.properties.repository.models.Properties
import teamcityapp.features.run_build.api.*

/** Resolve the account repository per operation. Await also cancels the legacy Rx subscription. */
class ComposeRunBuildRepository @Inject constructor(private val repositories: Provider<Repository>) :
    RunBuildRepository,
    FilterBuildsRepository {
    override suspend fun branches(buildTypeId: String): List<String> = repositories.get().listBranches(buildTypeId).await().branches.map { it.name }
    override suspend fun agents(buildTypeId: String): List<BuildAgent> {
        val repository = repositories.get()
        val type = repository.buildType(buildTypeId, false).await()
        val locator = if (type.compatibleAgents != null) "compatible:(buildType:(id:$buildTypeId))" else null
        return repository.listAgents(if (locator == null) false else null, null, locator, false).await().objects.map { BuildAgent(it.id.orEmpty(), it.name) }
    }
    override suspend fun queue(request: BuildRequest): QueueBuildResult = try {
        QueueBuildResult.Success(repositories.get().queueBuild(request.toLegacyBuild()).await().href)
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (error: HttpException) {
        if (error.code() == 403) QueueBuildResult.Forbidden else QueueBuildResult.Error
    } catch (_: Exception) {
        QueueBuildResult.Error
    }
}

internal fun BuildRequest.toLegacyBuild() = Build().also { build ->
    build.buildTypeId = buildTypeId
    build.branchName = branch
    build.isPersonal = personal
    build.isQueueAtTop = queueAtTop
    build.isCleanSources = cleanSources
    agent?.let { selected -> build.agent = Agent(selected.name).also { it.id = selected.id } }
    if (parameters.isNotEmpty()) build.properties = Properties(parameters.map { Properties.Property(it.name, it.value) })
}
