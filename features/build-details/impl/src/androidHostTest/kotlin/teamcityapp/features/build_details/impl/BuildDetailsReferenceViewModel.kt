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

package teamcityapp.features.build_details.impl

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.*
import teamcityapp.features.build_details.api.BuildDetailsRepository
import teamcityapp.features.build_details.impl.tracker.BuildDetailsTracker
import teamcityapp.libraries.builds.BuildLaunchData
import teamcityapp.libraries.list_state.*

internal sealed interface BuildDetailsReferenceState {
    data object Loading : BuildDetailsReferenceState
    data object Error : BuildDetailsReferenceState {
        val messageRes = R.string.error_opening_build
        val retryLabelRes = R.string.text_retry
    }
    data class Content(val build: BuildLaunchData) : BuildDetailsReferenceState
}

internal class BuildDetailsReferenceViewModel constructor(savedState: SavedStateHandle, repository: BuildDetailsRepository, tracker: BuildDetailsTracker) : ViewModel() {
    private val href = savedState.get<String>(HREF).orEmpty()
    private val trackRestart = savedState.get<Boolean>(TRACK_RESTART) == true
    private val loader = RefreshableListLoader(flowOf(href)) { reference, _ ->
        try {
            require(reference.isNotBlank())
            listOf(repository.queuedBuild(reference)).also {
                currentCoroutineContext().ensureActive()
                if (trackRestart) tracker.queuedOpened()
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (failure: Exception) {
            currentCoroutineContext().ensureActive()
            if (trackRestart) tracker.queuedFailed()
            throw failure
        }
    }

    // A short grace period preserves pending reads through recreation; popping clears the owner immediately.
    internal val state = loader.state.map { state ->
        when (state) {
            is ListUiState.Content -> BuildDetailsReferenceState.Content(state.items.single())
            ListUiState.Loading -> BuildDetailsReferenceState.Loading
            else -> BuildDetailsReferenceState.Error
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BuildDetailsReferenceState.Loading)
    fun retry() = loader.retry()
    companion object {
        const val HREF = "details.reference.href"
        const val TRACK_RESTART = "details.reference.restart"
    }
}
