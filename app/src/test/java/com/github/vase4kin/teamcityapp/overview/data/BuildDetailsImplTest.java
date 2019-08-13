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
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        mBuildDetails = new BuildDetailsImpl(mBuild);
    }

    @Test
    public void testGetHref() throws Exception {
        when(mBuild.getHref()).thenReturn("href");
        assertThat(mBuildDetails.getHref(), is(equalTo("href")));
    }

    @Test
    public void testGetWebUrl() throws Exception {
        when(mBuild.getWebUrl()).thenReturn("webUrl");
        assertThat(mBuildDetails.getWebUrl(), is(equalTo("webUrl")));
    }

    @Test
    public void testGetChangesHref() throws Exception {
        when(mBuild.getChanges()).thenReturn(mChanges);
        when(mChanges.getHref()).thenReturn("href");
        assertThat(mBuildDetails.getChangesHref(), is(equalTo("href")));
    }

    @Test
    public void testGetStatusTextIfBuildIsNotQueued() throws Exception {
        when(mBuild.getStatusText()).thenReturn("status");
        when(mBuild.getState()).thenReturn("state");
        assertThat(mBuildDetails.getStatusText(), is(equalTo("status")));
    }

    @Test
    public void testGetStatusTextIfBuildIsQueuedButDoesNotHaveWaitReason() throws Exception {
        when(mBuild.getState()).thenReturn(STATE_QUEUED);
        assertThat(mBuildDetails.getStatusText(), is(equalTo(TEXT_QUEUED_BUILD)));
    }

    @Test
    public void testGetStatusTextIfBuildIsQueuedButHasWaitReason() throws Exception {
        when(mBuild.getState()).thenReturn(STATE_QUEUED);
        when(mBuild.getWaitReason()).thenReturn("waitReason");
        assertThat(mBuildDetails.getStatusText(), is(equalTo("waitReason")));
    }

    @Test
    public void testGetStatusIcon() throws Exception {
        when(mBuild.getState()).thenReturn("");
        when(mBuild.getStatus()).thenReturn("");
        assertThat(mBuildDetails.getStatusIcon(), is(equalTo(IconUtils.ICON_SUCCESS)));
    }

    @Test
    public void testIsRunning() throws Exception {
        when(mBuild.getState()).thenReturn(STATE_RUNNING);
        assertThat(mBuildDetails.isRunning(), is(equalTo(true)));
    }

    @Test
    public void testIsQueued() throws Exception {
        when(mBuild.getState()).thenReturn(STATE_QUEUED);
        assertThat(mBuildDetails.isQueued(), is(equalTo(true)));
    }

    @Test
    public void testIsSuccess() throws Exception {
        when(mBuild.getStatus()).thenReturn(STATUS_SUCCESS);
        assertThat(mBuildDetails.isSuccess(), is(equalTo(true)));
    }

    @Test
    public void testIsFailed() throws Exception {
        when(mBuild.getStatus()).thenReturn(STATUS_FAILURE);
        assertThat(mBuildDetails.isFailed(), is(equalTo(true)));
    }

    @Test
    public void testHasCancellationInfo() throws Exception {
        when(mBuild.getCanceledInfo()).thenReturn(mCanceledInfo);
        assertThat(mBuildDetails.hasCancellationInfo(), is(equalTo(true)));
    }

    @Test
    public void testHasUserInfoWhoCancelledBuild() throws Exception {
        when(mBuild.getCanceledInfo()).thenReturn(mCanceledInfo);
        when(mCanceledInfo.getUser()).thenReturn(mUser);
        assertThat(mBuildDetails.hasUserInfoWhoCancelledBuild(), is(equalTo(true)));
    }

    @Test
    public void testGetUserNameWhoCancelledBuild() throws Exception {
        when(mBuild.getCanceledInfo()).thenReturn(mCanceledInfo);
        when(mCanceledInfo.getUser()).thenReturn(mUser);
        when(mUser.getName()).thenReturn("user");
        assertThat(mBuildDetails.getUserNameWhoCancelledBuild(), is(equalTo("user")));
    }

    @Test
    public void testGetCancellationTime() throws Exception {
        when(mBuild.getCanceledInfo()).thenReturn(mCanceledInfo);
        when(mCanceledInfo.getTimestamp()).thenReturn(TIME_STAMP);
        assertThat(mBuildDetails.getCancellationTime(), is(equalTo(TIME_FORMATTED)));
    }

    @Test
    public void testGetStartDate() throws Exception {
        when(mBuild.getStartDate()).thenReturn(TIME_STAMP);
        assertThat(mBuildDetails.getStartDate(), is(equalTo(TIME_FORMATTED)));
    }

    @Test
    public void testGetStartDateFormattedAsHeader() throws Exception {
        when(mBuild.getStartDate()).thenReturn(TIME_STAMP);
        assertThat(mBuildDetails.getStartDateFormattedAsHeader(), is(equalTo("21 June")));
    }

    @Test
    public void testGetQueuedDate() throws Exception {
        when(mBuild.getQueuedDate()).thenReturn(TIME_STAMP);
        assertThat(mBuildDetails.getQueuedDate(), is(equalTo(TIME_FORMATTED)));
    }

    @Test
    public void testGetFinishTime() throws Exception {
        when(mBuild.getStartDate()).thenReturn(TIME_STAMP);
        when(mBuild.getFinishDate()).thenReturn(TIME_STAMP);
        assertThat(mBuildDetails.getFinishTime(), is(equalTo("21 Jun 16 23:30 - 23:30 (0s)")));
    }

    @Test
    public void testGetEstimatedStartTime() throws Exception {
        when(mBuild.getStartEstimate()).thenReturn(TIME_STAMP);
        assertThat(mBuildDetails.getEstimatedStartTime(), is(equalTo(TIME_FORMATTED)));
    }

    @Test
    public void testGetBranchName() throws Exception {
        when(mBuild.getBranchName()).thenReturn("branch");
        assertThat(mBuildDetails.getBranchName(), is(equalTo("branch")));
    }

    @Test
    public void testHasAgentInfo() throws Exception {
        when(mBuild.getAgent()).thenReturn(mAgent);
        assertThat(mBuildDetails.hasAgentInfo(), is(equalTo(true)));
    }

    @Test
    public void testGetAgentName() throws Exception {
        when(mBuild.getAgent()).thenReturn(mAgent);
        assertThat(mBuildDetails.getAgentName(), is(equalTo("name")));
    }

    @Test
    public void testIsTriggeredByVcs() throws Exception {
        when(mBuild.getTriggered()).thenReturn(mTriggered);
        when(mTriggered.isVcs()).thenReturn(true);
        assertThat(mBuildDetails.isTriggeredByVcs(), is(equalTo(true)));
    }

    @Test
    public void testIsTriggeredByUser() throws Exception {
        when(mBuild.getTriggered()).thenReturn(mTriggered);
        when(mTriggered.isUser()).thenReturn(true);
        assertThat(mBuildDetails.isTriggeredByUser(), is(equalTo(true)));
    }

    @Test
    public void testIsRestarted() throws Exception {
        when(mBuild.getTriggered()).thenReturn(mTriggered);
        when(mTriggered.isRestarted()).thenReturn(true);
        assertThat(mBuildDetails.isRestarted(), is(equalTo(true)));
    }

    @Test
    public void testIsTriggeredByBuildType() throws Exception {
        when(mBuild.getTriggered()).thenReturn(mTriggered);
        when(mTriggered.isBuildType()).thenReturn(true);
        assertThat(mBuildDetails.isTriggeredByBuildType(), is(equalTo(true)));
    }

    @Test
    public void testIsTriggeredByUnknown() throws Exception {
        when(mBuild.getTriggered()).thenReturn(mTriggered);
        when(mTriggered.isUnknown()).thenReturn(true);
        assertThat(mBuildDetails.isTriggeredByUnknown(), is(equalTo(true)));
    }

    @Test
    public void tetsGetTriggeredDetails() throws Exception {
        when(mBuild.getTriggered()).thenReturn(mTriggered);
        when(mTriggered.getDetails()).thenReturn("details");
        assertThat(mBuildDetails.getTriggeredDetails(), is(equalTo("details")));
    }

    @Test
    public void testGetUserNameOfUserWhoTriggeredBuild() throws Exception {
        when(mBuild.getTriggered()).thenReturn(mTriggered);
        when(mTriggered.getUser()).thenReturn(mUser);
        when(mUser.getName()).thenReturn("name");
        assertThat(mBuildDetails.getUserNameOfUserWhoTriggeredBuild(), is(equalTo("name")));
    }

    @Test
    public void testGetNameOfTriggeredBuildTypeIfBuildTypeIsDeleted() throws Exception {
        when(mBuild.getTriggered()).thenReturn(mTriggered);
        when(mTriggered.getBuildType()).thenReturn(null);
        assertThat(mBuildDetails.getNameOfTriggeredBuildType(), is(equalTo(BuildDetailsImpl.TEXT_DELETED_CONFIGURATION)));
    }

    @Test
    public void testGetNameOfTriggeredBuildType() throws Exception {
        when(mBuild.getTriggered()).thenReturn(mTriggered);
        when(mTriggered.getBuildType()).thenReturn(mBuildType);
        when(mBuildType.getProjectName()).thenReturn("pn");
        when(mBuildType.getName()).thenReturn("cn");
        assertThat(mBuildDetails.getNameOfTriggeredBuildType(), is(equalTo("pn cn")));
    }

    @Test
    public void testGetBuildTypeId() throws Exception {
        when(mBuild.getBuildTypeId()).thenReturn("id");
        assertThat(mBuildDetails.getBuildTypeId(), is(equalTo("id")));
    }

    @Test
    public void testHasBuildTypeInfo() throws Exception {
        when(mBuild.getBuildType()).thenReturn(null);
        assertThat(mBuildDetails.hasBuildTypeInfo(), is(equalTo(false)));
    }

    @Test
    public void testGetBuildTypeFullName() throws Exception {
        when(mBuild.getBuildType()).thenReturn(mBuildType);
        when(mBuildType.getProjectName()).thenReturn("project");
        when(mBuildType.getName()).thenReturn("name");
        assertThat(mBuildDetails.getBuildTypeFullName(), is(equalTo("project - name")));
    }

    @Test
    public void testGetBuildTypeName() throws Exception {
        when(mBuild.getBuildType()).thenReturn(mBuildType);
        when(mBuildType.getName()).thenReturn("name");
        assertThat(mBuildDetails.getBuildTypeName(), is(equalTo("name")));
    }

    @Test
    public void testGetProjectName() throws Exception {
        when(mBuild.getBuildType()).thenReturn(mBuildType);
        when(mBuildType.getProjectName()).thenReturn("project");
        assertThat(mBuildDetails.getProjectName(), is(equalTo("project")));
    }

    @Test
    public void testGetProjectId() throws Exception {
        when(mBuild.getBuildType()).thenReturn(mBuildType);
        when(mBuildType.getProjectId()).thenReturn("id");
        assertThat(mBuildDetails.getProjectId(), is(equalTo("id")));
    }

    @Test
    public void testGetBuildId() throws Exception {
        when(mBuild.getId()).thenReturn("id");
        assertThat(mBuildDetails.getId(), is(equalTo("id")));
    }

    @Test
    public void testGetProperties() throws Exception {
        when(mBuild.getProperties()).thenReturn(mProperties);
        assertThat(mBuildDetails.getProperties(), is(equalTo(mProperties)));
    }

    @Test
    public void testIsTriggeredByUserIfTriggeredIsNull() throws Exception {
        when(mBuild.getTriggered()).thenReturn(null);
        assertThat(mBuildDetails.isTriggeredByUser("user"), is(equalTo(false)));
    }

    @Test
    public void testIsTriggeredByUserIfTriggeredByNotUser() throws Exception {
        when(mBuild.getTriggered()).thenReturn(mTriggered);
        when(mTriggered.isUser()).thenReturn(false);
        assertThat(mBuildDetails.isTriggeredByUser("user"), is(equalTo(false)));
    }

    @Test
    public void testIsTriggeredByUserIfUserIsNull() throws Exception {
        when(mBuild.getTriggered()).thenReturn(mTriggered);
        when(mTriggered.isUser()).thenReturn(true);
        when(mTriggered.getUser()).thenReturn(null);
        assertThat(mBuildDetails.isTriggeredByUser("user"), is(equalTo(false)));
    }

    @Test
    public void testIsTriggeredByUserIfUserIsNotNull() throws Exception {
        when(mBuild.getTriggered()).thenReturn(mTriggered);
        when(mTriggered.isUser()).thenReturn(true);
        when(mTriggered.getUser()).thenReturn(new User("user", null));
        assertThat(mBuildDetails.isTriggeredByUser("user"), is(equalTo(true)));
    }

    @Test
    public void testHasTests() throws Exception {
        when(mBuild.getTestOccurrences()).thenReturn(mTestOccurrences);
        assertThat(mBuildDetails.hasTests(), is(equalTo(true)));
    }

    @Test
    public void testGetTestsHref() throws Exception {
        when(mBuild.getTestOccurrences()).thenReturn(mTestOccurrences);
        when(mTestOccurrences.getHref()).thenReturn("href");
        assertThat(mBuildDetails.getTestsHref(), is(equalTo("href")));
    }

    @Test
    public void testGetPassedTestsCount() throws Exception {
        when(mBuild.getTestOccurrences()).thenReturn(mTestOccurrences);
        when(mTestOccurrences.getPassed()).thenReturn(3);
        assertThat(mBuildDetails.getPassedTestCount(), is(equalTo(3)));
    }

    @Test
    public void testGetFailedTestsCount() throws Exception {
        when(mBuild.getTestOccurrences()).thenReturn(mTestOccurrences);
        when(mTestOccurrences.getFailed()).thenReturn(4);
        assertThat(mBuildDetails.getFailedTestCount(), is(equalTo(4)));
    }

    @Test
    public void testGetIgnoredTestsCount() throws Exception {
        when(mBuild.getTestOccurrences()).thenReturn(mTestOccurrences);
        when(mTestOccurrences.getIgnored()).thenReturn(5);
        assertThat(mBuildDetails.getIgnoredTestCount(), is(equalTo(5)));
    }

    @Test
    public void testGetArtifactsHref() throws Exception {
        when(mBuild.getArtifacts()).thenReturn(mArtifacts);
        when(mArtifacts.getHref()).thenReturn("href");
        assertThat(mBuildDetails.getArtifactsHref(), is(equalTo("href")));
    }

    @Test
    public void testGetNumber() throws Exception {
        when(mBuild.getNumber()).thenReturn("Number");
        assertThat(mBuildDetails.getNumber(), is(equalTo("Number")));
    }

    @Test
    public void testGetNumberIfNumberIsNull() throws Exception {
        when(mBuild.getNumber()).thenReturn(null);
        assertThat(mBuildDetails.getNumber(), is(equalTo(TEXT_NO_NUMBER)));
    }

    @Test
    public void testIsPersonal() throws Exception {
        when(mBuild.isPersonal()).thenReturn(true);
        assertThat(mBuildDetails.isPersonal(), is(equalTo(true)));
    }

    @Test
    public void testIsPinned() throws Exception {
        when(mBuild.isPinned()).thenReturn(true);
        assertThat(mBuildDetails.isPinned(), is(equalTo(true)));
    }

    @Test
    public void testToBuild() throws Exception {
        assertThat(mBuildDetails.toBuild(), is(equalTo(mBuild)));
    }

}