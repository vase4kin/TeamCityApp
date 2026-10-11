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

package teamcityapp.features.filter_builds.impl

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.*
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import teamcityapp.features.filter_builds.api.*
import teamcityapp.libraries.theme.*

@Composable
fun FilterBuildsScreen(state: FilterBuildsUiState, onChange: (BuildFilter) -> Unit, onApply: () -> Unit, onClose: () -> Unit) {
    val filter = state.filter
    val labels = stringArrayResource(R.array.build_filters)
    val scrollState = rememberScrollState()
    TeamCityScreen(stringResource(R.string.title_filter_builds), onClose, bottomBar = {
        TeamCityBottomActionSurface(scrollState.canScrollForward, Modifier.testTag("filter-builds:bottom-action")) {
            Button(onApply, Modifier.fillMaxWidth().padding(TeamCityDimensions.contentPadding).heightIn(min = TeamCityDimensions.controlMinHeight).testTag("filter-builds:apply")) {
                Icon(painterResource(R.drawable.ic_done_24px), null)
                Spacer(Modifier.width(TeamCityDimensions.mediumSpacing))
                Text(stringResource(R.string.text_apply_filters_button))
            }
        }
    }) { modifier ->
        Column(modifier) {
            Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(scrollState).testTag("filter-builds:scroll").padding(TeamCityDimensions.contentPadding), verticalArrangement = Arrangement.spacedBy(TeamCityDimensions.contentPadding)) {
                Text(stringResource(R.string.text_filters), style = MaterialTheme.typography.headlineMedium)
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
                    Column(Modifier.padding(TeamCityDimensions.contentPadding)) {
                        FlowRow(Modifier.fillMaxWidth().selectableGroup().testTag("filter-builds:chooser"), horizontalArrangement = Arrangement.spacedBy(TeamCityDimensions.smallSpacing)) {
                            BuildStatusFilter.entries.forEach { status ->
                                FilterChip(selected = filter.status == status, onClick = { onChange(filter.copy(status = status)) }, label = { Text(if (status == BuildStatusFilter.None) stringResource(R.string.text_filters_none) else labels[status.ordinal]) }, modifier = Modifier.testTag("filter-builds:status:$status").semantics { role = Role.RadioButton })
                            }
                        }
                    }
                }
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
                    BranchField(state.branches, state.branchesFailed, filter.branch, { onChange(filter.copy(branch = it)) }, stringResource(R.string.text_build_branch), stringResource(R.string.text_loading_branches), stringResource(if (state.branchesFailed) R.string.branches_unavailable else R.string.text_no_branches_available_to_filter), stringResource(R.string.hint_default_filter_branch), filter = true)
                    TeamCitySwitch(stringResource(R.string.text_switcher_for_personal), filter.personal, { onChange(filter.copy(personal = it)) }, Modifier.testTag("filter-builds:personal"), contentPadding = PaddingValues(TeamCityDimensions.contentPadding))
                    if (filter.status != BuildStatusFilter.Queued) TeamCitySwitch(stringResource(R.string.text_switcher_for_pinned), filter.pinned, { onChange(filter.copy(pinned = it)) }, Modifier.testTag("filter-builds:pinned"), contentPadding = PaddingValues(TeamCityDimensions.contentPadding))
                }
            }
        }
    }
}

@Preview @Composable
private fun FilterPreview() {
    TeamCityTheme { FilterBuildsScreen(FilterBuildsUiState(), {}, {}, {}) }
}
