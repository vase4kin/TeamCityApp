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

package teamcityapp.features.home.impl

import androidx.annotation.StringRes
import teamcityapp.features.home.api.*

sealed interface HomeBadge {
    data object Loading : HomeBadge
    data class Count(val value: Int) : HomeBadge
    data class Failed(@StringRes val message: Int = R.string.home_count_failed) : HomeBadge
}
data class HomePromptUi(val prompt: HomePrompt, @StringRes val title: Int, @StringRes val description: Int)
data class HomeUiState(
    val selected: HomeTab = HomeTab.Projects,
    val badges: Map<HomeTab, HomeBadge> = HomeTab.entries.filter { it != HomeTab.Projects }.associateWith { HomeBadge.Loading },
    val query: HomeQuery? = null,
    val prompt: HomePromptUi? = null,
    val onboardingFailed: Boolean = false,
    val retryAttempt: Long = 0
) {
    val isActivity: Boolean get() = selected in listOf(HomeTab.Running, HomeTab.Queue, HomeTab.Agents)

    @get:StringRes val titleRes: Int get() = when (selected) {
        HomeTab.Projects -> R.string.home_projects
        HomeTab.Favorites -> R.string.home_favorites
        else -> R.string.home_activity
    }
    val accountInitials: String? get() = query?.account?.userName?.trim()?.takeIf { it.isNotEmpty() }
        ?.split(Regex("[\\s._-]+"))?.filter { it.isNotEmpty() }?.takeIf { it.isNotEmpty() }?.let { parts ->
            if (parts.size > 1) "${parts.first().first()}${parts.last().first()}".uppercase() else parts.first().take(2).uppercase()
        }
    val activityBadge: HomeBadge get() {
        val counts = listOf(HomeTab.Running, HomeTab.Queue, HomeTab.Agents).map { badges[it] ?: HomeBadge.Loading }
        return when {
            counts.any { it is HomeBadge.Failed } -> HomeBadge.Failed()
            counts.any { it is HomeBadge.Loading } -> HomeBadge.Loading
            else -> HomeBadge.Count(counts.sumOf { (it as HomeBadge.Count).value.toLong() }.coerceIn(0, Int.MAX_VALUE.toLong()).toInt())
        }
    }

    @get:StringRes val filterSummaryRes: Int? get() = when (selected) {
        HomeTab.Running -> when (query?.runningFilter) {
            teamcityapp.features.filter_bottom_sheet.api.QuickFilter.RunningFavorites -> R.string.home_filter_favorites
            teamcityapp.features.filter_bottom_sheet.api.QuickFilter.RunningAll -> R.string.home_filter_all_builds
            else -> null
        }

        HomeTab.Queue -> when (query?.queueFilter) {
            teamcityapp.features.filter_bottom_sheet.api.QuickFilter.QueuedFavorites -> R.string.home_filter_favorites
            teamcityapp.features.filter_bottom_sheet.api.QuickFilter.QueuedAll -> R.string.home_filter_all_builds
            else -> null
        }

        HomeTab.Agents -> when (query?.agentsFilter) {
            teamcityapp.features.filter_bottom_sheet.api.QuickFilter.AgentsConnected -> R.string.home_filter_connected
            teamcityapp.features.filter_bottom_sheet.api.QuickFilter.AgentsDisconnected -> R.string.home_filter_disconnected
            else -> null
        }

        else -> null
    }

    @get:StringRes val retryMessageRes: Int? get() = when {
        onboardingFailed -> R.string.home_onboarding_failed
        badges.values.any { it is HomeBadge.Failed } -> R.string.home_counts_failed
        else -> null
    }

    @get:StringRes val retryLabelRes: Int get() = if (onboardingFailed) R.string.home_retry else R.string.home_retry_counts

    @get:StringRes val fabLabelRes: Int get() = if (selected == HomeTab.Favorites) R.string.home_add_favorite else R.string.home_filter
}
internal fun HomePrompt.ui() = when (this) {
    HomePrompt.Drawer -> HomePromptUi(this, R.string.home_account_tip_title, R.string.home_account_tip_description)
    HomePrompt.Favorites -> HomePromptUi(this, R.string.title_onboarding_add_fav, R.string.text_onboarding_add_fav)
    HomePrompt.Running -> HomePromptUi(this, R.string.title_onboarding_filter, R.string.text_onboarding_filter)
    HomePrompt.Queue -> HomePromptUi(this, R.string.title_onboarding_filter_queued, R.string.text_onboarding_filter_queued)
    HomePrompt.Agents -> HomePromptUi(this, R.string.title_onboarding_filter_agents, R.string.text_onboarding_filter_agents)
}
