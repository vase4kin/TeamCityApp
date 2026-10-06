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

package teamcityapp.features.drawer.impl.tracker

import com.google.firebase.analytics.FirebaseAnalytics
import org.junit.Test
import org.mockito.Mockito.inOrder
import org.mockito.Mockito.mock

class DrawerTrackerTest {
    @Test fun preservesLegacyEventNamesAndPayloads() {
        val analytics = mock(FirebaseAnalytics::class.java)
        val tracker = DrawerTrackerImpl(analytics)
        tracker.trackView()
        tracker.trackChangeAccount()
        tracker.trackOpenPrivacy()
        tracker.trackRateTheApp()
        tracker.trackOpenAbout()
        tracker.trackOpenAddNewAccount()
        tracker.trackOpenManageAccounts()
        tracker.trackOpenSettings()
        val order = inOrder(analytics)
        order.verify(analytics).logEvent("screen_open_drawer", null)
        order.verify(analytics).logEvent("drawer_change_account", null)
        order.verify(analytics).logEvent("drawer_open_privacy", null)
        order.verify(analytics).logEvent("drawer_rate_the_app", null)
        order.verify(analytics).logEvent("drawer_open_about", null)
        order.verify(analytics).logEvent("drawer_open_add_new_account", null)
        order.verify(analytics).logEvent("drawer_open_manage_accounts", null)
        order.verify(analytics).logEvent("drawer_open_settings", null)
        order.verifyNoMoreInteractions()
    }
}
