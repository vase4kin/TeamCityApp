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
import teamcityapp.features.change_details.api.ChangeDetails
import teamcityapp.features.change_details.api.ChangedFile
import teamcityapp.features.change_details.api.navigation.ChangeDetailsDestination
import teamcityapp.features.manage_accounts.api.navigation.ManageAccountsDestination
import teamcityapp.features.settings.api.navigation.SettingsDestination
import teamcityapp.features.splash.api.navigation.SplashNavigationDestination
import teamcityapp.features.test_details.api.navigation.TestDetailsDestination
import teamcityapp.libraries.navigation.DestinationRegistry

/** The app owns installed component compatibility; feature APIs retain only destination keys. */
class IncomingDestinationResolver @Inject constructor(private val registry: DestinationRegistry) {
    fun destination(intent: Intent): NavKey? {
        intent.getStringExtra(MainActivity.EXTRA_DESTINATION)?.let { return registry.decodeDestination(it) }
        return when (intent.component?.className) {
            ABOUT_COMPONENT -> AboutDestination
            SETTINGS_COMPONENT -> SettingsDestination
            SPLASH_COMPONENT -> SplashNavigationDestination
            TEST_DETAILS_COMPONENT -> TestDetailsDestination(intent.getStringExtra("arg_test_url").orEmpty())
            CHANGE_DETAILS_COMPONENT -> ChangeDetailsDestination(changeDetails(intent))
            MANAGE_ACCOUNTS_COMPONENT -> ManageAccountsDestination
            else -> null
        }
    }
    private fun changeDetails(intent: Intent): ChangeDetails? {
        val id = intent.getStringExtra("change:id") ?: return null
        val names = intent.getStringArrayListExtra("change:file_names") ?: return null
        val types = intent.getStringArrayListExtra("change:file_types") ?: return null
        if (names.size != types.size) return null
        fun text(key: String) = intent.getStringExtra(key).orEmpty()
        return ChangeDetails(id, text("change:comment"), text("change:user"), text("change:date"), names.indices.map { ChangedFile(names[it], types[it]) }, text("change:revision"), text("change:web_url"))
    }

    companion object {
        const val TEST_DETAILS_COMPONENT = "teamcityapp.features.test_details.impl.TestDetailsActivity"
        const val CHANGE_DETAILS_COMPONENT = "teamcityapp.features.change_details.impl.ChangeDetailsActivity"
        const val MANAGE_ACCOUNTS_COMPONENT = "teamcityapp.features.manage_accounts.impl.ManageAccountsActivity"
        const val SPLASH_COMPONENT = "teamcityapp.features.splash.impl.SplashActivity"
        const val ABOUT_COMPONENT = "teamcityapp.features.about.impl.AboutActivity"
        const val SETTINGS_COMPONENT = "teamcityapp.features.settings.impl.SettingsActivity"
    }
}
