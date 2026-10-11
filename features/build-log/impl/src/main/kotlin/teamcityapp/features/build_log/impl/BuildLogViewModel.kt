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

package teamcityapp.features.build_log.impl

import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import teamcityapp.features.build_log.api.*

enum class BuildLogPage { Loading, Content, Error }
sealed interface BuildLogUiState {
    data object Loading : BuildLogUiState
    data object Error : BuildLogUiState
    data class Session(val session: BuildLogSession, val page: BuildLogPage = BuildLogPage.Loading, val attempt: Int = 0, val acknowledging: Boolean = false, val authenticationFailed: Boolean = false) : BuildLogUiState {
        @get:StringRes val messageRes: Int? = when {
            session.sslDisabled -> R.string.text_browse_build_log
            authenticationFailed -> R.string.log_authentication_error
            session.needsAuthentication -> R.string.text_login_again
            else -> null
        }

        @get:StringRes val actionLabelRes: Int? = when {
            session.sslDisabled -> R.string.text_browse_build_log_button
            authenticationFailed || session.needsAuthentication -> R.string.text_button_login
            else -> null
        }
    }
}

@HiltViewModel
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class BuildLogViewModel @Inject constructor(private val repository: BuildLogRepository, savedStateHandle: SavedStateHandle) : ViewModel() {
    private val id = savedStateHandle.get<String>("buildId").orEmpty()
    private var cached: BuildLogSession? = null
    private val refresh = MutableStateFlow(0)
    private val page = MutableStateFlow(BuildLogPage.Loading)
    private val attempt = MutableStateFlow(0)
    private enum class Acknowledgement { Idle, Loading, Error }
    private val acknowledging = MutableStateFlow(Acknowledgement.Idle)
    private val acknowledged = MutableStateFlow(false)
    private val data = refresh.flatMapLatest {
        flow<BuildLogUiState> {
            val session = cached ?: repository.session(id)
            cached = session
            emit(BuildLogUiState.Session(session))
        }.onStart { emit(cached?.let { BuildLogUiState.Session(it) } ?: BuildLogUiState.Loading) }
            .catch { error ->
                if (error is CancellationException) throw error
                emit(BuildLogUiState.Error)
            }
    }
    val state = combine(data, page, attempt, acknowledging, acknowledged) { state, page, attempt, busy, acknowledged ->
        if (state is BuildLogUiState.Session) state.copy(session = state.session.copy(needsAuthentication = state.session.needsAuthentication && !acknowledged), page = page, attempt = attempt, acknowledging = busy == Acknowledgement.Loading, authenticationFailed = busy == Acknowledgement.Error) else state
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(0), BuildLogUiState.Loading)
    fun retry() {
        if (cached == null) {
            refresh.value++
        } else {
            page.value = BuildLogPage.Loading
            attempt.value++
        }
    }
    fun pageStarted() {
        page.value = BuildLogPage.Loading
    }
    fun pageFinished() {
        if (page.value != BuildLogPage.Error) page.value = BuildLogPage.Content
    }
    fun pageFailed() {
        page.value = BuildLogPage.Error
    }
    fun authenticate() {
        if (acknowledging.value == Acknowledgement.Loading || acknowledged.value) return
        acknowledging.value = Acknowledgement.Loading
        viewModelScope.launch {
            try {
                repository.acknowledgeAuthentication()
                acknowledged.value = true
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                acknowledging.value = Acknowledgement.Error
            } finally {
                if (acknowledging.value != Acknowledgement.Error) acknowledging.value = Acknowledgement.Idle
            }
        }
    }
}
