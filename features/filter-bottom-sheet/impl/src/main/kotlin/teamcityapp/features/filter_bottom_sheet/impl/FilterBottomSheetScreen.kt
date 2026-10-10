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

package teamcityapp.features.filter_bottom_sheet.impl

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.*
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import teamcityapp.features.filter_bottom_sheet.api.QuickFilter
import teamcityapp.libraries.theme.*

@Composable
fun FilterBottomSheetScreen(state: FilterBottomSheetUiState, onApply: () -> Unit) {
    val filter = state.filter
    val title = when (filter) {
        QuickFilter.RunningAll, QuickFilter.RunningFavorites -> R.string.title_filter_running_builds
        QuickFilter.QueuedAll, QuickFilter.QueuedFavorites -> R.string.title_filter_queued_builds
        else -> R.string.title_filter_agents
    }
    val description = when (filter) {
        QuickFilter.RunningAll, QuickFilter.QueuedAll -> R.string.text_show_favorites
        QuickFilter.RunningFavorites -> R.string.text_show_running
        QuickFilter.QueuedFavorites -> R.string.text_show_queued
        QuickFilter.AgentsConnected -> R.string.text_show_disconnected
        QuickFilter.AgentsDisconnected -> R.string.text_show_connected
    }
    val selected = when (filter) {
        QuickFilter.RunningAll, QuickFilter.QueuedAll -> R.string.selected_all
        QuickFilter.RunningFavorites, QuickFilter.QueuedFavorites -> R.string.selected_favorites
        QuickFilter.AgentsConnected -> R.string.selected_connected
        QuickFilter.AgentsDisconnected -> R.string.selected_disconnected
    }
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
        Surface(Modifier.widthIn(max = TeamCityDimensions.paneMaxWidth).fillMaxWidth(), color = MaterialTheme.colorScheme.surfaceContainerLow, shape = androidx.compose.foundation.shape.RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)) {
            Column(Modifier.fillMaxWidth().navigationBarsPadding().selectableGroup().testTag("quick-filter:content")) {
                TeamCitySheetHeader(stringResource(title))
                Surface(Modifier.fillMaxWidth().padding(horizontal = 16.dp), shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.primaryContainer) {
                    Row(Modifier.padding(16.dp).testTag("quick-filter:selected").semantics { this.selected = true }, verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = true, onClick = null)
                        Text(stringResource(selected), Modifier.padding(start = 12.dp), style = MaterialTheme.typography.titleMedium)
                    }
                }
                Row(Modifier.fillMaxWidth().heightIn(min = 64.dp).selectable(selected = false, enabled = !state.applying, role = Role.RadioButton, onClick = onApply).padding(start = 32.dp, end = 32.dp).testTag("quick-filter:apply"), verticalAlignment = Alignment.CenterVertically) {
                    if (state.applying) CircularProgressIndicator(Modifier.size(24.dp)) else RadioButton(selected = false, onClick = null)
                    Text(stringResource(description), Modifier.padding(start = 12.dp), style = MaterialTheme.typography.bodyLarge)
                }
                if (state.failed) Text(stringResource(teamcityapp.libraries.resources.R.string.error_view_error_text), Modifier.padding(16.dp), color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Preview @Composable
private fun FilterPreview() {
    TeamCityTheme { FilterBottomSheetScreen(FilterBottomSheetUiState(), {}) }
}
