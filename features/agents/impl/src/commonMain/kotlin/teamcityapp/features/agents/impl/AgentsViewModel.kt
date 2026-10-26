/*
 * Copyright 2019 Andrey Tolpeev
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

package teamcityapp.features.agents.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import teamcityapp.features.agents.api.AgentsRepository
import teamcityapp.libraries.list_state.RefreshableListLoader

open class AgentsViewModel constructor(repository: AgentsRepository) : ViewModel() {
    private val loader = RefreshableListLoader(
        queries = repository.filter,
        pollIntervalMillis = 30_000,
        load = repository::agents
    )
    val state = loader.pollingState.map { (selected, list, updates) -> AgentsUiState(selected, list, updates) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(0), AgentsUiState())

    fun refresh() = loader.refresh()
    fun retry() = loader.retry()
    fun applyUpdates() = loader.applyUpdates()
}
