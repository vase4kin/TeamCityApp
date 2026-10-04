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
import androidx.compose.runtime.CompositionLocalProvider
import teamcityapp.libraries.app_theme.ThemeMode
import teamcityapp.libraries.theme.LoadingContent
import teamcityapp.libraries.theme.MessageContent
import teamcityapp.libraries.theme.ScreenNavigation
import teamcityapp.libraries.theme.TeamCityScreen
import teamcityapp.libraries.theme.TeamCityDimensions
import teamcityapp.libraries.theme.TeamCityTheme
import teamcityapp.libraries.resources.R as SharedR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    state: SettingsUiState, onSelect: (ThemeMode) -> Unit, onRetry: () -> Unit,
    onRetrySave: () -> Unit, onClose: () -> Unit, dialogOpen: Boolean = false,
    onOpenDialog: () -> Unit = {}, onDismissDialog: () -> Unit = {},
) {
    TeamCityScreen(title = stringResource(SharedR.string.drawer_item_settings), onClose = onClose, navigation = ScreenNavigation.Back,
    ) { modifier ->
        when (state) {
            SettingsUiState.Loading -> LoadingContent(modifier)
            SettingsUiState.Error -> CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurface) {
                MessageContent(stringResource(R.string.theme_load_error), modifier) {
                    TextButton(onClick = onRetry) { Text(stringResource(R.string.retry_theme)) }
                }
            }
            is SettingsUiState.Content -> Column(modifier.verticalScroll(rememberScrollState())) {
                Text(stringResource(R.string.title_general), color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleSmall, modifier = Modifier.fillMaxWidth().padding(start = TeamCityDimensions.contentPadding, end = TeamCityDimensions.contentPadding, top = TeamCityDimensions.sectionSpacing, bottom = TeamCityDimensions.smallSpacing))
                Row(Modifier.fillMaxWidth().testTag("settings:theme").clickable(enabled = !state.saving, role = Role.Button, onClick = onOpenDialog)
                    .padding(horizontal = TeamCityDimensions.contentPadding, vertical = TeamCityDimensions.contentPadding), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.width(TeamCityDimensions.leadingContentWidth)) { Icon(painterResource(R.drawable.ic_brightness_4_black_24dp), null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(TeamCityDimensions.iconSize)) }
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(R.string.title_theme), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                        Text(if (state.selected in state.options) themeName(state.selected) else stringResource(R.string.theme_unavailable), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                if (state.saving) LinearProgressIndicator(Modifier.fillMaxWidth().testTag("settings:saving"))
                if (state.saveFailed) Column(Modifier.padding(horizontal = TeamCityDimensions.contentPadding).testTag("settings:save_error")) {
                    Text(stringResource(R.string.theme_save_error), color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyMedium)
                    TextButton(onClick = onRetrySave) { Text(stringResource(R.string.retry_theme)) }
                }
            }
        }
    }
    if (dialogOpen && state is SettingsUiState.Content && !state.saving) {
        ThemeDialog(state, onDismissDialog) { mode ->
            onDismissDialog()
            onSelect(mode)
        }
    }
}

@Composable
private fun ThemeDialog(state: SettingsUiState.Content, onDismiss: () -> Unit, onSelect: (ThemeMode) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("settings:dialog"),
        title = { Text(stringResource(R.string.title_theme)) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()).selectableGroup()) {
                state.options.forEach { mode ->
                    Row(
                        modifier = Modifier.fillMaxWidth().heightIn(min = TeamCityDimensions.minimumTouchTarget)
                            .selectable(selected = state.selected == mode, role = Role.RadioButton, onClick = { onSelect(mode) })
                            .padding(vertical = TeamCityDimensions.smallSpacing),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = state.selected == mode, onClick = null)
                        Text(
                            text = themeName(mode),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(start = TeamCityDimensions.contentPadding),
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel_theme).uppercase(androidx.compose.ui.platform.LocalLocale.current.platformLocale))
            }
        },
    )
}

@Composable
private fun themeName(mode: ThemeMode) = stringResource(when(mode) {
    ThemeMode.Light -> SharedR.string.name_light_theme
    ThemeMode.Dark -> SharedR.string.name_dark_theme
    ThemeMode.AutoBattery -> SharedR.string.name_auto_battery
    ThemeMode.System -> SharedR.string.name_follow_system
})

@Preview @Composable
private fun SettingsPreview() { TeamCityTheme { SettingsScreen(SettingsUiState.Content(ThemeMode.System, listOf(ThemeMode.Light, ThemeMode.Dark, ThemeMode.System)), {}, {}, {}, {}) } }
@Preview @Composable
private fun ThemeDialogPreview() { TeamCityTheme { ThemeDialog(SettingsUiState.Content(ThemeMode.Dark, listOf(ThemeMode.Light, ThemeMode.Dark, ThemeMode.System)), {}, {}) } }
