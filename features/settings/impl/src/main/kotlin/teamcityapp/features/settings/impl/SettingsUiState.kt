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

package teamcityapp.features.settings.impl

import androidx.annotation.StringRes
import teamcityapp.libraries.app_theme.ThemeMode
import teamcityapp.libraries.resources.R as SharedR

sealed interface SettingsUiState {
    data object Loading : SettingsUiState
    data object Error : SettingsUiState
    data class Content(val selected: ThemeMode, val options: List<ThemeMode>, val saving: Boolean = false, val saveFailed: Boolean = false) : SettingsUiState {
        val themeOptions: List<ThemeOptionUiState> = options.map { ThemeOptionUiState(it, themeLabel(it)) }

        @get:StringRes val selectedThemeLabelRes: Int = if (selected in options) themeLabel(selected) else R.string.theme_unavailable
    }
}

/** Resource selection is part of the ViewModel-produced state; localization stays in Compose. */
data class ThemeOptionUiState(val mode: ThemeMode, @get:StringRes val labelRes: Int)

@StringRes private fun themeLabel(mode: ThemeMode): Int = when (mode) {
    ThemeMode.Light -> SharedR.string.name_light_theme
    ThemeMode.Dark -> SharedR.string.name_dark_theme
    ThemeMode.AutoBattery -> SharedR.string.name_auto_battery
    ThemeMode.System -> SharedR.string.name_follow_system
}
