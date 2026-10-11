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

package teamcityapp.features.favorites.impl

import teamcityapp.libraries.build_configurations.BuildConfigurationSummary
import teamcityapp.libraries.list_state.ListUiState

data class FavoritesUiState(
    val list: ListUiState<BuildConfigurationSummary> = ListUiState.Loading,
    val failure: FavoritesFailure = FavoritesFailure.None,
    val savedIds: List<String> = emptyList()
)

sealed interface FavoritesFailure {
    data object None : FavoritesFailure
    data class Partial(val unavailableIds: List<String>) : FavoritesFailure
    data object AllFailed : FavoritesFailure
}
