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

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import teamcityapp.features.build_details.api.*
import teamcityapp.features.build_overview.api.BuildOverviewAction
import teamcityapp.libraries.list_state.ListUiState
import teamcityapp.libraries.list_ui.*
import teamcityapp.libraries.theme.TeamCityDimensions
import teamcityapp.libraries.theme.TeamCityTheme
import teamcityapp.libraries.theme.resolve
import teamcityapp.libraries.theme.teamCityStatusColors

@Composable
fun BuildOverviewScreen(
    state: BuildOverviewUiState,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    onRowClick: (OverviewRow) -> Unit,
    modifier: Modifier = Modifier,
    onSection: (BuildDetailsSection) -> Unit = {},
    onLog: () -> Unit = {},
    onCountRetry: (BuildDetailsSection) -> Unit = {},
    bottomContentPadding: androidx.compose.ui.unit.Dp = 0.dp
) {
    TeamCityListContainer(state.list, onRefresh, onRetry, modifier = modifier.fillMaxSize().testTag("overview:screen"), empty = { TeamCityListEmpty(stringResource(R.string.overview_empty)) }) {
        val rows = (state.list as? ListUiState.Content<OverviewRow>)?.items.orEmpty()
        val status = rows.firstOrNull { it.field == OverviewField.Result || it.field == OverviewField.WaitReason }
        val time = rows.firstOrNull { it.field == OverviewField.Time || it.field == OverviewField.QueuedTime }
        val context = rows.filter { it.field == OverviewField.Configuration || it.field == OverviewField.Project }
        val execution = rows.filter { it != status && it != time && it !in context }
        val results = state.sections.filter { it.section in listOf(BuildDetailsSection.Changes, BuildDetailsSection.Tests, BuildDetailsSection.Artifacts) }
        val setup = state.sections.filter { it.section in listOf(BuildDetailsSection.Parameters, BuildDetailsSection.Snapshots) }
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            LazyColumn(Modifier.widthIn(max = TeamCityDimensions.screenContentMaxWidth).fillMaxSize().testTag("overview:list"), contentPadding = PaddingValues(top = TeamCityDimensions.smallSpacing, bottom = TeamCityDimensions.smallSpacing + bottomContentPadding)) {
                status?.let { row -> item(key = "status") { OverviewStatusCard(row, time, state.statusLabel, BuildOverviewAction.Log in state.actions, onRowClick, onLog) } }
                if (results.isNotEmpty()) {
                    item(key = "results-title") { OverviewGroupHeading(R.string.overview_results) }
                    itemsIndexed(results, key = { _, section -> section.section }) { index, section -> OverviewSectionRow(section, { onSection(section.section) }, { onCountRetry(section.section) }, listRowPosition(index, results.size)) }
                }
                if (execution.isNotEmpty()) {
                    item(key = "execution-title") { OverviewGroupHeading(R.string.overview_execution) }
                    itemsIndexed(execution, key = { _, row -> row.field }) { index, row -> OverviewRowContent(row, { onRowClick(row) }, listRowPosition(index, execution.size)) }
                }
                if (setup.isNotEmpty()) {
                    item(key = "setup-title") { OverviewGroupHeading(R.string.overview_setup) }
                    itemsIndexed(setup, key = { _, section -> section.section }) { index, section -> OverviewSectionRow(section, { onSection(section.section) }, { onCountRetry(section.section) }, listRowPosition(index, setup.size)) }
                }
                if (context.isNotEmpty()) {
                    item(key = "context-title") { OverviewGroupHeading(R.string.overview_context) }
                    itemsIndexed(context, key = { _, row -> row.field }) { index, row -> OverviewRowContent(row, { onRowClick(row) }, listRowPosition(index, context.size), showNavigation = row.action != OverviewRowAction.Copy) }
                }
            }
        }
    }
}

@Composable
private fun OverviewGroupHeading(@StringRes title: Int) {
    Text(stringResource(title), style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(horizontal = TeamCityDimensions.contentPadding + TeamCityDimensions.mediumSpacing, vertical = TeamCityDimensions.mediumSpacing))
}

