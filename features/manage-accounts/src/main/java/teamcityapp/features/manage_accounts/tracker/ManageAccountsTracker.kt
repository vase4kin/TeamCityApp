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

package teamcityapp.features.manage_accounts.tracker

/**
 * Tracker interface
 */
interface ManageAccountsTracker {

    /**
     * Track view
     */
    fun trackView()

    /**
     * Track account is removed
     */
    fun trackAccountRemove()

    /**
     * Track user clicks on ssl disabled warning
     */
    fun trackUserClicksOnSslDisabledWarning()

    companion object {

        /**
         * Account list screen name
         */
        const val SCREEN_NAME = "screen_account_list"

        /**
         * Remove account event
         */
        const val EVENT_REMOVE_ACCOUNT = "remove_account"

        /**
         * Remove account event
         */
        const val EVENT_SSL_WARNING = "account_ssl_warning"
    }
}
