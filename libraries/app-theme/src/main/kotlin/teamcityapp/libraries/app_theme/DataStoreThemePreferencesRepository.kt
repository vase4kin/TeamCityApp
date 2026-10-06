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

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

internal class DataStoreThemePreferencesRepository(
    private val store: DataStore<Preferences>,
    private val default: ThemeMode
) : ThemePreferencesRepository {
    override val theme = store.data.map { ThemeMode.fromStoredValue(it[THEME]) ?: default }.distinctUntilChanged()
    override suspend fun setTheme(mode: ThemeMode) {
        store.edit { it[THEME] = mode.storedValue }
    }
    companion object {
        const val LEGACY_KEY = "preference_key_theme"
        val THEME = stringPreferencesKey(LEGACY_KEY)
    }
}
