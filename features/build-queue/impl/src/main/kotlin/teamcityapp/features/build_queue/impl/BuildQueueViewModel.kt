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

package teamcityapp.features.build_queue.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import teamcityapp.features.build_queue.api.BuildQueueFilter
import teamcityapp.features.build_queue.api.BuildQueueRepository
import teamcityapp.libraries.list_state.RefreshableListLoader

@HiltViewModel
class BuildQueueViewModel @Inject constructor(repository: BuildQueueRepository) : ViewModel() {
    private val queries = repository.query.map { query ->
        query.copy(favoriteConfigurationIds = if (query.filter == BuildQueueFilter.All) emptyList() else query.favoriteConfigurationIds.toList())
    }
    private val loader = RefreshableListLoader(queries) { query, forceRefresh ->
        if (query.filter == BuildQueueFilter.Favorites && query.favoriteConfigurationIds.isEmpty()) {
            emptyList()
        } else {
            // Kotlin's sort is stable; equal case-insensitive IDs retain repository order.
            repository.builds(query, forceRefresh).sortedWith { left, right ->
                left.buildTypeId.orEmpty().compareTo(right.buildTypeId.orEmpty(), ignoreCase = true)
            }
        }
    }
    val state = loader.queryState.map { (query, rows) -> BuildQueueUiState(query, rows) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(0), BuildQueueUiState())
    private var paused = false

    fun refresh() = loader.refresh()
    fun retry() = loader.retry()
    fun onResumed() {
        if (paused) loader.reload()
        paused = false
    }
    fun onPaused() {
        paused = true
    }
    fun onConfigurationRecreation(wasVisible: Boolean) {
        if (wasVisible) paused = false
    }
}
