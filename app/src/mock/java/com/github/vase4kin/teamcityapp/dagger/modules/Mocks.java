/*
 * Copyright 2016 Andrey Tolpeev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.vase4kin.teamcityapp.dagger.modules;

import com.github.vase4kin.teamcityapp.agents.api.Agent;
import com.github.vase4kin.teamcityapp.agents.api.Agents;
import com.github.vase4kin.teamcityapp.artifact.api.Artifacts;
import com.github.vase4kin.teamcityapp.artifact.api.File;
import com.github.vase4kin.teamcityapp.artifact.api.Files;
import com.github.vase4kin.teamcityapp.buildlist.api.Build;
import com.github.vase4kin.teamcityapp.buildlist.api.Builds;
import com.github.vase4kin.teamcityapp.buildlist.api.Triggered;
import com.github.vase4kin.teamcityapp.buildlist.api.User;
import com.github.vase4kin.teamcityapp.changes.api.ChangeFiles;
import com.github.vase4kin.teamcityapp.changes.api.Changes;
import com.github.vase4kin.teamcityapp.navigation.api.BuildType;
import com.github.vase4kin.teamcityapp.navigation.api.BuildTypes;
import com.github.vase4kin.teamcityapp.navigation.api.NavigationNode;
import com.github.vase4kin.teamcityapp.navigation.api.Project;
import com.github.vase4kin.teamcityapp.navigation.api.Projects;
import com.github.vase4kin.teamcityapp.properties.api.Properties;
import com.github.vase4kin.teamcityapp.tests.api.TestOccurrences;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Class to provides mocked objects
 */
public class Mocks {

    public static final String URL = "https://teamcity.server.com";

    /**
     * MOCK
     *
     * @return connected agents
     */
    public static Agents connectedAgents() {
        List<Agent> agents = new ArrayList<>();
        agents.add(new Agent("agent 1"));
        agents.add(new Agent("agent 2"));
        agents.add(new Agent("agent 3"));
        return new Agents(3, agents);
    }

    /**
     * MOCK
     *
     * @return disconnected agents
     */
    public static Agents disconnectedAgents() {
        List<Agent> agents = new ArrayList<>();
        agents.add(new Agent("Mac mini 3434"));
        return new Agents(1, agents);
    }

    /**
     * MOCK
     *
     * @return projects and build type
     */
    public static NavigationNode navigationNode() {
        Project project = new Project();
        project.setName("Project");
        project.setDescription("Description");
        BuildType buildType = new BuildType();
        buildType.setId("build_type_id");
        buildType.setName("build type");
        return new NavigationNode(
                new Projects(Collections.singletonList(project)),
                new BuildTypes(Collections.singletonList(buildType)));
    }

    /**
     * MOCK
     *
     * @return running build
     */
    public static Build runningBuild() {
        return createBuildMock(
                "/guestAuth/app/rest/builds/id:783911",
                "2458",
                "Checkstyle_IdeaInspectionsPullRequest",
                "SUCCESS",
                "running",
                "Running tests",
                null,
                "refs/heads/master",
                "20160622T230008+0700",
                null,
                null,
                null);
    }

    /**
     * MOCK
     *
     * @return success build
     */
    public static Build successBuild() {
        return createNotFailedBuildMock(
                "/guestAuth/app/rest/builds/id:783912",
                "2459",
                "Checkstyle_IdeaInspectionsPullRequest",
                "SUCCESS",
                "finished",
                "Success",
                null,
                "refs/heads/master",
                "20160621T230008+0700",
                null,
                null,
                "20160621T233008+0700");
    }

    /**
     * MOCK
     *
     * @param properties - properties to set
     * @return success build with {@link Properties}
     */
    public static Build successBuild(Properties properties) {
        Build build = createNotFailedBuildMock(
                "/guestAuth/app/rest/builds/id:783912",
                "2459",
                "Checkstyle_IdeaInspectionsPullRequest",
                "SUCCESS",
                "finished",
                "Success",
                null,
                "refs/heads/master",
                "20160621T230008+0700",
                null,
                null,
                "20160621T233008+0700");
        build.setProperties(properties);
        return build;
    }

