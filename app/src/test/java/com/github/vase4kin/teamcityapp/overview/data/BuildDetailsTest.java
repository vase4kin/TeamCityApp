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

package com.github.vase4kin.teamcityapp.overview.data;

import android.content.Context;

import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.agents.api.Agent;
import com.github.vase4kin.teamcityapp.buildlist.api.Build;
import com.github.vase4kin.teamcityapp.buildlist.api.CanceledInfo;
import com.github.vase4kin.teamcityapp.buildlist.api.Triggered;
import com.github.vase4kin.teamcityapp.buildlist.api.User;
import com.github.vase4kin.teamcityapp.navigation.api.BuildType;
import com.github.vase4kin.teamcityapp.utils.IconUtils;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

public class BuildDetailsTest {

    /**
     * Test data
     */
    private static final String TIME_STAMP = "20160621T233008+0700";
    private static final String TIME_FORMATTED = "21 Jun 16 23:30";

    @Mock
    private Triggered mTriggered;

    @Mock
    private BuildType mBuildType;

    @Mock
    private Agent mAgent;

    @Mock
    private User mUser;

    @Mock
    private CanceledInfo mCanceledInfo;

    @Mock
    private Build mBuild;

    @Mock
    private Context mContext;

    private OverViewInteractor.BuildDetails mBuildDetails;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        mBuildDetails = new BuildDetailsImpl(mBuild, mContext);
    }

    @Test
    public void getHref() throws Exception {
        when(mBuild.getHref()).thenReturn("href");
        assertThat(mBuildDetails.getHref(), is(equalTo("href")));
    }

    @Test
    public void getStatusTextIfBuildIsNotQueued() throws Exception {
        when(mBuild.getStatusText()).thenReturn("status");
        assertThat(mBuildDetails.getStatusText(), is(equalTo("status")));
    }

    @Test
    public void getStatusIcon() throws Exception {
        when(mBuild.getState()).thenReturn("");
        when(mBuild.getStatus()).thenReturn("");
        assertThat(mBuildDetails.getStatusIcon(), is(equalTo(IconUtils.SUCCESS)));
    }

    @Test
    public void isRunning() throws Exception {
        when(mBuild.isRunning()).thenReturn(true);
        assertThat(mBuildDetails.isRunning(), is(equalTo(true)));
    }

    @Test
    public void isQueued() throws Exception {
        when(mBuild.isQueued()).thenReturn(true);
        assertThat(mBuildDetails.isQueued(), is(equalTo(true)));
    }

    @Test
    public void hasCancellationInfo() throws Exception {
        when(mBuild.getCanceledInfo()).thenReturn(mCanceledInfo);
        assertThat(mBuildDetails.hasCancellationInfo(), is(equalTo(true)));
    }

    @Test
    public void hasUserInfoWhoCancelledBuild() throws Exception {
        when(mBuild.getCanceledInfo()).thenReturn(mCanceledInfo);
        when(mCanceledInfo.getUser()).thenReturn(mUser);
        assertThat(mBuildDetails.hasUserInfoWhoCancelledBuild(), is(equalTo(true)));
    }

    @Test
    public void getUserNameWhoCancelledBuild() throws Exception {
        when(mBuild.getCanceledInfo()).thenReturn(mCanceledInfo);
        when(mCanceledInfo.getUser()).thenReturn(mUser);
        when(mUser.getName()).thenReturn("user");
        assertThat(mBuildDetails.getUserNameWhoCancelledBuild(), is(equalTo("user")));
    }

    @Test
    public void getCancellationTime() throws Exception {
        when(mBuild.getCanceledInfo()).thenReturn(mCanceledInfo);
        when(mCanceledInfo.getTimestamp()).thenReturn(TIME_STAMP);
        assertThat(mBuildDetails.getCancellationTime(), is(equalTo(TIME_FORMATTED)));
    }

    @Test
    public void getStartDate() throws Exception {
        when(mBuild.getStartDate()).thenReturn(TIME_STAMP);
        assertThat(mBuildDetails.getStartDate(), is(equalTo(TIME_FORMATTED)));
    }

    @Test
    public void getQueuedDate() throws Exception {
        when(mBuild.getQueuedDate()).thenReturn(TIME_STAMP);
        assertThat(mBuildDetails.getQueuedDate(), is(equalTo(TIME_FORMATTED)));
    }

    @Test
    public void getFinishTime() throws Exception {
        when(mBuild.getStartDate()).thenReturn(TIME_STAMP);
        when(mBuild.getFinishDate()).thenReturn(TIME_STAMP);
        assertThat(mBuildDetails.getFinishTime(), is(equalTo("21 Jun 16 23:30 - 23:30 (0s)")));
    }

    @Test
    public void getEstimatedStartTime() throws Exception {
        when(mBuild.getStartEstimate()).thenReturn(TIME_STAMP);
        assertThat(mBuildDetails.getEstimatedStartTime(), is(equalTo(TIME_FORMATTED)));
    }

    @Test
    public void getBranchName() throws Exception {
        when(mBuild.getBranchName()).thenReturn("branch");
        assertThat(mBuildDetails.getBranchName(), is(equalTo("branch")));
    }

    @Test
    public void hasAgentInfo() throws Exception {
        when(mBuild.getAgent()).thenReturn(mAgent);
        assertThat(mBuildDetails.hasAgentInfo(), is(equalTo(true)));
    }

    @Test
    public void getAgentName() throws Exception {
        when(mBuild.getAgent()).thenReturn(mAgent);
        when(mAgent.getName()).thenReturn("name");
        assertThat(mBuildDetails.getAgentName(), is(equalTo("name")));
    }

    @Test
    public void isTriggeredByVcs() throws Exception {
        when(mBuild.getTriggered()).thenReturn(mTriggered);
        when(mTriggered.isVcs()).thenReturn(true);
        assertThat(mBuildDetails.isTriggeredByVcs(), is(equalTo(true)));
    }

    @Test
    public void isTriggeredByUser() throws Exception {
        when(mBuild.getTriggered()).thenReturn(mTriggered);
        when(mTriggered.isUser()).thenReturn(true);
        assertThat(mBuildDetails.isTriggeredByUser(), is(equalTo(true)));
    }

    @Test
    public void isRestarted() throws Exception {
        when(mBuild.getTriggered()).thenReturn(mTriggered);
        when(mTriggered.isRestarted()).thenReturn(true);
        assertThat(mBuildDetails.isRestarted(), is(equalTo(true)));
    }

    @Test
    public void isTriggeredByBuildType() throws Exception {
        when(mBuild.getTriggered()).thenReturn(mTriggered);
        when(mTriggered.isBuildType()).thenReturn(true);
        assertThat(mBuildDetails.isTriggeredByBuildType(), is(equalTo(true)));
    }

    @Test
    public void isTriggeredByUnknown() throws Exception {
        when(mBuild.getTriggered()).thenReturn(mTriggered);
        when(mTriggered.isUnknown()).thenReturn(true);
        assertThat(mBuildDetails.isTriggeredByUnknown(), is(equalTo(true)));
    }

    @Test
    public void getTriggeredDetails() throws Exception {
        when(mBuild.getTriggered()).thenReturn(mTriggered);
        when(mTriggered.getDetails()).thenReturn("details");
        assertThat(mBuildDetails.getTriggeredDetails(), is(equalTo("details")));
    }

    @Test
    public void getUserNameOfUserWhoTriggeredBuild() throws Exception {
        when(mBuild.getTriggered()).thenReturn(mTriggered);
        when(mTriggered.getUser()).thenReturn(mUser);
        when(mUser.getName()).thenReturn("name");
        assertThat(mBuildDetails.getUserNameOfUserWhoTriggeredBuild(), is(equalTo("name")));
    }

    @Test
    public void getNameOfTriggeredBuildTypeIfBuildTypeIsDeleted() throws Exception {
        when(mBuild.getTriggered()).thenReturn(mTriggered);
        when(mTriggered.getBuildType()).thenReturn(null);
        when(mContext.getString(R.string.triggered_deleted_configuration_text)).thenReturn("dc");
        assertThat(mBuildDetails.getNameOfTriggeredBuildType(), is(equalTo("dc")));
    }

    @Test
    public void getNameOfTriggeredBuildType() throws Exception {
        when(mBuild.getTriggered()).thenReturn(mTriggered);
        when(mTriggered.getBuildType()).thenReturn(mBuildType);
        when(mBuildType.getProjectName()).thenReturn("pn");
        when(mBuildType.getName()).thenReturn("cn");
        assertThat(mBuildDetails.getNameOfTriggeredBuildType(), is(equalTo("pn cn")));
    }

}