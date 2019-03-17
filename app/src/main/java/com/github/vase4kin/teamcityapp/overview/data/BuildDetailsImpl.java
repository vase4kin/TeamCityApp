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

import androidx.annotation.Nullable;

import com.github.vase4kin.teamcityapp.buildlist.api.Build;
import com.github.vase4kin.teamcityapp.buildlist.api.Triggered;
import com.github.vase4kin.teamcityapp.buildlist.api.User;
import com.github.vase4kin.teamcityapp.navigation.api.BuildType;
import com.github.vase4kin.teamcityapp.properties.api.Properties;
import com.github.vase4kin.teamcityapp.utils.DateUtils;
import com.github.vase4kin.teamcityapp.utils.IconUtils;

/**
 * Impl of {@link BuildDetails}
 */
public class BuildDetailsImpl implements BuildDetails {

    // TODO: Use from resources
    //R.string.triggered_deleted_configuration_text
    static final String TEXT_DELETED_CONFIGURATION = "Deleted configuration";
    // TODO: Use from resources
    //R.string.triggered_deleted_user_text
    static final String TEXT_DELETED_USER = "Deleted user";
    // TODO: Use from resources
    static final String TEXT_NO_NUMBER = "No number";
    // TODO: Use from resources
    static final String TEXT_QUEUED_BUILD = "Queued build";

    private Build mBuild;

    public BuildDetailsImpl(Build build) {
        this.mBuild = build;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getHref() {
        return mBuild.getHref();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getWebUrl() {
        return mBuild.getWebUrl();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getChangesHref() {
        return mBuild.getChanges().getHref();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getStatusText() {
        if (isQueued()) {
            String waitReason = mBuild.getWaitReason();
            return waitReason != null
                    ? waitReason
                    : TEXT_QUEUED_BUILD;
        } else {
            return mBuild.getStatusText();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getStatusIcon() {
        return IconUtils.getBuildStatusIcon(mBuild.getStatus(), mBuild.getState());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isRunning() {
        return mBuild.getState().equals(STATE_RUNNING);
    }

    /**
     * {@inheritDoc}
     *
     * TODO: ADD TEST
     */
    @Override
    public boolean isFinished() {
        return mBuild.getState().equals(STATE_FINISHED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isQueued() {
        return mBuild.getState().equals(STATE_QUEUED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSuccess() {
        return mBuild.getStatus().equals(STATUS_SUCCESS);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isFailed() {
        return mBuild.getStatus().equals(STATUS_FAILURE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasCancellationInfo() {
        return mBuild.getCanceledInfo() != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasUserInfoWhoCancelledBuild() {
        return mBuild.getCanceledInfo().getUser() != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUserNameWhoCancelledBuild() {
        User userWhoCancelledBuild = mBuild.getCanceledInfo().getUser();
        return getUserName(userWhoCancelledBuild);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCancellationTime() {
        String cancelledTimeStamp = mBuild.getCanceledInfo().getTimestamp();
        return DateUtils.initWithDate(cancelledTimeStamp).formatStartDateToBuildTitle();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getStartDate() {
        return DateUtils.initWithDate(mBuild.getStartDate()).formatStartDateToBuildTitle();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getStartDateFormattedAsHeader() {
        return DateUtils.initWithDate(mBuild.getStartDate()).formatStartDateToBuildListItemHeader();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getQueuedDate() {
        return DateUtils.initWithDate(mBuild.getQueuedDate()).formatStartDateToBuildTitle();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFinishTime() {
        return DateUtils.initWithDate(mBuild.getStartDate(), mBuild.getFinishDate()).formatDateToOverview();
    }

    /**
     * {@inheritDoc}
     */
    @Nullable
    @Override
    public String getEstimatedStartTime() {
        return mBuild.getStartEstimate() != null
                ? DateUtils.initWithDate(mBuild.getStartEstimate()).formatStartDateToBuildTitle()
                : mBuild.getStartEstimate();
    }

    /**
     * {@inheritDoc}
     */
    @Nullable
    @Override
    public String getBranchName() {
        return mBuild.getBranchName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasAgentInfo() {
        return mBuild.getAgent() != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getAgentName() {
        return mBuild.getAgent().getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isTriggeredByVcs() {
        return mBuild.getTriggered().isVcs();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isTriggeredByUser() {
        return mBuild.getTriggered().isUser();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isRestarted() {
        return mBuild.getTriggered().isRestarted();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isTriggeredByBuildType() {
        return mBuild.getTriggered().isBuildType();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isTriggeredByUnknown() {
        return mBuild.getTriggered().isUnknown();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTriggeredDetails() {
        return mBuild.getTriggered().getDetails();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUserNameOfUserWhoTriggeredBuild() {
        User userWhoTriggeredBuild = mBuild.getTriggered().getUser();
        return getUserName(userWhoTriggeredBuild);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNameOfTriggeredBuildType() {
        BuildType buildType = mBuild.getTriggered().getBuildType();
        if (buildType == null) {
            return TEXT_DELETED_CONFIGURATION;
        } else {
            return buildType.getProjectName() + " " + buildType.getName();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getBuildTypeId() {
        return mBuild.getBuildTypeId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasBuildTypeInfo() {
        return mBuild.getBuildType() != null && mBuild.getBuildType().getProjectName() != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getBuildTypeFullName() {
        return mBuild.getBuildType().getProjectName() + " - " + mBuild.getBuildType().getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getBuildTypeName() {
        return mBuild.getBuildType().getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getProjectName() {
        return mBuild.getBuildType().getProjectName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getProjectId() {
        return mBuild.getBuildType().getProjectId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getId() {
        return mBuild.getId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Properties getProperties() {
        return mBuild.getProperties();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isTriggeredByUser(String userName) {
        Triggered triggered = mBuild.getTriggered();
        return triggered != null
                && triggered.isUser()
                && triggered.getUser() != null
                && userName.equals(triggered.getUser().getUsername());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasTests() {
        return mBuild.getTestOccurrences() != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTestsHref() {
        return mBuild.getTestOccurrences().getHref();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getPassedTestCount() {
        return mBuild.getTestOccurrences().getPassed();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getFailedTestCount() {
        return mBuild.getTestOccurrences().getFailed();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getIgnoredTestCount() {
        return mBuild.getTestOccurrences().getIgnored();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getArtifactsHref() {
        return mBuild.getArtifacts().getHref();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNumber() {
        return mBuild.getNumber() == null
                ? TEXT_NO_NUMBER
                : mBuild.getNumber();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isPersonal() {
        return mBuild.isPersonal();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isPinned() {
        return mBuild.isPinned();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasSnapshotDependencies() {
        return mBuild.getSnapshotBuilds() != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Build toBuild() {
        return mBuild;
    }

    /**
     * Get user name of User
     *
     * @param user - User
     * @return User name
     */
    private String getUserName(User user) {
        if (user == null) {
            return TEXT_DELETED_USER;
        } else {
            return user.getName() == null
                    ? user.getUsername()
                    : user.getName();
        }
    }
}
