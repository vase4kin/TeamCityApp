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

package teamcityapp.features.build_history.impl

import teamcityapp.features.build_history.api.BuildHistoryPrompt
import teamcityapp.libraries.builds.BuildLaunchData

sealed interface FavoriteState {
    data object Loading : FavoriteState
    data object Unavailable : FavoriteState
    data class Available(val favorite: Boolean, val updating: Boolean = false, val updateFailed: Boolean = false) : FavoriteState
}
sealed interface OnboardingState {
    data object Loading : OnboardingState
    data object Unavailable : OnboardingState
    data class Available(val prompt: BuildHistoryPrompt? = null, val saving: Boolean = false, val saveFailed: Boolean = false) : OnboardingState
}
sealed interface QueuedBuildState {
    data object Idle : QueuedBuildState
    data object Loading : QueuedBuildState
    data object Failed : QueuedBuildState
    data class Ready(val build: BuildLaunchData) : QueuedBuildState
}
enum class BuildHistoryNoticeKind { Queued, FiltersApplied, FavoriteAdded, FavoriteRemoved }
data class BuildHistoryNotice(val id: Long, val kind: BuildHistoryNoticeKind)
data class BuildHistoryControls(
    val favorite: FavoriteState = FavoriteState.Loading,
    val onboarding: OnboardingState = OnboardingState.Loading,
    val queuedBuild: QueuedBuildState = QueuedBuildState.Idle,
    val notice: BuildHistoryNotice? = null,
    val refreshPending: Boolean = false
)
internal enum class HistoryAppendState { Idle, Loading, Error }
