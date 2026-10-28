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

import kotlin.test.*
import teamcityapp.features.build_overview.api.BuildOverviewAction
import teamcityapp.libraries.analytics.*

class BuildOverviewTrackerImplTest {
    private val records = mutableListOf<AnalyticsEvent>()
    private val analytics = AnalyticsTracker { records += it }
    private fun expect(vararg names: String) = assertEquals(names.map { AnalyticsEvent(it) }, records)

    @Test fun overviewRetainsEveryActionIncludingBranchFilteredHistory() {
        val tracker = BuildOverviewTrackerImpl(analytics)
        BuildOverviewAction.entries.forEach { tracker.action(it) }
        tracker.action(BuildOverviewAction.Configuration, "private-branch-do-not-log")
        expect("share_build", "open_browser_build", "open_browser_build", "cancel_build", "cancel_build", "restart_build", "open_build_type", "open_project", "show_builds_filtered_by_branch")
    }
}
