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

package teamcityapp.features.build_overview.impl

import androidx.compose.runtime.*
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.collect
import teamcityapp.features.build_overview.api.*
import teamcityapp.features.build_overview.impl.router.BuildOverviewRouter

@Composable
internal fun BuildOverviewRoute(router: BuildOverviewRouter, visible: Boolean, onState: (BuildOverviewUiState) -> Unit, viewModel: BuildOverviewViewModel = hiltViewModel(), onCountRetry: (teamcityapp.features.build_details.api.BuildDetailsSection) -> Unit = {}) {
    val state = if (visible) {
        val collected by viewModel.state.collectAsStateWithLifecycle(minActiveState = Lifecycle.State.RESUMED)
        collected
    } else {
        // Hidden pages retain a snapshot without subscribing or starting work.
        remember(viewModel) { viewModel.state.value }
    }
    val latest by rememberUpdatedState(state)
    val isVisible by rememberUpdatedState(visible)
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(router) { onDispose { router.dispose() } }
    LaunchedEffect(state, visible) { onState(if (visible) state else BuildOverviewUiState()) }
    // Refresh requests must be retained while another Build Details screen is visible.
    LaunchedEffect(router, lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            router.requests.collect { request ->
                if (request == BuildOverviewRequest.Refresh) {
                    viewModel.refresh()
                } else if (isVisible && lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                    latest.build?.let { build ->
                        when (request) {
                            is BuildOverviewRequest.Branch -> router.dispatch(BuildOverviewAction.Configuration, build, request.name)
                            BuildOverviewRequest.Configuration -> router.dispatch(BuildOverviewAction.Configuration, build)
                            BuildOverviewRequest.Project -> router.dispatch(BuildOverviewAction.Project, build)
                            BuildOverviewRequest.Refresh -> Unit
                        }
                    }
                }
            }
        }
    }
    if (visible) {
        LifecycleResumeEffect(state.build, router) {
            state.build?.let {
                router.loaded(it)
                router.resumed(it)
            }
            onPauseOrDispose { router.dispose() }
        }
    }
    BuildOverviewScreen(state, viewModel::refresh, viewModel::retry, router::row, bottomContentPadding = teamcityapp.libraries.theme.LocalFloatingActionClearance.current, onCountRetry = onCountRetry, onSection = router::section, onLog = { latest.build?.let { router.dispatch(BuildOverviewAction.Log, it) } })
}
