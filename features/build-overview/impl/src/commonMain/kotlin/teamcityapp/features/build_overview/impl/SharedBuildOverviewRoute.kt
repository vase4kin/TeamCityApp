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

import androidx.compose.runtime.*
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.compose.*
import teamcityapp.features.build_details.api.BuildDetailsSection
import teamcityapp.features.build_overview.api.BuildOverviewAction
import teamcityapp.features.build_overview.impl.router.BuildOverviewActions
import teamcityapp.libraries.builds.BuildLaunchData
import teamcityapp.libraries.theme.SharedTeamCityScreen

@Composable
fun SharedBuildOverviewRoute(
    vm: BuildReferenceViewModel,
    content: BuildOverviewUiState,
    current: BuildLaunchData,
    busy: Boolean,
    actions: BuildOverviewActions,
    onConfiguration: (String) -> Unit,
    onProject: (String, String) -> Unit,
    onBranch: (String) -> Unit,
    onCopy: (String) -> Unit,
    onSection: (BuildDetailsSection) -> Unit,
    bottomContentPadding: Dp
) {
    SharedBuildOverviewScreen(
        content, vm::refresh, vm::retry, { row ->
            if (!busy) {
                when (row.action) {
                    OverviewRowAction.Configuration -> current.buildTypeId?.let {
                        actions.configuration()
                        onConfiguration(it)
                    }

                    OverviewRowAction.Project -> current.configuration?.projectId?.let {
                        actions.project()
                        onProject(it, current.configuration?.projectName.orEmpty())
                    }

                    OverviewRowAction.Branch -> (row.value as? OverviewText.Literal)?.let { onBranch(it.value) }

                    OverviewRowAction.Copy -> (row.value as? OverviewText.Literal)?.let { onCopy(it.value) }
                }
            }
        }, onSection = { if (!busy) onSection(it) },
        onLog = { actions.perform(BuildOverviewAction.Log, current) }, onCountRetry = vm::retryCount,
        bottomContentPadding = bottomContentPadding,
        rowActionAvailable = { !busy && (it.action != OverviewRowAction.Copy || it.value is OverviewText.Literal) }
    )
}

@Composable
fun SharedBuildReferenceRoute(vm: BuildReferenceViewModel, title: String, onBack: () -> Unit, onResolved: (BuildLaunchData) -> Unit) {
    val state by vm.state.collectAsStateWithLifecycle()
    state.build?.let { build ->
        LifecycleResumeEffect(build) {
            onResolved(build)
            onPauseOrDispose {}
        }
    }
    SharedTeamCityScreen(title, onBack) { modifier ->
        SharedBuildOverviewScreen(state.copy(actions = emptyList()), vm::refresh, vm::retry, {}, modifier, rowActionAvailable = { false })
    }
}

@Composable
fun SharedBuildSectionScreen(section: BuildDetailsSection, onBack: () -> Unit, content: @Composable (androidx.compose.ui.Modifier) -> Unit) {
    SharedTeamCityScreen(org.jetbrains.compose.resources.stringResource(overviewSection(section, null).title), onBack, content = content)
}
