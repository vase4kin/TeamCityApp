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

package com.github.vase4kin.teamcityapp.filter_bottom_sheet_dialog.data

import com.github.vase4kin.teamcityapp.filter_bottom_sheet_dialog.filter.Filter
import com.github.vase4kin.teamcityapp.filter_bottom_sheet_dialog.filter.FilterProvider
import com.github.vase4kin.teamcityapp.home.data.FilterAppliedEvent
import javax.inject.Inject
import org.greenrobot.eventbus.EventBus
import teamcityapp.features.filter_bottom_sheet.api.*

class AppQuickFilterRepository @Inject constructor(private val provider: FilterProvider, private val eventBus: EventBus, private val tracker: QuickFilterTracker) : QuickFilterRepository {
    override suspend fun apply(filter: QuickFilter) {
        val selected = Filter.entries[filter.ordinal]
        when {
            selected.isRunning -> {
                provider.runningBuildsFilter = selected
            }

            selected.isQueued -> {
                provider.queuedBuildsFilter = selected
            }

            selected.isAgents -> {
                provider.agentsFilter = selected
            }
        }
        tracker.selected(filter)
        // Preserve the existing running-build event contract; consumers read the updated provider.
        eventBus.post(FilterAppliedEvent(if (selected.isRunning) selected.opposite() else selected))
    }
}
