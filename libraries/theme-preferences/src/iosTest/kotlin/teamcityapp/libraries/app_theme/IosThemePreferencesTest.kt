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

import kotlin.test.*
import kotlinx.coroutines.test.runTest
import platform.Foundation.NSUUID
import platform.Foundation.NSUserDefaults

class IosThemePreferencesTest {
    @Test fun allSharedModesSurviveRepositoryRecreationUnderTheSuppliedKey() = runTest {
        val key = "theme.ownership-test." + NSUUID().UUIDString
        val defaults = NSUserDefaults.standardUserDefaults
        try {
            val preferences = IosThemePreferences(key)
            assertEquals(ThemeMode.System, preferences.theme.value)
            for (mode in ThemeMode.entries) {
                preferences.setTheme(mode)
                assertEquals(mode, preferences.theme.value)
                assertEquals(mode.storedValue, defaults.stringForKey(key))
                assertEquals(mode, IosThemePreferences(key).theme.value)
            }
        } finally {
            defaults.removeObjectForKey(key)
        }
    }
}
