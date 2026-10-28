/*
 * Copyright 2020 Andrey Tolpeev
 * Copyright 2026 Andrey Tolpeev
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

package teamcityapp.features.settings.impl.tracker

import teamcityapp.libraries.analytics.*

class SettingsTrackerImpl(private val analytics: AnalyticsTracker) : SettingsTracker {
    override fun trackView() = analytics.track(SettingsTracker.SCREEN_NAME)
    override fun trackLightThemeSet() = analytics.track(SettingsTracker.EVENT_NAME_LIGHT_THEME)
    override fun trackDarkThemeSet() = analytics.track(SettingsTracker.EVENT_NAME_DARK_THEME)
    override fun trackAutoBatteryThemeSet() = analytics.track(SettingsTracker.EVENT_NAME_AUTO_BATTERY_THEME)
    override fun trackSystemThemeSet() = analytics.track(SettingsTracker.EVENT_NAME_SYSTEM_THEME)
}
