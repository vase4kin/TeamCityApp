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

package teamcityapp.features.build_overview.impl.tracker

import teamcityapp.features.build_overview.api.BuildOverviewAction
import teamcityapp.libraries.analytics.*

class BuildOverviewTrackerImpl(private val analytics: AnalyticsTracker) : BuildOverviewTracker {
    override fun action(action: BuildOverviewAction, branch: String?) = analytics.track(
        when (action) {
            BuildOverviewAction.Share -> "share_build"
            BuildOverviewAction.Browser, BuildOverviewAction.Log -> "open_browser_build"
            BuildOverviewAction.Stop, BuildOverviewAction.RemoveFromQueue -> "cancel_build"
            BuildOverviewAction.Restart -> "restart_build"
            BuildOverviewAction.Configuration -> if (branch != null) "show_builds_filtered_by_branch" else "open_build_type"
            BuildOverviewAction.Project -> "open_project"
        }
    )
}
