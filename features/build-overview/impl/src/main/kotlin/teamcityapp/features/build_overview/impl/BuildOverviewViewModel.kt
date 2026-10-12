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

package teamcityapp.features.build_overview.impl

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.Json
import teamcityapp.features.build_details.api.*
import teamcityapp.features.build_overview.api.*
import teamcityapp.libraries.builds.BuildLaunchData
import teamcityapp.libraries.list_state.*

@HiltViewModel
class BuildOverviewViewModel @Inject constructor(private val savedStateHandle: SavedStateHandle, repository: BuildOverviewRepository) : ViewModel() {
    private val incoming = Json.decodeFromString<BuildLaunchData>(requireNotNull(savedStateHandle.get<String>(JSON_BUILD)))
    private var hostRefresh = savedStateHandle.get<Long>(HOST_REFRESH) ?: 0L
    private var loaded = false
    private var currentHref = incoming.href
    private val loader = RefreshableListLoader(flowOf(incoming.href)) { _, force ->
        val build = repository.build(currentHref, force || (!loaded && savedStateHandle.get<Boolean>(FORCE_INITIAL) == true))
        currentCoroutineContext().ensureActive()
        currentHref = build.href
        loaded = true
        listOf(build)
    }
    private val counts = MutableStateFlow<Map<BuildDetailsSection, BuildDetailsCount>>(emptyMap())
    fun metadata(value: Map<BuildDetailsSection, BuildDetailsCount>) {
        counts.value = value
    }
    val state = combine(loader.state, counts) { list, metadata -> buildOverviewState(list, incoming, metadata) }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(0), BuildOverviewUiState())
    fun refreshFromHost(version: Long) {
        if (version <= hostRefresh) return
        hostRefresh = version
        savedStateHandle[HOST_REFRESH] = version
        loader.refresh()
    }
    fun refresh() = loader.refresh()
    fun retry() = loader.retry()
    companion object {
        internal const val FORCE_INITIAL = "overview.force.initial"
        internal const val JSON_BUILD = "overview.build.json"
        private const val HOST_REFRESH = "overview.host.refresh"
    }
}
