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

package com.github.vase4kin.teamcityapp.build_details.tracker;

import com.github.vase4kin.teamcityapp.base.tracker.BaseViewTracker;

import java.util.Set;

/**
 * Tracker impl
 */
public class BuildDetailsTrackerImpl extends BaseViewTracker<BuildDetailsTracker> implements BuildDetailsTracker {

    public BuildDetailsTrackerImpl(Set<BuildDetailsTracker> trackers) {
        super(trackers);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackUserConfirmedCancel(final boolean isReAddToTheQueue) {
        logEvent(new TrackerMethod<BuildDetailsTracker>() {
            @Override
            public void execute(BuildDetailsTracker tracker) {
                tracker.trackUserConfirmedCancel(isReAddToTheQueue);
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackUserGetsForbiddenErrorOnBuildCancel() {
        logEvent(new TrackerMethod<BuildDetailsTracker>() {
            @Override
            public void execute(BuildDetailsTracker tracker) {
                tracker.trackUserGetsForbiddenErrorOnBuildCancel();
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackUserGetsServerErrorOnBuildCancel() {
        logEvent(new TrackerMethod<BuildDetailsTracker>() {
            @Override
            public void execute(BuildDetailsTracker tracker) {
                tracker.trackUserGetsServerErrorOnBuildCancel();
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackUserCanceledBuildSuccessfully() {
        logEvent(new TrackerMethod<BuildDetailsTracker>() {
            @Override
            public void execute(BuildDetailsTracker tracker) {
                tracker.trackUserCanceledBuildSuccessfully();
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackUserGetsForbiddenErrorOnBuildRestart() {
        logEvent(new TrackerMethod<BuildDetailsTracker>() {
            @Override
            public void execute(BuildDetailsTracker tracker) {
                tracker.trackUserGetsForbiddenErrorOnBuildRestart();
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackUserGetsServerErrorOnBuildRestart() {
        logEvent(new TrackerMethod<BuildDetailsTracker>() {
            @Override
            public void execute(BuildDetailsTracker tracker) {
                tracker.trackUserGetsServerErrorOnBuildRestart();
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackUserRestartedBuildSuccessfully() {
        logEvent(new TrackerMethod<BuildDetailsTracker>() {
            @Override
            public void execute(BuildDetailsTracker tracker) {
                tracker.trackUserRestartedBuildSuccessfully();
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackUserWantsToSeeQueuedBuildDetails() {
        logEvent(new TrackerMethod<BuildDetailsTracker>() {
            @Override
            public void execute(BuildDetailsTracker tracker) {
                tracker.trackUserWantsToSeeQueuedBuildDetails();
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackUserFailedToSeeQueuedBuildDetails() {
        logEvent(new TrackerMethod<BuildDetailsTracker>() {
            @Override
            public void execute(BuildDetailsTracker tracker) {
                tracker.trackUserFailedToSeeQueuedBuildDetails();
            }
        });
    }
}
