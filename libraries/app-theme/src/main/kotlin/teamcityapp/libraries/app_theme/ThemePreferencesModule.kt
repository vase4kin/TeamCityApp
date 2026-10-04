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

import android.os.Build
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object ThemePreferencesModule {
    @Provides fun options(): ThemeOptions = ThemeOptions.forSdk(Build.VERSION.SDK_INT)
    @Provides fun repository(store: ThemePreferencesStore): ThemePreferencesRepository = store.repository
    @Provides fun applier(): ThemeApplier = ThemeApplier { mode ->
        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(when(mode) {
            ThemeMode.Light -> androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
            ThemeMode.Dark -> androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
            ThemeMode.AutoBattery -> androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY
            ThemeMode.System -> androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        })
    }
}
