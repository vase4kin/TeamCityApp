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
     * @return [Agents]
     */

    suspend fun listAgents(
        includeDisconnected: Boolean?,
        fields: String?,
        locator: String?
    ): Agents

    /**
     * List build types by Id
     *
     * @param id - Id of build type
     * @return @return [NavigationNode]
     */

    suspend fun listBuildTypes(id: String): NavigationNode

    /**
     * Get single build type details
     *
     * @param url - Build type url
     * @return [BuildType]
     */

    suspend fun buildType(id: String): BuildType

    /**
     * Get single build details
     *
     * @param url - Build url
     * @return [Build]
     */

    suspend fun build(url: String): Build

    /**
     * List builds
     *
     * @param id - BuildType id
     * @param locator - TC locator
     * @return [Builds]
     */

    suspend fun listBuilds(
        id: String,
        locator: String
    ): Builds

    /**
     * List running builds
     *
     * @param locator - TC build locator
     * @param fields - Additional TC fields
     * @return [Builds]
     */

    suspend fun listRunningBuilds(
        locator: String,
        fields: String?
    ): Builds

    /**
     * List queued builds
     *
     * @param locator - TC build locator
     * @param fields - Additional TC fields
     * @return [Builds]
     */

    suspend fun listQueueBuilds(
        locator: String?,
        fields: String?
    ): Builds

    /**
     * List builds by locator
     *
     * @param locator - teamcity locator to list builds
     * @return [Builds]
     */

    suspend fun listBuilds(locator: String): Builds

    /**
     * List more builds
     *
     * @param url - More builds url
     * @return [Builds]
     */

    suspend fun listMoreBuilds(url: String): Builds

    /**
     * List build artifacts
     *
     * @param url - Artifacts url
     * @param locator - TC locator
     * @return [Files]
     */

    suspend fun listArtifacts(
        url: String,
        locator: String
    ): Files

    /**
     * Download artifact file by url
     *
     * @param url - Artifact file url
     * @return [ResponseBody]
     */

    suspend fun downloadFile(url: String): ResponseBody

    /**
     * List tests
     *
     * @param url - Build tests url
     * @return [TestOccurrences]
     */

    suspend fun listTestOccurrences(url: String): TestOccurrences

    /**
     * Get single test info by url
     *
     * @param url - Test url
     * @return [TestOccurrence]
     */

    suspend fun testOccurrence(url: String): TestOccurrence

    /**
     * List changes
     *
     * @param url - Build changes url
     * @return [Changes]
     */

    suspend fun listChanges(url: String): Changes

    /**
     * Get single change
     *
     * @param url - Change url
     * @return [com.github.vase4kin.teamcityapp.changes.api.Changes.Change]
     */

    suspend fun change(url: String): Changes.Change

    /**
     * List branches
     *
     * @param id - Build type id
     * @return [com.github.vase4kin.teamcityapp.changes.api.Changes.Change]
     */

    suspend fun listBranches(id: String): Branches

    /**
     * Queue build
     *
     * @param build - Build to post
     * @return [Build]
     */

    suspend fun queueBuild(build: Build): Build

    /**
     * Cancel/Removing build
     *
     * @param buildCancelRequest - request to stop a build
     * @return [Build]
     */

    suspend fun cancelBuild(url: String, buildCancelRequest: BuildCancelRequest): Build

    /**
     * Server info
     */

    suspend fun serverInfo(): ServerInfo
}
