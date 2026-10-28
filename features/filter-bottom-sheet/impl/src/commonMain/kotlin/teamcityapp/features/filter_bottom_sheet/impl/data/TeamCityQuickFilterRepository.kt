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

package teamcityapp.features.filter_bottom_sheet.impl.data

import teamcityapp.features.filter_bottom_sheet.api.*
import teamcityapp.libraries.accounts.*

class TeamCityQuickFilterRepository(private val preferences: AccountPreferenceState) : QuickFilterRepository {
    override suspend fun apply(filter: QuickFilter) {
        preferences.update {
            when (filter) {
                QuickFilter.RunningAll, QuickFilter.RunningFavorites -> it.copy(runningFavorites = filter == QuickFilter.RunningFavorites)
                QuickFilter.QueuedAll, QuickFilter.QueuedFavorites -> it.copy(queueFavorites = filter == QuickFilter.QueuedFavorites)
                QuickFilter.AgentsConnected, QuickFilter.AgentsDisconnected -> it.copy(agentsConnected = filter == QuickFilter.AgentsConnected)
            }
        }
    }
}
