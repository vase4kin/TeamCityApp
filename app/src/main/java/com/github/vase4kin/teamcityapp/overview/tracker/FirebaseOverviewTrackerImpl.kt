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

package com.github.vase4kin.teamcityapp.overview.tracker

import com.github.vase4kin.teamcityapp.base.tracker.BaseFirebaseTracker
import com.google.firebase.analytics.FirebaseAnalytics

/**
 * Impl of [OverviewTracker]
 */
class FirebaseOverviewTrackerImpl(firebaseAnalytics: FirebaseAnalytics) : BaseFirebaseTracker(firebaseAnalytics),
    OverviewTracker {

    /**
     * {@inheritDoc}
     */
    override fun trackUserClickedCancelBuildOption() {
        firebaseAnalytics.logEvent(OverviewTracker.EVENT_CANCEL_BUILD, null)
    }

    /**
     * {@inheritDoc}
     */
    override fun trackUserSharedBuild() {
        firebaseAnalytics.logEvent(OverviewTracker.EVENT_SHARE_BUILD, null)
    }

    /**
     * {@inheritDoc}
     */
    override fun trackView() {
        // Do nothing
    }

    /**
     * {@inheritDoc}
     */
    override fun trackUserRestartedBuild() {
        firebaseAnalytics.logEvent(OverviewTracker.EVENT_RESTART_BUILD, null)
    }

    /**
     * {@inheritDoc}
     */
    override fun trackUserOpenBrowser() {
        firebaseAnalytics.logEvent(OverviewTracker.EVENT_OPEN_BROWSER_BUILD, null)
    }

    /**
     * {@inheritDoc}
     */
    override fun trackUserWantsToSeeBuildListFilteredByBranch() {
        firebaseAnalytics.logEvent(OverviewTracker.EVENT_SHOW_BUILDS_FILTERED_BY_BRANCH, null)
    }

    /**
     * {@inheritDoc}
     */
    override fun trackUserOpensBuildType() {
        firebaseAnalytics.logEvent(OverviewTracker.EVENT_OPEN_BUILD_TYPE, null)
    }

    /**
     * {@inheritDoc}
     */
    override fun trackUserOpensProject() {
        firebaseAnalytics.logEvent(OverviewTracker.EVENT_OPEN_PROJECT, null)
    }
}
