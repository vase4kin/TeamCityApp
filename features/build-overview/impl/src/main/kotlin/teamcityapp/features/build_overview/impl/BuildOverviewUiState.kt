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

import teamcityapp.features.build_overview.api.BuildOverviewAction
import teamcityapp.libraries.builds.BuildLaunchData
import teamcityapp.libraries.list_state.ListUiState

data class BuildOverviewUiState(
    val list: ListUiState<OverviewRow> = ListUiState.Loading,
    val build: BuildLaunchData? = null,
    val actions: List<BuildOverviewAction> = emptyList()
)
internal fun buildOverviewState(state: ListUiState<BuildLaunchData>): BuildOverviewUiState = when (state) {
    ListUiState.Loading -> BuildOverviewUiState()

    ListUiState.Error -> BuildOverviewUiState(list = ListUiState.Error)

    is ListUiState.Empty -> BuildOverviewUiState(list = state)

    is ListUiState.Content -> {
        val build = state.items.single()
        val actions = if (state.isRefreshing) emptyList() else overviewActions(build)
        BuildOverviewUiState(ListUiState.Content(overviewRows(build), state.isRefreshing, state.refreshFailed), build, actions)
    }
}
internal fun overviewActions(build: BuildLaunchData): List<BuildOverviewAction> = listOf(
    BuildOverviewAction.Share,
    BuildOverviewAction.Browser,
    when {
        build.isRunning -> BuildOverviewAction.Stop
        build.isQueued -> BuildOverviewAction.RemoveFromQueue
        else -> BuildOverviewAction.Restart
    }
)
