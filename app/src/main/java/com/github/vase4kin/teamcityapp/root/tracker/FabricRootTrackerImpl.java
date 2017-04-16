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

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.crashlytics.android.answers.CustomEvent;
import com.github.vase4kin.teamcityapp.drawer.tracker.FabricDrawerTrackerImpl;

import io.fabric.sdk.android.Fabric;

/**
 * Root projects tracking class
 */
public class FabricRootTrackerImpl extends FabricDrawerTrackerImpl implements RootTracker {

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackView() {
        if (!Fabric.isInitialized()) return;
        Answers.getInstance().logContentView(new ContentViewEvent()
                .putContentName(SCREEN_NAME_ROOT));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackUserRatedTheApp() {
        logRateEvent(STATUS_RATED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackUserDidNotRateTheApp() {
        logRateEvent(STATUS_NOT_RATED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackUserDecidedToRateTheAppLater() {
        logRateEvent(STATUS_LATER);
    }

    /**
     * Log rate event
     *
     * @param status - Rate status
     */
    private void logRateEvent(String status) {
        if (!Fabric.isInitialized()) return;
        Answers.getInstance().logCustom(new CustomEvent(EVENT_RATE_APP)
                .putCustomAttribute(KEY_EVENT_STATUS, status));
    }
}
