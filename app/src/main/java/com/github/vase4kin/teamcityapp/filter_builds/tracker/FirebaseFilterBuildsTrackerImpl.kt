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

package com.github.vase4kin.teamcityapp.filter_builds.tracker

import com.github.vase4kin.teamcityapp.base.tracker.BaseFirebaseTracker
import com.google.firebase.analytics.FirebaseAnalytics

/**
 * Tracker firebase impl of [FilterBuildsTracker]
 */
class FirebaseFilterBuildsTrackerImpl(
    firebaseAnalytics: FirebaseAnalytics
) : BaseFirebaseTracker(firebaseAnalytics), FilterBuildsTracker {

    /**
     * {@inheritDoc}
     */
    override fun trackUserFilteredBuilds() {
        firebaseAnalytics.logEvent(FilterBuildsTracker.EVENT_RUN_BUILD_BUTTON_PRESSED, null)
    }

    /**
     * {@inheritDoc}
     */
    override fun trackView() {
        firebaseAnalytics.logEvent(FilterBuildsTracker.SCREEN_NAME_FILTER_BUILDS, null)
    }
}
