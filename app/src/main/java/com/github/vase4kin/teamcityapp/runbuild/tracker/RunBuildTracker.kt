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

package com.github.vase4kin.teamcityapp.runbuild.tracker

import com.github.vase4kin.teamcityapp.base.tracker.ViewTracker

/**
 * Run build tracking
 */
interface RunBuildTracker : ViewTracker {

    /**
     * Track user ran build success
     */
    fun trackUserRunBuildSuccess()

    /**
     * Track user ran build with custom params success
     */
    fun trackUserRunBuildWithCustomParamsSuccess()

    /**
     * Track user ran build failed
     */
    fun trackUserRunBuildFailed()

    /**
     * Track user ran build failed forbidden
     */
    fun trackUserRunBuildFailedForbidden()

    /**
     * Track user clicks on add new build param
     */
    fun trackUserClicksOnAddNewBuildParamButton()

    /**
     * Track user clicks on clear all build params
     */
    fun trackUserClicksOnClearAllBuildParamsButton()

    /**
     * Track user adds new build param
     */
    fun trackUserAddsBuildParam()

    companion object {

        /**
         * Run build content name
         */
        const val SCREEN_NAME_RUN_BUILD = "screen_run_build"

        /**
         * Title for run build success event
         */
        const val EVENT_RUN_BUILD_SUCCESS = "run_build_success"

        /**
         * Title for run build success with custom parameters event
         */
        const val EVENT_RUN_BUILD_SUCCESS_WITH_CUSTOM_PARAMETERS = "run_build_custom_params_success"

        /**
         * Title for run build failed event
         */
        const val EVENT_RUN_BUILD_FAILED = "run_build_failed"

        /**
         * Title for run build failed forbidden event
         */
        const val EVENT_RUN_BUILD_FAILED_FORBIDDEN = "run_build_forbidden_error"

        /**
         * Title for user clicks on add new build param button event
         */
        const val EVENT_USER_CLICKS_ADD_NEW_BUILD_PARAMETER_BUTTON = "run_build_add_custom_param_click"

        /**
         * Title for user clicks on clear all build params button event
         */
        const val EVENT_USER_CLICKS_CLEAR_ALL_BUILD_PARAMETERS_BUTTON = "run_build_clear_all_params"

        /**
         * Title for user adds new build param event
         */
        const val EVENT_USER_ADDS_NEW_BUILD_PARAMETER = "run_build_add_custom_param"
    }
}
