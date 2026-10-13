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
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import teamcityapp.features.filter_bottom_sheet.api.QuickFilter
import teamcityapp.features.home.api.HomeTab
import teamcityapp.libraries.theme.TeamCityDimensions
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
            FilterChip(
                selected = option.selected,
                enabled = enabled,
                onClick = { if (!option.selected) onSelected(option.filter) },
                label = { Text(stringResource(option.labelRes)) },
                leadingIcon = if (option.selected) {
                    { Icon(painterResource(R.drawable.ic_check), null, Modifier.size(FilterChipDefaults.IconSize)) }
                } else {
                    null
                },
                modifier = Modifier.testTag("home:filter:${option.filter.name}").semantics { role = Role.RadioButton }
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
