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
import org.jetbrains.compose.resources.StringResource
import teamcityapp.features.build_history.api.BuildHistoryPrompt
import teamcityapp.features.build_history.impl.resources.*
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
        val titleRes: StringResource? = when (prompt) {
            BuildHistoryPrompt.Run -> Res.string.history_prompt_run_title
            BuildHistoryPrompt.Filter -> Res.string.history_prompt_filter_title
            BuildHistoryPrompt.Favorite -> Res.string.history_prompt_favorite_title
            null -> null
        }

        val descriptionRes: StringResource? = when (prompt) {
            BuildHistoryPrompt.Run -> Res.string.history_prompt_run_description
            BuildHistoryPrompt.Filter -> Res.string.history_prompt_filter_description
            BuildHistoryPrompt.Favorite -> Res.string.history_prompt_favorite_description
            null -> null
        }

        val dismissLabelRes: StringResource = if (saveFailed) Res.string.history_retry else Res.string.history_got_it
    }
}
enum class BuildHistoryNoticeKind { Queued, FiltersApplied, FavoriteAdded, FavoriteRemoved }
data class BuildHistoryNotice(val id: Long, val kind: BuildHistoryNoticeKind) {
    val messageRes: StringResource = when (kind) {
        BuildHistoryNoticeKind.Queued -> Res.string.history_queued
        BuildHistoryNoticeKind.FiltersApplied -> Res.string.history_filters_applied
        BuildHistoryNoticeKind.FavoriteAdded -> Res.string.history_favorite_added
        BuildHistoryNoticeKind.FavoriteRemoved -> Res.string.history_favorite_removed
    }

    val actionLabelRes: StringResource? = when (kind) {
        BuildHistoryNoticeKind.Queued -> Res.string.history_show
        BuildHistoryNoticeKind.FiltersApplied -> Res.string.history_reset
        BuildHistoryNoticeKind.FavoriteAdded -> Res.string.history_view
        BuildHistoryNoticeKind.FavoriteRemoved -> null
    }
}
data class BuildHistoryControls(
    val favorite: FavoriteState = FavoriteState.Loading,
    val onboarding: OnboardingState = OnboardingState.Loading,
    val notice: BuildHistoryNotice? = null,
    val refreshPending: Boolean = false
) {
    val favoriteActionLabelRes: StringResource = when (favorite) {
        FavoriteState.Loading -> Res.string.history_favorites
        FavoriteState.Unavailable -> Res.string.history_retry_favorites
        is FavoriteState.Available -> if (favorite.favorite) Res.string.history_remove_favorite else Res.string.history_add_favorite
    }

    val favoriteStateDescriptionRes: StringResource? = when (favorite) {
        FavoriteState.Loading -> Res.string.history_loading_favorites

        FavoriteState.Unavailable -> Res.string.history_favorite_unavailable

        is FavoriteState.Available -> if (favorite.updating) {
            if (favorite.favorite) Res.string.history_removing_favorite else Res.string.history_adding_favorite
        } else {
            null
        }
    }

    val favoriteFailureMessageRes: StringResource? = when (favorite) {
        FavoriteState.Unavailable -> Res.string.history_favorite_unavailable

        is FavoriteState.Available -> when {
            !favorite.updateFailed -> null
            favorite.favorite -> Res.string.history_remove_favorite_failed
            else -> Res.string.history_add_favorite_failed
        }

        FavoriteState.Loading -> null
    }
}
enum class HistoryAppendState { Idle, Loading, Error }
