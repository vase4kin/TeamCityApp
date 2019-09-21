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

import com.github.vase4kin.teamcityapp.buildlist.api.Build
import com.github.vase4kin.teamcityapp.buildlist.api.User
import com.github.vase4kin.teamcityapp.properties.api.Properties
import com.github.vase4kin.teamcityapp.utils.DateUtils
import com.github.vase4kin.teamcityapp.utils.IconUtils

/**
 * Impl of [BuildDetails]
 */
class BuildDetailsImpl(private val build: Build) : BuildDetails {

    /**
     * {@inheritDoc}
     */
    override val href: String
        get() = build.href

    /**
     * {@inheritDoc}
     */
    override val webUrl: String
        get() = build.webUrl

    /**
     * {@inheritDoc}
     */
    override val changesHref: String?
        get() = build.changes?.href

    /**
     * {@inheritDoc}
     */
    override val statusText: String
        get() {
            return if (isQueued) {
                val waitReason = build.waitReason
                waitReason ?: TEXT_QUEUED_BUILD
            } else {
                build.statusText
            }
        }

    /**
     * {@inheritDoc}
     */
    override val statusIcon: String
        get() = IconUtils.getBuildStatusIcon(build.status, build.state)

    /**
     * {@inheritDoc}
     */
    override val isRunning: Boolean
        get() = build.state == BuildDetails.STATE_RUNNING

    /**
     * {@inheritDoc}
     *
     *
     * TODO: ADD TEST
     */
    override val isFinished: Boolean
        get() = build.state == BuildDetails.STATE_FINISHED

    /**
     * {@inheritDoc}
     */
    override val isQueued: Boolean
        get() = build.state == BuildDetails.STATE_QUEUED

    /**
     * {@inheritDoc}
     */
    override val isSuccess: Boolean
        get() = build.status == BuildDetails.STATUS_SUCCESS

    /**
     * {@inheritDoc}
     */
    override val isFailed: Boolean
        get() = build.status == BuildDetails.STATUS_FAILURE

    /**
     * {@inheritDoc}
     */
    override fun hasCancellationInfo(): Boolean {
        return build.canceledInfo != null
    }

    /**
     * {@inheritDoc}
     */
    override fun hasUserInfoWhoCancelledBuild(): Boolean {
        return build.canceledInfo?.user != null
    }

    /**
     * {@inheritDoc}
     */

    override val userNameWhoCancelledBuild: String?
        get() {
            val userWhoCancelledBuild = build.canceledInfo?.user
            return getUserName(userWhoCancelledBuild)
        }

    /**
     * {@inheritDoc}
     */
    override val cancellationTime: String?
        get() {
            val cancelledTimeStamp = build.canceledInfo?.timestamp
            return DateUtils.initWithDate(cancelledTimeStamp).formatStartDateToBuildTitle()
        }

    /**
     * {@inheritDoc}
     */
    override val startDate: String
        get() = DateUtils.initWithDate(build.startDate).formatStartDateToBuildTitle()

    /**
     * {@inheritDoc}
     */
    override val startDateFormattedAsHeader: String
        get() = DateUtils.initWithDate(build.startDate).formatStartDateToBuildListItemHeader()

    /**
     * {@inheritDoc}
     */
    override val queuedDate: String
        get() = DateUtils.initWithDate(build.queuedDate).formatStartDateToBuildTitle()

    /**
     * {@inheritDoc}
     */
    override val finishTime: String
        get() = DateUtils.initWithDate(build.startDate, build.finishDate).formatDateToOverview()
    /**
     * {@inheritDoc}
     */
    override val estimatedStartTime: String?
        get() {
            val startEstimate = build.startEstimate
            return if (startEstimate != null)
                DateUtils.initWithDate(startEstimate).formatStartDateToBuildTitle()
            else
                build.startEstimate
        }
    /**
     * {@inheritDoc}
     */
    override val branchName: String?
        get() = build.branchName

    /**
     * {@inheritDoc}
     */
    override fun hasAgentInfo(): Boolean {
        return build.agent != null
    }

    /**
     * {@inheritDoc}
     */
    override val agentName: String?
        get() = build.agent?.name

    /**
     * {@inheritDoc}
     */
    override val isTriggeredByVcs: Boolean
        get() = build.triggered?.isVcs == true

    /**
     * {@inheritDoc}
     */
    override val isTriggeredByUser: Boolean
        get() = build.triggered?.isUser == true