    /**
     * MOCK
     *
     * @return failed build
     */
    public static Build failedBuild() {
        return createBuildMock(
                "/guestAuth/app/rest/builds/id:783913",
                "2460",
                "Checkstyle_IdeaInspectionsPullRequest",
                "FAILURE",
                "finished",
                "Error with smth",
                null,
                null,
                "20160621T230008+0700",
                null,
                null,
                "20160621T233008+0700");
    }

    /**
     * MOCK
     *
     * @return queued build 1
     */
    public static Build queuedBuild1() {
        return createNotFailedBuildMock(
                "/guestAuth/app/rest/buildQueue/id:823050",
                null,
                "Checkstyle_IdeaInspectionsPullRequest",
                "",
                "queued",
                null,
                null,
                "refs/heads/master",
                null,
                null,
                "20160621T233008+0700",
                null);
    }

    /**
     * MOCK
     *
     * @return queued build 2
     */
    public static Build queuedBuild2() {
        return createNotFailedBuildMock(
                "/guestAuth/app/rest/buildQueue/id:823051",
                null,
                "Checkstyle_IdeaInspectionsPullRequest",
                "",
                "queued",
                null,
                "This build will not start because there are no compatible agents which can run it",
                "refs/heads/dev",
                null,
                null,
                "20160621T233008+0700",
                null);
    }

    /**
     * MOCK
     *
     * @return queued build 3
     */
    public static Build queuedBuild3() {
        Build build = createNotFailedBuildMock(
                "/guestAuth/app/rest/buildQueue/id:823052",
                null,
                "Checkstyle_My_Pants_Solution",
                "",
                "queued",
                null,
                "This build will not start because there are no compatible agents which can run it",
                "refs/heads/dev0feature",
                null,
                null,
                "20160621T233008+0700",
                null);
        BuildType buildType = new BuildType();
        buildType.setName("Another configuration");
        buildType.setProjectName("Project name one two");
        buildType.setProjectId("id");
        build.setBuildType(buildType);
        build.setSnapshotBuilds(new Builds(0, Collections.<Build>emptyList()));
        return build;
    }

    /**
     * MOCK
     *
     * @return parametrized failed build
     */
    public static Build createNotFailedBuildMock(String href,
                                                 String number,
                                                 String buildTypeId,
                                                 String status,
                                                 String state,
                                                 String statusText,
                                                 String waitReason,
                                                 String branchName,
                                                 String startDate,
                                                 String startEstimate,
                                                 String queuedDate,
                                                 String finishDate) {
        Build build = createBuildMock(href, number, buildTypeId, status, state, statusText, waitReason, branchName, startDate, startEstimate, queuedDate, finishDate);
        build.setTestOccurrences(null);
        return build;
    }

    /**
     * MOCK
     *
     * @return parametrized build
     */
    public static Build createBuildMock(String href,
                                        String number,
                                        String buildTypeId,
                                        String status,
                                        String state,
                                        String statusText,
                                        String waitReason,
                                        String branchName,
                                        String startDate,
                                        String startEstimate,
                                        String queuedDate,
                                        String finishDate) {
        Build build = new Build(
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
                "http://www.google.com");
        build.setChanges(new Changes("/guestAuth/app/rest/changes?locator=build:(id:826073)"));
        build.setArtifacts(new Artifacts("/guestAuth/app/rest/builds/id:92912/artifacts/children/"));
        build.setAgent(new Agent("agent-love"));
        build.setTriggered(new Triggered("user", "details", new User("code-lover", null)));
        List<Properties.Property> propertyList = new ArrayList<>();
        propertyList.add(new Properties.Property("sdk", "24"));
        propertyList.add(new Properties.Property("userName", "Murdock"));
        build.setProperties(new Properties(propertyList));
        build.setTestOccurrences(new TestOccurrences(10, 2, 4, "/guestAuth/app/rest/testOccurrences?locator=build:(id:835695)"));
        BuildType buildType = new BuildType();
        buildType.setId("Checkstyle_IdeaInspectionsPullRequest");
        buildType.setProjectId("projectId");
        buildType.setProjectName("project name");
        buildType.setName("build type name");
        build.setBuildType(buildType);
        return build;
    }

