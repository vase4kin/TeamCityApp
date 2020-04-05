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

package teamcityapp.features.drawer.tracker

import com.google.firebase.analytics.FirebaseAnalytics

class DrawerTrackerImpl(
    private val firebaseAnalytics: FirebaseAnalytics
) : DrawerTracker {

    /**
     * {@inheritDoc}
     */
    override fun trackChangeAccount() {
        firebaseAnalytics.logEvent(DrawerTracker.EVENT_CHANGE_ACCOUNT, null)
    }

    /**
     * {@inheritDoc}
     */
    override fun trackOpenPrivacy() {
        firebaseAnalytics.logEvent(DrawerTracker.EVENT_OPEN_PRIVACY, null)
    }

    /**
     * {@inheritDoc}
     */
    override fun trackRateTheApp() {
        firebaseAnalytics.logEvent(DrawerTracker.EVENT_RATE_THE_APP, null)
    }

    /**
     * {@inheritDoc}
     */
    override fun trackView() {
        firebaseAnalytics.logEvent(DrawerTracker.SCREEN_NAME, null)
    }

    /**
     * {@inheritDoc}
     */
    override fun trackOpenAbout() {
        firebaseAnalytics.logEvent(DrawerTracker.EVENT_OPEN_ABOUT, null)
    }

    /**
     * {@inheritDoc}
     */
    override fun trackOpenAddNewAccount() {
        firebaseAnalytics.logEvent(DrawerTracker.EVENT_OPEN_ADD_NEW_ACCOUNT, null)
    }

    /**
     * {@inheritDoc}
     */
    override fun trackOpenManageAccounts() {
        firebaseAnalytics.logEvent(DrawerTracker.EVENT_OPEN_MANAGE_ACCOUNTS, null)
    }

    /**
     * {@inheritDoc}
     */
    override fun trackOpenSettings() {
        firebaseAnalytics.logEvent(DrawerTracker.EVENT_OPEN_SETTINGS, null)
    }
}
