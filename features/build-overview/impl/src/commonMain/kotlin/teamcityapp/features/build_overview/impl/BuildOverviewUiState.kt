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
import org.jetbrains.compose.resources.StringResource
import teamcityapp.features.build_details.api.*
import teamcityapp.features.build_overview.api.BuildOverviewAction
import teamcityapp.features.build_overview.impl.resources.*
import teamcityapp.libraries.builds.BuildLaunchData
import teamcityapp.libraries.list_state.ListUiState

data class BuildOverviewUiState(
    val list: ListUiState<OverviewRow> = ListUiState.Loading,
    val build: BuildLaunchData? = null,
    val actions: List<BuildOverviewAction> = emptyList(),
    val sections: List<OverviewSectionUiState> = emptyList(),
    val statusLabel: StringResource = Res.string.overview_result
)
fun buildOverviewState(state: ListUiState<BuildLaunchData>, incoming: BuildLaunchData? = null, counts: Map<BuildDetailsSection, BuildDetailsCount> = emptyMap()): BuildOverviewUiState = when (state) {
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
                build.isRunning -> Res.string.overview_build_running
                build.isQueued -> Res.string.overview_build_queued
                build.canceledInfo != null -> Res.string.overview_build_stopped
                build.isFinished -> Res.string.overview_build_complete
                else -> Res.string.overview_result
            }
        )
    }
}
fun overviewActions(build: BuildLaunchData): List<BuildOverviewAction> = listOf(
    BuildOverviewAction.Share,
    BuildOverviewAction.Browser,
    when {
        build.isRunning -> BuildOverviewAction.Stop
        build.isQueued -> BuildOverviewAction.RemoveFromQueue
        else -> BuildOverviewAction.Restart
    }
) + if (build.isQueued) emptyList() else listOf(BuildOverviewAction.Log)

data class OverviewSectionUiState(val section: BuildDetailsSection, val title: StringResource, val description: StringResource, val count: BuildDetailsCount? = null) {
    val countDescription: StringResource? = when (count) {
        BuildDetailsCount.Loading -> Res.string.overview_count_loading
        BuildDetailsCount.Unavailable -> Res.string.overview_count_unavailable
        else -> null
    }
}
fun overviewSection(section: BuildDetailsSection, count: BuildDetailsCount?): OverviewSectionUiState = when (section) {
    BuildDetailsSection.Changes -> OverviewSectionUiState(section, Res.string.overview_changes, Res.string.overview_changes_description, count ?: BuildDetailsCount.Loading)
    BuildDetailsSection.Tests -> OverviewSectionUiState(section, Res.string.overview_tests, Res.string.overview_tests_description, count ?: BuildDetailsCount.Loading)
    BuildDetailsSection.Artifacts -> OverviewSectionUiState(section, Res.string.overview_artifacts, Res.string.overview_artifacts_description)
    BuildDetailsSection.Parameters -> OverviewSectionUiState(section, Res.string.overview_parameters, Res.string.overview_parameters_description)
    BuildDetailsSection.Snapshots -> OverviewSectionUiState(section, Res.string.overview_dependencies, Res.string.overview_dependencies_description)
    else -> error("Not an overview section")
}
