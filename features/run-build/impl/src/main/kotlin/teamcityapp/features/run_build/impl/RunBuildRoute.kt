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

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.*
import androidx.lifecycle.repeatOnLifecycle
import teamcityapp.features.run_build.impl.router.RunBuildRouter

@Composable
fun RunBuildRoute(router: RunBuildRouter, viewModel: RunBuildViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LifecycleResumeEffect(viewModel) {
        viewModel.viewed()
        onPauseOrDispose {}
    }

    var agentDialog by rememberSaveable { mutableStateOf(false) }
    var parameterDialog by rememberSaveable { mutableStateOf(false) }
    var name by rememberSaveable { mutableStateOf("") }
    var value by rememberSaveable { mutableStateOf("") }
    var invalid by rememberSaveable { mutableStateOf(false) }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(state.queuedHref, lifecycle, router) {
        state.queuedHref?.let { href -> lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) { if (viewModel.consumeSuccess(href)) router.queued(href) } }
    }
    LaunchedEffect(state.queueError) {
        if (state.queueError != null) {
            kotlinx.coroutines.delay(3500)
            viewModel.clearError()
        }
    }
    BackHandler { if (!state.queuing) router.close() }
    RunBuildScreen(
        state, viewModel::update, viewModel::queue, { if (!state.queuing) router.close() }, { agentDialog = true }, {
            viewModel.addingParameter()
            parameterDialog = true
        }, viewModel::clearParameters,
        agentDialog, if (parameterDialog) ParameterDialogState(name, value, invalid) else null,
        onDismissDialog = {
            agentDialog = false
            parameterDialog = false
        },
        onAgentSelected = {
            viewModel.update(state.request.copy(agent = it))
            agentDialog = false
        },
        onParameterChange = {
            name = it.name
            value = it.value
            invalid = it.invalid
        },
        onConfirmParameter = {
            if (viewModel.addParameter(name, value)) {
                parameterDialog = false
                name = ""
                value = ""
                invalid = false
            } else {
                invalid = true
            }
        }
    )
}
