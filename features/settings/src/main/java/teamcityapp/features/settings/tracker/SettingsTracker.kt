/*
 * Copyright 2020 Andrey Tolpeev
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

package teamcityapp.features.settings.tracker

import com.google.firebase.analytics.FirebaseAnalytics

interface SettingsTracker {
    fun trackLightThemeSet()
    fun trackDarkThemeSet()
    fun trackAutoBatteryThemeSet()
    fun trackSystemThemeSet()
    fun trackView()

    companion object {

        /**
         * Screen name to track
         */
        const val SCREEN_NAME = "screen_settings"

        /**
         * Event name to track setting light time
         */
        const val EVENT_NAME_LIGHT_THEME = "theme_set_light"

        /**
         * Event name to track setting dark time
         */
        const val EVENT_NAME_DARK_THEME = "theme_set_dark"

        /**
         * Event name to track setting auto-battery time
         */
        const val EVENT_NAME_AUTO_BATTERY_THEME = "theme_set_auto_battery"

        /**
         * Event name to track setting follow system time
         */
        const val EVENT_NAME_SYSTEM_THEME = "theme_set_system"
    }
}

class SettingsTrackerImpl(
    private val firebaseAnalytics: FirebaseAnalytics
): SettingsTracker {

    /**
     * {@inheritDoc}
     */
    override fun trackView() {
        firebaseAnalytics.logEvent(SettingsTracker.SCREEN_NAME, null)
    }

    /**
     * {@inheritDoc}
     */
    override fun trackLightThemeSet() {
        firebaseAnalytics.logEvent(SettingsTracker.EVENT_NAME_LIGHT_THEME, null)
    }

    /**
     * {@inheritDoc}
     */
    override fun trackDarkThemeSet() {
        firebaseAnalytics.logEvent(SettingsTracker.EVENT_NAME_DARK_THEME, null)
    }

    /**
     * {@inheritDoc}
     */
    override fun trackAutoBatteryThemeSet() {
        firebaseAnalytics.logEvent(SettingsTracker.EVENT_NAME_AUTO_BATTERY_THEME, null)
    }

    /**
     * {@inheritDoc}
     */
    override fun trackSystemThemeSet() {
        firebaseAnalytics.logEvent(SettingsTracker.EVENT_NAME_SYSTEM_THEME, null)
    }
}


