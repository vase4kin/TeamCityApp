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

package teamcityapp.features.filter_bottom_sheet.impl.tracker

import kotlin.test.*
import teamcityapp.features.filter_bottom_sheet.api.QuickFilter
import teamcityapp.libraries.analytics.*

class QuickFilterTrackerImplTest {
    private val records = mutableListOf<AnalyticsEvent>()
    private val analytics = AnalyticsTracker { records += it }
    private fun text(name: String, key: String, value: String) = AnalyticsEvent(name, mapOf(key to AnalyticsValue.Text(value)))

    @Test fun preservesAllSixLegacyValues() {
        val sheet = QuickFilterTrackerImpl(analytics)
        QuickFilter.entries.forEach(sheet::selected)
        val expected = listOf(
            text("filter_running_builds_selected", "filter", "RUNNING_ALL"),
            text("filter_running_builds_selected", "filter", "RUNNING_FAVORITES"),
            text("filter_queued_builds_selected", "filter", "QUEUE_ALL"),
            text("filter_queued_builds_selected", "filter", "QUEUE_FAVORITES"),
            text("filter_agents_selected", "filter", "AGENTS_CONNECTED"),
            text("filter_agents_selected", "filter", "AGENTS_DISCONNECTED")
        )
        assertEquals(expected, records)
    }
}
