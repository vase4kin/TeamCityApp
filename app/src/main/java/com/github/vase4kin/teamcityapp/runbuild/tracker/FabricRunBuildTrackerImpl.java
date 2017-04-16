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

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.crashlytics.android.answers.CustomEvent;

import io.fabric.sdk.android.Fabric;

/**
 * Impl of {@link RunBuildTracker}
 */
public class FabricRunBuildTrackerImpl implements RunBuildTracker {

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackUserRunBuildSuccess() {
        if (!Fabric.isInitialized()) return;
        Answers.getInstance().logCustom(new CustomEvent(EVENT_RUN_BUILD_SUCCESS));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackUserRunBuildWithCustomParamsSuccess() {
        if (!Fabric.isInitialized()) return;
        Answers.getInstance().logCustom(new CustomEvent(EVENT_RUN_BUILD_SUCCESS_WITH_CUSTOM_PARAMETERS));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackUserRunBuildFailed() {
        if (!Fabric.isInitialized()) return;
        Answers.getInstance().logCustom(new CustomEvent(EVENT_RUN_BUILD_FAILED));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackUserRunBuildFailedForbidden() {
        if (!Fabric.isInitialized()) return;
        Answers.getInstance().logCustom(new CustomEvent(EVENT_RUN_BUILD_FAILED_FORBIDDEN));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackView() {
        if (!Fabric.isInitialized()) return;
        Answers.getInstance().logContentView(new ContentViewEvent()
                .putContentName(SCREEN_NAME_RUN_BUILD));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackUserClicksOnAddNewBuildParamButton() {
        if (!Fabric.isInitialized()) return;
        Answers.getInstance().logCustom(new CustomEvent(EVENT_USER_CLICKS_ADD_NEW_BUILD_PARAMETER_BUTTON));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackUserClicksOnClearAllBuildParamsButton() {
        if (!Fabric.isInitialized()) return;
        Answers.getInstance().logCustom(new CustomEvent(EVENT_USER_CLICKS_CLEAR_ALL_BUILD_PARAMETERS_BUTTON));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackUserAddsBuildParam() {
        if (!Fabric.isInitialized()) return;
        Answers.getInstance().logCustom(new CustomEvent(EVENT_USER_ADDS_NEW_BUILD_PARAMETER));
    }
}
