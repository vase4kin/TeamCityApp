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

package com.github.vase4kin.teamcityapp.overview.data;

/**
 * Start build list activity filtered by branch event sending/receiving by {@link de.greenrobot.event.EventBus}
 */
public class StartBuildsListActivityFilteredByBranchEvent {

    /**
     * Branch name
     */
    private final String mBranchName;

    /**
     * Constructor
     *
     * @param branchName - Branch name
     */
    public StartBuildsListActivityFilteredByBranchEvent(String branchName) {
        this.mBranchName = branchName;
    }

    /**
     * @return branch name
     */
    public String getBranchName() {
        return mBranchName;
    }
}
