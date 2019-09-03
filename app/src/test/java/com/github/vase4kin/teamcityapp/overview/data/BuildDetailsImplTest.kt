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

package com.github.vase4kin.teamcityapp.overview.data

import com.github.vase4kin.teamcityapp.agents.api.Agent
import com.github.vase4kin.teamcityapp.artifact.api.Artifacts
import com.github.vase4kin.teamcityapp.buildlist.api.Build
import com.github.vase4kin.teamcityapp.buildlist.api.CanceledInfo
import com.github.vase4kin.teamcityapp.buildlist.api.Triggered
import com.github.vase4kin.teamcityapp.buildlist.api.User
import com.github.vase4kin.teamcityapp.changes.api.Changes
import com.github.vase4kin.teamcityapp.navigation.api.BuildType
import com.github.vase4kin.teamcityapp.overview.data.BuildDetails.Companion.STATE_QUEUED
import com.github.vase4kin.teamcityapp.overview.data.BuildDetails.Companion.STATE_RUNNING
import com.github.vase4kin.teamcityapp.overview.data.BuildDetails.Companion.STATUS_FAILURE
import com.github.vase4kin.teamcityapp.overview.data.BuildDetails.Companion.STATUS_SUCCESS
import com.github.vase4kin.teamcityapp.overview.data.BuildDetailsImpl.Companion.TEXT_NO_NUMBER
import com.github.vase4kin.teamcityapp.overview.data.BuildDetailsImpl.Companion.TEXT_QUEUED_BUILD
import com.github.vase4kin.teamcityapp.properties.api.Properties
import com.github.vase4kin.teamcityapp.tests.api.TestOccurrences
import com.github.vase4kin.teamcityapp.utils.IconUtils
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.equalTo
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.runners.MockitoJUnitRunner

private const val TIME_STAMP = "20160621T233008+0700"
private const val TIME_FORMATTED = "21 Jun 16 23:30"

@RunWith(MockitoJUnitRunner::class)
class BuildDetailsImplTest {

    @Mock
    private lateinit var artifacts: Artifacts
    @Mock
    private lateinit var testOccurrences: TestOccurrences
    @Mock
    private lateinit var changes: Changes
    @Mock
    private lateinit var properties: Properties
    @Mock
    private lateinit var triggered: Triggered
    @Mock
    private lateinit var buildType: BuildType
    @Mock
    private lateinit var user: User
    @Mock
    private lateinit var canceledInfo: CanceledInfo
    @Mock
    private lateinit var build: Build
    private val mAgent = Agent("name")
    private lateinit var mBuildDetails: BuildDetails

    @Before
    fun setUp() {
        mBuildDetails = BuildDetailsImpl(build)
    }

    @Test
    fun testGetHref() {
        `when`(build.href).thenReturn("href")
        assertThat(mBuildDetails.href, `is`(equalTo("href")))
    }

    @Test
    fun testGetWebUrl() {
        `when`(build.webUrl).thenReturn("webUrl")
        assertThat(mBuildDetails.webUrl, `is`(equalTo("webUrl")))
    }

    @Test
    fun testGetChangesHref() {
        `when`(build.changes).thenReturn(changes)
        `when`(changes.href).thenReturn("href")
        assertThat(mBuildDetails.changesHref, `is`(equalTo("href")))
    }

    @Test
    fun testGetStatusTextIfBuildIsNotQueued() {
        `when`(build.statusText).thenReturn("status")
        `when`(build.state).thenReturn("state")
        assertThat(mBuildDetails.statusText, `is`(equalTo("status")))
    }

    @Test
    fun testGetStatusTextIfBuildIsQueuedButDoesNotHaveWaitReason() {
        `when`(build.state).thenReturn(STATE_QUEUED)
        assertThat(mBuildDetails.statusText, `is`(equalTo(TEXT_QUEUED_BUILD)))
    }

    @Test
    fun testGetStatusTextIfBuildIsQueuedButHasWaitReason() {
        `when`(build.state).thenReturn(STATE_QUEUED)
        `when`(build.waitReason).thenReturn("waitReason")
        assertThat(mBuildDetails.statusText, `is`(equalTo("waitReason")))
    }

