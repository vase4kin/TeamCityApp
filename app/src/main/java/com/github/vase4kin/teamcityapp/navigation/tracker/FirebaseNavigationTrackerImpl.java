/*
 * Copyright 2016 Andrey Tolpeev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.vase4kin.teamcityapp.navigation.tracker;

import com.github.vase4kin.teamcityapp.base.tracker.BaseFirebaseTracker;
import com.google.firebase.analytics.FirebaseAnalytics;

/**
 * Navigation tracking class firebase impl
 */
public class FirebaseNavigationTrackerImpl extends BaseFirebaseTracker implements NavigationTracker {

    public FirebaseNavigationTrackerImpl(FirebaseAnalytics firebaseAnalytics) {
        super(firebaseAnalytics);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackView() {
        mFirebaseAnalytics.logEvent(SCREEN_NAME, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackUserClickedOnRateLater() {
        mFirebaseAnalytics.logEvent(EVENT_RATE_LATER, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackUserClickedOnRateNow() {
        mFirebaseAnalytics.logEvent(EVENT_RATE_NOW, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackUserSawRateTheApp() {
        mFirebaseAnalytics.logEvent(EVENT_RATE_SHOW, null);
    }
}
