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

package teamcityapp.features.running_builds.impl

import androidx.annotation.StringRes
import teamcityapp.features.running_builds.api.RunningBuildsFilter
import teamcityapp.features.running_builds.api.RunningBuildsQuery
import teamcityapp.libraries.build_ui.BuildRowUiState
import teamcityapp.libraries.builds.BuildLaunchData
import teamcityapp.libraries.list_state.ListUiState

data class RunningBuildsUiState(
    val query: RunningBuildsQuery = RunningBuildsQuery(accountKey = ""),
    val list: ListUiState<BuildLaunchData> = ListUiState.Loading
) {
    val rows: List<BuildRowUiState> = (list as? ListUiState.Content)?.items.orEmpty().map(::BuildRowUiState)

    @get:StringRes val emptyMessageRes: Int = if (query.filter == RunningBuildsFilter.Favorites) R.string.running_builds_empty_favorites else R.string.running_builds_empty_all
}