    @Test
    fun testGetStatusIcon() {
        `when`(build.state).thenReturn("")
        `when`(build.status).thenReturn("")
        assertThat(mBuildDetails.statusIcon, `is`(equalTo(IconUtils.ICON_SUCCESS)))
    }

    @Test
    fun testIsRunning() {
        `when`(build.state).thenReturn(STATE_RUNNING)
        assertThat(mBuildDetails.isRunning, `is`(equalTo(true)))
    }

    @Test
    fun testIsQueued() {
        `when`(build.state).thenReturn(STATE_QUEUED)
        assertThat(mBuildDetails.isQueued, `is`(equalTo(true)))
    }

    @Test
    fun testIsSuccess() {
        `when`(build.status).thenReturn(STATUS_SUCCESS)
        assertThat(mBuildDetails.isSuccess, `is`(equalTo(true)))
    }

    @Test
    fun testIsFailed() {
        `when`(build.status).thenReturn(STATUS_FAILURE)
        assertThat(mBuildDetails.isFailed, `is`(equalTo(true)))
    }

    @Test
    fun testHasCancellationInfo() {
        `when`(build.canceledInfo).thenReturn(canceledInfo)
        assertThat(mBuildDetails.hasCancellationInfo(), `is`(equalTo(true)))
    }

    @Test
    fun testHasUserInfoWhoCancelledBuild() {
        `when`(build.canceledInfo).thenReturn(canceledInfo)
        `when`(canceledInfo.user).thenReturn(user)
        assertThat(mBuildDetails.hasUserInfoWhoCancelledBuild(), `is`(equalTo(true)))
    }

    @Test
    fun testGetUserNameWhoCancelledBuild() {
        `when`(build.canceledInfo).thenReturn(canceledInfo)
        `when`(canceledInfo.user).thenReturn(user)
        `when`(user.name).thenReturn("user")
        assertThat(mBuildDetails.userNameWhoCancelledBuild, `is`(equalTo("user")))
    }

    @Test
    fun testGetCancellationTime() {
        `when`(build.canceledInfo).thenReturn(canceledInfo)
        `when`(canceledInfo.timestamp).thenReturn(TIME_STAMP)
        assertThat(mBuildDetails.cancellationTime, `is`(equalTo(TIME_FORMATTED)))
    }

    @Test
    fun testGetStartDate() {
        `when`(build.startDate).thenReturn(TIME_STAMP)
        assertThat(mBuildDetails.startDate, `is`(equalTo(TIME_FORMATTED)))
    }

    @Test
    fun testGetStartDateFormattedAsHeader() {
        `when`(build.startDate).thenReturn(TIME_STAMP)
        assertThat(mBuildDetails.startDateFormattedAsHeader, `is`(equalTo("21 June")))
    }

    @Test
    fun testGetQueuedDate() {
        `when`(build.queuedDate).thenReturn(TIME_STAMP)
        assertThat(mBuildDetails.queuedDate, `is`(equalTo(TIME_FORMATTED)))
    }

    @Test
    fun testGetFinishTime() {
        `when`(build.startDate).thenReturn(TIME_STAMP)
        `when`(build.finishDate).thenReturn(TIME_STAMP)
        assertThat(mBuildDetails.finishTime, `is`(equalTo("21 Jun 16 23:30 - 23:30 (0s)")))
    }

    @Test
    fun testGetEstimatedStartTime() {
        `when`(build.startEstimate).thenReturn(TIME_STAMP)
        assertThat(mBuildDetails.estimatedStartTime, `is`(equalTo(TIME_FORMATTED)))
    }

    @Test
    fun testGetBranchName() {
        `when`(build.branchName).thenReturn("branch")
        assertThat(mBuildDetails.branchName, `is`(equalTo("branch")))
    }

    @Test
    fun testHasAgentInfo() {
        `when`(build.agent).thenReturn(mAgent)
        assertThat(mBuildDetails.hasAgentInfo(), `is`(equalTo(true)))
    }

    @Test
    fun testGetAgentName() {
        `when`(build.agent).thenReturn(mAgent)
        assertThat(mBuildDetails.agentName, `is`(equalTo("name")))
    }

