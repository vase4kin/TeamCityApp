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

package com.github.vase4kin.teamcityapp.account.manage.tracker;

import com.github.vase4kin.teamcityapp.base.tracker.BaseViewTracker;

import java.util.Set;

/**
 * Manage account tracker
 */
public class ManageAccountsTrackerImpl extends BaseViewTracker<ManageAccountsTracker> implements ManageAccountsTracker {

    public ManageAccountsTrackerImpl(Set<ManageAccountsTracker> trackers) {
        super(trackers);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackAccountRemove() {
        logEvent(new TrackerMethod<ManageAccountsTracker>() {
            @Override
            public void execute(ManageAccountsTracker tracker) {
                tracker.trackAccountRemove();
            }
        });
    }
}
