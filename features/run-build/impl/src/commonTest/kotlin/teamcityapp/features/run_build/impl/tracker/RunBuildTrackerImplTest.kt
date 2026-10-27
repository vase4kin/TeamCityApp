/*
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

import kotlin.test.*
import teamcityapp.libraries.analytics.*

class RunBuildTrackerImplTest {
    private val records = mutableListOf<AnalyticsEvent>()
    private val analytics = AnalyticsTracker { records += it }
    private fun expect(vararg names: String) = assertEquals(names.map { AnalyticsEvent(it) }, records)

    @Test fun preservesEveryRunBuildEvent() {
        val tracker = RunBuildTrackerImpl(analytics)
        tracker.trackView()
        tracker.trackUserRunBuildSuccess()
        tracker.trackUserRunBuildWithCustomParamsSuccess()
        tracker.trackUserRunBuildFailed()
        tracker.trackUserRunBuildFailedForbidden()
        tracker.trackUserClicksOnAddNewBuildParamButton()
        tracker.trackUserClicksOnClearAllBuildParamsButton()
        tracker.trackUserAddsBuildParam()
        expect("screen_run_build", "run_build_success", "run_build_custom_params_success", "run_build_failed", "run_build_forbidden_error", "run_build_add_custom_param_click", "run_build_clear_all_params", "run_build_add_custom_param")
    }
}