    @Test
    fun testIsTriggeredByVcs() {
        `when`(build.triggered).thenReturn(triggered)
        `when`(triggered.isVcs).thenReturn(true)
        assertThat(mBuildDetails.isTriggeredByVcs, `is`(equalTo(true)))
    }

    @Test
    fun testIsTriggeredByUser() {
        `when`(build.triggered).thenReturn(triggered)
        `when`(triggered.isUser).thenReturn(true)
        assertThat(mBuildDetails.isTriggeredByUser, `is`(equalTo(true)))
    }

    @Test
    fun testIsRestarted() {
        `when`(build.triggered).thenReturn(triggered)
        `when`(triggered.isRestarted).thenReturn(true)
        assertThat(mBuildDetails.isRestarted, `is`(equalTo(true)))
    }

    @Test
    fun testIsTriggeredByBuildType() {
        `when`(build.triggered).thenReturn(triggered)
        `when`(triggered.isBuildType).thenReturn(true)
        assertThat(mBuildDetails.isTriggeredByBuildType, `is`(equalTo(true)))
    }

    @Test
    fun testIsTriggeredByUnknown() {
        `when`(build.triggered).thenReturn(triggered)
        `when`(triggered.isUnknown).thenReturn(true)
        assertThat(mBuildDetails.isTriggeredByUnknown, `is`(equalTo(true)))
    }

    @Test
    fun tetsGetTriggeredDetails() {
        `when`(build.triggered).thenReturn(triggered)
        `when`(triggered.details).thenReturn("details")
        assertThat(mBuildDetails.triggeredDetails, `is`(equalTo("details")))
    }

    @Test
    fun testGetUserNameOfUserWhoTriggeredBuild() {
        `when`(build.triggered).thenReturn(triggered)
        `when`(triggered.user).thenReturn(user)
        `when`(user.name).thenReturn("name")
        assertThat(mBuildDetails.userNameOfUserWhoTriggeredBuild, `is`(equalTo("name")))
    }

    @Test
    fun testGetNameOfTriggeredBuildTypeIfBuildTypeIsDeleted() {
        `when`(build.triggered).thenReturn(triggered)
        `when`(triggered.buildType).thenReturn(null)
        assertThat(mBuildDetails.nameOfTriggeredBuildType, `is`(equalTo(BuildDetailsImpl.TEXT_DELETED_CONFIGURATION)))
    }

    @Test
    fun testGetNameOfTriggeredBuildType() {
        `when`(build.triggered).thenReturn(triggered)
        `when`(triggered.buildType).thenReturn(buildType)
        `when`(buildType.projectName).thenReturn("pn")
        `when`(buildType.name).thenReturn("cn")
        assertThat(mBuildDetails.nameOfTriggeredBuildType, `is`(equalTo("pn cn")))
    }

    @Test
    fun testGetBuildTypeId() {
        `when`(build.buildTypeId).thenReturn("id")
        assertThat(mBuildDetails.buildTypeId, `is`(equalTo("id")))
    }

    @Test
    fun testHasBuildTypeInfo() {
        `when`(build.buildType).thenReturn(null)
        assertThat(mBuildDetails.hasBuildTypeInfo(), `is`(equalTo(false)))
    }

    @Test
    fun testGetBuildTypeFullName() {
        `when`(build.buildType).thenReturn(buildType)
        `when`(buildType.projectName).thenReturn("project")
        `when`(buildType.name).thenReturn("name")
        assertThat(mBuildDetails.buildTypeFullName, `is`(equalTo("project - name")))
    }

    @Test
    fun testGetBuildTypeName() {
        `when`(build.buildType).thenReturn(buildType)
        `when`(buildType.name).thenReturn("name")
        assertThat(mBuildDetails.buildTypeName, `is`(equalTo("name")))
    }

    @Test
    fun testGetProjectName() {
        `when`(build.buildType).thenReturn(buildType)
        `when`(buildType.projectName).thenReturn("project")
        assertThat(mBuildDetails.projectName, `is`(equalTo("project")))
    }

    @Test
    fun testGetProjectId() {
        `when`(build.buildType).thenReturn(buildType)
        `when`(buildType.projectId).thenReturn("id")
        assertThat(mBuildDetails.projectId, `is`(equalTo("id")))
    }

