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

package teamcityapp.features.artifacts.impl

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.collect
import teamcityapp.features.artifacts.api.*
import teamcityapp.features.artifacts.impl.router.ArtifactsRouter

@Composable
internal fun ArtifactsRoute(
    router: ArtifactsRouter,
    showToolbar: Boolean,
    visible: Boolean = true,
    viewModel: ArtifactsViewModel = hiltViewModel()
) {
    val state = if (visible) {
        val collected by viewModel.state.collectAsStateWithLifecycle(minActiveState = Lifecycle.State.RESUMED)
        collected
    } else {
        // Hidden pages retain a snapshot without subscribing or starting work.
        remember(viewModel) { viewModel.state.value }
    }
    var platformError by rememberSaveable { mutableStateOf<ArtifactPlatformError?>(null) }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val hostState by lifecycle.currentStateFlow.collectAsState()
    val presentsDialogs = visible && hostState.isAtLeast(Lifecycle.State.RESUMED)
    val download: (ArtifactDownload) -> Unit = { file ->
        if (router.permission(file) == ArtifactPermission.Allowed) {
            viewModel.download(file)
        } else {
            platformError = ArtifactPlatformError.PermissionDenied
        }
    }
    DisposableEffect(router) { onDispose { router.dispose() } }
    if (visible) {
        LifecycleResumeEffect(viewModel) {
            viewModel.onResumed()
            onPauseOrDispose { router.dispose() }
        }
        LaunchedEffect(router, lifecycle) {
            lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                router.actions.collect { action ->
                    when (action) {
                        is ArtifactAction.Download -> download(action.file)

                        is ArtifactAction.Open -> router.openFolder(action.name, viewModel.build, action.href)

                        is ArtifactAction.Browser -> try {
                            router.openBrowser(viewModel.build, action.href)
                        } catch (_: Exception) {
                            platformError = ArtifactPlatformError.BrowserUnavailable
                        }
                    }
                }
            }
        }
        LifecycleResumeEffect(state.download, viewModel) {
            when (val result = state.download) {
                is ArtifactDownloadState.Ready -> viewModel.claimDownloadedFile(result.token)?.let { file ->
                    try {
                        router.openFile(file)
                    } catch (_: Exception) {
                        platformError = ArtifactPlatformError.FileUnavailable
                    }
                }

                is ArtifactDownloadState.Failed -> if (viewModel.claimDownloadFailure(result.token)) router.downloadFailed()

                else -> Unit
            }
            onPauseOrDispose {}
        }
    }
    ArtifactsScreen(
        // A Compose dialog has its own window, independent of the Fragment pager's visibility.
        if (presentsDialogs) state else state.copy(download = ArtifactDownloadState.Idle),
        showToolbar, platformError.takeIf { presentsDialogs },
        viewModel::refresh, viewModel::retry, router::navigateUp,
        { file ->
            val children = file.childrenHref
            if (file.isFolder && children != null) router.openFolder(file.name, viewModel.build, children) else router.openActions(file)
        },
        router::openActions,
        { (state.download as? ArtifactDownloadState.Failed)?.let { download(it.file) } },
        viewModel::cancelDownload,
        { platformError = null }
    )
}
