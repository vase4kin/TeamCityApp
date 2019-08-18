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

package com.github.vase4kin.teamcityapp.overview.data;

import com.github.vase4kin.teamcityapp.agents.api.Agent;
import com.github.vase4kin.teamcityapp.artifact.api.Artifacts;
import com.github.vase4kin.teamcityapp.buildlist.api.Build;
import com.github.vase4kin.teamcityapp.buildlist.api.CanceledInfo;
import com.github.vase4kin.teamcityapp.buildlist.api.Triggered;
import com.github.vase4kin.teamcityapp.buildlist.api.User;
import com.github.vase4kin.teamcityapp.changes.api.Changes;
import com.github.vase4kin.teamcityapp.navigation.api.BuildType;
import com.github.vase4kin.teamcityapp.properties.api.Properties;
import com.github.vase4kin.teamcityapp.tests.api.TestOccurrences;
import com.github.vase4kin.teamcityapp.utils.IconUtils;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static com.github.vase4kin.teamcityapp.overview.data.BuildDetails.STATE_QUEUED;
import static com.github.vase4kin.teamcityapp.overview.data.BuildDetails.STATE_RUNNING;
import static com.github.vase4kin.teamcityapp.overview.data.BuildDetails.STATUS_FAILURE;
import static com.github.vase4kin.teamcityapp.overview.data.BuildDetails.STATUS_SUCCESS;
import static com.github.vase4kin.teamcityapp.overview.data.BuildDetailsImpl.TEXT_NO_NUMBER;
import static com.github.vase4kin.teamcityapp.overview.data.BuildDetailsImpl.TEXT_QUEUED_BUILD;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

public class BuildDetailsImplTest {

    /**
     * Test data
     */
    private static final String TIME_STAMP = "20160621T233008+0700";
    private static final String TIME_FORMATTED = "21 Jun 16 23:30";

    @Mock
    private Artifacts mArtifacts;

    @Mock
    private TestOccurrences mTestOccurrences;

    @Mock
    private Changes mChanges;

    @Mock
    private Properties mProperties;

    @Mock
    private Triggered mTriggered;

    @Mock
    private BuildType mBuildType;


    @Mock
    private User mUser;

    @Mock
    private CanceledInfo mCanceledInfo;

    @Mock
    private Build mBuild;
    private Agent mAgent = new Agent("name");

