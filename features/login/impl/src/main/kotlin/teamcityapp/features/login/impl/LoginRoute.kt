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

package teamcityapp.features.login.impl

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.*
import androidx.lifecycle.repeatOnLifecycle
import teamcityapp.features.login.impl.router.LoginRouter

@Composable
fun LoginRoute(router: LoginRouter, viewModel: LoginViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var dialog by rememberSaveable { mutableStateOf(LoginDialog.None) }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LifecycleResumeEffect(viewModel) {
        viewModel.viewed()
        onPauseOrDispose {}
    }
    LaunchedEffect(state.signedIn, lifecycle, router) {
        if (state.signedIn) {
            lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                if (viewModel.consumeSuccess()) router.openProjects()
            }
        }
    }

    LoginScreen(
        state, viewModel::update, viewModel::submit,
        { checked -> if (checked) dialog = LoginDialog.Ssl else viewModel.update(state.form.copy(sslDisabled = false)) },
        {
            viewModel.demoClicked()
            dialog = LoginDialog.Demo
        }, dialog,
        onConfirm = {
            val current = dialog
            dialog = LoginDialog.None
            if (current == LoginDialog.Ssl) viewModel.update(state.form.copy(sslDisabled = true)) else viewModel.tryDemo()
        },
        onDecline = {
            if (dialog == LoginDialog.Demo) viewModel.declineDemo()
            dialog = LoginDialog.None
        },
        onConfirmHttp = viewModel::confirmHttp, onDeclineHttp = viewModel::declineHttp, onDismissUnauthorized = viewModel::dismissUnauthorized
    )
}
