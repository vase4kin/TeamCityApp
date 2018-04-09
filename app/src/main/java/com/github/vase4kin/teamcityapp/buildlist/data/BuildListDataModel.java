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

package com.github.vase4kin.teamcityapp.buildlist.data;

import com.github.vase4kin.teamcityapp.base.list.adapter.ModelLoadMore;
import com.github.vase4kin.teamcityapp.base.list.view.BaseDataModel;
import com.github.vase4kin.teamcityapp.buildlist.api.Build;
import com.github.vase4kin.teamcityapp.overview.data.BuildDetails;

/**
 * Model managing build list data
 */
public interface BuildListDataModel extends BaseDataModel, Iterable<BuildDetails>, ModelLoadMore<BuildListDataModel> {

    /**
     * Get branch name
     *
     * @param position - Adapter position
     * @return
     */
    String getBranchName(int position);

    /**
     * @param position - adapter position
     * @return true if model has branch name
     */
    boolean hasBranch(int position);

    /**
     * Get build status icon
     *
     * @param position - Adapter position
     * @return
     */
    String getBuildStatusIcon(int position);

    /**
     * Get build status text
     *
     * @param position - Adapter position
     * @return
     */
    String getStatusText(int position);

    /**
     * @param position - Adapter position
     * @return
     */
    String getBuildNumber(int position);

    /**
     *
     * @param position - Adapter position
     * @return
     */
    Build getBuild(int position);

    /**
     * @param position - Adapter position
     * @return Build start date
     */
    String getStartDate(int position);

    /**
     * * @param position - Adapter position
     * @return Build type id
     */
    String getBuildTypeId(int position);

    /**
     * @param position - Adapter position
     *
     * @return {true} if build has build type info
     */
    boolean hasBuildTypeInfo(int position);

    /**
     * @param position - Adapter position
     *
     * @return return full name of configuration
     */
    String getBuildTypeFullName(int position);

    /**
     * @param position - Adapter position
     * @return return name of configuration
     */
    String getBuildTypeName(int position);

    /**
     * @param position - Adapter position
     * @return {true} if build is personal
     */
    boolean isPersonal(int position);

    /**
     * @param position - Adapter position
     * @return {true} if build is pinned
     */
    boolean isPinned(int position);

    /**
     * @param position - Adapter position
     * @return {true} if build is queued
     */
    boolean isQueued(int position);
}
