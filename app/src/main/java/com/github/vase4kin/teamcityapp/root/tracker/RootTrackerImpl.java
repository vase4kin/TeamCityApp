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

import com.github.vase4kin.teamcityapp.base.tracker.BaseViewTracker;

import java.util.Set;

/**
 * Tracker impl
 */
public class RootTrackerImpl extends BaseViewTracker<RootTracker> implements RootTracker {

    public RootTrackerImpl(Set<RootTracker> trackers) {
        super(trackers);
    }

    @Override
    public void trackChangeAccount() {
        logEvent(new TrackerMethod<RootTracker>() {
            @Override
            public void execute(RootTracker tracker) {
                tracker.trackChangeAccount();
            }
        });
    }

    @Override
    public void trackUserRatedTheApp() {
        logEvent(new TrackerMethod<RootTracker>() {
            @Override
            public void execute(RootTracker tracker) {
                tracker.trackUserRatedTheApp();
            }
        });
    }

    @Override
    public void trackUserDidNotRateTheApp() {
        logEvent(new TrackerMethod<RootTracker>() {
            @Override
            public void execute(RootTracker tracker) {
                tracker.trackUserDidNotRateTheApp();
            }
        });
    }

    @Override
    public void trackUserDecidedToRateTheAppLater() {
        logEvent(new TrackerMethod<RootTracker>() {
            @Override
            public void execute(RootTracker tracker) {
                tracker.trackUserDecidedToRateTheAppLater();
            }
        });
    }
}
