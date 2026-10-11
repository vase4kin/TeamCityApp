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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
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
import teamcityapp.features.filter_bottom_sheet.api.QuickFilter
import teamcityapp.libraries.theme.*

@Composable
fun FilterBottomSheetScreen(state: FilterBottomSheetUiState, onApply: () -> Unit) {
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
        Surface(Modifier.widthIn(max = TeamCityDimensions.paneMaxWidth).fillMaxWidth(), color = MaterialTheme.colorScheme.surfaceContainerLow, shape = androidx.compose.foundation.shape.RoundedCornerShape(topStart = TeamCityDimensions.largeCornerRadius, topEnd = TeamCityDimensions.largeCornerRadius)) {
            Column(Modifier.fillMaxWidth().navigationBarsPadding().verticalScroll(rememberScrollState()).selectableGroup().testTag("quick-filter:content")) {
                TeamCitySheetHeader(stringResource(state.titleRes))
                Surface(Modifier.fillMaxWidth().padding(horizontal = TeamCityDimensions.contentPadding), shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.primaryContainer) {
                    Row(Modifier.padding(TeamCityDimensions.contentPadding).testTag("quick-filter:selected").semantics { this.selected = true }, verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = true, onClick = null)
                        Text(stringResource(state.selectedRes), Modifier.padding(start = TeamCityDimensions.mediumSpacing), style = MaterialTheme.typography.titleMedium)
                    }
                }
                Row(Modifier.fillMaxWidth().heightIn(min = TeamCityDimensions.selectionRowMinHeight).selectable(selected = false, enabled = !state.applying, role = Role.RadioButton, onClick = onApply).padding(start = TeamCityDimensions.extraLargeSpacing, end = TeamCityDimensions.extraLargeSpacing).testTag("quick-filter:apply"), verticalAlignment = Alignment.CenterVertically) {
                    if (state.applying) CircularProgressIndicator(Modifier.size(TeamCityDimensions.iconSize)) else RadioButton(selected = false, onClick = null)
                    Text(stringResource(state.descriptionRes), Modifier.padding(start = TeamCityDimensions.mediumSpacing), style = MaterialTheme.typography.bodyLarge)
                }
                if (state.failed) ErrorNotice(stringResource(R.string.filter_apply_error), onApply, Modifier.padding(TeamCityDimensions.contentPadding).testTag("quick-filter:error"), enabled = !state.applying)
            }
        }
    }
}

@Preview @Composable
private fun FilterPreview() {
    TeamCityTheme { FilterBottomSheetScreen(FilterBottomSheetUiState(), {}) }
}
