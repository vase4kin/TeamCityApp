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

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.crashlytics.android.answers.CustomEvent;

import io.fabric.sdk.android.Fabric;

/**
 * Tracker class
 */
public class BuildDetailsViewTrackerImpl implements BuildDetailsTracker {

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackView() {
        if (!Fabric.isInitialized()) return;
        Answers.getInstance().logContentView(new ContentViewEvent()
                .putContentName(EVENT_CONTENT_NAME));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackUserConfirmedCancel(boolean isReAddToTheQueue) {
        if (!Fabric.isInitialized()) return;
        Answers.getInstance().logCustom(
                new CustomEvent(EVENT_USER_CONFIRMED_BUILD_CANCELLATION)
                        .putCustomAttribute(PARAM_IS_RE_ADD_TO_QUEUE, Boolean.toString(isReAddToTheQueue)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackUserGetsForbiddenErrorOnBuildCancel() {
        if (!Fabric.isInitialized()) return;
        Answers.getInstance().logCustom(new CustomEvent(EVENT_USER_GETS_FORBIDDEN_ERROR_ON_BUILD_CANCELLATION));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackUserGetsServerErrorOnBuildCancel() {
        if (!Fabric.isInitialized()) return;
        Answers.getInstance().logCustom(new CustomEvent(EVENT_USER_GETS_SERVER_ERROR_ON_BUILD_CANCELLATION));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackUserCanceledBuildSuccessfully() {
        if (!Fabric.isInitialized()) return;
        Answers.getInstance().logCustom(new CustomEvent(EVENT_USER_CANCELLED_BUILD_SUCCESSFULLY));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackUserGetsForbiddenErrorOnBuildRestart() {
        if (!Fabric.isInitialized()) return;
        Answers.getInstance().logCustom(new CustomEvent(EVENT_USER_GETS_FORBIDDEN_ERROR_ON_BUILD_RESTARTING));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackUserGetsServerErrorOnBuildRestart() {
        if (!Fabric.isInitialized()) return;
        Answers.getInstance().logCustom(new CustomEvent(EVENT_USER_GETS_SERVER_ERROR_ON_BUILD_RESTARTING));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackUserRestartedBuildSuccessfully() {
        if (!Fabric.isInitialized()) return;
        Answers.getInstance().logCustom(new CustomEvent(EVENT_USER_RESTARTED_BUILD_SUCCESSFULLY));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackUserWantsToSeeQueuedBuildDetails() {
        if (!Fabric.isInitialized()) return;
        Answers.getInstance().logCustom(new CustomEvent(EVENT_SHOW_QUEUED_BUILD_DETAILS_AFTER_RESTARTING));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackUserFailedToSeeQueuedBuildDetails() {
        if (!Fabric.isInitialized()) return;
        Answers.getInstance().logCustom(new CustomEvent(EVENT_SHOW_QUEUED_BUILD_DETAILS_FAILED_AFTER_RESTARTING));
    }
}
