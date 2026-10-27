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
import teamcityapp.features.about.repository.models.ServerInfo
import teamcityapp.features.test_details.repository.models.TestOccurrence

/** Offline fixture responses consumed by the Ktor mock engine. */
interface TeamCityService {

    /**
     * List agents
     *
     * @param includeDisconnected - determine what agents to return
     * @param fields - additional TC fields
     * @param locator - TC locator
     * @return [Single] with [Agents]
     */

    fun listAgents(
        includeDisconnected: Boolean?,
        fields: String?,
        locator: String?
    ): Single<Agents>

    /**
     * List build types by Id
     *
     * @param id - Id of build type
     * @return @return [Single] with [NavigationNode]
     */

    fun listBuildTypes(id: String): Single<NavigationNode>

    /**
     * Get single build type details
     *
     * @param url - Build type url
     * @return [Single] with [BuildType]
     */

    fun buildType(id: String): Single<BuildType>

    /**
     * Get single build details
     *
     * @param url - Build url
     * @return [Single] with [Build]
     */

    fun build(url: String): Single<Build>

    /**
     * List builds
     *
     * @param id - BuildType id
     * @param locator - TC locator
     * @return [Single] with [Builds]
     */

    fun listBuilds(
        id: String,
        locator: String
    ): Single<Builds>

    /**
     * List running builds
     *
     * @param locator - TC build locator
     * @param fields - Additional TC fields
     * @return [Single] with [Builds]
     */

    fun listRunningBuilds(
        locator: String,
        fields: String?
    ): Single<Builds>

    /**
     * List queued builds
     *
     * @param locator - TC build locator
     * @param fields - Additional TC fields
     * @return [Single] with [Builds]
     */

    fun listQueueBuilds(
        locator: String?,
        fields: String?
    ): Single<Builds>

    /**
     * List builds by locator
     *
     * @param locator - teamcity locator to list builds
     * @return [Single] with [Builds]
     */

    fun listBuilds(locator: String): Single<Builds>

    /**
     * List more builds
     *
     * @param url - More builds url
     * @return [Single] with [Builds]
     */

    fun listMoreBuilds(url: String): Single<Builds>

    /**
     * List build artifacts
     *
     * @param url - Artifacts url
     * @param locator - TC locator
     * @return [Single] with [Files]
     */

    fun listArtifacts(
        url: String,
        locator: String
    ): Single<Files>

    /**
     * Download artifact file by url
     *
     * @param url - Artifact file url
     * @return [Single] with [ResponseBody]
     */

    fun downloadFile(url: String): Single<ResponseBody>

    /**
     * List tests
     *
     * @param url - Build tests url
     * @return [Single] with [TestOccurrences]
     */

    fun listTestOccurrences(url: String): Single<TestOccurrences>

    /**
     * Get single test info by url
     *
     * @param url - Test url
     * @return [Single] with [TestOccurrence]
     */

    fun testOccurrence(url: String): Single<TestOccurrence>

    /**
     * List changes
     *
     * @param url - Build changes url
     * @return [Single] with [Changes]
     */

    fun listChanges(url: String): Single<Changes>

    /**
     * Get single change
     *
     * @param url - Change url
     * @return [Single] with [com.github.vase4kin.teamcityapp.changes.api.Changes.Change]
     */

    fun change(url: String): Single<Changes.Change>

    /**
     * List branches
     *
     * @param id - Build type id
     * @return [Single] with [com.github.vase4kin.teamcityapp.changes.api.Changes.Change]
     */

    fun listBranches(id: String): Single<Branches>

    /**
     * Queue build
     *
     * @param build - Build to post
     * @return [Single] with [Build]
     */

    fun queueBuild(build: Build): Single<Build>

    /**
     * Cancel/Removing build
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
