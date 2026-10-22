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

import org.jetbrains.compose.resources.StringResource
import teamcityapp.features.artifacts.api.*
import teamcityapp.features.artifacts.presentation.resources.*
import teamcityapp.libraries.list_state.ListUiState

data class ArtifactsUiState(
    val title: String = "",
    val list: ListUiState<Artifact> = ListUiState.Loading,
    val download: ArtifactDownloadState = ArtifactDownloadState.Idle
)
sealed interface ArtifactDownloadState {
    val messageRes: StringResource get() = if (this is Failed) Res.string.artifacts_download_failed else Res.string.artifacts_downloading

    val dismissLabelRes: StringResource get() = if (this is Failed) Res.string.artifacts_dismiss else Res.string.artifacts_cancel
    data object Idle : ArtifactDownloadState
    data class Downloading(val file: ArtifactDownload, val received: Long = 0, val total: Long? = null) : ArtifactDownloadState
    data class Failed(val file: ArtifactDownload, val token: Long = 0) : ArtifactDownloadState
    data class Ready(val file: DownloadedArtifact, val token: Long) : ArtifactDownloadState
}
enum class ArtifactPlatformError(val titleRes: StringResource, val messageRes: StringResource) {
    PermissionDenied(Res.string.artifacts_permission_title, Res.string.artifacts_permission_denied),
    FileUnavailable(Res.string.artifacts_open_failed_title, Res.string.artifacts_file_unavailable),
    BrowserUnavailable(Res.string.artifacts_open_failed_title, Res.string.artifacts_browser_unavailable)
}
