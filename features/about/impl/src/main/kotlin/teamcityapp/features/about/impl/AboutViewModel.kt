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

package teamcityapp.features.about.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import teamcityapp.features.about.api.AboutRepository
import javax.inject.Inject

@HiltViewModel
class AboutViewModel @Inject constructor(repository: AboutRepository) : ViewModel() {
    private var completedState: AboutUiState.Content? = null

    val state = flow<AboutUiState> {
        val completed = completedState
        if (completed != null) {
            emit(completed)
        } else {
            emit(AboutUiState.Content(ServerDetailsUiState.Available(repository.serverInfo().toUiModel())))
        }
    }
        .onStart { if (completedState == null) emit(AboutUiState.Loading) }
        .catch { emit(AboutUiState.Content(ServerDetailsUiState.Unavailable)) }
        .onEach { if (it is AboutUiState.Content) completedState = it }
        .stateIn(
            scope = viewModelScope,
            // Cancel a pending request as soon as the screen stops collecting.
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 0),
            initialValue = AboutUiState.Loading,
        )
}
