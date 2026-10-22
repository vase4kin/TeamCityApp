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

package teamcityapp.features.build_overview.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import teamcityapp.features.build_overview.api.BuildOverviewRepository
import teamcityapp.libraries.list_state.*

/** Resolve a reference through the same full-snapshot repository before exposing Overview rows. */
class BuildReferenceViewModel(href: String, repository: BuildOverviewRepository) : ViewModel() {
    private val loader = RefreshableListLoader(flowOf(href)) { reference, force -> listOf(repository.build(reference, force)) }
    val state = loader.state.map { buildOverviewState(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(0), BuildOverviewUiState())
    fun refresh() = loader.refresh()
    fun retry() = loader.retry()
}
