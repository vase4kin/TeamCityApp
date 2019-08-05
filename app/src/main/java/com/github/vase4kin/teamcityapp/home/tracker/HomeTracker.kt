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

package com.github.vase4kin.teamcityapp.home.tracker

import com.github.vase4kin.teamcityapp.drawer.tracker.DrawerTracker

/**
 * Root tracker
 */
interface HomeTracker : DrawerTracker {

    /**
     * Track user clicks on favorites fab
     */
    fun trackUserClickOnFavFab()

    /**
     * Track user clicks on snack bar action of favorites tab
     */
    fun trackUserClicksOnFavSnackBarAction()

    /**
     * Track user selects favorite tab
     */
    fun trackUserSelectsFavoritesTab()

    companion object {

        /**
         * Screen name to track
         */
        const val SCREEN_NAME_HOME = "screen_home"

        /**
         * Fav tab selected event to track
         */
        const val EVENT_USER_SELECTS_TAB_FAVORITES = "tab_favorites_selected"

        /**
         * Event
         */
        const val EVENT_USER_CLICKS_ON_FAB = "favorites_tab_click_on_fab"

        /**
         * Event
         */
        const val EVENT_USER_CLICKS_SNACK_BAR_ACTION = "favorites_tab_click_on_action"
    }
}
