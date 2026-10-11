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
import java.io.Serializable
import javax.inject.Inject
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.*
import teamcityapp.features.build_overview.api.*
import teamcityapp.libraries.builds.compatibility.BuildLaunchCodec
import teamcityapp.libraries.list_state.*

@HiltViewModel
class BuildOverviewViewModel @Inject constructor(savedStateHandle: SavedStateHandle, repository: BuildOverviewRepository, codec: BuildLaunchCodec) : ViewModel() {
    private val incoming = codec.decode(requireNotNull(savedStateHandle.get<Serializable>(BuildOverviewNavigation.BUILD)))
    private var loaded = false
    private var currentHref = incoming.href
    private val loader = RefreshableListLoader(flowOf(incoming.href)) { _, force ->
        val build = repository.build(currentHref, force || (!loaded && incoming.isRunning))
        currentCoroutineContext().ensureActive()
        currentHref = build.href
        loaded = true
        listOf(build)
    }
    val state = loader.state.map(::buildOverviewState).stateIn(viewModelScope, SharingStarted.WhileSubscribed(0), BuildOverviewUiState())
    fun refresh() = loader.refresh()
    fun retry() = loader.retry()
}
