/*
 * Copyright 2020 Andrey Tolpeev
 * Copyright 2026 Andrey Tolpeev
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

package teamcityapp.features.run_build.impl.tracker

import teamcityapp.features.run_build.api.RunBuildTracker
import teamcityapp.libraries.analytics.*

class RunBuildTrackerImpl(private val analytics: AnalyticsTracker) : RunBuildTracker {
    override fun trackView() = analytics.track(RunBuildTracker.SCREEN_NAME_RUN_BUILD)
    override fun trackUserRunBuildSuccess() = analytics.track(RunBuildTracker.EVENT_RUN_BUILD_SUCCESS)
    override fun trackUserRunBuildWithCustomParamsSuccess() = analytics.track(RunBuildTracker.EVENT_RUN_BUILD_SUCCESS_WITH_CUSTOM_PARAMETERS)
    override fun trackUserRunBuildFailed() = analytics.track(RunBuildTracker.EVENT_RUN_BUILD_FAILED)
    override fun trackUserRunBuildFailedForbidden() = analytics.track(RunBuildTracker.EVENT_RUN_BUILD_FAILED_FORBIDDEN)
    override fun trackUserClicksOnAddNewBuildParamButton() = analytics.track(RunBuildTracker.EVENT_USER_CLICKS_ADD_NEW_BUILD_PARAMETER_BUTTON)
    override fun trackUserClicksOnClearAllBuildParamsButton() = analytics.track(RunBuildTracker.EVENT_USER_CLICKS_CLEAR_ALL_BUILD_PARAMETERS_BUTTON)
    override fun trackUserAddsBuildParam() = analytics.track(RunBuildTracker.EVENT_USER_ADDS_NEW_BUILD_PARAMETER)
}
