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
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.Json
import teamcityapp.features.artifacts.api.*
import teamcityapp.features.artifacts.impl.tracker.ArtifactsTracker
import teamcityapp.libraries.builds.BuildLaunchData
import teamcityapp.libraries.list_state.RefreshableListLoader

open class ArtifactsViewModel constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: ArtifactsRepository,
    private val tracker: ArtifactsTracker
) : ViewModel() {
    val build = Json.decodeFromString<BuildLaunchData>(requireNotNull(savedStateHandle.get<String>(JSON_BUILD)))
    private val url = savedStateHandle.get<String>(ArtifactsViewModel.URL).orEmpty()
    private val title = savedStateHandle.get<String>(ArtifactsViewModel.NAME).orEmpty()
    private val loader = RefreshableListLoader(flowOf(url)) { path, force -> repository.entries(path, force) }
    private data class Transfer(val id: Long, val state: ArtifactDownloadState)
    private val download = MutableStateFlow(Transfer(0, ArtifactDownloadState.Idle))
    private var downloadJob: Job? = null
    private var transferGeneration = 0L
    private var nextToken = 0L
    private var lastClaimedToken = 0L
    val state = combine(loader.state, download) { list, file -> ArtifactsUiState(title, list, file.state) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(0), ArtifactsUiState(title))
    fun refresh() = loader.refresh()
    fun retry() = loader.retry()
    fun onResumed() = tracker.viewShown()

    /** Explicit transfers survive recreation; the UI cancels them on navigation/backgrounding. */
    fun download(file: ArtifactDownload) {
        if (downloadJob?.isActive == true) return
        val generation = ++transferGeneration
        download.value = Transfer(generation, ArtifactDownloadState.Downloading(file))
        downloadJob = viewModelScope.launch {
            try {
                val result = repository.download(file) { received, total ->
                    download.update { current -> if (current.id == generation && current.state is ArtifactDownloadState.Downloading) current.copy(state = ArtifactDownloadState.Downloading(file, received, total)) else current }
                }
                currentCoroutineContext().ensureActive()
                download.update { current -> if (current.id == generation) current.copy(state = ArtifactDownloadState.Ready(result, ++nextToken, file.name)) else current }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                currentCoroutineContext().ensureActive()
                download.update { current -> if (current.id == generation) current.copy(state = ArtifactDownloadState.Failed(file, ++nextToken)) else current }
            }
        }
    }
    fun retryDownload() {
        (download.value.state as? ArtifactDownloadState.Failed)?.let { download(it.file) }
    }
    fun cancelUnfinishedDownload() {
        if (download.value.state is ArtifactDownloadState.Downloading) cancelDownload()
    }
    fun cancelDownload() {
        transferGeneration++
        downloadJob?.cancel()
        downloadJob = null
        download.value = Transfer(transferGeneration, ArtifactDownloadState.Idle)
    }

    /** A replayed state after recreation must not launch a file viewer twice. */
    fun claimDownloadedFile(token: Long): DownloadedArtifact? {
        val ready = download.value.state as? ArtifactDownloadState.Ready ?: return null
        if (!ready.exportRequested || ready.token != token || token <= lastClaimedToken) return null
        lastClaimedToken = token
        download.value = Transfer(transferGeneration, ready.copy(exportRequested = false))
        return ready.file
    }

    /** A user action creates a new one-shot request over the existing completed bytes. */
    fun openDownloadedFile() {
        val ready = download.value.state as? ArtifactDownloadState.Ready ?: return
        if (ready.exportRequested) return
        download.value = Transfer(transferGeneration, ready.copy(token = ++nextToken, exportRequested = true, exportFailed = false))
    }

    fun exportFailed(token: Long) {
        val ready = download.value.state as? ArtifactDownloadState.Ready ?: return
        if (ready.token == token && !ready.exportRequested) download.value = Transfer(transferGeneration, ready.copy(exportFailed = true))
    }

    companion object {
        const val URL = "url"
        const val NAME = "name"

        const val JSON_BUILD = "artifacts.build.json"
    }
}
