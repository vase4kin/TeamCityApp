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

package com.github.vase4kin.teamcityapp.di.modules

import com.github.vase4kin.teamcityapp.agents.api.Agents
import com.github.vase4kin.teamcityapp.api.Repository
import com.github.vase4kin.teamcityapp.api.TeamCityService
import com.github.vase4kin.teamcityapp.artifact.api.Files
import com.github.vase4kin.teamcityapp.build_details.api.BuildCancelRequest
import com.github.vase4kin.teamcityapp.buildlist.api.Build
import com.github.vase4kin.teamcityapp.buildlist.api.Builds
import com.github.vase4kin.teamcityapp.changes.api.Changes
import com.github.vase4kin.teamcityapp.navigation.api.BuildType
import com.github.vase4kin.teamcityapp.navigation.api.NavigationNode
import com.github.vase4kin.teamcityapp.runbuild.api.Branches
import com.github.vase4kin.teamcityapp.tests.api.TestOccurrences
import io.reactivex.Single
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.rx2.await
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import teamcityapp.features.about.api.AboutServerInfo
import teamcityapp.features.test_details.repository.models.TestOccurrence
import teamcityapp.libraries.coroutines.IoDispatcher

/**
 * Fake repository impl for UI tests without cache support
 */
class FakeRepositoryImpl(
    private val teamCityService: TeamCityService,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : Repository {

    override fun listAgents(
        includeDisconnected: Boolean?,
        fields: String?,
        locator: String?,
        update: Boolean
    ): Single<Agents> = teamCityService.listAgents(includeDisconnected, fields, locator)

    override fun listBuildTypes(id: String, update: Boolean): Single<NavigationNode> = teamCityService.listBuildTypes(id)

    override fun buildType(id: String, update: Boolean): Single<BuildType> = teamCityService.buildType(id)

    override fun build(url: String, update: Boolean): Single<Build> = teamCityService.build(url)

    override fun listBuilds(id: String, locator: String, update: Boolean): Single<Builds> = teamCityService.listBuilds(id, locator)

    override fun listRunningBuilds(
        locator: String,
        fields: String?,
        update: Boolean
    ): Single<Builds> = teamCityService.listRunningBuilds(locator, fields)

    override fun listQueueBuilds(
        locator: String?,
        fields: String?,
        update: Boolean
    ): Single<Builds> = teamCityService.listQueueBuilds(locator, fields)

    override fun listSnapshotBuilds(id: String, update: Boolean): Single<Builds> = teamCityService.listBuilds(id)

    override fun listMoreBuilds(url: String): Single<Builds> = teamCityService.listMoreBuilds(url)

    override fun listArtifacts(url: String, locator: String, update: Boolean): Single<Files> = teamCityService.listArtifacts(url, locator)

    override fun downloadFile(url: String): Single<ResponseBody> = teamCityService.downloadFile(url)

    override fun listTestOccurrences(url: String, update: Boolean): Single<TestOccurrences> = teamCityService.listTestOccurrences(url)

    override fun testOccurrence(url: String): Single<TestOccurrence> = teamCityService.testOccurrence(url)

    override fun listChanges(url: String, update: Boolean): Single<Changes> = teamCityService.listChanges(url)

    override fun change(url: String): Single<Changes.Change> = teamCityService.change(url)

    override fun listBranches(buildTypeId: String): Single<Branches> = teamCityService.listBranches(buildTypeId)

    override fun queueBuild(build: Build): Single<Build> = teamCityService.queueBuild(build)

    override fun cancelBuild(url: String, buildCancelRequest: BuildCancelRequest): Single<Build> = teamCityService.cancelBuild(url, buildCancelRequest)

    override suspend fun testDetails(url: String): String = withContext(ioDispatcher) {
        // Coroutine cancellation disposes the existing RxCache/API subscription.
        testOccurrence(url).await().details.orEmpty()
    }

    override suspend fun serverInfo(): AboutServerInfo = withContext(ioDispatcher) {
        teamCityService.serverInfo().await().let { AboutServerInfo(it.version, it.webUrl) }
    }
}
