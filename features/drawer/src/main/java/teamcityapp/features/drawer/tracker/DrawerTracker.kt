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

package teamcityapp.features.drawer.tracker

interface DrawerTracker {

    /**
     * Track account was changed
     */
    fun trackChangeAccount()

    /**
     * Track open privacy
     */
    fun trackOpenPrivacy()

    /**
     * Track rate the app
     */
    fun trackRateTheApp()

    /**
     * Track open about
     */
    fun trackOpenAbout()

    /**
     * Track open add new account
     */
    fun trackOpenAddNewAccount()

    /**
     * Track open manage accounts
     */
    fun trackOpenManageAccounts()

    /**
     * Track view
     */
    fun trackView()

    companion object {

        /**
         * Screen name to track
         */
        const val SCREEN_NAME = "screen_open_drawer"

        /**
         * Event change name
         */
        const val EVENT_CHANGE_ACCOUNT = "drawer_change_account"

        /**
         * Event open privacy
         */
        const val EVENT_OPEN_PRIVACY = "drawer_open_privacy"

        /**
         * Event rate the app
         */
        const val EVENT_RATE_THE_APP = "drawer_rate_the_app"

        /**
         * Event open about
         */
        const val EVENT_OPEN_ABOUT = "drawer_open_about"

        /**
         * Event open add new account
         */
        const val EVENT_OPEN_ADD_NEW_ACCOUNT = "drawer_open_add_new_account"

        /**
         * Event open manage accounts
         */
        const val EVENT_OPEN_MANAGE_ACCOUNTS = "drawer_open_manage_accounts"
    }
}
