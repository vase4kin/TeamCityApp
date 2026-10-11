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

import androidx.annotation.StringRes
import teamcityapp.features.artifacts.api.*
import teamcityapp.libraries.list_state.ListUiState

data class ArtifactsUiState(
    val title: String = "",
    val list: ListUiState<Artifact> = ListUiState.Loading,
    val download: ArtifactDownloadState = ArtifactDownloadState.Idle
)
sealed interface ArtifactDownloadState {
    data object Idle : ArtifactDownloadState
    data class Downloading(val file: ArtifactDownload) : ArtifactDownloadState
    data class Failed(val file: ArtifactDownload, val token: Long = 0) : ArtifactDownloadState
    data class Ready(val file: DownloadedArtifact, val token: Long) : ArtifactDownloadState
}
enum class ArtifactPlatformError(@get:StringRes val titleRes: Int, @get:StringRes val messageRes: Int) {
    PermissionDenied(R.string.artifacts_permission_title, R.string.artifacts_permission_denied),
    FileUnavailable(R.string.artifacts_open_failed_title, R.string.artifacts_file_unavailable),
    BrowserUnavailable(R.string.artifacts_open_failed_title, R.string.artifacts_browser_unavailable)
}
