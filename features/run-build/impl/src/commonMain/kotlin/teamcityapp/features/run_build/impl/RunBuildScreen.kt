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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.flow.drop
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import teamcityapp.features.run_build.api.*
import teamcityapp.features.run_build.impl.resources.*
import teamcityapp.libraries.theme.*

data class ParameterDialogState(val name: String = "", val value: String = "", val invalid: Boolean = false)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun RunBuildScreen(state: RunBuildUiState, onChange: (BuildRequest) -> Unit, onQueue: () -> Unit, onClose: () -> Unit, onSelectAgent: () -> Unit, onAddParameter: () -> Unit, onClearParameters: () -> Unit, agentDialog: Boolean = false, parameterDialog: ParameterDialogState? = null, onDismissDialog: () -> Unit = {}, onAgentSelected: (BuildAgent) -> Unit = {}, onParameterChange: (ParameterDialogState) -> Unit = {}, onConfirmParameter: () -> Unit = {}, snackbar: SnackbarHostState = remember { SnackbarHostState() }) {
    val request = state.request
    var optionsExpanded by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(parameterDialog?.invalid) {
        if (parameterDialog?.invalid == true) optionsExpanded = true
    }
    val scrollState = rememberScrollState()
    SharedTeamCityScreen(stringResource(Res.string.title_run_build), onClose, bottomBar = {
        TeamCityBottomActionSurface(scrollState.canScrollForward, Modifier.testTag("run-build:bottom-action")) {
            TeamCityLoadingButton(onQueue, stringResource(Res.string.title_run_build), stringResource(state.queueLabelRes), state.queuing, Modifier.fillMaxWidth().padding(TeamCityDimensions.contentPadding).heightIn(min = TeamCityDimensions.controlMinHeight).testTag("run-build:submit"), icon = { Icon(painterResource(Res.drawable.ic_directions_run_white_24px), null) })
        }
    }, snackbarHost = { SnackbarHost(snackbar, Modifier.testTag("run-build:snackbar")) }) { modifier ->
        Column(modifier) {
            Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(scrollState).testTag("run-build:scroll").padding(TeamCityDimensions.contentPadding), verticalArrangement = Arrangement.spacedBy(TeamCityDimensions.contentPadding)) {
                Text(stringResource(Res.string.quick_setup), style = MaterialTheme.typography.headlineMedium)
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
                    BranchField(state.branches, state.branchesFailed, request.branch, { onChange(request.copy(branch = it)) }, stringResource(Res.string.text_build_branch), stringResource(Res.string.text_loading_branches), stringResource(state.branchesMessageRes), stringResource(Res.string.hint_default_build_branch), enabled = !state.queuing)
                    Column(Modifier.fillMaxWidth().heightIn(min = TeamCityDimensions.selectionRowMinHeight).clickable(enabled = !state.queuing && !state.agents.isNullOrEmpty(), onClick = onSelectAgent).padding(TeamCityDimensions.contentPadding).testTag("run-build:agent")) {
                        Text(stringResource(Res.string.text_agents), style = MaterialTheme.typography.titleMedium)
                        if (state.agents == null) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(stringResource(Res.string.text_loading_agents), style = MaterialTheme.typography.bodyMedium)
                                TeamCityLoadingIndicator(Modifier.padding(start = TeamCityDimensions.contentPadding).size(TeamCityDimensions.compactProgressIndicatorSize))
                            }
                        } else {
                            if (state.agentsFailed) {
                                SharedErrorNotice(stringResource(Res.string.agents_unavailable), modifier = Modifier.padding(top = TeamCityDimensions.smallSpacing))
                            } else {
                                Text(
                                    state.selectedAgentName ?: stringResource(state.agentFallbackRes),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                Card(Modifier.fillMaxWidth().testTag("run-build:options-card").animateContentSize(animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec()), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
                    Column(Modifier.padding(vertical = TeamCityDimensions.contentPadding)) {
                        Row(Modifier.fillMaxWidth().padding(horizontal = TeamCityDimensions.contentPadding), verticalAlignment = Alignment.CenterVertically) {
                            Text(stringResource(Res.string.run_options), Modifier.weight(1f), style = MaterialTheme.typography.titleLarge)
                            TextButton({ optionsExpanded = !optionsExpanded }, Modifier.testTag("run-build:options"), enabled = !state.queuing) {
                                Text(stringResource(if (optionsExpanded) Res.string.hide_options else Res.string.edit_options))
                            }
                        }
                        Text(
                            stringResource(
                                Res.string.run_options_summary,
                                stringResource(state.personalLabelRes),
                                stringResource(state.priorityLabelRes),
                                stringResource(state.cleanSourcesLabelRes)
                            ),
                            Modifier.padding(horizontal = TeamCityDimensions.contentPadding).testTag("run-build:summary"),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(stringResource(Res.string.parameter_count, request.parameters.size), Modifier.padding(horizontal = TeamCityDimensions.contentPadding).padding(top = TeamCityDimensions.smallSpacing).testTag("run-build:parameter-count"), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                        if (optionsExpanded) {
                            TeamCitySwitch(stringResource(Res.string.text_switcher_run_as_personal), request.personal, { onChange(request.copy(personal = it)) }, Modifier.testTag("run-build:personal"), !state.queuing, contentPadding = PaddingValues(horizontal = TeamCityDimensions.contentPadding, vertical = TeamCityDimensions.smallSpacing))
                            TeamCitySwitch(stringResource(Res.string.text_switcher_run_as_queue_at_the_top), request.queueAtTop, { onChange(request.copy(queueAtTop = it)) }, Modifier.testTag("run-build:top"), !state.queuing, contentPadding = PaddingValues(horizontal = TeamCityDimensions.contentPadding, vertical = TeamCityDimensions.smallSpacing))
                            TeamCitySwitch(stringResource(Res.string.text_switcher_run_as_clean_all_files), request.cleanSources, { onChange(request.copy(cleanSources = it)) }, Modifier.testTag("run-build:clean"), !state.queuing, contentPadding = PaddingValues(horizontal = TeamCityDimensions.contentPadding, vertical = TeamCityDimensions.smallSpacing))
                            Column(Modifier.padding(horizontal = TeamCityDimensions.contentPadding)) {
                                Text(stringResource(Res.string.text_parameters), Modifier.padding(top = TeamCityDimensions.contentPadding), style = MaterialTheme.typography.titleMedium)
                                request.parameters.forEach { parameter ->
                                    Text(parameter.name, Modifier.padding(top = TeamCityDimensions.mediumSpacing), style = TeamCityMonospace)
                                    Text(parameter.value, style = TeamCityMonospace, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                FlowRow(Modifier.padding(top = TeamCityDimensions.smallSpacing), horizontalArrangement = Arrangement.spacedBy(TeamCityDimensions.mediumSpacing)) {
                                    FilledTonalButton(onAddParameter, enabled = !state.queuing, modifier = Modifier.testTag("run-build:add")) { Text(stringResource(Res.string.text_add_parameter)) }
                                    OutlinedButton(onClearParameters, enabled = !state.queuing && request.parameters.isNotEmpty(), modifier = Modifier.testTag("run-build:clear")) { Text(stringResource(Res.string.text_clear_parameters)) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    if (agentDialog) AlertDialog(onDismissRequest = onDismissDialog, modifier = Modifier.testTag("run-build:agent-dialog"), shape = MaterialTheme.shapes.large, title = { Text(stringResource(Res.string.title_agent_chooser_dialog)) }, text = { Column(Modifier.verticalScroll(rememberScrollState())) { state.agents.orEmpty().forEach { agent -> Text(agent.name, Modifier.fillMaxWidth().clickable { onAgentSelected(agent) }.padding(vertical = TeamCityDimensions.contentPadding).testTag("run-build:agent:${agent.id}"), style = MaterialTheme.typography.bodyLarge) } } }, confirmButton = {})
    parameterDialog?.let { dialog ->
        // Native editing owns text, selection and composition atomically. Hoisting only
        // strings through a recomposing dialog can reset the platform caret.
        val nameField = rememberTextFieldState(dialog.name)
        val valueField = rememberTextFieldState(dialog.value)
        val currentDialog by rememberUpdatedState(dialog)
        val changeParameter by rememberUpdatedState(onParameterChange)
        LaunchedEffect(nameField, valueField) {
            snapshotFlow { nameField.text.toString() to valueField.text.toString() }.drop(1).collect { (name, value) ->
                changeParameter(currentDialog.copy(name = name, value = value, invalid = currentDialog.invalid && name == currentDialog.name))
            }
        }
        Dialog(onDismissRequest = onDismissDialog, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            Surface(Modifier.widthIn(max = TeamCityDimensions.formMaxWidth).fillMaxWidth().padding(horizontal = TeamCityDimensions.sectionSpacing).testTag("run-build:parameter-dialog"), shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surface) {
                Column(Modifier.padding(TeamCityDimensions.sectionSpacing)) {
                    Text(stringResource(Res.string.title_add_parameter), style = MaterialTheme.typography.headlineSmall)
                    Column(Modifier.fillMaxWidth().padding(top = TeamCityDimensions.contentPadding).heightIn(max = ParameterListMaxHeight).verticalScroll(rememberScrollState())) {
                        OutlinedTextField(
                            state = nameField,
                            modifier = Modifier.fillMaxWidth().testTag("parameter:name"),
                            label = { Text(stringResource(Res.string.hint_parameter_name)) },
                            lineLimits = TextFieldLineLimits.SingleLine,
                            keyboardOptions = KeyboardOptions(autoCorrectEnabled = false, capitalization = KeyboardCapitalization.None),
                            isError = dialog.invalid,
                            supportingText = if (dialog.invalid) {
                                { Text(stringResource(Res.string.text_error_parameter_name)) }
                            } else {
                                null
                            },
                            shape = MaterialTheme.shapes.large
                        )
                        OutlinedTextField(state = valueField, modifier = Modifier.fillMaxWidth().testTag("parameter:value"), label = { Text(stringResource(Res.string.hint_parameter_value)) }, lineLimits = TextFieldLineLimits.SingleLine, keyboardOptions = KeyboardOptions(autoCorrectEnabled = false, capitalization = KeyboardCapitalization.None), shape = MaterialTheme.shapes.large)
                    }
                    Row(Modifier.fillMaxWidth().padding(top = TeamCityDimensions.contentPadding), horizontalArrangement = Arrangement.End) {
                        TextButton(onDismissDialog) { Text(stringResource(Res.string.text_cancel_button)) }
                        TextButton({
                            onParameterChange(dialog.copy(name = nameField.text.toString(), value = valueField.text.toString()))
                            onConfirmParameter()
                        }, Modifier.testTag("parameter:confirm")) { Text(stringResource(Res.string.text_add_parameter_button)) }
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

private val ParameterListMaxHeight = 360.dp
