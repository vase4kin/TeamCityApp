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

package com.github.vase4kin.teamcityapp.base.tracker;

import com.github.vase4kin.teamcityapp.navigation.tracker.ViewTracker;

import java.util.Set;

/**
 * Base view tracker class
 *
 * @param <T> - tracker type
 */
public class BaseViewTracker<T extends ViewTracker> implements ViewTracker {

    protected final Set<T> mTrackers;

    public BaseViewTracker(Set<T> trackers) {
        this.mTrackers = trackers;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackView() {
        for (ViewTracker tracker : mTrackers) {
            tracker.trackView();
        }
    }
}
