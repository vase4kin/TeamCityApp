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

package teamcityapp.features.run_build.impl

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import teamcityapp.features.run_build.api.*
import teamcityapp.libraries.theme.*

data class ParameterDialogState(val name: String = "", val value: String = "", val invalid: Boolean = false)

@Composable
fun RunBuildScreen(state: RunBuildUiState, onChange: (BuildRequest) -> Unit, onQueue: () -> Unit, onClose: () -> Unit, onSelectAgent: () -> Unit, onAddParameter: () -> Unit, onClearParameters: () -> Unit, agentDialog: Boolean = false, parameterDialog: ParameterDialogState? = null, onDismissDialog: () -> Unit = {}, onAgentSelected: (BuildAgent) -> Unit = {}, onParameterChange: (ParameterDialogState) -> Unit = {}, onConfirmParameter: () -> Unit = {}) {
    val request = state.request
    TeamCityScreen(stringResource(R.string.title_run_build), onClose, appBarHeight = 56.dp, scrollToolbarWithContent = true, containerColor = MaterialTheme.colorScheme.surface) { modifier ->
        Box(modifier) {
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).testTag("run-build:scroll")) {
                BranchField(state.branches, state.branchesFailed, request.branch, { onChange(request.copy(branch = it)) }, stringResource(R.string.text_build_branch), stringResource(R.string.text_loading_branches), stringResource(R.string.text_no_branches_available), stringResource(R.string.hint_default_build_branch), enabled = !state.queuing)
                HorizontalDivider()
                Column(Modifier.fillMaxWidth().clickable(enabled = !state.queuing && !state.agents.isNullOrEmpty(), onClick = onSelectAgent).padding(horizontal = 16.dp, vertical = 12.dp).testTag("run-build:agent")) {
                    Text(stringResource(R.string.text_agents), Modifier.padding(top = 4.dp), style = MaterialTheme.typography.bodyLarge)
                    if (state.agents == null) {
                        Row(Modifier.padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(stringResource(R.string.text_loading_agents), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            CircularProgressIndicator(Modifier.padding(start = 16.dp).size(20.dp), strokeWidth = 2.dp)
                        }
                    } else {
                        Text(if (state.agents.isEmpty()) stringResource(R.string.text_no_agents_available) else request.agent?.name ?: stringResource(R.string.hint_default_filter_agent), Modifier.padding(top = 4.dp), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                HorizontalDivider()
                TeamCitySwitch(stringResource(R.string.text_switcher_run_as_personal), request.personal, { onChange(request.copy(personal = it)) }, Modifier.padding(horizontal = 16.dp, vertical = 12.dp).testTag("run-build:personal"), !state.queuing)
                HorizontalDivider()
                TeamCitySwitch(stringResource(R.string.text_switcher_run_as_queue_at_the_top), request.queueAtTop, { onChange(request.copy(queueAtTop = it)) }, Modifier.padding(horizontal = 16.dp, vertical = 12.dp).testTag("run-build:top"), !state.queuing)
                HorizontalDivider()
                TeamCitySwitch(stringResource(R.string.text_switcher_run_as_clean_all_files), request.cleanSources, { onChange(request.copy(cleanSources = it)) }, Modifier.padding(horizontal = 16.dp, vertical = 12.dp).testTag("run-build:clean"), !state.queuing)
                HorizontalDivider()
                Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Text(stringResource(R.string.text_parameters), Modifier.padding(top = 4.dp), style = MaterialTheme.typography.bodyLarge)
                    if (request.parameters.isEmpty()) Text(stringResource(R.string.text_filters_none), Modifier.padding(top = 4.dp), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    request.parameters.forEach { parameter ->
                        Row(Modifier.fillMaxWidth().padding(top = 4.dp)) {
                            Text(parameter.name, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                            Text(parameter.value, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    FlowRow(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Button(onAddParameter, enabled = !state.queuing, shape = RoundedCornerShape(4.dp), modifier = Modifier.testTag("run-build:add")) { Text(stringResource(R.string.text_add_parameter).uppercase()) }
                        OutlinedButton(onClearParameters, enabled = !state.queuing && request.parameters.isNotEmpty(), shape = RoundedCornerShape(4.dp), modifier = Modifier.testTag("run-build:clear")) { Text(stringResource(R.string.text_clear_parameters).uppercase()) }
                    }
                }
                HorizontalDivider()
                Spacer(Modifier.fillMaxWidth().height(96.dp).background(MaterialTheme.colorScheme.background))
            }
            ExtendedFloatingActionButton(onQueue, Modifier.align(Alignment.BottomCenter).padding(bottom = 40.dp).testTag("run-build:submit"), shape = RoundedCornerShape(28.dp), containerColor = MaterialTheme.colorScheme.secondary, contentColor = MaterialTheme.colorScheme.onSecondary) {
                Icon(painterResource(R.drawable.ic_directions_run_white_24px), null)
                Spacer(Modifier.width(12.dp))
                Text(stringResource(R.string.title_run_build).uppercase())
            }
            if (state.queueError != null) Snackbar(Modifier.align(Alignment.BottomCenter).padding(start = 16.dp, end = 16.dp, bottom = 104.dp).testTag("run-build:error")) { Text(stringResource(if (state.queueError == QueueBuildResult.Forbidden) R.string.error_forbidden_error else R.string.error_base_error)) }
        }
    }
    if (state.queuing) {
        AlertDialog(onDismissRequest = {}, modifier = Modifier.testTag("run-build:progress"), shape = RoundedCornerShape(4.dp), text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(Modifier.size(48.dp))
                Text(stringResource(R.string.text_queueing_build), Modifier.padding(start = 24.dp))
            }
        }, confirmButton = {})
    }
    if (agentDialog) AlertDialog(onDismissRequest = onDismissDialog, modifier = Modifier.testTag("run-build:agent-dialog"), shape = RoundedCornerShape(4.dp), title = { Text(stringResource(R.string.title_agent_chooser_dialog)) }, text = { Column(Modifier.verticalScroll(rememberScrollState())) { state.agents.orEmpty().forEach { agent -> Text(agent.name, Modifier.fillMaxWidth().clickable { onAgentSelected(agent) }.padding(vertical = 16.dp), style = MaterialTheme.typography.bodyLarge) } } }, confirmButton = {})
    parameterDialog?.let { dialog ->
        Dialog(onDismissRequest = onDismissDialog, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            Surface(Modifier.widthIn(max = 560.dp).fillMaxWidth().padding(horizontal = 24.dp).testTag("run-build:parameter-dialog"), shape = RoundedCornerShape(4.dp), color = MaterialTheme.colorScheme.surface) {
                Column(Modifier.padding(24.dp)) {
                    Text(stringResource(R.string.title_add_parameter), style = MaterialTheme.typography.headlineSmall)
                    Column(Modifier.fillMaxWidth().padding(top = 16.dp).heightIn(max = 360.dp).verticalScroll(rememberScrollState())) {
                        OutlinedTextField(
                            dialog.name,
                            { onParameterChange(dialog.copy(name = it, invalid = false)) },
                            Modifier.fillMaxWidth().testTag("parameter:name"),
                            label = { Text(stringResource(R.string.hint_parameter_name)) },
                            singleLine = true,
                            isError = dialog.invalid,
                            supportingText = if (dialog.invalid) {
                                { Text(stringResource(R.string.text_error_parameter_name)) }
                            } else {
                                null
                            },
                            shape = RoundedCornerShape(4.dp)
                        )
                        OutlinedTextField(dialog.value, { onParameterChange(dialog.copy(value = it)) }, Modifier.fillMaxWidth().testTag("parameter:value"), label = { Text(stringResource(R.string.hint_parameter_value)) }, singleLine = true, shape = RoundedCornerShape(4.dp))
                    }
                    Row(Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.End) {
                        TextButton(onDismissDialog) { Text(stringResource(R.string.text_cancel_button).uppercase()) }
                        TextButton(onConfirmParameter, Modifier.testTag("parameter:confirm")) { Text(stringResource(R.string.text_add_parameter_button).uppercase()) }
                    }
                }
            }
        }
    }
}

@Preview @Composable
private fun RunPreview() {
    TeamCityTheme { RunBuildScreen(RunBuildUiState(), {}, {}, {}, {}, {}, {}) }
}
