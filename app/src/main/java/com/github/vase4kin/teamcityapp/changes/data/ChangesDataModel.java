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

package com.github.vase4kin.teamcityapp.changes.data;

import com.github.vase4kin.teamcityapp.base.list.adapter.ModelLoadMore;
import com.github.vase4kin.teamcityapp.base.list.view.BaseDataModel;
import com.github.vase4kin.teamcityapp.changes.api.Changes;

/**
 * Model to manage changes data
 */
public interface ChangesDataModel extends BaseDataModel, Iterable<Changes.Change>, ModelLoadMore<ChangesDataModel> {

    /**
     * Get change version
     *
     * @param position - Adapter position
     * @return - Change version
     */
    String getVersion(int position);

    /**
     * Get change user name
     *
     * @param position - Adapter position
     * @return User name who did the change
     */
    String getUserName(int position);

    /**
     * Get change date
     *
     * @param position - Adapter position
     * @return Date when change was made
     */
    String getDate(int position);

    /**
     * Get change comment
     *
     * @param position - Adapter position
     * @return Change comment
     */
    String getComment(int position);

    /**
     * Get changed files count
     *
     * @param position - Adapter position
     * @return Number of files changed in this change
     */
    int getFilesCount(int position);

    /**
     * Get change object
     *
     * @param position - Adapter position
     * @return the whole change
     */
    Changes.Change getChange(int position);
}
