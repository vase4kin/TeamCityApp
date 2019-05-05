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

package com.github.vase4kin.teamcityapp.favorites.tracker;

import com.github.vase4kin.teamcityapp.base.tracker.BaseFirebaseTracker;
import com.google.firebase.analytics.FirebaseAnalytics;

/**
 * Favorites tracking class firebase impl
 */
public class FavoritesTrackerImpl extends BaseFirebaseTracker implements FavoritesTracker {

    public FavoritesTrackerImpl(FirebaseAnalytics firebaseAnalytics) {
        super(firebaseAnalytics);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackView() {
        firebaseAnalytics.logEvent(SCREEN_NAME, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackUserClickOnFab() {
        firebaseAnalytics.logEvent(EVENT_USER_CLICKS_ON_FAB, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackUserClicksOnSnackBarAction() {
        firebaseAnalytics.logEvent(EVENT_USER_CLICKS_SNACK_BAR_ACTION, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackUserOpensBuildType() {
        firebaseAnalytics.logEvent(EVENT_USER_OPENS_BUILD_TYPE, null);
    }
}
