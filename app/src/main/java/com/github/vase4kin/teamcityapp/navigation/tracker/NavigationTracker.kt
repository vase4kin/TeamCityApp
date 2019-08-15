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

package com.github.vase4kin.teamcityapp.navigation.tracker

import com.github.vase4kin.teamcityapp.base.tracker.ViewTracker

/**
 * Navigation tracker
 */
interface NavigationTracker : ViewTracker {

    /**
     * Track that user clicked on rate later
     */
    fun trackUserClickedOnRateCancel()

    /**
     * Track that user clicked on rate now
     */
    fun trackUserClickedOnRateNow()

    /**
     * Track user saw rate the app
     */
    fun trackUserSawRateTheApp()

    companion object {

        /**
         * Screen name
         */
        const val SCREEN_NAME = "screen_project"

        /**
         * Event 1
         */
        const val EVENT_RATE_LATER = "rate_cancel"

        /**
         * Event 2
         */
        const val EVENT_RATE_NOW = "rate_now"

        /**
         * Event 3
         */
        const val EVENT_RATE_SHOW = "rate_show"
    }
}
