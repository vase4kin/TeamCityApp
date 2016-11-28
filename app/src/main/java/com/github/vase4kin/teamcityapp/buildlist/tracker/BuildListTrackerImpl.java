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

package com.github.vase4kin.teamcityapp.buildlist.tracker;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.crashlytics.android.answers.CustomEvent;

import io.fabric.sdk.android.Fabric;

/**
 * Impl of {@link BuildListTracker}
 */
public class BuildListTrackerImpl implements BuildListTracker {

    /**
     * Content name
     */
    private String mContentName;

    /**
     * Constructor
     *
     * @param contentName - Content name
     */
    public BuildListTrackerImpl(String contentName) {
        this.mContentName = contentName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackView() {
        if (!Fabric.isInitialized()) return;
        Answers.getInstance().logContentView(new ContentViewEvent()
                .putContentName(mContentName));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackRunBuildButtonPressed() {
        if (!Fabric.isInitialized()) return;
        Answers.getInstance().logCustom(new CustomEvent(EVENT_RUN_BUILD_BUTTON_PRESSED));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackUserWantsToSeeQueuedBuildDetails() {
        if (!Fabric.isInitialized()) return;
        Answers.getInstance().logCustom(new CustomEvent(EVENT_SHOW_QUEUED_BUILD_DETAILS));
    }
}
