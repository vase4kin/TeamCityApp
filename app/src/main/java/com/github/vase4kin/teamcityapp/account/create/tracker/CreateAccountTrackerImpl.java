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

package com.github.vase4kin.teamcityapp.account.create.tracker;

import java.util.Set;

/**
 * Create account tracker
 */
public class CreateAccountTrackerImpl implements CreateAccountTracker {

    private Set<CreateAccountTracker> mTrackers;

    public CreateAccountTrackerImpl(Set<CreateAccountTracker> trackers) {
        this.mTrackers = trackers;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackView() {
        for (CreateAccountTracker tracker : mTrackers) {
            tracker.trackView();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackUserLoginSuccess() {
        for (CreateAccountTracker tracker : mTrackers) {
            tracker.trackUserLoginSuccess();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackGuestUserLoginSuccess() {
        for (CreateAccountTracker tracker : mTrackers) {
            tracker.trackGuestUserLoginSuccess();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackUserLoginFailed(String errorMessage) {
        for (CreateAccountTracker tracker : mTrackers) {
            tracker.trackUserLoginFailed(errorMessage);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackGuestUserLoginFailed(String errorMessage) {
        for (CreateAccountTracker tracker : mTrackers) {
            tracker.trackGuestUserLoginFailed(errorMessage);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackUserDataSaveFailed() {
        for (CreateAccountTracker tracker : mTrackers) {
            tracker.trackUserDataSaveFailed();
        }
    }
}
