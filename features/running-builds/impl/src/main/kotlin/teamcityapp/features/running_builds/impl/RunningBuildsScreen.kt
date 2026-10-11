/*
 * Copyright 2019 Andrey Tolpeev
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

package teamcityapp.features.running_builds.impl

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import teamcityapp.features.running_builds.api.RunningBuildsFilter
import teamcityapp.libraries.build_ui.TeamCityBuildConfigurationHeader
import teamcityapp.libraries.build_ui.TeamCityBuildRow
import teamcityapp.libraries.build_ui.buildConfigurationTitle
import teamcityapp.libraries.build_ui.buildRowKeys
import teamcityapp.libraries.builds.BuildLaunchData
import teamcityapp.libraries.list_state.ListUiState
import teamcityapp.libraries.list_ui.TeamCityListContainer
import teamcityapp.libraries.list_ui.TeamCityListEmpty
import teamcityapp.libraries.list_ui.listRowPosition
import teamcityapp.libraries.resources.R as SharedR
import teamcityapp.libraries.theme.TeamCityDimensions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RunningBuildsScreen(
    state: RunningBuildsUiState,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    onOpenDrawer: () -> Unit,
    onBuild: (BuildLaunchData) -> Unit,
    onBuildHistory: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(modifier, topBar = {
        CenterAlignedTopAppBar(title = { Text(stringResource(R.string.running_builds_title)) }, navigationIcon = {
            IconButton(onClick = onOpenDrawer, modifier = Modifier.testTag("running_builds:drawer")) { Icon(painterResource(SharedR.drawable.ic_dehaze_black_24dp), stringResource(R.string.running_builds_open_drawer)) }
        })
    }) { padding ->
        TeamCityListContainer(state.list, onRefresh, onRetry, modifier = Modifier.padding(padding).fillMaxSize(), empty = {
            TeamCityListEmpty(stringResource(state.emptyMessageRes))
        }) {
            val rows = (state.list as? ListUiState.Content<BuildLaunchData>)?.items ?: return@TeamCityListContainer
            val rowKeys = remember(rows) { buildRowKeys(rows) }
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                LazyColumn(Modifier.widthIn(max = TeamCityDimensions.screenContentMaxWidth).fillMaxSize().testTag("running_builds:list"), contentPadding = PaddingValues(bottom = TeamCityDimensions.contentPadding)) {
                    items(rows.size, key = { index -> rowKeys[index] }, contentType = { "build" }) { index ->
                        val build = rows[index]
                        if (index == 0 || buildConfigurationTitle(rows[index - 1]) != buildConfigurationTitle(build)) {
                            TeamCityBuildConfigurationHeader(build, onBuildHistory, Modifier.testTag("running_builds:configuration:$index"))
                        }
                        TeamCityBuildRow(
                            state.rows[index],
                            { onBuild(build) },
                            Modifier.testTag("running_builds:build:${build.id}"),
                            position = listRowPosition(
                                hasPrevious = index > 0 && buildConfigurationTitle(rows[index - 1]) == buildConfigurationTitle(build),
                                hasNext = index + 1 < rows.size && buildConfigurationTitle(rows[index + 1]) == buildConfigurationTitle(build)
                            )
                        )
                    }
                }
            }
        }
    }
}
