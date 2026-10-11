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

package teamcityapp.features.build_details.impl

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import teamcityapp.features.build_details.api.*
import teamcityapp.libraries.builds.BuildLaunchData
import teamcityapp.libraries.theme.TeamCityTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BuildDetailsScreen(
    state: BuildDetailsUiState,
    expanded: Boolean,
    onUp: () -> Unit,
    onSelect: (BuildDetailsTab) -> Unit,
    onAction: (BuildDetailsAction) -> Unit,
    onConfirm: (Boolean) -> Unit,
    onDismissConfirmation: () -> Unit,
    snackbar: SnackbarHostState,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var menu by rememberSaveable { mutableStateOf(false) }
    BackHandler(state.busy) {}
    Scaffold(
        modifier = modifier,
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Column {
                            Text(state.title, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            if (state.subtitle.isNotEmpty()) Text(state.subtitle, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    },
                    navigationIcon = { IconButton(onClick = onUp, enabled = !state.busy) { Icon(painterResource(R.drawable.ic_details_back), stringResource(R.string.navigate_up)) } },
                    actions = {
                        Box {
                            IconButton(onClick = { menu = true }, enabled = !state.busy) { Icon(painterResource(R.drawable.ic_details_more), stringResource(R.string.action_more)) }
                            DropdownMenu(expanded = menu && !state.busy, onDismissRequest = { menu = false }) {
                                state.menu.forEach { action ->
                                    DropdownMenuItem(text = { Text(stringResource(action.label)) }, onClick = {
                                        menu = false
                                        onAction(action.action)
                                    })
                                }
                            }
                        }
                    }
                )
                val tabs: @Composable () -> Unit = {
                    state.tabs.forEach { tab ->
                        val count = state.counts[tab]
                        val label = stringResource(tab.label())
                        Tab(
                            selected = state.selected == tab,
                            onClick = { onSelect(tab) },
                            enabled = !state.busy,
                            text = { Text(if (count == null) label else stringResource(R.string.tab_with_count, label, count), maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            modifier = Modifier.testTag("details:tab:${tab.name}")
                        )
                    }
                }
                if (expanded) {
                    PrimaryTabRow(state.tabs.indexOf(state.selected), tabs = tabs)
                } else {
                    PrimaryScrollableTabRow(state.tabs.indexOf(state.selected), edgePadding = 0.dp, tabs = tabs)
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding -> Box(Modifier.fillMaxSize().padding(padding)) { content() } }
    val confirmation = state.confirmation
    if (confirmation != null) {
        // Key checkbox state to the actual confirmed snapshot, including process restoration.
        var reAdd by rememberSaveable(confirmation) { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = onDismissConfirmation,
            text = {
                Column {
                    Text(stringResource(confirmation.message))
                    if (confirmation.kind == ConfirmationKind.Stop) {
                        Row {
                            Checkbox(checked = reAdd, onCheckedChange = { reAdd = it }, modifier = Modifier.testTag("details:readd"))
                            Text(stringResource(R.string.text_re_add_build), modifier = Modifier.padding(top = 12.dp))
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { onConfirm(reAdd) }, modifier = Modifier.testTag("details:confirm")) { Text(stringResource(confirmation.confirmLabel)) } },
            dismissButton = { TextButton(onClick = onDismissConfirmation) { Text(stringResource(R.string.text_cancel_button)) } }
        )
    }
    state.operation?.let { operation ->
        AlertDialog(
            onDismissRequest = {},
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(stringResource(operation.message))
                    LinearProgressIndicator(Modifier.fillMaxWidth())
                }
            },
            confirmButton = {}
        )
    }
}

@Preview
@Composable
private fun BuildDetailsPreview() {
    TeamCityTheme {
        BuildDetailsScreen(BuildDetailsUiState(BuildLaunchData("42", "/builds/42", number = "42", state = "finished"), configurationName = "Build Android"), false, {}, {}, {}, {}, {}, remember { SnackbarHostState() }) {
            Surface(Modifier.fillMaxSize()) { Text("Build #42", Modifier.padding(16.dp)) }
        }
    }
}
