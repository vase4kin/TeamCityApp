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

package com.github.vase4kin.teamcityapp.utils;

import android.support.annotation.VisibleForTesting;

/**
 * Icon utils class
 */
public class IconUtils {

    @VisibleForTesting
    public static final String FAILURE = "{md-error}";
    private static final String ERROR = "{md-report-problem}";
    private static final String UNKNOWN = "{md-help}";
    private static final String SUCCESS = "{md-check-circle}";
    private static final String QUEUE = "{mdi-clock-fast}";

    /**
     * @param status - Build status
     * @param state  - Build state
     * @return build status icon as String
     */
    public static String getBuildStatusIcon(String status, String state) {
        if (state.equals("running")) return RunningBuildIconUtils.RUNNING;
        if (state.equals("queued")) return QUEUE;
        switch (status) {
            case "FAILURE":
                return FAILURE;
            case "ERROR":
                return ERROR;
            case "UNKNOWN":
                return UNKNOWN;
            case "SUCCESS":
            default:
                return SUCCESS;
        }
    }

    /**
     * @param count - Files count
     * @return Item count icon as String
     */
    public static String getCountIcon(int count) {
        switch (count) {
            case 0:
                return "{md-crop-din}";
            case 1:
                return "{md-filter-1}";
            case 2:
                return "{md-filter-2}";
            case 3:
                return "{md-filter-3}";
            case 4:
                return "{md-filter-4}";
            case 5:
                return "{md-filter-5}";
            case 6:
                return "{md-filter-6}";
            case 7:
                return "{md-filter-7}";
            case 8:
                return "{md-filter-8}";
            case 9:
                return "{md-filter-9}";
            case 10:
            default:
                return "{md-filter-9-plus}";
        }
    }
}
