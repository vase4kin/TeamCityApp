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

package teamcityapp.features.navigation.impl

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
import androidx.compose.ui.tooling.preview.Preview
import teamcityapp.features.navigation.api.NavigationEntry
import teamcityapp.libraries.build_configurations.BuildConfigurationSummary
import teamcityapp.libraries.build_configurations.ProjectReference
import teamcityapp.libraries.list_state.ListUiState
import teamcityapp.libraries.list_ui.*
import teamcityapp.libraries.theme.ErrorNotice
import teamcityapp.libraries.theme.TeamCityDimensions
import teamcityapp.libraries.theme.TeamCityTheme
import teamcityapp.libraries.theme.resolve

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationScreen(
    state: NavigationUiState,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    onNavigateUp: () -> Unit,
    onEntryClick: (NavigationEntry) -> Unit,
    onRateCancel: () -> Unit,
    onRateNow: () -> Unit,
    modifier: Modifier = Modifier,
    onRatingRetry: () -> Unit = onRetry
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(state.title.resolve()) },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            painterResource(if (state.root) R.drawable.ic_navigation_drawer else R.drawable.ic_navigation_back),
                            stringResource(state.navigationLabelRes),
                            Modifier.testTag("navigation:up")
                        )
                    }
                }
            )
        }
    ) { padding ->
        TeamCityListContainer(
            state.list,
            onRefresh,
            onRetry,
            modifier = Modifier.padding(padding).fillMaxSize(),
            empty = { TeamCityListEmpty(stringResource(R.string.navigation_empty)) }
        ) {
            val entries = (state.list as? ListUiState.Content<NavigationEntry>)?.items ?: return@TeamCityListContainer
            val occurrences = mutableMapOf<String, Int>()
            val keys = entries.map { entry ->
                val identity = entry.identity()
                val occurrence = occurrences.getOrDefault(identity, 0)
                occurrences[identity] = occurrence + 1
                "$identity:$occurrence"
            }
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                LazyColumn(Modifier.widthIn(max = TeamCityDimensions.screenContentMaxWidth).fillMaxSize().testTag("navigation:list"), contentPadding = PaddingValues(vertical = TeamCityDimensions.smallSpacing)) {
                    itemsIndexed(entries, key = { index, _ -> keys[index] }, contentType = { _, entry -> entry::class }) { index, entry ->
                        val hasRating = state.rating is RatingPromptState.Available || state.rating == RatingPromptState.Unavailable
                        val hasPrevious = index > 0 && entries[index - 1]::class == entry::class && !(index == 1 && hasRating)
                        val hasNext = index + 1 < entries.size && entries[index + 1]::class == entry::class && !(index == 0 && hasRating)
                        NavigationEntryRow(entry, { onEntryClick(entry) }, position = listRowPosition(hasPrevious, hasNext))
                        if (index == 0 && state.rating is RatingPromptState.Available) {
                            RatingPromptCard(state.rating, onRateCancel, onRateNow)
                        } else if (index == 0 && state.rating == RatingPromptState.Unavailable) {
                            RatingUnavailable(onRatingRetry)
                        }
                    }
                }
            }
        }
    }
}

private fun NavigationEntry.identity(): String = when (this) {
    is NavigationEntry.Project -> "project:${reference.id}"
    is NavigationEntry.Configuration -> "configuration:${configuration.id}"
}

@Composable
internal fun NavigationEntryRow(entry: NavigationEntry, onClick: () -> Unit, modifier: Modifier = Modifier, position: ListRowPosition = ListRowPosition.Single) {
    val name: String
    val description: String?
    val icon: Int
    when (entry) {
        is NavigationEntry.Project -> {
            name = entry.reference.name
            description = entry.description
            icon = R.drawable.ic_filter_none_black_24dp
        }

        is NavigationEntry.Configuration -> {
            name = entry.configuration.name
            description = entry.configuration.description
            icon = R.drawable.ic_crop_din_black_24dp
        }
    }
    TeamCityListRow(
        onClick = onClick,
        modifier = modifier.testTag("navigation:row:${entry.identity()}"),
        position = position,
        leadingContent = {
            TeamCityListLeadingIcon {
                Icon(painterResource(icon), null, Modifier.size(TeamCityDimensions.iconSize))
            }
        }
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(TeamCityDimensions.extraSmallSpacing)) {
            Text(name, style = MaterialTheme.typography.titleMedium)
            if (description != null) Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
internal fun RatingPromptCard(state: RatingPromptState.Available, onCancel: () -> Unit, onRateNow: () -> Unit) {
    ElevatedCard(Modifier.fillMaxWidth().padding(TeamCityDimensions.contentPadding).testTag("navigation:rating")) {
        Column(Modifier.padding(TeamCityDimensions.contentPadding)) {
            Text(stringResource(R.string.navigation_rate_title), style = MaterialTheme.typography.titleMedium)
            Text(stringResource(R.string.navigation_rate_description), Modifier.padding(top = TeamCityDimensions.smallSpacing), style = MaterialTheme.typography.bodyMedium)
            if (state.saveFailed) ErrorNotice(stringResource(R.string.navigation_rate_save_failed), modifier = Modifier.padding(top = TeamCityDimensions.smallSpacing))
            Row(Modifier.fillMaxWidth().padding(top = TeamCityDimensions.smallSpacing), horizontalArrangement = Arrangement.End) {
                TextButton(onCancel, enabled = !state.isSaving, modifier = Modifier.testTag("navigation:rate-cancel")) { Text(stringResource(R.string.navigation_rate_cancel)) }
                TextButton(onRateNow, enabled = !state.isSaving, modifier = Modifier.testTag("navigation:rate-now")) { Text(stringResource(R.string.navigation_rate_now)) }
            }
            if (state.isSaving) LinearProgressIndicator(Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun RatingUnavailable(onRetry: () -> Unit) {
    ErrorNotice(stringResource(R.string.navigation_rate_unavailable), onRetry, Modifier.fillMaxWidth().padding(horizontal = TeamCityDimensions.contentPadding, vertical = TeamCityDimensions.smallSpacing).testTag("navigation:rating-unavailable"), actionLabel = stringResource(R.string.navigation_rate_retry))
}

@Preview
@Composable
private fun NavigationEntryRowPreview() {
    TeamCityTheme { NavigationEntryRow(NavigationEntry.Project(ProjectReference("project", "TeamCity"), "Build and test configurations"), {}) }
}

@Preview
@Composable
private fun RatingPromptPreview() {
    TeamCityTheme { RatingPromptCard(RatingPromptState.Available(), {}, {}) }
}

@Preview(widthDp = 1000)
@Composable
private fun NavigationScreenPreview() {
    val project = ProjectReference("project", "TeamCity")
    val entries = listOf(NavigationEntry.Project(project), NavigationEntry.Configuration(BuildConfigurationSummary("build", "Android", "Release build", project)))
    TeamCityTheme { NavigationScreen(NavigationUiState(project, ListUiState.Content(entries), root = true), {}, {}, {}, {}, {}, {}) }
}
