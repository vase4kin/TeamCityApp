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

package com.github.vase4kin.teamcityapp.api

import com.github.vase4kin.teamcityapp.account.create.helper.UrlFormatter
import com.github.vase4kin.teamcityapp.agents.api.Agents
import com.github.vase4kin.teamcityapp.api.cache.CacheProviders
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
import io.rx_cache2.DynamicKey
import io.rx_cache2.DynamicKeyGroup
import io.rx_cache2.EvictDynamicKey
import io.rx_cache2.EvictDynamicKeyGroup
import okhttp3.ResponseBody
import teamcityapp.features.about.repository.models.ServerInfo
import teamcityapp.features.test_details.repository.models.TestOccurrence

/**
 * Impl of [Repository]
 */
class RepositoryImpl(
    private val teamCityService: TeamCityService,
    private val cacheProviders: CacheProviders,
    private val urlFormatter: UrlFormatter
) : Repository {

    /**
     * {@inheritDoc}
     */
    override fun listAgents(
        includeDisconnected: Boolean?,
        fields: String?,
        locator: String?,
        update: Boolean
    ): Single<Agents> {
        val dynamicKey = String.format(
            "includeDisconnected:%s,fields:%s,locator:%s",
            includeDisconnected ?: "empty",
            fields ?: "empty",
            locator ?: "empty"
        )
        return cacheProviders.listAgents(
            teamCityService.listAgents(includeDisconnected, fields, locator),
            DynamicKey(dynamicKey),
            EvictDynamicKey(update)
        )
    }

    /**
     * {@inheritDoc}
     */
    override fun listBuildTypes(id: String, update: Boolean): Single<NavigationNode> {
        return cacheProviders.listBuildTypes(
            teamCityService.listBuildTypes(id),
            DynamicKey(id),
            EvictDynamicKey(update)
        )
    }

    /**
     * {@inheritDoc}
     */
    override fun buildType(id: String, update: Boolean): Single<BuildType> {
        return cacheProviders.buildType(
            teamCityService.buildType(id),
            DynamicKey(id),
            EvictDynamicKey(update)
        )
    }

    /**
     * {@inheritDoc}
     */
    override fun build(url: String, update: Boolean): Single<Build> {
        return cacheProviders.build(
            teamCityService.build(urlFormatter.formatBasicUrl(url)),
            DynamicKey(url),
            EvictDynamicKey(update)
        )
    }

    /**
     * {@inheritDoc}
     */
    override fun listBuilds(id: String, locator: String, update: Boolean): Single<Builds> {
        return cacheProviders.listBuilds(
            teamCityService.listBuilds(id, locator),
            DynamicKeyGroup(locator, id),
            EvictDynamicKeyGroup(update)
        )
    }

    /**
     * {@inheritDoc}
     */
    override fun listRunningBuilds(locator: String, fields: String?, update: Boolean): Single<Builds> {
        return cacheProviders.listRunningBuilds(
            teamCityService.listRunningBuilds(locator, fields),
            DynamicKey(locator + fields),
            EvictDynamicKey(update)
        )
    }

    /**
     * {@inheritDoc}
     */
    override fun listQueueBuilds(locator: String?, fields: String?, update: Boolean): Single<Builds> {
        return cacheProviders.listQueuedBuilds(
            teamCityService.listQueueBuilds(locator, fields),
            DynamicKey(locator + fields),
            EvictDynamicKey(update)
        )
    }

    /**
     * {@inheritDoc}
     */
    override fun listSnapshotBuilds(id: String, update: Boolean): Single<Builds> {
        val locator = String.format("snapshotDependency:(to:(id:%s),includeInitial:true),defaultFilter:false", id)
        return cacheProviders.listSnapshotBuilds(
            teamCityService.listBuilds(locator),
            DynamicKey(id),
            EvictDynamicKey(update)
        )
    }

    /**
     * {@inheritDoc}
     */
    override fun listMoreBuilds(url: String): Single<Builds> {
        return teamCityService.listMoreBuilds(urlFormatter.formatBasicUrl(url))
    }

    /**
     * {@inheritDoc}
     */
    override fun listArtifacts(url: String, locator: String, update: Boolean): Single<Files> {
        return cacheProviders.listArtifacts(
            teamCityService.listArtifacts(urlFormatter.formatBasicUrl(url), locator),
            DynamicKey(url),
            EvictDynamicKey(update)
        )
    }

    /**
     * {@inheritDoc}
     */
    override fun downloadFile(url: String): Single<ResponseBody> {
        return teamCityService.downloadFile(urlFormatter.formatBasicUrl(url))
    }

    /**
     * {@inheritDoc}
     */
    override fun listTestOccurrences(url: String, update: Boolean): Single<TestOccurrences> {
        return cacheProviders.listTestOccurrences(
            teamCityService.listTestOccurrences(urlFormatter.formatBasicUrl(url)),
            DynamicKey(url),
            EvictDynamicKey(update)
        )
    }

    /**
     * {@inheritDoc}
     */
    override fun testOccurrence(url: String): Single<TestOccurrence> {
        return cacheProviders.testOccurrence(
            teamCityService.testOccurrence(urlFormatter.formatBasicUrl(url)),
            DynamicKey(url)
        )
    }

    /**
     * {@inheritDoc}
     */
    override fun listChanges(url: String, update: Boolean): Single<Changes> {
        return cacheProviders.listChanges(
            teamCityService.listChanges(urlFormatter.formatBasicUrl(url)),
            DynamicKey(url),
            EvictDynamicKey(update)
        )
    }

    /**
     * {@inheritDoc}
     */
    override fun change(url: String): Single<Changes.Change> {
        return cacheProviders.change(teamCityService.change(urlFormatter.formatBasicUrl(url)), DynamicKey(url))
    }

    /**
     * {@inheritDoc}
     */
    override fun listBranches(buildTypeId: String): Single<Branches> {
        return cacheProviders.listBranches(teamCityService.listBranches(buildTypeId), DynamicKey(buildTypeId))
    }

    /**
     * {@inheritDoc}
     */
    override fun queueBuild(build: Build): Single<Build> {
        return teamCityService.queueBuild(build)
    }

    /**
     * {@inheritDoc}
     */
    override fun cancelBuild(url: String, buildCancelRequest: BuildCancelRequest): Single<Build> {
        return teamCityService.cancelBuild(urlFormatter.formatBasicUrl(url), buildCancelRequest)
    }

    /**
     * {@inheritDoc}
     */
    override fun serverInfo(): Single<ServerInfo> {
        return cacheProviders.serverInfo(teamCityService.serverInfo())
    }
}
