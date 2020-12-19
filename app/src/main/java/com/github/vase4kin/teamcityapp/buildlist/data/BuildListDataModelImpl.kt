/*
 * Copyright 2020 Andrey Tolpeev
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

package com.github.vase4kin.teamcityapp.buildlist.data

import androidx.annotation.VisibleForTesting
import com.github.vase4kin.teamcityapp.buildlist.api.Build
import com.github.vase4kin.teamcityapp.overview.data.BuildDetails
import com.github.vase4kin.teamcityapp.overview.data.BuildDetailsImpl
import com.github.vase4kin.teamcityapp.utils.AnimationUtils
import java.util.UUID

/**
 * Impl of [BuildListDataModel]
 */
class BuildListDataModelImpl(private val buildDetailsList: MutableList<BuildDetails>) : BuildListDataModel {

    /**
     * {@inheritDoc}
     */
    override val itemCount: Int
        get() = buildDetailsList.size

    /**
     * {@inheritDoc}
     */
    override fun getBranchName(position: Int): String {
        return buildDetailsList[position].branchName ?: ""
    }

    /**
     * {@inheritDoc}
     */
    override fun getBuildStatusIcon(position: Int): Int {
        return buildDetailsList[position].statusIcon
    }

    /**
     * {@inheritDoc}
     */
    override fun isRunning(position: Int): Boolean {
        return AnimationUtils.isAnimationOn && buildDetailsList[position].isRunning
    }

    /**
     * {@inheritDoc}
     */
    override fun getStatusText(position: Int): String {
        return buildDetailsList[position].statusText
    }

    /**
     * {@inheritDoc}
     */
    override fun getBuildNumber(position: Int): String {
        val buildNumber = buildDetailsList[position].number
        return if (buildNumber != null)
            String.format("#%s", buildNumber)
        else
            ""
    }

    /**
     * {@inheritDoc}
     */
    override fun getBuild(position: Int): Build {
        return buildDetailsList[position].toBuild()
    }

    /**
     * {@inheritDoc}
     */
    override fun isLoadMore(position: Int): Boolean {
        return buildDetailsList[position] == LOAD_MORE
    }

    /**
     * {@inheritDoc}
     */
    override fun addLoadMore() {
        buildDetailsList.add(LOAD_MORE)
    }

    /**
     * {@inheritDoc}
     */
    override fun removeLoadMore() {
        buildDetailsList.remove(LOAD_MORE)
    }

    /**
     * {@inheritDoc}
     */
    override fun addMoreBuilds(dataModel: BuildListDataModel) {
        val moreBuilds = mutableListOf<BuildDetails>()
        dataModel.iterator().forEach {
            moreBuilds.add(it)
        }
        buildDetailsList.addAll(moreBuilds)
    }

    /**
     * {@inheritDoc}
     */
    override fun getStartDate(position: Int): String {
        return buildDetailsList[position].startDateFormattedAsHeader
    }

    /**
     * {@inheritDoc}
     */
    override fun getBuildTypeId(position: Int): String {
        return buildDetailsList[position].buildTypeId
    }

    /**
     * {@inheritDoc}
     */
    override fun hasBuildTypeInfo(position: Int): Boolean {
        return buildDetailsList[position].hasBuildTypeInfo()
    }

    /**
     * {@inheritDoc}
     */
    override fun getBuildTypeFullName(position: Int): String {
        return buildDetailsList[position].buildTypeFullName
    }

    /**
     * {@inheritDoc}
     */
    override fun getBuildTypeName(position: Int): String {
        return buildDetailsList[position].buildTypeName ?: ""
    }

    /**
     * {@inheritDoc}
     */
    override fun isPersonal(position: Int): Boolean {
        return buildDetailsList[position].isPersonal
    }

    /**
     * {@inheritDoc}
     */
    override fun isPinned(position: Int): Boolean {
        return buildDetailsList[position].isPinned
    }

    /**
     * {@inheritDoc}
     */
    override fun isQueued(position: Int): Boolean {
        return buildDetailsList[position].isQueued
    }

    override fun isSuccess(position: Int): Boolean {
        return buildDetailsList[position].isSuccess
    }

    override fun isFailed(position: Int): Boolean {
        return buildDetailsList[position].isFailed
    }

    /**
     * {@inheritDoc}
     */
    override fun iterator(): Iterator<BuildDetails> {
        return buildDetailsList.iterator()
    }

    companion object {

        /**
         * Load more
         */
        @VisibleForTesting
        val LOAD_MORE: BuildDetails = BuildDetailsImpl(object : Build() {
            override fun getId(): String {
                return UUID.randomUUID().toString()
            }
        })
    }
}
