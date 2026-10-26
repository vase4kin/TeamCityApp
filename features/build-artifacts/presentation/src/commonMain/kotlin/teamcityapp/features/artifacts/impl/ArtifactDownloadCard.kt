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

package teamcityapp.features.artifacts.impl

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.resources.stringResource
import teamcityapp.features.artifacts.api.ArtifactDownload
import teamcityapp.features.artifacts.presentation.resources.*
import teamcityapp.libraries.theme.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ArtifactDownloadCard(state: ArtifactDownloadState, onDismiss: () -> Unit, onRetry: () -> Unit, onOpen: () -> Unit = {}) {
    val name = when (state) {
        is ArtifactDownloadState.Downloading -> state.file.name
        is ArtifactDownloadState.Failed -> state.file.name
        is ArtifactDownloadState.Ready -> state.name
        else -> return
    }
    Box(Modifier.fillMaxWidth(), contentAlignment = androidx.compose.ui.Alignment.TopCenter) {
        Card(
            Modifier.widthIn(max = TeamCityDimensions.screenContentMaxWidth).fillMaxWidth().padding(TeamCityDimensions.contentPadding).testTag("artifacts:download-card").semantics { liveRegion = LiveRegionMode.Polite },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
        ) {
            Column(Modifier.padding(TeamCityDimensions.contentPadding), verticalArrangement = Arrangement.spacedBy(TeamCityDimensions.smallSpacing)) {
                val fileLabel = stringResource(state.fileLabelRes, name)
                Text(name, modifier = Modifier.clearAndSetSemantics { contentDescription = fileLabel }, style = MaterialTheme.typography.titleMedium)
                Text(stringResource(state.messageRes), style = MaterialTheme.typography.bodyMedium)
                if (state is ArtifactDownloadState.Downloading) {
                    val total = state.total
                    if (total != null && total > 0) {
                        LinearProgressIndicator(progress = { (state.received.toDouble() / total).toFloat().coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth().testTag("artifacts:download-progress"))
                    } else {
                        TeamCityLinearLoadingIndicator(Modifier.testTag("artifacts:download-progress"))
                    }
                    if (state.received > 0) Text(stringResource(Res.string.artifacts_bytes_received, state.received), style = MaterialTheme.typography.bodySmall)
                    Text(stringResource(Res.string.artifacts_keep_screen_open), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(TeamCityDimensions.smallSpacing)) {
                    if (state is ArtifactDownloadState.Failed) TextButton(onRetry) { Text(stringResource(Res.string.artifacts_retry_download)) }
                    if (state is ArtifactDownloadState.Ready) {
                        TextButton(onOpen, enabled = !state.exportRequested, modifier = Modifier.testTag("artifacts:open-downloaded")) {
                            Text(stringResource(if (state.exportFailed) Res.string.artifacts_retry_export else Res.string.artifacts_open_downloaded))
                        }
                    }
                    TextButton(onDismiss) { Text(stringResource(state.dismissLabelRes)) }
                }
            }
        }
    }
}

@Preview
@Composable
private fun DownloadCardPreview() {
    TeamCityTheme { ArtifactDownloadCard(ArtifactDownloadState.Downloading(ArtifactDownload("app-debug.apk", "/content")), {}, {}) }
}
