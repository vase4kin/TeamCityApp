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
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.*
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
    Surface(color = MaterialTheme.colorScheme.surface) {
        Column(Modifier.fillMaxWidth().testTag("quick-filter:content")) {
            TeamCitySheetHeader(stringResource(title))
            Row(Modifier.fillMaxWidth().heightIn(min = 64.dp).clickable(enabled = !state.applying, onClick = onApply).padding(horizontal = 16.dp).testTag("quick-filter:apply"), verticalAlignment = Alignment.CenterVertically) {
                if (state.applying) CircularProgressIndicator(Modifier.size(24.dp)) else Icon(painterResource(R.drawable.ic_done_24px), null, Modifier.size(24.dp))
                Text(stringResource(description), Modifier.padding(start = 16.dp), style = MaterialTheme.typography.bodyLarge)
            }
            if (state.failed) Text(stringResource(teamcityapp.libraries.resources.R.string.error_view_error_text), Modifier.padding(16.dp), color = MaterialTheme.colorScheme.error)
        }
    }
}

@Preview @Composable
private fun FilterPreview() {
    TeamCityTheme { FilterBottomSheetScreen(FilterBottomSheetUiState(), {}) }
}
