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

package com.github.vase4kin.teamcityapp.build_details.api;

import android.support.annotation.VisibleForTesting;

/**
 * Build cancel request api object
 */
public class BuildCancelRequest {

    private String comment = "";
    private final boolean readdIntoQueue;

    /**
     * Default constructor
     *
     * @param reAddIntoQueue - Re-add build to the queue flag
     */
    public BuildCancelRequest(boolean reAddIntoQueue) {
        this.readdIntoQueue = reAddIntoQueue;
    }

    @VisibleForTesting
    public boolean isReaddIntoQueue() {
        return readdIntoQueue;
    }
}
