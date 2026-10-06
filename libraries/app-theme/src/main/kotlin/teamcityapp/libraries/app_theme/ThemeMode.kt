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

package teamcityapp.libraries.app_theme

/** Values deliberately retain the strings used by the existing theme preference. */
enum class ThemeMode(val storedValue: String) {
    Light("Light"),
    Dark("Dark"),
    AutoBattery("Auto-battery"),
    System("System");

    companion object {
        fun fromStoredValue(value: String?): ThemeMode? = entries.firstOrNull { it.storedValue == value }
    }
}

data class ThemeOptions(val modes: List<ThemeMode>, val default: ThemeMode) {
    companion object {
        fun forSdk(sdk: Int): ThemeOptions = when {
            sdk >= 29 -> ThemeOptions(listOf(ThemeMode.Light, ThemeMode.Dark, ThemeMode.System), ThemeMode.System)
            sdk == 28 -> ThemeOptions(listOf(ThemeMode.Light, ThemeMode.Dark, ThemeMode.AutoBattery), ThemeMode.AutoBattery)
            else -> ThemeOptions(listOf(ThemeMode.Light, ThemeMode.Dark), ThemeMode.Light)
        }
    }
}
