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

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;

import io.fabric.sdk.android.Fabric;

/**
 * Impl of {@link OverviewTracker}
 */
public class OverviewTrackerImpl implements OverviewTracker {

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackUserClickedCancelBuildOption() {
        if (!Fabric.isInitialized()) return;
        Answers.getInstance().logCustom(new CustomEvent(EVENT_CANCEL_BUILD));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackUserSharedBuild() {
        if (!Fabric.isInitialized()) return;
        Answers.getInstance().logCustom(new CustomEvent(EVENT_SHARE_BUILD));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackView() {
        // Do nothing
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackUserRestartedBuild() {
        if (!Fabric.isInitialized()) return;
        Answers.getInstance().logCustom(new CustomEvent(EVENT_RESTART_BUILD));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackUserWantsToSeeBuildListFilteredByBranch() {
        if (!Fabric.isInitialized()) return;
        Answers.getInstance().logCustom(new CustomEvent(EVENT_SHOW_BUILDS_FILTERED_BY_BRANCH));
    }
}
