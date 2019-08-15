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

import com.github.vase4kin.teamcityapp.navigation.api.BuildType
import com.github.vase4kin.teamcityapp.navigation.api.NavigationItem
import com.github.vase4kin.teamcityapp.navigation.api.Project
import com.github.vase4kin.teamcityapp.navigation.api.RateTheApp

/**
 * Impl of [NavigationDataModel]
 */
class NavigationDataModelImpl(private val items: MutableList<NavigationItem>) : NavigationDataModel {

    /**
     * {@inheritDoc}
     */
    override val itemCount: Int
        get() = items.size

    /**
     * {@inheritDoc}
     */
    override fun getName(position: Int): String {
        return items[position].name
    }

    /**
     * {@inheritDoc}
     */
    override fun getDescription(position: Int): String {
        return items[position].description
    }

    /**
     * {@inheritDoc}
     */
    override fun hasDescription(position: Int): Boolean {
        return items[position].description != null
    }

    /**
     * {@inheritDoc}
     */
    override fun isProject(position: Int): Boolean {
        return items[position] is Project
    }

    /**
     * {@inheritDoc}
     */
    override fun getNavigationItem(position: Int): NavigationItem {
        return items[position]
    }

    /**
     * {@inheritDoc}
     */
    override fun getProjectName(position: Int): String {
        return (items[position] as BuildType).projectName
    }

    /**
     * {@inheritDoc}
     */
    override fun getProjectId(position: Int): String {
        return (items[position] as BuildType).projectId
    }

    /**
     * {@inheritDoc}
     */
    override fun isRateTheApp(position: Int): Boolean {
        return items[position] is RateTheApp
    }

    /**
     * {@inheritDoc}
     */
    override fun removeItemByIndex(position: Int) {
        items.removeAt(position)
    }
}