    /**
     * MOCK
     *
     * @return changes
     */
    public static Changes changes() {
        List<Changes.Change> singleChanges = new ArrayList<>();
        singleChanges.add(new Changes.Change("/guestAuth/app/rest/changes/id:2503722"));
        return new Changes(singleChanges, 1);
    }

    /**
     * MOCK
     *
     * @return single change
     */
    public static Changes.Change singleChange() {
        ChangeFiles changeFiles = new ChangeFiles(Collections.singletonList(new ChangeFiles.ChangeFile("filename!")));
        return new Changes.Change("21312fsd1321", "john-117", "20160730T003638+0300", "Do you believe?", changeFiles);
    }

    /**
     * MOCK
     *
     * @return failed tests
     */
    public static TestOccurrences failedTests() {
        List<TestOccurrences.TestOccurrence> failedTests = new ArrayList<>();
        failedTests.add(new TestOccurrences.TestOccurrence("Test 1", "FAILURE", "/guestAuth/app/rest/testOccurrences/id:4482,build:(id:835695)"));
        failedTests.add(new TestOccurrences.TestOccurrence("Test 6", "FAILURE", "/guestAuth/app/rest/testOccurrences/id:4484,build:(id:835695)"));
        return new TestOccurrences(failedTests);
    }

    /**
     * MOCK
     *
     * @return passed tests
     */
    public static TestOccurrences passedTests() {
        List<TestOccurrences.TestOccurrence> failedTests = new ArrayList<>();
        failedTests.add(new TestOccurrences.TestOccurrence("Test 5", "SUCCESS", "/guestAuth/app/rest/testOccurrences/id:4482,build:(id:835695)"));
        failedTests.add(new TestOccurrences.TestOccurrence("Test 2", "SUCCESS", "/guestAuth/app/rest/testOccurrences/id:4484,build:(id:835695)"));
        return new TestOccurrences(failedTests);
    }

    /**
     * MOCK
     *
     * @return ignored tests
     */
    public static TestOccurrences ignoredTests() {
        List<TestOccurrences.TestOccurrence> failedTests = new ArrayList<>();
        failedTests.add(new TestOccurrences.TestOccurrence("Test 4", "UNKNOWN", "/guestAuth/app/rest/testOccurrences/id:4482,build:(id:835695)"));
        failedTests.add(new TestOccurrences.TestOccurrence("Test 9", "UNKNOWN", "/guestAuth/app/rest/testOccurrences/id:4484,build:(id:835695)"));
        return new TestOccurrences(failedTests);
    }

    /**
     * MOCK
     *
     * @return artifacts
     */
    public static Files artifacts() {
        List<File> files = new ArrayList<>();
        files.add(new File("res", new File.Children("/guestAuth/app/rest/builds/id:92912/artifacts/children/TCity.apk!/res"), "/guestAuth/app/rest/builds/id:92912/artifacts/metadata/TCity.apk!/res"));
        files.add(new File("AndroidManifest.xml", 7768L, new File.Content("/guestAuth/app/rest/builds/id:92912/artifacts/content/TCity.apk!/AndroidManifest.xml"), "/guestAuth/app/rest/builds/id:92912/artifacts/metadata/TCity.apk!/AndroidManifest.xml"));
        files.add(new File("index.html", 697840, new File.Content("/guestAuth/app/rest/builds/id:92912/artifacts/content/TCity.apk!/index.html"), "/guestAuth/app/rest/builds/id:92912/artifacts/metadata/TCity.apk!/index.html"));
        return new Files(files);
    }

    public static BuildType buildType() {
        return new BuildType();
    }
}
