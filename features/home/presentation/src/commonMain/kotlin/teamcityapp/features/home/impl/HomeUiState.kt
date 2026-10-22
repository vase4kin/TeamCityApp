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

import org.jetbrains.compose.resources.StringResource
import teamcityapp.features.filter_bottom_sheet.api.QuickFilter
import teamcityapp.features.home.api.*
import teamcityapp.features.home.presentation.resources.*

sealed interface HomeBadge {
    data object Loading : HomeBadge
    data class Count(val value: Int) : HomeBadge
    data class Failed(val message: StringResource = Res.string.home_count_failed) : HomeBadge
}
data class HomePromptUi(val prompt: HomePrompt, val title: StringResource, val description: StringResource)
data class HomeFilterOption(val filter: QuickFilter, val labelRes: StringResource, val selected: Boolean)
data class HomeUiState(
    val selected: HomeTab = HomeTab.Projects,
    val badges: Map<HomeTab, HomeBadge> = HomeTab.entries.filter { it != HomeTab.Projects }.associateWith { HomeBadge.Loading },
    val query: HomeQuery? = null,
    val prompt: HomePromptUi? = null,
    val onboardingFailed: Boolean = false,
    val retryAttempt: Long = 0,
    val applyingFilter: QuickFilter? = null,
    val filterFailed: Boolean = false
) {
    val isActivity: Boolean get() = selected in listOf(HomeTab.Running, HomeTab.Queue, HomeTab.Agents)

    val titleRes: StringResource get() = when (selected) {
        HomeTab.Projects -> Res.string.home_projects
        HomeTab.Favorites -> Res.string.home_favorites
        else -> Res.string.home_activity
    }
    val accountInitials: String? get() = query?.account?.userName?.trim()?.takeIf { it.isNotEmpty() }
        ?.split(Regex("[\\s._-]+"))?.filter { it.isNotEmpty() }?.takeIf { it.isNotEmpty() }?.let { parts ->
            if (parts.size > 1) "${parts.first().first()}${parts.last().first()}".uppercase() else parts.first().take(2).uppercase()
        }

    val filterOptions: List<HomeFilterOption> get() {
        val filters = when (selected) {
            HomeTab.Running -> listOf(QuickFilter.RunningAll, QuickFilter.RunningFavorites)
            HomeTab.Queue -> listOf(QuickFilter.QueuedAll, QuickFilter.QueuedFavorites)
            HomeTab.Agents -> listOf(QuickFilter.AgentsConnected, QuickFilter.AgentsDisconnected)
            else -> return emptyList()
        }
        val current = when (selected) {
            HomeTab.Running -> query?.runningFilter
            HomeTab.Queue -> query?.queueFilter
            else -> query?.agentsFilter
        }
        return filters.map { filter ->
            HomeFilterOption(
                filter,
                when (filter) {
                    QuickFilter.RunningAll, QuickFilter.QueuedAll -> Res.string.home_filter_all_builds
                    QuickFilter.RunningFavorites, QuickFilter.QueuedFavorites -> Res.string.home_filter_favorites
                    QuickFilter.AgentsConnected -> Res.string.home_filter_connected
                    QuickFilter.AgentsDisconnected -> Res.string.home_filter_disconnected
                },
                filter == current
            )
        }
    }

    val retryMessageRes: StringResource? get() {
        if (!isActivity) return null
        val failed = setOf(HomeTab.Running, HomeTab.Queue, HomeTab.Agents).filter { badges[it] is HomeBadge.Failed }.toSet()
        return when {
            failed.isEmpty() -> null
            failed == setOf(HomeTab.Running) -> Res.string.home_running_count_failed
            failed == setOf(HomeTab.Queue) -> Res.string.home_queue_count_failed
            failed == setOf(HomeTab.Agents) -> Res.string.home_agents_count_failed
            HomeTab.Agents !in failed -> Res.string.home_build_counts_failed
            else -> Res.string.home_build_and_agent_counts_failed
        }
    }

    val filterErrorRes: StringResource? get() = if (filterFailed && isActivity) Res.string.home_filter_failed else null
}
fun HomePrompt.ui() = when (this) {
    HomePrompt.Drawer -> HomePromptUi(this, Res.string.home_account_tip_title, Res.string.home_account_tip_description)
    HomePrompt.Favorites -> HomePromptUi(this, Res.string.title_onboarding_add_fav, Res.string.text_onboarding_add_fav)
    HomePrompt.Running -> HomePromptUi(this, Res.string.title_onboarding_filter, Res.string.text_onboarding_filter)
    HomePrompt.Queue -> HomePromptUi(this, Res.string.title_onboarding_filter_queued, Res.string.text_onboarding_filter_queued)
    HomePrompt.Agents -> HomePromptUi(this, Res.string.title_onboarding_filter_agents, Res.string.text_onboarding_filter_agents)
}
