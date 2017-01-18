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

package com.github.vase4kin.teamcityapp.buildlist.api;

import android.support.annotation.VisibleForTesting;

import com.github.vase4kin.teamcityapp.agents.api.Agent;
import com.github.vase4kin.teamcityapp.api.interfaces.Collectible;
import com.github.vase4kin.teamcityapp.artifact.api.Artifacts;
import com.github.vase4kin.teamcityapp.base.api.BaseObject;
import com.github.vase4kin.teamcityapp.changes.api.Changes;
import com.github.vase4kin.teamcityapp.navigation.api.BuildElement;
import com.github.vase4kin.teamcityapp.navigation.api.BuildType;
import com.github.vase4kin.teamcityapp.properties.api.Properties;
import com.github.vase4kin.teamcityapp.tests.api.TestOccurrences;

import java.util.Collections;
import java.util.List;

/**
 * Build
 */
public class Build extends BaseObject implements Collectible<BuildElement> {

    private static final String SUCCESS = "SUCCESS";
    private static final String FAILURE = "FAILURE";

    private String number;
    private String status;
    private String state;
    private String branchName;
    private String statusText;
    private BuildType buildType;
    private String buildTypeId;
    private String queuedDate;
    private String startDate;
    private String finishDate;
    private Triggered triggered;
    private Agent agent;
    private Artifacts artifacts;
    private TestOccurrences testOccurrences;
    private Properties properties;
    private Changes changes;
    private String waitReason;
    private String startEstimate;
    private CanceledInfo canceledInfo;
    private String webUrl;
    private boolean pinned;
    private boolean personal;

    public String getNumber() {
        return number;
    }

    public String getState() {
        return state;
    }

    public String getBranchName() {
        return branchName;
    }

    public String getQueuedDate() {
        return queuedDate;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getStatus() {
        return status;
    }

    public TestOccurrences getTestOccurrences() {
        return testOccurrences;
    }

    public Artifacts getArtifacts() {
        return artifacts;
    }

    public BuildType getBuildType() {
        return buildType;
    }

    public Properties getProperties() {
        return properties;
    }

    public Changes getChanges() {
        return changes;
    }

    public String getBuildTypeId() {
        return buildTypeId;
    }

    public boolean isQueued() {
        return state.equals("queued");
    }

    public boolean isRunning() {
        return state.equals("running");
    }

    public boolean isFailed() {
        return status.equals(FAILURE);
    }

    public boolean isSuccess() {
        return status.equals(SUCCESS);
    }

    public String getStatusText() {
        if (isQueued()) {
            return waitReason != null
                    ? waitReason
                    : "Queued build";
        } else {
            return statusText;
        }
    }

    public String getFinishDate() {
        return finishDate;
    }

    public Triggered getTriggered() {
        return triggered;
    }

    public Agent getAgent() {
        return agent;
    }

    public String getWaitReason() {
        return waitReason;
    }

    public String getStartEstimate() {
        return startEstimate;
    }

    public CanceledInfo getCanceledInfo() {
        return canceledInfo;
    }

    public String getWebUrl() {
        return webUrl;
    }

    @Override
    public List<BuildElement> getObjects() {
        return Collections.emptyList();
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public void setBuildTypeId(String buildTypeId) {
        this.buildType = new BuildType();
        this.buildType.setId(buildTypeId);
    }

    public boolean isPersonal() {
        return personal;
    }

    public boolean isPinned() {
        return pinned;
    }

    /**
     * Default
     */
    public Build() {
    }

    @VisibleForTesting
    public Build(String href,
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
                 String finishDate,
                 String webUrl) {
        this.number = number;
        this.buildTypeId = buildTypeId;
        this.status = status;
        this.state = state;
        this.branchName = branchName;
        this.startDate = startDate;
        this.queuedDate = queuedDate;
        this.finishDate = finishDate;
        this.waitReason = waitReason;
        this.startEstimate = startEstimate;
        this.href = href;
        this.statusText = statusText;
        this.webUrl = webUrl;
    }

    @VisibleForTesting
    public void setChanges(Changes changes) {
        this.changes = changes;
    }

    @VisibleForTesting
    public void setArtifacts(Artifacts artifacts) {
        this.artifacts = artifacts;
    }

    @VisibleForTesting
    public void setAgent(Agent agent) {
        this.agent = agent;
    }

    @VisibleForTesting
    public void setTriggered(Triggered triggered) {
        this.triggered = triggered;
    }

    @VisibleForTesting
    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    @VisibleForTesting
    public void setTestOccurrences(TestOccurrences testOccurrences) {
        this.testOccurrences = testOccurrences;
    }

    @VisibleForTesting
    public void setCanceledInfo(CanceledInfo canceledInfo) {
        this.canceledInfo = canceledInfo;
    }
}
