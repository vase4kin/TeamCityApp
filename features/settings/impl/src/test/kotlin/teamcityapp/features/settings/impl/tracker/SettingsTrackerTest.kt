/*
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

import com.google.firebase.analytics.FirebaseAnalytics
import org.junit.Test
import org.mockito.Mockito.inOrder
import org.mockito.Mockito.mock

class SettingsTrackerTest {
    @Test fun preservesLegacyEventNamesAndPayloads() {
        val analytics = mock(FirebaseAnalytics::class.java)
        val tracker = SettingsTrackerImpl(analytics)
        tracker.trackView()
        tracker.trackLightThemeSet()
        tracker.trackDarkThemeSet()
        tracker.trackAutoBatteryThemeSet()
        tracker.trackSystemThemeSet()
        val order = inOrder(analytics)
        order.verify(analytics).logEvent("screen_settings", null)
        order.verify(analytics).logEvent("theme_set_light", null)
        order.verify(analytics).logEvent("theme_set_dark", null)
        order.verify(analytics).logEvent("theme_set_auto_battery", null)
        order.verify(analytics).logEvent("theme_set_system", null)
        order.verifyNoMoreInteractions()
    }
}
