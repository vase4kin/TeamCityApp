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
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import teamcityapp.features.agents.api.AgentsRepository
import teamcityapp.libraries.list_state.RefreshableListLoader

@HiltViewModel
class AgentsViewModel @Inject constructor(repository: AgentsRepository) : ViewModel() {
    private val loader = RefreshableListLoader(
        queries = repository.filter,
        load = repository::agents
    )
    val state = loader.queryState.map { (selected, list) -> AgentsUiState(selected, list) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(0), AgentsUiState())

    private var paused = false

    fun refresh() = loader.refresh()
    fun retry() = loader.retry()

    /** Initial loading belongs to collection. Returning to the visible tab permits cached reloads. */
    fun onResumed() {
        if (paused) loader.reload()
        paused = false
    }

    fun onPaused() {
        paused = true
    }

    /** Called by the host when destruction is part of configuration recreation. */
    fun onConfigurationRecreation(wasVisible: Boolean) {
        if (wasVisible) paused = false
    }
}
