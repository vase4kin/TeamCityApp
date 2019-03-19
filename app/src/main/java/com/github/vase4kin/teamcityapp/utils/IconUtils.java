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

import androidx.annotation.VisibleForTesting;

import com.github.vase4kin.teamcityapp.overview.data.BuildDetails;

/**
 * Icon utils class
 */
public class IconUtils {

    @VisibleForTesting
    public static final String ICON_FAILURE = "{md-error}";
    private static final String ICON_ERROR = "{md-report-problem}";
    private static final String ICON_UNKNOWN = "{md-help}";
    public static final String ICON_SUCCESS = "{md-check-circle}";
    private static final String ICON_QUEUE = "{mdi-clock-fast}";

    /**
     * @param status - Build status
     * @param state  - Build state
     * @return build status icon as String
     */
    public static String getBuildStatusIcon(String status, String state) {
        if (state.equals(BuildDetails.STATE_RUNNING)) return RunningBuildIconUtils.RUNNING;
        if (state.equals(BuildDetails.STATE_QUEUED)) return ICON_QUEUE;
        switch (status) {
            case BuildDetails.STATUS_FAILURE:
                return ICON_FAILURE;
            case BuildDetails.STATUS_ERROR:
                return ICON_ERROR;
            case BuildDetails.STATUS_UNKNOWN:
                return ICON_UNKNOWN;
            case BuildDetails.STATUS_SUCCESS:
            default:
                return ICON_SUCCESS;
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
