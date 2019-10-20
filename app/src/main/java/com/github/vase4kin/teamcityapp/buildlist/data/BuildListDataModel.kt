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

package com.github.vase4kin.teamcityapp.buildlist.data

import com.github.vase4kin.teamcityapp.base.list.adapter.ModelLoadMore
import com.github.vase4kin.teamcityapp.base.list.view.BaseDataModel
import com.github.vase4kin.teamcityapp.buildlist.api.Build
import com.github.vase4kin.teamcityapp.overview.data.BuildDetails

/**
 * Model managing build list data
 */
interface BuildListDataModel : BaseDataModel, Iterable<BuildDetails>,
    ModelLoadMore<BuildListDataModel> {

    /**
     * Get branch name
     *
     * @param position - Adapter position
     * @return
     */
    fun getBranchName(position: Int): String

    /**
     * Get build status icon
     *
     * @param position - Adapter position
     * @return
     */
    fun getBuildStatusIcon(position: Int): Int

    /**
     * Get build status text
     *
     * @param position - Adapter position
     * @return
     */
    fun getStatusText(position: Int): String

    /**
     * @param position - Adapter position
     * @return
     */
    fun getBuildNumber(position: Int): String

    /**
     *
     * @param position - Adapter position
     * @return
     */
    fun getBuild(position: Int): Build

    /**
     * @param position - Adapter position
     * @return Build start date
     */
    fun getStartDate(position: Int): String

    /**
     * * @param position - Adapter position
     * @return Build type id
     */
    fun getBuildTypeId(position: Int): String

    /**
     * @param position - Adapter position
     *
     * @return {true} if build has build type info
     */
    fun hasBuildTypeInfo(position: Int): Boolean

    /**
     * @param position - Adapter position
     *
     * @return return full name of configuration
     */
    fun getBuildTypeFullName(position: Int): String

    /**
     * @param position - Adapter position
     * @return return name of configuration
     */
    fun getBuildTypeName(position: Int): String

    /**
     * @param position - Adapter position
     * @return {true} if build is personal
     */
    fun isPersonal(position: Int): Boolean

    /**
     * @param position - Adapter position
     * @return {true} if build is pinned
     */
    fun isPinned(position: Int): Boolean

    /**
     * @param position - Adapter position
     * @return {true} if build is queued
     */
    fun isQueued(position: Int): Boolean
}
