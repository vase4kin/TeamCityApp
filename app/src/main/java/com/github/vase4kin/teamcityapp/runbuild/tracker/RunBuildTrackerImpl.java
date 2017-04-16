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

package com.github.vase4kin.teamcityapp.runbuild.tracker;

import com.github.vase4kin.teamcityapp.base.tracker.BaseViewTracker;

import java.util.Set;

/**
 * Impl of {@link RunBuildTracker}
 */
public class RunBuildTrackerImpl extends BaseViewTracker<RunBuildTracker> implements RunBuildTracker {

    public RunBuildTrackerImpl(Set<RunBuildTracker> trackers) {
        super(trackers);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackView() {
        logEvent(new TrackerMethod<RunBuildTracker>() {
            @Override
            public void execute(RunBuildTracker tracker) {
                tracker.trackView();
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackUserRunBuildSuccess() {
        logEvent(new TrackerMethod<RunBuildTracker>() {
            @Override
            public void execute(RunBuildTracker tracker) {
                tracker.trackUserRunBuildSuccess();
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackUserRunBuildWithCustomParamsSuccess() {
        logEvent(new TrackerMethod<RunBuildTracker>() {
            @Override
            public void execute(RunBuildTracker tracker) {
                tracker.trackUserRunBuildWithCustomParamsSuccess();
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackUserRunBuildFailed() {
        logEvent(new TrackerMethod<RunBuildTracker>() {
            @Override
            public void execute(RunBuildTracker tracker) {
                tracker.trackUserRunBuildFailed();
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackUserRunBuildFailedForbidden() {
        logEvent(new TrackerMethod<RunBuildTracker>() {
            @Override
            public void execute(RunBuildTracker tracker) {
                tracker.trackUserRunBuildFailedForbidden();
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackUserClicksOnAddNewBuildParamButton() {
        logEvent(new TrackerMethod<RunBuildTracker>() {
            @Override
            public void execute(RunBuildTracker tracker) {
                tracker.trackUserClicksOnAddNewBuildParamButton();
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackUserClicksOnClearAllBuildParamsButton() {
        logEvent(new TrackerMethod<RunBuildTracker>() {
            @Override
            public void execute(RunBuildTracker tracker) {
                tracker.trackUserClicksOnClearAllBuildParamsButton();
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackUserAddsBuildParam() {
        logEvent(new TrackerMethod<RunBuildTracker>() {
            @Override
            public void execute(RunBuildTracker tracker) {
                tracker.trackUserAddsBuildParam();
            }
        });
    }
}
