package teamcityapp.features.test_details.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import teamcityapp.features.test_details.repository.TestDetailsRepository
import teamcityapp.features.test_details.tracker.TestDetailsTracker
import javax.inject.Inject

const val TEST_URL_KEY = "arg_test_url"

sealed interface TestDetailsUiState {
    data object Loading : TestDetailsUiState
    data object Empty : TestDetailsUiState
    data object Error : TestDetailsUiState
    data object InvalidInput : TestDetailsUiState
    data class Content(val text: String) : TestDetailsUiState
}

@HiltViewModel
class TestDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: TestDetailsRepository,
    tracker: TestDetailsTracker
) : ViewModel() {
    private val url = savedStateHandle.get<String>(TEST_URL_KEY).orEmpty()
    private val mutableState = MutableStateFlow<TestDetailsUiState>(TestDetailsUiState.Loading)
    val state = mutableState.asStateFlow()
    private var request: Job? = null

    init {
        tracker.trackView()
        load()
    }

    fun retry() = load()

    private fun load() {
        request?.cancel()
        if (url.isBlank()) {
            mutableState.value = TestDetailsUiState.InvalidInput
            return
        }
        mutableState.value = TestDetailsUiState.Loading
        request = viewModelScope.launch {
            try {
                val details = repository.details(url)
                mutableState.value = if (details.text.isEmpty()) TestDetailsUiState.Empty
                else TestDetailsUiState.Content(details.text)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                mutableState.value = TestDetailsUiState.Error
            }
        }
    }
}
