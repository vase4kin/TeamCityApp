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

package teamcityapp.features.build_overview.impl

import androidx.compose.foundation.clickable
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
import teamcityapp.libraries.list_state.ListUiState
import teamcityapp.libraries.list_ui.TeamCityListContainer
import teamcityapp.libraries.list_ui.TeamCityListEmpty
import teamcityapp.libraries.theme.TeamCityDimensions
import teamcityapp.libraries.theme.TeamCityTheme

@Composable
fun BuildOverviewScreen(state: BuildOverviewUiState, onRefresh: () -> Unit, onRetry: () -> Unit, onRowClick: (OverviewRow) -> Unit, modifier: Modifier = Modifier) {
    TeamCityListContainer(state.list, onRefresh, onRetry, modifier = modifier.fillMaxSize().testTag("overview:screen"), empty = { TeamCityListEmpty(stringResource(R.string.overview_empty)) }) {
        val rows = (state.list as? ListUiState.Content<OverviewRow>)?.items.orEmpty()
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            LazyColumn(Modifier.widthIn(max = TeamCityDimensions.screenContentMaxWidth).fillMaxSize().testTag("overview:list")) {
                items(rows, key = { it.field }, contentType = { it.icon }) { row -> OverviewRowContent(row, { onRowClick(row) }) }
            }
        }
    }
}

@Composable
internal fun OverviewRowContent(row: OverviewRow, onClick: () -> Unit) {
    Column(Modifier.fillMaxWidth().testTag("overview:row:${row.field}").clickable(onClick = onClick)) {
        Row(Modifier.fillMaxWidth().padding(TeamCityDimensions.contentPadding), verticalAlignment = Alignment.CenterVertically) {
            if (row.icon == OverviewIcon.Running) {
                CircularProgressIndicator(Modifier.size(TeamCityDimensions.iconSize), strokeWidth = 2.dp)
            } else {
                Icon(painterResource(row.icon.drawable()), null, Modifier.size(TeamCityDimensions.iconSize))
            }
            Column(Modifier.weight(1f).padding(start = TeamCityDimensions.contentPadding)) {
                Text(stringResource(row.field.label()), style = MaterialTheme.typography.bodyLarge)
                Text(row.value.resolve(), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        HorizontalDivider(Modifier.padding(start = 56.dp))
    }
}
private fun OverviewIcon.drawable(): Int = when (this) {
    OverviewIcon.Success -> R.drawable.overview_success
    OverviewIcon.Failure -> R.drawable.overview_failure
    OverviewIcon.Error -> R.drawable.overview_error
    OverviewIcon.Unknown -> R.drawable.overview_unknown
    OverviewIcon.Queued -> R.drawable.overview_queued
    OverviewIcon.Time -> R.drawable.overview_time
    OverviewIcon.Branch -> R.drawable.overview_branch
    OverviewIcon.Agent -> R.drawable.overview_agent
    OverviewIcon.Person -> R.drawable.overview_person
    OverviewIcon.Configuration -> R.drawable.overview_configuration
    OverviewIcon.Project -> R.drawable.overview_project
    OverviewIcon.Running -> error("Running uses a progress indicator")
}

@Preview
@Composable
private fun OverviewRowPreview() {
    TeamCityTheme { OverviewRowContent(OverviewRow(OverviewField.Result, OverviewText.Literal("Build finished successfully"), OverviewIcon.Success), {}) }
}

@Preview(widthDp = 1000)
@Composable
private fun OverviewScreenPreview() {
    TeamCityTheme { BuildOverviewScreen(BuildOverviewUiState(list = ListUiState.Content(listOf(OverviewRow(OverviewField.WaitReason, OverviewText.QueuedBuild, OverviewIcon.Queued)))), {}, {}, {}) }
}
