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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.testTag
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import teamcityapp.features.running_builds.api.RunningBuildsFilter
import teamcityapp.features.running_builds.impl.resources.*
import teamcityapp.libraries.build_ui.TeamCityBuildConfigurationHeader
import teamcityapp.libraries.build_ui.TeamCityBuildRow
import teamcityapp.libraries.build_ui.buildConfigurationTitle
import teamcityapp.libraries.build_ui.buildRowKeys
import teamcityapp.libraries.builds.BuildLaunchData
import teamcityapp.libraries.list_state.ListUiState
import teamcityapp.libraries.list_ui.TeamCityListContainer
import teamcityapp.libraries.list_ui.TeamCityListEmpty
import teamcityapp.libraries.list_ui.listRowPosition
import teamcityapp.libraries.theme.BindTeamCityAppBarToList
import teamcityapp.libraries.theme.LocalTeamCityAppBarState
import teamcityapp.libraries.theme.LocalTeamCityHeaderProvided
import teamcityapp.libraries.theme.TeamCityDimensions
import teamcityapp.libraries.theme.TeamCityTooltip
import teamcityapp.libraries.theme.teamCityTopAppBarColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedRunningBuildsScreen(
    state: RunningBuildsUiState,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    onOpenDrawer: () -> Unit,
    onBuild: (BuildLaunchData) -> Unit,
    onBuildHistory: (String, String) -> Unit,
    modifier: Modifier = Modifier,
    onApplyUpdates: () -> Unit = {},
    drawerModifier: Modifier = Modifier
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val listState = rememberLazyListState()
    BindTeamCityAppBarToList(listState, LocalTeamCityAppBarState.current ?: scrollBehavior.state)
    Scaffold(modifier.nestedScroll(scrollBehavior.nestedScrollConnection), topBar = {
        if (!LocalTeamCityHeaderProvided.current) {
            CenterAlignedTopAppBar(
                modifier = Modifier.testTag("screen:app-bar"),
                colors = teamCityTopAppBarColors(),
                scrollBehavior = scrollBehavior,
                title = { Text(stringResource(Res.string.running_builds_title)) },
                navigationIcon = {
                    TeamCityTooltip(stringResource(Res.string.running_builds_open_drawer)) {
                        IconButton(onClick = onOpenDrawer, modifier = Modifier.testTag("running_builds:drawer").then(drawerModifier)) { Icon(painterResource(Res.drawable.ic_dehaze_black_24dp), stringResource(Res.string.running_builds_open_drawer)) }
                    }
                }
            )
        }
    }) { padding ->
        TeamCityListContainer(state.list, onRefresh, onRetry, updatesAvailable = state.updatesAvailable, onApplyUpdates = onApplyUpdates, modifier = Modifier.padding(padding).fillMaxSize(), empty = {
            TeamCityListEmpty(stringResource(state.emptyMessageRes))
        }) {
            val rows = (state.list as? ListUiState.Content<BuildLaunchData>)?.items ?: return@TeamCityListContainer
            val rowKeys = remember(rows) { buildRowKeys(rows) }
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                LazyColumn(state = listState, modifier = Modifier.widthIn(max = TeamCityDimensions.screenContentMaxWidth).fillMaxSize().testTag("running_builds:list"), contentPadding = PaddingValues(bottom = TeamCityDimensions.contentPadding)) {
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
