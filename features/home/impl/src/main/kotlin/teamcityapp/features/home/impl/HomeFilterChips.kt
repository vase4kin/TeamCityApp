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

package teamcityapp.features.home.impl

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import teamcityapp.features.filter_bottom_sheet.api.QuickFilter
import teamcityapp.features.home.api.HomeTab
import teamcityapp.libraries.theme.TeamCityDimensions
import teamcityapp.libraries.theme.TeamCitySelectionChip
import teamcityapp.libraries.theme.TeamCityTheme

@Composable
internal fun HomeFilterChips(
    options: List<HomeFilterOption>,
    enabled: Boolean,
    onSelected: (QuickFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    FlowRow(modifier.selectableGroup(), horizontalArrangement = Arrangement.spacedBy(TeamCityDimensions.smallSpacing)) {
        options.forEach { option ->
            TeamCitySelectionChip(
                selected = option.selected,
                enabled = enabled,
                onClick = { onSelected(option.filter) },
                label = stringResource(option.labelRes),
                iconRes = when (option.filter) {
                    QuickFilter.RunningAll, QuickFilter.QueuedAll -> R.drawable.ic_filter_layers
                    QuickFilter.RunningFavorites, QuickFilter.QueuedFavorites -> R.drawable.ic_filter_star
                    QuickFilter.AgentsConnected -> R.drawable.ic_filter_plug
                    QuickFilter.AgentsDisconnected -> R.drawable.ic_filter_unplug
                },
                modifier = Modifier.testTag("home:filter:${option.filter.name}")
            )
        }
    }
}

@Preview
@Composable
private fun HomeFilterChipsPreview() {
    TeamCityTheme {
        HomeFilterChips(HomeUiState(selected = HomeTab.Running).filterOptions.map { it.copy(selected = it.filter == QuickFilter.RunningFavorites) }, true, {})
    }
}
