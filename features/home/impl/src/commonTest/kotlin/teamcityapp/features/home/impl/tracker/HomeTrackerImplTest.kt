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

import kotlin.test.*
import teamcityapp.features.filter_bottom_sheet.api.QuickFilter
import teamcityapp.features.filter_bottom_sheet.api.QuickFilterTracker
import teamcityapp.features.home.api.HomeTab
import teamcityapp.libraries.analytics.*

class HomeTrackerImplTest {
    private val records = mutableListOf<AnalyticsEvent>()
    private val analytics = AnalyticsTracker { records += it }
    private fun expect(vararg names: String) = assertEquals(names.map { AnalyticsEvent(it) }, records)
    private fun text(name: String, key: String, value: String) = AnalyticsEvent(name, mapOf(key to AnalyticsValue.Text(value)))
    private val selectedFilters = mutableListOf<QuickFilter>()
    private val filters = object : QuickFilterTracker {
        override fun selected(filter: QuickFilter) {
            selectedFilters += filter
        }
    }

    @Test fun homePreservesAllFiveLegacyTabNames() {
        val tracker = HomeTrackerImpl(analytics, filters)
        tracker.viewed()
        HomeTab.entries.forEach(tracker::selected)
        assertEquals(listOf(AnalyticsEvent("screen_home")) + listOf("PROJECTS", "FAVORITES", "RUNNING_BUILDS", "BUILD_QUEUE", "AGENTS").map { text("tab_selected", "tab", it) }, records)
    }

    @Test fun homeRetainsSpecificActionNamesAndProjectsHasNoFabEvent() {
        val tracker = HomeTrackerImpl(analytics, filters)
        HomeTab.entries.forEach(tracker::fab)
        tracker.addProjects()
        expect("favorites_tab_click_on_fab", "running_builds_tab_click_on_filter_fab", "build_queue_tab_click_on_filter_fab", "agents_tab_click_on_filter_fab", "favorites_tab_click_on_action")
    }

    @Test fun inlineFilterSelectionsDelegateToTheQuickFilterContract() {
        val tracker = HomeTrackerImpl(analytics, filters)
        QuickFilter.entries.forEach(tracker::filterSelected)
        assertEquals(QuickFilter.entries.toList(), selectedFilters)
        assertTrue(records.isEmpty())
    }
}
