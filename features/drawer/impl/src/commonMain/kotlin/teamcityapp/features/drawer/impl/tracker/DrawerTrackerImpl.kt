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

package teamcityapp.features.drawer.impl.tracker

import teamcityapp.libraries.analytics.*

class DrawerTrackerImpl(private val analytics: AnalyticsTracker) : DrawerTracker {
    override fun trackChangeAccount() = analytics.track(DrawerTracker.EVENT_CHANGE_ACCOUNT)
    override fun trackOpenPrivacy() = analytics.track(DrawerTracker.EVENT_OPEN_PRIVACY)
    override fun trackRateTheApp() = analytics.track(DrawerTracker.EVENT_RATE_THE_APP)
    override fun trackOpenAbout() = analytics.track(DrawerTracker.EVENT_OPEN_ABOUT)
    override fun trackOpenAddNewAccount() = analytics.track(DrawerTracker.EVENT_OPEN_ADD_NEW_ACCOUNT)
    override fun trackOpenManageAccounts() = analytics.track(DrawerTracker.EVENT_OPEN_MANAGE_ACCOUNTS)
    override fun trackOpenSettings() = analytics.track(DrawerTracker.EVENT_OPEN_SETTINGS)
    override fun trackView() = analytics.track(DrawerTracker.SCREEN_NAME)
}
