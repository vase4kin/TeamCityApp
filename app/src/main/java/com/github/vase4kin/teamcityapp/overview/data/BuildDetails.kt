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
import com.github.vase4kin.teamcityapp.properties.api.Properties
import teamcityapp.libraries.api.Jsonable

/**
 * Interface to handle build details
 */
interface BuildDetails : Jsonable {

    /**
     * @return {String} as server build url
     */
    val href: String

    /**
     * @return {String} as server build web url
     */
    val webUrl: String

    /**
     * @return build changes url as {String}
     */
    val changesHref: String?

    /**
     * @return build status text as {String}
     */
    val statusText: String

    /**
     * @return as status icon as {String}
     */
    val statusIcon: Int

    /**
     * @return {true} if build is running
     */
    val isRunning: Boolean

    /**
     * @return {true} if build is finished
     */
    val isFinished: Boolean

    /**
     * @return {true} if build is queued
     */
    val isQueued: Boolean

    /**
     * @return {true} if build is success
     */
    val isSuccess: Boolean

    /**
     * @return {true} if build is failed
     */
    val isFailed: Boolean

    /**
     * @return user name as {String}
     */
    val userNameWhoCancelledBuild: String?

    /**
     * @return cancellation time as {String}
     */
    val cancellationTime: String?

    /**
     * @return start date as {String}
     */
    val startDate: String

    /**
     * @return start date as {String} formatted specifically for header
     */
    val startDateFormattedAsHeader: String

    /**
     * @return queued date as {String}
     */
    val queuedDate: String

    /**
     * @return finish date as {String}
     */
    val finishTime: String

    /**
     * @return estimated start time as {String}. Can be null
     */
    val estimatedStartTime: String?

    /**
     * @return branch name as {String}. Can be null
     */
    val branchName: String?

    /**
     * @return agent name as {String}
     */
    val agentName: String?

    /**
     * @return {true} if build was triggered by vcs
     */
    val isTriggeredByVcs: Boolean

    /**
     * @return {true} if build was triggered by user
     */
    val isTriggeredByUser: Boolean

    /**
     * @return {true} if build was restarted
     */
    val isRestarted: Boolean

    /**
     * @return {true} if build was triggered by build type
     */
    val isTriggeredByBuildType: Boolean

    /**
     * @return {true} if build was triggered by unknown
     */
    val isTriggeredByUnknown: Boolean

    /**
     * @return triggered details as {String}
     */
    val triggeredDetails: String?

    /**
     * @return User name of the user who triggered build
     */
    val userNameOfUserWhoTriggeredBuild: String?

    /**
     * @return Name of configuration who triggered build
     */
    val nameOfTriggeredBuildType: String?

    /**
     * @return id of configuration
     */
    val buildTypeId: String

    /**
     * @return full name of configuration
     */
    val buildTypeFullName: String

    /**
     * @return return name of configuration
     */
    val buildTypeName: String?

    /**
     * @return return project of build
     */
    val projectName: String?

    /**
     * @return project id of build type
     */
    val projectId: String?

    /**
     * @return properties
     */
    val properties: Properties?

    /**
     * @return build tests href as {String}
     */
    val testsHref: String?

    /**
     * @return passed tests count as {int}
     */
    val passedTestCount: Int

    /**
     * @return failed tests count as {int}
     */
    val failedTestCount: Int

    /**
     * @return ignored tests count as {int}
     */
    val ignoredTestCount: Int

    /**
     * @return build artifacts href as {String}
     */
    val artifactsHref: String?

    /**
     * @return build number as {String}
     */
    val number: String

    /**
     * @return {true} if the build is personal
     */
    val isPersonal: Boolean

    /**
     * @return {true} if the build is pinned
     */
    val isPinned: Boolean

    /**
     * @return {true} if build has cancellation info
     */
    fun hasCancellationInfo(): Boolean

    /**
     * @return {true} if was cancelled by user
     */
    fun hasUserInfoWhoCancelledBuild(): Boolean

    /**
     * @return {true} if build has agent
     */
    fun hasAgentInfo(): Boolean

