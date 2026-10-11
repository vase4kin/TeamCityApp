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

import androidx.annotation.StringRes
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
    data class Available(val prompt: BuildHistoryPrompt? = null, val saving: Boolean = false, val saveFailed: Boolean = false) : OnboardingState {
        @get:StringRes val titleRes: Int? = when (prompt) {
            BuildHistoryPrompt.Run -> R.string.history_prompt_run_title
            BuildHistoryPrompt.Filter -> R.string.history_prompt_filter_title
            BuildHistoryPrompt.Favorite -> R.string.history_prompt_favorite_title
            null -> null
        }

        @get:StringRes val descriptionRes: Int? = when (prompt) {
            BuildHistoryPrompt.Run -> R.string.history_prompt_run_description
            BuildHistoryPrompt.Filter -> R.string.history_prompt_filter_description
            BuildHistoryPrompt.Favorite -> R.string.history_prompt_favorite_description
            null -> null
        }

        @get:StringRes val dismissLabelRes: Int = if (saveFailed) R.string.history_retry else R.string.history_got_it
    }
}
sealed interface QueuedBuildState {
    data object Idle : QueuedBuildState
    data object Loading : QueuedBuildState
    data object Failed : QueuedBuildState
    data class Ready(val build: BuildLaunchData) : QueuedBuildState
}
enum class BuildHistoryNoticeKind { Queued, FiltersApplied, FavoriteAdded, FavoriteRemoved }
data class BuildHistoryNotice(val id: Long, val kind: BuildHistoryNoticeKind) {
    @get:StringRes val messageRes: Int = when (kind) {
        BuildHistoryNoticeKind.Queued -> R.string.history_queued
        BuildHistoryNoticeKind.FiltersApplied -> R.string.history_filters_applied
        BuildHistoryNoticeKind.FavoriteAdded -> R.string.history_favorite_added
        BuildHistoryNoticeKind.FavoriteRemoved -> R.string.history_favorite_removed
    }

    @get:StringRes val actionLabelRes: Int? = when (kind) {
        BuildHistoryNoticeKind.Queued -> R.string.history_show
        BuildHistoryNoticeKind.FiltersApplied -> R.string.history_reset
        BuildHistoryNoticeKind.FavoriteAdded -> R.string.history_view
        BuildHistoryNoticeKind.FavoriteRemoved -> null
    }
}
data class BuildHistoryControls(
    val favorite: FavoriteState = FavoriteState.Loading,
    val onboarding: OnboardingState = OnboardingState.Loading,
    val queuedBuild: QueuedBuildState = QueuedBuildState.Idle,
    val notice: BuildHistoryNotice? = null,
    val refreshPending: Boolean = false
) {
    @get:StringRes val favoriteActionLabelRes: Int = if (favorite is FavoriteState.Available && favorite.favorite) R.string.history_remove_favorite else R.string.history_add_favorite

    @get:StringRes val favoriteFailureMessageRes: Int? = when (favorite) {
        FavoriteState.Unavailable -> R.string.history_favorite_unavailable
        is FavoriteState.Available -> if (favorite.updateFailed) R.string.history_favorite_update_failed else null
        FavoriteState.Loading -> null
    }
}
internal enum class HistoryAppendState { Idle, Loading, Error }
