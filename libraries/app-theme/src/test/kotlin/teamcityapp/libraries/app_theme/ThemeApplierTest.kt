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

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class ThemeApplierTest {
    @Test fun everyChoiceKeepsItsLegacyAppCompatMode() {
        val original = AppCompatDelegate.getDefaultNightMode()
        try {
            val applier = ThemePreferencesModule.applier()
            mapOf(ThemeMode.Light to AppCompatDelegate.MODE_NIGHT_NO,
                ThemeMode.Dark to AppCompatDelegate.MODE_NIGHT_YES,
                ThemeMode.AutoBattery to AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY,
                ThemeMode.System to AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM).forEach { (choice, expected) ->
                applier.apply(choice)
                assertEquals(expected, AppCompatDelegate.getDefaultNightMode())
            }
        } finally { AppCompatDelegate.setDefaultNightMode(original) }
    }
}