    /**
     * @return {true} if build has build type info
     */
    fun hasBuildTypeInfo(): Boolean

    /**
     * @return id of the build
     */
    override fun getId(): String

    /**
     * @return {true} if the build has tests
     */
    fun hasTests(): Boolean

    /**
     * @param userName - User name
     * @return {true} if build was triggered by user with name
     */
    fun isTriggeredByUser(userName: String): Boolean

    /**
     * @return {true} if the build has snapshot dependencies builds
     */
    fun hasSnapshotDependencies(): Boolean

    /**
     * @return build wrapped by build details
     */
    fun toBuild(): Build

    companion object {

        /**
         * Build queued state
         */
        const val STATE_QUEUED = "queued"

        /**
         * Build running state
         */
        const val STATE_RUNNING = "running"
        /**
         * Build finished state
         */
        const val STATE_FINISHED = "finished"
        /**
         * Build success status
         */
        const val STATUS_SUCCESS = "SUCCESS"
        /**
         * Build failure status
         */
        const val STATUS_FAILURE = "FAILURE"
        /**
         * Build error status
         */
        const val STATUS_ERROR = "ERROR"
        /**
         * Build unknown status
         */
        const val STATUS_UNKNOWN = "UNKNOWN"

        /**
         * Stub
         */
        val STUB: BuildDetails = object : BuildDetails {
            override val href: String
                get() = ""

            override val webUrl: String
                get() = ""

            override val changesHref: String?
                get() = null

            override val statusText: String
                get() = ""

            override val statusIcon: Int
                get() = 0

            override val isRunning: Boolean
                get() = false

            override val isFinished: Boolean
                get() = false

            override val isQueued: Boolean
                get() = false

            override val isSuccess: Boolean
                get() = false

            override val isFailed: Boolean
                get() = false

            override val userNameWhoCancelledBuild: String?
                get() = null

            override val cancellationTime: String?
                get() = null

            override val startDate: String
                get() = ""

            override val startDateFormattedAsHeader: String
                get() = ""

            override val queuedDate: String
                get() = ""

            override val finishTime: String
                get() = ""

            override val estimatedStartTime: String?
                get() = null

            override val branchName: String?
                get() = null

            override val agentName: String?
                get() = null

            override val isTriggeredByVcs: Boolean
                get() = false

            override val isTriggeredByUser: Boolean
                get() = false

            override val isRestarted: Boolean
                get() = false

            override val isTriggeredByBuildType: Boolean
                get() = false

            override val isTriggeredByUnknown: Boolean
                get() = false

            override val triggeredDetails: String?
                get() = null

            override val userNameOfUserWhoTriggeredBuild: String?
                get() = null

            override val nameOfTriggeredBuildType: String?
                get() = null

            override val buildTypeId: String
                get() = ""

            override val buildTypeFullName: String
                get() = ""

            override val buildTypeName: String?
                get() = null

            override val projectName: String?
                get() = null

            override val projectId: String?
                get() = null

            override val properties: Properties?
                get() = null

            override val testsHref: String?
                get() = null

            override val passedTestCount: Int
                get() = 0

            override val failedTestCount: Int
                get() = 0

            override val ignoredTestCount: Int
                get() = 0

            override val artifactsHref: String?
                get() = null

            override val number: String
                get() = ""

            override val isPersonal: Boolean
                get() = false

            override val isPinned: Boolean
                get() = false

            override fun hasCancellationInfo(): Boolean {
                return false
            }

            override fun hasUserInfoWhoCancelledBuild(): Boolean {
                return false
            }

            override fun hasAgentInfo(): Boolean {
                return false
            }

            override fun hasBuildTypeInfo(): Boolean {
                return false
            }

            override fun getId(): String {
                return "id"
            }

            override fun hasTests(): Boolean {
                return false
            }

            override fun isTriggeredByUser(userName: String): Boolean {
                return false
            }

            override fun hasSnapshotDependencies(): Boolean {
                return false
            }

            override fun toBuild(): Build {
                return Build()
            }
        }
    }
}
