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
import io.reactivex.Observable
import io.reactivex.Single
import io.rx_cache2.DynamicKey
import io.rx_cache2.DynamicKeyGroup
import io.rx_cache2.EvictDynamicKey
import io.rx_cache2.EvictDynamicKeyGroup
import java.security.MessageDigest
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.rx2.await
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import teamcityapp.features.about.api.AboutServerInfo
import teamcityapp.features.test_details.repository.models.TestOccurrence
import teamcityapp.libraries.coroutines.IoDispatcher
import teamcityapp.libraries.data.builds.BUILD_LAUNCH_FIELDS
import teamcityapp.libraries.remote.url.UrlFormatter

/**
 * Impl of [Repository]
 */
class RepositoryImpl(
    private val teamCityService: TeamCityService,
    private val cacheProviders: CacheProviders,
    private val urlFormatter: UrlFormatter,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
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
    override fun listBuildTypes(id: String, update: Boolean): Single<NavigationNode> = cacheProviders.listBuildTypes(
        teamCityService.listBuildTypes(id),
        DynamicKey(id),
        EvictDynamicKey(update)
    )

    /**
     * {@inheritDoc}
     */
    override fun buildType(id: String, update: Boolean): Single<BuildType> = cacheProviders.buildType(
        teamCityService.buildType(id),
        DynamicKey(id),
        EvictDynamicKey(update)
    )

    /**
     * {@inheritDoc}
     */
    override fun build(url: String, update: Boolean): Single<Build> = cacheProviders.build(
        Single.defer { teamCityService.build(urlFormatter.formatBasicUrl(url)) },
        DynamicKey(url),
        EvictDynamicKey(update)
    )

    /**
     * {@inheritDoc}
     */
    override fun listBuilds(id: String, locator: String, update: Boolean): Single<Builds> {
        if (id.isBlank()) {
            // RxCache rejects an empty group before callers can subscribe to the result.
            return Single.error(IllegalArgumentException("Build type ID is missing"))
        }
        return cacheProviders.listBuilds(
            teamCityService.listBuilds(id, locator),
            DynamicKeyGroup(locator, id),
            EvictDynamicKeyGroup(update)
        )
    }

    /**
     * {@inheritDoc}
     */
    override fun listRunningBuilds(locator: String, fields: String?, update: Boolean): Single<Builds> = cacheProviders.listRunningBuilds(
        seedProjectedBuilds(teamCityService.listRunningBuilds(locator, fields), fields),
        DynamicKey(buildCollectionCacheKey(locator, fields)),
        EvictDynamicKey(update)
    )

    private fun seedProjectedBuilds(upstream: Single<Builds>, fields: String?): Single<Builds> {
        if (fields != BUILD_LAUNCH_FIELDS) return upstream
        // Seed only a fresh network response. A collection cache hit must not renew an old
        // individual snapshot's age. Count-only and legacy summaries cannot seed details.
        return upstream.flatMap { page ->
            val rows = if (page.count == 0) emptyList() else page.objects.orEmpty()
            rows.forEach { row ->
                require(!row.id.isNullOrBlank() && !row.href.isNullOrBlank()) { "A build snapshot requires an ID and detail URL" }
            }
            Observable.fromIterable(rows)
                .concatMapSingle { row ->
                    cacheProviders.build(Single.just(row), DynamicKey(requireNotNull(row.href)), EvictDynamicKey(true))
                }
                .ignoreElements()
                .andThen(Single.just(page))
        }
    }

    private fun buildCollectionCacheKey(locator: String?, fields: String?): String {
        // Preserve persisted Home count/legacy summary keys. Rich projections need bounded keys:
        // RxCache uses the dynamic key in its disk filename, which cannot hold the full selector.
        if (fields == null || fields == "count") return locator + fields
        val digest = MessageDigest.getInstance("SHA-256").digest("$locator\u0000$fields".toByteArray(Charsets.UTF_8))
        return "projection:" + digest.joinToString("") { "%02x".format(it) }
    }

    /**
     * {@inheritDoc}
     */
    override fun listQueueBuilds(locator: String?, fields: String?, update: Boolean): Single<Builds> = cacheProviders.listQueuedBuilds(
        seedProjectedBuilds(teamCityService.listQueueBuilds(locator, fields), fields),
        DynamicKey(buildCollectionCacheKey(locator, fields)),
        EvictDynamicKey(update)
    )

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
    override fun listMoreBuilds(url: String): Single<Builds> = teamCityService.listMoreBuilds(urlFormatter.formatBasicUrl(url))

    /**
     * {@inheritDoc}
     */
    override fun listArtifacts(url: String, locator: String, update: Boolean): Single<Files> = cacheProviders.listArtifacts(
        teamCityService.listArtifacts(urlFormatter.formatBasicUrl(url), locator),
        DynamicKey(url),
        EvictDynamicKey(update)
    )

    /**
     * {@inheritDoc}
     */
    override fun downloadFile(url: String): Single<ResponseBody> = teamCityService.downloadFile(urlFormatter.formatBasicUrl(url))

    /**
     * {@inheritDoc}
     */
    override fun listTestOccurrences(url: String, update: Boolean): Single<TestOccurrences> = cacheProviders.listTestOccurrences(
        teamCityService.listTestOccurrences(urlFormatter.formatBasicUrl(url)),
        DynamicKey(url),
        EvictDynamicKey(update)
    )

    /**
     * {@inheritDoc}
     */
    override fun testOccurrence(url: String): Single<TestOccurrence> = cacheProviders.testOccurrence(
        teamCityService.testOccurrence(urlFormatter.formatBasicUrl(url)),
        DynamicKey(url)
    )

    /**
     * {@inheritDoc}
     */
    override fun listChanges(url: String, update: Boolean): Single<Changes> = cacheProviders.listChanges(
        teamCityService.listChanges(urlFormatter.formatBasicUrl(url)),
        DynamicKey(url),
        EvictDynamicKey(update)
    )

    /**
     * {@inheritDoc}
     */
    override fun change(url: String): Single<Changes.Change> = cacheProviders.change(teamCityService.change(urlFormatter.formatBasicUrl(url)), DynamicKey(url))

    /**
     * {@inheritDoc}
     */
    override fun listBranches(buildTypeId: String): Single<Branches> = cacheProviders.listBranches(teamCityService.listBranches(buildTypeId), DynamicKey(buildTypeId))

    /**
     * {@inheritDoc}
     */
    override fun queueBuild(build: Build): Single<Build> = teamCityService.queueBuild(build)

    /**
     * {@inheritDoc}
     */
    override fun cancelBuild(url: String, buildCancelRequest: BuildCancelRequest): Single<Build> = teamCityService.cancelBuild(urlFormatter.formatBasicUrl(url), buildCancelRequest)

    /**
     * {@inheritDoc}
     */
    override suspend fun testDetails(url: String): String = withContext(ioDispatcher) {
        // Coroutine cancellation disposes the existing RxCache/API subscription.
        testOccurrence(url).await().details.orEmpty()
    }

    override suspend fun serverInfo(): AboutServerInfo = withContext(ioDispatcher) {
        // Preserve RxCache and cancel its subscription when the coroutine is canceled.
        cacheProviders.serverInfo(teamCityService.serverInfo()).await().let {
            AboutServerInfo(it.version, it.webUrl)
        }
    }
}
