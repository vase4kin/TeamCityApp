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

package com.github.vase4kin.teamcityapp.home.tracker

import android.os.Bundle
import com.github.vase4kin.teamcityapp.app_navigation.AppNavigationItem
import com.github.vase4kin.teamcityapp.base.tracker.BaseFirebaseTracker
import com.github.vase4kin.teamcityapp.new_drawer.tracker.DrawerTracker
import com.google.firebase.analytics.FirebaseAnalytics

/**
 * Home screen tracking class
 */
class HomeTrackerImpl(firebaseAnalytics: FirebaseAnalytics) : BaseFirebaseTracker(firebaseAnalytics), HomeTracker {

    /**
     * {@inheritDoc}
     */
    override fun trackView() {
        firebaseAnalytics.logEvent(HomeTracker.SCREEN_NAME_HOME, null)
    }

    /**
     * {@inheritDoc}
     */
    override fun trackChangeAccount() {
        firebaseAnalytics.logEvent(DrawerTracker.EVENT_CHANGE_ACCOUNT, null)
    }

    /**
     * {@inheritDoc}
     */
    override fun trackUserClickOnFavFab() {
        firebaseAnalytics.logEvent(HomeTracker.EVENT_USER_CLICKS_ON_FAB, null)
    }

    /**
     * {@inheritDoc}
     */
    override fun trackUserClicksOnFavSnackBarAction() {
        firebaseAnalytics.logEvent(HomeTracker.EVENT_USER_CLICKS_SNACK_BAR_ACTION, null)
    }

    /**
     * {@inheritDoc}
     */
    override fun trackUserClicksOnRunningBuildsFilterFab() {
        firebaseAnalytics.logEvent(HomeTracker.EVENT_USER_CLICKS_ON_RUN_BUILDS_FILTER_FAB, null)
    }

    /**
     * {@inheritDoc}
     */
    override fun trackUserClicksOnBuildsQueueFilterFab() {
        firebaseAnalytics.logEvent(HomeTracker.EVENT_USER_CLICKS_ON_BUILD_QUEUE_FILTER_FAB, null)
    }

    /**
     * {@inheritDoc}
     */
    override fun trackUserClicksOnAgentsFilterFab() {
        firebaseAnalytics.logEvent(HomeTracker.EVENT_USER_CLICKS_ON_AGENTS_FILTER_FAB, null)
    }

    /**
     * {@inheritDoc}
     */
    override fun trackTabSelected(navigationItem: AppNavigationItem) {
        firebaseAnalytics.logEvent(
            HomeTracker.EVENT_USER_SELECTS_TAB,
            Bundle().apply { putString(HomeTracker.ARG_TAB, navigationItem.toString()) })
    }
}
