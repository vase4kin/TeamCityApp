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

import androidx.compose.runtime.*
import androidx.lifecycle.compose.*

@Composable
fun SharedLoginRoute(vm: LoginViewModel) {
    LifecycleResumeEffect(vm) {
        vm.viewed()
        onPauseOrDispose {}
    }
    val state by vm.state.collectAsStateWithLifecycle()
    var dialog by remember { mutableStateOf(LoginDialog.None) }
    SharedLoginScreen(
        state, vm::update, vm::submit,
        { disabled -> if (disabled) dialog = LoginDialog.Ssl else vm.update(state.form.copy(sslDisabled = false)) },
        {
            vm.demoClicked()
            dialog = LoginDialog.Demo
        },
        dialog,
        onConfirm = {
            when (dialog) {
                LoginDialog.Ssl -> vm.update(state.form.copy(sslDisabled = true))
                LoginDialog.Demo -> vm.tryDemo()
                LoginDialog.None -> Unit
            }
            dialog = LoginDialog.None
        },
        onDecline = {
            if (dialog == LoginDialog.Demo) vm.declineDemo()
            dialog = LoginDialog.None
        },
        onConfirmHttp = vm::confirmHttp, onDeclineHttp = vm::declineHttp, onDismissUnauthorized = vm::dismissUnauthorized
    )
}
