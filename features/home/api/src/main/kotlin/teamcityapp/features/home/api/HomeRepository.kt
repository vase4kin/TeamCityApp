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

package teamcityapp.features.home.api

import kotlinx.coroutines.flow.Flow
import teamcityapp.features.filter_bottom_sheet.api.QuickFilter

data class HomeAccount(val userName: String, val serverUrl: String)

data class HomeQuery(
    val accountKey: String,
    val favoriteIds: List<String>,
    val runningFilter: QuickFilter,
    val queueFilter: QuickFilter,
    val agentsFilter: QuickFilter,
    val account: HomeAccount? = null
)
sealed interface HomeCount {
    data class Available(val value: Int) : HomeCount
    data object Unavailable : HomeCount
}
enum class HomePrompt { Drawer, Favorites, Running, Queue, Agents }
interface HomeRepository {
    val query: Flow<HomeQuery>
    suspend fun count(query: HomeQuery, tab: HomeTab): HomeCount
    suspend fun pendingPrompts(): Set<HomePrompt>
    suspend fun acknowledgePrompt(prompt: HomePrompt)
}
