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

package teamcityapp.features.artifacts.impl

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import teamcityapp.features.artifacts.api.Artifact
import teamcityapp.features.artifacts.presentation.resources.*
import teamcityapp.libraries.list_state.ListUiState
import teamcityapp.libraries.list_ui.*
import teamcityapp.libraries.theme.BindTeamCityAppBarToList
import teamcityapp.libraries.theme.LocalTeamCityAppBarState
import teamcityapp.libraries.theme.TeamCityDimensions
import teamcityapp.libraries.theme.TeamCityLinearLoadingIndicator
import teamcityapp.libraries.theme.TeamCityTheme
import teamcityapp.libraries.theme.TeamCityTooltip
import teamcityapp.libraries.theme.teamCityTopAppBarColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtifactsScreen(
    state: ArtifactsUiState,
    showToolbar: Boolean,
    platformError: ArtifactPlatformError?,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    onNavigateUp: () -> Unit,
    onArtifactClick: (Artifact) -> Unit,
    onArtifactLongClick: (Artifact) -> Unit,
    onRetryDownload: () -> Unit,
    onCancelDownload: () -> Unit,
    onDismissPlatformError: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val listState = rememberLazyListState()
    BindTeamCityAppBarToList(listState, if (showToolbar) scrollBehavior.state else LocalTeamCityAppBarState.current)
    Scaffold(modifier.nestedScroll(scrollBehavior.nestedScrollConnection), topBar = {
        if (showToolbar) {
            CenterAlignedTopAppBar(
                modifier = Modifier.testTag("screen:app-bar"),
                colors = teamCityTopAppBarColors(),
                scrollBehavior = scrollBehavior,
                title = { Text(state.title) },
                navigationIcon = {
                    TeamCityTooltip(stringResource(Res.string.artifacts_back)) {
                        IconButton(onNavigateUp) { Icon(painterResource(Res.drawable.ic_artifacts_back), stringResource(Res.string.artifacts_back)) }
                    }
                }
            )
        }
    }) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            ArtifactDownloadCard(state.download, onCancelDownload, onRetryDownload)
            TeamCityListContainer(state.list, onRefresh, onRetry, modifier = Modifier.weight(1f), empty = { TeamCityListEmpty(stringResource(Res.string.artifacts_empty)) }) {
                val rows = (state.list as? ListUiState.Content<Artifact>)?.items.orEmpty()
                val occurrences = mutableMapOf<String, Int>()
                val keys = rows.map { file ->
                    val occurrence = (occurrences[file.href] ?: 0)
                    occurrences[file.href] = occurrence + 1
                    "${file.href}:$occurrence"
                }
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                    LazyColumn(state = listState, modifier = Modifier.widthIn(max = TeamCityDimensions.screenContentMaxWidth).fillMaxSize().testTag("artifacts:list"), contentPadding = PaddingValues(vertical = TeamCityDimensions.smallSpacing)) {
                        itemsIndexed(rows, key = { index, _ -> keys[index] }) { index, file ->
                            ArtifactRow(file, { onArtifactClick(file) }, { onArtifactLongClick(file) }, listRowPosition(index, rows.size), enabled = file.isFolder || state.download !is ArtifactDownloadState.Downloading)
                        }
                    }
                }
            }
        }
    }
    platformError?.let { error ->
        AlertDialog(
            onDismissRequest = onDismissPlatformError,
            title = { Text(stringResource(error.titleRes)) },
            text = {
                Text(
                    stringResource(
                        error.messageRes
                    )
                )
            },
            confirmButton = { TextButton(onDismissPlatformError) { Text(stringResource(Res.string.artifacts_ok)) } }
        )
    }
}

@Composable
fun ArtifactRow(file: Artifact, onClick: () -> Unit, onLongClick: () -> Unit, position: ListRowPosition = ListRowPosition.Single, enabled: Boolean = true) {
    TeamCityListRow(
        onClick = { if (enabled) onClick() },
        onLongClick = { if (enabled) onLongClick() },
        onLongClickLabel = stringResource(Res.string.artifacts_show_actions),
        modifier = Modifier.testTag("artifacts:row:${file.href}").then(if (enabled) Modifier else Modifier.semantics { disabled() }),
        position = position,
        leadingContent = {
            TeamCityListLeadingIcon {
                Icon(painterResource(if (file.childrenHref != null) Res.drawable.ic_artifacts_folder else Res.drawable.ic_artifacts_file), null, Modifier.size(TeamCityDimensions.iconSize))
            }
        }
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(TeamCityDimensions.extraSmallSpacing)) {
            Text(file.name, style = MaterialTheme.typography.titleMedium)
            if (file.size != 0L) Text(artifactFileSize(file.size), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Preview
@Composable
private fun ArtifactRowPreview() {
    TeamCityTheme { ArtifactRow(Artifact("teamcity.zip", "/metadata/teamcity.zip", 2048, childrenHref = "/children/teamcity.zip"), {}, {}) }
}

@Preview(widthDp = 1000)
@Composable
private fun ArtifactsScreenPreview() {
    TeamCityTheme { ArtifactsScreen(ArtifactsUiState("Artifacts", ListUiState.Empty()), true, null, {}, {}, {}, {}, {}, {}, {}, {}) }
}

@Composable
internal expect fun artifactFileSize(size: Long): String
