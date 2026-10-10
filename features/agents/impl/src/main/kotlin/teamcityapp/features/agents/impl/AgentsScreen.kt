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

package teamcityapp.features.agents.impl

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import teamcityapp.features.agents.api.Agent
import teamcityapp.features.agents.api.AgentsFilter
import teamcityapp.libraries.list_state.ListUiState
import teamcityapp.libraries.list_ui.TeamCityListContainer
import teamcityapp.libraries.list_ui.TeamCityListEmpty
import teamcityapp.libraries.resources.R as SharedR
import teamcityapp.libraries.theme.TeamCityDimensions
import teamcityapp.libraries.theme.TeamCityTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentsScreen(
    state: AgentsUiState,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    onOpenDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.agents_title)) },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer, modifier = Modifier.testTag("agents:drawer")) {
                        Icon(painterResource(SharedR.drawable.ic_dehaze_black_24dp), stringResource(R.string.agents_open_drawer))
                    }
                }
            )
        }
    ) { padding ->
        TeamCityListContainer(
            state = state.list,
            onRefresh = onRefresh,
            onRetry = onRetry,
            modifier = Modifier.padding(padding).fillMaxSize(),
            empty = {
                TeamCityListEmpty(
                    stringResource(
                        if (state.filter == AgentsFilter.Connected) R.string.agents_empty_connected else R.string.agents_empty_disconnected
                    )
                )
            }
        ) {
            val content = state.list as? ListUiState.Content<Agent> ?: return@TeamCityListContainer
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                LazyColumn(Modifier.widthIn(max = TeamCityDimensions.screenContentMaxWidth).fillMaxSize().testTag("agents:list")) {
                    items(content.items, key = { it.id }, contentType = { "agent" }) { agent ->
                        AgentRow(agent)
                    }
                }
            }
        }
    }
}

@Composable
internal fun AgentRow(agent: Agent, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth().testTag("agents:row:${agent.id}")) {
        Row(Modifier.fillMaxWidth().padding(TeamCityDimensions.contentPadding), verticalAlignment = Alignment.CenterVertically) {
            Icon(painterResource(SharedR.drawable.ic_directions_railway_black_24dp), null, Modifier.size(TeamCityDimensions.iconSize))
            Text(agent.name, Modifier.padding(start = TeamCityDimensions.contentPadding), style = MaterialTheme.typography.bodyLarge)
        }
        HorizontalDivider(Modifier.padding(start = 56.dp))
    }
}

@Preview
@Composable
private fun AgentRowPreview() {
    TeamCityTheme { AgentRow(Agent("1", "Linux build agent")) }
}

@Preview(widthDp = 1000)
@Composable
private fun AgentsScreenPreview() {
    TeamCityTheme { AgentsScreen(AgentsUiState(list = ListUiState.Content(listOf(Agent("1", "Linux build agent")))), {}, {}, {}) }
}
