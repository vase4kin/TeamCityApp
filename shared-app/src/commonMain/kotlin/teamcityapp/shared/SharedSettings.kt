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

package teamcityapp.shared

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.stringResource
import teamcityapp.features.about.api.*
import teamcityapp.features.about.api.navigation.AboutDestination
import teamcityapp.features.about.impl.*
import teamcityapp.features.settings.api.navigation.SettingsDestination
import teamcityapp.features.settings.impl.*
import teamcityapp.features.settings.impl.tracker.SettingsTrackerImpl
import teamcityapp.libraries.app_theme.*
import teamcityapp.libraries.navigation.*
import teamcityapp.libraries.shared_data.TeamCityAccountGraph
import teamcityapp.libraries.theme.*
import teamcityapp.shared.resources.*

internal fun sharedSettingsEntries(graph: TeamCityAccountGraph?): Set<DestinationRegistration> = setOf(
    SharedEntry(SettingsDestination::class, SettingsDestination.serializer()) { _, nav, _ ->
        val platform = LocalNativePlatform.current
        val analytics = platform.analytics
        val tracker = remember(analytics) {
            SettingsTrackerImpl(analytics)
        }
        val vm = viewModel { SettingsViewModel(platform.theme, ThemeOptions.Shared, tracker) }
        val state by vm.state.collectAsStateWithLifecycle()
        androidx.lifecycle.compose.LifecycleResumeEffect(vm) {
            vm.onScreenViewed()
            onPauseOrDispose {}
        }
        SettingsScreen(state, vm::select, vm::retry, vm::retrySave, { nav.pop() })
    },
    SharedEntry(AboutDestination::class, AboutDestination.serializer()) { _, nav, _ ->
        val platform = LocalNativePlatform.current
        val repository = remember(graph) {
            object : AboutRepository {
                override suspend fun serverInfo(): AboutServerInfo = checkNotNull(graph).serverInfo()
            }
        }
        SharedAboutRoute(repository, APP_VERSION, platform::browser, { action ->
            when (action) {
                AboutAction.Rate -> platform.rate()
                AboutAction.Libraries -> platform.openSourceLibraries()
                AboutAction.Issue -> platform.browser("https://github.com/vase4kin/TeamCityApp/issues/new/choose")
                AboutAction.Source -> platform.browser("https://github.com/vase4kin/TeamCityApp")
                AboutAction.Website -> platform.browser("https://teamcity.app")
                AboutAction.Email -> platform.browser("mailto:contact@teamcity.app?subject=Questions%20about%20TeamCityApp")
                AboutAction.Privacy -> platform.browser("https://telegra.ph/TeamCityApp-Private-Policy-09-18")
            }
        }, { nav.pop() })
    },
    // Persisted reader visits from earlier builds close back to their live About owner.
    SharedEntry(NativeLicensesDestination::class, NativeLicensesDestination.serializer()) { _, nav, entryId ->
        LaunchedEffect(entryId) { nav.closeRetiredLicenseScreens(entryId) }
    },
    SharedEntry(NativeLicenseDestination::class, NativeLicenseDestination.serializer()) { _, nav, entryId ->
        LaunchedEffect(entryId) { nav.closeRetiredLicenseScreens(entryId) }
    }
)
