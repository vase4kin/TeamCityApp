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

package com.github.vase4kin.teamcityapp.root.tracker;

import com.github.vase4kin.teamcityapp.base.tracker.BaseFirebaseTracker;
import com.github.vase4kin.teamcityapp.drawer.tracker.DrawerTracker;
import com.google.firebase.analytics.FirebaseAnalytics;

/**
 * Root projects tracking class
 */
public class FirebaseRootTrackerImpl extends BaseFirebaseTracker implements RootTracker {

    public FirebaseRootTrackerImpl(FirebaseAnalytics firebaseAnalytics) {
        super(firebaseAnalytics);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackView() {
        firebaseAnalytics.logEvent(SCREEN_NAME_ROOT, null);
    }

    @Override
    public void trackChangeAccount() {
        firebaseAnalytics.logEvent(DrawerTracker.EVENT_CHANGE_ACCOUNT, null);
    }
}
