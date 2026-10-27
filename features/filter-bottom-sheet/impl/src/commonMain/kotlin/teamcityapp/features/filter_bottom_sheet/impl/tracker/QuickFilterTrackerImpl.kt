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

package teamcityapp.features.filter_bottom_sheet.impl.tracker

import teamcityapp.features.filter_bottom_sheet.api.QuickFilter
import teamcityapp.features.filter_bottom_sheet.api.QuickFilterTracker
import teamcityapp.libraries.analytics.*

class QuickFilterTrackerImpl(private val analytics: AnalyticsTracker) : QuickFilterTracker {
    override fun selected(filter: QuickFilter) {
        val (event, value) = when (filter) {
            QuickFilter.RunningAll -> "filter_running_builds_selected" to "RUNNING_ALL"
            QuickFilter.RunningFavorites -> "filter_running_builds_selected" to "RUNNING_FAVORITES"
            QuickFilter.QueuedAll -> "filter_queued_builds_selected" to "QUEUE_ALL"
            QuickFilter.QueuedFavorites -> "filter_queued_builds_selected" to "QUEUE_FAVORITES"
            QuickFilter.AgentsConnected -> "filter_agents_selected" to "AGENTS_CONNECTED"
            QuickFilter.AgentsDisconnected -> "filter_agents_selected" to "AGENTS_DISCONNECTED"
        }
        analytics.track(event, "filter" to AnalyticsValue.Text(value))
    }
}
