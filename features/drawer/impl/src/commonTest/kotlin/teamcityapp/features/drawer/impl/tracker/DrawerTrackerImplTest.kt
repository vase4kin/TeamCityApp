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

import kotlin.test.*
import teamcityapp.libraries.analytics.*

class DrawerTrackerImplTest {
    private val records = mutableListOf<AnalyticsEvent>()
    private val analytics = AnalyticsTracker { records += it }
    private fun expect(vararg names: String) = assertEquals(names.map { AnalyticsEvent(it) }, records)

    @Test fun accountActionsRetainDrawerEventIdentities() {
        val tracker = DrawerTrackerImpl(analytics)
        tracker.trackView()
        tracker.trackChangeAccount()
        tracker.trackOpenPrivacy()
        tracker.trackRateTheApp()
        tracker.trackOpenAbout()
        tracker.trackOpenAddNewAccount()
        tracker.trackOpenManageAccounts()
        tracker.trackOpenSettings()
        expect("screen_open_drawer", "drawer_change_account", "drawer_open_privacy", "drawer_rate_the_app", "drawer_open_about", "drawer_open_add_new_account", "drawer_open_manage_accounts", "drawer_open_settings")
    }
}
