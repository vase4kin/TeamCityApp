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

package com.github.vase4kin.teamcityapp.api.cache

import com.github.vase4kin.teamcityapp.agents.api.Agents
import com.github.vase4kin.teamcityapp.artifact.api.Files
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
import io.rx_cache2.LifeCache
import io.rx_cache2.Migration
import io.rx_cache2.SchemeMigration
import teamcityapp.features.about.repository.models.ServerInfo
import teamcityapp.features.test_details.repository.models.TestOccurrence
import java.util.concurrent.TimeUnit

/**
 * Cache providers
 */
@SchemeMigration(
    value = [Migration(version = 1, evictClasses = [Changes.Change::class])]
)
interface CacheProviders {

    /**
     * Cache build types for one day
     */
    @LifeCache(duration = 1, timeUnit = TimeUnit.DAYS)
    fun listBuildTypes(
        navigationNodeSingle: Single<NavigationNode>,
        buildTypeUrl: DynamicKey,
        evictDynamicKey: EvictDynamicKey
    ): Single<NavigationNode>

    /**
     * Cache build type for 1 day
     */
    @LifeCache(duration = 1, timeUnit = TimeUnit.DAYS)
    fun buildType(
        buildTypeSingle: Single<BuildType>,
        buildTypeId: DynamicKey,
        evictDynamicKey: EvictDynamicKey
    ): Single<BuildType>

    /**
     * Cache branches for one minute
     */
    @LifeCache(duration = 1, timeUnit = TimeUnit.MINUTES)
    fun listBranches(
        branchesSingle: Single<Branches>,
        buildTypeId: DynamicKey
    ): Single<Branches>

    /**
     * Cache changes list for three minute
     */
    @LifeCache(duration = 3, timeUnit = TimeUnit.MINUTES)
    fun listChanges(
        changesSingle: Single<Changes>,
        changesUrl: DynamicKey,
        evictDynamicKey: EvictDynamicKey
    ): Single<Changes>

    /**
     * Cache single change for one hour
     */
    @LifeCache(duration = 1, timeUnit = TimeUnit.HOURS)
    fun change(
        changeSingle: Single<Changes.Change>,
        changeUrl: DynamicKey
    ): Single<Changes.Change>

    /**
     * Cache test list for three minute
     */
    @LifeCache(duration = 3, timeUnit = TimeUnit.MINUTES)
    fun listTestOccurrences(
        occurrencesSingle: Single<TestOccurrences>,
        testsUrl: DynamicKey,
        evictDynamicKey: EvictDynamicKey
    ): Single<TestOccurrences>

    /**
     * Cache single test for one hour
     */
    @LifeCache(duration = 1, timeUnit = TimeUnit.HOURS)
    fun testOccurrence(
        testOccurrenceSingle: Single<TestOccurrence>,
        testUrl: DynamicKey
    ): Single<TestOccurrence>

    /**
     * Cache artifacts for one hour
     */
    @LifeCache(duration = 1, timeUnit = TimeUnit.HOURS)
    fun listArtifacts(
        filesSingle: Single<Files>,
        artifactsUrl: DynamicKey,
        evictDynamicKey: EvictDynamicKey
    ): Single<Files>

    /**
     * Cache build list for 1 minutes
     */
    @LifeCache(duration = 1, timeUnit = TimeUnit.MINUTES)
    fun listBuilds(
        buildsSingle: Single<Builds>,
        buildsInfo: DynamicKeyGroup,
        evictDynamicKeyGroup: EvictDynamicKeyGroup
    ): Single<Builds>

    /**
     * Cache build for 1 hour (we cache only builds which have finished state)
     */
    @LifeCache(duration = 1, timeUnit = TimeUnit.HOURS)
    fun build(
        buildSingle: Single<Build>,
        buildUrl: DynamicKey,
        evictDynamicKey: EvictDynamicKey
    ): Single<Build>

    /**
     * Cache running build list for 1 minutes
     */
    @LifeCache(duration = 1, timeUnit = TimeUnit.MINUTES)
    fun listRunningBuilds(
        buildSingle: Single<Builds>,
        locatorPlusFields: DynamicKey,
        evictDynamicKey: EvictDynamicKey
    ): Single<Builds>

    /**
     * Cache queued build list for 1 minutes
     */
    @LifeCache(duration = 1, timeUnit = TimeUnit.MINUTES)
    fun listQueuedBuilds(
        buildSingle: Single<Builds>,
        fields: DynamicKey,
        evictDynamicKey: EvictDynamicKey
    ): Single<Builds>

    /**
     * Cache snapshot dependencies build list for 1 minutes
     */
    @LifeCache(duration = 1, timeUnit = TimeUnit.MINUTES)
    fun listSnapshotBuilds(
        buildSingle: Single<Builds>,
        fields: DynamicKey,
        evictDynamicKey: EvictDynamicKey
    ): Single<Builds>

    /**
     * Cache agent list for 3 minutes
     */
    @LifeCache(duration = 3, timeUnit = TimeUnit.MINUTES)
    fun listAgents(
        agentsSingle: Single<Agents>,
        includeDisconnectedPlusFields: DynamicKey,
        evictDynamicKey: EvictDynamicKey
    ): Single<Agents>

    @LifeCache(duration = 1, timeUnit = TimeUnit.HOURS)
    fun serverInfo(serverInfo: Single<ServerInfo>): Single<ServerInfo>
}
