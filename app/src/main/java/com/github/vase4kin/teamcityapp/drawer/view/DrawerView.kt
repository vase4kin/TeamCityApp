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

package com.github.vase4kin.teamcityapp.drawer.view

import com.github.vase4kin.teamcityapp.drawer.data.DrawerDataModel

/**
 * View interactions for drawer
 */
interface DrawerView {

    /**
     * Is model empty
     */
    val isModelEmpty: Boolean

    /**
     * Init views
     *
     * @param listener - Listener to receive view callbacks
     */
    fun initViews(listener: OnDrawerPresenterListener)

    /**
     * Show drawer data
     *
     * @param dataModel - Drawer data
     */
    fun showData(dataModel: DrawerDataModel)

    /**
     * On back button pressed
     */
    fun backButtonPressed()

    /**
     * Update agents badge
     *
     * @param count - Number to update
     */
    fun updateAgentsBadge(count: Int)

    companion object {

        /**
         * Drawer item ids
         */
        const val NO_SELECTION = -1
        const val HOME = 0
        const val AGENTS = 1
        const val PROFILES_MANAGING = 2
        const val ABOUT = 3
    }
}