    private BuildDetails mBuildDetails;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mBuildDetails = new BuildDetailsImpl(mBuild);
    }

    @Test
    public void testGetHref() {
        when(mBuild.getHref()).thenReturn("href");
        assertThat(mBuildDetails.getHref(), is(equalTo("href")));
    }

    @Test
    public void testGetWebUrl() {
        when(mBuild.getWebUrl()).thenReturn("webUrl");
        assertThat(mBuildDetails.getWebUrl(), is(equalTo("webUrl")));
    }

    @Test
    public void testGetChangesHref() {
        when(mBuild.getChanges()).thenReturn(mChanges);
        when(mChanges.getHref()).thenReturn("href");
        assertThat(mBuildDetails.getChangesHref(), is(equalTo("href")));
    }

    @Test
    public void testGetStatusTextIfBuildIsNotQueued() {
        when(mBuild.getStatusText()).thenReturn("status");
        when(mBuild.getState()).thenReturn("state");
        assertThat(mBuildDetails.getStatusText(), is(equalTo("status")));
    }

    @Test
    public void testGetStatusTextIfBuildIsQueuedButDoesNotHaveWaitReason() {
        when(mBuild.getState()).thenReturn(STATE_QUEUED);
        assertThat(mBuildDetails.getStatusText(), is(equalTo(TEXT_QUEUED_BUILD)));
    }

    @Test
    public void testGetStatusTextIfBuildIsQueuedButHasWaitReason() {
        when(mBuild.getState()).thenReturn(STATE_QUEUED);
        when(mBuild.getWaitReason()).thenReturn("waitReason");
        assertThat(mBuildDetails.getStatusText(), is(equalTo("waitReason")));
    }

    @Test
    public void testGetStatusIcon() {
        when(mBuild.getState()).thenReturn("");
        when(mBuild.getStatus()).thenReturn("");
        assertThat(mBuildDetails.getStatusIcon(), is(equalTo(IconUtils.ICON_SUCCESS)));
    }

    @Test
    public void testIsRunning() {
        when(mBuild.getState()).thenReturn(STATE_RUNNING);
        assertThat(mBuildDetails.isRunning(), is(equalTo(true)));
    }

    @Test
    public void testIsQueued() {
        when(mBuild.getState()).thenReturn(STATE_QUEUED);
        assertThat(mBuildDetails.isQueued(), is(equalTo(true)));
    }

    @Test
    public void testIsSuccess() {
        when(mBuild.getStatus()).thenReturn(STATUS_SUCCESS);
        assertThat(mBuildDetails.isSuccess(), is(equalTo(true)));
    }

    @Test
    public void testIsFailed() {
        when(mBuild.getStatus()).thenReturn(STATUS_FAILURE);
        assertThat(mBuildDetails.isFailed(), is(equalTo(true)));
    }

    @Test
    public void testHasCancellationInfo() {
        when(mBuild.getCanceledInfo()).thenReturn(mCanceledInfo);
        assertThat(mBuildDetails.hasCancellationInfo(), is(equalTo(true)));
    }

    @Test
    public void testHasUserInfoWhoCancelledBuild() {
        when(mBuild.getCanceledInfo()).thenReturn(mCanceledInfo);
        when(mCanceledInfo.getUser()).thenReturn(mUser);
        assertThat(mBuildDetails.hasUserInfoWhoCancelledBuild(), is(equalTo(true)));
    }

    @Test
    public void testGetUserNameWhoCancelledBuild() {
        when(mBuild.getCanceledInfo()).thenReturn(mCanceledInfo);
        when(mCanceledInfo.getUser()).thenReturn(mUser);
        when(mUser.getName()).thenReturn("user");
        assertThat(mBuildDetails.getUserNameWhoCancelledBuild(), is(equalTo("user")));
    }

    @Test
    public void testGetCancellationTime() {
        when(mBuild.getCanceledInfo()).thenReturn(mCanceledInfo);
        when(mCanceledInfo.getTimestamp()).thenReturn(TIME_STAMP);
        assertThat(mBuildDetails.getCancellationTime(), is(equalTo(TIME_FORMATTED)));
    }

    @Test
    public void testGetStartDate() {
        when(mBuild.getStartDate()).thenReturn(TIME_STAMP);
        assertThat(mBuildDetails.getStartDate(), is(equalTo(TIME_FORMATTED)));
    }

    @Test
    public void testGetStartDateFormattedAsHeader() {
        when(mBuild.getStartDate()).thenReturn(TIME_STAMP);
        assertThat(mBuildDetails.getStartDateFormattedAsHeader(), is(equalTo("21 June")));
    }

    @Test
    public void testGetQueuedDate() {
        when(mBuild.getQueuedDate()).thenReturn(TIME_STAMP);
        assertThat(mBuildDetails.getQueuedDate(), is(equalTo(TIME_FORMATTED)));
    }

    @Test
    public void testGetFinishTime() {
        when(mBuild.getStartDate()).thenReturn(TIME_STAMP);
        when(mBuild.getFinishDate()).thenReturn(TIME_STAMP);
        assertThat(mBuildDetails.getFinishTime(), is(equalTo("21 Jun 16 23:30 - 23:30 (0s)")));
    }

    @Test
    public void testGetEstimatedStartTime() {
        when(mBuild.getStartEstimate()).thenReturn(TIME_STAMP);
        assertThat(mBuildDetails.getEstimatedStartTime(), is(equalTo(TIME_FORMATTED)));
    }

    @Test
    public void testGetBranchName() {
        when(mBuild.getBranchName()).thenReturn("branch");
        assertThat(mBuildDetails.getBranchName(), is(equalTo("branch")));
    }

    @Test
    public void testHasAgentInfo() {
        when(mBuild.getAgent()).thenReturn(mAgent);
        assertThat(mBuildDetails.hasAgentInfo(), is(equalTo(true)));
    }

    @Test
    public void testGetAgentName() {
        when(mBuild.getAgent()).thenReturn(mAgent);
        assertThat(mBuildDetails.getAgentName(), is(equalTo("name")));
    }

    @Test
    public void testIsTriggeredByVcs() {
        when(mBuild.getTriggered()).thenReturn(mTriggered);
        when(mTriggered.isVcs()).thenReturn(true);
        assertThat(mBuildDetails.isTriggeredByVcs(), is(equalTo(true)));
    }

    @Test
    public void testIsTriggeredByUser() {
        when(mBuild.getTriggered()).thenReturn(mTriggered);
        when(mTriggered.isUser()).thenReturn(true);
        assertThat(mBuildDetails.isTriggeredByUser(), is(equalTo(true)));
    }

    @Test
    public void testIsRestarted() {
        when(mBuild.getTriggered()).thenReturn(mTriggered);
        when(mTriggered.isRestarted()).thenReturn(true);
        assertThat(mBuildDetails.isRestarted(), is(equalTo(true)));
    }

    @Test
    public void testIsTriggeredByBuildType() {
        when(mBuild.getTriggered()).thenReturn(mTriggered);
        when(mTriggered.isBuildType()).thenReturn(true);
        assertThat(mBuildDetails.isTriggeredByBuildType(), is(equalTo(true)));
    }

    @Test
    public void testIsTriggeredByUnknown() {
        when(mBuild.getTriggered()).thenReturn(mTriggered);
        when(mTriggered.isUnknown()).thenReturn(true);
        assertThat(mBuildDetails.isTriggeredByUnknown(), is(equalTo(true)));
    }

    @Test
    public void tetsGetTriggeredDetails() {
        when(mBuild.getTriggered()).thenReturn(mTriggered);
        when(mTriggered.getDetails()).thenReturn("details");
        assertThat(mBuildDetails.getTriggeredDetails(), is(equalTo("details")));
    }

    @Test
    public void testGetUserNameOfUserWhoTriggeredBuild() {
        when(mBuild.getTriggered()).thenReturn(mTriggered);
        when(mTriggered.getUser()).thenReturn(mUser);
        when(mUser.getName()).thenReturn("name");
        assertThat(mBuildDetails.getUserNameOfUserWhoTriggeredBuild(), is(equalTo("name")));
    }

    @Test
    public void testGetNameOfTriggeredBuildTypeIfBuildTypeIsDeleted() {
        when(mBuild.getTriggered()).thenReturn(mTriggered);
        when(mTriggered.getBuildType()).thenReturn(null);
        assertThat(mBuildDetails.getNameOfTriggeredBuildType(), is(equalTo(BuildDetailsImpl.TEXT_DELETED_CONFIGURATION)));
    }

    @Test
    public void testGetNameOfTriggeredBuildType() {
        when(mBuild.getTriggered()).thenReturn(mTriggered);
        when(mTriggered.getBuildType()).thenReturn(mBuildType);
        when(mBuildType.getProjectName()).thenReturn("pn");
        when(mBuildType.getName()).thenReturn("cn");
        assertThat(mBuildDetails.getNameOfTriggeredBuildType(), is(equalTo("pn cn")));
    }

    @Test
    public void testGetBuildTypeId() {
        when(mBuild.getBuildTypeId()).thenReturn("id");
        assertThat(mBuildDetails.getBuildTypeId(), is(equalTo("id")));
    }

    @Test
    public void testHasBuildTypeInfo() {
        when(mBuild.getBuildType()).thenReturn(null);
        assertThat(mBuildDetails.hasBuildTypeInfo(), is(equalTo(false)));
    }

    @Test
    public void testGetBuildTypeFullName() {
        when(mBuild.getBuildType()).thenReturn(mBuildType);
        when(mBuildType.getProjectName()).thenReturn("project");
        when(mBuildType.getName()).thenReturn("name");
        assertThat(mBuildDetails.getBuildTypeFullName(), is(equalTo("project - name")));
    }

    @Test
    public void testGetBuildTypeName() {
        when(mBuild.getBuildType()).thenReturn(mBuildType);
        when(mBuildType.getName()).thenReturn("name");
        assertThat(mBuildDetails.getBuildTypeName(), is(equalTo("name")));
    }

    @Test
    public void testGetProjectName() {
        when(mBuild.getBuildType()).thenReturn(mBuildType);
        when(mBuildType.getProjectName()).thenReturn("project");
        assertThat(mBuildDetails.getProjectName(), is(equalTo("project")));
    }

    @Test
    public void testGetProjectId() {
        when(mBuild.getBuildType()).thenReturn(mBuildType);
        when(mBuildType.getProjectId()).thenReturn("id");
        assertThat(mBuildDetails.getProjectId(), is(equalTo("id")));
    }

    @Test
    public void testGetBuildId() {
        when(mBuild.getId()).thenReturn("id");
        assertThat(mBuildDetails.getId(), is(equalTo("id")));
    }

    @Test
    public void testGetProperties() {
        when(mBuild.getProperties()).thenReturn(mProperties);
        assertThat(mBuildDetails.getProperties(), is(equalTo(mProperties)));
    }

    @Test
    public void testIsTriggeredByUserIfTriggeredIsNull() {
        when(mBuild.getTriggered()).thenReturn(null);
        assertThat(mBuildDetails.isTriggeredByUser("user"), is(equalTo(false)));
    }

    @Test
    public void testIsTriggeredByUserIfTriggeredByNotUser() {
        when(mBuild.getTriggered()).thenReturn(mTriggered);
        when(mTriggered.isUser()).thenReturn(false);
        assertThat(mBuildDetails.isTriggeredByUser("user"), is(equalTo(false)));
    }

    @Test
    public void testIsTriggeredByUserIfUserIsNull() {
        when(mBuild.getTriggered()).thenReturn(mTriggered);
        when(mTriggered.isUser()).thenReturn(true);
        when(mTriggered.getUser()).thenReturn(null);
        assertThat(mBuildDetails.isTriggeredByUser("user"), is(equalTo(false)));
    }

    @Test
    public void testIsTriggeredByUserIfUserIsNotNull() {
        when(mBuild.getTriggered()).thenReturn(mTriggered);
        when(mTriggered.isUser()).thenReturn(true);
        when(mTriggered.getUser()).thenReturn(new User("user", null));
        assertThat(mBuildDetails.isTriggeredByUser("user"), is(equalTo(true)));
    }

    @Test
    public void testHasTests() {
        when(mBuild.getTestOccurrences()).thenReturn(mTestOccurrences);
        assertThat(mBuildDetails.hasTests(), is(equalTo(true)));
    }

    @Test
    public void testGetTestsHref() {
        when(mBuild.getTestOccurrences()).thenReturn(mTestOccurrences);
        when(mTestOccurrences.getHref()).thenReturn("href");
        assertThat(mBuildDetails.getTestsHref(), is(equalTo("href")));
    }

    @Test
    public void testGetPassedTestsCount() {
        when(mBuild.getTestOccurrences()).thenReturn(mTestOccurrences);
        when(mTestOccurrences.getPassed()).thenReturn(3);
        assertThat(mBuildDetails.getPassedTestCount(), is(equalTo(3)));
    }

    @Test
    public void testGetFailedTestsCount() {
        when(mBuild.getTestOccurrences()).thenReturn(mTestOccurrences);
        when(mTestOccurrences.getFailed()).thenReturn(4);
        assertThat(mBuildDetails.getFailedTestCount(), is(equalTo(4)));
    }

    @Test
    public void testGetIgnoredTestsCount() {
        when(mBuild.getTestOccurrences()).thenReturn(mTestOccurrences);
        when(mTestOccurrences.getIgnored()).thenReturn(5);
        assertThat(mBuildDetails.getIgnoredTestCount(), is(equalTo(5)));
    }

    @Test
    public void testGetArtifactsHref() {
        when(mBuild.getArtifacts()).thenReturn(mArtifacts);
        when(mArtifacts.getHref()).thenReturn("href");
        assertThat(mBuildDetails.getArtifactsHref(), is(equalTo("href")));
    }

    @Test
    public void testGetNumber() {
        when(mBuild.getNumber()).thenReturn("Number");
        assertThat(mBuildDetails.getNumber(), is(equalTo("Number")));
    }

    @Test
    public void testGetNumberIfNumberIsNull() {
        when(mBuild.getNumber()).thenReturn(null);
        assertThat(mBuildDetails.getNumber(), is(equalTo(TEXT_NO_NUMBER)));
    }

    @Test
    public void testIsPersonal() {
        when(mBuild.isPersonal()).thenReturn(true);
        assertThat(mBuildDetails.isPersonal(), is(equalTo(true)));
    }

    @Test
    public void testIsPinned() {
        when(mBuild.isPinned()).thenReturn(true);
        assertThat(mBuildDetails.isPinned(), is(equalTo(true)));
    }

    @Test
    public void testToBuild() {
        assertThat(mBuildDetails.toBuild(), is(equalTo(mBuild)));
    }

}