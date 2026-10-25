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
import teamcityapp.features.about.impl.*
import teamcityapp.features.settings.api.navigation.SettingsDestination
import teamcityapp.features.settings.impl.*
import teamcityapp.features.settings.impl.tracker.SettingsTracker
import teamcityapp.libraries.app_theme.*
import teamcityapp.libraries.navigation.*
import teamcityapp.libraries.shared_data.TeamCityAccountGraph
import teamcityapp.libraries.theme.*
import teamcityapp.shared.resources.*

internal typealias NativeAboutDestination = teamcityapp.features.about.api.navigation.AboutDestination

@Serializable internal data object NativeLicensesDestination : NavKey
internal fun nativeSettingsEntries(graph: TeamCityAccountGraph?): Set<DestinationRegistration> = setOf(
    SharedEntry(SettingsDestination::class, SettingsDestination.serializer()) { _, nav, _ ->
        val platform = LocalNativePlatform.current
        val diagnostics = platform.diagnostics
        val tracker = remember(diagnostics) {
            object : SettingsTracker {
                override fun trackView() = diagnostics.event("screen_settings")
                override fun trackLightThemeSet() = diagnostics.event("theme_set_light")
                override fun trackDarkThemeSet() = diagnostics.event("theme_set_dark")
                override fun trackAutoBatteryThemeSet() = diagnostics.event("theme_set_auto_battery")
                override fun trackSystemThemeSet() = diagnostics.event("theme_set_system")
            }
        }
        val vm = viewModel { SettingsViewModel(platform.theme, ThemeOptions.Shared, tracker) }
        val state by vm.state.collectAsStateWithLifecycle()
        androidx.lifecycle.compose.LifecycleResumeEffect(vm) {
            vm.onScreenViewed()
            onPauseOrDispose {}
        }
        SettingsScreen(state, vm::select, vm::retry, vm::retrySave, { nav.pop() })
    },
    SharedEntry(NativeAboutDestination::class, NativeAboutDestination.serializer()) { _, nav, _ ->
        val platform = LocalNativePlatform.current
        val repository = remember(graph) {
            object : AboutRepository {
                override suspend fun serverInfo(): AboutServerInfo = checkNotNull(graph).serverInfo()
            }
        }
        SharedAboutRoute(repository, APP_VERSION, platform::browser, { action ->
            when (action) {
                AboutAction.Rate -> platform.rate()
                AboutAction.Libraries -> nav.push(NativeLicensesDestination)
                AboutAction.Issue -> platform.browser("https://github.com/vase4kin/TeamCityApp/issues/new/choose")
                AboutAction.Source -> platform.browser("https://github.com/vase4kin/TeamCityApp")
                AboutAction.Website -> platform.browser("https://teamcity.app")
                AboutAction.Email -> platform.browser("mailto:contact@teamcity.app?subject=Questions%20about%20TeamCityApp")
                AboutAction.Privacy -> platform.browser("https://telegra.ph/TeamCityApp-Private-Policy-09-18")
            }
        }, { nav.pop() })
    },
    SharedEntry(NativeLicensesDestination::class, NativeLicensesDestination.serializer()) { _, nav, _ ->
        val platform = LocalNativePlatform.current
        var revision by remember { mutableIntStateOf(0) }
        val catalog by produceState<LicenseCatalogState>(LicenseCatalogState.Loading, revision) {
            value = try {
                LicenseCatalogState.Content(platform.licenses.entries())
            } catch (c: kotlinx.coroutines.CancellationException) {
                throw c
            } catch (_: Exception) {
                LicenseCatalogState.Error
            }
        }
        when (val state = catalog) {
            LicenseCatalogState.Loading -> SharedTeamCityScreen(stringResource(Res.string.platform_licenses), { nav.pop() }) { LoadingContent(it) }
            LicenseCatalogState.Error -> SharedTeamCityScreen(stringResource(Res.string.platform_licenses), { nav.pop() }) { SharedErrorContent(it, { revision++ }) }
            is LicenseCatalogState.Content -> NativeLicensesList({ nav.push(NativeLicenseDestination(it)) }, { nav.pop() }, state.entries)
        }
    },
    SharedEntry(NativeLicenseDestination::class, NativeLicenseDestination.serializer()) { destination, nav, _ ->
        val platform = LocalNativePlatform.current
        var revision by remember { mutableIntStateOf(0) }
        var entry by remember { mutableStateOf<AppLicense?>(null) }
        val state by produceState<NativeLicenseText>(NativeLicenseText.Loading, destination.id, revision) {
            value = NativeLicenseText.Loading
            value = try {
                val found = platform.licenses.entries().single { it.id == destination.id }
                entry = found
                NativeLicenseText.Content(platform.licenses.text(found.id))
            } catch (c: kotlinx.coroutines.CancellationException) {
                throw c
            } catch (_: Exception) {
                NativeLicenseText.Error
            }
        }
        NativeLicenseScreen(entry?.name ?: stringResource(Res.string.platform_licenses), state, { revision++ }, entry?.source?.let { source -> { platform.browser(source) } }, { nav.pop() })
    }
)
