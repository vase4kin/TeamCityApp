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

package teamcityapp.features.settings.view

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import dagger.android.support.AndroidSupportInjection
import teamcityapp.features.settings.R
import teamcityapp.features.settings.tracker.SettingsTracker
import teamcityapp.libraries.utils.applyThemeFromSettings
import javax.inject.Inject

class SettingsFragment : PreferenceFragmentCompat(),
    SharedPreferences.OnSharedPreferenceChangeListener {

    @Inject
    lateinit var tracker: SettingsTracker

    override fun onResume() {
        super.onResume()
        preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.theme_preferences, rootKey)
    }

    override fun onSharedPreferenceChanged(
        sharedPreferences: SharedPreferences?,
        key: String?
    ) {
        val themePreferenceKey = getString(R.string.key_preference_theme)
        if (key == themePreferenceKey) {
            requireContext().applyThemeFromSettings(
                onLightThemeSet = {
                    tracker.trackLightThemeSet()
                },
                onDarkThemeSet = {
                    tracker.trackDarkThemeSet()
                },
                onAutoBatteryThemeSet = {
                    tracker.trackAutoBatteryThemeSet()
                },
                onSystemThemeSet = {
                    tracker.trackSystemThemeSet()
                }
            )
        }
    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }
}
