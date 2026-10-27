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

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import platform.Foundation.NSUserDefaults

class IosThemePreferences(private val key: String) : ThemePreferencesRepository {
    private val defaults = NSUserDefaults.standardUserDefaults
    override val theme = MutableStateFlow(ThemeMode.fromStoredValue(defaults.stringForKey(key)) ?: ThemeMode.System)
    override suspend fun setTheme(mode: ThemeMode) {
        withContext(Dispatchers.Default) { defaults.setObject(mode.storedValue, key) }
        theme.value = mode
    }
}
