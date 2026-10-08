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

package teamcityapp.features.create_account.impl

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.*
import androidx.lifecycle.repeatOnLifecycle
import teamcityapp.features.create_account.impl.router.CreateAccountRouter

@Composable
fun CreateAccountRoute(router: CreateAccountRouter, viewModel: CreateAccountViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var dialog by rememberSaveable { mutableStateOf(CreateAccountDialog.None) }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LifecycleResumeEffect(viewModel) {
        viewModel.viewed()
        onPauseOrDispose {}
    }
    LaunchedEffect(state.created, lifecycle, router) {
        if (state.created) {
            lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                if (viewModel.consumeSuccess()) router.openProjects()
            }
        }
    }

    val close = {
        if (!state.form.busy) {
            if (state.form.serverUrl.isNotEmpty()) dialog = CreateAccountDialog.Discard else router.close()
        }
    }
    BackHandler(onBack = close)

    CreateAccountScreen(
        state,
        viewModel::update,
        viewModel::submit,
        { checked -> if (checked) dialog = CreateAccountDialog.Ssl else viewModel.update(state.form.copy(sslDisabled = false)) },
        close,
        dialog,
        onConfirm = {
            val current = dialog
            dialog = CreateAccountDialog.None
            if (current == CreateAccountDialog.Ssl) viewModel.update(state.form.copy(sslDisabled = true)) else router.close()
        },
        onDecline = { dialog = CreateAccountDialog.None }
    )
}
