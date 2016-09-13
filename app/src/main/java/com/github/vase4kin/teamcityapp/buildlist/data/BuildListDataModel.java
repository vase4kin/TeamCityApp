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

import com.github.vase4kin.teamcityapp.base.list.view.BaseDataModel;
import com.github.vase4kin.teamcityapp.buildlist.api.Build;

/**
 * Model managing build list data
 */
public interface BuildListDataModel extends BaseDataModel, Iterable<Build> {

    /**
     * Is it an instance of {@link com.github.vase4kin.teamcityapp.buildlist.api.LoadMoreBuild} in adaper
     *
     * @param position - Adapter position
     */
    boolean isLoadMoreBuild(int position);

    /**
     * Get branch name
     *
     * @param position - Adapter position
     * @return
     */
    String getBranchName(int position);

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
     * Add build to data model
     *
     * @param build - Build to add
     */
    void add(Build build);

    /**
     * Remove build from data model
     *
     * @param build - Build to remove
     */
    void remove(Build build);

    /**
     * Add new model to current model
     *
     * @param dataModel
     */
    void add(BuildListDataModel dataModel);

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
}
