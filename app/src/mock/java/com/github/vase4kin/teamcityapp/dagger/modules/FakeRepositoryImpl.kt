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

package com.github.vase4kin.teamcityapp.dagger.modules

import com.github.vase4kin.teamcityapp.about.ServerInfo
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
import okhttp3.ResponseBody
import teamcity.features.test_details.api.models.TestOccurrence

/**
 * Fake repository impl for UI tests without cache support
 */
class FakeRepositoryImpl(private val teamCityService: TeamCityService) : Repository {

    override fun listAgents(
        includeDisconnected: Boolean?,
        fields: String?,
        locator: String?,
        update: Boolean
    ): Single<Agents> {
        return teamCityService.listAgents(includeDisconnected, fields, locator)
    }

    override fun listBuildTypes(id: String, update: Boolean): Single<NavigationNode> {
        return teamCityService.listBuildTypes(id)
    }

    override fun buildType(id: String, update: Boolean): Single<BuildType> {
        return teamCityService.buildType(id)
    }

    override fun build(url: String, update: Boolean): Single<Build> {
        return teamCityService.build(url)
    }

    override fun listBuilds(id: String, locator: String, update: Boolean): Single<Builds> {
        return teamCityService.listBuilds(id, locator)
    }

    override fun listRunningBuilds(
        locator: String,
        fields: String?,
        update: Boolean
    ): Single<Builds> {
        return teamCityService.listRunningBuilds(locator, fields)
    }

    override fun listQueueBuilds(
        locator: String?,
        fields: String?,
        update: Boolean
    ): Single<Builds> {
        return teamCityService.listQueueBuilds(locator, fields)
    }

    override fun listSnapshotBuilds(id: String, update: Boolean): Single<Builds> {
        return teamCityService.listBuilds(id)
    }

    override fun listMoreBuilds(url: String): Single<Builds> {
        return teamCityService.listMoreBuilds(url)
    }

    override fun listArtifacts(url: String, locator: String, update: Boolean): Single<Files> {
        return teamCityService.listArtifacts(url, locator)
    }

    override fun downloadFile(url: String): Single<ResponseBody> {
        return teamCityService.downloadFile(url)
    }

    override fun listTestOccurrences(url: String, update: Boolean): Single<TestOccurrences> {
        return teamCityService.listTestOccurrences(url)
    }

    override fun testOccurrence(url: String): Single<TestOccurrence> {
        return teamCityService.testOccurrence(url)
    }

    override fun listChanges(url: String, update: Boolean): Single<Changes> {
        return teamCityService.listChanges(url)
    }

    override fun change(url: String): Single<Changes.Change> {
        return teamCityService.change(url)
    }

    override fun listBranches(buildTypeId: String): Single<Branches> {
        return teamCityService.listBranches(buildTypeId)
    }

    override fun queueBuild(build: Build): Single<Build> {
        return teamCityService.queueBuild(build)
    }

    override fun cancelBuild(url: String, buildCancelRequest: BuildCancelRequest): Single<Build> {
        return teamCityService.cancelBuild(url, buildCancelRequest)
    }

    override fun serverInfo(): Single<ServerInfo> {
        return teamCityService.serverInfo()
    }
}
