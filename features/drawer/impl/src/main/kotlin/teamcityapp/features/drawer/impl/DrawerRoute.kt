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

package teamcityapp.features.drawer.impl
import androidx.compose.runtime.*
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import teamcityapp.features.drawer.impl.router.DrawerRouter
@Composable
fun DrawerRoute(router: DrawerRouter, viewModel: DrawerViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(router) {
        router.attach()
        onDispose { router.detach() }
    }
    LifecycleResumeEffect(viewModel) {
        viewModel.onScreenViewed()
        onPauseOrDispose {}
    }
    SideEffect { router.setInteractionsEnabled(state.canInteract) }
    LaunchedEffect(state.openHome, lifecycle, router) {
        if (state.openHome) {
            lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                if (viewModel.consumeHome()) router.openHome()
            }
        }
    }
    DrawerScreen(
        state, viewModel::select,
        {
            if (state.canInteract) {
                viewModel.onAddAccountClicked()
                router.openAddAccount()
            }
        }, {
            if (state.canInteract) {
                viewModel.onManageAccountsClicked()
                router.openManageAccounts()
            }
        },
        {
            if (state.canInteract) {
                viewModel.onSettingsClicked()
                router.openSettings()
            }
        }, {
            if (state.canInteract) {
                viewModel.onAboutClicked()
                router.openAbout()
            }
        },
        {
            if (state.canInteract) {
                viewModel.onPrivacyClicked()
                router.openPrivacy()
            }
        }, {
            if (state.canInteract) {
                viewModel.onRateClicked()
                router.openRate()
            }
        },
        onRetry = viewModel::retry,
        onRetrySelection = viewModel::retrySelection, onDismissMissing = viewModel::dismissMissing
    )
}
