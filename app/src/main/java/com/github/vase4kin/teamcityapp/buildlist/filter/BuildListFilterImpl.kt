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

package com.github.vase4kin.teamcityapp.buildlist.filter

import com.github.vase4kin.teamcityapp.filter_builds.view.FilterBuildsView

/**
 * Impl of [BuildListFilter]
 */
class BuildListFilterImpl : BuildListFilter {

    private var filterType: Int = FilterBuildsView.FILTER_NONE
    private var branch: String? = null
    private var isPersonal = false
    private var isPinned = false

    /**
     * {@inheritDoc}
     */
    override fun setFilter(filter: Int) {
        this.filterType = filter
    }

    /**
     * {@inheritDoc}
     */
    override fun setBranch(branch: String) {
        this.branch = branch
    }

    /**
     * {@inheritDoc}
     */
    override fun setPersonal(isPersonal: Boolean) {
        this.isPersonal = isPersonal
    }

    /**
     * {@inheritDoc}
     */
    override fun setPinned(isPinned: Boolean) {
        this.isPinned = isPinned
    }

    /**
     * {@inheritDoc}
     */
    override fun toLocator(): String {
        val locatorBuilder = StringBuilder()
        when (filterType) {
            FilterBuildsView.FILTER_SUCCESS -> locatorBuilder.append("status:SUCCESS")
            FilterBuildsView.FILTER_FAILED -> locatorBuilder.append("status:FAILURE")
            FilterBuildsView.FILTER_ERROR -> locatorBuilder.append("status:ERROR")
            FilterBuildsView.FILTER_CANCELLED -> locatorBuilder.append("canceled:true")
            FilterBuildsView.FILTER_FAILED_TO_START -> locatorBuilder.append("failedToStart:true")
            FilterBuildsView.FILTER_RUNNING -> locatorBuilder.append("running:true")
            FilterBuildsView.FILTER_QUEUED -> locatorBuilder.append("state:queued")
            FilterBuildsView.FILTER_NONE -> locatorBuilder.append("state:any,canceled:any,failedToStart:any")
            else -> locatorBuilder.append("state:any,canceled:any,failedToStart:any")
        }
        locatorBuilder.append(",")
        if (!branch.isNullOrEmpty()) {
            locatorBuilder.append("branch:name:")
            locatorBuilder.append(branch)
        } else {
            locatorBuilder.append("branch:default:any")
        }
        locatorBuilder.append(",")
        locatorBuilder.append("personal:")
        locatorBuilder.append(isPersonal)
        locatorBuilder.append(",")
        locatorBuilder.append("pinned:")
        // Queued builds can be shown if only pinned:any, because queued builds can't be pinned
        // ONLY DO SO FOR QUEUED BUILDS FILTER (For now it's expected behavior)
        if (filterType == FilterBuildsView.FILTER_QUEUED) {
            locatorBuilder.append("any")
        } else {
            locatorBuilder.append(isPinned)
        }
        // Remove count for queued and running build cause they don't have next href
        if (filterType == FilterBuildsView.FILTER_RUNNING) {
            return locatorBuilder.toString()
        }
        if (filterType == FilterBuildsView.FILTER_QUEUED) {
            return locatorBuilder.toString()
        }
        locatorBuilder.append(",")
        locatorBuilder.append("count:10")
        return locatorBuilder.toString()
    }
}
