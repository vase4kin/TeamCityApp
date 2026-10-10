/*
 * Copyright 2020 Andrey Tolpeev
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

package teamcityapp.features.favorites.impl

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import teamcityapp.libraries.build_configurations.BuildConfigurationSummary
import teamcityapp.libraries.build_configurations.ProjectReference
import teamcityapp.libraries.list_state.ListUiState
import teamcityapp.libraries.list_ui.TeamCityListContainer
import teamcityapp.libraries.list_ui.TeamCityListEmpty
import teamcityapp.libraries.theme.TeamCityDimensions
import teamcityapp.libraries.theme.TeamCityTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    state: FavoritesUiState,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    onOpenDrawer: () -> Unit,
    onProjectClick: (ProjectReference) -> Unit,
    onConfigurationClick: (BuildConfigurationSummary) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.favorites_title)) },
                navigationIcon = {
                    IconButton(onOpenDrawer, modifier = Modifier.testTag("favorites:up")) {
                        Icon(painterResource(R.drawable.ic_navigation_drawer), stringResource(R.string.favorites_open_drawer))
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            if (state.failure == FavoritesFailure.AllFailed) {
                Text(
                    stringResource(R.string.favorites_all_failed),
                    Modifier.fillMaxWidth().padding(TeamCityDimensions.contentPadding).semantics { liveRegion = LiveRegionMode.Polite },
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            TeamCityListContainer(
                state.list,
                onRefresh,
                onRetry,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                empty = { TeamCityListEmpty(stringResource(R.string.favorites_empty)) }
            ) {
                val rows = (state.list as? ListUiState.Content<BuildConfigurationSummary>)?.items ?: return@TeamCityListContainer
                // Group by identity even when separate projects share a display name.
                val groups = rows.groupBy { it.project.id }
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                    LazyColumn(Modifier.widthIn(max = TeamCityDimensions.screenContentMaxWidth).fillMaxSize().testTag("favorites:list")) {
                        if (state.failure is FavoritesFailure.Partial) {
                            item(key = "partial", contentType = "partial") {
                                FavoritesPartialFailure(state.failure.unavailableIds.size, onRetry, enabled = !state.list.isRefreshing())
                            }
                        }
                        groups.forEach { (id, configurations) ->
                            val project = configurations.first().project
                            item(key = "project:$id", contentType = "project") { FavoritesProjectHeader(project, { onProjectClick(project) }) }
                            val occurrences = mutableMapOf<String, Int>()
                            configurations.forEach { configuration ->
                                val occurrence = occurrences.getOrDefault(configuration.id, 0)
                                occurrences[configuration.id] = occurrence + 1
                                item(key = "configuration:$id:${configuration.id}:$occurrence", contentType = "configuration") {
                                    FavoriteConfigurationRow(configuration, { onConfigurationClick(configuration) })
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun ListUiState<*>.isRefreshing(): Boolean = when (this) {
    is ListUiState.Content -> isRefreshing
    is ListUiState.Empty -> isRefreshing
    else -> false
}

@Composable
internal fun FavoritesProjectHeader(project: ProjectReference, onClick: () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.surfaceContainer) {
        Text(
            project.name,
            Modifier.fillMaxWidth().testTag("favorites:project:${project.id}").clickable(onClick = onClick).padding(TeamCityDimensions.contentPadding),
            style = MaterialTheme.typography.titleSmall
        )
    }
}

@Composable
internal fun FavoriteConfigurationRow(configuration: BuildConfigurationSummary, onClick: () -> Unit) {
    Column(Modifier.fillMaxWidth().testTag("favorites:configuration:${configuration.id}").clickable(onClick = onClick)) {
        Row(Modifier.fillMaxWidth().padding(TeamCityDimensions.contentPadding), verticalAlignment = Alignment.CenterVertically) {
            Icon(painterResource(R.drawable.ic_crop_din_black_24dp), null, Modifier.size(TeamCityDimensions.iconSize))
            Column(Modifier.weight(1f).padding(start = TeamCityDimensions.contentPadding)) {
                Text(configuration.name, style = MaterialTheme.typography.bodyLarge)
                configuration.description?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        }
        HorizontalDivider()
    }
}

@Composable
private fun FavoritesPartialFailure(count: Int, onRetry: () -> Unit, enabled: Boolean) {
    Surface(color = MaterialTheme.colorScheme.errorContainer, modifier = Modifier.testTag("favorites:partial")) {
        Column(Modifier.fillMaxWidth().padding(TeamCityDimensions.contentPadding)) {
            Text(pluralStringResource(R.plurals.favorites_partial_failed, count, count), Modifier.semantics { liveRegion = LiveRegionMode.Polite }, style = MaterialTheme.typography.bodyMedium)
            TextButton(onRetry, enabled = enabled) { Text(stringResource(R.string.favorites_retry)) }
        }
    }
}

@Preview
@Composable
private fun FavoriteConfigurationRowPreview() {
    TeamCityTheme { FavoriteConfigurationRow(BuildConfigurationSummary("build", "Android release", "Build and publish the application", ProjectReference("project", "TeamCity")), {}) }
}

@Preview
@Composable
private fun FavoritesProjectHeaderPreview() {
    TeamCityTheme { FavoritesProjectHeader(ProjectReference("project", "TeamCity"), {}) }
}

@Preview(widthDp = 1000)
@Composable
private fun FavoritesScreenPreview() {
    val configuration = BuildConfigurationSummary("build", "Android release", null, ProjectReference("project", "TeamCity"))
    TeamCityTheme { FavoritesScreen(FavoritesUiState(ListUiState.Content(listOf(configuration)), FavoritesFailure.Partial(listOf("missing"))), {}, {}, {}, {}, {}) }
}
