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
    val onboardingFailed: Boolean = false
) {
    @get:StringRes val retryLabelRes: Int get() = if (onboardingFailed) R.string.home_retry else R.string.home_retry_counts

    @get:StringRes val fabLabelRes: Int get() = if (selected == HomeTab.Favorites) R.string.home_add_favorite else R.string.home_filter
}
internal fun HomePrompt.ui() = when (this) {
    HomePrompt.Drawer -> HomePromptUi(this, R.string.title_onboarding_navigation_drawer, R.string.text_onboarding_navigation_drawer)
    HomePrompt.Favorites -> HomePromptUi(this, R.string.title_onboarding_add_fav, R.string.text_onboarding_add_fav)
    HomePrompt.Running -> HomePromptUi(this, R.string.title_onboarding_filter, R.string.text_onboarding_filter)
    HomePrompt.Queue -> HomePromptUi(this, R.string.title_onboarding_filter_queued, R.string.text_onboarding_filter_queued)
    HomePrompt.Agents -> HomePromptUi(this, R.string.title_onboarding_filter_agents, R.string.text_onboarding_filter_agents)
}
