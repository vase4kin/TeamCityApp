/*
 * Copyright 2019 Andrey Tolpeev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.vase4kin.teamcityapp.utils

import androidx.annotation.VisibleForTesting

import com.github.vase4kin.teamcityapp.overview.data.BuildDetails

/**
 * Icon utils class
 */
class IconUtils {

    companion object {

        @VisibleForTesting
        const val ICON_FAILURE = "{md-error}"
        private const val ICON_ERROR = "{md-report-problem}"
        private const val ICON_UNKNOWN = "{md-help}"
        const val ICON_SUCCESS = "{md-check-circle}"
        private const val ICON_QUEUE = "{mdi-clock-fast}"

        /**
         * @param status - Build status
         * @param state - Build state
         * @return build status icon as String
         */
        fun getBuildStatusIcon(status: String, state: String): String {
            if (state == BuildDetails.STATE_RUNNING) return RunningBuildIconUtils.RUNNING
            if (state == BuildDetails.STATE_QUEUED) return ICON_QUEUE
            return when (status) {
                BuildDetails.STATUS_FAILURE -> ICON_FAILURE
                BuildDetails.STATUS_ERROR -> ICON_ERROR
                BuildDetails.STATUS_UNKNOWN -> ICON_UNKNOWN
                BuildDetails.STATUS_SUCCESS -> ICON_SUCCESS
                else -> ICON_SUCCESS
            }
        }

        /**
         * @param count - Files count
         * @return Item count icon as String
         */
        fun getCountIcon(count: Int): String {
            return when (count) {
                0 -> "{md-crop-din}"
                1 -> "{md-filter-1}"
                2 -> "{md-filter-2}"
                3 -> "{md-filter-3}"
                4 -> "{md-filter-4}"
                5 -> "{md-filter-5}"
                6 -> "{md-filter-6}"
                7 -> "{md-filter-7}"
                8 -> "{md-filter-8}"
                9 -> "{md-filter-9}"
                10 -> "{md-filter-9-plus}"
                else -> "{md-filter-9-plus}"
            }
        }
    }
}
