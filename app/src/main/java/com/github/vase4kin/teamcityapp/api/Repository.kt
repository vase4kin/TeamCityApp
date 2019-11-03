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

import com.github.vase4kin.teamcityapp.about.ServerInfo
import com.github.vase4kin.teamcityapp.agents.api.Agents
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
import teamcityapp.features.test_details.repository.TestDetailsRepository

/**
 * Repository to manage api
 */
interface Repository: TestDetailsRepository {

    /**
     * List agents (cache's supported)
     *
     * @param includeDisconnected - determine what agents to return
     * @param fields - additional TC fields
     * @param locator - TC locator
     * @param update - update cache
     * @return [Single] with [Agents]
     */
    fun listAgents(
        includeDisconnected: Boolean?,
        fields: String?,
        locator: String?,
        update: Boolean
    ): Single<Agents>

    /**
     * List build types by url (cache's supported)
     *
     * @param id - Id of build type
     * @param update - Update cache
     * @return @return [Single] with [NavigationNode]
     */
    fun listBuildTypes(id: String, update: Boolean): Single<NavigationNode>

    /**
     * Get build type (cache's supported)
     *
     * @param id - BuildType id
     * @param update - Update cache
     * @return [Single] with [Builds]
     */
    fun buildType(id: String, update: Boolean): Single<BuildType>

    /**
     * Get single build details (cache's supported)
     *
     * @param url - Build url
     * @param update - Update cache
     * @return [Single] with [Build]
     */
    fun build(url: String, update: Boolean): Single<Build>

    /**
     * List builds (cache's supported)
     *
     * @param id - BuildType id
     * @param locator - TC locator
     * @param update - Update cache
     * @return [Single] with [Builds]
     */
    fun listBuilds(id: String, locator: String, update: Boolean): Single<Builds>

    /**
     * List running builds (cache's supported)
     *
     * @param locator - TC build locator
     * @param fields - Additional TC fields
     * @param update - Update cache
     * @return [Single] with [Builds]
     */
    fun listRunningBuilds(locator: String, fields: String?, update: Boolean): Single<Builds>

    /**
     * List queued builds (cache's supported)
     *
     * @param locator - TC build locator
     * @param fields - Additional TC fields
     * @param update - Update cache
     * @return [Single] with [Builds]
     */
    fun listQueueBuilds(locator: String?, fields: String?, update: Boolean): Single<Builds>

    /**
     * List snapshot dependencies builds (cache's supported)
     *
     * @param id - Build id
     * @param update - Update cache
     * @return [Single] with [Builds]
     */
    fun listSnapshotBuilds(id: String, update: Boolean): Single<Builds>

    /**
     * List more builds (no cache's supported)
     *
     * @param url - More builds url
     * @return [Single] with [Builds]
     */
    fun listMoreBuilds(url: String): Single<Builds>

    /**
     * List build artifacts (cache's supported)
     *
     * @param url - Artifacts url
     * @param locator - TC locator
     * @param update - Update cache
     * @return [Single] with [Files]
     */
    fun listArtifacts(url: String, locator: String, update: Boolean): Single<Files>

    /**
     * Download artifact file by url (no cache's supported)
     *
     * @param url - Artifact file url
     * @return [Single] with [ResponseBody]
     */
    fun downloadFile(url: String): Single<ResponseBody>

    /**
     * List tests (cache's supported)
     *
     * @param url - Build tests url
     * @param update - Update cache
     * @return [Single] with [TestOccurrences]
     */
    fun listTestOccurrences(url: String, update: Boolean): Single<TestOccurrences>

    /**
     * List changes (cache's supported)
     *
     * @param url - Build changes url
     * @param update - Update cache
     * @return [Single] with [Changes]
     */
    fun listChanges(url: String, update: Boolean): Single<Changes>

    /**
     * Get single change (cache's supported)
     *
     * @param url - Change url
     * @return [Single] with [com.github.vase4kin.teamcityapp.changes.api.Changes.Change]
     */
    fun change(url: String): Single<Changes.Change>

    /**
     * List branches (cache's supported)
     *
     * @param buildTypeId - Build type id
     * @return [Single] with [Branches]
     */
    fun listBranches(buildTypeId: String): Single<Branches>

    /**
     * Queue build (no cache's supported)
     *
     * @param build - Build to post
     * @return [Single] with [Build]
     */
    fun queueBuild(build: Build): Single<Build>

    /**
     * Cancel/Removing build (no cache's supported)
     *
     * @param buildCancelRequest - request to stop a build
     * @return [Single] with [Build]
     */
    fun cancelBuild(url: String, buildCancelRequest: BuildCancelRequest): Single<Build>

    /**
     * Server info
     */
    fun serverInfo(): Single<ServerInfo>
}
