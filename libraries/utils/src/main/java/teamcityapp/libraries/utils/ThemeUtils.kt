/*
 * Copyright 2020 Andrey Tolpeev
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

package teamcityapp.libraries.utils

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager

fun Context.applyThemeFromSettings(
    onLightThemeSet: () -> Unit = {},
    onDarkThemeSet: () -> Unit = {},
    onAutoBatteryThemeSet: () -> Unit = {},
    onSystemThemeSet: () -> Unit = {}
) {
    val themePreferenceKey = getString(R.string.key_preference_theme)
    val selectedOption = PreferenceManager.getDefaultSharedPreferences(this)
        .getString(themePreferenceKey, "")

    when (selectedOption) {
        getString(R.string.value_light_theme) -> {
            onLightThemeSet()
            setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
        getString(R.string.value_dark_theme) -> {
            onDarkThemeSet()
            setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
        getString(R.string.value_auto_battery) -> {
            onAutoBatteryThemeSet()
            setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY)
        }
        getString(R.string.value_follow_system) -> {
            onSystemThemeSet()
            setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }
}

private fun setDefaultNightMode(mode: Int) {
    AppCompatDelegate.setDefaultNightMode(mode)
}
