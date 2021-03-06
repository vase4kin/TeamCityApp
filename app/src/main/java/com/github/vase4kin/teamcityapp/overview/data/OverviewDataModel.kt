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

import com.github.vase4kin.teamcityapp.base.list.view.BaseDataModel

/**
 * Data model to manage build details items
 */
interface OverviewDataModel : BaseDataModel {

    /**
     * @param position - Adapter position
     * @return Icon
     */
    fun getIcon(position: Int): Int

    /**
     * Is build running
     */
    fun isRunning(position: Int): Boolean

    /**
     * @param position - Adapter position
     * @return Description for element
     */
    fun getDescription(position: Int): String

    /**
     * @param position - Adapter position
     * @return Title of element
     */
    fun getHeaderName(position: Int): String

    /**
     * @param position - Adapter position
     * @return {true} if card is branch one
     */
    fun isBranchCard(position: Int): Boolean

    /**
     * @param position - Adapter position
     * @return {true} if card is build type one
     */
    fun isBuildTypeCard(position: Int): Boolean

    /**
     * @param position - Adapter position
     * @return {true} if card is project one
     */
    fun isProjectCard(position: Int): Boolean
}