@Composable
private fun OverviewStatusCard(row: OverviewRow, time: OverviewRow?, @StringRes label: Int, showLog: Boolean, onRow: (OverviewRow) -> Unit, onLog: () -> Unit) {
    val statusColors = teamCityStatusColors()
    val (containerColor, contentColor) = when {
        label == R.string.overview_build_stopped || row.icon == OverviewIcon.Queued || row.icon == OverviewIcon.Unknown -> statusColors.warning.let { it.container to it.onContainer }
        row.icon == OverviewIcon.Failure || row.icon == OverviewIcon.Error -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
        row.icon == OverviewIcon.Success -> statusColors.success.let { it.container to it.onContainer }
        else -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
    }
    Card(
        Modifier.padding(horizontal = TeamCityDimensions.contentPadding, vertical = TeamCityDimensions.smallSpacing).fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = containerColor, contentColor = contentColor)
    ) {
        Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(TeamCityDimensions.smallSpacing)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(TeamCityDimensions.smallSpacing)) {
                if (row.icon == OverviewIcon.Running) {
                    CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = TeamCityDimensions.progressStrokeWidth)
                } else {
                    Icon(painterResource(row.icon.drawable()), null, Modifier.size(24.dp))
                }
                Text(stringResource(label), style = MaterialTheme.typography.labelLarge)
            }
            Text(row.text.resolve(), style = MaterialTheme.typography.headlineMedium, modifier = Modifier.testTag("overview:row:${row.field}").clickable { onRow(row) })
            time?.let { Text(it.text.resolve(), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.testTag("overview:row:${it.field}").clickable { onRow(it) }) }
            if (showLog) {
                FilledTonalButton(onClick = onLog, modifier = Modifier.testTag("overview:log"), colors = ButtonDefaults.filledTonalButtonColors(containerColor = MaterialTheme.colorScheme.surface, contentColor = MaterialTheme.colorScheme.primary)) {
                    Text(stringResource(R.string.overview_view_log))
                    Spacer(Modifier.width(8.dp))
                    Icon(painterResource(R.drawable.overview_browser), null, Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun OverviewSectionRow(section: OverviewSectionUiState, onClick: () -> Unit, onRetry: () -> Unit, position: ListRowPosition) {
    val countDescription = section.countDescription?.let { stringResource(it) }
    TeamCityListRow(onClick, Modifier.testTag("overview:section:${section.section.name}"), position, leadingContent = {
        TeamCityListLeadingIcon { Icon(painterResource(section.section.drawable()), null, Modifier.size(TeamCityDimensions.iconSize)) }
    }) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(TeamCityDimensions.extraSmallSpacing)) {
            Text(stringResource(section.title), style = MaterialTheme.typography.titleMedium)
            Text(stringResource(section.description), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        when (val count = section.count) {
            is BuildDetailsCount.Available -> Badge(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest, contentColor = MaterialTheme.colorScheme.onSurfaceVariant) { Text(count.count.toString()) }
            BuildDetailsCount.Loading -> CircularProgressIndicator(Modifier.size(18.dp).semantics { contentDescription = countDescription.orEmpty() }, strokeWidth = 2.dp)
            BuildDetailsCount.Unavailable -> IconButton(onClick = onRetry, modifier = Modifier.testTag("overview:retry-count:${section.section.name}")) { Icon(painterResource(R.drawable.overview_retry), stringResource(R.string.overview_retry_count) + ": " + countDescription.orEmpty()) }
            null -> Unit
        }
        Icon(painterResource(R.drawable.overview_chevron), null, Modifier.size(18.dp))
    }
}
private fun BuildDetailsSection.drawable(): Int = when (this) {
    BuildDetailsSection.Changes -> R.drawable.overview_branch
    BuildDetailsSection.Tests -> R.drawable.overview_tests
    BuildDetailsSection.Artifacts -> R.drawable.overview_artifacts
    BuildDetailsSection.Parameters -> R.drawable.overview_parameters
    BuildDetailsSection.Snapshots -> R.drawable.overview_dependencies
    else -> error("Not a build section")
}

@Composable
internal fun OverviewRowContent(row: OverviewRow, onClick: () -> Unit, position: ListRowPosition = ListRowPosition.Single, showNavigation: Boolean = false) {
    val isError = row.icon == OverviewIcon.Failure || row.icon == OverviewIcon.Error
    TeamCityListRow(
        onClick = onClick,
        modifier = Modifier.testTag("overview:row:${row.field}"),
        position = position,
        leadingContent = {
            TeamCityListLeadingIcon(
                containerColor = if (isError) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceContainerHighest,
                contentColor = if (isError) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurfaceVariant
            ) {
                if (row.icon == OverviewIcon.Running) {
                    CircularProgressIndicator(Modifier.size(TeamCityDimensions.iconSize), strokeWidth = TeamCityDimensions.progressStrokeWidth)
                } else {
                    Icon(painterResource(row.icon.drawable()), null, Modifier.size(TeamCityDimensions.iconSize))
                }
            }
        }
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(TeamCityDimensions.extraSmallSpacing)) {
            Text(stringResource(row.labelRes), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(row.text.resolve(), style = MaterialTheme.typography.titleMedium)
        }
        if (showNavigation) Icon(painterResource(R.drawable.overview_chevron), null, Modifier.size(18.dp))
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
