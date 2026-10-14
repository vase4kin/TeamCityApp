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

import androidx.annotation.StringRes
import teamcityapp.features.build_details.api.*
import teamcityapp.features.build_overview.api.BuildOverviewAction
import teamcityapp.libraries.builds.BuildLaunchData
import teamcityapp.libraries.list_state.ListUiState

data class BuildOverviewUiState(
    val list: ListUiState<OverviewRow> = ListUiState.Loading,
    val build: BuildLaunchData? = null,
    val actions: List<BuildOverviewAction> = emptyList(),
    val sections: List<OverviewSectionUiState> = emptyList(),
    @get:StringRes val statusLabel: Int = R.string.overview_result
) {
    val actionItems: List<OverviewActionUiState> = actions.map { OverviewActionUiState(it, it.label()) }
}

data class OverviewActionUiState(val action: BuildOverviewAction, @get:StringRes val labelRes: Int)
internal fun buildOverviewState(state: ListUiState<BuildLaunchData>, incoming: BuildLaunchData? = null, counts: Map<BuildDetailsSection, BuildDetailsCount> = emptyMap()): BuildOverviewUiState = when (state) {
    ListUiState.Loading -> BuildOverviewUiState()

    ListUiState.Error -> BuildOverviewUiState(list = ListUiState.Error)

    is ListUiState.Empty -> BuildOverviewUiState(list = state)

    is ListUiState.Content -> {
        val build = state.items.single()
        val actions = overviewActions(build).filter { !state.isRefreshing || it == BuildOverviewAction.Log }
        BuildOverviewUiState(
            ListUiState.Content(overviewRows(build), state.isRefreshing, state.refreshFailed),
            build,
            actions,
            (incoming ?: build).detailsSections().map { section -> overviewSection(section, counts[section]) },
            when {
                build.isRunning -> R.string.overview_build_running
                build.isQueued -> R.string.overview_build_queued
                build.canceledInfo != null -> R.string.overview_build_stopped
                build.isFinished -> R.string.overview_build_complete
                else -> R.string.overview_result
            }
        )
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
) + if (build.isQueued) emptyList() else listOf(BuildOverviewAction.Log)

data class OverviewSectionUiState(val section: BuildDetailsSection, @get:StringRes val title: Int, @get:StringRes val description: Int, val count: BuildDetailsCount? = null) {
    @get:StringRes val countDescription: Int? = when (count) {
        BuildDetailsCount.Loading -> R.string.overview_count_loading
        BuildDetailsCount.Unavailable -> R.string.overview_count_unavailable
        else -> null
    }
}
internal fun overviewSection(section: BuildDetailsSection, count: BuildDetailsCount?): OverviewSectionUiState = when (section) {
    BuildDetailsSection.Changes -> OverviewSectionUiState(section, R.string.overview_changes, R.string.overview_changes_description, count ?: BuildDetailsCount.Loading)
    BuildDetailsSection.Tests -> OverviewSectionUiState(section, R.string.overview_tests, R.string.overview_tests_description, count ?: BuildDetailsCount.Loading)
    BuildDetailsSection.Artifacts -> OverviewSectionUiState(section, R.string.overview_artifacts, R.string.overview_artifacts_description)
    BuildDetailsSection.Parameters -> OverviewSectionUiState(section, R.string.overview_parameters, R.string.overview_parameters_description)
    BuildDetailsSection.Snapshots -> OverviewSectionUiState(section, R.string.overview_dependencies, R.string.overview_dependencies_description)
    else -> error("Not an overview section")
}
