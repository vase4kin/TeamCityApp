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

import com.github.vase4kin.teamcityapp.filter_bottom_sheet_dialog.filter.*
import com.github.vase4kin.teamcityapp.home.data.FilterAppliedEvent
import kotlinx.coroutines.test.runTest
import org.greenrobot.eventbus.EventBus
import org.junit.Assert.*
import org.junit.Test
import org.mockito.kotlin.*
import teamcityapp.features.filter_bottom_sheet.api.QuickFilter
import teamcityapp.features.filter_bottom_sheet.api.QuickFilterTracker

class AppQuickFilterRepositoryTest {
    @Test fun everyChoiceUpdatesOnlyItsOwningFilterAndPostsTheLegacyEvent() = runTest {
        QuickFilter.entries.forEach { choice ->
            val provider = FilterProvider()
            val bus = mock<EventBus>()
            val tracker = mock<QuickFilterTracker>()
            val selected = Filter.entries[choice.ordinal]
            AppQuickFilterRepository(provider, bus, tracker).apply(choice)
            assertEquals(if (selected.isRunning) selected else Filter.RUNNING_FAVORITES, provider.runningBuildsFilter)
            assertEquals(if (selected.isQueued) selected else Filter.QUEUE_FAVORITES, provider.queuedBuildsFilter)
            assertEquals(if (selected.isAgents) selected else Filter.AGENTS_CONNECTED, provider.agentsFilter)
            verify(bus).post(FilterAppliedEvent(if (selected.isRunning) selected.opposite() else selected))
            verify(tracker).selected(choice)
        }
    }
}
