/*
 * Copyright 2019 Andrey Tolpeev
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

/* Updated for Compose in 2026. */

package teamcityapp.features.test_details.impl

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import teamcityapp.features.test_details.api.TestDetailsRepository
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TestDetailsViewModel @Inject constructor(
    savedState: SavedStateHandle,
    repository: TestDetailsRepository,
) : ViewModel() {
    private val url = savedState.get<String>(ARG_TEST_URL).orEmpty()
    private val retries = MutableStateFlow(0)
    private var completedState: TestDetailsUiState? = null

    val state = retries.flatMapLatest {
        flow {
            val completed = completedState
            emit(completed ?: if (url.isEmpty()) TestDetailsUiState.InvalidInput else {
                val details = repository.testDetails(url)
                if (details.isEmpty()) TestDetailsUiState.Empty else TestDetailsUiState.Content(details)
            })
        }
            .onStart { if (completedState == null && url.isNotEmpty()) emit(TestDetailsUiState.Loading) }
            .catch { error ->
                if (error is CancellationException) throw error
                emit(TestDetailsUiState.Error)
            }
            .onEach { if (it != TestDetailsUiState.Loading) completedState = it }
    }.stateIn(
        viewModelScope,
        // Cancel pending work immediately; keep completed content through configuration changes.
        SharingStarted.WhileSubscribed(stopTimeoutMillis = 0),
        if (url.isEmpty()) TestDetailsUiState.InvalidInput else TestDetailsUiState.Loading,
    )

    fun retry() {
        if (state.value != TestDetailsUiState.Error) return
        completedState = null
        retries.value++
    }

    companion object {
        const val ARG_TEST_URL = "arg_test_url"
    }
}
