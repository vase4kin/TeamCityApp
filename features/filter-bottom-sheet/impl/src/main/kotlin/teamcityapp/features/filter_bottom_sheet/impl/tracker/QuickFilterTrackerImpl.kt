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
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import javax.inject.Inject
import teamcityapp.features.filter_bottom_sheet.api.*
class QuickFilterTrackerImpl @Inject constructor(private val analytics: FirebaseAnalytics) : QuickFilterTracker {
    override fun selected(filter: QuickFilter) {
        val event = when (filter) {
            QuickFilter.RunningAll, QuickFilter.RunningFavorites -> "filter_running_builds_selected"
            QuickFilter.QueuedAll, QuickFilter.QueuedFavorites -> "filter_queued_builds_selected"
            QuickFilter.AgentsConnected, QuickFilter.AgentsDisconnected -> "filter_agents_selected"
        }
        val legacyName = when (filter) {
            QuickFilter.RunningAll -> "RUNNING_ALL"
            QuickFilter.RunningFavorites -> "RUNNING_FAVORITES"
            QuickFilter.QueuedAll -> "QUEUE_ALL"
            QuickFilter.QueuedFavorites -> "QUEUE_FAVORITES"
            QuickFilter.AgentsConnected -> "AGENTS_CONNECTED"
            QuickFilter.AgentsDisconnected -> "AGENTS_DISCONNECTED"
        }
        analytics.logEvent(event, Bundle().apply { putString("filter", legacyName) })
    }
}
