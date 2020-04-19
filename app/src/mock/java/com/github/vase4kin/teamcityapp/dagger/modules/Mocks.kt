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

package com.github.vase4kin.teamcityapp.dagger.modules

import com.github.vase4kin.teamcityapp.agents.api.Agent
import com.github.vase4kin.teamcityapp.agents.api.Agents
import com.github.vase4kin.teamcityapp.artifact.api.Artifacts
import com.github.vase4kin.teamcityapp.artifact.api.File
import com.github.vase4kin.teamcityapp.artifact.api.Files
import com.github.vase4kin.teamcityapp.buildlist.api.Build
import com.github.vase4kin.teamcityapp.buildlist.api.Builds
import com.github.vase4kin.teamcityapp.buildlist.api.Triggered
import com.github.vase4kin.teamcityapp.buildlist.api.User
import com.github.vase4kin.teamcityapp.changes.api.ChangeFiles
import com.github.vase4kin.teamcityapp.changes.api.Changes
import com.github.vase4kin.teamcityapp.navigation.api.BuildType
import com.github.vase4kin.teamcityapp.navigation.api.BuildTypes
import com.github.vase4kin.teamcityapp.navigation.api.NavigationNode
import com.github.vase4kin.teamcityapp.navigation.api.Project
import com.github.vase4kin.teamcityapp.navigation.api.Projects
import com.github.vase4kin.teamcityapp.properties.api.Properties
import com.github.vase4kin.teamcityapp.tests.api.TestOccurrences
import teamcityapp.features.test_details.repository.models.TestOccurrence

import java.util.ArrayList

/**
 * Class to provides mocked objects
 */
class Mocks {

