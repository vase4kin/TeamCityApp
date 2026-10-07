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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import teamcityapp.features.filter_builds.api.*
import teamcityapp.libraries.theme.*

@Composable
fun FilterBuildsScreen(state: FilterBuildsUiState, onChange: (BuildFilter) -> Unit, onApply: () -> Unit, onClose: () -> Unit, onChooseFilter: () -> Unit, dialog: Boolean = false, onDismissDialog: () -> Unit = {}) {
    val filter = state.filter
    val labels = stringArrayResource(R.array.build_filters)
    TeamCityScreen(stringResource(R.string.title_filter_builds), onClose, appBarHeight = 56.dp, scrollToolbarWithContent = true) { modifier ->
        Box(modifier) {
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).testTag("filter-builds:scroll")) {
                Column(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface)) {
                    BranchField(state.branches, state.branchesFailed, filter.branch, { onChange(filter.copy(branch = it)) }, stringResource(R.string.text_build_branch), stringResource(R.string.text_loading_branches), stringResource(if (state.branchesFailed) R.string.text_no_branches_available else R.string.text_no_branches_available_to_filter), stringResource(R.string.hint_default_filter_branch), filter = true)
                    HorizontalDivider()
                    Column(Modifier.fillMaxWidth().clickable(onClick = onChooseFilter).padding(horizontal = 16.dp, vertical = 12.dp).testTag("filter-builds:chooser")) {
                        Text(stringResource(R.string.text_filters), style = MaterialTheme.typography.bodyLarge)
                        Text(if (filter.status == BuildStatusFilter.None) stringResource(R.string.text_filters_none) else labels[filter.status.ordinal], style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    HorizontalDivider()
                    TeamCitySwitch(stringResource(R.string.text_switcher_for_personal), filter.personal, { onChange(filter.copy(personal = it)) }, Modifier.testTag("filter-builds:personal"), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp))
                    HorizontalDivider()
                    if (filter.status != BuildStatusFilter.Queued) {
                        TeamCitySwitch(stringResource(R.string.text_switcher_for_pinned), filter.pinned, { onChange(filter.copy(pinned = it)) }, Modifier.testTag("filter-builds:pinned"), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp))
                        HorizontalDivider()
                    }
                }
                Spacer(Modifier.height(96.dp))
            }
            TeamCityExtendedFloatingActionButton(onApply, Modifier.align(Alignment.BottomCenter).padding(bottom = 40.dp).testTag("filter-builds:apply")) {
                Icon(painterResource(R.drawable.ic_done_24px), null)
                Spacer(Modifier.width(12.dp))
                Text(stringResource(R.string.text_apply_filters_button).uppercase())
            }
        }
    }
    if (dialog) {
        AlertDialog(onDismissRequest = onDismissDialog, modifier = Modifier.testTag("filter-builds:dialog"), shape = RoundedCornerShape(4.dp), title = { Text(stringResource(R.string.title_filter_chooser_dialog)) }, text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                BuildStatusFilter.entries.filter { it != BuildStatusFilter.None }.forEach { status ->
                    Text(
                        labels[status.ordinal],
                        Modifier.fillMaxWidth().clickable {
                            onChange(filter.copy(status = status))
                            onDismissDialog()
                        }.padding(vertical = 16.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }, confirmButton = {})
    }
}

@Preview @Composable
private fun FilterPreview() {
    TeamCityTheme { FilterBuildsScreen(FilterBuildsUiState(), {}, {}, {}, {}) }
}
