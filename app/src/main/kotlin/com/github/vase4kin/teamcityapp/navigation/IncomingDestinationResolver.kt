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

package com.github.vase4kin.teamcityapp.navigation

import android.content.Intent
import androidx.navigation3.runtime.NavKey
import javax.inject.Inject
import teamcityapp.features.about.api.navigation.AboutDestination
import teamcityapp.features.settings.api.navigation.SettingsDestination
import teamcityapp.libraries.navigation.DestinationRegistry

/** The app owns installed component compatibility; feature APIs retain only destination keys. */
class IncomingDestinationResolver @Inject constructor(private val registry: DestinationRegistry) {
    fun destination(intent: Intent): NavKey? {
        intent.getStringExtra(MainActivity.EXTRA_DESTINATION)?.let { return registry.decodeDestination(it) }
        return when (intent.component?.className) {
            ABOUT_COMPONENT -> AboutDestination
            SETTINGS_COMPONENT -> SettingsDestination
            else -> null
        }
    }
    companion object {
        const val ABOUT_COMPONENT = "teamcityapp.features.about.impl.AboutActivity"
        const val SETTINGS_COMPONENT = "teamcityapp.features.settings.impl.SettingsActivity"
    }
}
