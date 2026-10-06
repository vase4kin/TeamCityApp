/*
 * Copyright 2019 Andrey Tolpeev
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

package teamcityapp.features.splash.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import teamcityapp.features.splash.api.SplashDestination
import teamcityapp.features.splash.api.SplashRepository

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SplashViewModel @Inject constructor(private val repository: SplashRepository) : ViewModel() {
    private val snapshot = MutableStateFlow<SplashUiState>(SplashUiState.Loading)
    private val refresh = MutableStateFlow(0)
    val state = channelFlow {
        launch {
            refresh.flatMapLatest {
                flow<SplashUiState> {
                    val completed = snapshot.value
                    if (completed != SplashUiState.Loading) {
                        emit(completed)
                    } else {
                        val destination = if (repository.hasAccounts()) SplashDestination.Home else SplashDestination.Login
                        emit(SplashUiState.Ready(destination))
                    }
                }.catch { error ->
                    if (error is CancellationException) throw error
                    emit(SplashUiState.Error)
                }
            }.collect { snapshot.value = it }
        }
        snapshot.collect { send(it) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis = 0), SplashUiState.Loading)

    fun retry() {
        if (!snapshot.compareAndSet(SplashUiState.Error, SplashUiState.Loading)) return
        refresh.value++
    }

    /** Claim before the platform launch so recreation and a rapid resume cannot replay it. */
    fun consumeDestination(destination: SplashDestination): Boolean = snapshot.compareAndSet(SplashUiState.Ready(destination), SplashUiState.Navigated)
}
