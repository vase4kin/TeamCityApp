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

import com.github.vase4kin.teamcityapp.BuildConfig
import com.github.vase4kin.teamcityapp.agents.api.Agent
import com.github.vase4kin.teamcityapp.agents.api.Agents
import com.github.vase4kin.teamcityapp.api.TeamCityService
import com.github.vase4kin.teamcityapp.artifact.api.Files
import com.github.vase4kin.teamcityapp.build_details.api.BuildCancelRequest
import com.github.vase4kin.teamcityapp.buildlist.api.Build
import com.github.vase4kin.teamcityapp.buildlist.api.Builds
import com.github.vase4kin.teamcityapp.changes.api.Changes
import com.github.vase4kin.teamcityapp.navigation.api.BuildType
import com.github.vase4kin.teamcityapp.navigation.api.NavigationNode
import com.github.vase4kin.teamcityapp.runbuild.api.Branch
import com.github.vase4kin.teamcityapp.runbuild.api.Branches
import com.github.vase4kin.teamcityapp.tests.api.TestOccurrences
import com.google.gson.Gson
import java.util.ArrayList
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import teamcityapp.features.about.repository.models.ServerInfo
import teamcityapp.features.test_details.repository.models.TestOccurrence

/**
 * Fake TC REST impl
 */
open class FakeTeamCityServiceImpl : TeamCityService {

    override suspend fun listAgents(
        includeDisconnected: Boolean?,
        fields: String?,
        locator: String?
    ): Agents = if (includeDisconnected != null && includeDisconnected) {
        Mocks.disconnectedAgents()
    } else if (includeDisconnected != null && !includeDisconnected) {
        Mocks.connectedAgents()
    } else {
        val merged = ArrayList<Agent>()
        merged.addAll(Mocks.connectedAgents().objects)
        merged.addAll(Mocks.disconnectedAgents().objects)
        Agents(4, merged)
    }

    override suspend fun listBuildTypes(id: String): NavigationNode = Mocks.navigationNode()

    override suspend fun buildType(id: String): BuildType = Mocks.buildTypeMock()

    override suspend fun build(url: String): Build = when (url) {
        // queued builds
        "/guestAuth/app/rest/buildQueue/id:823050" -> Mocks.queuedBuild1()

        "/guestAuth/app/rest/buildQueue/id:823051" -> Mocks.queuedBuild2()

        "/guestAuth/app/rest/buildQueue/id:823052" -> Mocks.queuedBuild3()

        // running build
        "/guestAuth/app/rest/builds/id:783911" -> Mocks.runningBuild()

        "/guestAuth/app/rest/builds/id:7839123" -> Mocks.runningBuild2()

        "/guestAuth/app/rest/builds/id:783912" -> Mocks.successBuild()

        "/guestAuth/app/rest/builds/id:783913" -> Mocks.failedBuild()

        else -> throw NoSuchElementException("No matching offline fixture")
    }

    override suspend fun listBuilds(id: String, locator: String): Builds {
        val builds = ArrayList<Build>()
        builds.add(Mocks.runningBuild())
        builds.add(Mocks.successBuild())
        builds.add(Mocks.failedBuild())
        return Builds(3, builds)
    }

    override suspend fun listRunningBuilds(locator: String, fields: String?): Builds {
        val builds = ArrayList<Build>()
        builds.add(Mocks.runningBuild())
        return Builds(1, builds)
    }

    override suspend fun listQueueBuilds(locator: String?, fields: String?): Builds {
        val builds = ArrayList<Build>()
        builds.add(Mocks.queuedBuild1())
        builds.add(Mocks.queuedBuild2())
        builds.add(Mocks.queuedBuild3())
        return Builds(3, builds)
    }

    override suspend fun listBuilds(locator: String): Builds = listQueueBuilds(null, null)

    override suspend fun listMoreBuilds(url: String): Builds = throw NoSuchElementException("No matching offline fixture")

    override suspend fun listArtifacts(url: String, locator: String): Files = when (url) {
        "/guestAuth/app/rest/builds/id:92912/artifacts/children/" -> Mocks.artifacts()
        else -> throw NoSuchElementException("No matching offline fixture")
    }

    override suspend fun downloadFile(url: String): ResponseBody = "text".toResponseBody("text/plain".toMediaType())

    override suspend fun listTestOccurrences(url: String): TestOccurrences = when (url) {
        "/guestAuth/app/rest/testOccurrences?locator=build:(id:835695),status:FAILURE,count:10" ->
            Mocks.failedTests()

        "/guestAuth/app/rest/testOccurrences?locator=build:(id:835695),status:SUCCESS,count:10" ->
            Mocks.passedTests()

        "/guestAuth/app/rest/testOccurrences?locator=build:(id:835695),status:UNKNOWN,count:10" ->
            Mocks.ignoredTests()

        "/guestAuth/app/rest/testOccurrences?locator=build:(id:835695),count:2147483647&fields=count" ->
            TestOccurrences(16)

        "/guestAuth/app/rest/testOccurrences?locator=build:(id:835695),count:2147483647&fields=count,passed,failed,ignored" ->
            Gson().fromJson("""{"count":16,"passed":10,"failed":2,"ignored":4}""", TestOccurrences::class.java)

        else -> throw NoSuchElementException("No matching offline fixture")
    }

    override suspend fun testOccurrence(url: String): TestOccurrence = when (url) {
        "/guestAuth/app/rest/testOccurrences/id:4482,build:(id:835695)" ->
            TestOccurrence("exception error")

        "/guestAuth/app/rest/testOccurrences/id:4484,build:(id:835695)" ->
            throw
            RuntimeException("Something bad happens!")

        else -> throw NoSuchElementException("No matching offline fixture")
    }

    override suspend fun listChanges(url: String): Changes = when (url) {
        "/guestAuth/app/rest/changes?locator=build:(id:826073),count:10", "/guestAuth/app/rest/changes?locator=build:(id:826073),count:2147483647&fields=count" ->
            Mocks.changes()

        else -> throw NoSuchElementException("No matching offline fixture")
    }

    override suspend fun change(url: String): Changes.Change = when (url) {
        "/guestAuth/app/rest/changes/id:2503722" -> Mocks.singleChange()
        else -> throw NoSuchElementException("No matching offline fixture")
    }

    override suspend fun listBranches(id: String): Branches = Branches(listOf(Branch("master")))

    override suspend fun queueBuild(build: Build): Build = Mocks.queuedBuild1()

    override suspend fun cancelBuild(url: String, buildCancelRequest: BuildCancelRequest): Build = Mocks.failedBuild()

    override suspend fun serverInfo(): ServerInfo = ServerInfo(BuildConfig.VERSION_NAME, Mocks.URL)
}
