/*
 * Copyright 2019 Andrey Tolpeev
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

package teamcityapp.features.build_history.impl.tracker

import com.google.firebase.analytics.FirebaseAnalytics
import javax.inject.Inject

interface BuildHistoryTracker {
    fun viewShown()
    fun runBuildPressed()
    fun queuedBuildRequested()
}
class BuildHistoryTrackerImpl @Inject constructor(private val analytics: FirebaseAnalytics) : BuildHistoryTracker {
    override fun viewShown() = analytics.logEvent("screen_build_list", null)
    override fun runBuildPressed() = analytics.logEvent("run_build_fab_click", null)
    override fun queuedBuildRequested() = analytics.logEvent("build_list_show_queued_details", null)
}
