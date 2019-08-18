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

package com.github.vase4kin.teamcityapp.navigation.data

import com.github.vase4kin.teamcityapp.base.list.view.BaseDataModel
import com.github.vase4kin.teamcityapp.navigation.api.NavigationItem

/**
 * Data model to manage navigation data
 */
interface NavigationDataModel : BaseDataModel {

    /**
     * Get project/buildType title
     *
     * @param position - Adapter position
     * @return project/buildType title
     */
    fun getName(position: Int): String

    /**
     * Get project/buildType description
     * @param position - Adapter position
     * @return project/buildType description
     */
    fun getDescription(position: Int): String

    /**
     * @param position - Adapter position
     * @return true if description exists
     */
    fun hasDescription(position: Int): Boolean

    /**
     * Is project
     */
    fun isProject(position: Int): Boolean

    /**
     * Get the whole navigation item
     *
     * @param position - Adapter position
     * @return navigation item
     */
    fun getNavigationItem(position: Int): NavigationItem

    /**
     * Get project name
     *
     * @param position - Adapter position
     * @return project name
     */
    fun getProjectName(position: Int): String

    /**
     * Get build type id
     *
     * @param position - Adapter position
     * @return project name
     */
    fun getProjectId(position: Int): String

    /**
     * There should be a comment
     */
    fun isRateTheApp(position: Int): Boolean

    /**
     * There should be a comment
     */
    fun removeItemByIndex(position: Int)
}
