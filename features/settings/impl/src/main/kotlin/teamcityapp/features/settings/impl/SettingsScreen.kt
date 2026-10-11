/*
 * Copyright 2020 Andrey Tolpeev
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

package teamcityapp.features.settings.impl

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import teamcityapp.libraries.app_theme.ThemeMode
import teamcityapp.libraries.resources.R as SharedR
import teamcityapp.libraries.theme.ErrorContent
import teamcityapp.libraries.theme.ErrorNotice
import teamcityapp.libraries.theme.LoadingContent
import teamcityapp.libraries.theme.ScreenNavigation
import teamcityapp.libraries.theme.TeamCityDimensions
import teamcityapp.libraries.theme.TeamCityScreen
import teamcityapp.libraries.theme.TeamCityTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    state: SettingsUiState,
    onSelect: (ThemeMode) -> Unit,
    onRetry: () -> Unit,
    onRetrySave: () -> Unit,
    onClose: () -> Unit
) {
    TeamCityScreen(title = stringResource(SharedR.string.drawer_item_settings), onClose = onClose, navigation = ScreenNavigation.Back) { modifier ->
        when (state) {
            SettingsUiState.Loading -> LoadingContent(modifier)

            SettingsUiState.Error -> ErrorContent(modifier, onRetry, message = stringResource(R.string.theme_load_error))

            is SettingsUiState.Content -> Column(modifier.verticalScroll(rememberScrollState())) {
                Text(
                    stringResource(R.string.title_general),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.fillMaxWidth().padding(start = TeamCityDimensions.contentPadding, end = TeamCityDimensions.contentPadding, top = TeamCityDimensions.sectionSpacing, bottom = TeamCityDimensions.smallSpacing)
                )
                Column(Modifier.fillMaxWidth().padding(16.dp).selectableGroup().testTag("settings:theme"), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(R.string.title_theme), style = MaterialTheme.typography.headlineMedium)
                    Text(stringResource(R.string.current_theme, if (state.selected in state.options) themeName(state.selected) else stringResource(R.string.theme_unavailable)), Modifier.testTag("settings:current"), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (state.selected !in state.options) Text(stringResource(R.string.theme_unavailable), Modifier.testTag("settings:unavailable"), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (state.saving) LinearProgressIndicator(Modifier.fillMaxWidth().testTag("settings:saving"))
                    if (state.saveFailed) {
                        ErrorNotice(stringResource(R.string.theme_save_error), onRetrySave, Modifier.testTag("settings:save_error"))
                    }
                    state.options.forEach { mode ->
                        Surface(Modifier.fillMaxWidth().testTag("settings:option:$mode").selectable(state.selected == mode, enabled = !state.saving, role = Role.RadioButton, onClick = { onSelect(mode) }), shape = MaterialTheme.shapes.large, color = if (state.selected == mode) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow) {
                            Row(Modifier.padding(20.dp).heightIn(min = 48.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(themeName(mode), style = MaterialTheme.typography.titleMedium)
                                    MaterialTheme(
                                        colorScheme = teamcityapp.libraries.theme.teamCityColorScheme(
                                            when (mode) {
                                                ThemeMode.Light -> false
                                                ThemeMode.Dark -> true
                                                else -> androidx.compose.foundation.isSystemInDarkTheme()
                                            }
                                        )
                                    ) {
                                        Row(Modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            repeat(3) { index ->
                                                Surface(
                                                    Modifier.size(if (index == 0) 32.dp else 24.dp),
                                                    shape = MaterialTheme.shapes.small,
                                                    color = when (index) {
                                                        0 -> MaterialTheme.colorScheme.primary
                                                        1 -> MaterialTheme.colorScheme.secondaryContainer
                                                        else -> MaterialTheme.colorScheme.tertiaryContainer
                                                    }
                                                ) {}
                                            }
                                        }
                                    }
                                }
                                RadioButton(state.selected == mode, onClick = null, enabled = !state.saving)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun themeName(mode: ThemeMode) = stringResource(
    when (mode) {
        ThemeMode.Light -> SharedR.string.name_light_theme
        ThemeMode.Dark -> SharedR.string.name_dark_theme
        ThemeMode.AutoBattery -> SharedR.string.name_auto_battery
        ThemeMode.System -> SharedR.string.name_follow_system
    }
)

@Preview @Composable
private fun SettingsPreview() {
    TeamCityTheme { SettingsScreen(SettingsUiState.Content(ThemeMode.System, listOf(ThemeMode.Light, ThemeMode.Dark, ThemeMode.System)), {}, {}, {}, {}) }
}
