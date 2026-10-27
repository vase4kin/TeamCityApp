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

package teamcityapp.features.home.impl.tracker

import teamcityapp.features.filter_bottom_sheet.api.QuickFilter
import teamcityapp.features.filter_bottom_sheet.api.QuickFilterTracker
import teamcityapp.features.home.api.HomeTab
import teamcityapp.libraries.analytics.*

class HomeTrackerImpl(private val analytics: AnalyticsTracker, private val quickFilters: QuickFilterTracker) : HomeTracker {
    override fun viewed() = analytics.track("screen_home")
    override fun selected(tab: HomeTab) = analytics.track(
        "tab_selected",
        "tab" to AnalyticsValue.Text(
            when (tab) {
                HomeTab.Projects -> "PROJECTS"
                HomeTab.Favorites -> "FAVORITES"
                HomeTab.Running -> "RUNNING_BUILDS"
                HomeTab.Queue -> "BUILD_QUEUE"
                HomeTab.Agents -> "AGENTS"
            }
        )
    )
    override fun fab(tab: HomeTab) {
        val event = when (tab) {
            HomeTab.Favorites -> "favorites_tab_click_on_fab"
            HomeTab.Running -> "running_builds_tab_click_on_filter_fab"
            HomeTab.Queue -> "build_queue_tab_click_on_filter_fab"
            HomeTab.Agents -> "agents_tab_click_on_filter_fab"
            HomeTab.Projects -> return
        }
        analytics.track(event)
    }
    override fun addProjects() = analytics.track("favorites_tab_click_on_action")
    override fun filterSelected(filter: QuickFilter) = quickFilters.selected(filter)
}
