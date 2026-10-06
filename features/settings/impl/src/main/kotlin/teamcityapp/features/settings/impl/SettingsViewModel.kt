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

package teamcityapp.features.settings.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import teamcityapp.features.settings.impl.tracker.SettingsTracker
import teamcityapp.libraries.app_theme.ThemeMode
import teamcityapp.libraries.app_theme.ThemeOptions
import teamcityapp.libraries.app_theme.ThemePreferencesRepository

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: ThemePreferencesRepository,
    private val options: ThemeOptions,
    private val tracker: SettingsTracker
) : ViewModel() {
    private data class Save(val saving: Boolean = false, val failed: Boolean = false)
    private val save = MutableStateFlow(Save())
    private val reload = MutableStateFlow(0)
    private var failedMode: ThemeMode? = null
    private var hasContent = false

    val state: StateFlow<SettingsUiState> = reload.flatMapLatest {
        repository.theme.map<ThemeMode, SettingsUiState> {
            hasContent = true
            SettingsUiState.Content(it, options.modes)
        }.onStart {
            if (!hasContent) emit(SettingsUiState.Loading)
        }.catch {
            if (it is CancellationException) throw it
            hasContent = false
            emit(SettingsUiState.Error)
        }
    }.combine(save) { content, operation ->
        if (content is SettingsUiState.Content) {
            content.copy(saving = operation.saving, saveFailed = operation.failed)
        } else {
            content
        }
    }.stateIn(
        viewModelScope,
        // Stop observing immediately in the background; reread persisted changes on return.
        SharingStarted.WhileSubscribed(stopTimeoutMillis = 0),
        SettingsUiState.Loading
    )

    fun onScreenViewed() {
        tracker.trackView()
    }

    fun retry() {
        if (state.value == SettingsUiState.Error) reload.value++
    }

    fun select(mode: ThemeMode) {
        val content = state.value as? SettingsUiState.Content ?: return
        if (save.value.saving || mode !in options.modes || content.selected == mode) return
        save.value = Save(saving = true)
        viewModelScope.launch {
            try {
                repository.setTheme(mode)
            } catch (error: CancellationException) {
                save.value = Save()
                throw error
            } catch (error: Exception) {
                failedMode = mode
                save.value = Save(failed = true)
                return@launch
            }
            failedMode = null
            save.value = Save()
            when (mode) {
                ThemeMode.Light -> tracker.trackLightThemeSet()
                ThemeMode.Dark -> tracker.trackDarkThemeSet()
                ThemeMode.AutoBattery -> tracker.trackAutoBatteryThemeSet()
                ThemeMode.System -> tracker.trackSystemThemeSet()
            }
        }
    }

    fun retrySave() {
        failedMode?.let(::select)
    }
}
