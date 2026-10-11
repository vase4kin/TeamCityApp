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

package teamcityapp.features.snapshot_dependencies.impl

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import teamcityapp.features.snapshot_dependencies.api.SnapshotDependenciesRepository
import teamcityapp.libraries.list_state.ListUiState
import teamcityapp.libraries.list_state.RefreshableListLoader

@HiltViewModel
class SnapshotDependenciesViewModel @Inject constructor(
    repository: SnapshotDependenciesRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val buildId = checkNotNull(savedStateHandle.get<String>(SnapshotDependenciesFragment.BUILD_ID)).also { require(it.isNotBlank()) }
    val buildTypeName: String = savedStateHandle[SnapshotDependenciesFragment.BUILD_TYPE_NAME] ?: ""
    private val loader = RefreshableListLoader(flowOf(buildId), repository::dependencies)
    val state = loader.state.map(::SnapshotDependenciesUiState).stateIn(viewModelScope, SharingStarted.WhileSubscribed(0), SnapshotDependenciesUiState())
    private var paused = false

    fun refresh() = loader.refresh()
    fun retry() = loader.retry()

    /** Visible return reloads with the normal cache policy; first load belongs to collection. */
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
