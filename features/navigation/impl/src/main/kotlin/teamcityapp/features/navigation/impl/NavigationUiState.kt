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

package teamcityapp.features.navigation.impl

import teamcityapp.features.navigation.api.NavigationEntry
import teamcityapp.libraries.build_configurations.ProjectReference
import teamcityapp.libraries.list_state.ListUiState

data class NavigationUiState(
    val project: ProjectReference = ProjectReference("_Root", ""),
    val list: ListUiState<NavigationEntry> = ListUiState.Loading,
    val rating: RatingPromptState = RatingPromptState.Hidden,
    val openRating: Boolean = false
)

sealed interface RatingPromptState {
    data object Hidden : RatingPromptState
    data object Unavailable : RatingPromptState
    data class Available(val isSaving: Boolean = false, val saveFailed: Boolean = false) : RatingPromptState
}
