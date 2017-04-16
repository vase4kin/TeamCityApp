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

package com.github.vase4kin.teamcityapp.drawer.tracker;

import com.github.vase4kin.teamcityapp.base.tracker.BaseViewTracker;

import java.util.Set;

/**
 * Tracker impl
 */
public class DrawerTrackerImpl extends BaseViewTracker<DrawerTracker> implements DrawerTracker {

    public DrawerTrackerImpl(Set<DrawerTracker> trackers) {
        super(trackers);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackView() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackChangeAccount() {
        logEvent(new TrackerMethod<DrawerTracker>() {
            @Override
            public void execute(DrawerTracker tracker) {
                tracker.trackChangeAccount();
            }
        });
    }
}
