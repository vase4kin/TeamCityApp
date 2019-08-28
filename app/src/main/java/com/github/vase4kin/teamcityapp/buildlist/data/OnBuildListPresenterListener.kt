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

import com.github.vase4kin.teamcityapp.buildlist.api.Build
import com.mugen.MugenCallbacks

/**
 * On build item click listener
 */
interface OnBuildListPresenterListener : MugenCallbacks {

    /**
     * Handle click event on build item in adapter
     *
     * @param build - Build
     */
    fun onBuildClick(build: Build)

    /**
     * On run build fab button click
     */
    fun onRunBuildFabClick()

    /**
     * Show queued build snack bar
     */
    fun onShowQueuedBuildSnackBarClick()

    /**
     * Show favorites
     */
    fun onNavigateToFavorites()

    /**
     * On filter builds option menu click
     */
    fun onFilterBuildsOptionMenuClick()

    /**
     * On add to favorites build click
     */
    fun onAddToFavoritesOptionMenuClick()

    /**
     * On reset filters action click
     */
    fun onResetFiltersSnackBarActionClick()

    companion object {
        val EMPTY = object : OnBuildListPresenterListener {
            override fun onBuildClick(build: Build) {
            }

            override fun onRunBuildFabClick() {
            }

            override fun onShowQueuedBuildSnackBarClick() {
            }

            override fun onNavigateToFavorites() {
            }

            override fun onFilterBuildsOptionMenuClick() {
            }

            override fun onAddToFavoritesOptionMenuClick() {
            }

            override fun onResetFiltersSnackBarActionClick() {
            }

            override fun onLoadMore() {
            }

            override fun isLoading(): Boolean = false

            override fun hasLoadedAllItems(): Boolean = false
        }
    }
}
