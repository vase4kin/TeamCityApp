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
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url
import teamcityapp.features.test_details.api.models.TestOccurrence

/**
 * Authorization header
 */
const val AUTHORIZATION = "Authorization"

/**
 * Base header for all requests
 */
private const val APPLICATION_JSON = "Accept: application/json"

/**
 * TeamCity Api Service
 */
interface TeamCityService {

    /**
     * List agents
     *
     * @param includeDisconnected - determine what agents to return
     * @param fields - additional TC fields
     * @param locator - TC locator
     * @return [Single] with [Agents]
     */
    @Headers(APPLICATION_JSON)
    @GET("app/rest/agents")
    fun listAgents(
        @Query("includeDisconnected") includeDisconnected: Boolean?,
        @Query("fields") fields: String?,
        @Query("locator") locator: String?
    ): Single<Agents>

    /**
     * List build types by Id
     *
     * @param id - Id of build type
     * @return @return [Single] with [NavigationNode]
     */
    @Headers(APPLICATION_JSON)
    @GET("app/rest/projects/id:{id}")
    fun listBuildTypes(@Path("id") id: String): Single<NavigationNode>

    /**
     * Get single build type details
     *
     * @param url - Build type url
     * @return [Single] with [BuildType]
     */
    @Headers(APPLICATION_JSON)
    @GET("app/rest/buildTypes/id:{id}")
    fun buildType(@Path("id") id: String): Single<BuildType>

    /**
     * Get single build details
     *
     * @param url - Build url
     * @return [Single] with [Build]
     */
    @Headers(APPLICATION_JSON)
    @GET
    fun build(@Url url: String): Single<Build>

    /**
     * List builds
     *
     * @param id - BuildType id
     * @param locator - TC locator
     * @return [Single] with [Builds]
     */
    @Headers(APPLICATION_JSON)
    @GET("app/rest/buildTypes/id:{id}/builds")
    fun listBuilds(
        @Path("id") id: String,
        @Query("locator") locator: String
    ): Single<Builds>

    /**
     * List running builds
     *
     * @param locator - TC build locator
     * @param fields - Additional TC fields
     * @return [Single] with [Builds]
     */
    @Headers(APPLICATION_JSON)
    @GET("app/rest/builds")
    fun listRunningBuilds(
        @Query("locator") locator: String,
        @Query("fields") fields: String?
    ): Single<Builds>

    /**
     * List queued builds
     *
     * @param locator - TC build locator
     * @param fields - Additional TC fields
     * @return [Single] with [Builds]
     */
    @Headers(APPLICATION_JSON)
    @GET("app/rest/buildQueue")
    fun listQueueBuilds(
        @Query("locator") locator: String?,
        @Query("fields") fields: String?
    ): Single<Builds>

    /**
     * List builds by locator
     *
     * @param locator - teamcity locator to list builds
     * @return [Single] with [Builds]
     */
    @Headers(APPLICATION_JSON)
    @GET("app/rest/builds")
    fun listBuilds(@Query("locator") locator: String): Single<Builds>

    /**
     * List more builds
     *
     * @param url - More builds url
     * @return [Single] with [Builds]
     */
    @Headers(APPLICATION_JSON)
    @GET
    fun listMoreBuilds(@Url url: String): Single<Builds>

    /**
     * List build artifacts
     *
     * @param url - Artifacts url
     * @param locator - TC locator
     * @return [Single] with [Files]
     */
    @Headers(APPLICATION_JSON)
    @GET
    fun listArtifacts(
        @Url url: String,
        @Query("locator") locator: String
    ): Single<Files>

    /**
     * Download artifact file by url
     *
     * @param url - Artifact file url
     * @return [Single] with [ResponseBody]
     */
    @Headers(APPLICATION_JSON)
    @GET
    fun downloadFile(@Url url: String): Single<ResponseBody>

    /**
     * List tests
     *
     * @param url - Build tests url
     * @return [Single] with [TestOccurrences]
     */
    @Headers(APPLICATION_JSON)
    @GET
    fun listTestOccurrences(@Url url: String): Single<TestOccurrences>

    /**
     * Get single test info by url
     *
     * @param url - Test url
     * @return [Single] with [TestOccurrence]
     */
    @Headers(APPLICATION_JSON)
    @GET
    fun testOccurrence(@Url url: String): Single<TestOccurrence>

    /**
     * List changes
     *
     * @param url - Build changes url
     * @return [Single] with [Changes]
     */
    @Headers(APPLICATION_JSON)
    @GET
    fun listChanges(@Url url: String): Single<Changes>

    /**
     * Get single change
     *
     * @param url - Change url
     * @return [Single] with [com.github.vase4kin.teamcityapp.changes.api.Changes.Change]
     */
    @Headers(APPLICATION_JSON)
    @GET
    fun change(@Url url: String): Single<Changes.Change>

    /**
     * List branches
     *
     * @param id - Build type id
     * @return [Single] with [com.github.vase4kin.teamcityapp.changes.api.Changes.Change]
     */
    @Headers(APPLICATION_JSON)
    @GET("app/rest/buildTypes/id:{id}/branches?locator=policy:ALL_BRANCHES")
    fun listBranches(@Path("id") id: String): Single<Branches>

    /**
     * Queue build
     *
     * @param build - Build to post
     * @return [Single] with [Build]
     */
    @Headers(APPLICATION_JSON)
    @POST("app/rest/buildQueue")
    fun queueBuild(@Body build: Build): Single<Build>

    /**
     * Cancel/Removing build
     *
     * @param buildCancelRequest - request to stop a build
     * @return [Single] with [Build]
     */
    @Headers(APPLICATION_JSON)
    @POST
    fun cancelBuild(@Url url: String, @Body buildCancelRequest: BuildCancelRequest): Single<Build>

    /**
     * Server info
     */
    @Headers(APPLICATION_JSON)
    @GET("app/rest/server")
    fun serverInfo(): Single<ServerInfo>
}
