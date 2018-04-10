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

import android.support.annotation.Nullable;

import com.github.vase4kin.teamcityapp.base.api.Jsonable;
import com.github.vase4kin.teamcityapp.buildlist.api.Build;
import com.github.vase4kin.teamcityapp.properties.api.Properties;

/**
 * Interface to handle build details
 */
public interface BuildDetails extends Jsonable {

    /**
     * Build queued state
     */
    String STATE_QUEUED = "queued";

    /**
     * Build running state
     */
    String STATE_RUNNING = "running";
    /**
     * Build finished state
     */
    String STATE_FINISHED = "finished";
    /**
     * Build success status
     */
    String STATUS_SUCCESS = "SUCCESS";
    /**
     * Build failure status
     */
    String STATUS_FAILURE = "FAILURE";
    /**
     * Build error status
     */
    String STATUS_ERROR = "ERROR";
    /**
     * Build unknown status
     */
    String STATUS_UNKNOWN = "UNKNOWN";

    /**
     * @return {String} as server build url
     */
    String getHref();

    /**
     * @return {String} as server build web url
     */
    String getWebUrl();

    /**
     * @return build changes url as {String}
     */
    String getChangesHref();

    /**
     * @return build status text as {String}
     */
    String getStatusText();

    /**
     * @return as status icon as {String}
     */
    String getStatusIcon();

    /**
     * @return {true} if build is running
     */
    boolean isRunning();

    /**
     * @return {true} if build is finished
     */
    boolean isFinished();

    /**
     * @return {true} if build is queued
     */
    boolean isQueued();

    /**
     * @return {true} if build is success
     */
    boolean isSuccess();

    /**
     * @return {true} if build is failed
     */
    boolean isFailed();

    /**
     * @return {true} if build has cancellation info
     */
    boolean hasCancellationInfo();

    /**
     * @return {true} if was cancelled by user
     */
    boolean hasUserInfoWhoCancelledBuild();

    /**
     * @return user name as {String}
     */
    String getUserNameWhoCancelledBuild();

    /**
     * @return cancellation time as {String}
     */
    String getCancellationTime();

    /**
     * @return start date as {String}
     */
    String getStartDate();

    /**
     * @return start date as {String} formatted specifically for header
     */
    String getStartDateFormattedAsHeader();

    /**
     * @return queued date as {String}
     */
    String getQueuedDate();

    /**
     * @return finish date as {String}
     */
    String getFinishTime();

    /**
     * @return estimated start time as {String}. Can be null
     */
    @Nullable
    String getEstimatedStartTime();

    /**
     * @return branch name as {String}. Can be null
     */
    @Nullable
    String getBranchName();

    /**
     * @return {true} if build has agent
     */
    boolean hasAgentInfo();

    /**
     * @return agent name as {String}
     */
    String getAgentName();

    /**
     * @return {true} if build was triggered by vcs
     */
    boolean isTriggeredByVcs();

    /**
     * @return {true} if build was triggered by user
     */
    boolean isTriggeredByUser();

    /**
     * @return {true} if build was restarted
     */
    boolean isRestarted();

    /**
     * @return {true} if build was triggered by build type
     */
    boolean isTriggeredByBuildType();

    /**
     * @return {true} if build was triggered by unknown
     */
    boolean isTriggeredByUnknown();

    /**
     * @return triggered details as {String}
     */
    String getTriggeredDetails();

    /**
     * @return User name of the user who triggered build
     */
    String getUserNameOfUserWhoTriggeredBuild();

    /**
     * @return Name of configuration who triggered build
     */
    String getNameOfTriggeredBuildType();

    /**
     * @return id of configuration
     */
    String getBuildTypeId();

    /**
     * @return {true} if build has build type info
     */
    boolean hasBuildTypeInfo();

    /**
     * @return full name of configuration
     */
    String getBuildTypeFullName();

    /**
     * @return return name of configuration
     */
    String getBuildTypeName();

    /**
     * @return return project of build
     */
    String getProjectName();

    /**
     * @return project id of build type
     */
    String getProjectId();

    /**
     * @return id of the build
     */
    String getId();

    Properties getProperties();

    /**
     * @return {true} if the build has tests
     */
    boolean hasTests();

    /**
     * @return build tests href as {String}
     */
    String getTestsHref();

