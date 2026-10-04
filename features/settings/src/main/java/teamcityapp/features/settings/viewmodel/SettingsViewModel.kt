package teamcityapp.features.settings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import teamcityapp.features.settings.tracker.SettingsTracker
import teamcityapp.libraries.settings.SettingsRepository
import teamcityapp.libraries.settings.ThemeMode
import javax.inject.Inject

sealed interface SettingsUiState {
    data object Loading : SettingsUiState
    data object LoadError : SettingsUiState
    data class Content(val theme: ThemeMode, val saving: Boolean = false, val saveError: Boolean = false) : SettingsUiState
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: SettingsRepository,
    private val tracker: SettingsTracker
) : ViewModel() {
    val availableThemes = repository.availableThemes
    private val mutableState = MutableStateFlow<SettingsUiState>(SettingsUiState.Loading)
    val state = mutableState.asStateFlow()

    private var observation: Job? = null

    init { observeTheme() }

    fun retry() = observeTheme()

    private fun observeTheme() {
        observation?.cancel()
        mutableState.value = SettingsUiState.Loading
        observation = viewModelScope.launch {
            try {
                repository.theme.collect { theme ->
                    val previous = mutableState.value as? SettingsUiState.Content
                    mutableState.value = SettingsUiState.Content(theme, saving = previous?.saving ?: false)
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                mutableState.value = SettingsUiState.LoadError
            }
        }
    }

    fun selectTheme(theme: ThemeMode) {
        val current = mutableState.value as? SettingsUiState.Content ?: return
        if (current.saving || current.theme == theme || theme !in availableThemes) return
        mutableState.value = current.copy(saving = true, saveError = false)
        viewModelScope.launch {
            try {
                repository.setTheme(theme)
                when (theme) {
                    ThemeMode.LIGHT -> tracker.trackLightThemeSet()
                    ThemeMode.DARK -> tracker.trackDarkThemeSet()
                    ThemeMode.AUTO_BATTERY -> tracker.trackAutoBatteryThemeSet()
                    ThemeMode.SYSTEM -> tracker.trackSystemThemeSet()
                }
                mutableState.value = SettingsUiState.Content(theme)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                mutableState.update { state ->
                    (state as? SettingsUiState.Content)?.copy(saving = false, saveError = true) ?: state
                }
            }
        }
    }
}
