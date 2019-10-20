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

import android.content.Context
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.navigation.api.BuildElement

/**
 * Impl of [OverviewDataModel]
 */
class OverviewDataModelImpl(
    private val elements: List<BuildElement>,
    private val context: Context
) : OverviewDataModel {

    /**
     * {@inheritDoc}
     */
    override val itemCount: Int
        get() = elements.size

    /**
     * {@inheritDoc}
     */
    override fun getIcon(position: Int): Int {
        return elements[position].icon
    }

    /**
     * {@inheritDoc}
     */
    override fun getDescription(position: Int): String {
        return elements[position].description
    }

    /**
     * {@inheritDoc}
     */
    override fun getHeaderName(position: Int): String {
        return elements[position].sectionName
    }

    /**
     * {@inheritDoc}
     */
    override fun isBranchCard(position: Int): Boolean {
        return context.getString(R.string.build_branch_section_text) == getHeaderName(position)
    }

    /**
     * {@inheritDoc}
     */
    override fun isBuildTypeCard(position: Int): Boolean {
        return context.getString(R.string.build_type_by_section_text) == getHeaderName(position)
    }

    /**
     * {@inheritDoc}
     */
    override fun isProjectCard(position: Int): Boolean {
        return context.getString(R.string.build_project_by_section_text) == getHeaderName(position)
    }
}