    /**
     * @return passed tests count as {int}
     */
    int getPassedTestCount();

    /**
     * @return failed tests count as {int}
     */
    int getFailedTestCount();

    /**
     * @return ignored tests count as {int}
     */
    int getIgnoredTestCount();

    /**
     * @param userName - User name
     * @return {true} if build was triggered by user with name
     */
    boolean isTriggeredByUser(String userName);

    /**
     * @return build artifacts href as {String}
     */
    String getArtifactsHref();

    /**
     * @return build number as {String}
     */
    String getNumber();

    /**
     * @return {true} if the build is personal
     */
    boolean isPersonal();

    /**
     * @return {true} if the build is pinned
     */
    boolean isPinned();

    /**
     * @return build wrapped by build details
     */
    Build toBuild();

    /**
     * Stub
     */
    BuildDetails STUB = new BuildDetails() {
        @Override
        public String getHref() {
            return null;
        }

        @Override
        public String getWebUrl() {
            return null;
        }

        @Override
        public String getChangesHref() {
            return null;
        }

        @Override
        public String getStatusText() {
            return null;
        }

        @Override
        public String getStatusIcon() {
            return null;
        }

        @Override
        public boolean isRunning() {
            return false;
        }

        @Override
        public boolean isFinished() {
            return false;
        }

        @Override
        public boolean isQueued() {
            return false;
        }

        @Override
        public boolean isSuccess() {
            return false;
        }

        @Override
        public boolean isFailed() {
            return false;
        }

        @Override
        public boolean hasCancellationInfo() {
            return false;
        }

        @Override
        public boolean hasUserInfoWhoCancelledBuild() {
            return false;
        }

        @Override
        public String getUserNameWhoCancelledBuild() {
            return null;
        }

        @Override
        public String getCancellationTime() {
            return null;
        }

        @Override
        public String getStartDate() {
            return null;
        }

        @Override
        public String getStartDateFormattedAsHeader() {
            return null;
        }

        @Override
        public String getQueuedDate() {
            return null;
        }

        @Override
        public String getFinishTime() {
            return null;
        }

        @Nullable
        @Override
        public String getEstimatedStartTime() {
            return null;
        }

        @Nullable
        @Override
        public String getBranchName() {
            return null;
        }

        @Override
        public boolean hasAgentInfo() {
            return false;
        }

        @Override
        public String getAgentName() {
            return null;
        }

        @Override
        public boolean isTriggeredByVcs() {
            return false;
        }

        @Override
        public boolean isTriggeredByUser() {
            return false;
        }

        @Override
        public boolean isRestarted() {
            return false;
        }

        @Override
        public boolean isTriggeredByBuildType() {
            return false;
        }

        @Override
        public boolean isTriggeredByUnknown() {
            return false;
        }

        @Override
        public String getTriggeredDetails() {
            return null;
        }

        @Override
        public String getUserNameOfUserWhoTriggeredBuild() {
            return null;
        }

        @Override
        public String getNameOfTriggeredBuildType() {
            return null;
        }

        @Override
        public String getBuildTypeId() {
            return null;
        }

        @Override
        public boolean hasBuildTypeInfo() {
            return false;
        }

        @Override
        public String getBuildTypeFullName() {
            return null;
        }

        @Override
        public String getBuildTypeName() {
            return null;
        }

        @Override
        public String getProjectName() {
            return null;
        }

        @Override
        public String getProjectId() {
            return null;
        }

        @Override
        public String getId() {
            return null;
        }

        @Override
        public Properties getProperties() {
            return null;
        }

        @Override
        public boolean hasTests() {
            return false;
        }

        @Override
        public String getTestsHref() {
            return null;
        }

        @Override
        public int getPassedTestCount() {
            return 0;
        }

        @Override
        public int getFailedTestCount() {
            return 0;
        }

        @Override
        public int getIgnoredTestCount() {
            return 0;
        }

        @Override
        public boolean isTriggeredByUser(String userName) {
            return false;
        }

        @Override
        public String getArtifactsHref() {
            return null;
        }

        @Override
        public String getNumber() {
            return null;
        }

        @Override
        public boolean isPersonal() {
            return false;
        }

        @Override
        public boolean isPinned() {
            return false;
        }

        @Override
        public Build toBuild() {
            return null;
        }
    };

}
