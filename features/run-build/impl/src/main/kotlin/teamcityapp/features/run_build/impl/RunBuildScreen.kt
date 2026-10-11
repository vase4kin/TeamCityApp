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

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun RunBuildScreen(state: RunBuildUiState, onChange: (BuildRequest) -> Unit, onQueue: () -> Unit, onClose: () -> Unit, onSelectAgent: () -> Unit, onAddParameter: () -> Unit, onClearParameters: () -> Unit, agentDialog: Boolean = false, parameterDialog: ParameterDialogState? = null, onDismissDialog: () -> Unit = {}, onAgentSelected: (BuildAgent) -> Unit = {}, onParameterChange: (ParameterDialogState) -> Unit = {}, onConfirmParameter: () -> Unit = {}) {
    val request = state.request
    var optionsExpanded by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(state.queueError, parameterDialog?.invalid) {
        if (state.queueError != null || parameterDialog?.invalid == true) optionsExpanded = true
    }
    val scrollState = rememberScrollState()
    TeamCityScreen(stringResource(R.string.title_run_build), onClose, bottomBar = {
        TeamCityBottomActionSurface(scrollState.canScrollForward, Modifier.testTag("run-build:bottom-action")) {
            Button(onQueue, Modifier.fillMaxWidth().padding(16.dp).heightIn(min = 56.dp).testTag("run-build:submit"), enabled = !state.queuing) {
                Icon(painterResource(R.drawable.ic_directions_run_white_24px), null)
                Spacer(Modifier.width(12.dp))
                Text(stringResource(if (state.queuing) R.string.text_queueing_build else R.string.title_run_build))
            }
        }
    }) { modifier ->
        Column(modifier) {
            if (state.queueError != null) {
                ErrorNotice(stringResource(if (state.queueError == QueueBuildResult.Forbidden) R.string.error_forbidden_error else R.string.error_base_error), modifier = Modifier.fillMaxWidth().padding(16.dp).testTag("run-build:error"))
            }
            Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(scrollState).testTag("run-build:scroll").padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(stringResource(R.string.quick_setup), style = MaterialTheme.typography.headlineMedium)
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
                    BranchField(state.branches, state.branchesFailed, request.branch, { onChange(request.copy(branch = it)) }, stringResource(R.string.text_build_branch), stringResource(R.string.text_loading_branches), stringResource(if (state.branchesFailed) R.string.branches_unavailable else R.string.text_no_branches_available), stringResource(R.string.hint_default_build_branch), enabled = !state.queuing)
                    Column(Modifier.fillMaxWidth().heightIn(min = 64.dp).clickable(enabled = !state.queuing && !state.agents.isNullOrEmpty(), onClick = onSelectAgent).padding(16.dp).testTag("run-build:agent")) {
                        Text(stringResource(R.string.text_agents), style = MaterialTheme.typography.titleMedium)
                        if (state.agents == null) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(stringResource(R.string.text_loading_agents), style = MaterialTheme.typography.bodyMedium)
                                CircularProgressIndicator(Modifier.padding(start = 16.dp).size(20.dp), strokeWidth = 2.dp)
                            }
                        } else {
                            if (state.agentsFailed) {
                                ErrorNotice(stringResource(R.string.agents_unavailable), modifier = Modifier.padding(top = 8.dp))
                            } else {
                                Text(
                                    if (state.agents.isEmpty()) {
                                        stringResource(R.string.text_no_agents_available)
                                    } else {
                                        request.agent?.name ?: stringResource(R.string.hint_default_filter_agent)
                                    },
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                Card(Modifier.fillMaxWidth().testTag("run-build:options-card").animateContentSize(animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec()), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
                    Column(Modifier.padding(vertical = 16.dp)) {
                        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(stringResource(R.string.run_options), Modifier.weight(1f), style = MaterialTheme.typography.titleLarge)
                            TextButton({ optionsExpanded = !optionsExpanded }, Modifier.testTag("run-build:options"), enabled = !state.queuing) {
                                Text(stringResource(if (optionsExpanded) R.string.hide_options else R.string.edit_options))
                            }
                        }
                        Text(
                            stringResource(
                                R.string.run_options_summary,
                                stringResource(if (request.personal) R.string.option_on else R.string.option_off),
                                stringResource(if (request.queueAtTop) R.string.priority_top else R.string.priority_normal),
                                stringResource(if (request.cleanSources) R.string.option_on else R.string.option_off)
                            ),
                            Modifier.padding(horizontal = 16.dp).testTag("run-build:summary"),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(stringResource(R.string.parameter_count, request.parameters.size), Modifier.padding(horizontal = 16.dp).padding(top = 8.dp).testTag("run-build:parameter-count"), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                        if (optionsExpanded) {
                            TeamCitySwitch(stringResource(R.string.text_switcher_run_as_personal), request.personal, { onChange(request.copy(personal = it)) }, Modifier.testTag("run-build:personal"), !state.queuing, contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp))
                            TeamCitySwitch(stringResource(R.string.text_switcher_run_as_queue_at_the_top), request.queueAtTop, { onChange(request.copy(queueAtTop = it)) }, Modifier.testTag("run-build:top"), !state.queuing, contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp))
                            TeamCitySwitch(stringResource(R.string.text_switcher_run_as_clean_all_files), request.cleanSources, { onChange(request.copy(cleanSources = it)) }, Modifier.testTag("run-build:clean"), !state.queuing, contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp))
                            Column(Modifier.padding(horizontal = 16.dp)) {
                                Text(stringResource(R.string.text_parameters), Modifier.padding(top = 16.dp), style = MaterialTheme.typography.titleMedium)
                                request.parameters.forEach { parameter ->
                                    Text(parameter.name, Modifier.padding(top = 12.dp), style = TeamCityMonospace)
                                    Text(parameter.value, style = TeamCityMonospace, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                FlowRow(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    FilledTonalButton(onAddParameter, enabled = !state.queuing, modifier = Modifier.testTag("run-build:add")) { Text(stringResource(R.string.text_add_parameter)) }
                                    OutlinedButton(onClearParameters, enabled = !state.queuing && request.parameters.isNotEmpty(), modifier = Modifier.testTag("run-build:clear")) { Text(stringResource(R.string.text_clear_parameters)) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    if (state.queuing) {
        AlertDialog(onDismissRequest = {}, modifier = Modifier.testTag("run-build:progress"), shape = MaterialTheme.shapes.large, text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(Modifier.size(48.dp))
                Text(stringResource(R.string.text_queueing_build), Modifier.padding(start = 24.dp))
            }
        }, confirmButton = {})
    }
    if (agentDialog) AlertDialog(onDismissRequest = onDismissDialog, modifier = Modifier.testTag("run-build:agent-dialog"), shape = MaterialTheme.shapes.large, title = { Text(stringResource(R.string.title_agent_chooser_dialog)) }, text = { Column(Modifier.verticalScroll(rememberScrollState())) { state.agents.orEmpty().forEach { agent -> Text(agent.name, Modifier.fillMaxWidth().clickable { onAgentSelected(agent) }.padding(vertical = 16.dp), style = MaterialTheme.typography.bodyLarge) } } }, confirmButton = {})
    parameterDialog?.let { dialog ->
        Dialog(onDismissRequest = onDismissDialog, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            Surface(Modifier.widthIn(max = 560.dp).fillMaxWidth().padding(horizontal = 24.dp).testTag("run-build:parameter-dialog"), shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surface) {
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
                            shape = MaterialTheme.shapes.large
                        )
                        OutlinedTextField(dialog.value, { onParameterChange(dialog.copy(value = it)) }, Modifier.fillMaxWidth().testTag("parameter:value"), label = { Text(stringResource(R.string.hint_parameter_value)) }, singleLine = true, shape = MaterialTheme.shapes.large)
                    }
                    Row(Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.End) {
                        TextButton(onDismissDialog) { Text(stringResource(R.string.text_cancel_button)) }
                        TextButton(onConfirmParameter, Modifier.testTag("parameter:confirm")) { Text(stringResource(R.string.text_add_parameter_button)) }
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