    @Test
    fun testGetBuildId() {
        `when`(build.getId()).thenReturn("id")
        assertThat(mBuildDetails.id, `is`(equalTo("id")))
    }

    @Test
    fun testGetProperties() {
        `when`(build.properties).thenReturn(properties)
        assertThat(mBuildDetails.properties, `is`(equalTo(properties)))
    }

    @Test
    fun testIsTriggeredByUserIfTriggeredIsNull() {
        `when`(build.triggered).thenReturn(null)
        assertThat(mBuildDetails.isTriggeredByUser("user"), `is`(equalTo(false)))
    }

    @Test
    fun testIsTriggeredByUserIfTriggeredByNotUser() {
        `when`(build.triggered).thenReturn(triggered)
        `when`(triggered.isUser).thenReturn(false)
        assertThat(mBuildDetails.isTriggeredByUser("user"), `is`(equalTo(false)))
    }

    @Test
    fun testIsTriggeredByUserIfUserIsNull() {
        `when`(build.triggered).thenReturn(triggered)
        `when`(triggered.isUser).thenReturn(true)
        `when`(triggered.user).thenReturn(null)
        assertThat(mBuildDetails.isTriggeredByUser("user"), `is`(equalTo(false)))
    }

    @Test
    fun testIsTriggeredByUserIfUserIsNotNull() {
        `when`(build.triggered).thenReturn(triggered)
        `when`(triggered.isUser).thenReturn(true)
        `when`(triggered.user).thenReturn(User("user", null))
        assertThat(mBuildDetails.isTriggeredByUser("user"), `is`(equalTo(true)))
    }

    @Test
    fun testHasTests() {
        `when`(build.testOccurrences).thenReturn(testOccurrences)
        assertThat(mBuildDetails.hasTests(), `is`(equalTo(true)))
    }

    @Test
    fun testGetTestsHref() {
        `when`(build.testOccurrences).thenReturn(testOccurrences)
        `when`(testOccurrences.href).thenReturn("href")
        assertThat(mBuildDetails.testsHref, `is`(equalTo("href")))
    }

    @Test
    fun testGetPassedTestsCount() {
        `when`(build.testOccurrences).thenReturn(testOccurrences)
        `when`(testOccurrences.passed).thenReturn(3)
        assertThat(mBuildDetails.passedTestCount, `is`(equalTo(3)))
    }

    @Test
    fun testGetFailedTestsCount() {
        `when`(build.testOccurrences).thenReturn(testOccurrences)
        `when`(testOccurrences.failed).thenReturn(4)
        assertThat(mBuildDetails.failedTestCount, `is`(equalTo(4)))
    }

    @Test
    fun testGetIgnoredTestsCount() {
        `when`(build.testOccurrences).thenReturn(testOccurrences)
        `when`(testOccurrences.ignored).thenReturn(5)
        assertThat(mBuildDetails.ignoredTestCount, `is`(equalTo(5)))
    }

    @Test
    fun testGetArtifactsHref() {
        `when`(build.artifacts).thenReturn(artifacts)
        `when`(artifacts.href).thenReturn("href")
        assertThat(mBuildDetails.artifactsHref, `is`(equalTo("href")))
    }

    @Test
    fun testGetNumber() {
        `when`(build.number).thenReturn("Number")
        assertThat(mBuildDetails.number, `is`(equalTo("Number")))
    }

    @Test
    fun testGetNumberIfNumberIsNull() {
        `when`(build.number).thenReturn(null)
        assertThat(mBuildDetails.number, `is`(equalTo(TEXT_NO_NUMBER)))
    }

    @Test
    fun testIsPersonal() {
        `when`(build.isPersonal).thenReturn(true)
        assertThat(mBuildDetails.isPersonal, `is`(equalTo(true)))
    }

    @Test
    fun testIsPinned() {
        `when`(build.isPinned).thenReturn(true)
        assertThat(mBuildDetails.isPinned, `is`(equalTo(true)))
    }

    @Test
    fun testToBuild() {
        assertThat(mBuildDetails.toBuild(), `is`(equalTo(build)))
    }
}