    companion object {

        const val URL = "https://teamcity.server.com"

        /**
         * MOCK
         *
         * @return connected agents
         */
        fun connectedAgents(): Agents {
            val agents = ArrayList<Agent>()
            agents.add(Agent("agent 1"))
            agents.add(Agent("agent 2"))
            agents.add(Agent("agent 3"))
            return Agents(3, agents)
        }

        /**
         * MOCK
         *
         * @return disconnected agents
         */
        fun disconnectedAgents(): Agents {
            val agents = ArrayList<Agent>()
            agents.add(Agent("Mac mini 3434"))
            return Agents(1, agents)
        }

        /**
         * MOCK
         *
         * @return projects and build type
         */
        fun navigationNode(): NavigationNode {
            val project = Project()
            project.id = "id"
            project.name = "Project"
            project.description = "Description"
            return NavigationNode(
                Projects(listOf(project)),
                BuildTypes(listOf(buildTypeMock()))
            )
        }

        fun buildTypeMock(): BuildType {
            val buildType = BuildType()
            buildType.setId("build_type_id")
            buildType.name = "build type"
            buildType.projectName = "Secret project"
            buildType.projectId = "projectId123"
            return buildType
        }

        /**
         * MOCK
         *
         * @return running build
         */
        fun runningBuild(): Build {
            return createBuildMock(
                "/guestAuth/app/rest/builds/id:783911",
                "2458",
                "Checkstyle_IdeaInspectionsPullRequest",
                "SUCCESS",
                "running",
                "Running tests", null,
                "refs/heads/master",
                "20160622T230008+0700", null, null, null
            )
        }

        /**
         * MOCK
         *
         * @return running build 2
         */
        fun runningBuild2(): Build {
            return createBuildMock(
                "/guestAuth/app/rest/builds/id:7839123",
                "2459",
                "Checkstyle_My_Pants_Solution",
                "SUCCESS",
                "running",
                "Running tests baby", null,
                "refs/heads/dev",
                "20160622T230008+0700", null, null, null
            )
        }

        /**
         * MOCK
         *
         * @return success build
         */
        fun successBuild(): Build {
            return createNotFailedBuildMock(
                "/guestAuth/app/rest/builds/id:783912",
                "2459",
                "Checkstyle_IdeaInspectionsPullRequest",
                "SUCCESS",
                "finished",
                "Success", null,
                "refs/heads/master",
                "20160621T230008+0700", null, null,
                "20160621T233008+0700"
            )
        }

        /**
         * MOCK
         *
         * @param properties - properties to set
         * @return success build with [Properties]
         */
        fun successBuild(properties: Properties?): Build {
            val build = createNotFailedBuildMock(
                "/guestAuth/app/rest/builds/id:783912",
                "2459",
                "Checkstyle_IdeaInspectionsPullRequest",
                "SUCCESS",
                "finished",
                "Success", null,
                "refs/heads/master",
                "20160621T230008+0700", null, null,
                "20160621T233008+0700"
            )
            build.properties = properties
            return build
        }

        /**
         * MOCK
         *
         * @return failed build
         */
        fun failedBuild(): Build {
            return createBuildMock(
                "/guestAuth/app/rest/builds/id:783913",
                "2460",
                "Checkstyle_IdeaInspectionsPullRequest",
                "FAILURE",
                "finished",
                "Error with smth", null, null,
                "20160621T230008+0700", null, null,
                "20160621T233008+0700"
            )
        }

        /**
         * MOCK
         *
         * @return queued build 1
         */
        fun queuedBuild1(): Build {
            return createNotFailedBuildMock(
                "/guestAuth/app/rest/buildQueue/id:823050", null,
                "Checkstyle_IdeaInspectionsPullRequest",
                "",
                "queued", null, null,
                "refs/heads/master", null, null,
                "20160621T233008+0700", null
            )
        }

        /**
         * MOCK
         *
         * @return queued build 2
         */
        fun queuedBuild2(): Build {
            return createNotFailedBuildMock(
                "/guestAuth/app/rest/buildQueue/id:823051", null,
                "Checkstyle_IdeaInspectionsPullRequest",
                "",
                "queued", null,
                "This build will not start because there are no compatible agents which can run it",
                "refs/heads/dev", null, null,
                "20160621T233008+0700", null
            )
        }

        /**
         * MOCK
         *
         * @return queued build 3
         */
        fun queuedBuild3(): Build {
            val build = createNotFailedBuildMock(
                "/guestAuth/app/rest/buildQueue/id:823052", null,
                "Checkstyle_My_Pants_Solution",
                "",
                "queued", null,
                "This build will not start because there are no compatible agents which can run it",
                "refs/heads/dev0feature", null, null,
                "20160621T233008+0700", null
            )
            val buildType = BuildType()
            buildType.name = "Another configuration"
            buildType.projectName = "Project name one two"
            buildType.projectId = "id"
            build.buildType = buildType
            build.snapshotBuilds = Builds(0, emptyList())
            return build
        }

        /**
         * MOCK
         *
         * @return parametrized failed build
         */
        fun createNotFailedBuildMock(
            href: String,
            number: String?,
            buildTypeId: String,
            status: String,
            state: String,
            statusText: String?,
            waitReason: String?,
            branchName: String,
            startDate: String?,
            startEstimate: String?,
            queuedDate: String?,
            finishDate: String?
        ): Build {
            val build = createBuildMock(
                href,
                number,
                buildTypeId,
                status,
                state,
                statusText,
                waitReason,
                branchName,
                startDate,
                startEstimate,
                queuedDate,
                finishDate
            )
            build.testOccurrences = null
            return build
        }

        /**
         * MOCK
         *
         * @return parametrized build
         */
        fun createBuildMock(
            href: String,
            number: String?,
            buildTypeId: String,
            status: String,
            state: String,
            statusText: String?,
            waitReason: String?,
            branchName: String?,
            startDate: String?,
            startEstimate: String?,
            queuedDate: String?,
            finishDate: String?
        ): Build {
            val build = Build(
                href,
                number,
                buildTypeId,
                status,
                state,
                statusText,
                waitReason,
                branchName,
                startDate,
                startEstimate,
                queuedDate,
                finishDate,
                "http://www.google.com"
            )
            build.changes = Changes("/guestAuth/app/rest/changes?locator=build:(id:826073)")
            build.artifacts = Artifacts("/guestAuth/app/rest/builds/id:92912/artifacts/children/")
            build.agent = Agent("agent-love")
            build.triggered = Triggered("user", "details", User("code-lover", null))
            val propertyList = ArrayList<Properties.Property>()
            propertyList.add(Properties.Property("sdk", "24"))
            propertyList.add(Properties.Property("userName", "Murdock"))
            build.properties = Properties(propertyList)
            build.testOccurrences =
                TestOccurrences(
                    10,
                    2,
                    4,
                    "/guestAuth/app/rest/testOccurrences?locator=build:(id:835695)"
                )
            val buildType = BuildType()
            buildType.setId("Checkstyle_IdeaInspectionsPullRequest")
            buildType.projectId = "projectId"
            buildType.projectName = "project name"
            buildType.name = "build type name"
            build.buildType = buildType
            return build
        }

        /**
         * MOCK
         *
         * @return changes
         */
        fun changes(): Changes {
            val singleChanges = ArrayList<Changes.Change>()
            singleChanges.add(Changes.Change("/guestAuth/app/rest/changes/id:2503722"))
            return Changes(singleChanges, 1)
        }

        /**
         * MOCK
         *
         * @return single change
         */
        fun singleChange(): Changes.Change {
            val changeFiles = ChangeFiles(listOf(ChangeFiles.ChangeFile("filename!", "Edited")))
            return Changes.Change(
                "21312fsd1321",
                "john-117",
                "20160730T003638+0300",
                "Do you believe?",
                changeFiles,
                "https://google.com/",
                "id"
            )
        }

        /**
         * MOCK
         *
         * @return failed tests
         */
        fun failedTests(): TestOccurrences {
            val failedTests = ArrayList<TestOccurrence>()
            failedTests.add(
                TestOccurrence(
                    "Test 1",
                    "FAILURE",
                    "/guestAuth/app/rest/testOccurrences/id:4482,build:(id:835695)"
                )
            )
            failedTests.add(
                TestOccurrence(
                    "Test 6",
                    "FAILURE",
                    "/guestAuth/app/rest/testOccurrences/id:4484,build:(id:835695)"
                )
            )
            return TestOccurrences(failedTests)
        }

        /**
         * MOCK
         *
         * @return passed tests
         */
        fun passedTests(): TestOccurrences {
            val failedTests = ArrayList<TestOccurrence>()
            failedTests.add(
                TestOccurrence(
                    "Test 5",
                    "SUCCESS",
                    "/guestAuth/app/rest/testOccurrences/id:4482,build:(id:835695)"
                )
            )
            failedTests.add(
                TestOccurrence(
                    "Test 2",
                    "SUCCESS",
                    "/guestAuth/app/rest/testOccurrences/id:4484,build:(id:835695)"
                )
            )
            return TestOccurrences(failedTests)
        }

        /**
         * MOCK
         *
         * @return ignored tests
         */
        fun ignoredTests(): TestOccurrences {
            val failedTests = ArrayList<TestOccurrence>()
            failedTests.add(
                TestOccurrence(
                    "Test 4",
                    "UNKNOWN",
                    "/guestAuth/app/rest/testOccurrences/id:4482,build:(id:835695)"
                )
            )
            failedTests.add(
                TestOccurrence(
                    "Test 9",
                    "UNKNOWN",
                    "/guestAuth/app/rest/testOccurrences/id:4484,build:(id:835695)"
                )
            )
            return TestOccurrences(failedTests)
        }

        /**
         * MOCK
         *
         * @return artifacts
         */
        fun artifacts(): Files {
            val files = ArrayList<File>()
            files.add(
                File(
                    "res",
                    File.Children("/guestAuth/app/rest/builds/id:92912/artifacts/children/TCity.apk!/res"),
                    "/guestAuth/app/rest/builds/id:92912/artifacts/metadata/TCity.apk!/res"
                )
            )
            files.add(
                File(
                    "AndroidManifest.xml",
                    7768L,
                    File.Content("/guestAuth/app/rest/builds/id:92912/artifacts/content/TCity.apk!/AndroidManifest.xml"),
                    "/guestAuth/app/rest/builds/id:92912/artifacts/metadata/TCity.apk!/AndroidManifest.xml"
                )
            )
            files.add(
                File(
                    "index.html",
                    697840,
                    File.Content("/guestAuth/app/rest/builds/id:92912/artifacts/content/TCity.apk!/index.html"),
                    "/guestAuth/app/rest/builds/id:92912/artifacts/metadata/TCity.apk!/index.html"
                )
            )
            return Files(files)
        }
    }
}
