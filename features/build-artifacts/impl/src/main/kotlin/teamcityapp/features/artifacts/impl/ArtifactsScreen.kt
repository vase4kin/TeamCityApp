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

import android.text.format.Formatter
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import teamcityapp.features.artifacts.api.Artifact
import teamcityapp.libraries.list_state.ListUiState
import teamcityapp.libraries.list_ui.*
import teamcityapp.libraries.theme.ErrorNotice
import teamcityapp.libraries.theme.TeamCityDimensions
import teamcityapp.libraries.theme.TeamCityTheme

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
    Scaffold(modifier, topBar = {
        if (showToolbar) {
            CenterAlignedTopAppBar(
                title = { Text(state.title) },
                navigationIcon = { IconButton(onNavigateUp) { Icon(painterResource(R.drawable.ic_artifacts_back), stringResource(R.string.artifacts_back)) } }
            )
        }
    }) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            if (state.download is ArtifactDownloadState.Failed) {
                ErrorNotice(stringResource(R.string.artifacts_download_failed), onRetryDownload, Modifier.fillMaxWidth().padding(horizontal = TeamCityDimensions.contentPadding, vertical = TeamCityDimensions.smallSpacing), actionLabel = stringResource(R.string.artifacts_retry_download))
            }
            TeamCityListContainer(state.list, onRefresh, onRetry, modifier = Modifier.weight(1f), empty = { TeamCityListEmpty(stringResource(R.string.artifacts_empty)) }) {
                val rows = (state.list as? ListUiState.Content<Artifact>)?.items.orEmpty()
                val occurrences = mutableMapOf<String, Int>()
                val keys = rows.map { file ->
                    val occurrence = occurrences.getOrDefault(file.href, 0)
                    occurrences[file.href] = occurrence + 1
                    "${file.href}:$occurrence"
                }
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                    LazyColumn(Modifier.widthIn(max = TeamCityDimensions.screenContentMaxWidth).fillMaxSize().testTag("artifacts:list"), contentPadding = PaddingValues(vertical = TeamCityDimensions.smallSpacing)) {
                        itemsIndexed(rows, key = { index, _ -> keys[index] }) { index, file ->
                            ArtifactRow(file, { onArtifactClick(file) }, { onArtifactLongClick(file) }, listRowPosition(index, rows.size))
                        }
                    }
                }
            }
        }
    }
    if (state.download is ArtifactDownloadState.Downloading) {
        AlertDialog(
            onDismissRequest = onCancelDownload,
            title = { Text(stringResource(R.string.artifacts_downloading_title)) },
            text = {
                Column {
                    Text(stringResource(R.string.artifacts_downloading))
                    LinearProgressIndicator(Modifier.fillMaxWidth().padding(top = TeamCityDimensions.contentPadding))
                }
            },
            confirmButton = { TextButton(onCancelDownload) { Text(stringResource(R.string.artifacts_cancel)) } }
        )
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
            confirmButton = { TextButton(onDismissPlatformError) { Text(stringResource(R.string.artifacts_ok)) } }
        )
    }
}

@Composable
internal fun ArtifactRow(file: Artifact, onClick: () -> Unit, onLongClick: () -> Unit, position: ListRowPosition = ListRowPosition.Single) {
    val context = LocalContext.current
    TeamCityListRow(
        onClick = onClick,
        onLongClick = onLongClick,
        modifier = Modifier.testTag("artifacts:row:${file.href}"),
        position = position,
        leadingContent = {
            TeamCityListLeadingIcon {
                Icon(painterResource(if (file.childrenHref != null) R.drawable.ic_artifacts_folder else R.drawable.ic_artifacts_file), null, Modifier.size(TeamCityDimensions.iconSize))
            }
        }
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(TeamCityDimensions.extraSmallSpacing)) {
            Text(file.name, style = MaterialTheme.typography.titleMedium)
            if (file.size != 0L) Text(Formatter.formatFileSize(context, file.size), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
