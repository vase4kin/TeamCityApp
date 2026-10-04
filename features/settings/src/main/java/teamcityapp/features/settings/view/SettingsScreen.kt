package teamcityapp.features.settings.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import teamcityapp.features.settings.R
import teamcityapp.features.settings.viewmodel.SettingsUiState
import teamcityapp.libraries.settings.ThemeMode
import teamcityapp.libraries.theme.*

@Composable
fun SettingsScreen(
    state: SettingsUiState,
    availableThemes: List<ThemeMode>,
    onSelectTheme: (ThemeMode) -> Unit,
    onRetry: () -> Unit,
    onClose: () -> Unit
) {
    TeamCityScreen(stringResource(R.string.drawer_item_settings), onClose) { modifier ->
        when (state) {
            SettingsUiState.Loading -> LoadingContent(modifier)
            SettingsUiState.LoadError -> MessageContent(stringResource(R.string.settings_load_error), modifier) {
                Button(onClick = onRetry) { Text(stringResource(R.string.error_view_retry_button_text)) }
            }
            is SettingsUiState.Content -> Column(modifier.padding(16.dp).selectableGroup()) {
                Text(stringResource(R.string.title_general), style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(16.dp))
                Text(stringResource(R.string.title_theme), style = MaterialTheme.typography.titleLarge)
                availableThemes.forEach { theme ->
                    Row(
                        Modifier.fillMaxWidth().heightIn(min = 56.dp).selectable(
                            selected = state.theme == theme,
                            enabled = !state.saving,
                            role = Role.RadioButton,
                            onClick = { onSelectTheme(theme) }
                        ),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        RadioButton(selected = state.theme == theme, onClick = null, enabled = !state.saving)
                        Spacer(Modifier.width(12.dp))
                        Text(stringResource(theme.labelResource()))
                    }
                }
                if (state.saveError) Text(stringResource(R.string.settings_save_error), color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

private fun ThemeMode.labelResource(): Int = when (this) {
    ThemeMode.LIGHT -> R.string.name_light_theme
    ThemeMode.DARK -> R.string.name_dark_theme
    ThemeMode.AUTO_BATTERY -> R.string.name_auto_battery
    ThemeMode.SYSTEM -> R.string.name_follow_system
}

@Preview
@Composable
private fun SettingsPreview() {
    TeamCityTheme { SettingsScreen(SettingsUiState.Content(ThemeMode.SYSTEM), ThemeMode.availableFor(36), {}, {}, {}) }
}