    /**
     * {@inheritDoc}
     */
    override val isRestarted: Boolean
        get() = build.triggered?.isRestarted == true

    /**
     * {@inheritDoc}
     */
    override val isTriggeredByBuildType: Boolean
        get() = build.triggered?.isBuildType == true

    /**
     * {@inheritDoc}
     */
    override val isTriggeredByUnknown: Boolean
        get() = build.triggered?.isUnknown == true

    /**
     * {@inheritDoc}
     */
    override val triggeredDetails: String?
        get() = build.triggered?.details

    /**
     * {@inheritDoc}
     */
    override val userNameOfUserWhoTriggeredBuild: String?
        get() {
            val userWhoTriggeredBuild = build.triggered?.user
            return getUserName(userWhoTriggeredBuild)
        }

    /**
     * {@inheritDoc}
     */
    /**
     * {@inheritDoc}
     */
    override val nameOfTriggeredBuildType: String?
        get() {
            val buildType = build.triggered?.buildType
            return if (buildType == null) {
                TEXT_DELETED_CONFIGURATION
            } else {
                buildType.projectName + " " + buildType.name
            }
        }

    /**
     * {@inheritDoc}
     */
    override val buildTypeId: String
        get() = build.buildTypeId

    /**
     * {@inheritDoc}
     */
    override fun hasBuildTypeInfo(): Boolean {
        return build.buildType != null && build.buildType?.projectName != null
    }

    /**
     * {@inheritDoc}
     */
    override val buildTypeFullName: String
        get() = build.buildType?.projectName + " - " + build.buildType?.name

    /**
     * {@inheritDoc}
     */
    override val buildTypeName: String?
        get() = build.buildType?.name

    /**
     * {@inheritDoc}
     */
    override val projectName: String?
        get() = build.buildType?.projectName

    /**
     * {@inheritDoc}
     */
    override val projectId: String?
        get() = build.buildType?.projectId

    /**
     * {@inheritDoc}
     */
    override fun getId(): String {
        return build.getId()
    }

    /**
     * {@inheritDoc}
     */
    override val properties: Properties?
        get() = build.properties

    /**
     * {@inheritDoc}
     */
    override fun isTriggeredByUser(userName: String): Boolean {
        val triggered = build.triggered
        return (triggered != null &&
            triggered.isUser &&
            triggered.user != null &&
            userName == triggered.user.username)
    }

    /**
     * {@inheritDoc}
     */
    override fun hasTests(): Boolean {
        return build.testOccurrences != null
    }

    /**
     * {@inheritDoc}
     */
    override val testsHref: String?
        get() = build.testOccurrences?.href

    /**
     * {@inheritDoc}
     */
    override val passedTestCount: Int
        get() = build.testOccurrences?.passed ?: 0

    /**
     * {@inheritDoc}
     */
    override val failedTestCount: Int
        get() = build.testOccurrences?.failed ?: 0

    /**
     * {@inheritDoc}
     */
    override val ignoredTestCount: Int
        get() = build.testOccurrences?.ignored ?: 0

    /**
     * {@inheritDoc}
     */
    override val artifactsHref: String?
        get() = build.artifacts?.href

    /**
     * {@inheritDoc}
     */
    override val number: String
        get() = build.number ?: TEXT_NO_NUMBER

    /**
     * {@inheritDoc}
     */
    override val isPersonal: Boolean
        get() = build.isPersonal

    /**
     * {@inheritDoc}
     */
    override val isPinned: Boolean
        get() = build.isPinned

    /**
     * {@inheritDoc}
     */
    override fun hasSnapshotDependencies(): Boolean {
        return build.snapshotBuilds != null
    }

    /**
     * {@inheritDoc}
     */
    override fun toBuild(): Build {
        return build
    }

    /**
     * Get user name of User
     *
     * @param user - User
     * @return User name
     */
    private fun getUserName(user: User?): String {
        return if (user == null) {
            TEXT_DELETED_USER
        } else {
            if (user.name == null)
                user.username
            else
                user.name
        }
    }

    companion object {
        // TODO: Use from resources
        // R.string.triggered_deleted_configuration_text
        internal const val TEXT_DELETED_CONFIGURATION = "Deleted configuration"
        // TODO: Use from resources
        // R.string.triggered_deleted_user_text
        internal const val TEXT_DELETED_USER = "Deleted user"
        // TODO: Use from resources
        internal const val TEXT_NO_NUMBER = "No number"
        // TODO: Use from resources
        internal const val TEXT_QUEUED_BUILD = "Queued build"
    }
}
