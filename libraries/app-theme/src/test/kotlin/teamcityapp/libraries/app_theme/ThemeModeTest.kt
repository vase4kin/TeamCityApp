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

import org.junit.Assert.*
import org.junit.Test

class ThemeModeTest {
    @Test fun olderAndroidKeepsLightAndDarkChoices() {
        for (sdk in 24..27) assertEquals(ThemeOptions(listOf(ThemeMode.Light, ThemeMode.Dark), ThemeMode.Light), ThemeOptions.forSdk(sdk))
    }

    @Test fun androidPieKeepsAutoBatteryDefault() {
        assertEquals(ThemeOptions(listOf(ThemeMode.Light, ThemeMode.Dark, ThemeMode.AutoBattery), ThemeMode.AutoBattery), ThemeOptions.forSdk(28))
    }

    @Test fun modernAndroidKeepsFollowSystemDefault() {
        for (sdk in listOf(29, 35, 37)) assertEquals(ThemeOptions(listOf(ThemeMode.Light, ThemeMode.Dark, ThemeMode.System), ThemeMode.System), ThemeOptions.forSdk(sdk))
    }

    @Test fun everyExistingSerializedValueStillResolves() {
        listOf("Light", "Dark", "Auto-battery", "System").zip(ThemeMode.entries).forEach { (value, mode) ->
            assertEquals(mode, ThemeMode.fromStoredValue(value))
            assertEquals(value, mode.storedValue)
        }
        assertNull(ThemeMode.fromStoredValue("unknown"))
        assertNull(ThemeMode.fromStoredValue(null))
        assertNull(ThemeMode.fromStoredValue(""))
    }
}
