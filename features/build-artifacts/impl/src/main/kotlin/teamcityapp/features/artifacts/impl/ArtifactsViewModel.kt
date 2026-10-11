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

package teamcityapp.features.artifacts.impl

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.Serializable
import javax.inject.Inject
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import teamcityapp.features.artifacts.api.*
import teamcityapp.features.artifacts.impl.tracker.ArtifactsTracker
import teamcityapp.libraries.builds.compatibility.BuildLaunchCodec
import teamcityapp.libraries.list_state.RefreshableListLoader

@HiltViewModel
class ArtifactsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: ArtifactsRepository,
    codec: BuildLaunchCodec,
    private val tracker: ArtifactsTracker
) : ViewModel() {
    val build = codec.decode(requireNotNull(savedStateHandle.get<Serializable>(ArtifactsNavigation.BUILD)))
    private val url = savedStateHandle.get<String>(ArtifactsNavigation.URL).orEmpty()
    private val title = savedStateHandle.get<String>(ArtifactsNavigation.NAME).orEmpty()
    private val loader = RefreshableListLoader(flowOf(url)) { path, force -> repository.entries(path, force) }
    private val download = MutableStateFlow<ArtifactDownloadState>(ArtifactDownloadState.Idle)
    private var downloadJob: Job? = null
    private var nextToken = 0L
    private var lastClaimedToken = 0L
    private var lastFailureToken = 0L
    val state = combine(loader.state, download) { list, file -> ArtifactsUiState(title, list, file) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(0), ArtifactsUiState(title))
    fun refresh() = loader.refresh()
    fun retry() = loader.retry()
    fun onResumed() = tracker.viewShown()

    /** Explicit user downloads survive configuration changes, and cancel with the retained VM. */
    fun download(file: ArtifactDownload) {
        if (downloadJob?.isActive == true) return
        downloadJob = viewModelScope.launch {
            download.value = ArtifactDownloadState.Downloading(file)
            try {
                val result = repository.download(file)
                currentCoroutineContext().ensureActive()
                download.value = ArtifactDownloadState.Ready(result, ++nextToken)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                currentCoroutineContext().ensureActive()
                download.value = ArtifactDownloadState.Failed(file, ++nextToken)
            }
        }
    }
    fun retryDownload() {
        (download.value as? ArtifactDownloadState.Failed)?.let { download(it.file) }
    }
    fun claimDownloadFailure(token: Long): Boolean {
        val failed = download.value as? ArtifactDownloadState.Failed ?: return false
        if (failed.token != token || token <= lastFailureToken) return false
        lastFailureToken = token
        return true
    }
    fun cancelDownload() {
        downloadJob?.cancel()
        download.value = ArtifactDownloadState.Idle
    }

    /** A replayed state after recreation must not launch a file viewer twice. */
    fun claimDownloadedFile(token: Long): DownloadedArtifact? {
        val ready = download.value as? ArtifactDownloadState.Ready ?: return null
        if (ready.token != token || token <= lastClaimedToken) return null
        lastClaimedToken = token
        download.value = ArtifactDownloadState.Idle
        return ready.file
    }
}
