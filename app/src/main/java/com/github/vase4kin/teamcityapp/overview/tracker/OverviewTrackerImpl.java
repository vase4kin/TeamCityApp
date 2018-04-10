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

package com.github.vase4kin.teamcityapp.overview.tracker;

import com.github.vase4kin.teamcityapp.base.tracker.BaseViewTracker;

import java.util.Set;

/**
 * Impl of {@link OverviewTracker}
 */
public class OverviewTrackerImpl extends BaseViewTracker<OverviewTracker> implements OverviewTracker {

    public OverviewTrackerImpl(Set<OverviewTracker> trackers) {
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
    public void trackUserClickedCancelBuildOption() {
        logEvent(new TrackerMethod<OverviewTracker>() {
            @Override
            public void execute(OverviewTracker tracker) {
                tracker.trackUserClickedCancelBuildOption();
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackUserSharedBuild() {
        logEvent(new TrackerMethod<OverviewTracker>() {
            @Override
            public void execute(OverviewTracker tracker) {
                tracker.trackUserSharedBuild();
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackUserRestartedBuild() {
        logEvent(new TrackerMethod<OverviewTracker>() {
            @Override
            public void execute(OverviewTracker tracker) {
                tracker.trackUserRestartedBuild();
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackUserWantsToSeeBuildListFilteredByBranch() {
        logEvent(new TrackerMethod<OverviewTracker>() {
            @Override
            public void execute(OverviewTracker tracker) {
                tracker.trackUserWantsToSeeBuildListFilteredByBranch();
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackUserOpensBuildType() {
        logEvent(new TrackerMethod<OverviewTracker>() {
            @Override
            public void execute(OverviewTracker tracker) {
                tracker.trackUserOpensBuildType();
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackUserOpensProject() {
        logEvent(new TrackerMethod<OverviewTracker>() {
            @Override
            public void execute(OverviewTracker tracker) {
                tracker.trackUserOpensProject();
            }
        });
    }
}
